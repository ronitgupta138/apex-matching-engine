<div align="center">

# ⚡ Trade Matching Engine

**Ultra-Low Latency In-Memory Continuous Double Auction Order Matching Engine & Real-Time Market Data Streamer**

[![Java](https://img.shields.io/badge/Java-17-0891b2?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-0891b2?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![WebSockets](https://img.shields.io/badge/WebSockets-STOMP-0891b2?style=flat-square&logo=socketdotio&logoColor=white)](https://spring.io/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-0891b2?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![CI](https://img.shields.io/badge/CI-GitHub_Actions-0891b2?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/ronitgupta138/trade-matching-engine/actions)

</div>

---

## 📌 Architectural Overview

Trade Matching Engine is a high-performance, in-memory **Continuous Double Auction Order Book** implementing strict **Price-Time Priority (FIFO)** execution semantics used by tier-1 exchanges and electronic trading desks.

```
                  [ Incoming Order Stream ]
                             │
                             ▼
         ┌───────────────────────────────────────┐
         │       Thread-Safe Matching Core       │
         │  (ReentrantLock + Navigable Red-Black) │
         └───────┬───────────────────────┬───────┘
                 │                       │
      [ Match Triggered ]        [ Resting Order ]
                 │                       │
                 ▼                       ▼
    ┌────────────────────────┐   ┌────────────────────────┐
    │  Trade Execution Event │   │  L2 Depth Book Update  │
    └────────────┬───────────┘   └───────────┬────────────┘
                 │                           │
                 └─────────────┬─────────────┘
                               │
                               ▼
            ┌────────────────────────────────────┐
            │  Real-Time WebSocket Broadcaster   │
            │     /topic/trades/{symbol}         │
            │     /topic/depth/{symbol}          │
            └────────────────────────────────────┘
```

---

## ⚡ Key Engineering Characteristics

* **Algorithmic Complexity:**
  * **Order Placement / Insertion:** $\mathcal{O}(\log N)$ via Navigable Red-Black Tree (`TreeMap`).
  * **FIFO Queue Matching:** $\mathcal{O}(1)$ at equal price levels using `ArrayDeque`.
  * **Order Lookup & Cancellation:** $\mathcal{O}(1)$ via Concurrent Index Map.
* **Continuous Double Auction:**
  * Bids sorted in descending order (highest price first).
  * Asks sorted in ascending order (lowest price first).
  * Immediate execution when crossing spread; Maker order establishes execution price.
* **Order Types Supported:**
  * **LIMIT:** Rest in order book at target price if not matched immediately.
  * **MARKET:** Sweep resting order book liquidity immediately; expires unfilled residual.
  * **CANCEL:** Atomic removal of active resting orders.
* **Real-Time Data Streaming:** STOMP over WebSockets streaming Level 2 (L2) depth snapshots and live trade execution feeds.
* **Integrated Market Maker Bot:** Automated liquidity provider injecting realistic order flows and bid-ask spreads for simulation and latency testing.

---

## 📡 REST & WebSocket API Specification

### 📥 REST Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/orders` | Submit LIMIT or MARKET order |
| `GET` | `/api/orders/{symbol}/{id}` | Query order state & remaining fill |
| `DELETE` | `/api/orders/{symbol}/{id}` | Cancel active resting order |
| `GET` | `/api/market/depth/{symbol}` | Get aggregated L2 Level 2 order book snapshot |
| `GET` | `/api/market/trades/{symbol}` | Get recent trade execution history |
| `GET` | `/api/market/symbols` | List active trading pairs (`BTC-USD`, `ETH-USD`, etc.) |

### 🔌 WebSocket STOMP Topics

* **Endpoint:** `ws://localhost:8080/ws-trade`
* **L2 Depth Feed:** `/topic/depth/{symbol}`
* **Trade Feed:** `/topic/trades/{symbol}`

---

## 🚀 Quick Start (Docker)

```bash
git clone https://github.com/ronitgupta138/trade-matching-engine.git
cd trade-matching-engine

# Run matching engine container
docker compose up --build -d

# Inspect health check
curl http://localhost:8080/api/health
```

---

## 🧪 Verification & Test Suite

```bash
./mvnw clean test
```

---

## 📜 License
This project is open-source under the [MIT License](LICENSE).
