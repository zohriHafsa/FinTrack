CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS insights (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('SPENDING_ANOMALY', 'TREND', 'FORECAST')),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_insights_company_id ON insights(company_id);
CREATE INDEX idx_insights_company_valid_until ON insights(company_id, valid_until);
CREATE INDEX idx_insights_generated_at ON insights(generated_at);
CREATE INDEX idx_insights_type ON insights(type);