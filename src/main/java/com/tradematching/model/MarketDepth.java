package com.tradematching.model;

import java.time.Instant;
import java.util.List;

public class MarketDepth {

    private final String symbol;
    private final List<PriceLevel> bids;
    private final List<PriceLevel> asks;
    private final Instant timestamp;

    public MarketDepth(String symbol, List<PriceLevel> bids, List<PriceLevel> asks) {
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
        this.timestamp = Instant.now();
    }

    public String getSymbol() { return symbol; }
    public List<PriceLevel> getBids() { return bids; }
    public List<PriceLevel> getAsks() { return asks; }
    public Instant getTimestamp() { return timestamp; }
}
