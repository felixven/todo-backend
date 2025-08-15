package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {
    private Long id;
    private LocalDate dueDate;
    private LocalDate createdDate;
    private String title;
    private String description;
    private boolean completed;
    private boolean reviewed;
    private String completedBy;
    private LocalDateTime completedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private boolean isOverdue;

}
