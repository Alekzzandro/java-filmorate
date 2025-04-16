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
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
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
                    new HashSet<>(likeStorage.getLikes(filmId)),
                    mpa,
                    genreService.getFilmGenres(filmId)
            );
        });

        if (films.isEmpty()) {
            throw new FilmNotFoundException("Фильмы не найдены!");
        }

        return films;
    }

    @Override
    public Film create(Film film) {

        // Проверяем дату релиза перед сохранением
        if (!FilmService.isReleaseDateValid(film.getReleaseDate())) {
            throw new InvalidReleaseDateException(
                    "Дата релиза должна быть не раньше 28 декабря 1895 года."
            );
        }

        // Проверяем, что MPA_RATING_ID не равен NULL
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new MpaNotFoundException("MPA rating is required");
        }

        // Проверяем, что MPA с указанным ID существует
        Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new MpaNotFoundException("Рейтинг с ID=" + film.getMpa().getId() + " не найден!");
        }

        // Вставка фильма в таблицу "films" и получение сгенерированного ID
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(film.toMap());
        film.setId(generatedId.longValue());

        // Установка MPA
        try {
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        } catch (MpaNotFoundException e) {
            throw e; // Перебрасываем исключение, чтобы оно обработалось GlobalExceptionHandler
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении MPA: " + e.getMessage(), e);
        }

        // Обработка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genre.setName(genreService.getGenreById(genre.getId()).getName());
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при получении жанра: " + e.getMessage(), e);
                }
            }
            genreService.putGenres(film); // Сохранение связей между фильмом и жанрами
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
                Collection<Genre> sortGenres = film.getGenres().stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toList());
                film.setGenres(new LinkedHashSet<>(sortGenres));
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
        Film film;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", filmId);
        if (filmRows.first()) {
            Mpa mpa = mpaService.getMpaById(filmRows.getLong("mpa_rating_id"));
            if (mpa == null) {
                throw new MpaNotFoundException("Рейтинг с ID=" + filmRows.getLong("mpa_rating_id") + " не найден!");
            }
            Set<Genre> genres = genreService.getFilmGenres(filmId);
            film = new Film(
                    filmRows.getLong("id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    new HashSet<>(likeStorage.getLikes(filmRows.getLong("id"))),
                    mpa,
                    genres);
        } else {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }
        if (film.getGenres().isEmpty()) {
            film.setGenres(null);
        }
        return film;
    }

    @Override
    public Film delete(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Film film = getFilmById(filmId);
        String sqlQuery = "DELETE FROM films WHERE id = ? ";
        if (jdbcTemplate.update(sqlQuery, filmId) == 0) {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }
        return film;
    }
}