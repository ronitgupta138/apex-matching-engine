package com.tradematching.service;

import com.tradematching.dto.OrderRequest;
import com.tradematching.dto.OrderResponse;
import com.tradematching.engine.OrderBook;
import com.tradematching.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngineService {

    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final MarketBroadcaster broadcaster;

    public MatchingEngineService(MarketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        // Pre-initialize top market pairs
        List.of("BTC-USD", "ETH-USD", "AAPL", "TSLA", "NVDA").forEach(this::getOrCreateOrderBook);
    }

    public OrderBook getOrCreateOrderBook(String symbol) {
        return books.computeIfAbsent(symbol.toUpperCase().trim(), OrderBook::new);
    }

    public OrderResponse placeOrder(OrderRequest req) {
        OrderBook book = getOrCreateOrderBook(req.getSymbol());

        Order order = new Order(
                req.getSymbol(),
                req.getUserId(),
                req.getSide(),
                req.getType(),
                req.getPrice(),
                req.getQuantity()
        );

        List<Trade> trades = book.processOrder(order);

        // Real-time broadcast to WebSockets
        MarketDepth depth = book.getMarketDepth(15);
        broadcaster.broadcastDepth(depth);
        if (!trades.isEmpty()) {
            broadcaster.broadcastTrades(order.getSymbol(), trades);
        }

        return new OrderResponse(order, trades);
    }

    public boolean cancelOrder(String symbol, Long orderId) {
        OrderBook book = books.get(symbol.toUpperCase().trim());
        if (book != null) {
            boolean cancelled = book.cancelOrder(orderId);
            if (cancelled) {
                broadcaster.broadcastDepth(book.getMarketDepth(15));
            }
            return cancelled;
        }
        return false;
    }

    public Optional<Order> getOrder(String symbol, Long orderId) {
        OrderBook book = books.get(symbol.toUpperCase().trim());
        if (book != null) {
            return book.getOrder(orderId);
        }
        return Optional.empty();
    }

    public MarketDepth getMarketDepth(String symbol, int depthLimit) {
        OrderBook book = getOrCreateOrderBook(symbol);
        return book.getMarketDepth(depthLimit);
    }

    public List<Trade> getRecentTrades(String symbol, int limit) {
        OrderBook book = getOrCreateOrderBook(symbol);
        return book.getRecentTrades(limit);
    }

    public Set<String> getActiveSymbols() {
        return books.keySet();
    }
}
