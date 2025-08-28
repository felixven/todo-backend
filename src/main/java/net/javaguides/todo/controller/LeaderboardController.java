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

    // ========== 協作榜 ==========

    /**
     * 協作榜：統計使用者完成過的細項數量
     * Repository → todoItemRepository.collabLeaderboard()
     * 回傳：List<CollabRowDto>
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/collab")
    public ResponseEntity<List<CollabRowDto>> getCollabBoard() {
        List<Object[]> rows = todoItemRepository.collabLeaderboard();
        List<CollabRowDto> dtoList = rows.stream()
                .map(r -> new CollabRowDto(
                        (Long) r[0],          // userId
                        (String) r[1],        // userName（顯示名稱）
                        (Long) r[2]           // collabCount（細項完成數）
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 協作榜明細：查看某使用者完成過哪些細項
     * Repository → todoItemRepository.collabDetailsForUser(userId)
     * 回傳：List<CollabItemDetailDto>
     */
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

    // ========== 完成者榜（FK-only） ==========

    /**
     * 完成者榜：統計每個使用者完成的任務數（只計第一完成者）
     * Repository → todoRepository.finisherLeaderboardByUserId()
     * 回傳：List<FinisherRowWithIdDto>
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/finish-by-id")
    public ResponseEntity<List<FinisherRowWithIdDto>> getFinisherBoardById() {
        List<Object[]> rows = todoRepository.finisherLeaderboardByUserId();
        List<FinisherRowWithIdDto> out = rows.stream()
                .map(r -> new FinisherRowWithIdDto(
                        (Long) r[0],                // userId
                        (String) r[1],              // userName（firstName 或其他顯示名）
                        ((Number) r[2]).longValue() // finishCount（完成任務數）
                ))
                // 排序：完成數由大到小
                .sorted(Comparator.comparingLong(FinisherRowWithIdDto::getFinishCount).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    /**
     * 完成者榜明細：查看某使用者完成過哪些任務
     * Repository → todoRepository.finisherDetailsByUserId(userId)
     * 回傳：List<FinisherTodoDetailDto>
     */
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