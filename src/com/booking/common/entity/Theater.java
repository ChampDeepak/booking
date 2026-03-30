package com.booking.common.entity;

import java.util.List;

public class Theater {
    private String theaterId;
    private String theaterName;
    private String city;
    private List<Auditorium> auditoriums;

    public Theater() {}

    public String getTheaterId() { return theaterId; }
    public String getTheaterName() { return theaterName; }
    public String getCity() { return city; }
    public List<Auditorium> getAuditoriums() { return auditoriums; }

    @Override
    public String toString() {
        return theaterName + " (" + theaterId + ", " + city + ")";
    }
}
