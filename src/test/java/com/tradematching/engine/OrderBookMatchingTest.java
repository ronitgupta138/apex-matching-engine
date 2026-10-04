package com.tradematching.engine;

import com.tradematching.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookMatchingTest {

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("BTC-USD");
    }

    @Test
    void testExactLimitMatch() {
        // 1. Resting Sell: 10 BTC @ $60,000
        Order sell = new Order("BTC-USD", "SELLER-1", OrderSide.SELL, OrderType.LIMIT,
                BigDecimal.valueOf(60000), 10);
        List<Trade> trades1 = orderBook.processOrder(sell);
        assertTrue(trades1.isEmpty(), "No trade should occur for first resting order");
        assertEquals(OrderStatus.NEW, sell.getStatus());

        // 2. Incoming Buy: 10 BTC @ $60,000
        Order buy = new Order("BTC-USD", "BUYER-1", OrderSide.BUY, OrderType.LIMIT,
                BigDecimal.valueOf(60000), 10);
        List<Trade> trades2 = orderBook.processOrder(buy);

        assertEquals(1, trades2.size(), "Should produce exactly 1 trade");
        Trade trade = trades2.get(0);
        assertEquals(BigDecimal.valueOf(60000), trade.getPrice());
        assertEquals(10, trade.getQuantity());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
    }

    @Test
    void testPartialFillsAndPriceTimePriority() {
        // Place resting asks at different price levels
        Order ask1 = new Order("BTC-USD", "SELLER-A", OrderSide.SELL, OrderType.LIMIT, BigDecimal.valueOf(100), 5);
        Order ask2 = new Order("BTC-USD", "SELLER-B", OrderSide.SELL, OrderType.LIMIT, BigDecimal.valueOf(100), 5);
        Order ask3 = new Order("BTC-USD", "SELLER-C", OrderSide.SELL, OrderType.LIMIT, BigDecimal.valueOf(105), 10);

        orderBook.processOrder(ask1);
        orderBook.processOrder(ask2);
        orderBook.processOrder(ask3);

        // Incoming large aggressive Buy: 15 shares @ $110
        Order aggressiveBuy = new Order("BTC-USD", "WHALE-BUYER", OrderSide.BUY, OrderType.LIMIT, BigDecimal.valueOf(110), 15);
        List<Trade> trades = orderBook.processOrder(aggressiveBuy);

        assertEquals(3, trades.size(), "Should sweep ask1, ask2, and partial fill ask3");

        // First trade: ask1 (FIFO time priority @ 100)
        assertEquals(5, trades.get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(100), trades.get(0).getPrice());
        assertEquals(ask1.getId(), trades.get(0).getSellOrderId());

        // Second trade: ask2 (FIFO time priority @ 100)
        assertEquals(5, trades.get(1).getQuantity());
        assertEquals(BigDecimal.valueOf(100), trades.get(1).getPrice());
        assertEquals(ask2.getId(), trades.get(1).getSellOrderId());

        // Third trade: ask3 (next price level @ 105)
        assertEquals(5, trades.get(2).getQuantity());
        assertEquals(BigDecimal.valueOf(105), trades.get(2).getPrice());
        assertEquals(ask3.getId(), trades.get(2).getSellOrderId());

        assertEquals(OrderStatus.FILLED, aggressiveBuy.getStatus());
        assertEquals(5, ask3.getRemainingQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, ask3.getStatus());
    }

    @Test
    void testOrderCancellation() {
        Order sell = new Order("BTC-USD", "SELLER-1", OrderSide.SELL, OrderType.LIMIT, BigDecimal.valueOf(50000), 10);
        orderBook.processOrder(sell);

        MarketDepth depthBefore = orderBook.getMarketDepth(5);
        assertEquals(1, depthBefore.getAsks().size());

        boolean cancelled = orderBook.cancelOrder(sell.getId());
        assertTrue(cancelled);
        assertEquals(OrderStatus.CANCELLED, sell.getStatus());

        MarketDepth depthAfter = orderBook.getMarketDepth(5);
        assertEquals(0, depthAfter.getAsks().size(), "Book should be empty after cancellation");
    }

    @Test
    void testMarketOrderExecution() {
        // Seed book with asks
        orderBook.processOrder(new Order("BTC-USD", "SELLER-1", OrderSide.SELL, OrderType.LIMIT, BigDecimal.valueOf(200), 20));

        // Market Buy for 8 shares (no price limit)
        Order marketBuy = new Order("BTC-USD", "BUYER-1", OrderSide.BUY, OrderType.MARKET, null, 8);
        List<Trade> trades = orderBook.processOrder(marketBuy);

        assertEquals(1, trades.size());
        assertEquals(8, trades.get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(200), trades.get(0).getPrice());
        assertEquals(OrderStatus.FILLED, marketBuy.getStatus());
    }
}
