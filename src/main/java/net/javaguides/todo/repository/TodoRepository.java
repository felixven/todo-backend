package net.javaguides.todo.repository;

import net.javaguides.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo,Long> {//todo here is entity under the entity package
    // 找出已完成但尚未審核的任務
    List<Todo> findByCompletedTrueAndReviewedFalse();

    // 找出已完成且已審核的任務
    List<Todo> findByCompletedTrueAndReviewedTrue();

    List<Todo> findByDueDateBefore(LocalDate date);

    // ✅ 完成者榜（用 FK）
    @Query("SELECT t.completedByUser.id, t.completedByUser.firstName, COUNT(t) " +
            "FROM Todo t " +
            "WHERE t.completed = true AND t.completedByUser IS NOT NULL " +
            "GROUP BY t.completedByUser.id, t.completedByUser.firstName " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> finisherLeaderboardByUserId();

    // ✅ 完成者榜明細（用 FK）
    @Query("SELECT t.id, t.title, t.completedAt " +
            "FROM Todo t " +
            "WHERE t.completed = true AND t.completedByUser.id = ?1 " +
            "ORDER BY t.completedAt DESC")
    List<Object[]> finisherDetailsByUserId(Long userId);
}
