# POS Microservices

- **api-gateway** – entry point on port 8080; routes `/api/auth/**`, `/api/product/**`, `/api/orders/**` to the respective services.
- **auth-service** – port 8090
- **product-service** – port 8081
- **order-service** – port 8083

All entity IDs use **UUIDs**. PostgreSQL runs via Docker (see `docker-compose.yaml`).

## Wipe all 3 databases

To remove all data and recreate empty DBs (containers will keep running with fresh data):

```bash
docker compose down -v
docker compose up -d
```

Then restart each service so JPA re-creates schema (`ddl-auto: update`).

## Run API Gateway

From repo root:

```bash
cd api-gateway && mvn spring-boot:run
```

Ensure auth-, product-, and order-service (and their DBs) are running first. Then use `http://localhost:8080/api/...` for all requests.
