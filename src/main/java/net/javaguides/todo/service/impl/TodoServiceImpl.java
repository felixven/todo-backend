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
        // convert TodoDto into Todo JPA entity
        // Todo todo=new Todo();
        // todo.setTitle(todoDto.getTitle());
        // todo.setDescription(todoDto.getDescription());
        // todo.setCompleted(todoDto.isCompleted());

        Todo todo = modelMapper.map(todoDto, Todo.class); // single line convert TodoDto into Todo JPA entity

        todo.setCreatedDate(LocalDate.now());
        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        // Todo JPA entity
        Todo savedTodo = todoRepository.save(todo);

        // Convert saved Todo entity object into TodoDto object
        // TodoDto savedTodoDto=new TodoDto();
        // savedTodoDto.setId(savedTodo.getId());
        // savedTodoDto.setTitle(savedTodoDto.getTitle());
        // savedTodoDto.setDescription(savedTodo.getDescription());
        // savedTodoDto.setCompleted(savedTodo.isCompleted());

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
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id)); // 上面那行是retrieve data from databases
        todo.setTitle(todoDto.getTitle());
        todo.setDescription(todoDto.getDescription());
        todo.setDueDate(todoDto.getDueDate());
        todo.setCompleted(todoDto.isCompleted());

        // 已經審核的todo不能再edit
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
        //todoRepository.deleteById(id);
        todoRepository.delete(todo); // 這裡才會觸發 orphanRemoval
    }

    @Override
    public TodoDto completeTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        // 已審核的任務不可再標記完成（沿用你原本規則）
        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "This task has already been reviewed and cannot be marked as completed.");
        }

        // 取得當前使用者（等會要用 username 做參與者判斷）
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // --- 防偷功勞：已由他人完成則不可覆蓋（409）；若已完成且是自己，視為冪等直接回傳 ---
        if (todo.isCompleted()) {
            if (todo.getCompletedByUser() != null && !todo.getCompletedByUser().getId().equals(user.getId())) {
                throw new TodoAPIException(HttpStatus.CONFLICT,
                        "This task is already completed by " + todo.getCompletedByUser().getFirstName());
            }
            // 同一人重複 → 冪等
            return toDto(todo);
        }

        // 有細項才檢查是否仍有未完成
        // 有細項才檢查（查一次就夠）
        long itemsTotal = todoItemRepository.countByTodo_Id(id);
        if (itemsTotal > 0) {
            // ① 仍有未完成 → 擋 409
            boolean hasIncomplete = todoItemRepository.existsByTodo_IdAndCompletedFalse(id);
            if (hasIncomplete) {
                throw new TodoAPIException(HttpStatus.CONFLICT,
                        "All items must be completed before this task can be marked as completed.");
            }

            // ② 全部完成後，再檢查是否為參與者 → 非參與者擋 403
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

        // 寫入 completedByUser（FK）
        todo.setCompletedByUser(user);
        todo.setCompletedAt(LocalDateTime.now());

        Todo updated = todoRepository.save(todo);
        return toDto(updated);
    }

    @Override
    public TodoDto inCompleteTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        // 已審核不可改為未完成（沿用你的規則）
        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "This task has already been reviewed and cannot be marked as incomplete.");
        }

        // 若目前本來就未完成，直接回傳（冪等處理）
        if (!todo.isCompleted()) {
            return toDto(todo);
        }

        // 取得目前使用者與是否為 Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;
        boolean isAdmin = (auth != null) && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 取出目前使用者
        User currentUser = userRepository.findByUsernameOrEmail(currentUsername, currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 只有「原完成者」或「Admin」可以 Reopen（FK-only 判斷）
        boolean isOwner = (todo.getCompletedByUser() != null
                && todo.getCompletedByUser().getId().equals(currentUser.getId()));
        if (!isAdmin && !isOwner) {
            // 中文註解：避免他人將他人完成的任務改為未完成
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "Only the original finisher or an admin can mark this task as incomplete.");
        }

        // 保持你原本的清空邏輯
        todo.setCompleted(false);
        todo.setCompletedByUser(null);
        todo.setCompletedAt(null);

        // reopen 後，審核狀態也清空
        todo.setReviewed(false);
        todo.setReviewedBy(null);
        todo.setReviewedAt(null);

        // 逾期重算
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
            // 中文註解：未完成的任務不能被審核
            throw new IllegalStateException("Cannot review a task that is not completed.");
        }
        todo.setReviewed(Boolean.TRUE);

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        todo.setReviewedBy(user.getFirstName());
        todo.setReviewedAt(LocalDateTime.now()); // 中文註解：設定審核時間

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
                .filter(t -> t.getDueDate().isBefore(today))   // 已過截止日
                .filter(t -> !t.isCompleted())                 // 未完成
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
        List<Todo> overdueTodos = todoRepository.findByDueDateBefore(today); // ❌ 還沒排除 completed=true

        // 加上條件過濾：
        List<Todo> filtered = overdueTodos.stream()
                .filter(todo -> !todo.isCompleted()) // ✅ 僅顯示未完成的
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
            double pct = (total == 0) ? 0.0 : Math.round((count * 10000.0) / total) / 100.0; // xx.xx
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

    // 專用：將 Todo entity 映射成 TodoDto（補上 completedById / completedByName）
    private TodoDto toDto(Todo t) {
        TodoDto dto = new TodoDto();
        // 基本欄位手動帶入（不透過 ModelMapper）
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

        // 完成者（FK）
        if (t.getCompletedByUser() != null) {
            dto.setCompletedById(t.getCompletedByUser().getId());
            dto.setCompletedByName(t.getCompletedByUser().getFirstName()); // 只顯示 firstName
        } else {
            dto.setCompletedById(null);
            dto.setCompletedByName(null);
        }
        return dto;
    }
}
