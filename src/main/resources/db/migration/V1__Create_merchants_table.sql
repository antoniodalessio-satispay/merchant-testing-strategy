-- Create merchants table
CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    business_type VARCHAR(50),
    phonetics VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX idx_merchants_email ON merchants(email);

-- Create index on business_type for filtering
CREATE INDEX idx_merchants_business_type ON merchants(business_type);
