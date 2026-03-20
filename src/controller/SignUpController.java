
package controller;

import model.SignUpModel;
import view.LoginView;
import view.SignUpView;
import model.Role;


public class SignUpController {

    private final SignUpView view;
    private final SignUpModel model;

    public SignUpController(SignUpView view) {
        this.view  = view;
        this.model = new SignUpModel();
    }

    public void handleSignUp() {
        String fullName = view.getFullName();
        String email    = view.getEmail();
        String username = view.getUsername();
        String password = view.getPassword();
        String confirm  = view.getConfirmPassword();
        String role     = view.getRole();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            view.setStatus("All fields are required.", true); return;
        }
        if (!email.contains("@")) {
            view.setStatus("Please enter a valid email.", true); return;
        }
        if (username.length() < 3) {
            view.setStatus("Username must be at least 3 characters.", true); return;
        }
        if (password.length() < 6) {
            view.setStatus("Password must be at least 6 characters.", true); return;
        }
        if (!password.equals(confirm)) {
            view.setStatus("Passwords do not match.", true); return;
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            view.setStatus("Admin registration is not allowed.", true);
            return;
        }
        model.setFullName(fullName);
        model.setEmail(email);
        model.setUsername(username.toLowerCase());
        model.setPassword(password);
        model.setConfirmPassword(confirm);
        model.setSelectedRole(Role.valueOf(role.toUpperCase()));

        if (model.register()) {
        	view.setStatus("Signed up successfully!", false);
        } else {
            view.setStatus("Username or email already exists.", true);
        }
    }
}
