package com.tradematching.dto;

import com.tradematching.model.Order;
import com.tradematching.model.OrderSide;
import com.tradematching.model.OrderStatus;
import com.tradematching.model.OrderType;
import com.tradematching.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long orderId;
    private String symbol;
    private String userId;
    private OrderSide side;
    private OrderType type;
    private BigDecimal price;
    private long originalQuantity;
    private long remainingQuantity;
    private long filledQuantity;
    private OrderStatus status;
    private Instant createdAt;
    private List<Trade> executedTrades;

    public OrderResponse(Order order, List<Trade> executedTrades) {
        this.orderId = order.getId();
        this.symbol = order.getSymbol();
        this.userId = order.getUserId();
        this.side = order.getSide();
        this.type = order.getType();
        this.price = order.getPrice();
        this.originalQuantity = order.getQuantity();
        this.remainingQuantity = order.getRemainingQuantity();
        this.filledQuantity = order.getFilledQuantity();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.executedTrades = executedTrades;
    }

    public Long getOrderId() { return orderId; }
    public String getSymbol() { return symbol; }
    public String getUserId() { return userId; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public long getOriginalQuantity() { return originalQuantity; }
    public long getRemainingQuantity() { return remainingQuantity; }
    public long getFilledQuantity() { return filledQuantity; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Trade> getExecutedTrades() { return executedTrades; }
}
