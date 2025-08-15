package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private Long todoId;
    private Long userId;
    private String username;      // 方便前端判斷是否本人
    private String authorFullName;// firstName + lastName（找不到就用 username）
    private String content;
    private LocalDateTime createdAt;
}
