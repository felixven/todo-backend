package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 協作榜明細：某使用者在符合條件任務中完成過的細項清單 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollabItemDetailDto {
    private Long todoId;
    private String todoTitle;
    private Long itemId;
    private String itemTitle;
    private LocalDateTime completedAt;
}
