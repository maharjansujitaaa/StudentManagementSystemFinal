package view;

import javafx.stage.Stage;

/**
 * StudentDashboard — thin wrapper so LoginController signature stays unchanged.
 * LoginController calls: new StudentDashboard().show(stage, username)
 */
public class StudentDashboard {
    public void show(Stage stage, String username) {
        new StudentDashboardView().show(stage, username);
    }
}
