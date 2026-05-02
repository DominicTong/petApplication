-- ============================================================
--  Pet Garden App — Database Schema
--  Run this file once in MySQL to set up the database:
--    mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS petapp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE petapp;

-- ─── USER DATA ──────────────────────────────────────────────
-- Single-row table; id=1 is always the local player.
CREATE TABLE IF NOT EXISTS user_data (
    id                    INT PRIMARY KEY DEFAULT 1,
    coins                 INT  NOT NULL DEFAULT 0,
    total_tasks_completed INT  NOT NULL DEFAULT 0
);

-- Ensure the player row exists with a starting balance
INSERT IGNORE INTO user_data (id, coins, total_tasks_completed)
VALUES (1, 0, 0);

-- ─── TASKS ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tasks (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP    NULL
);

-- ─── ANIMAL POOL ────────────────────────────────────────────
-- All possible animals the player can collect.
-- drop_weight controls how likely each animal is to appear;
--   higher weight = more common.
CREATE TABLE IF NOT EXISTS animals (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    emoji        VARCHAR(10)  NOT NULL,
    rarity       ENUM('COMMON','UNCOMMON','RARE','LEGENDARY') NOT NULL DEFAULT 'COMMON',
    description  VARCHAR(255) NOT NULL,
    drop_weight  INT          NOT NULL DEFAULT 100
);

-- ─── PLAYER COLLECTION ──────────────────────────────────────
-- Each row = one copy of an animal owned by the player.
-- Multiple rows for the same animal = the player has duplicates (×N).
CREATE TABLE IF NOT EXISTS user_collection (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    animal_id   INT       NOT NULL,
    obtained_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animals(id) ON DELETE CASCADE
);

-- ─── SEED: ANIMALS ──────────────────────────────────────────
-- Only insert if the table is empty to avoid duplicates on re-run.
INSERT INTO animals (name, emoji, rarity, description, drop_weight)
SELECT * FROM (VALUES
    -- COMMON  (weight 80–100)
    ROW('Cat',       '🐱', 'COMMON',   'Independent, mysterious, and always judging you.',        100),
    ROW('Dog',       '🐶', 'COMMON',   'Loyal, energetic, and always happy to see you.',          100),
    ROW('Rabbit',    '🐰', 'COMMON',   'Fluffy, quick, and impossibly soft.',                      90),
    ROW('Hamster',   '🐹', 'COMMON',   'Tiny but packs an enormous personality.',                  85),
    ROW('Duck',      '🦆', 'COMMON',   'Loves puddles, quacks loudly, has opinions.',              80),
    ROW('Chick',     '🐥', 'COMMON',   'Freshly hatched and full of wonder.',                      80),

    -- UNCOMMON (weight 40–60)
    ROW('Frog',      '🐸', 'UNCOMMON', 'Expert jumper. Stares into the void thoughtfully.',        60),
    ROW('Hedgehog',  '🦔', 'UNCOMMON', 'Prickly on the outside, warm on the inside.',              55),
    ROW('Penguin',   '🐧', 'UNCOMMON', 'Always dressed for a formal occasion.',                    50),
    ROW('Fox',       '🦊', 'UNCOMMON', 'Cunning, charming, and suspiciously clever.',              45),
    ROW('Koala',     '🐨', 'UNCOMMON', 'Sleeps 22 hours a day. Honestly inspirational.',           40),

    -- RARE     (weight 10–20)
    ROW('Owl',       '🦉', 'RARE',     'Wise beyond their years. Reads more than you.',            20),
    ROW('Deer',      '🦌', 'RARE',     'Graceful, gentle, and a forest spirit.',                   16),
    ROW('Peacock',   '🦚', 'RARE',     'Strikingly beautiful and well aware of it.',               14),
    ROW('Axolotl',   '🐟', 'RARE',     'The smiling wonder that can regenerate any limb.',         10),

    -- LEGENDARY (weight 1–3)
    ROW('Dragon',    '🐉', 'LEGENDARY','Ancient, magnificent, and slightly terrifying.',             3),
    ROW('Unicorn',   '🦄', 'LEGENDARY','Pure magic in physical form.',                              2),
    ROW('Phoenix',   '🔥', 'LEGENDARY','Born from the flames. Cannot be stopped.',                  1)
) AS tmp(name, emoji, rarity, description, drop_weight)
WHERE NOT EXISTS (SELECT 1 FROM animals LIMIT 1);
