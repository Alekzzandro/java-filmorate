package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final LikeStorage likeStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService,
                         LikeStorage likeStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.likeStorage = likeStorage;
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "FROM films f " +
                "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long filmId = rs.getLong("id");
            Mpa mpa = new Mpa(
                    rs.getLong("mpa_rating_id"),
                    rs.getString("name"),
                    rs.getString("description")
            );
            return new Film(
                    filmId,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(), // Лайки будут добавлены позже
                    mpa,
                    null // Жанры будут добавлены позже
            );
        });

        if (films.isEmpty()) {
            throw new FilmNotFoundException("Фильмы не найдены!");
        }

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

    @Override
    public Film create(Film film) {
        if (!FilmService.isReleaseDateValid(film.getReleaseDate())) {
            throw new InvalidReleaseDateException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new MpaNotFoundException("MPA rating is required");
        }

        Mpa mpa = mpaService.getMpaById(film.getMpa().getId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(film.toMap());
        film.setId(generatedId.longValue());

        film.setMpa(mpa);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genre.setName(genreService.getGenreById(genre.getId()).getName());
            }
            genreService.putGenres(film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        if (film == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_rating_id = ? WHERE id = ?";
        if (jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) != 0) {
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
            if (film.getGenres() != null) {
                Collection<Genre> sortedGenres = film.getGenres().stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toList());
                film.setGenres(new LinkedHashSet<>(sortedGenres));
                for (Genre genre : film.getGenres()) {
                    genre.setName(genreService.getGenreById(genre.getId()).getName());
                }
            }
            genreService.putGenres(film);
            return film;
        } else {
            throw new FilmNotFoundException("Фильм с ID=" + film.getId() + " не найден!");
        }
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                "WHERE f.id = ?";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sql, filmId);
        if (!filmRows.first()) {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }

        Mpa mpa = new Mpa(
                filmRows.getLong("mpa_rating_id"),
                filmRows.getString("mpa_name"),
                filmRows.getString("mpa_description")
        );

        Set<Genre> genres = genreService.getFilmGenres(filmId);
        Set<Long> likes = likeStorage.getLikes(filmId);

        Film film = new Film(
                filmRows.getLong("id"),
                filmRows.getString("name"),
                filmRows.getString("description"),
                filmRows.getDate("release_date").toLocalDate(),
                filmRows.getInt("duration"),
                likes,
                mpa,
                genres
        );

        return film;
    }

    @Override
    public Film delete(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Film film = getFilmById(filmId);
        String sqlQuery = "DELETE FROM films WHERE id = ?";
        if (jdbcTemplate.update(sqlQuery, filmId) == 0) {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }
        return film;
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
}