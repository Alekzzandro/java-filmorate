package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, User> emailToUserMap = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException, DuplicatedDataException {
        validateUser(user);
        if (emailToUserMap.containsKey(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user = user.toBuilder().id(getNextId()).build();
        users.put(user.getId(), user);
        emailToUserMap.put(user.getEmail(), user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws ValidationException, ConditionsNotMetException, DuplicatedDataException {
        validateUser(user);
        if (user.getId() == null || user.getId() <= 0) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new ConditionsNotMetException("Пользователь с указанным ID не найден");
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty() && !user.getEmail().equals(existingUser.getEmail())) {
            if (emailToUserMap.containsKey(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            emailToUserMap.remove(existingUser.getEmail());
            emailToUserMap.put(user.getEmail(), user);
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        } else {
            existingUser.setName(existingUser.getLogin());
        }
        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            existingUser.setLogin(user.getLogin());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }
        log.info("Юзер c id {} обновлен", user.getId());
        return existingUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    private int getNextId() {
        return nextId++;
    }

    private void validateUser(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            throw new ValidationException("Имейл должен быть указан и содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
