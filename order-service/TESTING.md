# Order Service – Testing

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

```bash
curl -s -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "test-order-001",
    "items": [
      { "productId": 1, "quantity": 2, "price": 9.99 },
      { "productId": 2, "quantity": 1, "price": 14.50 }
    ]
  }' | jq .
```

Note the returned `id` (e.g. `1`).

### Idempotency – same key again (200 OK, same order)

Send the **same** request again (same `idempotencyKey`):

```bash
curl -s -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "test-order-001",
    "items": [
      { "productId": 1, "quantity": 2, "price": 9.99 },
      { "productId": 2, "quantity": 1, "price": 14.50 }
    ]
  }' | jq .
```

You should get the **same** `id` and body as the first call (no duplicate order).

### Get order by ID

```bash
curl -s http://localhost:8083/orders/1 | jq .
```

Replace `1` with the `id` from the create response.

### Confirm order (PENDING → CONFIRMED)

```bash
curl -s -X POST http://localhost:8083/orders/1/confirm | jq .
```

Replace `1` with the order `id`. Response will have `"status": "CONFIRMED"`. Only PENDING orders can be confirmed.
