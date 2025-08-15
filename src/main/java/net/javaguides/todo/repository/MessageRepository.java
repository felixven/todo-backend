package net.javaguides.todo.repository;

import net.javaguides.todo.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTodo_IdOrderByCreatedAtAsc(Long todoId);
}
