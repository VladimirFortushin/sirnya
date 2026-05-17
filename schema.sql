CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       login VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE otp_config (
                            id SERIAL PRIMARY KEY CHECK (id = 1),
                            code_length INT NOT NULL DEFAULT 6,
                            lifetime_seconds INT NOT NULL DEFAULT 300,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO otp_config (id, code_length, lifetime_seconds) VALUES (1, 6, 300)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE otp_codes (
                           id SERIAL PRIMARY KEY,
                           user_id INT REFERENCES users(id) ON DELETE CASCADE,
                           operation_id VARCHAR(100),
                           code VARCHAR(20) NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                               CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           expires_at TIMESTAMP NOT NULL
);