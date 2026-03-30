# Movie Ticket Booking System

## How to Run

Prerequisites: Java 11+, Maven

```bash
git clone <repo-url>
cd booking
mvn compile
mvn exec:java -Dexec.mainClass="com.booking.Main"
```

`Main.java` runs a full simulation covering every feature end-to-end.

---

## Entities

### User
Identified by email (unique identifier as per requirements). Each user has a `UserRole`
(ADMIN, CUSTOMER). The role does not directly control access — instead, each role maps
to a set of `Permission` enum values (ADD_SHOW, ADD_MOVIE, ADD_THEATER). Authorization
checks `user.getRole().hasPermission(permission)` so new roles can be introduced by
adding one enum entry without touching any service code (Open/Closed Principle via RBAC).

### Theater and Auditorium
A `Theater` has a city, name, and a list of `Auditorium` objects. This models the
requirement that a theater can have multiple screens. Each auditorium has its own ID
and name. Shows are scheduled per auditorium.

### Movie
A `Movie` belongs to a specific theater (via theaterId) and carries movieName, language,
and genre. The theaterId link is what lets us query "movies playing in a city" — we first
find theaters in that city, then find movies linked to those theaters. Language field
supports the multilingual movie requirement.

### Show
A `Show` ties a movie to a specific auditorium in a theater with a time slot (startTime,
endTime) and a showBasePrice. This models the requirement that each screen can have
multiple shows. The showBasePrice feeds into the pricing system.

### Seat
A `Seat` belongs to a show (via showId) and has a `SeatType` (GOLD, SILVER, DIAMOND)
with its own basePrice. The `SeatStatus` enum tracks its lifecycle: AVAILABLE → HELD → BOOKED.
Seats also track `heldBy` (user email) and `heldAt` (timestamp) for the hold expiry mechanism.

### Ticket
Created after successful payment. Links a user to a show and a list of seat IDs with the
paid amount. Has a `cancelled` flag for the cancellation flow.

### Bill
Generated during the billing step. Captures showId, seatIds, totalAmount, and which
pricing strategy was used. Stored in a `ConcurrentHashMap` inside `BillingService` and
looked up by billId during payment.

---

## How Each Requirement is Implemented

### Browse Theaters in a City (User API)

`BrowseService` implements `IBrowseService`. It queries `Database.getTheatersByCity(city)`
which filters theaters by city name. A `ProxyBrowseService` wraps it — before delegating,
the proxy validates that the city string is non-empty. If validation fails, it throws
`ValidationException`. This proxy pattern keeps input validation separate from business logic.

### Browse Movies in a City (User API)

Same `BrowseService.getMoviesByCity(city)`. Internally, Database first finds all theater
IDs in that city, then returns movies whose theaterId matches any of them. The proxy
validates the city input before delegation.

### Browse Movies by Theater (User API)

`BrowseService.getMoviesByTheater(theaterId)`. The proxy validates that the theaterId
is non-empty and that the theater actually exists in the database before delegating.

### View Shows for a Movie (User API)

`FetchShowService` implements `IFetchShowService`. It calls `Database.getShowsByMovieId(movieId)`
to return all shows for a given movie. `ProxyFetchShowService` validates that the movieId
is non-empty and that the movie exists before delegating.

### Seat Selection and Hold (User API)

`ReserveSeatService` implements `IReserveSeatService` with two methods:

- `holdSeats(showId, seatIds, userEmail)` — Temporarily holds selected seats. Uses a
  per-show `ReentrantLock` from a `ConcurrentHashMap<String, ReentrantLock>` to handle
  concurrent bookings. Under the lock, it atomically checks that all requested seats are
  AVAILABLE, then marks them all as HELD with the user's email and current timestamp.
  If any seat is not available, it throws `SeatUnavailableException` without modifying
  any seat. Per-show locking means users booking different shows never block each other.

- `confirmSeats(showId, seatIds)` — Transitions HELD seats to BOOKED after payment succeeds.
  Also acquires the show lock for atomicity.

### Seat Hold Expiry

`SeatHoldManager` addresses the requirement that seats are released if payment is not
completed within the hold duration. It runs a background daemon thread via
`ScheduledExecutorService` that polls every 2 seconds. For each HELD seat, it checks
if `now - heldAt > holdDuration` and if so, resets the seat to AVAILABLE and clears
the hold metadata. This prevents seats from being locked forever when a user abandons
the payment flow.

### Pricing (Configurable)

The requirement asks for configurable pricing based on seat category, show timing, day
of week, etc. This is implemented via the `PricingStrategy` interface with a single method
`calculatePrice(Show, Seat)`. Two implementations exist:

- `FixedPricingStrategy` — returns showBasePrice + seatBasePrice
- `PeakHourPricingStrategy` — returns (showBasePrice + seatBasePrice) * 1.5

`BillingService.generateBill(showId, seatIds, strategy)` iterates over the seats, applies
the given strategy to each, and sums the result into a `Bill`. The caller chooses which
strategy to pass in. Adding a new pricing rule (weekend surcharge, demand-based, etc.)
means implementing `PricingStrategy` — `BillingService` does not change (Strategy Pattern).

### Make Payment (User API)

`PaymentService` takes a billId and userEmail, processes the payment through an
`IPaymentAdapter`, then calls `ReserveSeatService.confirmSeats()` to transition seats
from HELD to BOOKED, and finally creates a `Ticket`. Uses `AtomicInteger` for thread-safe
ticket ID generation.

Payment gateways are abstracted behind `IPaymentAdapter` with `processPayment(billId, amount)`.
Currently implemented: `UpiAdapter` and `RazorpayAdapter`. Adding a new gateway (Stripe,
net banking) means writing one adapter class — no changes to `PaymentService` (Adapter Pattern).

`ProxyPaymentService` wraps it with validation — checks that billId and userEmail are
non-empty, and that both the bill and user exist in the database.

### Cancel Booking and Process Refund (User API)

`CancellationService` implements `ICancellationService`. `cancelTicket(ticketId)` validates
the ticket is not already cancelled, resets all its seats back to AVAILABLE, processes the
refund through an `IRefundAdapter`, and marks the ticket as cancelled.

Refund gateways are abstracted the same way as payments — `IRefundAdapter` with
`processRefund(ticketId, amount)`. Currently implemented: `RazorpayRefundAdapter`.
`ProxyCancellationService` validates the ticket exists before delegating.

### Add Theater, Add Movie, Add Show (Admin APIs)

These are conceptually three separate services with distinct responsibilities. In this
implementation, they are grouped under a single `IAdminService` interface and `AdminService`
class for simplicity. The name "AdminService" refers to the nature of the operations
(content management), not to who can access them.

- `addTheater(theaterId, name, city, auditoriumNames)` — creates the Theater with
  auto-generated Auditorium objects
- `addMovie(movieId, theaterId, movieName, language, genre)` — creates and stores the Movie
- `addShow(showId, movieId, theaterId, auditoriumId, startTime, endTime, basePrice)` — creates
  and stores the Show

`ProxyAdminService` wraps these with RBAC checks via `authorize(userEmail, Permission)`.
Each `UserRole` declares its permissions as an `EnumSet<Permission>`. The proxy calls
`user.getRole().hasPermission(Permission.ADD_SHOW)` — so access is controlled by what a
role is allowed to do, not by checking role names directly. Adding a new role like OPERATOR
that can add shows but not theaters means adding one enum entry:
`OPERATOR(EnumSet.of(Permission.ADD_SHOW))` — no proxy code changes.

---

## Data Layer

`Database` is a singleton loaded from `database.json` at startup. It holds all entities
in lists and provides query methods (getTheatersByCity, getMoviesByTheaterId, getShowsByMovieId,
getSeatsByShowId, getUserByEmail, etc.). Thread-safe singleton initialization via
synchronized block. Supports `reload()` to reset state.

---

## Package Structure

```
com.booking/
├── admin/           — IAdminService, AdminService, ProxyAdminService
├── browse/          — IBrowseService, BrowseService, ProxyBrowseService
├── fetchshow/       — IFetchShowService, FetchShowService, ProxyFetchShowService
├── billing/         — BillingService, Bill, PricingStrategy, FixedPricingStrategy, PeakHourPricingStrategy
├── reservation/     — IReserveSeatService, ReserveSeatService, SeatHoldManager
├── payment/         — PaymentService, ProxyPaymentService, IPaymentAdapter, UpiAdapter, RazorpayAdapter
├── cancellation/    — ICancellationService, CancellationService, ProxyCancellationService, IRefundAdapter, RazorpayRefundAdapter
└── common/
    ├── entity/      — User, Theater, Auditorium, Movie, Show, Seat, Ticket
    ├── enums/       — UserRole, Permission, SeatStatus, SeatType
    ├── exception/   — ValidationException, SeatUnavailableException
    └── Database.java
```
