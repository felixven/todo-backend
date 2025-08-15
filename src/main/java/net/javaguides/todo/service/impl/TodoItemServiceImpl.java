package net.javaguides.todo.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.TodoItemDto;
import net.javaguides.todo.entity.Todo;
import net.javaguides.todo.entity.TodoItem;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.exception.ResourceNotFoundException;
import net.javaguides.todo.exception.TodoAPIException;
import net.javaguides.todo.repository.TodoItemRepository;
import net.javaguides.todo.repository.TodoRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.service.TodoItemService;
import net.javaguides.todo.utils.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TodoItemServiceImpl implements TodoItemService {

    private final TodoItemRepository todoItemRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    // ===== 讀取 =====
    @Override
    public List<TodoItemDto> listByTodo(Long todoId) {
        // 確認任務存在（符合你既有風格）
        todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        return todoItemRepository.findByTodo_IdOrderByIdAsc(todoId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> summary(Long todoId) {
        // 有細項 → progress = completed / total
        // 無細項 → 交由前端拿主任務 completed 顯示 0%/100%（這裡只回 items 的統計）
        long total = todoItemRepository.countByTodo_Id(todoId);
        long completed = todoItemRepository.countByTodo_IdAndCompletedTrue(todoId);
        double progress = (total == 0) ? 0.0 : (completed * 1.0 / total);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("completed", completed);
        m.put("progress", progress);
        return m;
    }

    // ===== 管理細項 =====
    @Override
    public TodoItemDto addItem(Long todoId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Item title cannot be empty.");
        }
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be modified.");
        }

        TodoItem it = new TodoItem();
        it.setTodo(todo);
        it.setTitle(title.trim());
        TodoItem saved = todoItemRepository.save(it);
        return toDto(saved);
    }

    @Override
    public void deleteItem(Long todoId, Long itemId) {
        // （可選）確認 todo 存在
        todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        TodoItem it = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        // 若任務已審核，禁止修改
        if (it.getTodo().isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be modified.");
        }
        todoItemRepository.delete(it);
    }

    // ===== 勾選／取消 =====
    @Override
    public TodoItemDto completeItem(Long itemId) {
        TodoItem it = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (it.getTodo().isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be modified.");
        }
        if (it.isCompleted()) {
            return toDto(it); // 已完成就直接回（冪等）
        }

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        it.setCompleted(true);
        it.setCompletedBy(user);
        it.setCompletedAt(LocalDateTime.now());
        TodoItem saved = todoItemRepository.save(it);
        return toDto(saved);
    }

    @Override
    public TodoItemDto uncompleteItem(Long itemId) {
        TodoItem it = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (it.getTodo().isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be modified.");
        }
        if (!it.isCompleted()) {
            return toDto(it); // 已是未完成就直接回（冪等）
        }

        String username = SecurityUtil.getCurrentUsername();
        User me = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isAdmin = me.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

        // 僅本人或 Admin 可取消
        if (!isAdmin && (it.getCompletedBy() == null ||
                !Objects.equals(it.getCompletedBy().getId(), me.getId()))) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "Only the completer or admin can undo this item.");
        }

        it.setCompleted(false);
        it.setCompletedBy(null);
        it.setCompletedAt(null);
        TodoItem saved = todoItemRepository.save(it);
        return toDto(saved);
    }

    // ===== mapper =====
    private TodoItemDto toDto(TodoItem it) {
        Long byId = it.getCompletedBy() != null ? it.getCompletedBy().getId() : null;
        String byName = null;
        if (it.getCompletedBy() != null) {
            User u = it.getCompletedBy();
            String full = ((u.getLastName() != null ? u.getLastName() : "") +
                    (u.getFirstName() != null ? u.getFirstName() : "")).trim();
            byName = full.isEmpty() ? u.getUsername() : full;
        }

        TodoItemDto dto = new TodoItemDto();
        dto.setId(it.getId());
        dto.setTodoId(it.getTodo().getId());
        dto.setTitle(it.getTitle());
        dto.setCompleted(it.isCompleted());
        dto.setCompletedById(byId);
        dto.setCompletedByName(byName);
        dto.setCompletedAt(it.getCompletedAt());
        dto.setCreatedAt(it.getCreatedAt());
        return dto;
    }
}


