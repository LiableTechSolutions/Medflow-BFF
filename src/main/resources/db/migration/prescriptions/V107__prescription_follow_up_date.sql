ALTER TABLE prescriptions
  ADD COLUMN follow_up_date DATE;

CREATE INDEX idx_prescriptions_follow_up_date ON prescriptions (follow_up_date);
