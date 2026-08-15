package com.devhub.ai;

import com.devhub.projects.Project;
import com.devhub.projects.ProjectRepository;
import com.devhub.projects.ProjectStatus;
import com.devhub.support.BaseIntegrationTest;
import com.devhub.tasks.Task;
import com.devhub.tasks.TaskRepository;
import com.devhub.users.User;
import com.devhub.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
}
