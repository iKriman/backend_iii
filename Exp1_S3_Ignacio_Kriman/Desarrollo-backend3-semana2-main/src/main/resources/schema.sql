CREATE TABLE IF NOT EXISTS daily_transaction_report (
    id VARCHAR(50) PRIMARY KEY,
    cuenta VARCHAR(50),
    monto DECIMAL(10, 2),
    tipo VARCHAR(20),
    estado VARCHAR(20),
    fecha DATE
);