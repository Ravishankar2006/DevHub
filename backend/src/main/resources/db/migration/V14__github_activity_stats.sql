ALTER TABLE github_repos ADD COLUMN is_fork BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE github_repos ADD COLUMN open_issues INT NOT NULL DEFAULT 0;
ALTER TABLE github_repos ADD COLUMN watchers INT NOT NULL DEFAULT 0;
ALTER TABLE github_repos ADD COLUMN languages_json TEXT;

ALTER TABLE github_accounts ADD COLUMN current_streak INT NOT NULL DEFAULT 0;
ALTER TABLE github_accounts ADD COLUMN longest_streak INT NOT NULL DEFAULT 0;
ALTER TABLE github_accounts ADD COLUMN commits_last_30_days INT NOT NULL DEFAULT 0;
ALTER TABLE github_accounts ADD COLUMN daily_activity_json TEXT;
