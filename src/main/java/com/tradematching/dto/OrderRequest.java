package com.tradematching.dto;

import com.tradematching.model.OrderSide;
import com.tradematching.model.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Order side (BUY/SELL) is required")
    private OrderSide side;

    @NotNull(message = "Order type (LIMIT/MARKET) is required")
    private OrderType type;

    private BigDecimal price; // required for LIMIT orders

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    public OrderRequest() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
}
