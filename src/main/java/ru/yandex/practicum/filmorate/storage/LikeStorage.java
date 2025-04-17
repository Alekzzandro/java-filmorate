package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.HashSet;
import java.util.List;


@Component
public class LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public LikeStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopular(Long count) {
        String getPopularQuery = "SELECT id, name, description, release_date, duration, mpa_rating_id " +
                "FROM films LEFT JOIN likes ON films.id = likes.film_id " +
                "GROUP BY films.id ORDER BY COUNT(likes.user_id) DESC LIMIT ?";

        List<Film> films = jdbcTemplate.query(getPopularQuery, (rs, rowNum) -> {
            Long filmId = rs.getLong("id");
            Mpa mpa = mpaService.getMpaById(rs.getLong("mpa_rating_id"));
            if (mpa == null) {
                throw new MpaNotFoundException("Рейтинг с ID=" + rs.getLong("mpa_rating_id") + " не найден!");
            }
            return new Film(
                    filmId,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(getLikes(filmId)),
                    mpa,
                    genreService.getFilmGenres(filmId)
            );
        }, count);

        return films;
    }

    public List<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }
}