CREATE TABLE lab_orders (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    ordered_by UUID NOT NULL,
    test_name VARCHAR(150) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result_summary VARCHAR(2000),
    ordered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lab_orders_patient ON lab_orders (patient_id);
CREATE INDEX idx_lab_orders_status ON lab_orders (status);
