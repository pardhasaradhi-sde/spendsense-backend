CREATE TABLE budgets(
    id UUID PRIMARY KEY ,
    amount DECIMAL(19,2) NOT NULL,
    last_alert_sent TIMESTAMP,
    user_id UUID UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

create INDEX idx_budgets_user_id ON budgets(user_id);