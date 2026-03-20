
package view;

import controller.SignUpController;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class SignUpView {

    private Stage stage;
    private TextField fullNameField, emailField, usernameField;
    private PasswordField passwordField, confirmPasswordField;
    private ComboBox<String> roleBox;
    private Label statusLabel;

    public void show(Stage stage) {
        this.stage = stage;
        stage.setTitle("EduManage - Sign Up");

        HBox root = new HBox();
        root.setPrefWidth(900);
        root.setPrefHeight(650);

        // Left panel — fixed width
        StackPane left = new StackPane();
        left.setPrefWidth(380);
        left.setMinWidth(380);
        left.setMaxWidth(380);
        left.setStyle("-fx-background-color:#4F46E5;");

        VBox leftContent = new VBox(16);
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setPadding(new Insets(40));

        Label icon = new Label("🎓");
        icon.setStyle("-fx-font-size:60px;");

        Label title = new Label("Join EduManage");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:white;");

        Label sub = new Label("Create your account and start\nmanaging your school today.");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:rgba(255,255,255,0.8); -fx-text-alignment:center;");
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        leftContent.getChildren().addAll(icon, title, sub);
        left.getChildren().add(leftContent);

        // Right panel — takes remaining space
        VBox form = buildForm();
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white; -fx-background:white;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(left, scroll);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(650);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    private VBox buildForm() {
        VBox form = new VBox(0);
        form.setStyle("-fx-background-color:white;");
        form.setPadding(new Insets(40, 60, 40, 60));
        form.setFillWidth(true);

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");

        Label sub = new Label("Fill in the details to get started");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");
        VBox.setMargin(sub, new Insets(4, 0, 24, 0));

        form.getChildren().addAll(title, sub);

        fullNameField        = addField(form, "Full Name *", "Enter your full name");
        emailField           = addField(form, "Email *", "Enter your email");
        usernameField        = addField(form, "Username *", "Choose a username");
        passwordField        = addPasswordField(form, "Password *", "Create a password");
        confirmPasswordField = addPasswordField(form, "Confirm Password *", "Confirm your password");

        // Role combo
        Label roleLbl = new Label("Role *");
        roleLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(roleLbl, new Insets(8, 0, 6, 0));

        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("STUDENT", "TEACHER");
        roleBox.setValue("STUDENT");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(42);
        roleBox.setStyle("-fx-font-size:13px;");
        VBox.setMargin(roleBox, new Insets(0, 0, 20, 0));

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#EF4444;");
        VBox.setMargin(statusLabel, new Insets(0, 0, 8, 0));

        Button signupBtn = new Button("Create Account");
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setPrefHeight(44);
        signupBtn.setStyle(
            "-fx-background-color:#4F46E5; -fx-text-fill:white; -fx-font-size:14px;" +
            "-fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;"
        );
        VBox.setMargin(signupBtn, new Insets(0, 0, 16, 0));

        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        Label already = new Label("Already have an account?");
        already.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");
        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setStyle("-fx-font-size:13px; -fx-text-fill:#4F46E5; -fx-border-color:transparent;");
        loginRow.getChildren().addAll(already, loginLink);

        form.getChildren().addAll(roleLbl, roleBox, statusLabel, signupBtn, loginRow);

        SignUpController ctrl = new SignUpController(this);
        signupBtn.setOnAction(e -> ctrl.handleSignUp());
        loginLink.setOnAction(e -> { LoginView lv = new LoginView(); lv.show(stage); });

        return form;
    }

    private TextField addField(VBox form, String label, String prompt) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(lbl, new Insets(8, 0, 6, 0));

        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(42);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(
            "-fx-background-color:#F9FAFB; -fx-border-color:#D1D5DB;" +
            "-fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-font-size:13px; -fx-padding:0 12 0 12;"
        );
        VBox.setMargin(tf, new Insets(0, 0, 4, 0));
        form.getChildren().addAll(lbl, tf);
        return tf;
    }

    private PasswordField addPasswordField(VBox form, String label, String prompt) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        VBox.setMargin(lbl, new Insets(8, 0, 6, 0));

        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(42);
        pf.setMaxWidth(Double.MAX_VALUE);
        pf.setStyle(
            "-fx-background-color:#F9FAFB; -fx-border-color:#D1D5DB;" +
            "-fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-font-size:13px; -fx-padding:0 12 0 12;"
        );
        VBox.setMargin(pf, new Insets(0, 0, 4, 0));
        form.getChildren().addAll(lbl, pf);
        return pf;
    }

    public String getFullName()        { return fullNameField.getText().trim(); }
    public String getEmail()           { return emailField.getText().trim(); }
    public String getUsername()        { return usernameField.getText().trim(); }
    public String getPassword()        { return passwordField.getText().trim(); }
    public String getConfirmPassword() { return confirmPasswordField.getText().trim(); }
    public String getRole()            { return roleBox.getValue(); }

    public void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + (error ? "#EF4444" : "#22C55E") + ";");
    }

    public Stage getStage() { return stage; }
}