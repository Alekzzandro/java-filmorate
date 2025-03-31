package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(@Valid User user) throws DuplicatedDataException {
        log.info("Добавление нового пользователя: {}", user);
        if (userStorage.getUserByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        return userStorage.addUser(user);
    }

    public User updateUser(@Valid User user) throws NotFoundException {
        log.info("Обновление пользователя: {}", user);
        User existingUser = userStorage.getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным ID не найден"));

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
        if (user.getEmail() != null && !user.getEmail().isEmpty() && !user.getEmail().equals(existingUser.getEmail())) {
            if (userStorage.getUserByEmail(user.getEmail()).isPresent()) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            existingUser.setEmail(user.getEmail());
        }

        log.info("Юзер c id {} обновлен", user.getId());
        return userStorage.updateUser(existingUser);
    }

    public User getUserById(int id) {
        log.info("Запрос пользователя по ID: {}", id);
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным ID не найден"));
    }

    public List<User> getAllUsers() {
        log.info("Запрошены все пользователи");
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) throws NotFoundException {
        log.info("Добавление друга {} к пользователю {}", friendId, userId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void removeFriend(int userId, int friendId) throws NotFoundException {
        log.info("Удаление друга {} у пользователя {}", friendId, userId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            friend.getFriends().remove(userId);
        }

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getFriends(int userId) {
        log.info("Запрошен список друзей пользователя {}", userId);
        User user = getUserById(userId);
        if (user.getFriends() == null) {
            return List.of();
        }
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.info("Запрошен список общих друзей пользователей {} и {}", userId, otherId);
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        if (user.getFriends() == null || otherUser.getFriends() == null) {
            return List.of();
        }

        Set<Integer> commonFriendsIds = new HashSet<>(user.getFriends());
        commonFriendsIds.retainAll(otherUser.getFriends());

        return commonFriendsIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}