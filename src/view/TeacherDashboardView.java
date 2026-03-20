package view;

import controller.TeacherController;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.TeacherModel;

import static view.TeacherUIHelper.*;

/**
 * TeacherDashboardView — slim coordinator.
 * Builds sidebar + top bar, then delegates each page to its own class.
 *
 * Page classes:
 *   TeacherDashboardPage      — home with stat cards + hover chart + events
 *   TeacherStudentsPage       — full CRUD (view/add/edit/delete), classes 1-10
 *   TeacherAttendancePage     — persistent marking, On Leave, classes 1-10
 *   TeacherClassesPage        — add/edit/delete/view class cards
 *   TeacherExamsPage          — exams table + grades table
 *   TeacherNotificationsPage  — create/read/delete notifications
 *   TeacherReportsPage        — summary cards + bar charts + tables
 */
public class TeacherDashboardView {

    // ── State ──────────────────────────────────────────────────────────────
    private Stage             stage;
    private TeacherController controller;
    private String            username;
    private StackPane         contentArea;
    private Label             pageTitleLbl, pageSubLbl;
    private HBox              topActionArea;
    private Button            activeSidebarBtn;

    // ── Page instances (created once, reused) ─────────────────────────────
    private TeacherDashboardPage     dashPage;
    private TeacherStudentsPage      studentsPage;
    private TeacherAttendancePage    attendancePage;
    private TeacherClassesPage       classesPage;
    private TeacherExamsPage         examsPage;
    private TeacherNotificationsPage notifsPage;
    private TeacherReportsPage       reportsPage;

    // ── Entry point ────────────────────────────────────────────────────────
    public void show(Stage stage, String username) {
        this.stage    = stage;
        this.username = username;

        TeacherModel model = new TeacherModel(username);
        this.controller = new TeacherController(model, this);

        // Instantiate all page classes
        dashPage      = new TeacherDashboardPage(controller, username);
        studentsPage  = new TeacherStudentsPage(controller, stage);
        attendancePage= new TeacherAttendancePage(controller, stage);
        classesPage   = new TeacherClassesPage(controller, stage);
        examsPage     = new TeacherExamsPage(controller, stage);
        notifsPage    = new TeacherNotificationsPage(controller, stage);
        reportsPage   = new TeacherReportsPage(controller);

        // Wire refresh callbacks
        studentsPage.setOnRefresh(this::showStudents);
        classesPage.setOnRefresh(this::showClasses);

        stage.setTitle("EduManage - Teacher Dashboard");
        stage.setMinWidth(1000); stage.setMinHeight(650);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setLeft(buildSidebar());
        root.setCenter(buildMainArea());

        javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        stage.setX(screen.getMinX()); stage.setY(screen.getMinY());
        stage.setWidth(screen.getWidth()); stage.setHeight(screen.getHeight());
        stage.setMaximized(true);

        stage.setScene(new Scene(root));
        stage.show();
        Platform.runLater(this::showDashboard);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(250);
        sb.setStyle("-fx-background-color:" + C_WHITE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 1 0 0;");

        // Logo
        HBox logo = new HBox(12); logo.setPadding(new Insets(18,16,18,16)); logo.setAlignment(Pos.CENTER_LEFT);
        StackPane li = new StackPane();
        Rectangle lr = new Rectangle(42,42); lr.setArcWidth(10); lr.setArcHeight(10); lr.setFill(Color.web(C_ACCENT));
        Label le = new Label("🎓"); le.setStyle("-fx-font-size:20px;");
        li.getChildren().addAll(lr, le);
        VBox lt = new VBox(2,
            lbl("EduManage",         "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            lbl("Student Management","-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"));
        logo.getChildren().addAll(li, lt);

        Separator sep1 = new Separator(); sep1.setStyle("-fx-background-color:"+C_BORDER+";");

        // Nav
        String[][] nav = {
            {"📊","Dashboard",    "dashboard"},
            {"👥","Students",     "students"},
            {"✅","Attendance",   "attendance"},
            {"📖","Classes",      "classes"},
            {"📝","Exams & Grades","exams"},
            {"🔔","Notifications","notifications"},
            {"📈","Reports",      "reports"}
        };
        VBox navBox = new VBox(2); navBox.setPadding(new Insets(8,0,8,0)); VBox.setVgrow(navBox, Priority.ALWAYS);
        for (String[] item : nav) {
            Button btn = makeSidebarBtn(item[0], item[1], item[2]);
            navBox.getChildren().add(btn);
            if (item[2].equals("dashboard")) activeSidebarBtn = btn;
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Separator sep2 = new Separator(); sep2.setStyle("-fx-background-color:"+C_BORDER+";");

        // User footer
        HBox footer = new HBox(10); footer.setPadding(new Insets(14,16,10,16)); footer.setAlignment(Pos.CENTER_LEFT);
        StackPane av = new StackPane();
        av.getChildren().addAll(new Circle(20,Color.web("#DBEAFE")),
            lbl(username.isEmpty()?"T":username.substring(0,1).toUpperCase(),
                "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";"));
        String displayName = username.contains(" ") ? username : "Dr. " + cap(username);
        VBox ui = new VBox(2,
            lbl(displayName,"  -fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            lbl("teacher","-fx-background-color:#F1F5F9; -fx-text-fill:"+C_MUTED+"; -fx-font-size:11px; -fx-background-radius:4; -fx-padding:1 6 1 6;"));
        footer.getChildren().addAll(av, ui);

        Button logoutBtn = new Button("  🚪  Logout");
        logoutBtn.setPrefWidth(Double.MAX_VALUE);
        String ls="-fx-background-color:transparent; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:10 16 12 16;";
        String lh="-fx-background-color:#FEE2E2; -fx-text-fill:"+C_RED+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:10 16 12 16;";
        logoutBtn.setStyle(ls);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(lh));
        logoutBtn.setOnMouseExited(e  -> logoutBtn.setStyle(ls));
        logoutBtn.setOnAction(e -> controller.handleLogout());

        sb.getChildren().addAll(logo, sep1, navBox, spacer, sep2, footer, logoutBtn);
        return sb;
    }

    private Button makeSidebarBtn(String icon, String label, String action) {
        Button btn = new Button(icon + "  " + label);
        btn.setPrefWidth(Double.MAX_VALUE); btn.setPrefHeight(46);
        String idle   = "-fx-background-color:transparent; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        String hover  = "-fx-background-color:#F8FAFF; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        String active = "-fx-background-color:"+C_SEL+"; -fx-text-fill:"+C_ACCENT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        btn.setStyle(idle);
        btn.setOnMouseEntered(e -> { if(btn!=activeSidebarBtn) btn.setStyle(hover); });
        btn.setOnMouseExited(e  -> { if(btn!=activeSidebarBtn) btn.setStyle(idle);  });
        btn.setOnAction(e -> {
            if(activeSidebarBtn!=null) activeSidebarBtn.setStyle(idle);
            btn.setStyle(active); activeSidebarBtn=btn;
            controller.navigate(action);
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAIN AREA (top bar + content pane)
    // ══════════════════════════════════════════════════════════════════════
    private BorderPane buildMainArea() {
        BorderPane main = new BorderPane();
        main.setStyle("-fx-background-color:" + C_BG + ";");

        HBox topBar = new HBox(); topBar.setPadding(new Insets(16,28,16,28)); topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        VBox titleBox = new VBox(4); HBox.setHgrow(titleBox, Priority.ALWAYS);
        pageTitleLbl = new Label("Teacher Dashboard");
        pageTitleLbl.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        pageSubLbl = new Label("Welcome back, "+username+"!");
        pageSubLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        titleBox.getChildren().addAll(pageTitleLbl, pageSubLbl);
        topActionArea = new HBox(8); topActionArea.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().addAll(titleBox, topActionArea);

        contentArea = new StackPane(); contentArea.setStyle("-fx-background-color:"+C_BG+";");
        main.setTop(topBar); main.setCenter(contentArea);
        return main;
    }

    // ── Navigation helpers ─────────────────────────────────────────────────
    private void setPage(String title, String sub) {
        pageTitleLbl.setText(title); pageSubLbl.setText(sub); topActionArea.getChildren().clear();
    }

    private void setContent(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void addTopBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color:#374151; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;"));
        btn.setOnAction(e -> action.run()); topActionArea.getChildren().add(btn);
    }

    private void addTopSecondaryBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color:white; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color:#F8F9FA; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color:white; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;"));
        btn.setOnAction(e -> action.run()); topActionArea.getChildren().add(btn);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PAGE NAVIGATION  — each method delegates to its page class
    // ══════════════════════════════════════════════════════════════════════
    public void showDashboard() {
        setPage("Teacher Dashboard", "Welcome back, " + username + "!");
        setContent(dashPage.build());
    }

    public void showStudents() {
        setPage("Students", "Manage your student records");
        addTopBtn("+ Add Student", studentsPage::showAddDialog);
        setContent(studentsPage.build());
    }

    public void showAttendance() {
        setPage("Attendance", "Mark and manage student attendance");
        setContent(attendancePage.build());
    }

    public void showClasses() {
        setPage("Classes", "Your assigned classes and schedules");
        addTopBtn("+ Add Class", classesPage::showAddDialog);
        setContent(classesPage.build());
    }

    public void showExams() {
        setPage("Exams & Grades", "Manage exams and student grades");
        addTopSecondaryBtn("⬇ Report Card", () -> examsPage.showReportCardQuickDialog());
        addTopBtn("+ Add Exam", () -> examsPage.showAddExamDialog(this::showExams));
        setContent(examsPage.build());
    }

    public void showNotifications() {
        long unread = controller.getUnreadCount();
        setPage("Notifications", "Manage announcements and reminders (" + unread + " unread)");
        notifsPage.setSubtitleLbl(pageSubLbl);
        addTopBtn("+ Create Notification", notifsPage::showCreateDialog);
        setContent(notifsPage.build());
    }

    public void showReports() {
        setPage("Reports & Analytics", "View performance reports and analytics");
        addTopBtn("⬇ Export Report", () -> showAlert("Report exported!"));
        // Rebuild each visit so live charts reflect latest attendance/grades
        reportsPage = new TeacherReportsPage(controller);
        setContent(reportsPage.build());
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    public void doLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.YES,ButtonType.NO); a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.NO)==ButtonType.YES) new LoginView().show(stage);
    }
}
