package edu.helpdesk.repository;
import edu.helpdesk.model.Tag;
import java.util.List;
import edu.helpdesk.model.Ticket;
public interface TicketTagRepository {

    void addTagToTicket(long ticketId, long tagId);
    void removeTagFromTicket(long ticketId, long tagId);

    List<Tag> findTagsByTicketId(long ticketId);
    List<Long> findTicketIdsByTagId(long tagId);
    List<Ticket> findTicketsByTagId(long tagId);
}
