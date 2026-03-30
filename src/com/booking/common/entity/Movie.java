package com.booking.common.entity;

public class Movie {
    private String movieId;
    private String theaterId;
    private String movieName;
    private String language;
    private String genre;

    public Movie() {}

    public String getMovieId() { return movieId; }
    public String getTheaterId() { return theaterId; }
    public String getMovieName() { return movieName; }
    public String getLanguage() { return language; }
    public String getGenre() { return genre; }

    @Override
    public String toString() {
        return movieName + " [" + language + "] (" + movieId + ")";
    }
}
