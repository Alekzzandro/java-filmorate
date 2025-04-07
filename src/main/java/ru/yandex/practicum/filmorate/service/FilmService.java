package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAllFilms() {
        log.info("Запрошены все фильмы");
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) throws ValidationException {
        log.info("Добавление нового фильма: {}", film);
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) throws ValidationException, NotFoundException {
        log.info("Обновление фильма: {}", film);
        if (film.getId() <= 0) {
            log.error("ID фильма не указан или некорректен: {}", film.getId());
            throw new ValidationException("ID фильма должен быть указан и положительным числом");
        }
        if (!filmStorage.getFilmById(film.getId()).isPresent()) {
            log.error("Фильм с указанным ID не найден: {}", film.getId());
            throw new NotFoundException("Фильм с указанным ID не найден");
        }
        validateFilm(film);
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        log.info("Запрос фильма по ID: {}", id);
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с указанным ID не найден"));
    }

    public void addLike(int filmId, int userId) throws NotFoundException {
        log.info("Добавление лайка пользователем {} фильму {}", userId, filmId);
        Film film = getFilmById(filmId);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(int filmId, int userId) throws NotFoundException {
        log.info("Удаление лайка пользователем {} фильму {}", userId, filmId);
        Film film = getFilmById(filmId);

        if (film.getLikes() == null || !film.getLikes().contains(userId)) {
            log.warn("Лайк пользователя {} для фильма {} не найден", userId, filmId);
            throw new NotFoundException("Лайк пользователя для фильма не найден");
        }

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Запрошены {} самых популярных фильмов", count);
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1); // Сортировка по убыванию количества лайков
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isEmpty()) {
            log.error("Название фильма не может быть пустым: {}", film);
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Максимальная длина описания — 200 символов: {}", film.getDescription());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.error("Дата релиза не указана: {}", film);
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза — не раньше 28 декабря 1895 года: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Продолжительность фильма должна быть положительным числом: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}