package com.booking.cancellation;

import com.booking.common.entity.Ticket;

public interface ICancellationService {
    Ticket cancelTicket(String ticketId);
}
