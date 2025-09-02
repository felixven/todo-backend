package net.javaguides.todo.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.ParticipantEntryDto;
import net.javaguides.todo.dto.ParticipationDetailResponse;
import net.javaguides.todo.dto.TodoDto;
import net.javaguides.todo.entity.Todo;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.exception.ResourceNotFoundException;
import net.javaguides.todo.exception.TodoAPIException;
import net.javaguides.todo.repository.TodoItemRepository;
import net.javaguides.todo.repository.TodoRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.service.TodoService;
import net.javaguides.todo.utils.SecurityUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor // take care of creating the parameterized constructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TodoItemRepository todoItemRepository;

    @Override
    public TodoDto addTodo(TodoDto todoDto) {

        Todo todo = modelMapper.map(todoDto, Todo.class); // single line convert TodoDto into Todo JPA entity

        todo.setCreatedDate(LocalDate.now());
        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        // Todo JPA entity
        Todo savedTodo = todoRepository.save(todo);
        return toDto(savedTodo);
    }

    @Override
    public TodoDto getTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id" + id));
        return toDto(todo);
    }

    @Override
    public List<TodoDto> getAllTodos() {
        List<Todo> todos = todoRepository.findAll();
        return todos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public TodoDto updateTodo(TodoDto todoDto, Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        todo.setTitle(todoDto.getTitle());
        todo.setDescription(todoDto.getDescription());
        todo.setDueDate(todoDto.getDueDate());
        todo.setCompleted(todoDto.isCompleted());

        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be edited.");
        }

        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        if (todo.isOverdue() && todo.getDueDate() != null && todo.getDueDate().isAfter(LocalDate.now())) {
            todo.setOverdue(false);
        }

        Todo updated = todoRepository.save(todo);
        return toDto(updated);
    }

    @Override
    public void deleteTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id:" + id));
        todoRepository.delete(todo);
    }

    @Override
    public TodoDto completeTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));
        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "This task has already been reviewed and cannot be marked as completed.");
        }
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (todo.isCompleted()) {
            if (todo.getCompletedByUser() != null && !todo.getCompletedByUser().getId().equals(user.getId())) {
                throw new TodoAPIException(HttpStatus.CONFLICT,
                        "This task is already completed by " + todo.getCompletedByUser().getFirstName());
            }
            return toDto(todo);
        }

        long itemsTotal = todoItemRepository.countByTodo_Id(id);
        if (itemsTotal > 0) {
            boolean hasIncomplete = todoItemRepository.existsByTodo_IdAndCompletedFalse(id);
            if (hasIncomplete) {
                throw new TodoAPIException(HttpStatus.CONFLICT,
                        "All items must be completed before this task can be marked as completed.");
            }
            boolean isParticipant = todoItemRepository.isParticipant(id, user.getUsername());
            if (!isParticipant) {
                throw new TodoAPIException(HttpStatus.FORBIDDEN,
                        "Only participants who completed at least one item can complete this task.");
            }
        }

        todo.setCompleted(Boolean.TRUE);
        todo.setReviewed(Boolean.FALSE);

        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        todo.setCompletedByUser(user);
        todo.setCompletedAt(LocalDateTime.now());

        Todo updated = todoRepository.save(todo);
        return toDto(updated);
    }

    @Override
    public TodoDto inCompleteTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "This task has already been reviewed and cannot be marked as incomplete.");
        }

        if (!todo.isCompleted()) {
            return toDto(todo);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;
        boolean isAdmin = (auth != null) && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        User currentUser = userRepository.findByUsernameOrEmail(currentUsername, currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isOwner = (todo.getCompletedByUser() != null
                && todo.getCompletedByUser().getId().equals(currentUser.getId()));
        if (!isAdmin && !isOwner) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "Only the original finisher or an admin can mark this task as incomplete.");
        }

        todo.setCompleted(false);
        todo.setCompletedByUser(null);
        todo.setCompletedAt(null);
        todo.setReviewed(false);
        todo.setReviewedBy(null);
        todo.setReviewedAt(null);

        if (todo.getDueDate() != null) {
            todo.setOverdue(todo.getDueDate().isBefore(LocalDate.now()));
        } else {
            todo.setOverdue(false);
        }

        Todo updated = todoRepository.save(todo);
        return toDto(updated);
    }

    @Override
    public TodoDto reviewTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        if (!todo.isCompleted()) {
            throw new IllegalStateException("Cannot review a task that is not completed.");
        }
        todo.setReviewed(Boolean.TRUE);

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        todo.setReviewedBy(user.getFirstName());
        todo.setReviewedAt(LocalDateTime.now());

        Todo updatedTodo = todoRepository.save(todo);
        return toDto(updatedTodo);
    }

    @Override
    public List<TodoDto> getPendingReviewTodos() {
        List<Todo> todos = todoRepository.findByCompletedTrueAndReviewedFalse();
        return todos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TodoDto> getReviewedTodos() {
        List<Todo> todos = todoRepository.findByCompletedTrueAndReviewedTrue();
        return todos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getTodoStatistics() {
        List<Todo> todos = todoRepository.findAll();
        LocalDate today = LocalDate.now();

        long total = todos.size();
        long completed = todos.stream().filter(Todo::isCompleted).count();
        long pendingReview = todos.stream().filter(t -> t.isCompleted() && !t.isReviewed()).count();
        long reviewed = todos.stream().filter(Todo::isReviewed).count();
        long overdue = todos.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isBefore(today))
                .filter(t -> !t.isCompleted())
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pendingReview", pendingReview);
        stats.put("reviewed", reviewed);
        stats.put("overdue", overdue);

        return stats;
    }

    @Override
    public List<TodoDto> getOverdueTodos() {
        LocalDate today = LocalDate.now();
        List<Todo> overdueTodos = todoRepository.findByDueDateBefore(today);

        List<Todo> filtered = overdueTodos.stream()
                .filter(todo -> !todo.isCompleted())
                .collect(Collectors.toList());

        return filtered.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getParticipationStats(Long todoId) {
        List<Object[]> rows = todoItemRepository.countCompletedItemsByUser(todoId);
        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : rows) {
            String username = (String) row[0];
            Long count = (Long) row[1];
            stats.put(username, count);
        }
        return stats;
    }

    @Override
    public ParticipationDetailResponse getParticipationDetail(Long todoId) {
        List<Object[]> rows = todoItemRepository.countCompletedItemsByUser(todoId);

        long total = 0L;
        for (Object[] r : rows) {
            total += ((Number) r[1]).longValue();
        }

        boolean eligible = rows.size() >= 2;

        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (a != null) ? a.getName() : null;
        boolean currentIsParticipant = (currentUsername != null)
                && todoItemRepository.isParticipant(todoId, currentUsername);

        List<ParticipantEntryDto> participants = new ArrayList<>();
        for (Object[] r : rows) {
            String username = (String) r[0];
            long count = ((Number) r[1]).longValue();
            double pct = (total == 0) ? 0.0 : Math.round((count * 10000.0) / total) / 100.0;
            participants.add(new ParticipantEntryDto(username, count, pct));
        }
        participants.sort(Comparator.comparingLong(ParticipantEntryDto::getCount).reversed());

        ParticipationDetailResponse res = new ParticipationDetailResponse();
        res.setTodoId(todoId);
        res.setEligibleForCollabBoard(eligible);
        res.setTotalCompletedItems(total);
        res.setCurrentUserIsParticipant(currentIsParticipant);
        res.setParticipants(participants);
        return res;
    }

    private TodoDto toDto(Todo t) {
        TodoDto dto = new TodoDto();
        dto.setId(t.getId());
        dto.setDueDate(t.getDueDate());
        dto.setCreatedDate(t.getCreatedDate());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        dto.setCompleted(t.isCompleted());
        dto.setReviewed(t.isReviewed());
        dto.setCompletedAt(t.getCompletedAt());
        dto.setReviewedBy(t.getReviewedBy());
        dto.setReviewedAt(t.getReviewedAt());
        dto.setOverdue(t.isOverdue());

        if (t.getCompletedByUser() != null) {
            dto.setCompletedById(t.getCompletedByUser().getId());
            dto.setCompletedByName(t.getCompletedByUser().getFirstName());
        } else {
            dto.setCompletedById(null);
            dto.setCompletedByName(null);
        }
        return dto;
    }
}
