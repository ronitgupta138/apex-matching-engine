package com.tradematching.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Trade {

    private static final AtomicLong TRADE_ID_GENERATOR = new AtomicLong(5000);

    private final Long tradeId;
    private final String symbol;
    private final Long buyOrderId;
    private final Long sellOrderId;
    private final String buyerUserId;
    private final String sellerUserId;
    private final BigDecimal price;
    private final long quantity;
    private final Instant executedAt;

    public Trade(String symbol, Long buyOrderId, Long sellOrderId,
                 String buyerUserId, String sellerUserId, BigDecimal price, long quantity) {
        this.tradeId = TRADE_ID_GENERATOR.incrementAndGet();
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = Instant.now();
    }

    public Long getTradeId() { return tradeId; }
    public String getSymbol() { return symbol; }
    public Long getBuyOrderId() { return buyOrderId; }
    public Long getSellOrderId() { return sellOrderId; }
    public String getBuyerUserId() { return buyerUserId; }
    public String getSellerUserId() { return sellerUserId; }
    public BigDecimal getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public Instant getExecutedAt() { return executedAt; }
}
