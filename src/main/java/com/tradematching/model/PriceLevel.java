package com.tradematching.model;

import java.math.BigDecimal;

public class PriceLevel {

    private final BigDecimal price;
    private final long totalVolume;
    private final int orderCount;

    public PriceLevel(BigDecimal price, long totalVolume, int orderCount) {
        this.price = price;
        this.totalVolume = totalVolume;
        this.orderCount = orderCount;
    }

    public BigDecimal getPrice() { return price; }
    public long getTotalVolume() { return totalVolume; }
    public int getOrderCount() { return orderCount; }
}
