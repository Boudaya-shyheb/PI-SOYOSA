-- Create order courier assignments table
CREATE TABLE IF NOT EXISTS order_courier_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    courier_id BIGINT NOT NULL,
    assigned_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_courier_assignment_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_courier_assignments_order ON order_courier_assignments(order_id);
CREATE INDEX IF NOT EXISTS idx_order_courier_assignments_courier ON order_courier_assignments(courier_id);
