package net.javaguides.todo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo_items")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    /** 完成者（可為 null；完成時記錄誰完成） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")   // 對應 users.id
    private User completedBy;

    /** 完成時間（可為 null） */
    private LocalDateTime completedAt;

    /** 建立時間（自動帶） */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
