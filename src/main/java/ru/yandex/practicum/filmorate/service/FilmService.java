package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
    }

    // Методы для работы с фильмами
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film delete(Long filmId) {
        return filmStorage.delete(filmId);
    }

    // Методы для работы с лайками
    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с ID=" + filmId + " не найден!");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        likeStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с ID=" + filmId + " не найден!");
        }
        if (!film.getLikes().contains(userId)) {
            throw new UserNotFoundException("Лайк от пользователя с ID=" + userId + " не найден!");
        }
        likeStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopular(Long count) {
        if (count < 1) {
            throw new ValidationException("Количество фильмов для вывода не должно быть меньше 1");
        }
        return likeStorage.getPopular(count);
    }

    // Валидация фильма
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым!");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не должно превышать 200 символов!");
        }
        if (film.getReleaseDate() == null || !isReleaseDateValid(film.getReleaseDate())) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной!");
        }
    }

    public static boolean isReleaseDateValid(LocalDate releaseDate) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return releaseDate != null && releaseDate.isAfter(minDate);
    }
}