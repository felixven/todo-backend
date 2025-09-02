package net.javaguides.todo.service;

import net.javaguides.todo.dto.TodoItemDto;

import java.util.List;
import java.util.Map;

public interface TodoItemService {

    List<TodoItemDto> listByTodo(Long todoId);
    Map<String, Object> summary(Long todoId); // { total, completed, progress }

    TodoItemDto addItem(Long todoId, String title);
    void deleteItem(Long todoId, Long itemId);

    TodoItemDto completeItem(Long itemId);
    TodoItemDto uncompleteItem(Long itemId);
}
