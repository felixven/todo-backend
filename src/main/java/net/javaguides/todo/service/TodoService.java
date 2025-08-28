package net.javaguides.todo.service;

import net.javaguides.todo.dto.ParticipationDetailResponse;
import net.javaguides.todo.dto.TodoDto;

import java.util.List;
import java.util.Map;

public interface TodoService {

    TodoDto addTodo(TodoDto todoDto);
    TodoDto getTodo(Long id);
    List<TodoDto> getAllTodos();
    TodoDto updateTodo(TodoDto todoDto,Long id);
    void deleteTodo(Long id);
    TodoDto completeTodo(Long id);
    TodoDto inCompleteTodo(Long id);
    TodoDto reviewTodo(Long id);
    List<TodoDto> getPendingReviewTodos();
    List<TodoDto> getReviewedTodos();
    Map<String, Long> getTodoStatistics();
    List<TodoDto> getOverdueTodos();
    Map<String, Long> getParticipationStats(Long todoId);
    ParticipationDetailResponse getParticipationDetail(Long todoId);
}
