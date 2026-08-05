-- Create Learning Resources table
CREATE TABLE learning_resources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    resource_type VARCHAR(20) NOT NULL DEFAULT 'COURSE',
    provider VARCHAR(255),
    url VARCHAR(1024),
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    progress_percent INT NOT NULL DEFAULT 0,
    estimated_completion_date DATE,
    tags TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_learning_resources_user_id ON learning_resources(user_id);
