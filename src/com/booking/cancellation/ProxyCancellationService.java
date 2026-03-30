package com.booking.cancellation;

import com.booking.common.Database;
import com.booking.common.entity.Ticket;
import com.booking.common.exception.ValidationException;

public class ProxyCancellationService implements ICancellationService {
    private final CancellationService service;
    private final Database db;

    public ProxyCancellationService(CancellationService service, Database db) {
        this.service = service;
        this.db = db;
    }

    @Override
    public Ticket cancelTicket(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new ValidationException("Ticket ID cannot be empty");
        }
        Ticket ticket = db.getTicketById(ticketId);
        if (ticket == null) {
            throw new ValidationException("Ticket not found: " + ticketId);
        }
        return service.cancelTicket(ticketId);
    }
}
