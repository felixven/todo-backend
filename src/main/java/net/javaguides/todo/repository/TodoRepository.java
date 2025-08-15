package net.javaguides.todo.repository;

import net.javaguides.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo,Long> {//todo here is entity under the entity package
    // 找出已完成但尚未審核的任務
    List<Todo> findByCompletedTrueAndReviewedFalse();

    // 找出已完成且已審核的任務
    List<Todo> findByCompletedTrueAndReviewedTrue();

    List<Todo> findByDueDateBefore(LocalDate date);
}
