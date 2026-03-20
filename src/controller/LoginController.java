package controller;

import model.LoginModel;
import model.Role;
import view.AdminDashboard;
import view.LoginView;
import view.StudentDashboard;
import view.TeacherDashboard;
import javafx.application.Platform;

public class LoginController {

    private final LoginView view;
    private final LoginModel model;

    public LoginController(LoginView view) {
        this.view = view;
        this.model = new LoginModel();
    }

    public void handleLogin() {
        String username = view.getUsername();
        String password = view.getPassword();
        String roleStr  = view.getRole();

        if (username.isEmpty() || password.isEmpty()) {
            view.setStatus("Please enter username and password.", true);
            return;
        }

        model.setUsername(username);
        model.setPassword(password);
        try {
        	model.setSelectedRole(Role.valueOf(roleStr.toUpperCase()));
        } catch (Exception e) {
        	model.setSelectedRole(Role.STUDENT);
        }

        if (model.authenticate()) {
            view.setStatus("Login successful!", false);
            Platform.runLater(() -> {
            	Role role = model.getSelectedRole();
                if (role == Role.ADMIN) {
                    new AdminDashboard().show(view.getStage(), username);
                } else if (role == Role.TEACHER) {
                    new TeacherDashboard().show(view.getStage(), username);
                } else {
                    new StudentDashboard().show(view.getStage(), username);
                }
            });
        } else {
            view.setStatus("Invalid credentials. Please try again.", true);
        }
    }
}
