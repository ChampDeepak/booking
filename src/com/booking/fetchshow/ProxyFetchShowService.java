package com.booking.fetchshow;

import com.booking.common.Database;
import com.booking.common.entity.Show;
import com.booking.common.exception.ValidationException;
import java.util.List;

public class ProxyFetchShowService implements IFetchShowService {
    private final IFetchShowService service;
    private final Database db;

    public ProxyFetchShowService(IFetchShowService service, Database db) {
        this.service = service;
        this.db = db;
    }

    @Override
    public List<Show> getShowsByMovie(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new ValidationException("Movie ID cannot be empty");
        }
        if (db.getMovieById(movieId) == null) {
            throw new ValidationException("Movie not found: " + movieId);
        }
        return service.getShowsByMovie(movieId);
    }
}
