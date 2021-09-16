package guide.todo.controller;

import guide.todo.tasks.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
class TaskController {
    private final TaskService taskService;

    TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    List<TaskResponse> retrieveAll() {
        final var tasks = taskService.selectAll();

        return tasks.stream()
                .map(TaskController::toTaskResponse)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/{id}")
    ResponseEntity<TaskAttributesResponse> retrieveById(
            @PathVariable("id") final String taskIdString
    ) {
        final var taskId = toTaskId(taskIdString);

        final var task = taskService.select(taskId);

        return ResponseEntity.of(task.map(TaskController::toTaskAttributesResponse));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskIdResponse create(@RequestBody final TaskCreateRequest taskCreateRequest) {
        final var taskAttributesInsert = toTaskAttributesInsert(taskCreateRequest);

        final var createdTaskId = taskService.insert(taskAttributesInsert);

        return toTaskIdResponse(createdTaskId);
    }

    @PutMapping("/{id}")
    TaskAttributesResponse update(
            @PathVariable("id") final String taskIdString,
            @RequestBody final TaskUpdateRequest taskUpdateRequest
    ) {
        final var taskId = toTaskId(taskIdString);
        final var taskAttributes = toTaskAttributes(taskUpdateRequest);

        final var updatedTask = taskService.update(taskId, taskAttributes);

        return toTaskAttributesResponse(updatedTask);
    }

    @PatchMapping("/{id}")
    TaskAttributesResponse patch(
            @PathVariable("id") final String taskIdString,
            @RequestBody final TaskPatchRequest taskPatchRequest
    ) {
        final var taskId = toTaskId(taskIdString);
        final var taskAttributesPatch = toTaskAttributesPatch(taskPatchRequest);

        final var updatedTask = taskService.patch(taskId, taskAttributesPatch);

        return toTaskAttributesResponse(updatedTask);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") final String taskIdString) {
        final var taskId = toTaskId(taskIdString);

        taskService.delete(taskId);
    }

    @ExceptionHandler(TaskService.NoEntityException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleEmptyResultDataAccessException() {
    }

    static UUID toTaskId(String taskIdString) {
        return UUID.fromString(taskIdString);
    }


    static TaskAttributes toTaskAttributes(final TaskCreateRequest taskCreateRequest) {
        return new TaskAttributes(
                taskCreateRequest.getDetails(),
                TaskStatus.ACTIVE
        );
    }

    static TaskAttributesInsert toTaskAttributesInsert(TaskCreateRequest taskCreateRequest) {
        return new TaskAttributesInsert(taskCreateRequest.getDetails());
    }


    static TaskAttributes toTaskAttributes(final TaskUpdateRequest taskUpdateRequest) {
        final TaskStatus status = toTaskStatus(taskUpdateRequest.getStatus());
        return new TaskAttributes(
                taskUpdateRequest.getDetails(),
                status
        );
    }

    static TaskAttributesPatch toTaskAttributesPatch(final TaskPatchRequest taskPatchRequest) {
        final TaskStatus status = toTaskStatus(taskPatchRequest.getStatus());
        return new TaskAttributesPatch(
                taskPatchRequest.getDetails(),
                status
        );
    }

    static TaskResponse toTaskResponse(final Task task) {
        return new TaskResponse(
                task.getId().toString(),
                task.getDetails(),
                task.getStatus().name().toLowerCase(Locale.ENGLISH)
        );
    }

    static TaskAttributesResponse toTaskAttributesResponse(final TaskAttributes attributes) {
        return new TaskAttributesResponse(
                attributes.getDetails(),
                toTaskStatusString(attributes.getStatus())
        );
    }

    static TaskIdResponse toTaskIdResponse(final UUID id) {
        return new TaskIdResponse(id.toString());
    }

    static String toTaskStatusString(final TaskStatus taskStatus) {
        return taskStatus.name().toLowerCase(Locale.ENGLISH);
    }

    static TaskStatus toTaskStatus(final String taskStatusString) {
        try {
            return TaskStatus.valueOf(taskStatusString.toUpperCase(Locale.ENGLISH));
        } catch (Exception e) {
            return null;
        }
    }
}
