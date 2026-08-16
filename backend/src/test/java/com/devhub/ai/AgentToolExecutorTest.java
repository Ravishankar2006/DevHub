package com.devhub.ai;

import com.devhub.calendar.CalendarEvent;
import com.devhub.calendar.CalendarEventRepository;
import com.devhub.goals.Goal;
import com.devhub.goals.GoalRepository;
import com.devhub.goals.Habit;
import com.devhub.goals.HabitRepository;
import com.devhub.notes.Note;
import com.devhub.notes.NoteRepository;
import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.projects.ProjectStatus;
import com.devhub.support.BaseIntegrationTest;
import com.devhub.tasks.Task;
import com.devhub.tasks.TaskPriority;
import com.devhub.tasks.TaskRepository;
import com.devhub.tasks.TaskStatus;
import com.devhub.users.User;
import com.devhub.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgentToolExecutorTest extends BaseIntegrationTest {

    @Autowired
    private AgentToolExecutor agentToolExecutor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private GoalRepository goalRepository;

    private User registerUser(String prefix) throws Exception {
        String email = uniqueEmail(prefix);
        registerAndLogin(email);
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Test
    void createTaskResolvesMatchingProjectByName() throws Exception {
        User user = registerUser("agent-task");
        Project project = projectRepository.save(Project.builder()
                .user(user)
                .name("DevHub")
                .status(ProjectStatus.IN_PROGRESS)
                .build());

        AgentToolResult result = agentToolExecutor.execute(user, "create_task", Map.of(
                "title", "Write docs",
                "projectName", "devhub"));

        assertThat(result.success()).isTrue();
        UUID taskId = UUID.fromString((String) result.payload().get("id"));
        Task saved = taskRepository.findById(taskId).orElseThrow();
        assertThat(saved.getProject().getId()).isEqualTo(project.getId());
    }

    @Test
    void createTaskWithoutMatchingProjectNameStaysStandalone() throws Exception {
        User user = registerUser("agent-task-nomatch");

        AgentToolResult result = agentToolExecutor.execute(user, "create_task", Map.of(
                "title", "Standalone task",
                "projectName", "Nonexistent Project"));

        assertThat(result.success()).isTrue();
        UUID taskId = UUID.fromString((String) result.payload().get("id"));
        Task saved = taskRepository.findById(taskId).orElseThrow();
        assertThat(saved.getProject()).isNull();
    }

    @Test
    void createCalendarEventWithoutStartTimeFails() throws Exception {
        User user = registerUser("agent-event-fail");

        AgentToolResult result = agentToolExecutor.execute(user, "create_calendar_event", Map.of("title", "Standup"));

        assertThat(result.success()).isFalse();
    }

    @Test
    void createHabitHappyPath() throws Exception {
        User user = registerUser("agent-habit");

        AgentToolResult result = agentToolExecutor.execute(user, "create_habit", Map.of("title", "Read daily"));

        assertThat(result.success()).isTrue();
        assertThat(result.payload().get("title")).isEqualTo("Read daily");
    }

    @Test
    void createNoteHappyPath() throws Exception {
        User user = registerUser("agent-note");

        AgentToolResult result = agentToolExecutor.execute(user, "create_note", Map.of(
                "title", "Groceries", "content", "eggs, bread"));

        assertThat(result.success()).isTrue();
    }

    @Test
    void createGoalHappyPath() throws Exception {
        User user = registerUser("agent-goal");

        AgentToolResult result = agentToolExecutor.execute(user, "create_goal", Map.of("title", "Ship v1"));

        assertThat(result.success()).isTrue();
    }

    @Test
    void createProjectHappyPath() throws Exception {
        User user = registerUser("agent-project");

        AgentToolResult result = agentToolExecutor.execute(user, "create_project", Map.of("name", "New Project"));

        assertThat(result.success()).isTrue();
    }

    @Test
    void unknownToolReturnsError() throws Exception {
        User user = registerUser("agent-unknown");

        AgentToolResult result = agentToolExecutor.execute(user, "delete_everything", Map.of());

        assertThat(result.success()).isFalse();
        assertThat(result.payload().get("message")).isEqualTo("Unknown tool: delete_everything");
    }

    @Test
    void projectNameResolutionIsScopedToOwner() throws Exception {
        User owner = registerUser("agent-owner");
        projectRepository.save(Project.builder().user(owner).name("Secret Project").status(ProjectStatus.PLANNING).build());

        User other = registerUser("agent-other");

        AgentToolResult result = agentToolExecutor.execute(other, "create_task", Map.of(
                "title", "Sneaky task",
                "projectName", "Secret Project"));

        assertThat(result.success()).isTrue();
        UUID taskId = UUID.fromString((String) result.payload().get("id"));
        Task saved = taskRepository.findById(taskId).orElseThrow();
        assertThat(saved.getProject()).isNull();
    }

    @Test
    void previewCreateTaskDoesNotPersist() throws Exception {
        User user = registerUser("agent-preview-task");

        AgentToolResult result = agentToolExecutor.preview(user, "create_task", Map.of("title", "Draft task"));

        assertThat(result.success()).isTrue();
        assertThat(result.payload().get("summary")).asString().contains("Draft task");
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).isEmpty();
    }

    @Test
    void previewAmbiguousDoesNotThrowUncaught() throws Exception {
        User user = registerUser("agent-preview-ambiguous");
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        AgentToolResult result = agentToolExecutor.preview(user, "update_task", Map.of("title", "Dup", "status", "DONE"));

        assertThat(result.success()).isFalse();
        assertThat(result.payload().get("message")).asString().contains("Found 2 tasks matching");
    }

    @Test
    void updateTaskAppliesOnlyProvidedFields() throws Exception {
        User user = registerUser("agent-update-task");
        Task task = taskRepository.save(Task.builder()
                .user(user)
                .title("Original title")
                .description("Original description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(java.time.LocalDate.of(2026, 12, 1))
                .build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_task", Map.of(
                "title", "Original title",
                "status", "DONE"));

        assertThat(result.success()).isTrue();
        Task reloaded = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(reloaded.getTitle()).isEqualTo("Original title");
        assertThat(reloaded.getDescription()).isEqualTo("Original description");
        assertThat(reloaded.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(reloaded.getDueDate()).isEqualTo(java.time.LocalDate.of(2026, 12, 1));
    }

    @Test
    void updateTaskWithAmbiguousTitleReturnsError() throws Exception {
        User user = registerUser("agent-task-ambig");
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_task", Map.of("title", "Dup", "status", "DONE"));

        assertThat(result.success()).isFalse();
        assertThat(result.payload().get("message")).asString().contains("Found 2 tasks matching");
    }

    @Test
    void updateTaskWithNoMatchReturnsError() throws Exception {
        User user = registerUser("agent-update-task-nomatch");

        AgentToolResult result = agentToolExecutor.execute(user, "update_task", Map.of("title", "Nope", "status", "DONE"));

        assertThat(result.success()).isFalse();
        assertThat(result.payload().get("message")).isEqualTo("No task found matching 'Nope'.");
    }

    @Test
    void deleteTaskAmbiguousReturnsErrorWithoutDeleting() throws Exception {
        User user = registerUser("agent-del-task-ambig");
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
        taskRepository.save(Task.builder().user(user).title("Dup").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        AgentToolResult result = agentToolExecutor.execute(user, "delete_task", Map.of("title", "Dup"));

        assertThat(result.success()).isFalse();
        assertThat(taskRepository.findByUserIdOrderByPositionAsc(user.getId())).hasSize(2);
    }

    @Test
    void deleteTaskHappyPathRemovesRow() throws Exception {
        User user = registerUser("agent-delete-task");
        Task task = taskRepository.save(Task.builder().user(user).title("Gone soon").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        AgentToolResult result = agentToolExecutor.execute(user, "delete_task", Map.of("title", "Gone soon"));

        assertThat(result.success()).isTrue();
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void updateProjectAppliesOnlyProvidedFields() throws Exception {
        User user = registerUser("agent-update-project");
        Project project = projectRepository.save(Project.builder()
                .user(user)
                .name("Original Name")
                .description("Original description")
                .status(ProjectStatus.PLANNING)
                .stackTags("Java,React")
                .archived(false)
                .build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_project", Map.of(
                "name", "Original Name",
                "status", "IN_PROGRESS"));

        assertThat(result.success()).isTrue();
        Project reloaded = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(reloaded.getName()).isEqualTo("Original Name");
        assertThat(reloaded.getDescription()).isEqualTo("Original description");
        assertThat(reloaded.getStackTags()).isEqualTo("Java,React");
        assertThat(reloaded.isArchived()).isFalse();
    }

    @Test
    void updateProjectWithAmbiguousNameReturnsError() throws Exception {
        User user = registerUser("agent-proj-ambig");
        projectRepository.save(Project.builder().user(user).name("Dup").status(ProjectStatus.PLANNING).build());
        projectRepository.save(Project.builder().user(user).name("Dup").status(ProjectStatus.PLANNING).build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_project", Map.of("name", "Dup", "status", "IN_PROGRESS"));

        assertThat(result.success()).isFalse();
        assertThat(result.payload().get("message")).asString().contains("Found 2 projects matching");
    }

    @Test
    void updateHabitChangesOnlyGivenField() throws Exception {
        User user = registerUser("agent-update-habit");
        Habit habit = habitRepository.save(Habit.builder().user(user).title("Read daily").frequency(com.devhub.goals.HabitFrequency.DAILY).build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_habit", Map.of(
                "title", "Read daily",
                "frequency", "WEEKLY"));

        assertThat(result.success()).isTrue();
        Habit reloaded = habitRepository.findById(habit.getId()).orElseThrow();
        assertThat(reloaded.getFrequency()).isEqualTo(com.devhub.goals.HabitFrequency.WEEKLY);
        assertThat(reloaded.getTitle()).isEqualTo("Read daily");
    }

    @Test
    void updateNoteChangesOnlyGivenField() throws Exception {
        User user = registerUser("agent-update-note");
        Note note = noteRepository.save(Note.builder().user(user).title("Groceries").content("eggs").tags("food,home").build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_note", Map.of(
                "title", "Groceries",
                "content", "eggs, bread, milk"));

        assertThat(result.success()).isTrue();
        Note reloaded = noteRepository.findById(note.getId()).orElseThrow();
        assertThat(reloaded.getContent()).isEqualTo("eggs, bread, milk");
        assertThat(reloaded.getTitle()).isEqualTo("Groceries");
        assertThat(reloaded.getTags()).isEqualTo("food,home");
    }

    @Test
    void updateCalendarEventChangesOnlyGivenField() throws Exception {
        User user = registerUser("agent-update-event");
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
        CalendarEvent event = calendarEventRepository.save(CalendarEvent.builder()
                .user(user)
                .title("Standup")
                .description("Daily sync")
                .startTime(start)
                .location("Room A")
                .build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_calendar_event", Map.of(
                "title", "Standup",
                "location", "Room B"));

        assertThat(result.success()).isTrue();
        CalendarEvent reloaded = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getLocation()).isEqualTo("Room B");
        assertThat(reloaded.getDescription()).isEqualTo("Daily sync");
        assertThat(reloaded.getStartTime()).isEqualTo(start);
    }

    @Test
    void updateGoalChangesOnlyGivenField() throws Exception {
        User user = registerUser("agent-update-goal");
        Goal goal = goalRepository.save(Goal.builder().user(user).title("Ship v1").description("Launch it").progressPercent(20).build());

        AgentToolResult result = agentToolExecutor.execute(user, "update_goal", Map.of(
                "title", "Ship v1",
                "progressPercent", 80));

        assertThat(result.success()).isTrue();
        Goal reloaded = goalRepository.findById(goal.getId()).orElseThrow();
        assertThat(reloaded.getProgressPercent()).isEqualTo(80);
        assertThat(reloaded.getDescription()).isEqualTo("Launch it");
    }
}
