package com.booking.browse;

import com.booking.common.Database;
import com.booking.common.entity.Movie;
import com.booking.common.entity.Theater;
import com.booking.common.exception.ValidationException;
import java.util.List;

public class ProxyBrowseService implements IBrowseService {
    private final IBrowseService service;
    private final Database db;

    public ProxyBrowseService(IBrowseService service, Database db) {
        this.service = service;
        this.db = db;
    }

    @Override
    public List<Movie> getMoviesByCity(String city) {
        if (city == null || city.isBlank()) {
            throw new ValidationException("City cannot be empty");
        }
        return service.getMoviesByCity(city);
    }

    @Override
    public List<Theater> getTheatersByCity(String city) {
        if (city == null || city.isBlank()) {
            throw new ValidationException("City cannot be empty");
        }
        return service.getTheatersByCity(city);
    }

    @Override
    public List<Movie> getMoviesByTheater(String theaterId) {
        if (theaterId == null || theaterId.isBlank()) {
            throw new ValidationException("Theater ID cannot be empty");
        }
        if (db.getTheaterById(theaterId) == null) {
            throw new ValidationException("Theater not found: " + theaterId);
        }
        return service.getMoviesByTheater(theaterId);
    }
}
