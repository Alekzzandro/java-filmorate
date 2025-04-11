package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Film create(Film film) {
        long nextId = idGenerator.incrementAndGet();
        film = film.toBuilder().id(nextId).build();
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new IllegalArgumentException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getMovieRating().size(), f1.getMovieRating().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}