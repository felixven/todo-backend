package net.javaguides.todo.service;

import net.javaguides.todo.dto.TodoItemDto;

import java.util.List;
import java.util.Map;

public interface TodoItemService {

    // 讀取
    List<TodoItemDto> listByTodo(Long todoId);
    Map<String, Object> summary(Long todoId); // { total, completed, progress }

    // 管理細項（建議 Controller 端用 @PreAuthorize 限 Admin）
    TodoItemDto addItem(Long todoId, String title);
    void deleteItem(Long todoId, Long itemId);

    // 勾選 / 取消
    TodoItemDto completeItem(Long itemId);
    TodoItemDto uncompleteItem(Long itemId);
}
