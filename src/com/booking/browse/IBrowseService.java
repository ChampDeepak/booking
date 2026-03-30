package com.booking.browse;

import com.booking.common.entity.Movie;
import com.booking.common.entity.Theater;
import java.util.List;

public interface IBrowseService {
    List<Movie> getMoviesByCity(String city);
    List<Theater> getTheatersByCity(String city);
    List<Movie> getMoviesByTheater(String theaterId);
}
