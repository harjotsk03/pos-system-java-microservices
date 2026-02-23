# API Gateway

Single entry point (port **8080**) for all POS microservices. Routes are prefixed with `/api` and rewritten when forwarding.

| Gateway path        | Forwards to        | Backend path   |
|---------------------|--------------------|----------------|
| `/api/auth/**`      | auth-service:8090 | `/auth/**`     |
| `/api/product/**`   | product-service:8081 | `/product/**` |
| `/api/orders/**`    | order-service:8083  | `/orders/**`  |

**Run:** `mvn spring-boot:run` (from this directory). Ensure auth, product, and order services (and their DBs) are running first.

**Example via gateway:**
```bash
# Create order (use UUID for productId if you have products)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"key-1","items":[{"productId":"<product-uuid>","quantity":1,"price":9.99}]}'
```
