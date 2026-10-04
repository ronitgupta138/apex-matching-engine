package com.tradematching.simulator;

import com.tradematching.dto.OrderRequest;
import com.tradematching.model.OrderSide;
import com.tradematching.model.OrderType;
import com.tradematching.service.MatchingEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "trade.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class MarketSimulationBot {

    private static final Logger log = LoggerFactory.getLogger(MarketSimulationBot.class);
    private final MatchingEngineService matchingEngine;
    private final Random random = new Random();

    public MarketSimulationBot(MatchingEngineService matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    // Runs every 4 seconds to inject organic liquidity and execute mock trades
    @Scheduled(fixedRate = 4000)
    public void generateOrganicLiquidity() {
        String symbol = "BTC-USD";
        double baseMidPrice = 64500.0;
        double spread = 5.0 + random.nextDouble() * 10.0;

        double bidPrice = baseMidPrice - (spread / 2.0) - (random.nextInt(15));
        double askPrice = baseMidPrice + (spread / 2.0) + (random.nextInt(15));

        long bidQty = 1 + random.nextInt(5);
        long askQty = 1 + random.nextInt(5);

        // Place resting Bid
        OrderRequest bidOrder = new OrderRequest();
        bidOrder.setSymbol(symbol);
        bidOrder.setUserId("MM-BOT-01");
        bidOrder.setSide(OrderSide.BUY);
        bidOrder.setType(OrderType.LIMIT);
        bidOrder.setPrice(BigDecimal.valueOf(bidPrice).setScale(2, RoundingMode.HALF_UP));
        bidOrder.setQuantity(bidQty);
        matchingEngine.placeOrder(bidOrder);

        // Place resting Ask
        OrderRequest askOrder = new OrderRequest();
        askOrder.setSymbol(symbol);
        askOrder.setUserId("MM-BOT-02");
        askOrder.setSide(OrderSide.SELL);
        askOrder.setType(OrderType.LIMIT);
        askOrder.setPrice(BigDecimal.valueOf(askPrice).setScale(2, RoundingMode.HALF_UP));
        askOrder.setQuantity(askQty);
        matchingEngine.placeOrder(askOrder);

        // Randomly execute a market trade (20% probability)
        if (random.nextDouble() < 0.20) {
            OrderRequest marketOrder = new OrderRequest();
            marketOrder.setSymbol(symbol);
            marketOrder.setUserId("RETAIL-TRADER-" + random.nextInt(100));
            marketOrder.setSide(random.nextBoolean() ? OrderSide.BUY : OrderSide.SELL);
            marketOrder.setType(OrderType.MARKET);
            marketOrder.setQuantity(1L);
            matchingEngine.placeOrder(marketOrder);
            log.debug("Simulated market order executed on {}", symbol);
        }
    }
}
