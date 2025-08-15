package net.javaguides.todo.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.TodoDto;
import net.javaguides.todo.entity.Todo;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.exception.ResourceNotFoundException;
import net.javaguides.todo.exception.TodoAPIException;
import net.javaguides.todo.repository.TodoRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.service.TodoService;
import net.javaguides.todo.utils.SecurityUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor//take care of creating the parameterized constructor
public class TodoServiceImpl implements TodoService {

    private TodoRepository todoRepository;

    private ModelMapper modelMapper;

    private UserRepository userRepository;

    @Override
    public TodoDto addTodo(TodoDto todoDto) {

//        convert TodoDto into Todo Jpa entity
//        Todo todo=new Todo();
//        todo.setTitle(todoDto.getTitle());
//        todo.setDescription(todoDto.getDescription());
//        todo.setCompleted(todoDto.isCompleted());

        Todo todo=modelMapper.map(todoDto,Todo.class); //single line convert TodoDto into Todo Jpa entity

        todo.setCreatedDate(LocalDate.now());
        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }
        //Todo Jpa entity
        Todo savedTodo=todoRepository.save(todo);

        //Convert saved Todo entity object into TodoDto object
//        TodoDto savedTodoDto=new TodoDto();
//        savedTodoDto.setId(savedTodo.getId());
//        savedTodoDto.setTitle(savedTodoDto.getTitle());
//        savedTodoDto.setDescription(savedTodo.getDescription());
//        savedTodoDto.setCompleted(savedTodo.isCompleted());

        TodoDto savedTodoDto=modelMapper.map(savedTodo,TodoDto.class);

        return  savedTodoDto;

    }

    @Override
    public TodoDto getTodo(Long id) {

        Todo todo= todoRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Todo not found with id"+id));
        return modelMapper.map(todo,TodoDto.class);
    }

    @Override
    public List<TodoDto> getAllTodos() {

        List<Todo> todos = todoRepository.findAll();

        return todos.stream().map((todo) -> modelMapper.map(todo, TodoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public TodoDto updateTodo(TodoDto todoDto, Long id) {
        Todo todo=todoRepository.findById(id)
        .orElseThrow(()->new ResourceNotFoundException("Todo not found with id: "+id));//上面那行是retrieve data from databases
        todo.setTitle(todoDto.getTitle());
        todo.setDescription(todoDto.getDescription());
        todo.setDueDate(todoDto.getDueDate());
        todo.setCompleted(todoDto.isCompleted());


        //已經審核的todo不能再edit
        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Reviewed task cannot be edited.");
        }

        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        if (todo.isOverdue() && todo.getDueDate() != null && todo.getDueDate().isAfter(LocalDate.now())) {
            todo.setOverdue(false);
        }

        Todo updatedTodo=todoRepository.save(todo);
        return modelMapper.map(updatedTodo,TodoDto.class);
    }

    @Override
    public void deleteTodo(Long id) {
        Todo todo=todoRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Todo not found with id:"+ id));
        todoRepository.deleteById(id);
    }

    @Override
    public TodoDto completeTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,  "This task has already been reviewed and cannot be marked as completed.");
        }

        todo.setCompleted(Boolean.TRUE);
        todo.setReviewed(Boolean.FALSE);

        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDate.now())) {
            todo.setOverdue(true);
        }

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        todo.setCompletedBy(user.getFirstName());
        todo.setCompletedAt(LocalDateTime.now());

        Todo updatedTodo = todoRepository.save(todo);
        return modelMapper.map(updatedTodo, TodoDto.class);
    }

    @Override
    public TodoDto inCompleteTodo(Long id) {

        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        if (todo.isReviewed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "This task has already been reviewed and cannot be marked as incomplete.");
        }

        todo.setCompleted(Boolean.FALSE);
        todo.setCompletedBy(null);       // 🔺 清除完成者
        todo.setCompletedAt(null);
        Todo updatedTodo = todoRepository.save(todo);
        return modelMapper.map(updatedTodo, TodoDto.class);
    }

    @Override
    public TodoDto reviewTodo(Long id) {

        Todo todo=todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

        if (!todo.isCompleted()) {
            throw new IllegalStateException("Cannot review a task that is not completed.");
        }
        todo.setReviewed(Boolean.TRUE);

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        todo.setReviewedBy(user.getFirstName());
        todo.setReviewedAt(LocalDateTime.now()); // ✅ 設定審核時間

        Todo updatedTodo = todoRepository.save(todo);
        return modelMapper.map(updatedTodo, TodoDto.class);
    }

    @Override
    public List<TodoDto> getPendingReviewTodos() {
        List<Todo> todos = todoRepository.findByCompletedTrueAndReviewedFalse();
        return todos.stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoDto> getReviewedTodos() {
        List<Todo> todos = todoRepository.findByCompletedTrueAndReviewedTrue();
        return todos.stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getTodoStatistics() {
        List<Todo> todos = todoRepository.findAll();
        LocalDate today = LocalDate.now();

        long total = todos.size();
        long completed = todos.stream().filter(Todo::isCompleted).count();
        long pendingReview = todos.stream().filter(t -> t.isCompleted() && !t.isReviewed()).count();
        long reviewed = todos.stream().filter(Todo::isReviewed).count();
        long overdue = todos.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isBefore(today))   // 已過截止日
                .filter(t -> !t.isCompleted())                 // 未完成
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pendingReview", pendingReview);
        stats.put("reviewed", reviewed);
        stats.put("overdue", overdue);

        return stats;
    }

    @Override
    public List<TodoDto> getOverdueTodos() {
        LocalDate today = LocalDate.now();
        List<Todo> overdueTodos = todoRepository.findByDueDateBefore(today); // ❌ 還沒排除 completed=true

        // 加上條件過濾：
        List<Todo> filtered = overdueTodos.stream()
                .filter(todo -> !todo.isCompleted()) // ✅ 僅顯示未完成的
                .collect(Collectors.toList());

        return filtered.stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .collect(Collectors.toList());
    }
}
