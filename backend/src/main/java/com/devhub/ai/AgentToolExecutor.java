package com.devhub.ai;

import com.devhub.calendar.CalendarEvent;
import com.devhub.calendar.CalendarEventRepository;
import com.devhub.calendar.CalendarEventService;
import com.devhub.calendar.CalendarEventCategory;
import com.devhub.calendar.dto.CalendarEventDto;
import com.devhub.calendar.dto.CalendarEventRequest;
import com.devhub.common.ApiException;
import com.devhub.goals.Goal;
import com.devhub.goals.GoalRepository;
import com.devhub.goals.GoalService;
import com.devhub.goals.GoalStatus;
import com.devhub.goals.GoalType;
import com.devhub.goals.Habit;
import com.devhub.goals.HabitFrequency;
import com.devhub.goals.HabitRepository;
import com.devhub.goals.HabitService;
import com.devhub.goals.dto.GoalDto;
import com.devhub.goals.dto.GoalRequest;
import com.devhub.goals.dto.HabitDto;
import com.devhub.goals.dto.HabitRequest;
import com.devhub.notes.Note;
import com.devhub.notes.NoteFolder;
import com.devhub.notes.NoteFolderRepository;
import com.devhub.notes.NoteRepository;
import com.devhub.notes.NoteService;
import com.devhub.notes.dto.NoteDto;
import com.devhub.notes.dto.NoteMapper;
import com.devhub.notes.dto.NoteRequest;
import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.projects.ProjectService;
import com.devhub.projects.ProjectStatus;
import com.devhub.projects.dto.ProjectDto;
import com.devhub.projects.dto.ProjectMapper;
import com.devhub.projects.dto.ProjectRequest;
import com.devhub.tasks.Task;
import com.devhub.tasks.TaskPriority;
import com.devhub.tasks.TaskRepository;
import com.devhub.tasks.TaskService;
import com.devhub.tasks.TaskStatus;
import com.devhub.tasks.dto.TaskDto;
import com.devhub.tasks.dto.TaskRequest;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentToolExecutor {

    private final TaskService taskService;
    private final HabitService habitService;
    private final NoteService noteService;
    private final CalendarEventService calendarEventService;
    private final GoalService goalService;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final GoalRepository goalRepository;
    private final NoteFolderRepository noteFolderRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;
    private final NoteRepository noteRepository;
    private final CalendarEventRepository calendarEventRepository;

    private record ResolvedUpdate<T>(T entity, Object request) {}

    /**
     * Resolves and validates a tool call without mutating anything. Used at propose-time so a
     * proposal is only persisted when the underlying action would actually succeed.
     */
    public AgentToolResult preview(User currentUser, String toolName, Map<String, Object> args) {
        log.info("Agent tool preview: user={} tool={} args={}", currentUser.getId(), toolName, args);
        try {
            return switch (toolName) {
                case "create_task" -> AgentToolResult.ok(Map.of("summary", previewCreateTask(args)));
                case "update_task" -> AgentToolResult.ok(Map.of("summary", previewUpdateTask(currentUser, args)));
                case "delete_task" -> AgentToolResult.ok(Map.of("summary", previewDeleteTask(currentUser, args)));
                case "create_habit" -> AgentToolResult.ok(Map.of("summary", previewCreateHabit(args)));
                case "update_habit" -> AgentToolResult.ok(Map.of("summary", previewUpdateHabit(currentUser, args)));
                case "delete_habit" -> AgentToolResult.ok(Map.of("summary", previewDeleteHabit(currentUser, args)));
                case "create_note" -> AgentToolResult.ok(Map.of("summary", previewCreateNote(args)));
                case "update_note" -> AgentToolResult.ok(Map.of("summary", previewUpdateNote(currentUser, args)));
                case "delete_note" -> AgentToolResult.ok(Map.of("summary", previewDeleteNote(currentUser, args)));
                case "create_calendar_event" -> AgentToolResult.ok(Map.of("summary", previewCreateCalendarEvent(args)));
                case "update_calendar_event" -> AgentToolResult.ok(Map.of("summary", previewUpdateCalendarEvent(currentUser, args)));
                case "delete_calendar_event" -> AgentToolResult.ok(Map.of("summary", previewDeleteCalendarEvent(currentUser, args)));
                case "create_goal" -> AgentToolResult.ok(Map.of("summary", previewCreateGoal(args)));
                case "update_goal" -> AgentToolResult.ok(Map.of("summary", previewUpdateGoal(currentUser, args)));
                case "delete_goal" -> AgentToolResult.ok(Map.of("summary", previewDeleteGoal(currentUser, args)));
                case "create_project" -> AgentToolResult.ok(Map.of("summary", previewCreateProject(args)));
                case "update_project" -> AgentToolResult.ok(Map.of("summary", previewUpdateProject(currentUser, args)));
                case "delete_project" -> AgentToolResult.ok(Map.of("summary", previewDeleteProject(currentUser, args)));
                default -> AgentToolResult.error("Unknown tool: " + toolName);
            };
        } catch (ApiException e) {
            return AgentToolResult.error(e.getMessage());
        } catch (Exception e) {
            log.warn("Agent tool preview failed: user={} tool={}", currentUser.getId(), toolName, e);
            return AgentToolResult.error("Could not validate this action: " + e.getMessage());
        }
    }

    /** Actually performs a tool call. Only invoked once a proposal has been confirmed. */
    public AgentToolResult execute(User currentUser, String toolName, Map<String, Object> args) {
        log.info("Agent tool call: user={} tool={} args={}", currentUser.getId(), toolName, args);
        try {
            return switch (toolName) {
                case "create_task" -> doCreateTask(currentUser, args);
                case "update_task" -> doUpdateTask(currentUser, args);
                case "delete_task" -> doDeleteTask(currentUser, args);
                case "create_habit" -> doCreateHabit(currentUser, args);
                case "update_habit" -> doUpdateHabit(currentUser, args);
                case "delete_habit" -> doDeleteHabit(currentUser, args);
                case "create_note" -> doCreateNote(currentUser, args);
                case "update_note" -> doUpdateNote(currentUser, args);
                case "delete_note" -> doDeleteNote(currentUser, args);
                case "create_calendar_event" -> doCreateCalendarEvent(currentUser, args);
                case "update_calendar_event" -> doUpdateCalendarEvent(currentUser, args);
                case "delete_calendar_event" -> doDeleteCalendarEvent(currentUser, args);
                case "create_goal" -> doCreateGoal(currentUser, args);
                case "update_goal" -> doUpdateGoal(currentUser, args);
                case "delete_goal" -> doDeleteGoal(currentUser, args);
                case "create_project" -> doCreateProject(currentUser, args);
                case "update_project" -> doUpdateProject(currentUser, args);
                case "delete_project" -> doDeleteProject(currentUser, args);
                default -> AgentToolResult.error("Unknown tool: " + toolName);
            };
        } catch (ApiException e) {
            return AgentToolResult.error(e.getMessage());
        } catch (Exception e) {
            log.warn("Agent tool call failed: user={} tool={}", currentUser.getId(), toolName, e);
            return AgentToolResult.error("Could not complete this action: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Task

    private Task resolveTaskTarget(User user, Map<String, Object> args, String argKey) {
        String title = requireString(args, argKey);
        if (title == null) throw new ApiException("A title is required to identify the task.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(taskRepository.findByUserIdOrderByPositionAsc(user.getId()), Task::getTitle, title, "task");
    }

    private ResolvedUpdate<Task> resolveTaskUpdate(User user, Map<String, Object> args) {
        Task task = resolveTaskTarget(user, args, "title");

        TaskRequest request = new TaskRequest();
        request.setTitle(hasArg(args, "newTitle") ? stringArg(args, "newTitle") : task.getTitle());
        request.setDescription(hasArg(args, "description") ? stringArg(args, "description") : task.getDescription());
        request.setProjectId(hasArg(args, "projectName")
                ? resolveProjectId(user, stringArg(args, "projectName"))
                : (task.getProject() != null ? task.getProject().getId() : null));
        request.setMilestoneId(task.getMilestone() != null ? task.getMilestone().getId() : null);
        request.setStatus(hasArg(args, "status") ? parseEnum(TaskStatus.class, args.get("status")) : task.getStatus());
        request.setPriority(hasArg(args, "priority") ? parseEnum(TaskPriority.class, args.get("priority")) : task.getPriority());
        request.setDueDate(hasArg(args, "dueDate") ? parseLocalDate(stringArg(args, "dueDate")) : task.getDueDate());
        return new ResolvedUpdate<>(task, request);
    }

    private String previewCreateTask(Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) throw new ApiException("A title is required to create a task.", HttpStatus.BAD_REQUEST);
        return "Create task \"" + title + "\"" + describeChanges(args, List.of("description", "projectName", "status", "priority", "dueDate"));
    }

    private AgentToolResult doCreateTask(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) return AgentToolResult.error("A title is required to create a task.");

        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription(stringArg(args, "description"));
        request.setProjectId(resolveProjectId(user, stringArg(args, "projectName")));
        request.setStatus(parseEnum(TaskStatus.class, args.get("status")));
        request.setPriority(parseEnum(TaskPriority.class, args.get("priority")));
        request.setDueDate(parseLocalDate(stringArg(args, "dueDate")));

        TaskDto dto = taskService.createTask(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewUpdateTask(User user, Map<String, Object> args) {
        Task task = resolveTaskUpdate(user, args).entity();
        return "Update task \"" + task.getTitle() + "\"" + describeChanges(args, List.of("newTitle", "description", "projectName", "status", "priority", "dueDate"));
    }

    private AgentToolResult doUpdateTask(User user, Map<String, Object> args) {
        ResolvedUpdate<Task> resolved = resolveTaskUpdate(user, args);
        TaskDto dto = taskService.updateTask(user, resolved.entity().getId(), (TaskRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewDeleteTask(User user, Map<String, Object> args) {
        Task task = resolveTaskTarget(user, args, "title");
        return "Delete task \"" + task.getTitle() + "\" -- this cannot be undone.";
    }

    private AgentToolResult doDeleteTask(User user, Map<String, Object> args) {
        Task task = resolveTaskTarget(user, args, "title");
        taskService.deleteTask(user, task.getId());
        return AgentToolResult.ok(Map.of("id", task.getId().toString(), "title", task.getTitle()));
    }

    // --------------------------------------------------------------- Habit

    private Habit resolveHabitTarget(User user, Map<String, Object> args, String argKey) {
        String title = requireString(args, argKey);
        if (title == null) throw new ApiException("A title is required to identify the habit.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(habitRepository.findByUserIdOrderByCreatedAtDesc(user.getId()), Habit::getTitle, title, "habit");
    }

    private ResolvedUpdate<Habit> resolveHabitUpdate(User user, Map<String, Object> args) {
        Habit habit = resolveHabitTarget(user, args, "title");

        HabitRequest request = new HabitRequest();
        request.setTitle(hasArg(args, "newTitle") ? stringArg(args, "newTitle") : habit.getTitle());
        request.setGoalId(hasArg(args, "goalName")
                ? resolveGoalId(user, stringArg(args, "goalName"))
                : (habit.getGoal() != null ? habit.getGoal().getId() : null));
        request.setFrequency(hasArg(args, "frequency") ? parseEnum(HabitFrequency.class, args.get("frequency")) : habit.getFrequency());
        return new ResolvedUpdate<>(habit, request);
    }

    private String previewCreateHabit(Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) throw new ApiException("A title is required to create a habit.", HttpStatus.BAD_REQUEST);
        return "Create habit \"" + title + "\"" + describeChanges(args, List.of("goalName", "frequency"));
    }

    private AgentToolResult doCreateHabit(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) return AgentToolResult.error("A title is required to create a habit.");

        HabitRequest request = new HabitRequest();
        request.setTitle(title);
        request.setGoalId(resolveGoalId(user, stringArg(args, "goalName")));
        request.setFrequency(parseEnum(HabitFrequency.class, args.get("frequency")));

        HabitDto dto = habitService.createHabit(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewUpdateHabit(User user, Map<String, Object> args) {
        Habit habit = resolveHabitUpdate(user, args).entity();
        return "Update habit \"" + habit.getTitle() + "\"" + describeChanges(args, List.of("newTitle", "goalName", "frequency"));
    }

    private AgentToolResult doUpdateHabit(User user, Map<String, Object> args) {
        ResolvedUpdate<Habit> resolved = resolveHabitUpdate(user, args);
        HabitDto dto = habitService.updateHabit(user, resolved.entity().getId(), (HabitRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewDeleteHabit(User user, Map<String, Object> args) {
        Habit habit = resolveHabitTarget(user, args, "title");
        return "Delete habit \"" + habit.getTitle() + "\" -- this cannot be undone.";
    }

    private AgentToolResult doDeleteHabit(User user, Map<String, Object> args) {
        Habit habit = resolveHabitTarget(user, args, "title");
        habitService.deleteHabit(user, habit.getId());
        return AgentToolResult.ok(Map.of("id", habit.getId().toString(), "title", habit.getTitle()));
    }

    // ---------------------------------------------------------------- Note

    private Note resolveNoteTarget(User user, Map<String, Object> args, String argKey) {
        String title = requireString(args, argKey);
        if (title == null) throw new ApiException("A title is required to identify the note.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(noteRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()), Note::getTitle, title, "note");
    }

    private ResolvedUpdate<Note> resolveNoteUpdate(User user, Map<String, Object> args) {
        Note note = resolveNoteTarget(user, args, "title");

        NoteRequest request = new NoteRequest();
        request.setTitle(hasArg(args, "newTitle") ? stringArg(args, "newTitle") : note.getTitle());
        request.setContent(hasArg(args, "content") ? stringArg(args, "content") : note.getContent());
        request.setTags(hasArg(args, "tags") ? stringListArg(args, "tags") : NoteMapper.parseTags(note.getTags()));
        request.setFolderId(hasArg(args, "folderName")
                ? resolveFolderId(user, stringArg(args, "folderName"))
                : (note.getFolder() != null ? note.getFolder().getId() : null));
        return new ResolvedUpdate<>(note, request);
    }

    private String previewCreateNote(Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) throw new ApiException("A title is required to create a note.", HttpStatus.BAD_REQUEST);
        return "Create note \"" + title + "\"" + describeChanges(args, List.of("content", "tags", "folderName"));
    }

    private AgentToolResult doCreateNote(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) return AgentToolResult.error("A title is required to create a note.");

        NoteRequest request = new NoteRequest();
        request.setTitle(title);
        request.setContent(stringArg(args, "content"));
        request.setTags(stringListArg(args, "tags"));
        request.setFolderId(resolveFolderId(user, stringArg(args, "folderName")));

        NoteDto dto = noteService.createNote(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewUpdateNote(User user, Map<String, Object> args) {
        Note note = resolveNoteUpdate(user, args).entity();
        return "Update note \"" + note.getTitle() + "\"" + describeChanges(args, List.of("newTitle", "content", "tags", "folderName"));
    }

    private AgentToolResult doUpdateNote(User user, Map<String, Object> args) {
        ResolvedUpdate<Note> resolved = resolveNoteUpdate(user, args);
        NoteDto dto = noteService.updateNote(user, resolved.entity().getId(), (NoteRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewDeleteNote(User user, Map<String, Object> args) {
        Note note = resolveNoteTarget(user, args, "title");
        return "Delete note \"" + note.getTitle() + "\" -- this cannot be undone.";
    }

    private AgentToolResult doDeleteNote(User user, Map<String, Object> args) {
        Note note = resolveNoteTarget(user, args, "title");
        noteService.deleteNote(user, note.getId());
        return AgentToolResult.ok(Map.of("id", note.getId().toString(), "title", note.getTitle()));
    }

    // -------------------------------------------------------- CalendarEvent

    private List<CalendarEvent> allUserEvents(User user) {
        return calendarEventRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(
                user.getId(), Instant.now().minus(3650, ChronoUnit.DAYS), Instant.now().plus(3650, ChronoUnit.DAYS));
    }

    private CalendarEvent resolveEventTarget(User user, Map<String, Object> args, String argKey) {
        String title = requireString(args, argKey);
        if (title == null) throw new ApiException("A title is required to identify the calendar event.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(allUserEvents(user), CalendarEvent::getTitle, title, "calendar event");
    }

    private ResolvedUpdate<CalendarEvent> resolveEventUpdate(User user, Map<String, Object> args) {
        CalendarEvent event = resolveEventTarget(user, args, "title");

        CalendarEventRequest request = new CalendarEventRequest();
        request.setTitle(hasArg(args, "newTitle") ? stringArg(args, "newTitle") : event.getTitle());
        request.setDescription(hasArg(args, "description") ? stringArg(args, "description") : event.getDescription());
        request.setCategory(hasArg(args, "category") ? parseEnum(CalendarEventCategory.class, args.get("category")) : event.getCategory());
        request.setStartTime(hasArg(args, "startTime") ? parseInstant(stringArg(args, "startTime")) : event.getStartTime());
        request.setEndTime(hasArg(args, "endTime") ? parseInstant(stringArg(args, "endTime")) : event.getEndTime());
        request.setAllDay(hasArg(args, "allDay") ? booleanArg(args, "allDay") : event.isAllDay());
        request.setLocation(hasArg(args, "location") ? stringArg(args, "location") : event.getLocation());
        return new ResolvedUpdate<>(event, request);
    }

    private String previewCreateCalendarEvent(Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) throw new ApiException("A title is required to create a calendar event.", HttpStatus.BAD_REQUEST);
        if (parseInstant(stringArg(args, "startTime")) == null) {
            throw new ApiException("A valid startTime (ISO-8601 date-time) is required to create a calendar event.", HttpStatus.BAD_REQUEST);
        }
        return "Create calendar event \"" + title + "\"" + describeChanges(args, List.of("description", "category", "startTime", "endTime", "allDay", "location"));
    }

    private AgentToolResult doCreateCalendarEvent(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) return AgentToolResult.error("A title is required to create a calendar event.");

        Instant startTime = parseInstant(stringArg(args, "startTime"));
        if (startTime == null) {
            return AgentToolResult.error("A valid startTime (ISO-8601 date-time) is required to create a calendar event.");
        }

        CalendarEventRequest request = new CalendarEventRequest();
        request.setTitle(title);
        request.setDescription(stringArg(args, "description"));
        request.setCategory(parseEnum(CalendarEventCategory.class, args.get("category")));
        request.setStartTime(startTime);
        request.setEndTime(parseInstant(stringArg(args, "endTime")));
        request.setAllDay(booleanArg(args, "allDay"));
        request.setLocation(stringArg(args, "location"));

        CalendarEventDto dto = calendarEventService.createEvent(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewUpdateCalendarEvent(User user, Map<String, Object> args) {
        CalendarEvent event = resolveEventUpdate(user, args).entity();
        return "Update calendar event \"" + event.getTitle() + "\"" + describeChanges(args, List.of("newTitle", "description", "category", "startTime", "endTime", "allDay", "location"));
    }

    private AgentToolResult doUpdateCalendarEvent(User user, Map<String, Object> args) {
        ResolvedUpdate<CalendarEvent> resolved = resolveEventUpdate(user, args);
        CalendarEventDto dto = calendarEventService.updateEvent(user, resolved.entity().getId(), (CalendarEventRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewDeleteCalendarEvent(User user, Map<String, Object> args) {
        CalendarEvent event = resolveEventTarget(user, args, "title");
        return "Delete calendar event \"" + event.getTitle() + "\" -- this cannot be undone.";
    }

    private AgentToolResult doDeleteCalendarEvent(User user, Map<String, Object> args) {
        CalendarEvent event = resolveEventTarget(user, args, "title");
        calendarEventService.deleteEvent(user, event.getId());
        return AgentToolResult.ok(Map.of("id", event.getId().toString(), "title", event.getTitle()));
    }

    // ---------------------------------------------------------------- Goal

    private Goal resolveGoalTarget(User user, Map<String, Object> args, String argKey) {
        String title = requireString(args, argKey);
        if (title == null) throw new ApiException("A title is required to identify the goal.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(goalRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()), Goal::getTitle, title, "goal");
    }

    private ResolvedUpdate<Goal> resolveGoalUpdate(User user, Map<String, Object> args) {
        Goal goal = resolveGoalTarget(user, args, "title");

        GoalRequest request = new GoalRequest();
        request.setTitle(hasArg(args, "newTitle") ? stringArg(args, "newTitle") : goal.getTitle());
        request.setDescription(hasArg(args, "description") ? stringArg(args, "description") : goal.getDescription());
        request.setType(hasArg(args, "type") ? parseEnum(GoalType.class, args.get("type")) : goal.getType());
        request.setStatus(hasArg(args, "status") ? parseEnum(GoalStatus.class, args.get("status")) : goal.getStatus());
        request.setTargetDate(hasArg(args, "targetDate") ? parseLocalDate(stringArg(args, "targetDate")) : goal.getTargetDate());
        request.setProgressPercent(hasArg(args, "progressPercent") ? clampProgress(intArg(args, "progressPercent")) : goal.getProgressPercent());
        request.setProjectId(hasArg(args, "projectName")
                ? resolveProjectId(user, stringArg(args, "projectName"))
                : (goal.getProject() != null ? goal.getProject().getId() : null));
        return new ResolvedUpdate<>(goal, request);
    }

    private String previewCreateGoal(Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) throw new ApiException("A title is required to create a goal.", HttpStatus.BAD_REQUEST);
        return "Create goal \"" + title + "\"" + describeChanges(args, List.of("description", "type", "targetDate", "progressPercent", "projectName"));
    }

    private AgentToolResult doCreateGoal(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) return AgentToolResult.error("A title is required to create a goal.");

        GoalRequest request = new GoalRequest();
        request.setTitle(title);
        request.setDescription(stringArg(args, "description"));
        request.setType(parseEnum(GoalType.class, args.get("type")));
        request.setTargetDate(parseLocalDate(stringArg(args, "targetDate")));
        request.setProgressPercent(intArg(args, "progressPercent"));
        request.setProjectId(resolveProjectId(user, stringArg(args, "projectName")));

        GoalDto dto = goalService.createGoal(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewUpdateGoal(User user, Map<String, Object> args) {
        Goal goal = resolveGoalUpdate(user, args).entity();
        return "Update goal \"" + goal.getTitle() + "\"" + describeChanges(args, List.of("newTitle", "description", "type", "status", "targetDate", "progressPercent", "projectName"));
    }

    private AgentToolResult doUpdateGoal(User user, Map<String, Object> args) {
        ResolvedUpdate<Goal> resolved = resolveGoalUpdate(user, args);
        GoalDto dto = goalService.updateGoal(user, resolved.entity().getId(), (GoalRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private String previewDeleteGoal(User user, Map<String, Object> args) {
        Goal goal = resolveGoalTarget(user, args, "title");
        return "Delete goal \"" + goal.getTitle() + "\" -- this cannot be undone.";
    }

    private AgentToolResult doDeleteGoal(User user, Map<String, Object> args) {
        Goal goal = resolveGoalTarget(user, args, "title");
        goalService.deleteGoal(user, goal.getId());
        return AgentToolResult.ok(Map.of("id", goal.getId().toString(), "title", goal.getTitle()));
    }

    // ------------------------------------------------------------- Project

    private Project resolveProjectTarget(User user, Map<String, Object> args, String argKey) {
        String name = requireString(args, argKey);
        if (name == null) throw new ApiException("A name is required to identify the project.", HttpStatus.BAD_REQUEST);
        return resolveExactlyOne(projectRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()), Project::getName, name, "project");
    }

    private ResolvedUpdate<Project> resolveProjectUpdate(User user, Map<String, Object> args) {
        Project project = resolveProjectTarget(user, args, "name");

        ProjectRequest request = new ProjectRequest();
        request.setName(hasArg(args, "newName") ? stringArg(args, "newName") : project.getName());
        request.setDescription(hasArg(args, "description") ? stringArg(args, "description") : project.getDescription());
        request.setRepoUrl(hasArg(args, "repoUrl") ? stringArg(args, "repoUrl") : project.getRepoUrl());
        request.setLiveUrl(hasArg(args, "liveUrl") ? stringArg(args, "liveUrl") : project.getLiveUrl());
        request.setRoadmap(hasArg(args, "roadmap") ? stringArg(args, "roadmap") : project.getRoadmap());
        request.setStackTags(hasArg(args, "stackTags") ? stringListArg(args, "stackTags") : ProjectMapper.parseStackTags(project.getStackTags()));
        request.setStatus(hasArg(args, "status") ? parseEnum(ProjectStatus.class, args.get("status")) : project.getStatus());
        request.setArchived(project.isArchived());
        return new ResolvedUpdate<>(project, request);
    }

    private String previewCreateProject(Map<String, Object> args) {
        String name = requireString(args, "name");
        if (name == null) throw new ApiException("A name is required to create a project.", HttpStatus.BAD_REQUEST);
        return "Create project \"" + name + "\"" + describeChanges(args, List.of("description", "repoUrl", "liveUrl", "roadmap", "stackTags", "status"));
    }

    private AgentToolResult doCreateProject(User user, Map<String, Object> args) {
        String name = requireString(args, "name");
        if (name == null) return AgentToolResult.error("A name is required to create a project.");

        ProjectRequest request = new ProjectRequest();
        request.setName(name);
        request.setDescription(stringArg(args, "description"));
        request.setRepoUrl(stringArg(args, "repoUrl"));
        request.setLiveUrl(stringArg(args, "liveUrl"));
        request.setRoadmap(stringArg(args, "roadmap"));
        request.setStackTags(stringListArg(args, "stackTags"));
        request.setStatus(parseEnum(ProjectStatus.class, args.get("status")));

        ProjectDto dto = projectService.createProject(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "name", dto.getName()));
    }

    private String previewUpdateProject(User user, Map<String, Object> args) {
        Project project = resolveProjectUpdate(user, args).entity();
        return "Update project \"" + project.getName() + "\"" + describeChanges(args, List.of("newName", "description", "repoUrl", "liveUrl", "roadmap", "stackTags", "status"));
    }

    private AgentToolResult doUpdateProject(User user, Map<String, Object> args) {
        ResolvedUpdate<Project> resolved = resolveProjectUpdate(user, args);
        ProjectDto dto = projectService.updateProject(user, resolved.entity().getId(), (ProjectRequest) resolved.request());
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "name", dto.getName()));
    }

    private String previewDeleteProject(User user, Map<String, Object> args) {
        Project project = resolveProjectTarget(user, args, "name");
        return "Delete project \"" + project.getName() + "\" -- this also deletes its tasks and milestones and cannot be undone.";
    }

    private AgentToolResult doDeleteProject(User user, Map<String, Object> args) {
        Project project = resolveProjectTarget(user, args, "name");
        projectService.deleteProject(user, project.getId());
        return AgentToolResult.ok(Map.of("id", project.getId().toString(), "name", project.getName()));
    }

    // -------------------------------------------------------------- Shared

    private <T> T resolveExactlyOne(List<T> candidates, Function<T, String> nameOf, String query, String entityLabel) {
        String q = query.strip();
        List<T> matches = candidates.stream()
                .filter(c -> nameOf.apply(c) != null && nameOf.apply(c).strip().equalsIgnoreCase(q))
                .toList();
        if (matches.isEmpty()) {
            throw new ApiException("No " + entityLabel + " found matching '" + query + "'.", HttpStatus.NOT_FOUND);
        }
        if (matches.size() > 1) {
            String listing = matches.stream().map(nameOf).collect(Collectors.joining("\", \"", "\"", "\""));
            throw new ApiException("Found " + matches.size() + " " + entityLabel + "s matching '" + query
                    + "': " + listing + ". Please specify which one.", HttpStatus.CONFLICT);
        }
        return matches.get(0);
    }

    private boolean hasArg(Map<String, Object> args, String key) {
        return args.containsKey(key) && args.get(key) != null;
    }

    private String describeChanges(Map<String, Object> args, List<String> changeableKeys) {
        List<String> parts = new ArrayList<>();
        for (String key : changeableKeys) {
            if (hasArg(args, key)) {
                parts.add(key + " -> " + args.get(key));
            }
        }
        return parts.isEmpty() ? "" : " (" + String.join(", ", parts) + ")";
    }

    private UUID resolveProjectId(User user, String name) {
        if (name == null || name.isBlank()) return null;
        return projectRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(p -> p.getName() != null && p.getName().strip().equalsIgnoreCase(name.strip()))
                .findFirst()
                .map(Project::getId)
                .orElse(null);
    }

    private UUID resolveGoalId(User user, String name) {
        if (name == null || name.isBlank()) return null;
        return goalRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(g -> g.getTitle() != null && g.getTitle().strip().equalsIgnoreCase(name.strip()))
                .findFirst()
                .map(Goal::getId)
                .orElse(null);
    }

    private UUID resolveFolderId(User user, String name) {
        if (name == null || name.isBlank()) return null;
        return noteFolderRepository.findByUserIdOrderByNameAsc(user.getId()).stream()
                .filter(f -> f.getName() != null && f.getName().strip().equalsIgnoreCase(name.strip()))
                .findFirst()
                .map(NoteFolder::getId)
                .orElse(null);
    }

    private String requireString(Map<String, Object> args, String key) {
        String value = stringArg(args, key);
        return (value == null || value.isBlank()) ? null : value;
    }

    private String stringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean booleanArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Boolean b) return b;
        if (value == null) return false;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Integer intArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value).strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int clampProgress(Integer progressPercent) {
        if (progressPercent == null) return 0;
        return Math.max(0, Math.min(100, progressPercent));
    }

    private List<String> stringListArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> E parseEnum(Class<E> type, Object raw) {
        if (raw == null) return null;
        try {
            return Enum.valueOf(type, String.valueOf(raw).strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Instant parseInstant(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Instant.parse(raw.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
