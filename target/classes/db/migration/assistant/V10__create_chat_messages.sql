CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_chat_messages_thread ON chat_messages (user_id, conversation_id, created_at);
