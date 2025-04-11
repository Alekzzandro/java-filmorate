package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmServiceTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Film film;

	@BeforeEach
	void setUp() {
		film = Film.builder()
				.name("Interstellar")
				.description("Great movie")
				.releaseDate(LocalDate.of(2014, 11, 7))
				.duration(169)
				.genres(Collections.singleton(FilmGenre.DRAMA))
				.mpaRating(MpaRating.PG_13)
				.build();
	}

	@Test
	void shouldCreateValidFilm() throws Exception {
		mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(Matchers.greaterThan(0)))
				.andExpect(jsonPath("$.name").value(film.getName()))
				.andExpect(jsonPath("$.description").value(film.getDescription()))
				.andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
				.andExpect(jsonPath("$.genres", hasItem("DRAMA")))
				.andExpect(jsonPath("$.mpaRating").value(film.getMpaRating().toString()));
	}

	@Test
	void shouldThrowExceptionWhenNameIsEmpty() throws Exception {
		Film invalidFilm = film.toBuilder().name("").build();

		mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(invalidFilm)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.name").value("Название не должно быть пустым"));
	}

	@Test
	void shouldThrowExceptionWhenDescriptionTooLong() throws Exception {
		Film invalidFilm = film.toBuilder().description("A".repeat(201)).build();

		mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(invalidFilm)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.description").value("Максимальная длина описания — 200 символов"));
	}

	@Test
	void shouldThrowExceptionWhenReleaseDateIsTooEarly() throws Exception {
		Film invalidFilm = film.toBuilder().releaseDate(LocalDate.of(1800, 1, 1)).build();

		mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(invalidFilm)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.releaseDate").value("Дата релиза не должна быть раньше 28 декабря 1895 года"));
	}

	@Test
	void shouldThrowExceptionWhenDurationIsZero() throws Exception {
		Film invalidFilm = film.toBuilder().duration(0).build();

		mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(invalidFilm)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.duration").value("Продолжительность фильма должна быть положительным числом"));
	}

	@Test
	void shouldUpdateValidFilm() throws Exception {
		String createdFilmContent = mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Film createdFilm = objectMapper.readValue(createdFilmContent, Film.class);

		Film updatedFilm = createdFilm.toBuilder()
				.name("Updated Name")
				.build();

		mockMvc.perform(put("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(updatedFilm)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(createdFilm.getId()))
				.andExpect(jsonPath("$.name").value("Updated Name"))
				.andExpect(jsonPath("$.description").value(createdFilm.getDescription()))
				.andExpect(jsonPath("$.releaseDate").value(createdFilm.getReleaseDate().toString()))
				.andExpect(jsonPath("$.duration").value(createdFilm.getDuration()));
	}

	@Test
	void shouldReturnCorrectFilm() throws Exception {
		String createdFilmContent = mockMvc.perform(post("/films")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Film createdFilm = objectMapper.readValue(createdFilmContent, Film.class);

		mockMvc.perform(get("/films/" + createdFilm.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(createdFilm.getId()))
				.andExpect(jsonPath("$.name").value(createdFilm.getName()))
				.andExpect(jsonPath("$.description").value(createdFilm.getDescription()))
				.andExpect(jsonPath("$.releaseDate").value(createdFilm.getReleaseDate().toString()))
				.andExpect(jsonPath("$.duration").value(createdFilm.getDuration()));
	}
}