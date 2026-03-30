package com.booking.browse;

import com.booking.common.Database;
import com.booking.common.entity.Movie;
import com.booking.common.entity.Theater;
import java.util.List;

public class BrowseService implements IBrowseService {
    private final Database db;

    public BrowseService(Database db) {
        this.db = db;
    }

    @Override
    public List<Movie> getMoviesByCity(String city) {
        return db.getMoviesByCity(city);
    }

    @Override
    public List<Theater> getTheatersByCity(String city) {
        return db.getTheatersByCity(city);
    }

    @Override
    public List<Movie> getMoviesByTheater(String theaterId) {
        return db.getMoviesByTheaterId(theaterId);
    }
}
