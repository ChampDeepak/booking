package com.booking.reservation;

import com.booking.common.Database;
import com.booking.common.entity.Seat;
import com.booking.common.enums.SeatStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SeatHoldManager {
    private final Database db;
    private final long holdDurationMs;
    private final ScheduledExecutorService scheduler;

    public SeatHoldManager(Database db, long holdDurationMs) {
        this.db = db;
        this.holdDurationMs = holdDurationMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SeatHoldManager");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::releaseExpiredHolds, 2, 2, TimeUnit.SECONDS);
        System.out.println("  [SeatHoldManager] Started (hold duration: " + holdDurationMs + "ms, polling every 2s)");
    }

    public void stop() {
        scheduler.shutdownNow();
        System.out.println("  [SeatHoldManager] Stopped");
    }

    private void releaseExpiredHolds() {
        long now = System.currentTimeMillis();
        for (Seat seat : db.getAllSeats()) {
            if (seat.getStatus() == SeatStatus.HELD && seat.getHeldAt() > 0) {
                if (now - seat.getHeldAt() > holdDurationMs) {
                    System.out.println("  [SeatHoldManager] Releasing expired hold: " +
                        seat.getSeatId() + " (was held by " + seat.getHeldBy() + ")");
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHeldBy(null);
                    seat.setHeldAt(0);
                }
            }
        }
    }
}
