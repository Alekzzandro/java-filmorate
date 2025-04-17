package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
public class MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa_rating";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"))
        );
    }

    public Mpa getMpaById(Long mpaId) {
        if (mpaId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa_rating WHERE id = ?", mpaId);
        if (mpaRows.first()) {
            return new Mpa(
                    mpaRows.getLong("id"),
                    mpaRows.getString("name"),
                    mpaRows.getString("description")
            );
        } else {
            return null;
        }
    }
}