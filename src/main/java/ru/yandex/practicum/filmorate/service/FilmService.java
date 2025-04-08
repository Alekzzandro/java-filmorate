package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(UserService userService, FilmStorage filmStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        film = film.toBuilder()
                .id(getNextId())
                .build();

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film film) {
        Film oldFilm = findFilmById(film.getId());

        film.getMovieRating().addAll(oldFilm.getMovieRating());

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    public Film findById(Long filmId) {
        log.info("Поиск фильма с id {}", filmId);
        return findFilmById(filmId);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);
        User user = userService.findById(userId);

        if (film.getMovieRating().contains(userId)) {
            throw new IllegalArgumentException("Пользователь уже ставил лайк этому фильму");
        }

        film.getMovieRating().add(userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);
        User user = userService.findById(userId);

        film.getMovieRating().remove(userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getTopFilms(count);
    }

    private Film findFilmById(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    private final AtomicLong idGenerator = new AtomicLong(0);

    private long getNextId() {
        return idGenerator.incrementAndGet();
    }
}