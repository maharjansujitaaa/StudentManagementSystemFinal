package view;

import controller.StudentController;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import static view.StudentUIHelper.*;

/**
 * StudentProfilePage — three tabs: Profile | Student ID Card | Change Password
 */
public class StudentProfilePage {

    private final StudentController controller;

    public StudentProfilePage(StudentController controller) {
        this.controller = controller;
    }

    public Node build() {
        VBox page = new VBox(0);
        page.setPadding(new Insets(24, 32, 24, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        TabPane tabs = new TabPane(); tabs.setStyle("-fx-font-size:13px;");
        Tab t1 = profileTab(); t1.setClosable(false);
        Tab t2 = idCardTab();  t2.setClosable(false);
        Tab t3 = passwordTab();t3.setClosable(false);
        tabs.getTabs().addAll(t1, t2, t3);

        page.getChildren().add(tabs);
        ScrollPane scroll = bgScroll(); scroll.setContent(page);
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════════════
    // PROFILE TAB
    // ══════════════════════════════════════════════════════════════════════
    private Tab profileTab() {
        Tab tab = new Tab("👤  My Profile");
        VBox content = new VBox(20); content.setPadding(new Insets(20,0,0,0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Profile header card ─────────────────────────────────────────
        HBox headerCard = new HBox(24); headerCard.setPadding(new Insets(24));
        headerCard.setAlignment(Pos.CENTER_LEFT);
        headerCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");

        // Avatar circle with initial
        StackPane avatar = new StackPane();
        Circle ac = new Circle(45, Color.web(C_ACCENT, 0.15));
        Circle ac2 = new Circle(45); ac2.setStyle("-fx-fill:transparent; -fx-stroke:"+C_ACCENT+"; -fx-stroke-width:2;");
        String initial = controller.getStudentName().isEmpty() ? "S" : controller.getStudentName().substring(0,1).toUpperCase();
        Label initLbl = new Label(initial); initLbl.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";");
        avatar.getChildren().addAll(ac, initLbl);

        VBox nameBox = new VBox(6); HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameL   = lbl(controller.getStudentName(), "-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label classL  = lbl(controller.getStudentClass()+" • Roll No: "+controller.getRollNo(),
                             "-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        Label schoolL = lbl(controller.getSchoolName(), "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        HBox badges = new HBox(8,
            badge("Active", C_GREEN, "white"),
            badge(controller.getAcademicYear(), C_ACCENT, "white"),
            badge("Section: "+controller.getSection(), "#F1F5F9", C_TEXT)
        );
        nameBox.getChildren().addAll(nameL, classL, schoolL, badges);
        headerCard.getChildren().addAll(avatar, nameBox);

        // ── Info grids ──────────────────────────────────────────────────
        HBox gridsRow = new HBox(16);

        // Personal info
        VBox personalCard = infoCard("👤  Personal Information", new String[][]{
            {"Full Name",       controller.getStudentName()},
            {"Date of Birth",   controller.getDOB()},
            {"Gender",          controller.getGender()},
            {"Blood Group",     controller.getBloodGroup()},
            {"Phone",           controller.getPhone()},
            {"Email",           controller.getEmail()},
            {"Address",         controller.getAddress()},
        });
        HBox.setHgrow(personalCard, Priority.ALWAYS);

        // Academic info
        VBox academicCard = infoCard("🎓  Academic Information", new String[][]{
            {"Class",           controller.getStudentClass()},
            {"Section",         controller.getSection()},
            {"Roll No.",        controller.getRollNo()},
            {"Admission Date",  controller.getAdmissionDate()},
            {"Medium",          controller.getMediumOfStudy()},
            {"Academic Year",   controller.getAcademicYear()},
            {"School",          controller.getSchoolName()},
        });
        HBox.setHgrow(academicCard, Priority.ALWAYS);

        gridsRow.getChildren().addAll(personalCard, academicCard);

        // ── Parent/Guardian ─────────────────────────────────────────────
        VBox parentCard = infoCard("👨‍👩‍👧  Parent / Guardian Information", new String[][]{
            {"Parent/Guardian Name", controller.getParentName()},
            {"Contact Number",       controller.getParentPhone()},
            {"Relation",             "Father"},
            {"Email",                "parent@email.com"},
        });

        content.getChildren().addAll(headerCard, gridsRow, parentCard);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp);
        return tab;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ID CARD TAB
    // ══════════════════════════════════════════════════════════════════════
    private Tab idCardTab() {
        Tab tab = new Tab("🪪  Student ID Card");
        VBox content = new VBox(20); content.setPadding(new Insets(20,0,0,0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        Label hint = lbl("Your official student identity card is shown below.",
            "-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");

        // ID Card visual
        VBox idCard = buildIDCard();
        HBox centered = new HBox(idCard); centered.setAlignment(Pos.CENTER);

        // Print button
        Button printBtn = primaryBtn("🖨  Print / Download ID Card");
        printBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "ID Card sent to printer.\nStudent: "+controller.getStudentName()+"\nRoll No: "+controller.getRollNo(),
                ButtonType.OK);
            a.setHeaderText(null); a.showAndWait();
        });
        HBox printRow = new HBox(printBtn); printRow.setAlignment(Pos.CENTER);

        content.getChildren().addAll(hint, centered, printRow);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp);
        return tab;
    }

    private VBox buildIDCard() {
        VBox card = new VBox(0); card.setMaxWidth(360); card.setMinWidth(360);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:14; -fx-background-radius:14; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),16,0,0,4);");

        // Header strip
        VBox header = new VBox(4); header.setPadding(new Insets(16,20,16,20)); header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color:"+C_ACCENT+"; -fx-background-radius:14 14 0 0;");
        Label schoolLbl = lbl(controller.getSchoolName(), "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:white; -fx-wrap-text:true; -fx-text-alignment:CENTER;");
        schoolLbl.setWrapText(true); schoolLbl.setAlignment(Pos.CENTER);
        Label cardTypeLbl = lbl("STUDENT IDENTITY CARD", "-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.85); -fx-letter-spacing:2px;");
        header.getChildren().addAll(schoolLbl, cardTypeLbl);

        // Body
        HBox body = new HBox(16); body.setPadding(new Insets(20,20,16,20)); body.setAlignment(Pos.CENTER_LEFT);

        // Photo placeholder
        StackPane photo = new StackPane();
        Rectangle pr = new Rectangle(80,95); pr.setArcWidth(8); pr.setArcHeight(8);
        pr.setFill(Color.web(C_ACCENT, 0.1));
        pr.setStroke(Color.web(C_ACCENT, 0.3)); pr.setStrokeWidth(1.5);
        Label photoLbl = new Label(controller.getStudentName().isEmpty()?"S":controller.getStudentName().substring(0,1).toUpperCase());
        photoLbl.setStyle("-fx-font-size:32px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";");
        photo.getChildren().addAll(pr, photoLbl);

        VBox info = new VBox(6); HBox.setHgrow(info, Priority.ALWAYS);
        info.getChildren().addAll(
            lbl(controller.getStudentName(), "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            idRow("Class",   controller.getStudentClass()),
            idRow("Roll No", controller.getRollNo()),
            idRow("DOB",     controller.getDOB()),
            idRow("Blood",   controller.getBloodGroup())
        );
        body.getChildren().addAll(photo, info);

        // Footer strip
        HBox footer = new HBox(); footer.setPadding(new Insets(10,20,10,20)); footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0; -fx-background-radius:0 0 14 14;");
        Label yearLbl = lbl("Academic Year: "+controller.getAcademicYear(),
            "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+"; -fx-font-weight:bold;");
        HBox.setHgrow(yearLbl, Priority.ALWAYS);
        Label validLbl = lbl("Valid: "+controller.getAcademicYear(), "-fx-font-size:10px; -fx-text-fill:"+C_MUTED+";");
        footer.getChildren().addAll(yearLbl, validLbl);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    private HBox idRow(String key, String value) {
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT);
        Label k = lbl(key+":", "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+"; -fx-min-width:55px;");
        Label v = lbl(value,   "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        row.getChildren().addAll(k, v); return row;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CHANGE PASSWORD TAB
    // ══════════════════════════════════════════════════════════════════════
    private Tab passwordTab() {
        Tab tab = new Tab("🔒  Change Password");
        VBox content = new VBox(20); content.setPadding(new Insets(20,0,0,0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        VBox card = new VBox(20); card.setPadding(new Insets(28,32,28,32)); card.setMaxWidth(520);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");

        Label title = lbl("Change Your Password", "-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label sub   = lbl("Choose a strong password with at least 8 characters.",
                           "-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");

        PasswordField currentPF = styledPF("Enter current password");
        PasswordField newPF     = styledPF("At least 8 characters");
        PasswordField confirmPF = styledPF("Re-enter new password");

        // Strength indicator
        Label strengthLbl = lbl("", "-fx-font-size:12px; -fx-font-weight:bold;");
        ProgressBar strengthBar = new ProgressBar(0); strengthBar.setPrefWidth(Double.MAX_VALUE); strengthBar.setPrefHeight(6);
        strengthBar.setStyle("-fx-accent:"+C_MUTED+";");

        newPF.textProperty().addListener((obs, o, n) -> {
            int strength = 0;
            if (n.length() >= 8)                    strength++;
            if (n.matches(".*[A-Z].*"))             strength++;
            if (n.matches(".*[0-9].*"))             strength++;
            if (n.matches(".*[^A-Za-z0-9].*"))     strength++;
            double pct = strength / 4.0;
            strengthBar.setProgress(pct);
            String[] labels = {"","Weak","Fair","Good","Strong"};
            String[] colors = {C_MUTED, C_RED, C_ORANGE, C_BLUE, C_GREEN};
            strengthLbl.setText(strength>0 ? "Password strength: "+labels[strength] : "");
            strengthLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+colors[strength]+";");
            strengthBar.setStyle("-fx-accent:"+colors[strength]+";");
        });

        Button saveBtn = primaryBtn("Update Password");
        saveBtn.setPrefWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            if (currentPF.getText().isEmpty()) { showWarning("Please enter your current password."); return; }
            if (newPF.getText().length() < 8)  { showWarning("New password must be at least 8 characters."); return; }
            if (!newPF.getText().equals(confirmPF.getText())) { showWarning("Passwords do not match."); return; }
            currentPF.clear(); newPF.clear(); confirmPF.clear(); strengthBar.setProgress(0); strengthLbl.setText("");
            Alert a = new Alert(Alert.AlertType.INFORMATION,"✅ Password updated successfully!",ButtonType.OK);
            a.setHeaderText(null); a.showAndWait();
        });

        card.getChildren().addAll(
            title, sub,
            fieldGroup("Current Password", currentPF),
            new Separator(),
            fieldGroup("New Password", newPF),
            new VBox(4, strengthLbl, strengthBar),
            fieldGroup("Confirm New Password", confirmPF),
            saveBtn
        );

        HBox centered = new HBox(card); centered.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(centered);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp);
        return tab;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private VBox infoCard(String title, String[][] rows) {
        VBox card = new VBox(0); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label t = lbl(title, "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        card.getChildren().add(t);
        card.getChildren().add(new Separator());
        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(0);
        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        for (int i = 0; i < rows.length; i++) {
            Label key = lbl(rows[i][0], "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
            Label val = lbl(rows[i][1], "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            val.setWrapText(true);
            HBox row = new HBox(12, key, val); row.setPadding(new Insets(10,0,10,0));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(i<rows.length-1 ? "-fx-border-color:transparent transparent "+C_BORDER+" transparent; -fx-border-width:0 0 1 0;" : "");
            card.getChildren().add(row);
        }
        return card;
    }

    private VBox fieldGroup(String label, Control field) {
        VBox box = new VBox(6);
        box.getChildren().addAll(lbl(label, "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), field);
        return box;
    }

    private PasswordField styledPF(String prompt) {
        PasswordField pf = new PasswordField(); pf.setPromptText(prompt); pf.setPrefHeight(40);
        pf.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px;");
        return pf;
    }

    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK); a.setHeaderText(null); a.showAndWait();
    }
}