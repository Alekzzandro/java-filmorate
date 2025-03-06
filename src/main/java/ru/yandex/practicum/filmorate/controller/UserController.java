package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user = user.toBuilder().id(getNextId()).build();
        users.put(user.getId(), user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() >= 0 || users.isEmpty()) {
            if (!users.containsKey(user.getId())) {
                log.error("Юзер с id {} не найден", user.getId());
                throw new NotFoundException("Юзер с id " + user.getId() + " не найден");
            }
        }

        users.put(user.getId(), user);
        log.info("Юзер c id {} обновлен", user.getId());
        return user;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    private int getNextId() {
        log.trace("Сгенерирован id: {}", users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1);
        return users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1;
    }
}