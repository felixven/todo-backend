package net.javaguides.todo.controller;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.ParticipationDetailResponse;
import net.javaguides.todo.dto.TodoDto;
import net.javaguides.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api/todos")
@AllArgsConstructor
public class TodoController {

    private TodoService todoService;

    //Build Add Todo REST API
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TodoDto> addTodo(@RequestBody TodoDto todoDto) {//@RequestBody extract the JSON object from the http request and then it will convert that JSON into TodoDto Java class object

        TodoDto savedTodo = todoService.addTodo(todoDto);

        return new ResponseEntity<>(savedTodo, HttpStatus.CREATED);
    }

    //Build get todo rest API
    //create a method and convert that method into rest API using spring annotation
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}")
    public ResponseEntity<TodoDto> getTodo(@PathVariable("id") Long todoId) {
        TodoDto todoDto = todoService.getTodo(todoId);
        return new ResponseEntity<>(todoDto, HttpStatus.OK);
    }

    //Build Get All Todos REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<TodoDto>> getAllTodos() {
        List<TodoDto> todos = todoService.getAllTodos();
        //return new ResponseEntity<>(todos, HttpStatus.OK);
        return ResponseEntity.ok(todos);
    }

    //Build Update Todo Rest Api
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")//update the existing resource(whole object)
    public ResponseEntity<TodoDto> updateTodo(@RequestBody TodoDto todoDto, @PathVariable("id") Long todoId) {//@RequestBody extract the updated JSON from the HTTP and it'll convert that JSON into Java object
        TodoDto updatedTodo = todoService.updateTodo(todoDto, todoId);
        return ResponseEntity.ok(updatedTodo);
    }

    //Build Delete Todo Rest Api
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable("id") Long todoId) {
        todoService.deleteTodo(todoId);
        return ResponseEntity.ok("Todo deleted successfully!");
    }

    //Build Complete Todo Rest Api
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("{id}/complete")//update the existing resource partially(only 部分field,here is "complete")
    public ResponseEntity<TodoDto> completeTodo(@PathVariable("id") Long todoId) {
        TodoDto updatedTodo = todoService.completeTodo(todoId);
        return ResponseEntity.ok(updatedTodo);
    }

    // Build In Complete Todo REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("{id}/in-complete")
    public ResponseEntity<TodoDto> inCompleteTodo(@PathVariable("id") Long todoId) {
        TodoDto updatedTodo = todoService.inCompleteTodo(todoId);
        return ResponseEntity.ok(updatedTodo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/review")
    public ResponseEntity<TodoDto> reviewTodo(@PathVariable("id") Long id) {
        TodoDto reviewedTodo = todoService.reviewTodo(id);
        return ResponseEntity.ok(reviewedTodo);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/pending-review")
    public ResponseEntity<List<TodoDto>> getPendingReviewTodos() {
        return ResponseEntity.ok(todoService.getPendingReviewTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/reviewed")
    public ResponseEntity<List<TodoDto>> getReviewedTodos() {
        return ResponseEntity.ok(todoService.getReviewedTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getTodoStats() {
        Map<String, Long> stats = todoService.getTodoStatistics();
        return ResponseEntity.ok(stats);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/overdue")
    public ResponseEntity<List<TodoDto>> getOverdueTodos() {
        return ResponseEntity.ok(todoService.getOverdueTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/participation")
    public ResponseEntity<Map<String, Long>> getParticipation(@PathVariable("id") Long todoId) {
        return ResponseEntity.ok(todoService.getParticipationStats(todoId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/participation-detail")
    public ResponseEntity<ParticipationDetailResponse> getParticipationDetail(@PathVariable("id") Long todoId) {
        return ResponseEntity.ok(todoService.getParticipationDetail(todoId));
    }

}
