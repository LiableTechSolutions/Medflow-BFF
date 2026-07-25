CREATE TABLE workspace_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(1000) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO workspace_settings (setting_key, setting_value, updated_at) VALUES
    ('organization.name', 'MedFlow Clinic', now()),
    ('organization.tagline', 'The clinical operating system for modern care teams', now()),
    ('appearance.theme', 'light', now()),
    ('security.session-timeout-minutes', '480', now());
