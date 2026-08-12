CREATE TABLE IF NOT EXISTS insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMP NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_insights_company_id ON insights(company_id);
CREATE INDEX IF NOT EXISTS idx_insights_type ON insights(type);
CREATE INDEX IF NOT EXISTS idx_insights_valid_until ON insights(valid_until);