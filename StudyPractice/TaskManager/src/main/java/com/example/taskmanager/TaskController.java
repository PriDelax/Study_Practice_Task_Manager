package com.example.taskmanager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskController {
    private static TaskController taskController;
    private TaskController(){}

    public static TaskController getInstance(){
        if (taskController == null){
            taskController = new TaskController();
        }
        return taskController;
    }
    public void save(Task task) throws SQLException {
        String sql = task.getId() == null
                ? "INSERT INTO tasks (name, description, priority, status, date, project_id, completed_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE tasks SET name = ?, description = ?, priority = ?, status = ?, date = ?, project_id = ?, completed_at = ? WHERE id = ?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setString(4, task.getStatus().name());
            stmt.setDate(5, task.getDate() == null ? null : Date.valueOf(task.getDate()));
            stmt.setObject(6, task.getProjectId(), Types.INTEGER);
            LocalDateTime completed_at = task.getCompletedAt();
            stmt.setTimestamp(7, completed_at == null ? null : Timestamp.valueOf(completed_at));
            if (task.getId() != null) {
                stmt.setLong(8, task.getId());
            }
            stmt.executeUpdate();
            if (task.getId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) task.setId(rs.getLong(1));
                }
                try (PreparedStatement statement = conn.prepareStatement("SELECT created_at FROM tasks WHERE id = ?")){
                    statement.setLong(1, task.getId());
                    try (ResultSet resultSet = statement.executeQuery()){
                        if (resultSet.next()){
                            Timestamp timestamp = resultSet.getTimestamp("created_at");
                            task.setCreatedAt(timestamp.toLocalDateTime());
                        }
                    }
                }
            }
        }
    }

    public List<Task> findByProjectId(Long projectId) throws SQLException {
        String sql = projectId == null ? "SELECT * FROM tasks" : "SELECT * FROM tasks WHERE project_id = ?";
        return executeQuery(sql, projectId);
    }

    private List<Task> executeQuery(String sql, Object param) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getLong("id"));
                    t.setName(rs.getString("name"));
                    t.setDescription(rs.getString("description"));
                    String priority = rs.getString("priority").trim().toUpperCase();
                    t.setPriority(Task.Priority.valueOf(priority));
                    String status = rs.getString("status").trim().toUpperCase();
                    t.setStatus(Task.Status.valueOf(status));
                    Date due = rs.getDate("date");
                    t.setDate(due == null ? null : due.toLocalDate());
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    t.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
                    Timestamp completedAt = rs.getTimestamp("completed_at");
                    t.setCompletedAt(completedAt == null ? null : completedAt.toLocalDateTime());
                    t.setProjectId(rs.getLong("project_id"));
                    tasks.add(t);
                }
            }
        }
        return tasks;
    }
    public void delete(Long id) throws SQLException{
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement("DELETE from tasks WHERE id = ?")) {
            stmt.setLong(1,id);
            stmt.executeUpdate();
        }
    }
    public List<Task> findAll() throws SQLException{
        return executeQuery("SELECT * FROM tasks", null);
    }
}
