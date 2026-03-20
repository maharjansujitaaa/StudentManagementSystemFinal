package view;

import controller.LoginController;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class LoginView {

    private Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;
    private ToggleGroup roleGroup;
    private Label statusLabel;

    public void show(Stage stage) {
        this.stage = stage;
        stage.setTitle("EduManage - Login");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);

        HBox root = new HBox();
        root.setPrefSize(900, 600);

        // Left panel
        StackPane leftPanel = buildLeftPanel();
        leftPanel.setPrefWidth(420);
        HBox.setHgrow(leftPanel, Priority.NEVER);

        // Right panel
        VBox rightPanel = buildRightPanel();
        rightPanel.setPrefWidth(480);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        root.getChildren().addAll(leftPanel, rightPanel);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
        	    getClass().getResource("/application/application.css").toExternalForm()
        	);
        stage.setScene(scene);
        stage.show();
    }

    private StackPane buildLeftPanel() {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: #DDE8F4;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        // Logo icon
        StackPane logoBox = new StackPane();
        Rectangle logoRect = new Rectangle(56, 56);
        logoRect.setArcWidth(14); logoRect.setArcHeight(14);
        logoRect.setFill(Color.web("#4F46E5"));
        Label logoLbl = new Label("E");
        logoLbl.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:white;");
        logoBox.getChildren().addAll(logoRect, logoLbl);

        Label appName = new Label("EduManage");
        appName.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");

        Label appSub = new Label("Student Management System");
        appSub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");

        // Illustration boxes
        VBox illustration = new VBox(12);
        illustration.setAlignment(Pos.CENTER);
        illustration.setPadding(new Insets(20, 0, 0, 0));

        String[] features = {"📊  Manage Students & Teachers", "📅  Track Attendance", "📝  Exams & Grades", "💰  Fee Management"};
        for (String f : features) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 16, 8, 16));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.6); -fx-background-radius:8;");
            Label lbl = new Label(f);
            lbl.setStyle("-fx-font-size:13px; -fx-text-fill:#1E293B;");
            row.getChildren().add(lbl);
            illustration.getChildren().add(row);
        }

        content.getChildren().addAll(logoBox, appName, appSub, illustration);
        pane.getChildren().add(content);
        return pane;
    }

    private VBox buildRightPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color:white;");
        panel.setPadding(new Insets(50, 60, 50, 60));
        panel.setSpacing(0);

        // Title
        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        Label subtitle = new Label("Sign in to your account");
        subtitle.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");
        VBox.setMargin(subtitle, new Insets(4, 0, 28, 0));

        // Username
        Label userLbl = new Label("Username");
        userLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(userLbl, new Insets(0, 0, 6, 0));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        styleField(usernameField);
        VBox.setMargin(usernameField, new Insets(0, 0, 16, 0));

        // Password
        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(passLbl, new Insets(0, 0, 6, 0));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        styleField(passwordField);
        VBox.setMargin(passwordField, new Insets(0, 0, 16, 0));

        // Role selection
        Label roleLbl = new Label("Role");
        roleLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(roleLbl, new Insets(0, 0, 8, 0));

        roleGroup = new ToggleGroup();
        HBox roleRow = new HBox(20);
        roleRow.setAlignment(Pos.CENTER_LEFT);
        String[] roles = {"Admin", "Teacher", "Student"};
        for (int i = 0; i < roles.length; i++) {
            RadioButton rb = new RadioButton(roles[i]);
            rb.setToggleGroup(roleGroup);
            rb.setUserData(roles[i].toUpperCase());
            rb.setStyle("-fx-font-size:13px; -fx-text-fill:#1E293B;");
            if (i == 2) rb.setSelected(true);
            roleRow.getChildren().add(rb);
        }
        VBox.setMargin(roleRow, new Insets(0, 0, 24, 0));

        // Status label
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#EF4444;");
        VBox.setMargin(statusLabel, new Insets(0, 0, 8, 0));

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);
        loginBtn.setStyle("-fx-background-color:#2563EB; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;");
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color:#1D4ED8; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;"));
        loginBtn.setOnMouseExited(e  -> loginBtn.setStyle("-fx-background-color:#2563EB; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;"));
        VBox.setMargin(loginBtn, new Insets(0, 0, 16, 0));

        // Sign up link
        HBox signupRow = new HBox(4);
        signupRow.setAlignment(Pos.CENTER);
        Label noAcc = new Label("Don't have an account?");
        noAcc.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");
        Hyperlink signupLink = new Hyperlink("Sign Up");
        signupLink.setStyle("-fx-font-size:13px; -fx-text-fill:#2563EB; -fx-border-color:transparent;");
        signupRow.getChildren().addAll(noAcc, signupLink);

        panel.getChildren().addAll(
            title, subtitle,
            userLbl, usernameField,
            passLbl, passwordField,
            roleLbl, roleRow,
            statusLabel, loginBtn, signupRow
        );

        // Wire up controller
        LoginController ctrl = new LoginController(this);
        loginBtn.setOnAction(e -> ctrl.handleLogin());
        passwordField.setOnAction(e -> ctrl.handleLogin());
        signupLink.setOnAction(e -> {
            SignUpView sv = new SignUpView();
            sv.show(stage);
        });

        return panel;
    }

    private void styleField(TextField tf) {
        tf.setPrefHeight(42);
        tf.setStyle("-fx-background-color:#F9FAFB; -fx-border-color:#D1D5DB; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-padding:0 12 0 12;");
        tf.focusedProperty().addListener((obs, o, n) -> {
            if (n) tf.setStyle("-fx-background-color:white; -fx-border-color:#2563EB; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-padding:0 12 0 12;");
            else   tf.setStyle("-fx-background-color:#F9FAFB; -fx-border-color:#D1D5DB; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-padding:0 12 0 12;");
        });
    }

    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return passwordField.getText().trim(); }
    public String getRole()     { return roleGroup.getSelectedToggle() != null ? roleGroup.getSelectedToggle().getUserData().toString() : "STUDENT"; }
    public void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + (error ? "#EF4444" : "#22C55E") + ";");
    }
    public Stage getStage() { return stage; }
}
