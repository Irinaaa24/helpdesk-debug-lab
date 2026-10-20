package edu.helpdesk.repository.jdbc;
import edu.helpdesk.config.Database;
import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.Tag;
import edu.helpdesk.repository.TicketTagRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.helpdesk.model.Ticket;
import edu.helpdesk.model.TicketStatus;
import edu.helpdesk.model.TicketPriority;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class JdbcTicketTagRepository implements TicketTagRepository {

    @Override
    public void addTagToTicket(long ticketId, long tagId) {
        String sql = """
                INSERT INTO ticket_tags (ticket_id, tag_id)
                VALUES (?, ?)
                ON CONFLICT (ticket_id, tag_id) DO NOTHING
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);
            statement.setLong(2, tagId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось добавить тег к заявке", e);
        }
    }

    @Override
    public void removeTagFromTicket(long ticketId, long tagId) {
        String sql = """
                DELETE FROM ticket_tags
                WHERE ticket_id = ? AND tag_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);
            statement.setLong(2, tagId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось удалить тег из заявки", e);
        }
    }

    @Override
    public List<Tag> findTagsByTicketId(long ticketId) {
        String sql = """
                SELECT t.id, t.name
                FROM tags t
                JOIN ticket_tags tt ON tt.tag_id = t.id
                WHERE tt.ticket_id = ?
                ORDER BY t.name
                """;

        List<Tag> tags = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось получить теги заявки", e);
        }

        return tags;
    }

    @Override
    public List<Long> findTicketIdsByTagId(long tagId) {
        String sql = """
                SELECT DISTINCT ticket_id
                FROM ticket_tags
                WHERE tag_id = ?
                ORDER BY ticket_id
                """;

        List<Long> ticketIds = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, tagId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ticketIds.add(rs.getLong("ticket_id"));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось найти заявки по тегу", e);
        }

        return ticketIds;
    }

    @Override
    public List<Ticket> findTicketsByTagId(long tagId) {
        String sql = """
            SELECT DISTINCT t.id, t.title, t.description, t.status,
                   t.priority, t.author_id, t.assignee_id,
                   t.category_id, t.created_at, t.updated_at
            FROM tickets t
            JOIN ticket_tags tt ON tt.ticket_id = t.id
            WHERE tt.tag_id = ?
            ORDER BY t.id
            """;

        List<Ticket> result = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, tagId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");

                    long assigneeValue = rs.getLong("assignee_id");
                    Long assigneeId = rs.wasNull() ? null : assigneeValue;

                    long categoryValue = rs.getLong("category_id");
                    Long categoryId = rs.wasNull() ? null : categoryValue;

                    result.add(new Ticket(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            TicketStatus.valueOf(rs.getString("status")),
                            TicketPriority.valueOf(rs.getString("priority")),
                            rs.getLong("author_id"),
                            assigneeId,
                            categoryId,
                            created == null ? null : created.toLocalDateTime(),
                            updated == null ? null : updated.toLocalDateTime()
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Не удалось найти заявки по тегу", e);
        }

        return result;
    }

}
