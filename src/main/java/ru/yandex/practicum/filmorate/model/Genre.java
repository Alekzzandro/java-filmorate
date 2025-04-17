package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class Genre {
    private Long id;
    private String name;
}