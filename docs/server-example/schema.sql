-- Database Schema untuk IntiKasir Activation System
-- PostgreSQL

-- Table: Serial Numbers
CREATE TABLE IF NOT EXISTS serial_numbers (
    id SERIAL PRIMARY KEY,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    tier VARCHAR(50) NOT NULL DEFAULT 'basic', -- trial, basic, pro, enterprise
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NULL, -- NULL = no expiry for SN generation
    used_at TIMESTAMP NULL,
    used_by_device VARCHAR(255) NULL,
    notes TEXT NULL
);

CREATE INDEX idx_serial_number ON serial_numbers(serial_number);
CREATE INDEX idx_is_used ON serial_numbers(is_used);
CREATE INDEX idx_tier ON serial_numbers(tier);

-- Table: Activations
CREATE TABLE IF NOT EXISTS activations (
    id SERIAL PRIMARY KEY,
    serial_number VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    expiry_timestamp BIGINT NOT NULL,
    activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    deactivated_at TIMESTAMP NULL,
    deactivation_reason TEXT NULL,
    ip_address VARCHAR(50) NULL,

    FOREIGN KEY (serial_number) REFERENCES serial_numbers(serial_number)
);

CREATE INDEX idx_device_id ON activations(device_id);
CREATE INDEX idx_serial_number_activation ON activations(serial_number);
CREATE INDEX idx_is_active ON activations(is_active);
CREATE INDEX idx_activated_at ON activations(activated_at);

-- Table: Audit Logs
CREATE TABLE IF NOT EXISTS activation_audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL, -- ACTIVATE, DEACTIVATE, VERIFY
    serial_number VARCHAR(255) NULL,
    device_id VARCHAR(255) NULL,
    success BOOLEAN NOT NULL,
    message TEXT NULL,
    ip_address VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_action ON activation_audit_logs(action);
CREATE INDEX idx_audit_created_at ON activation_audit_logs(created_at);
CREATE INDEX idx_audit_success ON activation_audit_logs(success);

-- Sample data untuk testing
INSERT INTO serial_numbers (serial_number, tier, notes) VALUES
('SN-TRIAL-DEMO-001', 'trial', 'Demo trial license'),
('SN-BASIC-DEMO-001', 'basic', 'Demo basic license'),
('SN-PRO-DEMO-001', 'pro', 'Demo pro license'),
('SN-ENTERPRISE-DEMO-001', 'enterprise', 'Demo enterprise license');

-- View: Active Activations
CREATE OR REPLACE VIEW active_activations AS
SELECT
    a.id,
    a.serial_number,
    a.device_id,
    a.tier,
    a.activated_at,
    to_timestamp(a.expiry_timestamp / 1000) as expiry_date,
    EXTRACT(DAY FROM (to_timestamp(a.expiry_timestamp / 1000) - NOW())) as days_remaining,
    a.ip_address
FROM activations a
WHERE a.is_active = true
ORDER BY a.activated_at DESC;

-- View: Available Serial Numbers
CREATE OR REPLACE VIEW available_serial_numbers AS
SELECT
    sn.serial_number,
    sn.tier,
    sn.created_at,
    sn.expiry_date,
    CASE
        WHEN sn.expiry_date IS NULL THEN true
        WHEN sn.expiry_date > NOW() THEN true
        ELSE false
    END as is_valid
FROM serial_numbers sn
WHERE sn.is_used = false
ORDER BY sn.created_at DESC;

-- View: Activation Statistics
CREATE OR REPLACE VIEW activation_statistics AS
SELECT
    COUNT(*) as total_activations,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_count,
    COUNT(CASE WHEN is_active = false THEN 1 END) as inactive_count,
    COUNT(CASE WHEN tier = 'trial' THEN 1 END) as trial_count,
    COUNT(CASE WHEN tier = 'basic' THEN 1 END) as basic_count,
    COUNT(CASE WHEN tier = 'pro' THEN 1 END) as pro_count,
    COUNT(CASE WHEN tier = 'enterprise' THEN 1 END) as enterprise_count
FROM activations;

-- Function: Clean expired activations (untuk cron job)
CREATE OR REPLACE FUNCTION cleanup_expired_activations()
RETURNS void AS $$
BEGIN
    UPDATE activations
    SET is_active = false,
        deactivated_at = NOW(),
        deactivation_reason = 'Expired automatically'
    WHERE is_active = true
    AND to_timestamp(expiry_timestamp / 1000) < NOW();
END;
$$ LANGUAGE plpgsql;

-- Usage: SELECT cleanup_expired_activations();

COMMENT ON TABLE serial_numbers IS 'Tabel untuk menyimpan Serial Number yang di-generate';
COMMENT ON TABLE activations IS 'Tabel untuk menyimpan aktivasi device';
COMMENT ON TABLE activation_audit_logs IS 'Tabel untuk audit log semua aktivitas aktivasi';
COMMENT ON VIEW active_activations IS 'View untuk melihat aktivasi yang masih aktif';
COMMENT ON VIEW available_serial_numbers IS 'View untuk melihat Serial Number yang masih tersedia';
COMMENT ON VIEW activation_statistics IS 'View untuk statistik aktivasi';

