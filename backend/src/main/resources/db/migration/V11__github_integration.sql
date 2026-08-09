CREATE TABLE github_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    github_user_id BIGINT NOT NULL,
    github_username VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    access_token TEXT NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE github_repos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    github_account_id UUID NOT NULL REFERENCES github_accounts(id) ON DELETE CASCADE,
    github_repo_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(100),
    stars INT NOT NULL DEFAULT 0,
    forks INT NOT NULL DEFAULT 0,
    html_url TEXT NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    pushed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (github_account_id, github_repo_id)
);
CREATE INDEX idx_github_repos_account_id ON github_repos(github_account_id);
