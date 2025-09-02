package net.javaguides.todo.controller;

import net.javaguides.todo.dto.CollabItemDetailDto;
import net.javaguides.todo.dto.CollabRowDto;
import net.javaguides.todo.dto.FinisherRowWithIdDto;
import net.javaguides.todo.dto.FinisherTodoDetailDto;
import lombok.AllArgsConstructor;
import net.javaguides.todo.repository.TodoItemRepository;
import net.javaguides.todo.repository.TodoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/todos/leaderboard")
@AllArgsConstructor
public class LeaderboardController {

    private final TodoItemRepository todoItemRepository;
    private final TodoRepository todoRepository;
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/collab")
    public ResponseEntity<List<CollabRowDto>> getCollabBoard() {
        List<Object[]> rows = todoItemRepository.collabLeaderboard();
        List<CollabRowDto> dtoList = rows.stream()
                .map(r -> new CollabRowDto(
                        (Long) r[0],          // userId
                        (String) r[1],        // userName
                        (Long) r[2]           // collabCount（完成數）
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/collab/{userId}/items")
    public ResponseEntity<List<CollabItemDetailDto>> getCollabDetails(@PathVariable Long userId) {
        List<Object[]> rows = todoItemRepository.collabDetailsForUser(userId);
        List<CollabItemDetailDto> dtoList = rows.stream()
                .map(r -> new CollabItemDetailDto(
                        (Long) r[0],          // todoId
                        (String) r[1],        // todoTitle
                        (Long) r[2],          // itemId
                        (String) r[3],        // itemTitle
                        (LocalDateTime) r[4]  // completedAt
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/finish-by-id")
    public ResponseEntity<List<FinisherRowWithIdDto>> getFinisherBoardById() {
        List<Object[]> rows = todoRepository.finisherLeaderboardByUserId();
        List<FinisherRowWithIdDto> out = rows.stream()
                .map(r -> new FinisherRowWithIdDto(
                        (Long) r[0],                // userId
                        (String) r[1],              // userName
                        ((Number) r[2]).longValue() // finishCount（完成任務數）
                ))
                .sorted(Comparator.comparingLong(FinisherRowWithIdDto::getFinishCount).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/finish/{userId}/todos-by-id")
    public ResponseEntity<List<FinisherTodoDetailDto>> getFinisherDetailsByUserId(@PathVariable Long userId) {
        List<Object[]> rows = todoRepository.finisherDetailsByUserId(userId);
        List<FinisherTodoDetailDto> out = rows.stream()
                .map(r -> new FinisherTodoDetailDto(
                        (Long) r[0],              // todoId
                        (String) r[1],            // todoTitle
                        (LocalDateTime) r[2]      // completedAt
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }
}