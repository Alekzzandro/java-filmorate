package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class User {
    Integer id;

    @NotNull
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^$|^\\S+$", message = "Логин не должен содержать пробелы")
    String login;

    @NonFinal
    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;

        if (name == null || name.isBlank()) {
            this.name = login;
        }
    }

    public User() {

    }
}