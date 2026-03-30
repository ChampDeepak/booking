package com.booking.reservation;

import com.booking.common.Database;
import com.booking.common.entity.Seat;
import com.booking.common.enums.SeatStatus;
import com.booking.common.exception.SeatUnavailableException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ReserveSeatService implements IReserveSeatService {
    private final Database db;
    // per-show lock: different shows don't block each other
    private final ConcurrentHashMap<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

    public ReserveSeatService(Database db) {
        this.db = db;
    }

    @Override
    public boolean holdSeats(String showId, List<String> seatIds, String userEmail) {
        ReentrantLock lock = showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
        lock.lock();
        try {
            // check all seats available first (atomic check)
            for (String seatId : seatIds) {
                Seat seat = db.getSeatById(seatId);
                if (seat == null) {
                    throw new SeatUnavailableException("Seat not found: " + seatId);
                }
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException(
                        "Seat " + seatId + " is " + seat.getStatus() + " (requested by " + userEmail + ")");
                }
            }
            // all available, mark HELD atomically
            for (String seatId : seatIds) {
                Seat seat = db.getSeatById(seatId);
                seat.setStatus(SeatStatus.HELD);
                seat.setHeldBy(userEmail);
                seat.setHeldAt(System.currentTimeMillis());
            }
            System.out.println("  [ReserveSeatService] Seats " + seatIds + " HELD for " + userEmail);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void confirmSeats(String showId, List<String> seatIds) {
        ReentrantLock lock = showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
        lock.lock();
        try {
            for (String seatId : seatIds) {
                Seat seat = db.getSeatById(seatId);
                seat.setStatus(SeatStatus.BOOKED);
                seat.setHeldBy(null);
                seat.setHeldAt(0);
            }
            System.out.println("  [ReserveSeatService] Seats " + seatIds + " BOOKED");
        } finally {
            lock.unlock();
        }
    }
}
