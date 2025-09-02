package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemDto {

    private Long id;
    private Long todoId;
    private String title;
    private boolean completed;
    private Long completedById;
    private String completedByName;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
