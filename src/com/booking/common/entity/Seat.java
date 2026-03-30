package com.booking.common.entity;

import com.booking.common.enums.SeatStatus;
import com.booking.common.enums.SeatType;

public class Seat {
    private String seatId;
    private String seatType;
    private String showId;
    private double basePrice;
    private String status;

    // runtime fields (not in JSON)
    private transient String heldBy;
    private transient long heldAt;

    public Seat() {}

    public String getSeatId() { return seatId; }
    public String getShowId() { return showId; }
    public double getBasePrice() { return basePrice; }

    public SeatType getSeatType() { return SeatType.valueOf(seatType); }

    public SeatStatus getStatus() { return SeatStatus.valueOf(status); }
    public void setStatus(SeatStatus s) { this.status = s.name(); }

    public String getHeldBy() { return heldBy; }
    public void setHeldBy(String heldBy) { this.heldBy = heldBy; }

    public long getHeldAt() { return heldAt; }
    public void setHeldAt(long heldAt) { this.heldAt = heldAt; }

    @Override
    public String toString() {
        return seatId + " [" + seatType + ", " + status + ", Rs." + basePrice + "]";
    }
}
