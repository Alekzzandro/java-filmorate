package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.stream.Collectors;

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
        // Получаем популярные фильмы с количеством лайков
        String getPopularQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "FROM films f LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";

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
                    new HashSet<>(),
                    mpa,
                    null
            );
        }, count);

        // Получаем все лайки для этих фильмов
        Map<Long, Set<Long>> filmLikesMap = getFilmLikesMap(films);

        // Получаем все жанры для этих фильмов
        Map<Long, Set<Genre>> filmGenresMap = getFilmGenresMap(films);

        // Добавляем лайки и жанры к фильмам
        for (Film film : films) {
            film.setLikes(filmLikesMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }

    private Map<Long, Set<Long>> getFilmLikesMap(List<Film> films) {
        if (films.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN ("
                + films.stream().map(film -> "?").collect(Collectors.joining(", ")) + ")";

        List<Object> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        return jdbcTemplate.query(sql, filmIds.toArray(), (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            return new AbstractMap.SimpleEntry<>(filmId, userId);
        }).stream().collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ));
    }

    private Map<Long, Set<Genre>> getFilmGenresMap(List<Film> films) {
        if (films.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN ("
                + films.stream().map(film -> "?").collect(Collectors.joining(", ")) + ")";

        List<Object> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        return jdbcTemplate.query(sql, filmIds.toArray(), (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("id"), rs.getString("name"));
            return new AbstractMap.SimpleEntry<>(filmId, genre);
        }).stream().collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ));
    }

    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId)
                .stream()
                .collect(Collectors.toSet());
    }
}