package edu.helpdesk.service;
import edu.helpdesk.model.Tag;
import edu.helpdesk.repository.TagRepository;
import edu.helpdesk.repository.TicketTagRepository;
import edu.helpdesk.repository.TicketRepository;
import java.util.List;
import edu.helpdesk.model.Ticket;

public class TagService {
    private final TagRepository tags;
    private final TicketTagRepository ticketTags;
    private final TicketRepository tickets;

    public TagService(
            TagRepository tags,
            TicketTagRepository ticketTags,
            TicketRepository tickets
    ) {
        this.tags = tags;
        this.ticketTags = ticketTags;
        this.tickets = tickets;
    }
    public Tag createTag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название тега обязательно");
        }

        String normalizedName = name.trim();

        return tags.findByName(normalizedName)
                .orElseGet(() -> tags.save(new Tag(normalizedName)));
    }
    public void addTagToTicket(long ticketId, long tagId) {
        tickets.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Заявка не найдена"));

        tags.findById(tagId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тег не найден"));

        ticketTags.addTagToTicket(ticketId, tagId);
    }
    public void removeTagFromTicket(long ticketId, long tagId) {
        tickets.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Заявка не найдена"));

        tags.findById(tagId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тег не найден"));

        ticketTags.removeTagFromTicket(ticketId, tagId);
    }
    public List<Tag> findTagsByTicket(long ticketId) {
        tickets.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Заявка не найдена"));

        return ticketTags.findTagsByTicketId(ticketId);
    }
    public List<Long> findTicketIdsByTag(long tagId) {
        tags.findById(tagId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тег не найден"));

        return ticketTags.findTicketIdsByTagId(tagId);
    }
    public List<Tag> findAllTags() {
        return tags.findAll();
    }

    public List<Ticket> findTicketsByTag(long tagId) {
        tags.findById(tagId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тег не найден"));

        return ticketTags.findTicketsByTagId(tagId);
    }

}
