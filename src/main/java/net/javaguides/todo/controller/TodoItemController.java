package net.javaguides.todo.controller;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.TodoItemDto;
import net.javaguides.todo.service.TodoItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/todos/{todoId}/items")
@AllArgsConstructor
public class TodoItemController {

    private final TodoItemService todoItemService;

    // 讀取細項列表
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<TodoItemDto>> list(@PathVariable Long todoId) {
        return ResponseEntity.ok(todoItemService.listByTodo(todoId));
    }

    // 新增細項（Admin）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TodoItemDto> add(@PathVariable Long todoId,
                                           @RequestBody Map<String, String> body) {
        String title = body.get("title");
        TodoItemDto saved = todoItemService.addItem(todoId, title);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 刪除細項（Admin）
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{itemId}")
    public ResponseEntity<String> delete(@PathVariable Long todoId,
                                         @PathVariable Long itemId) {
        todoItemService.deleteItem(todoId, itemId);
        return ResponseEntity.ok("Item deleted successfully!");
    }

    // 勾選完成（User/Admin）
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("{itemId}/complete")
    public ResponseEntity<TodoItemDto> complete(@PathVariable Long todoId,
                                                @PathVariable Long itemId) {
        // todoId 目前僅作路由語意用途；邏輯在 service 依 itemId 處理
        return ResponseEntity.ok(todoItemService.completeItem(itemId));
    }

    // 取消完成（User/Admin；僅本人或 Admin）
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("{itemId}/incomplete")
    public ResponseEntity<TodoItemDto> incomplete(@PathVariable Long todoId,
                                                  @PathVariable Long itemId) {
        return ResponseEntity.ok(todoItemService.uncompleteItem(itemId));
    }

    // 進度摘要（User/Admin）
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable Long todoId) {
        return ResponseEntity.ok(todoItemService.summary(todoId));
    }
}
