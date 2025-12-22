package com.example.taskmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    static {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            String createProjectsTable = """
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    colour TEXT DEFAULT '#3498db'
                );
                """;
            String createTasksTable = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    priority TEXT NOT NULL CHECK(priority IN ('НИЗКИЙ','СРЕДНИЙ','ВЫСОКИЙ')),
                    status TEXT NOT NULL CHECK(status IN ('ОЖИДАЕТ','ВЫПОЛНЯЕТСЯ','ЗАВЕРШЕНА')),
                    date DATE,
                    project_id INTEGER,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    completed_at DATETIME,
                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
                );
                """;

            stmt.execute(createProjectsTable);
            stmt.execute(createTasksTable);
            System.out.println("База данных инициализирована");
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}
