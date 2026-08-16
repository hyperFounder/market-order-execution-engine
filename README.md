## Summary. 

A low-latency, non-blocking **Spring Boot 3** order execution engine with asynchronous processing. It ingests market orders via non-blocking REST endpoints and offloads processing to a custom thread pool ```(ThreadPoolTaskExecutor: 8 core / 16 max threads, 1000 queue capacity).```

## Key Features
- **Non-Blocking Architecture:** Ingests REST requests (`/api/v1/orders/submit`) and returns immediate `202 ACCEPTED` acknowledgments while routing executes asynchronously.
- **Thread-safe** `ConcurrentHashMap` in-memory store for O(1) order status storage (no database) I/O overhead.

## Architecture
```text
[ Client ] ---> ( POST /submit ) ---> [ OrderIngestionController ]
                                            │
                                  (@Valid Validation Check)
                                            │
                                            ▼
                                   [ OrderRouterService ]
                                   ├─ Check Liquidity Limit (>1,000,000)
                                   ├─ Map DTO -> OrderEntity (UUID + PENDING_ROUTE)
                                   ├─ Save to ConcurrentHashMap Store
                                   └─ Spawn Async Task: routeToExchange(): **CompletableFuture<void>**
                                            │
             ┌──────────────────────────────┴──────────────────────────────┐
             ▼                                                             ▼
  Return 202 ACCEPTED (Immediate)                              Background Thread (hft-exec-*)
                                                               ├─ Sleep 5ms (Simulate Exchange)
                                                               ├─ Set status = FILLED
                                                               └─ Update ConcurrentHashMap Store (S
```
### Build & Run

1. Build: ```mvn clean install```
2. Start the application: ```mvn spring-boot:run```. The server will start on: ```http://localhost:8080```


### API Endpoints

```POST /api/v1/orders/submit```

#### Request Body (JSON)
```json
curl -X POST http://localhost:8080/api/v1/orders/submit \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "NVDA",
    "side": "BUY",
    "price": 125.50,
    "quantity": 500
  }'
```

```GET api/v1/orders/{orderId}```
- ```curl http://localhost:8080/api/v1/orders/0274c28b-bfb7-4dd1-a044-9b105c8f060f```
