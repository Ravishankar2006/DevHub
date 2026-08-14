CREATE TABLE leetcode_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    leetcode_username VARCHAR(255) NOT NULL,
    ranking BIGINT,
    total_solved INT NOT NULL DEFAULT 0,
    easy_solved INT NOT NULL DEFAULT 0,
    medium_solved INT NOT NULL DEFAULT 0,
    hard_solved INT NOT NULL DEFAULT 0,
    total_active_days INT NOT NULL DEFAULT 0,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    daily_activity_json TEXT,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
