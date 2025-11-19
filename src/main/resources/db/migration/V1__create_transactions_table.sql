-- =====================================================
-- V1: Create Transactions Table
-- =====================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    account_iban VARCHAR(24) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fee NUMERIC(19, 4) DEFAULT 0,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_amount_positive CHECK (amount >= 0),
    CONSTRAINT chk_fee_positive CHECK (fee >= 0),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SETTLED', 'CANCELLED', 'FAILED')),
    CONSTRAINT chk_type CHECK (type IN ('PAYMENT', 'REFUND', 'TRANSFER', 'DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT chk_currency CHECK (currency IN ('EUR', 'USD', 'GBP'))
);

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Unique index on reference (already created by UNIQUE constraint)
CREATE UNIQUE INDEX IF NOT EXISTS idx_reference ON transactions(reference);

-- Index on account_iban (frequently queried)
CREATE INDEX IF NOT EXISTS idx_account_iban ON transactions(account_iban);

-- Index on status (for filtering by status)
CREATE INDEX IF NOT EXISTS idx_status ON transactions(status);

-- Index on transaction_date (for date range queries)
CREATE INDEX IF NOT EXISTS idx_date ON transactions(transaction_date);

-- Composite index for account + status queries
CREATE INDEX IF NOT EXISTS idx_account_status ON transactions(account_iban, status);

-- Index on created_at for recent transactions
CREATE INDEX IF NOT EXISTS idx_created_at ON transactions(created_at DESC);

-- =====================================================
-- Comments
-- =====================================================

COMMENT ON TABLE transactions IS 'Payment transactions table';
COMMENT ON COLUMN transactions.reference IS 'Unique transaction reference';
COMMENT ON COLUMN transactions.account_iban IS 'Customer account IBAN';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount';
COMMENT ON COLUMN transactions.currency IS 'Transaction currency (EUR, USD, GBP)';
COMMENT ON COLUMN transactions.fee IS 'Transaction fee';
COMMENT ON COLUMN transactions.status IS 'Transaction status (PENDING, SETTLED, CANCELLED, FAILED)';
COMMENT ON COLUMN transactions.type IS 'Transaction type (PAYMENT, REFUND, TRANSFER, DEPOSIT, WITHDRAWAL)';
COMMENT ON COLUMN transactions.channel IS 'Transaction channel (WEB, MOBILE, API, etc.)';
COMMENT ON COLUMN transactions.transaction_date IS 'Date of transaction';
COMMENT ON COLUMN transactions.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN transactions.updated_at IS 'Record update timestamp';
