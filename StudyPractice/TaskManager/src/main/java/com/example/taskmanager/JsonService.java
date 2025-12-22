package com.example.taskmanager;

import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonService {
    public static class ExportData {
        public List<Project> projects;
        public List<Task> tasks;
        public ExportData(){}
        public ExportData(List<Project> projects, List<Task> tasks){
            this.projects = projects;
            this.tasks = tasks;
        }
        public List<Project> getProjects(){return projects;}
        public List<Task> getTasks(){return tasks;}
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    src == null ? null : new JsonPrimitive(src.format(DATETIME_FORMATTER))
            )
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                if (json == null || json.isJsonNull()) return null;
                return LocalDateTime.parse(json.getAsString(), DATETIME_FORMATTER);
            })
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    src == null ? null : new JsonPrimitive(src.format(DATE_FORMATTER))
            )
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
                if (json == null || json.isJsonNull()) return null;
                return LocalDate.parse(json.getAsString(), DATE_FORMATTER);
            })
            .create();

    public static void exportToJSON(String filePath) throws Exception {
        try {
            ProjectController projectController = new ProjectController();
            TaskController taskController = TaskController.getInstance();
            List<Project> projects = projectController.findAll();
            List<Task> tasks = taskController.findAll();
            ExportData date = new ExportData(projects, tasks);
            try (FileWriter fileWriter = new FileWriter(filePath)){
                GSON.toJson(date, fileWriter);
            }
            System.out.println("Экспорт успешно завершён в: " + filePath);
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте:");
            e.printStackTrace();
            throw e;
        }
    }
    public static void importFromJSON(String filePath) throws Exception {
        Map<Long, Long> projectIdMap = new HashMap<>();
        Database.initializeDatabase();
        JsonService.ExportData exportData;
        try (FileReader fileReader = new FileReader(filePath)){
            exportData = GSON.fromJson(fileReader, JsonService.ExportData.class);
            System.out.println("Результат десериализации: " + exportData);
            if (exportData != null) {
                System.out.println("projects == null? " + (exportData.projects == null));
                System.out.println("tasks == null? " + (exportData.tasks == null));
            }
        }
        clearDatabase();
        ProjectController projectController = new ProjectController();
        if (exportData.getProjects() != null){
            for (Project project : exportData.getProjects()){
                Long oldId = project.getId();
                project.setId(null);
                projectController.save(project);
                projectIdMap.put(oldId, project.getId());
            }
        }
        TaskController taskController = TaskController.getInstance();
        if (exportData.getTasks() != null){
            for (Task task : exportData.getTasks()){
                task.setId(null);
                if (task.getProjectId() != null){
                    task.setProjectId(projectIdMap.get(task.getProjectId()));
                }
                taskController.save(task);
            }
        }
    }
    private static void clearDatabase() throws Exception{
        try (Connection connection = Database.getConnection()){
            connection.createStatement().execute("DELETE from tasks");
            connection.createStatement().execute("DELETE from projects");
        }
    }
}
