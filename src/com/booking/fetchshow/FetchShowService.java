package com.booking.fetchshow;

import com.booking.common.Database;
import com.booking.common.entity.Show;
import java.util.List;

public class FetchShowService implements IFetchShowService {
    private final Database db;

    public FetchShowService(Database db) {
        this.db = db;
    }

    @Override
    public List<Show> getShowsByMovie(String movieId) {
        return db.getShowsByMovieId(movieId);
    }
}
