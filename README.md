# Grid07 - Social Media Bot Guardrail System

A Spring Boot microservice that manages and throttles bot interactions on a social media platform using Redis as an atomic guardrail layer.

## Tech Stack

- Java 17
- Spring Boot 3.x
- PostgreSQL 16 (Docker)
- Redis 7 (Docker)
- Docker & Docker Compose
- Lombok

## How to Run

### 1. Start Docker containers
```bash
docker-compose up -d
```

### 2. Run the Spring Boot app
Open in IntelliJ and press **Shift + F10**

### 3. App runs on
http://localhost:8080

### 4. Import Postman Collection
Import `grid07-postman-collection.json` from the root of this repository into Postman to test all endpoints.

## Thread Safety - How Atomic Locks Work in Phase 2

The bot horizontal cap (max 100 bots per post) uses Redis atomic INCR operation.

### The Problem Without Atomicity
Two threads read bot_count = 99 simultaneously. Both think they are the 100th bot and both pass.

Thread 1 reads bot_count = 99 → thinks it's allowed ✅
Thread 2 reads bot_count = 99 → thinks it's allowed ✅
Both pass → 101 bots get through ❌

### The Solution - Redis Atomic INCR
Redis is single-threaded internally. INCR reads and increments in one uninterruptible operation. 
No two threads can ever read the same value.

Thread 1: INCR → 100 → allowed ✅
Thread 2: INCR → 101 → blocked ❌
Thread 3: INCR → 102 → blocked ❌
Exactly 100 pass through ✅

This was verified with 200 concurrent threads in Phase 4. Exactly 100 passed — never more.

### Cooldown Lock - Redis SET NX
The bot cooldown uses SET NX (Set if Not eXists):
- First bot sets the key with 10 minute TTL
- Any subsequent bot finds the key already exists and is blocked
- After 10 minutes the key auto-deletes and the lock releases
- This is atomic — two bots cannot both find the key absent simultaneously

## Redis Key Design

| Key | Purpose | TTL |
|---|---|---|
| `post:{id}:bot_count` | Bot reply counter per post | None |
| `post:{id}:virality_score` | Engagement score per post | None |
| `cooldown:bot_{id}:human_{id}` | Bot-to-user cooldown | 10 min |
| `user:{id}:pending_notifs` | Queued notifications | None |
| `notif_cooldown:user_{id}` | Notification throttle | 15 min |