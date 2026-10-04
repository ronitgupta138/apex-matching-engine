package com.tradematching.service;

import com.tradematching.model.MarketDepth;
import com.tradematching.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(MarketBroadcaster.class);
    private final SimpMessagingTemplate messagingTemplate;

    public MarketBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastDepth(MarketDepth depth) {
        try {
            messagingTemplate.convertAndSend("/topic/depth/" + depth.getSymbol(), depth);
        } catch (Exception e) {
            log.debug("WebSocket broadcast skipped: {}", e.getMessage());
        }
    }

    public void broadcastTrades(String symbol, List<Trade> trades) {
        for (Trade trade : trades) {
            try {
                messagingTemplate.convertAndSend("/topic/trades/" + symbol, trade);
            } catch (Exception e) {
                log.debug("WebSocket trade broadcast skipped: {}", e.getMessage());
            }
        }
    }
}
