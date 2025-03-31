package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FilmorateApplication.class)
public class FilmServiceTest {

	@Autowired
	private FilmService filmService;

	@MockBean
	private FilmStorage filmStorage;

	@BeforeEach
	public void setUp() {
		reset(filmStorage);
	}

	@Test
	public void testAddFilm() throws ValidationException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);

		when(filmStorage.addFilm(any(Film.class))).thenAnswer(invocation -> {
			Film addedFilm = invocation.getArgument(0);
			addedFilm.setId(1);
			return addedFilm;
		});

		Film addedFilm = filmService.addFilm(film);

		assertNotNull(addedFilm, "Фильм не добавлен");
		assertEquals(film.getName(), addedFilm.getName(), "Название фильма не совпадает");
		assertEquals(film.getDescription(), addedFilm.getDescription(), "Описание фильма не совпадает");
		assertEquals(film.getReleaseDate(), addedFilm.getReleaseDate(), "Дата релиза фильма не совпадает");
		assertEquals(film.getDuration(), addedFilm.getDuration(), "Продолжительность фильма не совпадает");
		assertEquals(1, addedFilm.getId(), "ID фильма не установлен");

		verify(filmStorage, times(1)).addFilm(film);
	}

	@Test
	public void testAddFilmEmptyName() {
		Film film = new Film();
		film.setName("");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			filmService.addFilm(film);
		});

		assertEquals("Название фильма не может быть пустым", exception.getMessage());
		verify(filmStorage, never()).addFilm(film);
	}

	@Test
	public void testAddFilmLongDescription() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("a".repeat(201));
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			filmService.addFilm(film);
		});

		assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
		verify(filmStorage, never()).addFilm(film);
	}

	@Test
	public void testAddFilmInvalidReleaseDate() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(1895, 12, 27)); // дата раньше 28 декабря 1895
		film.setDuration(120);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			filmService.addFilm(film);
		});

		assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
		verify(filmStorage, never()).addFilm(film);
	}

	@Test
	public void testAddFilmNegativeDuration() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(-10);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			filmService.addFilm(film);
		});

		assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
		verify(filmStorage, never()).addFilm(film);
	}

	@Test
	public void testUpdateFilm() throws ValidationException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		film.setId(1);

		when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
		when(filmStorage.updateFilm(any(Film.class))).thenAnswer(invocation -> invocation.getArgument(0));

		film.setName("Updated Film");
		film.setDuration(150);

		Film updatedFilm = filmService.updateFilm(film);

		assertNotNull(updatedFilm, "Фильм не обновлен");
		assertEquals(film.getName(), updatedFilm.getName(), "Название фильма не совпадает");
		assertEquals(film.getDescription(), updatedFilm.getDescription(), "Описание фильма не совпадает");
		assertEquals(film.getReleaseDate(), updatedFilm.getReleaseDate(), "Дата релиза фильма не совпадает");
		assertEquals(film.getDuration(), updatedFilm.getDuration(), "Продолжительность фильма не совпадает");
		assertEquals(1, updatedFilm.getId(), "ID фильма не установлен");

		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, times(1)).updateFilm(film);
	}

	@Test
	public void testUpdateFilmUnknownId() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		film.setId(1);

		when(filmStorage.getFilmById(1)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			filmService.updateFilm(film);
		});

		assertEquals("Фильм с указанным ID не найден", exception.getMessage());
		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, never()).updateFilm(film);
	}

	@Test
	public void testGetFilmById() throws ValidationException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		film.setId(1);

		when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));

		Film retrievedFilm = filmService.getFilmById(1);

		assertNotNull(retrievedFilm, "Фильм не найден");
		assertEquals(film.getName(), retrievedFilm.getName(), "Название фильма не совпадает");
		assertEquals(film.getDescription(), retrievedFilm.getDescription(), "Описание фильма не совпадает");
		assertEquals(film.getReleaseDate(), retrievedFilm.getReleaseDate(), "Дата релиза фильма не совпадает");
		assertEquals(film.getDuration(), retrievedFilm.getDuration(), "Продолжительность фильма не совпадает");
		assertEquals(1, retrievedFilm.getId(), "ID фильма не установлен");

		verify(filmStorage, times(1)).getFilmById(1);
	}

	@Test
	public void testGetFilmByIdUnknownId() {
		when(filmStorage.getFilmById(1)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			filmService.getFilmById(1);
		});

		assertEquals("Фильм с указанным ID не найден", exception.getMessage());
		verify(filmStorage, times(1)).getFilmById(1);
	}

	@Test
	public void testAddLike() throws ValidationException, NotFoundException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		film.setId(1);
		film.setLikes(new HashSet<>());

		when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
		when(filmStorage.updateFilm(any(Film.class))).thenAnswer(invocation -> invocation.getArgument(0));

		filmService.addLike(1, 101);

		assertTrue(film.getLikes().contains(101), "Лайк не добавлен");

		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, times(1)).updateFilm(film);
	}

	@Test
	public void testAddLikeUnknownFilmId() {
		when(filmStorage.getFilmById(1)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			filmService.addLike(1, 101);
		});

		assertEquals("Фильм с указанным ID не найден", exception.getMessage());
		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, never()).updateFilm(any(Film.class));
	}

	@Test
	public void testRemoveLike() throws ValidationException, NotFoundException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		film.setId(1);
		film.setLikes(new HashSet<>());
		film.getLikes().add(101);

		when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
		when(filmStorage.updateFilm(any(Film.class))).thenAnswer(invocation -> invocation.getArgument(0));

		filmService.removeLike(1, 101);

		assertFalse(film.getLikes().contains(101), "Лайк не удален");

		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, times(1)).updateFilm(film);
	}

	@Test
	public void testRemoveLikeUnknownFilmId() {
		when(filmStorage.getFilmById(1)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			filmService.removeLike(1, 101);
		});

		assertEquals("Фильм с указанным ID не найден", exception.getMessage());
		verify(filmStorage, times(1)).getFilmById(1);
		verify(filmStorage, never()).updateFilm(any(Film.class));
	}

	@Test
	public void testGetPopularFilms() throws ValidationException {
		Film film1 = new Film();
		film1.setName("Film 1");
		film1.setDescription("Description of film 1");
		film1.setReleaseDate(LocalDate.of(2023, 1, 1));
		film1.setDuration(120);
		film1.setId(1);
		film1.setLikes(new HashSet<>());
		film1.getLikes().add(101);
		film1.getLikes().add(102);

		Film film2 = new Film();
		film2.setName("Film 2");
		film2.setDescription("Description of film 2");
		film2.setReleaseDate(LocalDate.of(2023, 2, 1));
		film2.setDuration(150);
		film2.setId(2);
		film2.setLikes(new HashSet<>());
		film2.getLikes().add(101);

		Film film3 = new Film();
		film3.setName("Film 3");
		film3.setDescription("Description of film 3");
		film3.setReleaseDate(LocalDate.of(2023, 3, 1));
		film3.setDuration(180);
		film3.setId(3);
		film3.setLikes(new HashSet<>());

		when(filmStorage.getAllFilms()).thenReturn(List.of(film1, film2, film3));

		List<Film> popularFilms = filmService.getPopularFilms(10);

		assertEquals(3, popularFilms.size(), "Неверное количество популярных фильмов");
		assertEquals(film1.getId(), popularFilms.get(0).getId(), "Первый фильм в списке не соответствует ожидаемому");
		assertEquals(film2.getId(), popularFilms.get(1).getId(), "Второй фильм в списке не соответствует ожидаемому");
		assertEquals(film3.getId(), popularFilms.get(2).getId(), "Третий фильм в списке не соответствует ожидаемому");

		verify(filmStorage, times(1)).getAllFilms();
	}
}