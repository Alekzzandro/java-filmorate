package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, User> emailToUserMap = new HashMap<>();
    private int nextId = 1;

    @Override
    public User addUser(User user) {
        user = user.toBuilder().id(getNextId()).friends(new HashSet<>()).likes(new HashSet<>()).build();
        users.put(user.getId(), user);
        emailToUserMap.put(user.getEmail(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user.getLikes() == null) {
            user.setLikes(new HashSet<>());
        }
        users.put(user.getId(), user);
        emailToUserMap.put(user.getEmail(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(emailToUserMap.get(email));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private int getNextId() {
        return nextId++;
    }
}