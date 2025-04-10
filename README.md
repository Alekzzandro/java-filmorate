# Filmorate Database Schema

![ER-Diagram]([untitled.png))

## Оглавление

- [Описание проекта](#описание-проекта)
- [Таблицы и связи](#таблицы-и-связи)
- [Примеры запросов](#примеры-запросов)

---

## Описание проекта

Filmorate позволяет пользователям:
- Добавлять и обновлять фильмы.
- Добавлять и удалять друзей.
- Ставить лайки фильмам.
- Просматривать топ популярных фильмов.
- Управлять жанрами и возрастными рейтингами фильмов.

---

### Описание схемы

База данных предназначена для хранения информации о фильмах, пользователях, их лайках и социальных связях. Схема состоит из следующих таблиц:

#### 1. **film**
- **film_id**: Уникальный идентификатор фильма (первичный ключ).
- **name**: Название фильма.
- **description**: Описание фильма.
- **release_date**: Дата релиза фильма.
- **duration**: Продолжительность фильма в минутах.
- **mpa_rating_id**: Внешний ключ на таблицу `mpa_rating`.

#### 2. **genre**
- **genre_id**: Уникальный идентификатор жанра (первичный ключ).
- **name**: Название жанра.

#### 3. **film_genre**
- **film_id**: Внешний ключ на таблицу `film`.
- **genre_id**: Внешний ключ на таблицу `genre`.
- **Составной первичный ключ**: (`film_id`, `genre_id`).

#### 4. **user**
- **user_id**: Уникальный идентификатор пользователя (первичный ключ).
- **email**: Электронная почта пользователя.
- **login**: Логин пользователя.
- **name**: Имя пользователя.
- **birthday**: Дата рождения.

#### 5. **user_like**
- **film_id**: Внешний ключ на таблицу `film`.
- **user_id**: Внешний ключ на таблицу `user`.
- **Составной первичный ключ**: (`film_id`, `user_id`).

#### 6. **friends**
- **user_id**: Внешний ключ на таблицу `user` (первый пользователь).
- **friend_id**: Внешний ключ на таблицу `user` (второй пользователь).
- **status_id**: Внешний ключ на таблицу `friendship_status`.
- **Составной первичный ключ**: (`user_id`, `friend_id`).

#### 7. **mpa_rating**
- **mpa_rating_id**: Уникальный идентификатор рейтинга (первичный ключ).
- **name**: Название рейтинга.

#### 8. **friendship_status**
- **status_id**: Уникальный идентификатор статуса дружбы (первичный ключ).
- **name**: Название статуса (например, "PENDING", "CONFIRMED").

---

## Таблицы и связи

### Связи между таблицами

1. **`film` ↔ `mpa_rating`**:
   - Связь "один ко многим": один рейтинг может быть связан с несколькими фильмами (`film.mpa_rating_id` ссылается на `mpa_rating.mpa_rating_id`).

2. **`film` ↔ `genre` через `film_genre`**:
   - Связь "многие ко многим":
     - `film_genre.film_id` ссылается на `film.film_id`.
     - `film_genre.genre_id` ссылается на `genre.genre_id`.

3. **`film` ↔ `user` через `user_like`**:
   - Связь "многие ко многим":
     - `user_like.film_id` ссылается на `film.film_id`.
     - `user_like.user_id` ссылается на `user.user_id`.

4. **`user` ↔ `user` через `friends`**:
   - Связь "многие ко многим":
     - `friends.user_id` ссылается на `user.user_id`.
     - `friends.friend_id` ссылается на `user.user_id`.

5. **`friends` ↔ `friendship_status`**:
   - Связь "один ко многим": один статус дружбы может быть связан с несколькими записями в таблице `friends` (`friends.status_id` ссылается на `friendship_status.status_id`).

---

## Примеры запросов

#### 1. Получить топ-10 фильмов по лайкам
```sql
SELECT f.film_id, f.name, COUNT(l.user_id) AS likes_count
FROM film f
LEFT JOIN user_like l ON f.film_id = l.film_id
GROUP BY f.film_id, f.name
ORDER BY likes_count DESC
LIMIT 10;
```
#### 2. Получить все друзья пользователя с ID = 1
```sql
SELECT u.user_id, u.login, u.name
FROM friends fr
JOIN user u ON fr.friend_id = u.user_id
WHERE fr.user_id = 1 AND fr.status_id = (
    SELECT status_id FROM friendship_status WHERE name = 'CONFIRMED'
);
```
#### 3. Получить общих друзей двух пользователей с ID = 1 и ID = 2
```sql
SELECT u.user_id, u.login, u.name
FROM friends f1
JOIN friends f2 ON f1.friend_id = f2.friend_id
JOIN user u ON f1.friend_id = u.user_id
WHERE f1.user_id = 1 AND f2.user_id = 2;
```
#### 4. Получить все жанры фильма с ID = 5
```sql
SELECT g.genre_id, g.name AS genre_name
FROM film_genre fg
JOIN genre g ON fg.genre_id = g.genre_id
WHERE fg.film_id = 5;
```
#### 5. Получить всех пользователей с возрастом больше 18 лет
```sql
SELECT u.user_id, u.login, u.name
FROM user u
WHERE EXTRACT(YEAR FROM AGE(u.birthday)) > 18;
```
