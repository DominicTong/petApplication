package com.petapp.model;

import java.sql.Timestamp;

/**
 * Represents a single to-do task belonging to the player.
 */
public class Task {

    private final int       id;
    private final String    title;
    private       boolean   completed;
    private final Timestamp createdAt;

    public Task(int id, String title, boolean completed, Timestamp createdAt) {
        this.id        = id;
        this.title     = title;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int       getId()                      { return id; }
    public String    getTitle()                   { return title; }
    public boolean   isCompleted()                { return completed; }
    public void      setCompleted(boolean v)      { this.completed = v; }
    public Timestamp getCreatedAt()               { return createdAt; }
}
