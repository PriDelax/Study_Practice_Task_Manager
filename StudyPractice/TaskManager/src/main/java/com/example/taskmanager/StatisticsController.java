package com.example.taskmanager;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class StatisticsController {
    @FXML private PieChart priorityPieChart;
    @FXML private Label totalLabel, doneLabel, overdueLabel, averageTimeLabel;
    private Stage dialogStage;
    private final TaskController taskController = TaskController.getInstance();

    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }
    @FXML
    private void initialize(){
        try {
            List<Task> allTasks = taskController.findAll();
            long low = allTasks.stream().filter(t -> t.getPriority() == Task.Priority.НИЗКИЙ).count();
            long medium = allTasks.stream().filter(t -> t.getPriority() == Task.Priority.СРЕДНИЙ).count();
            long high = allTasks.stream().filter(t -> t.getPriority() == Task.Priority.ВЫСОКИЙ).count();
            priorityPieChart.getData().clear();
            if (low > 0) priorityPieChart.getData().add(new PieChart.Data("Низкий", low));
            if (medium > 0) priorityPieChart.getData().add(new PieChart.Data("Средний", medium));
            if (high > 0) priorityPieChart.getData().add(new PieChart.Data("Высокий", high));
            totalLabel.setText("Общее количество задач: " + allTasks.size());
            long done = allTasks.stream().filter(t -> t.getStatus() == Task.Status.ЗАВЕРШЕНА).count();
            doneLabel.setText("Завершено задач: " + done);
            long overdue = allTasks.stream().filter(t -> t.getStatus() != Task.Status.ЗАВЕРШЕНА && t.getDate() != null && t.getDate().isBefore(LocalDate.now())).count();
            overdueLabel.setText("Просрочено: " + overdue);
            List<Task> completedTasks = allTasks.stream()
                    .filter(t -> t.getStatus() == Task.Status.ЗАВЕРШЕНА)
                    .filter(t -> t.getCreatedAt() != null && t.getCompletedAt() != null)
                    .filter(t -> !t.getCompletedAt().isBefore(t.getCreatedAt()))
                    .filter(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toHours() < 24 * 7)
                    .toList();
            if (completedTasks.isEmpty()) {
                averageTimeLabel.setText("Среднее время выполнения: —");
            } else {
                long totalMinutes = completedTasks.stream()
                        .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                        .sum();
                double avgMinutes = (double) totalMinutes / completedTasks.size();
                long hours = (long) (avgMinutes / 60);
                long minutes = (long) (avgMinutes % 60);
                averageTimeLabel.setText( String.format("Среднее время выполнения: %d ч %d мин", hours, minutes));
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @FXML
    private void close(){
        if (dialogStage != null){
            dialogStage.close();
        }
    }
}
