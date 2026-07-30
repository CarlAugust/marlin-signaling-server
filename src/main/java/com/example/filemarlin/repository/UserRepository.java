package com.example.filemarlin.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.filemarlin.entity.User;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (results, rowNum) -> {
        User user = new User();
        user.setId(results.getLong("id"));
        user.setUsername(results.getString("username"));
        user.setPassword(results.getString("password"));
        return user;
    };

    public int save(User user) {
        
        String sql = "INSERT INTO user (username, password) VALUES (?, ?)";
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword());
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password FROM user WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, new Object[]{username});
            return Optional.of(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id, username, password FROM user WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, new Object[]{id});
            return Optional.of(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
