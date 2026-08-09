CREATE INDEX idx_milestones_user_id ON milestones(user_id);
CREATE INDEX idx_tasks_milestone_id ON tasks(milestone_id);
CREATE INDEX idx_goals_project_id ON goals(project_id);
