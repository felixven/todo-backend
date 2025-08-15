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

    /** 所屬任務 ID */
    private Long todoId;

    /** 細項標題 */
    private String title;

    /** 是否完成 */
    private boolean completed;

    /** 完成者 ID（可為 null） */
    private Long completedById;

    /** 完成者名稱（顯示用，可為 null） */
    private String completedByName;

    /** 完成時間 */
    private LocalDateTime completedAt;

    /** 建立時間 */
    private LocalDateTime createdAt;
}
