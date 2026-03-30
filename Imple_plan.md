Goal: V0 simulation in Java demonstrating Proxy, Strategy, Adapter patterns + concurrency handling.                                                                                          
                                                                                                                                                                                               
  Key decisions:                                                                                                                                                                               
  - Schema: Flat structure in database.json -- theaters (with nested auditoriums), movies, shows, seats, tickets, users. No separate seatMaps collection.                                      
  - Folder structure: Accepted your feature-based approach with one adjustment -- merged fetchcitymovie/fetchtheater/fetchtheatermovie into a single browse/ folder since they're trivial      
  lookups. Keeps file count at ~37.                                                                                                                                                      
  - Concurrency: ReentrantLock per show (via ConcurrentHashMap), ScheduledExecutorService for hold expiry. Demo spawns 3 threads racing for same seats.                                        
  - Strategy: Two real pricing strategies (Fixed + PeakHour) swapped at runtime.                                                                       
  - Adapter: UPI + Razorpay adapters (simulated, print messages). Reused for refunds.                                                                                                          
  - Proxy: On every service -- validates inputs before delegating.                                                                                                                             
  - Stubbed: Admin APIs, auth (just role check), payment internals.                                                                                                                            
  - Main.java: 6 scripted demo sections exercising each pattern with clear console output.                                                                                                     
  - Dependency: Gson for JSON parsing.                                                                                                                                                         
                                                                                                                                                                                               
  7 implementation phases, ordered by dependency (foundation -> browse -> billing -> reservation -> payment -> cancellation/admin -> Main.java runner).                                        
                                                                                                                                                                                               
───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 Ready to code?                                                                                                                                                                                
                                                        
 Here is Claude's plan:                                                                                                                                                                        
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
 V0 Simulation Plan - Movie Ticket Booking System                                                                                                                                              
                                                        
 Context

 Build a v0 simulation in Java to demonstrate Proxy, Strategy, Adapter patterns and concurrency handling for seat booking. Uses a database.json as fake DB. Some parts (admin, auth) are
 hardcoded/stubbed. The simulation runs via Main.java which exercises all patterns with clear console output.

 ---
 Phase 1: Schema + Foundation (files 1-12)

 database.json

 Located at src/com/booking/database.json. Contains:
 - theaters (3): 2 in Mumbai, 1 in Bangalore. Auditoriums nested inside.
 - movies (4): linked to theaters via theaterId (per design assumption: same movie in different theaters = different movieId)
 - shows (4): linked to movie + theater + auditorium, with startTime/endTime/showBasePrice
 - seats (~10): per-show seats with seatId, seatType (GOLD/DIAMOND/SILVER), basePrice, status=AVAILABLE
 - tickets: empty array (populated during simulation)
 - users (2): one CUSTOMER, one ADMIN

 Design decision: No separate seatMaps collection. Seats link to shows via showId directly -- SeatMap becomes a query, not an entity.

 Entities (common/entity/)

 Plain Java POJOs: Theater.java, Auditorium.java, Movie.java, Show.java, Seat.java, Ticket.java, User.java

 Enums (common/enums/)

 SeatStatus (AVAILABLE, HELD, BOOKED), SeatType (GOLD, DIAMOND, SILVER), UserRole (CUSTOMER, ADMIN)

 Database.java (common/)

 Loads database.json using Gson, populates in-memory lists. Provides helper methods: getMoviesByCity(), getTheatersByCity(), getSeatsByShowId(), etc. This is the foundation everything
 depends on.

 Exceptions (common/exception/)

 ValidationException, SeatUnavailableException

 ---
 Phase 2: Browse + FetchShow -- Proxy Pattern (files 13-18)

 browse/

 - IBrowseService -- interface with getMoviesByCity(city), getTheatersByCity(city), getMoviesByTheater(theaterId)
 - BrowseService -- queries Database to filter movies/theaters
 - ProxyBrowseService -- PROXY: validates city not blank, theaterId exists, then delegates

 Why merge fetch services: FetchCityMovie, FetchTheater, FetchTheaterMovie are all trivial lookups. 3 separate interface+service+proxy triads (9 files) for simple filtering adds no value.
 One BrowseService with 3 methods still demonstrates Proxy.

 fetchshow/

 - IFetchShowService, FetchShowService, ProxyFetchShowService
 - Proxy validates movieId exists before delegating

 ---
 Phase 3: Billing -- Strategy Pattern (files 19-23)

 billing/

 - PricingStrategy (interface): double calculatePrice(Show show, Seat seat)
 - FixedPricingStrategy: showBasePrice + seatBasePrice
 - PeakHourPricingStrategy: (showBasePrice + seatBasePrice) * 1.5
 - Bill (DTO): billId, showId, seatIds, totalAmount, strategyUsed
 - BillingService: accepts PricingStrategy, iterates seats, returns Bill

 ---
 Phase 4: Reservation -- Concurrency (files 24-26)

 reservation/

 - IReserveSeatService: boolean holdSeats(showId, seatIds, userEmail)
 - ReserveSeatService: CONCURRENCY CORE
   - ConcurrentHashMap<String, ReentrantLock> -- one lock per show
   - holdSeats(): acquires lock, checks all seats AVAILABLE, marks HELD atomically, releases lock
   - If any seat not AVAILABLE -> throws SeatUnavailableException
 - SeatHoldManager: ScheduledExecutorService runs every 2s, releases seats HELD > 10 seconds

 Why ReentrantLock over synchronized: more explicit, supports tryLock with timeout, visually obvious as concurrency mechanism in demo.

 ---
 Phase 5: Payment -- Adapter Pattern (files 27-32)

 payment/

 - IPaymentAdapter (interface): boolean processPayment(billId, amount)
 - UpiAdapter: prints "UPI Payment of Rs.X processed", returns true
 - RazorpayAdapter: prints "Razorpay Payment of Rs.X processed", returns true
 - PaymentService: takes IPaymentAdapter, on success creates Ticket + marks seats BOOKED
 - ProxyPaymentService: validates bill exists and user before delegating

 ---
 Phase 6: Cancellation + Admin (files 33-37)

 cancellation/

 - ICancellationService, CancellationService, ProxyCancellationService
 - IRefundAdapter -- reuses Adapter pattern for refunds
 - Cancels ticket, marks seats AVAILABLE, simulates refund

 admin/

 - IAdminService, AdminService, ProxyAdminService
 - Proxy checks ADMIN role. Service adds shows/movies to in-memory lists. Stubbed.

 ---
 Phase 7: Main.java -- Simulation Runner

 Runs 6 scripted demo sections:

 1. Proxy Pattern Demo: valid + invalid browse/fetch requests showing validation
 2. Strategy Pattern Demo: same seats priced with Fixed vs PeakHour strategy
 3. Adapter Pattern Demo: same payment via UPI vs Razorpay adapter
 4. Concurrency Demo: 3 threads race to hold same seats -- one wins, two fail
 5. Hold Expiry Demo: hold seats, don't pay, wait 12s, seats auto-release
 6. Admin Proxy Demo: CUSTOMER denied, ADMIN succeeds

 ---
 Folder Structure

 src/com/booking/
 |-- Main.java
 |-- database.json
 |-- common/
 |   |-- Database.java
 |   |-- enums/       (SeatStatus, SeatType, UserRole)
 |   |-- entity/      (Theater, Auditorium, Movie, Show, Seat, Ticket, User)
 |   |-- exception/   (ValidationException, SeatUnavailableException)
 |-- browse/          (IBrowseService, BrowseService, ProxyBrowseService)
 |-- fetchshow/       (IFetchShowService, FetchShowService, ProxyFetchShowService)
 |-- billing/         (PricingStrategy, FixedPricingStrategy, PeakHourPricingStrategy, Bill, BillingService)
 |-- reservation/     (IReserveSeatService, ReserveSeatService, SeatHoldManager)
 |-- payment/         (IPaymentAdapter, UpiAdapter, RazorpayAdapter, PaymentService, ProxyPaymentService)
 |-- cancellation/    (ICancellationService, CancellationService, ProxyCancellationService, IRefundAdapter)
 |-- admin/           (IAdminService, AdminService, ProxyAdminService)

 ~37 files total

 ---
 Dependencies

 - Gson (com.google.gson) for JSON parsing -- add as jar or use manual parsing if zero-dependency preferred

 Verification

 1. Compile: javac -cp gson.jar src/com/booking/**/*.java
 2. Run: java -cp src:gson.jar com.booking.Main
 3. Expected: 6 clearly labeled demo sections in console output showing all patterns working
 4. Concurrency section should show thread contention (one success, others fail)
 5. Hold expiry section should show automatic seat release after timeout