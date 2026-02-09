CREATE DATABASE IF NOT EXISTS f1_fantasy_db;
USE f1_fantasy_db;

-- Users table (For authentication)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Constructors (Teams)
CREATE TABLE teams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_location VARCHAR(100),
    logo_url VARCHAR(255)
);

-- Drivers (Linked to Teams)
CREATE TABLE drivers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    permanent_number INT,
    country VARCHAR(50),
    points DECIMAL(5, 1) DEFAULT 0.0,
    price DECIMAL(10, 2) DEFAULT 10.00, -- For Fantasy Logic later
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
);

-- Races
CREATE TABLE races (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    date DATE,
    circuit VARCHAR(100),
    is_completed BOOLEAN DEFAULT FALSE
);