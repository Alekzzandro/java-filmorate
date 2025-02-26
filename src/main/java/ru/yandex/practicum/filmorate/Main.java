package ru.yandex.practicum.filmorate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;

import java.time.LocalDate;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final FilmService filmService;
    private final UserService userService;

    public Main(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            Film film1 = new Film(0, "Интерстеллар", "Фильм о космосе, времени и любви.", LocalDate.of(2014, 11, 7), 169);
            Film addedFilm1 = filmService.addFilm(film1);
            System.out.println("Добавлен фильм: " + addedFilm1);

            Film film2 = new Film(0, "Темный рыцарь", "Фильм о Бэтмене.", LocalDate.of(2008, 7, 18), 152);
            Film addedFilm2 = filmService.addFilm(film2);
            System.out.println("Добавлен фильм: " + addedFilm2);
        } catch (ValidationException e) {
            System.out.println("Ошибка валидации фильма: " + e.getMessage());
        }

        try {
            User user1 = new User(0, "user1@mail.com", "user1", "User One", LocalDate.of(1990, 5, 15));
            User addedUser1 = userService.addUser(user1);
            System.out.println("Добавлен пользователь: " + addedUser1);

            User user2 = new User(0, "user2@mail.com", "user2", "User Two", LocalDate.of(1992, 8, 25));
            User addedUser2 = userService.addUser(user2);
            System.out.println("Добавлен пользователь: " + addedUser2);
        } catch (ValidationException | DuplicatedDataException e) {
            System.out.println("Ошибка при добавлении пользователя: " + e.getMessage());
        }

        System.out.println("Список всех фильмов:");
        for (Film film : filmService.getAllFilms()) {
            System.out.println(film);
        }

        System.out.println("Список всех пользователей:");
        for (User user : userService.getAllUsers()) {
            System.out.println(user);
        }
    }
}
