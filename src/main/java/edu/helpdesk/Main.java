package edu.helpdesk;

import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.TicketPriority;
import edu.helpdesk.model.TicketStatus;
import edu.helpdesk.repository.CommentRepository;
import edu.helpdesk.repository.TicketRepository;
import edu.helpdesk.repository.UserRepository;
import edu.helpdesk.repository.jdbc.JdbcCommentRepository;
import edu.helpdesk.repository.jdbc.JdbcTicketRepository;
import edu.helpdesk.repository.jdbc.JdbcUserRepository;
import edu.helpdesk.service.TicketService;
import edu.helpdesk.model.Tag;
import edu.helpdesk.repository.TagRepository;
import edu.helpdesk.repository.TicketTagRepository;
import edu.helpdesk.repository.jdbc.JdbcTagRepository;
import edu.helpdesk.repository.jdbc.JdbcTicketTagRepository;
import edu.helpdesk.service.TagService;
import java.util.List;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TicketRepository tickets = new JdbcTicketRepository();
        UserRepository users = new JdbcUserRepository();
        CommentRepository comments = new JdbcCommentRepository();
        TicketService service = new TicketService(tickets, users, comments);

        TagRepository tagRepository = new JdbcTagRepository();
        TicketTagRepository ticketTagRepository = new JdbcTicketTagRepository();

        TagService tagService = new TagService(
                tagRepository,
                ticketTagRepository,
                tickets
        );

        System.out.println("=== HELP DESK / DEBUG LAB ===");
        System.out.println("Учебный проект. Некоторые сценарии работают неверно — это часть задания.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("> ");
                String command = scanner.nextLine().trim();
                try {
                    switch (command) {
                        case "1" -> service.findAll().forEach(System.out::println);
                        case "2" -> {
                            System.out.print("ID заявки: ");
                            System.out.println(service.find(Long.parseLong(scanner.nextLine())));
                        }
                        case "3" -> {
                            System.out.print("Статус (NEW/IN_PROGRESS/RESOLVED/CLOSED): ");
                            service.findByStatus(TicketStatus.valueOf(scanner.nextLine().trim().toUpperCase()))
                                    .forEach(System.out::println);
                        }
                        case "4" -> {
                            System.out.print("Фрагмент темы: ");
                            service.search(scanner.nextLine()).forEach(System.out::println);
                        }
                        case "5" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID исполнителя: ");
                            long assignee = Long.parseLong(scanner.nextLine());
                            service.assignTicket(ticketId, assignee);
                            System.out.println("Исполнитель назначен.");
                        }
                        case "6" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID автора: ");
                            long authorId = Long.parseLong(scanner.nextLine());
                            System.out.print("Комментарий: ");
                            System.out.println(service.addComment(ticketId, authorId, scanner.nextLine()));
                        }
                        case "7" -> {
                            System.out.print("ID заявки: ");
                            service.comments(Long.parseLong(scanner.nextLine())).forEach(System.out::println);
                        }
                        case "8" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("Новый статус: ");
                            TicketStatus status = TicketStatus.valueOf(scanner.nextLine().trim().toUpperCase());
                            System.out.print("ID пользователя, меняющего статус: ");
                            long actor = Long.parseLong(scanner.nextLine());
                            service.changeStatus(ticketId, status, actor);
                            System.out.println("Статус изменён.");
                        }
                        case "9" -> {
                            System.out.print("Тема: ");
                            String title = scanner.nextLine();
                            System.out.print("Описание: ");
                            String description = scanner.nextLine();
                            System.out.print("Приоритет (LOW/MEDIUM/HIGH/CRITICAL): ");
                            TicketPriority priority = TicketPriority.valueOf(scanner.nextLine().trim().toUpperCase());
                            System.out.print("ID автора: ");
                            long authorId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID категории (Enter = без категории): ");
                            String categoryRaw = scanner.nextLine().trim();
                            Long categoryId = categoryRaw.isBlank() ? null : Long.parseLong(categoryRaw);
                            System.out.println(service.createTicket(title, description, priority, authorId, categoryId));
                        }
                        case "10" -> {
                            System.out.print("Введите название тега: ");
                            String name = scanner.nextLine();
                            try {
                                Tag tag = tagService.createTag(name);
                                System.out.println("Тег создан: " + tag);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: " + e.getMessage());
                            }
                        }
                        case "11" -> {
                            System.out.println("Список тегов:");
                            tagService.findAllTags().forEach(System.out::println);

                            System.out.print("Введите ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());

                            System.out.print("Введите ID тега: ");
                            long tagId = Long.parseLong(scanner.nextLine());
                            try {
                                tagService.addTagToTicket(ticketId, tagId);
                                System.out.println("Тег добавлен к заявке.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: " + e.getMessage());
                            }
                        }
                        case "12" -> {
                            System.out.println("Список тегов:");
                            tagService.findAllTags().forEach(System.out::println);
                            System.out.print("Введите ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("Введите ID тега: ");
                            long tagId = Long.parseLong(scanner.nextLine());
                            try {
                                tagService.removeTagFromTicket(ticketId, tagId);
                                System.out.println("Связь тега с заявкой удалена.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: " + e.getMessage());
                            }
                        }
                        case "13" -> {
                            System.out.print("Введите ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            try {
                                System.out.println("Теги заявки:");
                                List<Tag> tags = tagService.findTagsByTicket(ticketId);
                                if (tags.isEmpty()) {
                                    System.out.println("У заявки пока нет тегов.");
                                } else {
                                    tags.forEach(System.out::println);
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: " + e.getMessage());
                            }
                        }
                        case "14" -> {
                            System.out.println("Список тегов:");
                            tagService.findAllTags().forEach(System.out::println);
                            System.out.print("Введите ID тега: ");
                            long tagId = Long.parseLong(scanner.nextLine());
                            try {
                                System.out.println("Заявки с этим тегом:");
                                List<edu.helpdesk.model.Ticket> foundTickets =
                                        tagService.findTicketsByTag(tagId);
                                if (foundTickets.isEmpty()) {
                                    System.out.println("Заявок с этим тегом нет.");
                                } else {
                                    foundTickets.forEach(System.out::println);
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: " + e.getMessage());
                            }
                        }
                        case "15" -> {
                            System.out.print("Номер страницы (начиная с 1): ");
                            int page = Integer.parseInt(scanner.nextLine().trim());

                            int pageSize = 5;
                            List<edu.helpdesk.model.Ticket> pageTickets =
                                    tickets.findPage(page, pageSize);

                            if (pageTickets.isEmpty()) {
                                System.out.println(
                                        "На этой странице заявок нет.");
                            } else {
                                System.out.println("Страница " + page + ":");
                                pageTickets.forEach(System.out::println);
                            }
                        }
                        case "0" -> running = false;
                        default -> System.out.println("Неизвестная команда.");
                    }
                } catch (DataAccessException e) {
                    System.out.println("Ошибка доступа к данным: " + e.getMessage());
                    if (e.getCause() != null) System.out.println("Причина: " + e.getCause().getMessage());
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
                1 — список заявок
                2 — заявка по ID
                3 — заявки по статусу
                4 — поиск по теме
                5 — назначить исполнителя
                6 — добавить комментарий
                7 — комментарии заявки
                8 — изменить статус
                9  — создать заявку
                10 — создать тег
                11 — добавить тег к заявке
                12 — удалить тег из заявки
                13 — показать теги заявки
                14 — найти заявки по тегу
                15 — просмотр заявок по страницам
                0  — выход
                """);
    }
}
