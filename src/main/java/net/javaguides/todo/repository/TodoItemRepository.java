package net.javaguides.todo.repository;

import net.javaguides.todo.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByTodo_IdOrderByIdAsc(Long todoId);
    long countByTodo_Id(Long todoId);
    long countByTodo_IdAndCompletedTrue(Long todoId);
}
