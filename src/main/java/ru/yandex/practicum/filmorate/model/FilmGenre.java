package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FilmGenre {
    COMEDY,
    DRAMA,
    CARTOON,
    THRILLER,
    DOCUMENTARY,
    ACTION;

    @JsonValue
    public String getName() {
        return name();
    }
}