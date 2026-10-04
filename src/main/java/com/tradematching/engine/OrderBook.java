package com.tradematching.engine;

import com.tradematching.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBook {

    private final String symbol;
    private final ReentrantLock lock = new ReentrantLock(true);

    // Bids: Highest price first (descending)
    private final NavigableMap<BigDecimal, ArrayDeque<Order>> bids = new TreeMap<>(Collections.reverseOrder());

    // Asks: Lowest price first (ascending)
    private final NavigableMap<BigDecimal, ArrayDeque<Order>> asks = new TreeMap<>();

    // Quick lookup for orders: ID -> Order
    private final Map<Long, Order> orderIndex = new ConcurrentHashMap<>();

    // Execution history buffer
    private final List<Trade> tradeHistory = Collections.synchronizedList(new ArrayList<>());

    public OrderBook(String symbol) {
        this.symbol = symbol.toUpperCase().trim();
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Submit an incoming order to the matching engine.
     * Evaluates resting liquidity, executes trades, and rests remaining quantity if LIMIT order.
     */
    public List<Trade> processOrder(Order incomingOrder) {
        lock.lock();
        try {
            List<Trade> executedTrades = new ArrayList<>();
            orderIndex.put(incomingOrder.getId(), incomingOrder);

            if (incomingOrder.getSide() == OrderSide.BUY) {
                matchBuyOrder(incomingOrder, executedTrades);
            } else {
                matchSellOrder(incomingOrder, executedTrades);
            }

            tradeHistory.addAll(executedTrades);
            return executedTrades;
        } finally {
            lock.unlock();
        }
    }

    private void matchBuyOrder(Order buyOrder, List<Trade> trades) {
        Iterator<Map.Entry<BigDecimal, ArrayDeque<Order>>> askIterator = asks.entrySet().iterator();

        while (askIterator.hasNext() && !buyOrder.isFilled()) {
            Map.Entry<BigDecimal, ArrayDeque<Order>> entry = askIterator.next();
            BigDecimal bestAskPrice = entry.getKey();
            ArrayDeque<Order> restingOrders = entry.getValue();

            // For Limit orders: stop matching if best ask is higher than buy price limit
            if (buyOrder.getType() == OrderType.LIMIT && bestAskPrice.compareTo(buyOrder.getPrice()) > 0) {
                break;
            }

            Iterator<Order> queueIterator = restingOrders.iterator();
            while (queueIterator.hasNext() && !buyOrder.isFilled()) {
                Order restingSell = queueIterator.next();

                long matchQty = Math.min(buyOrder.getRemainingQuantity(), restingSell.getRemainingQuantity());
                BigDecimal executionPrice = restingSell.getPrice(); // Maker sets execution price

                buyOrder.fill(matchQty);
                restingSell.fill(matchQty);

                Trade trade = new Trade(
                        symbol,
                        buyOrder.getId(),
                        restingSell.getId(),
                        buyOrder.getUserId(),
                        restingSell.getUserId(),
                        executionPrice,
                        matchQty
                );
                trades.add(trade);

                if (restingSell.isFilled()) {
                    queueIterator.remove();
                }
            }

            if (restingOrders.isEmpty()) {
                askIterator.remove();
            }
        }

        // Rest remaining Limit order in the bid book
        if (!buyOrder.isFilled() && buyOrder.getType() == OrderType.LIMIT) {
            bids.computeIfAbsent(buyOrder.getPrice(), k -> new ArrayDeque<>()).add(buyOrder);
        } else if (!buyOrder.isFilled() && buyOrder.getType() == OrderType.MARKET) {
            buyOrder.setStatus(OrderStatus.CANCELLED); // Market order expires unfilled portion
        }
    }

    private void matchSellOrder(Order sellOrder, List<Trade> trades) {
        Iterator<Map.Entry<BigDecimal, ArrayDeque<Order>>> bidIterator = bids.entrySet().iterator();

        while (bidIterator.hasNext() && !sellOrder.isFilled()) {
            Map.Entry<BigDecimal, ArrayDeque<Order>> entry = bidIterator.next();
            BigDecimal bestBidPrice = entry.getKey();
            ArrayDeque<Order> restingOrders = entry.getValue();

            // For Limit orders: stop matching if best bid is lower than sell price limit
            if (sellOrder.getType() == OrderType.LIMIT && bestBidPrice.compareTo(sellOrder.getPrice()) < 0) {
                break;
            }

            Iterator<Order> queueIterator = restingOrders.iterator();
            while (queueIterator.hasNext() && !sellOrder.isFilled()) {
                Order restingBuy = queueIterator.next();

                long matchQty = Math.min(sellOrder.getRemainingQuantity(), restingBuy.getRemainingQuantity());
                BigDecimal executionPrice = restingBuy.getPrice(); // Maker sets execution price

                sellOrder.fill(matchQty);
                restingBuy.fill(matchQty);

                Trade trade = new Trade(
                        symbol,
                        restingBuy.getId(),
                        sellOrder.getId(),
                        restingBuy.getUserId(),
                        sellOrder.getUserId(),
                        executionPrice,
                        matchQty
                );
                trades.add(trade);

                if (restingBuy.isFilled()) {
                    queueIterator.remove();
                }
            }

            if (restingOrders.isEmpty()) {
                bidIterator.remove();
            }
        }

        // Rest remaining Limit order in the ask book
        if (!sellOrder.isFilled() && sellOrder.getType() == OrderType.LIMIT) {
            asks.computeIfAbsent(sellOrder.getPrice(), k -> new ArrayDeque<>()).add(sellOrder);
        } else if (!sellOrder.isFilled() && sellOrder.getType() == OrderType.MARKET) {
            sellOrder.setStatus(OrderStatus.CANCELLED);
        }
    }

    /**
     * Cancel an active resting order in the book.
     */
    public boolean cancelOrder(Long orderId) {
        lock.lock();
        try {
            Order order = orderIndex.get(orderId);
            if (order == null || order.isFilled() || order.getStatus() == OrderStatus.CANCELLED) {
                return false;
            }

            order.setStatus(OrderStatus.CANCELLED);
            BigDecimal price = order.getPrice();

            if (order.getSide() == OrderSide.BUY) {
                ArrayDeque<Order> queue = bids.get(price);
                if (queue != null) {
                    queue.remove(order);
                    if (queue.isEmpty()) bids.remove(price);
                }
            } else {
                ArrayDeque<Order> queue = asks.get(price);
                if (queue != null) {
                    queue.remove(order);
                    if (queue.isEmpty()) asks.remove(price);
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public Optional<Order> getOrder(Long orderId) {
        return Optional.ofNullable(orderIndex.get(orderId));
    }

    /**
     * Generate Level 2 (L2) aggregated market depth snapshot.
     */
    public MarketDepth getMarketDepth(int depthLimit) {
        lock.lock();
        try {
            List<PriceLevel> bidLevels = new ArrayList<>();
            for (Map.Entry<BigDecimal, ArrayDeque<Order>> entry : bids.entrySet()) {
                if (bidLevels.size() >= depthLimit) break;
                long totalVol = entry.getValue().stream().mapToLong(Order::getRemainingQuantity).sum();
                bidLevels.add(new PriceLevel(entry.getKey(), totalVol, entry.getValue().size()));
            }

            List<PriceLevel> askLevels = new ArrayList<>();
            for (Map.Entry<BigDecimal, ArrayDeque<Order>> entry : asks.entrySet()) {
                if (askLevels.size() >= depthLimit) break;
                long totalVol = entry.getValue().stream().mapToLong(Order::getRemainingQuantity).sum();
                askLevels.add(new PriceLevel(entry.getKey(), totalVol, entry.getValue().size()));
            }

            return new MarketDepth(symbol, bidLevels, askLevels);
        } finally {
            lock.unlock();
        }
    }

    public List<Trade> getRecentTrades(int limit) {
        synchronized (tradeHistory) {
            int size = tradeHistory.size();
            int start = Math.max(0, size - limit);
            List<Trade> subList = new ArrayList<>(tradeHistory.subList(start, size));
            Collections.reverse(subList);
            return subList;
        }
    }
}
