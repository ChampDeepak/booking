package com.booking.admin;

import com.booking.common.Database;
import com.booking.common.entity.Auditorium;
import com.booking.common.entity.Movie;
import com.booking.common.entity.Show;
import com.booking.common.entity.Theater;

import java.util.ArrayList;
import java.util.List;

public class AdminService implements IAdminService {
    private final Database db;

    public AdminService(Database db) {
        this.db = db;
    }

    @Override
    public Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                        String startTime, String endTime, double showBasePrice) {
        Show show = new Show(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
        db.addShow(show);
        System.out.println("  [AdminService] Show added: " + show);
        return show;
    }

    @Override
    public Movie addMovie(String movieId, String theaterId, String movieName, String language, String genre) {
        Movie movie = new Movie(movieId, theaterId, movieName, language, genre);
        db.addMovie(movie);
        System.out.println("  [AdminService] Movie added: " + movie);
        return movie;
    }

    @Override
    public Theater addTheater(String theaterId, String theaterName, String city, List<String> auditoriumNames) {
        List<Auditorium> auditoriums = new ArrayList<>();
        for (int i = 0; i < auditoriumNames.size(); i++) {
            auditoriums.add(new Auditorium(theaterId + "-A" + (i + 1), auditoriumNames.get(i)));
        }
        Theater theater = new Theater(theaterId, theaterName, city, auditoriums);
        db.addTheater(theater);
        System.out.println("  [AdminService] Theater added: " + theater);
        return theater;
    }
}
