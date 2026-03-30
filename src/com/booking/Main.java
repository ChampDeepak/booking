package com.booking;

import com.booking.admin.*;
import com.booking.billing.*;
import com.booking.browse.*;
import com.booking.cancellation.*;
import com.booking.common.Database;
import com.booking.common.entity.*;
// import com.booking.common.enums.SeatStatus;
import com.booking.common.exception.*;
import com.booking.fetchshow.*;
import com.booking.payment.*;
import com.booking.reservation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static Database db;
    private static ProxyBrowseService browseService;
    private static ProxyFetchShowService fetchShowService;
    private static BillingService billingService;
    private static ReserveSeatService reserveSeatService;
    private static PaymentService paymentService;
    private static ProxyPaymentService proxyPaymentService;
    private static SeatHoldManager holdManager;

    public static void main(String[] args) throws Exception {
        System.out.println("=== MOVIE TICKET BOOKING SYSTEM — v0 SIMULATION ===\n");

        initServices();

        demoProxyPattern();
        demoStrategyPattern();
        demoAdapterPattern();
        demoConcurrency();
        demoHoldExpiry();
        demoAdminProxy();
        demoCancellation();

        holdManager.stop();
        System.out.println("\n=== SIMULATION COMPLETE ===");
    }

    private static void initServices() {
        db = Database.getInstance();

        BrowseService realBrowse = new BrowseService(db);
        browseService = new ProxyBrowseService(realBrowse, db);

        FetchShowService realFetchShow = new FetchShowService(db);
        fetchShowService = new ProxyFetchShowService(realFetchShow, db);

        billingService = new BillingService(db);
        reserveSeatService = new ReserveSeatService(db);
        paymentService = new PaymentService(db, billingService, reserveSeatService);
        proxyPaymentService = new ProxyPaymentService(paymentService, billingService, db);

        holdManager = new SeatHoldManager(db, 10_000); // 10 second hold
        holdManager.start();
    }

    // ==================== SECTION 1: PROXY PATTERN ====================
    private static void demoProxyPattern() {
        System.out.println("--- SECTION 1: PROXY PATTERN DEMO ---\n");

        // valid request
        System.out.println("[1a] getMoviesByCity(\"Mumbai\"):");
        List<Movie> movies = browseService.getMoviesByCity("Mumbai");
        movies.forEach(m -> System.out.println("     " + m));

        // invalid: empty city
        System.out.println("\n[1b] getMoviesByCity(\"\") — should throw ValidationException:");
        try {
            browseService.getMoviesByCity("");
        } catch (ValidationException e) {
            System.out.println("     CAUGHT: " + e.getMessage());
        }

        // valid theater browse
        System.out.println("\n[1c] getTheatersByCity(\"Mumbai\"):");
        List<Theater> theaters = browseService.getTheatersByCity("Mumbai");
        theaters.forEach(t -> System.out.println("     " + t));

        // invalid movie ID
        System.out.println("\n[1d] getShowsByMovie(\"INVALID_ID\") — should throw ValidationException:");
        try {
            fetchShowService.getShowsByMovie("INVALID_ID");
        } catch (ValidationException e) {
            System.out.println("     CAUGHT: " + e.getMessage());
        }

        // valid fetch shows
        System.out.println("\n[1e] getShowsByMovie(\"M1\") — Interstellar shows:");
        List<Show> shows = fetchShowService.getShowsByMovie("M1");
        shows.forEach(s -> System.out.println("     " + s));

        System.out.println();
    }

    // ==================== SECTION 2: STRATEGY PATTERN ====================
    private static void demoStrategyPattern() {
        System.out.println("--- SECTION 2: STRATEGY PATTERN DEMO ---\n");

        List<String> seatIds = Arrays.asList("SEAT-S1-1", "SEAT-S1-3");

        // FixedPricingStrategy
        PricingStrategy fixed = new FixedPricingStrategy();
        Bill bill1 = billingService.generateBill("S1", seatIds, fixed);
        System.out.println("[2a] " + fixed.getStrategyName() + ": " + bill1);

        // PeakHourPricingStrategy
        PricingStrategy peak = new PeakHourPricingStrategy();
        Bill bill2 = billingService.generateBill("S1", seatIds, peak);
        System.out.println("[2b] " + peak.getStrategyName() + ": " + bill2);

        System.out.println();
    }

    // ==================== SECTION 3: ADAPTER PATTERN ====================
    private static void demoAdapterPattern() {
        System.out.println("--- SECTION 3: ADAPTER PATTERN DEMO ---\n");

        // generate a bill for adapter demo using different show seats
        List<String> seatIds = Arrays.asList("SEAT-S4-1", "SEAT-S4-2");
        Bill bill = billingService.generateBill("S4", seatIds, new FixedPricingStrategy());

        System.out.println("[3a] Pay via UPI adapter:");
        IPaymentAdapter upi = new UpiAdapter();
        upi.processPayment(bill.getBillId(), bill.getTotalAmount());

        System.out.println("\n[3b] Pay via Razorpay adapter:");
        IPaymentAdapter razorpay = new RazorpayAdapter();
        razorpay.processPayment(bill.getBillId(), bill.getTotalAmount());

        System.out.println();
    }

    // ==================== SECTION 4: CONCURRENCY DEMO ====================
    private static void demoConcurrency() throws Exception {
        System.out.println("--- SECTION 4: CONCURRENCY DEMO ---\n");
        System.out.println("[4a] 3 threads racing to hold SEAT-S1-1 and SEAT-S1-2 for Show S1:\n");

        List<String> targetSeats = Arrays.asList("SEAT-S1-1", "SEAT-S1-2");
        String[] users = {"deepak@example.com", "user2@example.com", "user3@example.com"};

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(3);
        String[] winner = {null};

        for (String userEmail : users) {
            new Thread(() -> {
                try {
                    startGate.await(); // all threads start simultaneously
                    reserveSeatService.holdSeats("S1", targetSeats, userEmail);
                    synchronized (winner) {
                        winner[0] = userEmail;
                    }
                    System.out.println("  Thread [" + userEmail + "] SUCCEEDED");
                } catch (SeatUnavailableException e) {
                    System.out.println("  Thread [" + userEmail + "] FAILED: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Booker-" + userEmail).start();
        }

        startGate.countDown(); // release all threads
        doneLatch.await();     // wait for all to finish

        // print seat status
        System.out.println("\n  Seat status after race:");
        for (String seatId : targetSeats) {
            Seat seat = db.getSeatById(seatId);
            System.out.println("  " + seat);
        }

        // winner completes payment
        System.out.println("\n[4b] Winner completes payment:\n");
        Bill bill = billingService.generateBill("S1", targetSeats, new FixedPricingStrategy());
        Ticket ticket = proxyPaymentService.makePayment(bill.getBillId(), winner[0], new UpiAdapter());
        System.out.println("  Ticket: " + ticket);

        // print final seat status
        System.out.println("\n  Seat status after payment:");
        for (String seatId : targetSeats) {
            Seat seat = db.getSeatById(seatId);
            System.out.println("  " + seat);
        }

        System.out.println();
    }

    // ==================== SECTION 5: HOLD EXPIRY DEMO ====================
    private static void demoHoldExpiry() throws Exception {
        System.out.println("--- SECTION 5: HOLD EXPIRY DEMO ---\n");

        List<String> seatIds = Arrays.asList("SEAT-S1-3", "SEAT-S1-4");
        System.out.println("[5a] Holding seats " + seatIds + " but NOT paying...");
        reserveSeatService.holdSeats("S1", seatIds, "lazy@example.com");

        System.out.println("  Status right after hold:");
        seatIds.forEach(id -> System.out.println("  " + db.getSeatById(id)));

        System.out.println("\n  Waiting 12 seconds for hold expiry...\n");
        Thread.sleep(12_000);

        System.out.println("  Status after expiry:");
        seatIds.forEach(id -> System.out.println("  " + db.getSeatById(id)));

        System.out.println();
    }

    // ==================== SECTION 6: ADMIN PROXY DEMO (RBAC) ====================
    private static void demoAdminProxy() {
        System.out.println("--- SECTION 6: ADMIN PROXY DEMO (RBAC) ---\n");

        AdminService realAdmin = new AdminService(db);
        ProxyAdminService proxyAdmin = new ProxyAdminService(realAdmin, db);

        // customer tries admin operations — should be denied
        System.out.println("[6a] CUSTOMER tries to addShow — should be denied:");
        try {
            proxyAdmin.addShowAsUser("deepak@example.com", "S99", "M1", "T1", "A1",
                "2026-04-02T10:00:00", "2026-04-02T13:00:00", 200);
        } catch (ValidationException e) {
            System.out.println("     CAUGHT: " + e.getMessage());
        }

        System.out.println("\n[6b] CUSTOMER tries to addMovie — should be denied:");
        try {
            proxyAdmin.addMovieAsUser("deepak@example.com", "M99", "T1", "Oppenheimer", "English", "Drama");
        } catch (ValidationException e) {
            System.out.println("     CAUGHT: " + e.getMessage());
        }

        System.out.println("\n[6c] CUSTOMER tries to addTheater — should be denied:");
        try {
            proxyAdmin.addTheaterAsUser("deepak@example.com", "T99", "IMAX Wadala", "Mumbai",
                Arrays.asList("Screen 1", "Screen 2"));
        } catch (ValidationException e) {
            System.out.println("     CAUGHT: " + e.getMessage());
        }

        // admin succeeds
        System.out.println("\n[6d] ADMIN adds theater:");
        proxyAdmin.addTheaterAsUser("admin@example.com", "T99", "IMAX Wadala", "Mumbai",
            Arrays.asList("Screen 1", "Screen 2"));

        System.out.println("\n[6e] ADMIN adds movie:");
        proxyAdmin.addMovieAsUser("admin@example.com", "M99", "T99", "Oppenheimer", "English", "Drama");

        System.out.println("\n[6f] ADMIN adds show:");
        proxyAdmin.addShowAsUser("admin@example.com", "S99", "M99", "T99", "T99-A1",
            "2026-04-02T10:00:00", "2026-04-02T13:00:00", 300);

        System.out.println();
    }

    // ==================== SECTION 7: CANCELLATION DEMO ====================
    private static void demoCancellation() {
        System.out.println("--- SECTION 7: CANCELLATION DEMO ---\n");

        List<Ticket> tickets = db.getTickets();
        if (tickets.isEmpty()) {
            System.out.println("  No tickets to cancel (skipping)");
            return;
        }

        Ticket ticket = tickets.get(0);
        System.out.println("[7a] Cancelling ticket: " + ticket.getTicketId());

        CancellationService cancelService = new CancellationService(db);
        cancelService.setRefundAdapter(new RazorpayRefundAdapter());
        ProxyCancellationService proxyCancelService = new ProxyCancellationService(cancelService, db);

        Ticket cancelled = proxyCancelService.cancelTicket(ticket.getTicketId());

        System.out.println("\n  Seat status after cancellation:");
        for (String seatId : cancelled.getSeatIds()) {
            System.out.println("  " + db.getSeatById(seatId));
        }

        System.out.println();
    }
}
