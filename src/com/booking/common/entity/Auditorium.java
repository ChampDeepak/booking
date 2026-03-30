package com.booking.common.entity;

public class Auditorium {
    private String auditoriumId;
    private String auditoriumName;

    public Auditorium() {}

    public Auditorium(String auditoriumId, String auditoriumName) {
        this.auditoriumId = auditoriumId;
        this.auditoriumName = auditoriumName;
    }

    public String getAuditoriumId() { return auditoriumId; }
    public String getAuditoriumName() { return auditoriumName; }

    @Override
    public String toString() {
        return auditoriumName + " (" + auditoriumId + ")";
    }
}
