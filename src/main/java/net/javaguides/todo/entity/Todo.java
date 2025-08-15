package net.javaguides.todo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Column(updatable = false)
    private LocalDate createdDate;

    @Column(nullable = false)//括弧裡面其實可以不用強調name="description"，會自己匹配 (name="title",nullable = false)
    private String title;

    @Column(nullable = false)//括弧裡面其實可以不用強調name="description"，會自己匹配，同上的概念
    private String description;


    private boolean completed;

    @Column(nullable = false)
    private boolean reviewed = false;

    @Column
    private String completedBy;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String reviewedBy;

    @Column
    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private boolean isOverdue = false;

}
