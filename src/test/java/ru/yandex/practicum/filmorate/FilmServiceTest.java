package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FilmorateApplication.class)
public class FilmServiceTest {
	private FilmService filmService;

	@BeforeEach
	public void setUp() {
		filmService = new FilmService();
	}

	@Test
	public void testAddFilm() throws ValidationException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);

		Film addedFilm = filmService.addFilm(film);

		assertNotNull(addedFilm, "Фильм не добавлен");
		assertEquals(film.getName(), addedFilm.getName(), "Название фильма не совпадает");
		assertEquals(film.getDescription(), addedFilm.getDescription(), "Описание фильма не совпадает");
		assertEquals(film.getReleaseDate(), addedFilm.getReleaseDate(), "Дата релиза фильма не совпадает");
		assertEquals(film.getDuration(), addedFilm.getDuration(), "Продолжительность фильма не совпадает");
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
	}

	@Test
	public void testUpdateFilm() throws ValidationException {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description of test film");
		film.setReleaseDate(LocalDate.of(2023, 1, 1));
		film.setDuration(120);
		filmService.addFilm(film);

		film.setName("Updated Film");
		film.setDuration(150);

		Film updatedFilm = filmService.updateFilm(film);

		assertNotNull(updatedFilm, "Фильм не обновлен");
		assertEquals(film.getName(), updatedFilm.getName(), "Название фильма не совпадает");
		assertEquals(film.getDescription(), updatedFilm.getDescription(), "Описание фильма не совпадает");
		assertEquals(film.getReleaseDate(), updatedFilm.getReleaseDate(), "Дата релиза фильма не совпадает");
		assertEquals(film.getDuration(), updatedFilm.getDuration(), "Продолжительность фильма не совпадает");
	}

	@Test
	public void testUpdateFilmNotFound() {
		Film film = new Film();
		film.setId(999);
		film.setName("Updated Film");
		film.setDuration(150);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			filmService.updateFilm(film);
		});

		assertEquals("Фильм с указанным ID не найден", exception.getMessage());
	}
}
