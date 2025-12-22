package com.example.taskmanager;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    public enum Priority {НИЗКИЙ, СРЕДНИЙ, ВЫСОКИЙ};
    public enum Status {ОЖИДАЕТ, ВЫПОЛНЯЕТСЯ, ЗАВЕРШЕНА};
    public Long id;
    public String name;
    public String description;
    public Priority priority;
    public Status status;
    public LocalDate date;
    public Long project_id;
    public LocalDateTime createdAt;
    public LocalDateTime completedAt;
    public Task() {}
    public Task(String name, Priority priority, Status status) {
        this.name = name;
        this.priority = priority;
        this.status = status;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getProjectId() { return project_id; }
    public void setProjectId(Long project_id) { this.project_id = project_id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}