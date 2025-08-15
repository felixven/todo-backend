package net.javaguides.todo.service;

import net.javaguides.todo.dto.MessageDto;

import java.util.List;

public interface MessageService {
    List<MessageDto> listMessagesByTodo(Long todoId);
    MessageDto addMessage(Long todoId, String content);
    void deleteMessage(Long todoId, Long messageId);
}
