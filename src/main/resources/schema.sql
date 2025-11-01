DROP TABLE IF EXISTS ticket_history;
DROP TABLE IF EXISTS ticket;

CREATE TABLE ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    assignee VARCHAR(100),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_date TIMESTAMP,
    last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_date TIMESTAMP,
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_status CHECK (status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS', 'BLOCKED', 'RESOLVED', 'CLOSED'))
);

CREATE TABLE ticket_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE
);