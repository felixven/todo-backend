package net.javaguides.todo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 和 Todo 一樣用 id 命名，保持一致

    @NotBlank
    @Size(min = 1, max = 2000, message = "內容需 1~2000 字")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 發在哪個任務底下
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")   // 外鍵欄位名，與 Address 一樣的寫法
    private Todo todo;

    // 誰發的留言
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();  // 和你現有欄位風格一致用 LocalDateTime
        }
    }
}