package com.tradematching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradematching.dto.OrderRequest;
import com.tradematching.model.OrderSide;
import com.tradematching.model.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Trade-Matching-Engine"));
    }

    @Test
    void testSubmitAndCancelOrderFlow() throws Exception {
        OrderRequest req = new OrderRequest();
        req.setSymbol("ETH-USD");
        req.setUserId("TRADER-99");
        req.setSide(OrderSide.BUY);
        req.setType(OrderType.LIMIT);
        req.setPrice(BigDecimal.valueOf(3250.50));
        req.setQuantity(5L);

        // 1. Submit Order
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andExpect(jsonPath("$.data.symbol").value("ETH-USD"))
                .andReturn().getResponse().getContentAsString();

        // 2. Fetch Symbols
        mockMvc.perform(get("/api/market/symbols"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Fetch Market Depth
        mockMvc.perform(get("/api/market/depth/ETH-USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.symbol").value("ETH-USD"));
    }
}
