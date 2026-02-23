# Order Service – Testing

**IDs are UUIDs.** Use UUID strings for `productId` in order items and for order `id` in URLs (e.g. `GET /orders/550e8400-e29b-41d4-a716-446655440000`).

## 1. Run unit tests

```bash
cd order-service
mvn test
```

This runs the idempotency tests in `OrderServiceTest` (create new order, and return existing when same idempotency key is used).

---

## 2. Manual API testing (with app running)

### Start dependencies and app

**Terminal 1 – database (from repo root):**
```bash
cd /path/to/pos-microservices
docker compose up order-db
# Or: export POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres && docker compose up order-db
```

**Terminal 2 – order service:**
```bash
cd order-service
mvn spring-boot:run
```

Service will be at **http://localhost:8083**.

### Create an order (first time – 201 Created)

Use UUIDs for `productId` (from product-service). Example:

```bash
curl -s -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "test-order-001",
    "items": [
      { "productId": "550e8400-e29b-41d4-a716-446655440000", "quantity": 2, "price": 9.99 },
      { "productId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8", "quantity": 1, "price": 14.50 }
    ]
  }' | jq .
```

Note the returned `id` (UUID).

### Idempotency – same key again (200 OK, same order)

Send the **same** request again (same `idempotencyKey`); you should get the **same** `id` and body (no duplicate order).

### Get order by ID

```bash
curl -s http://localhost:8083/orders/<order-uuid> | jq .
```

### Confirm order (PENDING → CONFIRMED)

```bash
curl -s -X POST http://localhost:8083/orders/<order-uuid>/confirm | jq .
```

Response will have `"status": "CONFIRMED"`. Only PENDING orders can be confirmed.

---

## Via API Gateway (port 8080)

If the API gateway is running, use the same paths with prefix `http://localhost:8080/api` (e.g. `POST http://localhost:8080/api/orders`). The gateway rewrites to the order-service.
