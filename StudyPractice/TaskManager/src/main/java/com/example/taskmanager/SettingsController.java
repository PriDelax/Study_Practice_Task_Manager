package com.example.taskmanager;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class SettingsController {
    @FXML public TextField dbPathField;
    @FXML private RadioButton smallFont, mediumFont, largeFont;
    private Stage dialogStage;
    private final Preferences preferences = Preferences.userNodeForPackage(SettingsController.class);
    private MainController mainController;

    public void setMainController(MainController mainController){
        this.mainController = mainController;
    }

    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }

    @FXML
    public void initialize(){
        String dbPath = Paths.get(System.getProperty("user.home"), ".taskmanager", "tasks.db").toString();
        dbPathField.setText(dbPath);
        String savedFont = preferences.get("fontSize", "mediumFont");
        switch (savedFont){
            case "small" -> smallFont.setSelected(true);
            case "large" -> largeFont.setSelected(true);
            default -> mediumFont.setSelected(true);
        }
    }
    @FXML
    private void openDbFolder(){
        String dbPath = dbPathField.getText();
        File folder = new File(dbPath).getParentFile();
        if (folder.exists()){
            try {
                java.awt.Desktop.getDesktop().open(folder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void reset(){
        preferences.put("fontSize", "medium");
        applySettings();
    }

    @FXML
    private void apply(){
        String fontSize = getSelectedFontSize();
        preferences.put("fontSize", fontSize);
        applySettings();
    }

    @FXML
    private void close(){
        dialogStage.close();
    }

    private String getSelectedFontSize(){
        if (smallFont.isSelected()) return "small";
        if (largeFont.isSelected()) return "large";
        return "medium";
    }

    @FXML
    private void exportData() {
        if (mainController != null) {
            mainController.exportData();
        }
    }

    @FXML
    private void importData() {
        if (mainController != null) {
            mainController.importData();
        }
    }

    private void applySettings(){
        if (mainController != null){
            String fontSize = preferences.get("fontSize", "medium");
            mainController.applyFontSize(fontSize);
        }
    }
}
