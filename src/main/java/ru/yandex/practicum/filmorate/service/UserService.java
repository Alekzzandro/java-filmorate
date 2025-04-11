package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        user = user.toBuilder()
                .id(getNextId())
                .build();

        userStorage.create(user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    public User update(User user) {
        User oldUser = findUserById(user.getId());

        user.getFriends().addAll(oldUser.getFriends());

        userStorage.update(user);
        log.info("Юзер c id {} обновлен", user.getId());
        return user;
    }

    public User findById(Long userId) {
        log.info("Поиск пользователя с id {}", userId);
        return findUserById(userId);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = findUserById(userId);
        User friend = findUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("{} и {} теперь друзья!", user.getName(), friend.getName());
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        User user = findUserById(userId);
        User friend = findUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("{} и {} больше не друзья!", user.getName(), friend.getName());
    }

    public List<User> commonFriends(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);

        return user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .map(userStorage::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    public List<User> getFriends(Long userId) {
        User user = findUserById(userId);

        return user.getFriends().stream()
                .map(userStorage::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    private User findUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private final AtomicLong idGenerator = new AtomicLong(0);

    private long getNextId() {
        return idGenerator.incrementAndGet();
    }
}