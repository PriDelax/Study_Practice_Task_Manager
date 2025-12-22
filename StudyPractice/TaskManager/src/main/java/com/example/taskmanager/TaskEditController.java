package com.example.taskmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TaskEditController {
    @FXML private Label headerLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Project> projectComboBox;
    @FXML private RadioButton lowRadio, mediumRadio, highRadio;
    @FXML public ToggleGroup priorityGroup;
    @FXML private DatePicker datePicker;
    @FXML private CheckBox doneCheckBox;
    @FXML private Button deleteButton;

    private Task task;
    private final TaskController taskController = TaskController.getInstance();
    private final ProjectController projectController = new ProjectController();
    private Stage dialogStage;

    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }
    public void setTask(Task task){
        this.task = task;
        if (task != null){
            headerLabel.setText("Редактирование задачи");
            nameField.setText(task.getName());
            descriptionArea.setText(task.getDescription());
            datePicker.setValue(task.getDate());
            doneCheckBox.setSelected(task.getStatus() == Task.Status.ЗАВЕРШЕНА);
            switch (task.getPriority()){
                case НИЗКИЙ -> lowRadio.setSelected(true);
                case СРЕДНИЙ -> mediumRadio.setSelected(true);
                case ВЫСОКИЙ -> highRadio.setSelected(true);
            }
            deleteButton.setVisible(true);
        } else {
            mediumRadio.setSelected(true);
            doneCheckBox.setSelected(false);
        }
        loadProjects();
    }
    private void loadProjects(){
        try {
            List<Project> projects = projectController.findAll();
            projectComboBox.getItems().clear();
            projectComboBox.getItems().addAll(projects);
            if (task != null && task.getProjectId() != null){
                Project selected = projects.stream().filter(p -> p.getId().equals(task.getProjectId())).findFirst().orElse(null);
                projectComboBox.setValue(selected);
            } else {
                projectComboBox.getSelectionModel().selectFirst();
            }
            projectComboBox.setCellFactory(lv -> new ListCell<Project>() {
                @Override
                protected void updateItem(Project project, boolean empty) {
                    super.updateItem(project, empty);
                    if (empty || project == null) {
                        setText(null);
                    } else {
                        setText(project.getName());
                    }
                }
            });
            projectComboBox.setButtonCell(new ListCell<Project>() {
                @Override
                protected void updateItem(Project project, boolean empty) {
                    super.updateItem(project, empty);
                    if (empty || project == null) {
                        setText(null);
                    } else {
                        setText(project.getName());
                    }
                }
            });
        } catch (SQLException sqlException){
            showError("Ошибка загрузки проектов: " + sqlException.getMessage());
        }
    }

    @FXML
    private void save(){
        String name = nameField.getText().trim();
        if (name.isEmpty()){
            showError("Название задачи не может быть пустым");
            return;
        }
        try {
            if (task == null){
                task = new Task();
            }
            task.setName(name);
            task.setDescription(descriptionArea.getText());
            task.setDate(datePicker.getValue());
            if (lowRadio.isSelected()) task.setPriority(Task.Priority.НИЗКИЙ);
            else if (mediumRadio.isSelected()) task.setPriority(Task.Priority.СРЕДНИЙ);
            else task.setPriority(Task.Priority.ВЫСОКИЙ);
            task.setStatus(doneCheckBox.isSelected() ? Task.Status.ЗАВЕРШЕНА : Task.Status.ВЫПОЛНЯЕТСЯ);
            if (doneCheckBox.isSelected()) {
                task.setStatus(Task.Status.ЗАВЕРШЕНА);
                if (task.getCompletedAt() == null) {
                    task.setCompletedAt(LocalDateTime.now());
                }
            } else {
                task.setStatus(Task.Status.ВЫПОЛНЯЕТСЯ);
                task.setCompletedAt(null);
            }
            Project selectedProject = projectComboBox.getValue();
            task.setProjectId(selectedProject != null ? selectedProject.getId() : null);
            taskController.save(task);
            dialogStage.close();
        } catch (SQLException sqlException) {
            showError("Ошибка сохранения задачи: " + sqlException.getMessage());
        }
    }
    @FXML
    private void delete(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить задачу " + task.getName() + "?");
        if (alert.showAndWait().get() == ButtonType.OK){
            try {
                taskController.delete(task.getId());
                dialogStage.close();
            } catch (SQLException sqlException){
                showError("Ошибка удаления: " + sqlException.getMessage());
            }
        }
    }

    @FXML
    private void cancel(){
        dialogStage.close();
    }

    private void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
