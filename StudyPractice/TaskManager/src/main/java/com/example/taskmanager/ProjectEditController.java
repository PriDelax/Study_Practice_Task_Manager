package com.example.taskmanager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;

public class ProjectEditController {
    @FXML private Label headerLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Button color1, color2, color3, color4, color5, color6, color7;

    private Project project;
    private String selectedColor = "#3498db";
    private final ProjectController projectController = new ProjectController();
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            headerLabel.setText("Редактировать проект");
            nameField.setText(project.getName());
            descriptionArea.setText(project.getDescription());
            selectedColor = project.getColour();
        }
    }

    @FXML
    private void colorSelect(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        selectedColor = (String) btn.getUserData();
        Button[] buttons = {color1, color2, color3, color4, color5, color6, color7};
        for (Button b : buttons) {
            if (b != null) {
                String color = (String) b.getUserData();
                if (color.startsWith("linear-gradient")) {
                    b.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
                } else {
                    b.setStyle("-fx-background-color: " + color + ";");
                }
            }
        }
        if (btn.getUserData() instanceof String gradient && gradient.startsWith("linear-gradient")) {
            btn.setStyle("-fx-background-color: " + gradient + "; -fx-background-radius: 4; -fx-border-color: black; -fx-border-width: 2px;");
        } else {
            btn.setStyle("-fx-background-color: " + selectedColor + "; -fx-border-color: black; -fx-border-width: 2px;");
        }
    }

    @FXML
    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Название проекта не может быть пустым.");
            return;
        }
        try {
            if (project == null) {
                project = new Project(name, descriptionArea.getText());
            } else {
                project.setName(name);
                project.setDescription(descriptionArea.getText());
            }
            project.setColour(selectedColor);
            projectController.save(project);
            dialogStage.close();
        } catch (SQLException e) {
            showError("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
