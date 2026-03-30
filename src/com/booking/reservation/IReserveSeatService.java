package com.booking.reservation;

import java.util.List;

public interface IReserveSeatService {
    boolean holdSeats(String showId, List<String> seatIds, String userEmail);
    void confirmSeats(String showId, List<String> seatIds);
}
