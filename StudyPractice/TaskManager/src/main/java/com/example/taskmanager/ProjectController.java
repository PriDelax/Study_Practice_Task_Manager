package com.example.taskmanager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectController {
    public void save(Project project) throws SQLException {
        String sql = project.getId() == null || project.getId() <= 0 ? "INSERT INTO projects (name, description, colour) VALUES (?, ?, ?)" : "UPDATE projects SET name = ?, description = ?, colour = ? WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getColour());
            if (project.getId() != null && project.getId() > 0) {
                stmt.setLong(4, project.getId());
            }
            stmt.executeUpdate();
            if (project.getId() == null) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) project.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM projects")) {
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setColour(rs.getString("colour"));
                projects.add(p);
            }
        }
        return projects;
    }
    public void delete(Long id) throws SQLException{
        try (Connection connection = Database.getConnection(); PreparedStatement stmt = connection.prepareStatement("DELETE FROM projects WHERE id = ?")){
            stmt.setLong(1,id);
            stmt.executeUpdate();
        }
    }
}
