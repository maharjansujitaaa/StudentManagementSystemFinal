package view;

import javafx.stage.Stage;

/**
 * TeacherDashboard — thin wrapper so LoginController signature stays unchanged.
 * LoginController calls: new TeacherDashboard().show(stage, username)
 */
public class TeacherDashboard {
    public void show(Stage stage, String username) {
        new TeacherDashboardView().show(stage, username);
    }
}
