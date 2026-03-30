package com.booking.admin;

import com.booking.common.Database;
import com.booking.common.entity.Movie;
import com.booking.common.entity.Show;
import com.booking.common.entity.Theater;
import com.booking.common.entity.User;
import com.booking.common.enums.Permission;
import com.booking.common.exception.ValidationException;

import java.util.List;

public class ProxyAdminService implements IAdminService {
    private final IAdminService service;
    private final Database db;

    public ProxyAdminService(IAdminService service, Database db) {
        this.service = service;
        this.db = db;
    }

    private User authorize(String userEmail, Permission permission) {
        User user = db.getUserByEmail(userEmail);
        if (user == null) {
            throw new ValidationException("User not found: " + userEmail);
        }
        if (!user.getRole().hasPermission(permission)) {
            throw new ValidationException(permission + " permission required. User " + userEmail
                    + " has role: " + user.getRole());
        }
        return user;
    }

    @Override
    public Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                        String startTime, String endTime, double showBasePrice) {
        return service.addShow(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
    }

    @Override
    public Movie addMovie(String movieId, String theaterId, String movieName, String language, String genre) {
        return service.addMovie(movieId, theaterId, movieName, language, genre);
    }

    @Override
    public Theater addTheater(String theaterId, String theaterName, String city, List<String> auditoriumNames) {
        return service.addTheater(theaterId, theaterName, city, auditoriumNames);
    }

    public Show addShowAsUser(String userEmail, String showId, String movieId, String theaterId,
                              String auditoriumId, String startTime, String endTime, double showBasePrice) {
        authorize(userEmail, Permission.ADD_SHOW);
        return service.addShow(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
    }

    public Movie addMovieAsUser(String userEmail, String movieId, String theaterId,
                                String movieName, String language, String genre) {
        authorize(userEmail, Permission.ADD_MOVIE);
        return service.addMovie(movieId, theaterId, movieName, language, genre);
    }

    public Theater addTheaterAsUser(String userEmail, String theaterId, String theaterName,
                                    String city, List<String> auditoriumNames) {
        authorize(userEmail, Permission.ADD_THEATER);
        return service.addTheater(theaterId, theaterName, city, auditoriumNames);
    }
}
