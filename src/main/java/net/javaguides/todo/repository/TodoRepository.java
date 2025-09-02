package net.javaguides.todo.repository;

import net.javaguides.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo,Long> {//todo here is entity under the entity package
    List<Todo> findByCompletedTrueAndReviewedFalse();

    List<Todo> findByCompletedTrueAndReviewedTrue();

    List<Todo> findByDueDateBefore(LocalDate date);

    @Query("SELECT t.completedByUser.id, t.completedByUser.firstName, COUNT(t) " +
            "FROM Todo t " +
            "WHERE t.completed = true AND t.completedByUser IS NOT NULL " +
            "GROUP BY t.completedByUser.id, t.completedByUser.firstName " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> finisherLeaderboardByUserId();

    @Query("SELECT t.id, t.title, t.completedAt " +
            "FROM Todo t " +
            "WHERE t.completed = true AND t.completedByUser.id = ?1 " +
            "ORDER BY t.completedAt DESC")
    List<Object[]> finisherDetailsByUserId(Long userId);
}
