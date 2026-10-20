package edu.helpdesk.repository.jdbc;

import edu.helpdesk.config.Database;
import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.Tag;
import edu.helpdesk.repository.TagRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTagRepository implements TagRepository {

    @Override
    public Tag save(Tag tag) {
        String sql = """
                INSERT INTO tags (name)
                VALUES (?)
                RETURNING id, name
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, tag.getName());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Tag(
                            rs.getLong("id"),
                            rs.getString("name")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось сохранить тег", e);
        }

        throw new DataAccessException(
                "Не удалось получить сохранённый тег",
                new SQLException("INSERT не вернул сохранённую запись")
        );
    }

    @Override
    public Optional<Tag> findById(Long id) {
        String sql = "SELECT id, name FROM tags WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти тег по ID", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT id, name FROM tags WHERE name = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tag(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось найти тег по названию", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Tag> findAll() {
        String sql = "SELECT id, name FROM tags ORDER BY id";
        List<Tag> tags = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                tags.add(new Tag(
                        rs.getLong("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список тегов", e);
        }

        return tags;
    }
}
