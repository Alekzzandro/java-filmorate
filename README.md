# Filmorate Database Schema

![ER-Diagram](Diagram.png)

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

#### 1. **films**
- **id**: Уникальный идентификатор фильма (первичный ключ).
- **name**: Название фильма.
- **description**: Описание фильма.
- **release_date**: Дата релиза фильма.
- **duration**: Продолжительность фильма в минутах.
- **mpa_rating_id**: Внешний ключ на таблицу `mpa_rating`.

#### 2. **genres**
- **id**: Уникальный идентификатор жанра (первичный ключ).
- **name**: Название жанра.

#### 3. **film_genres**
- **film_id**: Внешний ключ на таблицу `film`.
- **genre_id**: Внешний ключ на таблицу `genre`.
- **Составной первичный ключ**: (`film_id`, `genre_id`).

#### 4. **users**
- **id**: Уникальный идентификатор пользователя (первичный ключ).
- **email**: Электронная почта пользователя.
- **login**: Логин пользователя.
- **name**: Имя пользователя.
- **birthday**: Дата рождения.

#### 5. **likes**
- **film_id**: Внешний ключ на таблицу `film`.
- **user_id**: Внешний ключ на таблицу `user`.
- **Составной первичный ключ**: (`film_id`, `user_id`).

#### 6. **friends**
- **user_id**: Внешний ключ на таблицу `user` (первый пользователь).
- **friend_id**: Внешний ключ на таблицу `user` (второй пользователь).
- **status_id**: Внешний ключ на таблицу `friendship_status`.
- **Составной первичный ключ**: (`user_id`, `friend_id`).

#### 7. **friends**
- **user_id**: Внешний ключ на таблицу users (первый пользователь).
- **friend_id**: Внешний ключ на таблицу users (второй пользователь).
- **status **: Статус дружбы (например, "PENDING", "CONFIRMED").
- **Составной первичный ключ**: (user_id, friend_id).

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

---

## Примеры запросов

#### 1. Получить топ-10 фильмов по лайкам
```sql
SELECT 
    f.id AS film_id,
    f.name AS film_name,
    COUNT(l.user_id) AS like_count
FROM 
    films f
LEFT JOIN 
    likes l ON f.id = l.film_id
GROUP BY 
    f.id, f.name
ORDER BY 
    like_count DESC
LIMIT 10;
```
#### 2. Получить все друзья пользователя с ID = 1
```sql
SELECT 
    u.id AS friend_id,
    u.name AS friend_name,
    u.email AS friend_email,
    u.login AS friend_login,
    f.status
FROM 
    friends f
JOIN 
    users u ON f.friend_id = u.id
WHERE 
    f.user_id = 1;
```
#### 3. Получить общих друзей двух пользователей с ID = 1 и ID = 2
```sql
SELECT 
    u.id AS common_friend_id,
    u.name AS common_friend_name,
    u.email AS common_friend_email,
    u.login AS common_friend_login
FROM 
    friends f1
JOIN 
    friends f2 ON f1.friend_id = f2.friend_id
JOIN 
    users u ON f1.friend_id = u.id
WHERE 
    f1.user_id = 1
    AND f2.user_id = 2
    AND f1.status = 'CONFIRMED'
    AND f2.status = 'CONFIRMED';
```
#### 4. Получить все жанры фильма с ID = 5
```sql
SELECT 
    g.id AS genre_id,
    g.name AS genre_name
FROM 
    film_genres fg
JOIN 
    genres g ON fg.genre_id = g.id
WHERE 
    fg.film_id = 5;
```
#### 5. Получить всех пользователей с возрастом больше 18 лет
```sql
SELECT 
    id,
    email,
    login,
    name,
    birthday
FROM 
    users
WHERE 
    DATE_PART('year', AGE(CURRENT_DATE, birthday)) > 18;
```
