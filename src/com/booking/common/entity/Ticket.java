package com.booking.common.entity;

import java.util.List;

public class Ticket {
    private String ticketId;
    private String userName;
    private String userEmail;
    private String showId;
    private String theaterName;
    private String auditoriumName;
    private List<String> seatIds;
    private double totalAmount;
    private boolean cancelled;

    public Ticket(String ticketId, String userName, String userEmail, String showId,
                  String theaterName, String auditoriumName, List<String> seatIds, double totalAmount) {
        this.ticketId = ticketId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.showId = showId;
        this.theaterName = theaterName;
        this.auditoriumName = auditoriumName;
        this.seatIds = seatIds;
        this.totalAmount = totalAmount;
        this.cancelled = false;
    }

    public String getTicketId() { return ticketId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getShowId() { return showId; }
    public String getTheaterName() { return theaterName; }
    public String getAuditoriumName() { return auditoriumName; }
    public List<String> getSeatIds() { return seatIds; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public String toString() {
        return "Ticket{id=" + ticketId + ", user=" + userName + ", show=" + showId +
               ", seats=" + seatIds + ", amount=Rs." + totalAmount +
               (cancelled ? ", CANCELLED" : "") + "}";
    }
}
