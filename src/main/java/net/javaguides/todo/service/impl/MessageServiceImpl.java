package net.javaguides.todo.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.MessageDto;
import net.javaguides.todo.entity.Message;
import net.javaguides.todo.entity.Todo;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.exception.ResourceNotFoundException;
import net.javaguides.todo.exception.TodoAPIException;
import net.javaguides.todo.repository.MessageRepository;
import net.javaguides.todo.repository.TodoRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.service.MessageService;
import net.javaguides.todo.utils.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;
    private TodoRepository todoRepository;
    private UserRepository userRepository;

    @Override
    public List<MessageDto> listMessagesByTodo(Long todoId) {
        todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        return messageRepository.findByTodo_IdOrderByCreatedAtAsc(todoId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MessageDto addMessage(Long todoId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Message content cannot be empty.");
        }

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Message m = new Message();
        m.setTodo(todo);
        m.setUser(user);
        m.setContent(content.trim());
        Message saved = messageRepository.save(m);

        return toDto(saved);
    }

    @Override
    public void deleteMessage(Long todoId, Long messageId) {
        todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        String requester = SecurityUtil.getCurrentUsername();
        User me = userRepository.findByUsernameOrEmail(requester, requester)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isAdmin = me.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        String ownerUsername = m.getUser().getUsername();

        if (!isAdmin && !Objects.equals(ownerUsername, requester)) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "Only author or admin can delete the message.");
        }

        messageRepository.delete(m);
    }

    private MessageDto toDto(Message m) {
        User u = m.getUser();
        String full = ((u.getLastName() != null ? u.getLastName() : "") +
                (u.getFirstName() != null ? u.getFirstName() : "")).trim();
        if (full.isEmpty()) full = u.getUsername();
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setTodoId(m.getTodo().getId());
        dto.setUserId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setAuthorFullName(full);
        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
