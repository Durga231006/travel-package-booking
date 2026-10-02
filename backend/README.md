# Travel Package Booking (Spring Boot + MySQL)

Manage customers, destinations, travel packages and bookings.
Tech: Java 17, Spring Boot 3.3, Spring Data JPA, JdbcTemplate, MySQL 8.
The frontend is a separate project (travel-frontend) opened in VS Code.

## How to run

1. Start MySQL (pick one):
   - Docker: `docker compose up -d`
   - Or use your own MySQL 8 and set `DB_USERNAME` / `DB_PASSWORD` (defaults: `root` / `root`).
2. Run the app: `mvn spring-boot:run` (or Run As > Spring Boot App in Eclipse)
3. Check the API: http://localhost:8080/api/packages
4. Start the frontend from VS Code (see the travel-frontend README)

On start, `schema.sql` creates the database objects and `data.sql` adds sample data
(safe to run again and again).

## Requirement checklist

| Requirement | Where |
|---|---|
| JOIN: bookings with customer and destination details | `BookingJdbcRepository.findAllBookingDetails()` -> `GET /api/bookings` |
| Subquery: packages with bookings above average | `BookingJdbcRepository.findPackagesAboveAverageBookings()` -> `GET /api/packages/above-average-bookings` |
| Procedure: book package | `sp_book_package` in `schema.sql` -> `POST /api/bookings` |
| Function: calculate package cost | `fn_calculate_package_cost` in `schema.sql` -> `GET /api/packages/{id}/cost?travelers=3` |
| Trigger: update package availability | `trg_bookings_after_insert` (seats go down, SOLD_OUT at 0) and `trg_bookings_after_update` (seats come back on cancel) |
| REST API | `controller` package (CORS for the frontend in `config/CorsConfig.java`) |
| Frontend | separate `travel-frontend` project |

Business rule in the function: groups of 5 or more travelers get a 10% discount.

## REST API

| Method | URL | Purpose |
|---|---|---|
| GET / POST | `/api/customers` | list / add customers |
| GET / PUT / DELETE | `/api/customers/{id}` | one customer |
| GET / POST | `/api/destinations` | list / add destinations |
| GET / PUT / DELETE | `/api/destinations/{id}` | one destination |
| GET / POST | `/api/packages` | list / add packages |
| GET / PUT / DELETE | `/api/packages/{id}` | one package |
| GET | `/api/packages/above-average-bookings` | subquery report |
| GET | `/api/packages/{id}/cost?travelers=3` | cost from SQL function |
| GET | `/api/bookings` | JOIN report |
| POST | `/api/bookings` | book a package (stored procedure) |
| PUT | `/api/bookings/{id}/cancel` | cancel a booking (trigger returns the seats) |

### Example: book a package

Request: `POST /api/bookings`
```json
{ "customerId": 1, "packageId": 2, "travelers": 2 }
```
Response `201 Created`:
```json
{ "bookingId": 9, "totalCost": 30000.00, "message": "Booking confirmed successfully" }
```
Error response (no seats left) `400 Bad Request`:
```json
{ "timestamp": "2026-10-01T10:00:00", "status": 400, "error": "Bad Request",
  "message": "Not enough seats available for this package" }
```

## Request flow (booking)

`app.js` -> `BookingController` -> `BookingService` (@Transactional) -> `BookingJdbcRepository`
-> `CALL sp_book_package(...)` -> inside MySQL: checks, `fn_calculate_package_cost`, `INSERT INTO bookings`
-> trigger `trg_bookings_after_insert` updates `packages` -> result returns back up to the browser.

## Project structure

```
src/main/java/com/travel/booking
  controller/   REST endpoints (receive HTTP, call services)
  service/      business logic and error handling
  repository/   JPA repositories + BookingJdbcRepository (JOIN, subquery, procedure, function)
  entity/       Customer, Destination, TravelPackage (map to tables)
  dto/          request/response records
  exception/    custom exceptions + GlobalExceptionHandler (JSON errors)
  config/       CorsConfig (lets the VS Code frontend call the API)
src/main/resources
  schema.sql    tables, function, procedure, triggers
  data.sql      sample data
```
