package com.booking.cancellation;

import com.booking.common.Database;
import com.booking.common.entity.Seat;
import com.booking.common.entity.Ticket;
import com.booking.common.enums.SeatStatus;
import com.booking.common.exception.ValidationException;

public class CancellationService implements ICancellationService {
    private final Database db;
    private IRefundAdapter refundAdapter;

    public CancellationService(Database db) {
        this.db = db;
    }

    public void setRefundAdapter(IRefundAdapter adapter) {
        this.refundAdapter = adapter;
    }

    @Override
    public Ticket cancelTicket(String ticketId) {
        Ticket ticket = db.getTicketById(ticketId);
        if (ticket == null) throw new ValidationException("Ticket not found: " + ticketId);
        if (ticket.isCancelled()) throw new ValidationException("Ticket already cancelled: " + ticketId);

        // release seats
        for (String seatId : ticket.getSeatIds()) {
            Seat seat = db.getSeatById(seatId);
            seat.setStatus(SeatStatus.AVAILABLE);
        }

        // process refund
        refundAdapter.processRefund(ticketId, ticket.getTotalAmount());

        ticket.setCancelled(true);
        System.out.println("  [CancellationService] Ticket cancelled: " + ticket);
        return ticket;
    }
}
