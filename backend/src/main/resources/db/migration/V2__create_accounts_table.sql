
CREATE TABLE accounts(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    default_account BOOLEAN NOT NULL DEFAULT false,
    user_id UUID not null,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    constraint fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);