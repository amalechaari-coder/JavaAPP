CREATE DATABASE IF NOT EXISTS queue_jee_app;
USE queue_jee_app;

CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cabinets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_id INT NULL,
    cabinet_id INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    ticket_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL,
    FOREIGN KEY (cabinet_id) REFERENCES cabinets(id) ON DELETE CASCADE
);

INSERT IGNORE INTO cabinets (name) VALUES
('Cabinet General'),
('Cabinet Dentaire'),
('Cabinet Pediatrique');
