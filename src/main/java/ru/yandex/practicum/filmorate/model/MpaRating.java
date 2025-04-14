package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public enum MpaRating {
    G("У фильма нет возрастных ограничений"),
    PG("Детям рекомендуется смотреть фильм с родителями"),
    PG_13("Детям до 13 лет просмотр не желателен"),
    R("Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("Лицам до 18 лет просмотр запрещён");

    private final String description;

    MpaRating(String description) {
        this.description = description;
    }
}