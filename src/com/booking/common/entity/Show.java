package com.booking.common.entity;

public class Show {
    private String showId;
    private String movieId;
    private String theaterId;
    private String auditoriumId;
    private String startTime;
    private String endTime;
    private double showBasePrice;

    public Show() {}

    public Show(String showId, String movieId, String theaterId, String auditoriumId,
                String startTime, String endTime, double showBasePrice) {
        this.showId = showId;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.auditoriumId = auditoriumId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.showBasePrice = showBasePrice;
    }

    public String getShowId() { return showId; }
    public String getMovieId() { return movieId; }
    public String getTheaterId() { return theaterId; }
    public String getAuditoriumId() { return auditoriumId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getShowBasePrice() { return showBasePrice; }

    @Override
    public String toString() {
        return "Show " + showId + " [" + startTime + " - " + endTime + "] BasePrice=" + showBasePrice;
    }
}
