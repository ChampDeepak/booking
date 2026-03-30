package com.booking.admin;

import com.booking.common.entity.Movie;
import com.booking.common.entity.Show;
import com.booking.common.entity.Theater;

import java.util.List;

public interface IAdminService {
    Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                 String startTime, String endTime, double showBasePrice);

    Movie addMovie(String movieId, String theaterId, String movieName, String language, String genre);

    Theater addTheater(String theaterId, String theaterName, String city, List<String> auditoriumNames);
}
