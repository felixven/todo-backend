package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 完成者榜明細：某完成者作為「第一完成者」的任務清單 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinisherTodoDetailDto {
    private Long todoId;
    private String todoTitle;
    private LocalDateTime completedAt;
}
