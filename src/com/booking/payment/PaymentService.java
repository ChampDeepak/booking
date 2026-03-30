package com.booking.payment;

import com.booking.billing.Bill;
import com.booking.billing.BillingService;
import com.booking.common.Database;
import com.booking.common.entity.*;
import com.booking.reservation.ReserveSeatService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentService {
    private final Database db;
    private final BillingService billingService;
    private final ReserveSeatService reserveSeatService;
    private IPaymentAdapter paymentAdapter;
    private static final AtomicInteger ticketCounter = new AtomicInteger(1);

    public PaymentService(Database db, BillingService billingService, ReserveSeatService reserveSeatService) {
        this.db = db;
        this.billingService = billingService;
        this.reserveSeatService = reserveSeatService;
    }

    public void setPaymentAdapter(IPaymentAdapter adapter) {
        this.paymentAdapter = adapter;
    }

    public Ticket makePayment(String billId, String userEmail) {
        Bill bill = billingService.getBillById(billId);
        boolean success = paymentAdapter.processPayment(billId, bill.getTotalAmount());

        if (!success) {
            throw new RuntimeException("Payment failed for bill " + billId);
        }

        // confirm seats: HELD -> BOOKED
        reserveSeatService.confirmSeats(bill.getShowId(), bill.getSeatIds());

        // create ticket
        User user = db.getUserByEmail(userEmail);
        Show show = db.getShowById(bill.getShowId());
        Theater theater = db.getTheaterById(show.getTheaterId());
        String audName = theater.getAuditoriums().stream()
                .filter(a -> a.getAuditoriumId().equals(show.getAuditoriumId()))
                .findFirst().map(Auditorium::getAuditoriumName).orElse("Unknown");

        Ticket ticket = new Ticket(
            "TKT-" + ticketCounter.getAndIncrement(),
            user.getName(), userEmail, bill.getShowId(),
            theater.getTheaterName(), audName,
            bill.getSeatIds(), bill.getTotalAmount()
        );
        db.addTicket(ticket);
        System.out.println("  [PaymentService] Ticket created: " + ticket);
        return ticket;
    }
}
