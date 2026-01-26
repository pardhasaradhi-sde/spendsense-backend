Create Table users(
    id UUID PRIMARY KEY,
    clerk_user_id VARCHAR(255) UNIQUE NOT NULL ,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL ,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NUll
);

CREATE INDEX idx_clerk_user_id on users(clerk_user_id);
CREATE INDEX idx_email on users(email);