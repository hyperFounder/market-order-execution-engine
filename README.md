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
                                   └─ Spawn Async Task: routeToExchange()
                                            │
             ┌──────────────────────────────┴──────────────────────────────┐
             ▼                                                             ▼
  Return 202 ACCEPTED (Immediate)                              Background Thread (hft-exec-*)
                                                               ├─ Sleep 5ms (Simulate Exchange)
                                                               ├─ Set status = FILLED
                                                               └─ Update ConcurrentHashMap Store
```
## Features
