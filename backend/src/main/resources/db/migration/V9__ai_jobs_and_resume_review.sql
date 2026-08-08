CREATE TABLE ai_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    target_id UUID NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ai_jobs_user_id ON ai_jobs(user_id);
CREATE INDEX idx_ai_jobs_status ON ai_jobs(status);

ALTER TABLE resumes ADD COLUMN review_score INT;
ALTER TABLE resumes ADD COLUMN review_summary TEXT;
ALTER TABLE resumes ADD COLUMN review_issues TEXT;
ALTER TABLE resumes ADD COLUMN review_suggestions TEXT;
ALTER TABLE resumes ADD COLUMN reviewed_at TIMESTAMP WITH TIME ZONE;
