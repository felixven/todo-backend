package net.javaguides.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_user_id")
    private User completedByUser;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String reviewedBy;

    @Column
    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private boolean isOverdue = false;

    @OneToMany(mappedBy="todo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    private List<TodoItem> items = new ArrayList<>();

    @OneToMany(mappedBy="todo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();


}
