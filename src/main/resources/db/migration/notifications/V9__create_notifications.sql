CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    category VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notifications_created ON notifications (created_at);
CREATE INDEX idx_notifications_read ON notifications (is_read);
