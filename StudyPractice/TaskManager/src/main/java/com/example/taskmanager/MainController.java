package com.example.taskmanager;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;

public class MainController {
    @FXML private ListView<Project> projectListView;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Boolean> statusColumn;
    @FXML private TableColumn<Task, String> nameColumn;
    @FXML private TableColumn<Task, Task.Priority> priorityColumn;
    @FXML private TableColumn<Task, String> DateColumn;
    @FXML private TableColumn<Task, String> projectColumn;
    @FXML private Label tasksNameLabel;
    @FXML private Label statsLabel;
    @FXML private Button themeToggle;
    @FXML private Button showAllTasksButton;
    private boolean showAllTasksMode = true;

    private final ProjectController projectController = new ProjectController();
    private final TaskController taskController = TaskController.getInstance();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private Long selectedProjectId = null;
    private final Preferences preferences = Preferences.userNodeForPackage(MainController.class);
    private Scene scene;
    private boolean isDarkTheme = false;

    @FXML
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        String theme = isDarkTheme ? "dark" : "light";
        preferences.put("theme", theme);
        applyTheme(theme);
        themeToggle.setText("Темная тема: " + (isDarkTheme ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setScene(Scene scene){
        this.scene = scene;
        String savedTheme = preferences.get("theme", "light");
        isDarkTheme = "dark".equals(savedTheme);
        applyTheme(savedTheme);
        themeToggle.setText("Темная тема: " + (isDarkTheme ? "ВКЛ" : "ВЫКЛ"));
    }

    private Task.Priority priorityFromText(String text) {
        if (text == null){
            return Task.Priority.НИЗКИЙ;
        }
        return switch (text) {
            case "Высокий" -> Task.Priority.ВЫСОКИЙ;
            case "Средний" -> Task.Priority.СРЕДНИЙ;
            case "Низкий" -> Task.Priority.НИЗКИЙ;
            default -> Task.Priority.НИЗКИЙ;
        };
    }

    @FXML
    public void initialize() {
        loadProjects();
        loadTasks();
        projectListView.setItems(projects);
        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                setText(empty ? null : project.getName());
            }
        });
        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, proj) -> {
            if (proj != null && !showAllTasksMode) {
                selectedProjectId = proj.getId();
                tasksNameLabel.setText("Задачи: " + proj.getName());
                loadTasks();
            }
        });
        taskTable.setItems(tasks);
        statusColumn.setCellValueFactory(data ->
                new SimpleBooleanProperty(data.getValue().getStatus() == Task.Status.ЗАВЕРШЕНА)
        );
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName() != null ? data.getValue().getName() : "")
        );
        priorityColumn.setCellValueFactory(data ->
                new SimpleObjectProperty(data.getValue().getPriority())
        );
        priorityColumn.setCellFactory(col -> new TableCell<Task, Task.Priority>() {
            @Override
            protected void updateItem(Task.Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = switch (priority) {
                        case ВЫСОКИЙ -> "Высокий";
                        case СРЕДНИЙ -> "Средний";
                        case НИЗКИЙ -> "Низкий";
                    };
                    setText(text);
                    String color = switch (priority) {
                        case ВЫСОКИЙ -> "red";
                        case СРЕДНИЙ -> "orange";
                        case НИЗКИЙ -> "green";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        DateColumn.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getDate();
            String display = (date != null) ? date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "—";
            return new SimpleStringProperty(display);
        });
        projectColumn.setCellValueFactory(data -> {
            Long projectId = data.getValue().getProjectId();
            if (projectId == null) {
                return new SimpleStringProperty("—");
            }
            String projectName = projects.stream().filter(p -> p.getId().equals(projectId)).map(Project::getName).findFirst().orElse("?");
            return new SimpleStringProperty(projectName);
        });
        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    setText(project.getName());
                    String color = project.getColour();
                    String textcolor = isColorDark(color) ? "white" : "black";
                    setStyle("-fx-background-color: " + color + ";" + "-fx-text-fill: " + textcolor + ";" + "-fx-padding: 8px;" +
                            "-fx-border-color: black;" +
                            "-fx-border-width: 0 0 1 0;"
                    );

                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem("Редактировать");
                    MenuItem deleteItem = new MenuItem("Удалить");
                    editItem.setOnAction(event -> {
                        openProjectEdit(project);
                    });
                    deleteItem.setOnAction(event -> {
                        deleteProject(project);
                    });

                    contextMenu.getItems().addAll(editItem, deleteItem);
                    setContextMenu(contextMenu);
                }
            }
        });
        taskTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editTask(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadProjects() {
        try {
            List<Project> all = projectController.findAll();
            projects.setAll(all);
        } catch (SQLException e) {
            showError("Ошибка загрузки проектов", e);
        }
    }

    private boolean isColorDark(String hexColor) {
        try {
            String color = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            // Яркость по формуле восприятия
            double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;
            return brightness < 128; // тёмный, если яркость < 128
        } catch (Exception e) {
            return false; // по умолчанию — светлый текст
        }
    }

    private void loadTasks() {
        try {
            List<Task> all = taskController.findByProjectId(selectedProjectId);
            tasks.setAll(all);
            updateStats();
        } catch (SQLException e) {
            showError("Ошибка загрузки задач", e);
        }
    }

    private void updateStats() {
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == Task.Status.ЗАВЕРШЕНА).count();
        statsLabel.setText(String.format("Всего задач: %d | Завершено: %d", total, done));
    }

    @FXML
    private void createTask() {
        openTaskCreate(null);
    }

    @FXML
    private void editTask(Task task){
        openTaskCreate(task);
    }

    private void openTaskCreate(Task task){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("task-create-and-edit.fxml"));
            VBox page = fxmlLoader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(task == null ? "Новая задача" : "Редактирование задачи");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            Scene dialogScene = new Scene(page);
            MainController.applyCurrentTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            TaskEditController taskEditController = fxmlLoader.getController();
            taskEditController.setDialogStage(dialogStage);
            taskEditController.setTask(task);
            dialogStage.showAndWait();
            loadTasks();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    private void openProjectEdit(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("project-create-and-edit.fxml"));
            VBox page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактировать проект");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            Scene dialogScene = new Scene(page);
            MainController.applyCurrentTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            ProjectEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProject(project);
            dialogStage.showAndWait();
            loadProjects();
            if (selectedProjectId != null && selectedProjectId.equals(project.getId())) {
                loadTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @FXML
    private void Settings() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
            VBox page = fxmlLoader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Настройки");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            Scene dialogScene = new Scene(page);
            MainController.applyCurrentTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            SettingsController settingsController = fxmlLoader.getController();
            settingsController.setDialogStage(dialogStage);
            settingsController.setMainController(this);
            dialogStage.showAndWait();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void applyTheme(String theme) {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        prefs.put("theme", theme);
        if (scene != null) {
            URL css = getClass().getResource(theme + ".css");
            if (css != null) {
                scene.getStylesheets().setAll(css.toExternalForm());
            }
        }
    }

    @FXML
    private void Statistics() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("statistics.fxml"));
            VBox page = fxmlLoader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Статистика");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            Scene dialogScene = new Scene(page);
            MainController.applyCurrentTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            StatisticsController statisticsController = fxmlLoader.getController();
            statisticsController.setDialogStage(dialogStage);
            dialogStage.showAndWait();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    @FXML
    private void showAllTasks() {
        showAllTasksMode = !showAllTasksMode;
        if (showAllTasksMode){
            selectedProjectId = null;
            tasksNameLabel.setText("Задачи: Все проекты");
            showAllTasksButton.setText("Для конкретного проекта");
        } else {
            Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
            if (selectedProject != null){
                selectedProjectId = selectedProject.getId();
                tasksNameLabel.setText("Задачи" + selectedProject.getName());
            } else {
                if (projects.isEmpty()){
                    Project first = projects.getFirst();
                    projectListView.getSelectionModel().select(first);
                    selectedProjectId = first.getId();
                    tasksNameLabel.setText("Задачи" + first.getName());
                } else {
                    selectedProjectId = null;
                    tasksNameLabel.setText("Задачи:Проектов пока нет");
                }
            }
            showAllTasksButton.setText("Все задачи");
        }
        loadTasks();
    }

    @FXML
    private void createProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("project-create-and-edit.fxml"));
            VBox page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Новый проект");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            Scene dialogScene = new Scene(page);
            MainController.applyCurrentTheme(dialogScene);
            dialogStage.setScene(dialogScene);
            ProjectEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProject(null);
            dialogStage.showAndWait();
            loadProjects();
            if (selectedProjectId == null) {
                loadTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProject(Project project){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить проект <<" + project.getName() + ">>? \nВсе связанные задачи также будут удалены!");
        if (alert.showAndWait().get() == ButtonType.OK){
            try {
                projectController.delete(project.getId());
                loadProjects();
                loadTasks();
                if (selectedProjectId != null && selectedProjectId.equals(project.getId())){
                    selectedProjectId = null;
                    tasksNameLabel.setText("Задачи:Все проекты");
                    loadTasks();
                }
            } catch (SQLException sqlException){
                showError("Ошибка удаления проекта: " + sqlException.getMessage(), sqlException);
            }
        }
    }

    public void applyFontSize(String fontSize){
        if (scene == null) return;
        String fontSizeValue = switch (fontSize){
            case "small" -> "small-font";
            case "large" -> "large-font";
            default -> "medium-font";
        };
        scene.getRoot().getStyleClass().removeIf(cls ->
                cls.equals("small-font") || cls.equals("medium-font") || cls.equals("large-font"));
        scene.getRoot().getStyleClass().add(fontSizeValue);
    }

    @FXML
    private void close(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход из приложения");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите выйти из приложения?");
        if (alert.showAndWait().get() == ButtonType.OK){
            Stage stage = (Stage) taskTable.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void exportData(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить в JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON","*.json"));
        File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
        if (file != null){
            try {
                JsonService.exportToJSON(file.getAbsolutePath());
                new Alert(Alert.AlertType.CONFIRMATION, "Данные успешно сохранены!").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage()).showAndWait();
            }
        }
    }

    private static void applyCurrentTheme(Scene scene) {
        if (scene == null) return;

        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String theme = prefs.get("theme", "light");

        URL css = MainController.class.getResource(theme + ".css");
        if (css != null) {
            scene.getStylesheets().setAll(css.toExternalForm());
        }
    }

    @FXML
    public void importData(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить из JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON","*.json"));
        File file = fileChooser.showOpenDialog(taskTable.getScene().getWindow());
        if (file != null){
            try {
                JsonService.importFromJSON(file.getAbsolutePath());
                loadProjects();
                loadTasks();
                System.out.println("После импорта: проектов = " + projects.size() + ", задач = " + tasks.size());
                new Alert(Alert.AlertType.CONFIRMATION, "Данные успешно импортированы!").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка импорта: " + e.getMessage()).showAndWait();
            }
        }
    }

    private void showError(String message, Exception e) {
        new Alert(Alert.AlertType.ERROR, message + "\n" + e.getMessage()).showAndWait();
    }
}