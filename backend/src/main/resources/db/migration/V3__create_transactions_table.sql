
CREATE TABLE transactions(
    id UUID PRIMARY KEY ,
    type VARCHAR(255) NOT NULL ,
    amount DECIMAL(19,2) NOT NULL ,
    description TEXT,
    date TIMESTAMP NOT NULL ,
    category VARCHAR(255) NOT NULL,
    receipt_url varchar(500),
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    recurring_interval VARCHAR(50),
    next_recurring_date TIMESTAMP,
    last_processed TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    user_id UUID NOT NULL ,
    account_id UUID NOT NULL ,
    created_at TIMESTAMP NOT NULL ,
    updated_at TIMESTAMP NOT NULL ,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ,
    CONSTRAINT fk_transactions_account FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_type ON transactions(type);
