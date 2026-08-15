package com.devhub.ai;

import com.devhub.calendar.CalendarEventService;
import com.devhub.calendar.CalendarEventCategory;
import com.devhub.calendar.dto.CalendarEventDto;
import com.devhub.calendar.dto.CalendarEventRequest;
import com.devhub.common.ApiException;
import com.devhub.goals.Goal;
import com.devhub.goals.GoalRepository;
import com.devhub.goals.GoalService;
import com.devhub.goals.GoalType;
import com.devhub.goals.HabitFrequency;
import com.devhub.goals.HabitService;
import com.devhub.goals.dto.GoalDto;
import com.devhub.goals.dto.GoalRequest;
import com.devhub.goals.dto.HabitDto;
import com.devhub.goals.dto.HabitRequest;
import com.devhub.notes.NoteFolder;
import com.devhub.notes.NoteFolderRepository;
import com.devhub.notes.NoteService;
import com.devhub.notes.dto.NoteDto;
import com.devhub.notes.dto.NoteRequest;
import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.projects.ProjectService;
import com.devhub.projects.ProjectStatus;
import com.devhub.projects.dto.ProjectDto;
import com.devhub.projects.dto.ProjectRequest;
import com.devhub.tasks.TaskPriority;
import com.devhub.tasks.TaskService;
import com.devhub.tasks.TaskStatus;
import com.devhub.tasks.dto.TaskDto;
import com.devhub.tasks.dto.TaskRequest;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public AgentToolResult execute(User currentUser, String toolName, Map<String, Object> args) {
        log.info("Agent tool call: user={} tool={} args={}", currentUser.getId(), toolName, args);
        try {
            return switch (toolName) {
                case "create_task" -> createTask(currentUser, args);
                case "create_habit" -> createHabit(currentUser, args);
                case "create_note" -> createNote(currentUser, args);
                case "create_calendar_event" -> createCalendarEvent(currentUser, args);
                case "create_goal" -> createGoal(currentUser, args);
                case "create_project" -> createProject(currentUser, args);
                default -> AgentToolResult.error("Unknown tool: " + toolName);
            };
        } catch (ApiException e) {
            return AgentToolResult.error(e.getMessage());
        } catch (Exception e) {
            log.warn("Agent tool call failed: user={} tool={}", currentUser.getId(), toolName, e);
            return AgentToolResult.error("Could not complete this action: " + e.getMessage());
        }
    }

    private AgentToolResult createTask(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) {
            return AgentToolResult.error("A title is required to create a task.");
        }

        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription(stringArg(args, "description"));
        request.setProjectId(resolveProjectId(user, stringArg(args, "projectName")));
        request.setStatus(parseEnum(TaskStatus.class, args.get("status")));
        request.setPriority(parseEnum(TaskPriority.class, args.get("priority")));

        LocalDate dueDate = parseLocalDate(stringArg(args, "dueDate"));
        request.setDueDate(dueDate);

        TaskDto dto = taskService.createTask(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private AgentToolResult createHabit(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) {
            return AgentToolResult.error("A title is required to create a habit.");
        }

        HabitRequest request = new HabitRequest();
        request.setTitle(title);
        request.setGoalId(resolveGoalId(user, stringArg(args, "goalName")));
        request.setFrequency(parseEnum(HabitFrequency.class, args.get("frequency")));

        HabitDto dto = habitService.createHabit(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private AgentToolResult createNote(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) {
            return AgentToolResult.error("A title is required to create a note.");
        }

        NoteRequest request = new NoteRequest();
        request.setTitle(title);
        request.setContent(stringArg(args, "content"));
        request.setTags(stringListArg(args, "tags"));
        request.setFolderId(resolveFolderId(user, stringArg(args, "folderName")));

        NoteDto dto = noteService.createNote(user, request);
        return AgentToolResult.ok(Map.of("id", dto.getId().toString(), "title", dto.getTitle()));
    }

    private AgentToolResult createCalendarEvent(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) {
            return AgentToolResult.error("A title is required to create a calendar event.");
        }

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

    private AgentToolResult createGoal(User user, Map<String, Object> args) {
        String title = requireString(args, "title");
        if (title == null) {
            return AgentToolResult.error("A title is required to create a goal.");
        }

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

    private AgentToolResult createProject(User user, Map<String, Object> args) {
        String name = requireString(args, "name");
        if (name == null) {
            return AgentToolResult.error("A name is required to create a project.");
        }

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
