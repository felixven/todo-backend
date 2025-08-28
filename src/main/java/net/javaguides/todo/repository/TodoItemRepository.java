package net.javaguides.todo.repository;

import net.javaguides.todo.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByTodo_IdOrderByIdAsc(Long todoId);

    long countByTodo_Id(Long todoId);

    long countByTodo_IdAndCompletedTrue(Long todoId);

    boolean existsByTodo_IdAndCompletedFalse(Long todoId);

    boolean existsByTodo_IdAndCompletedTrueAndCompletedBy_Username(Long todoId, String username);

    // 判斷某使用者是否曾經完成過這個任務的任一細項
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM TodoItem i " +
            "WHERE i.todo.id = ?1 AND i.completed = true AND i.completedBy.username = ?2")
    boolean isParticipant(Long todoId, String username);

    @Query("SELECT i.completedBy.username, COUNT(i) " +
            "FROM TodoItem i " +
            "WHERE i.todo.id = ?1 AND i.completed = true AND i.completedBy IS NOT NULL " +
            "GROUP BY i.completedBy.username")
    List<Object[]> countCompletedItemsByUser(Long todoId);

    // 協作榜：僅計算「有細項且參與者≥2」的任務中的細項完成數，依使用者彙總
    @Query("SELECT i.completedBy.id, i.completedBy.firstName, COUNT(i) " +
            "FROM TodoItem i " +
            "WHERE i.completed = true AND i.completedBy IS NOT NULL " +
            "AND (SELECT COUNT(DISTINCT i2.completedBy.username) " +
            "     FROM TodoItem i2 " +
            "     WHERE i2.todo = i.todo AND i2.completed = true AND i2.completedBy IS NOT NULL) >= 2 " +
            "GROUP BY i.completedBy.id, i.completedBy.firstName " +
            "ORDER BY COUNT(i) DESC")
    List<Object[]> collabLeaderboard();

    // 協作榜明細：指定使用者在符合條件任務中完成過的細項清單
    @Query("SELECT i.todo.id, i.todo.title, i.id, i.title, i.completedAt " +
            "FROM TodoItem i " +
            "WHERE i.completed = true " +
            "AND i.completedBy.id = ?1 " +
            "AND (SELECT COUNT(DISTINCT i2.completedBy.username) " +
            "     FROM TodoItem i2 " +
            "     WHERE i2.todo = i.todo AND i2.completed = true AND i2.completedBy IS NOT NULL) >= 2 " +
            "ORDER BY i.completedAt DESC")
    List<Object[]> collabDetailsForUser(Long userId);

}
