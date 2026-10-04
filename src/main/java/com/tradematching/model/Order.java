package com.tradematching.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Order {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1000);

    private final Long id;
    private final String symbol;
    private final String userId;
    private final OrderSide side;
    private final OrderType type;
    private BigDecimal price; // null for pure market orders if unpriced
    private long quantity;
    private long remainingQuantity;
    private OrderStatus status;
    private final Instant createdAt;

    public Order(String symbol, String userId, OrderSide side, OrderType type, BigDecimal price, long quantity) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.symbol = symbol.toUpperCase().trim();
        this.userId = userId;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
        this.createdAt = Instant.now();
    }

    public Order(Long id, String symbol, String userId, OrderSide side, OrderType type, BigDecimal price, long quantity) {
        this.id = id;
        this.symbol = symbol.toUpperCase().trim();
        this.userId = userId;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getUserId() { return userId; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public long getQuantity() { return quantity; }
    public long getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(long remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }

    public long getFilledQuantity() {
        return quantity - remainingQuantity;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }

    public void fill(long filledQty) {
        if (filledQty > remainingQuantity) {
            throw new IllegalArgumentException("Filled quantity exceeds remaining quantity");
        }
        this.remainingQuantity -= filledQty;
        if (this.remainingQuantity == 0) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }
}
