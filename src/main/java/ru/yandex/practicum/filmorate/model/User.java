package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class User {
    Integer id;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^$|^\\S+$", message = "Логин не должен содержать пробелы")
    String login;

    @NonFinal
    String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    @NonFinal
    Set<Integer> friends;

    @NonFinal
    Set<Integer> likes;

    public User(int id, String email, String login, String name, LocalDate birthday, Set<Integer> friends, Set<Integer> likes) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends != null ? friends : new HashSet<>();
        this.likes = likes != null ? likes : new HashSet<>();

        if (name == null || name.isBlank()) {
            this.name = login;
        }
    }

    public User() {
    }
}