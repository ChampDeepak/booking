package com.booking.common;

import com.booking.common.entity.*;
import com.booking.common.enums.SeatStatus;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    private static Database instance;

    private List<Theater> theaters;
    private List<Movie> movies;
    private List<Show> shows;
    private List<Seat> seats;
    private List<Ticket> tickets;
    private List<User> users;

    private Database() {
        loadFromJson();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // for re-loading fresh data between simulation sections
    public void reload() {
        loadFromJson();
    }

    private void loadFromJson() {
        try (Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/com/booking/database.json"))) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Gson gson = new Gson();

            theaters = gson.fromJson(root.get("theaters"), new TypeToken<List<Theater>>(){}.getType());
            movies = gson.fromJson(root.get("movies"), new TypeToken<List<Movie>>(){}.getType());
            shows = gson.fromJson(root.get("shows"), new TypeToken<List<Show>>(){}.getType());
            seats = gson.fromJson(root.get("seats"), new TypeToken<List<Seat>>(){}.getType());
            users = gson.fromJson(root.get("users"), new TypeToken<List<User>>(){}.getType());
            tickets = new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database.json", e);
        }
    }

    // --- Theater queries ---
    public List<Theater> getTheatersByCity(String city) {
        return theaters.stream()
                .filter(t -> t.getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
    }

    public Theater getTheaterById(String theaterId) {
        return theaters.stream()
                .filter(t -> t.getTheaterId().equals(theaterId))
                .findFirst().orElse(null);
    }

    // --- Movie queries ---
    public List<Movie> getMoviesByCity(String city) {
        Set<String> theaterIds = getTheatersByCity(city).stream()
                .map(Theater::getTheaterId)
                .collect(Collectors.toSet());
        return movies.stream()
                .filter(m -> theaterIds.contains(m.getTheaterId()))
                .collect(Collectors.toList());
    }

    public List<Movie> getMoviesByTheaterId(String theaterId) {
        return movies.stream()
                .filter(m -> m.getTheaterId().equals(theaterId))
                .collect(Collectors.toList());
    }

    public Movie getMovieById(String movieId) {
        return movies.stream()
                .filter(m -> m.getMovieId().equals(movieId))
                .findFirst().orElse(null);
    }

    // --- Show queries ---
    public List<Show> getShowsByMovieId(String movieId) {
        return shows.stream()
                .filter(s -> s.getMovieId().equals(movieId))
                .collect(Collectors.toList());
    }

    public Show getShowById(String showId) {
        return shows.stream()
                .filter(s -> s.getShowId().equals(showId))
                .findFirst().orElse(null);
    }

    // --- Seat queries ---
    public List<Seat> getSeatsByShowId(String showId) {
        return seats.stream()
                .filter(s -> s.getShowId().equals(showId))
                .collect(Collectors.toList());
    }

    public Seat getSeatById(String seatId) {
        return seats.stream()
                .filter(s -> s.getSeatId().equals(seatId))
                .findFirst().orElse(null);
    }

    // --- User queries ---
    public User getUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    // --- Ticket operations ---
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public Ticket getTicketById(String ticketId) {
        return tickets.stream()
                .filter(t -> t.getTicketId().equals(ticketId))
                .findFirst().orElse(null);
    }

    public List<Ticket> getTickets() { return tickets; }

    // --- Admin operations ---
    public void addShow(Show show) { shows.add(show); }
    public void addMovie(Movie movie) { movies.add(movie); }

    public List<Show> getAllShows() { return shows; }
    public List<Movie> getAllMovies() { return movies; }
    public List<Seat> getAllSeats() { return seats; }
}
