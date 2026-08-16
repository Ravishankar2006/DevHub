CREATE TABLE ai_proposed_actions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(30) NOT NULL,
    target_entity_ref VARCHAR(255),
    payload TEXT NOT NULL,
    summary TEXT NOT NULL,
    destructive BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_ai_proposed_actions_conversation_id ON ai_proposed_actions(conversation_id);
CREATE INDEX idx_ai_proposed_actions_user_id ON ai_proposed_actions(user_id);
CREATE INDEX idx_ai_proposed_actions_status ON ai_proposed_actions(status);
