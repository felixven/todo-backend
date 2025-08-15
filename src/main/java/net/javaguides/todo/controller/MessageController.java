package net.javaguides.todo.controller;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.MessageDto;
import net.javaguides.todo.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api/todos/{todoId}/messages")
@AllArgsConstructor
public class MessageController {

    private MessageService messageService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<MessageDto>> list(@PathVariable Long todoId) {
        return ResponseEntity.ok(messageService.listMessagesByTodo(todoId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<MessageDto> add(@PathVariable Long todoId,
                                          @RequestBody Map<String, String> body) {
        MessageDto saved = messageService.addMessage(todoId, body.get("content"));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("{messageId}")
    public ResponseEntity<String> delete(@PathVariable Long todoId,
                                         @PathVariable Long messageId) {
        messageService.deleteMessage(todoId, messageId);
        return ResponseEntity.ok("Message deleted successfully!");
    }
}
