package view;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;

import static view.UIHelper.*;

/**
 * AdminDashboard — coordinator only.
 * All page logic is in separate page classes that share AppState.
 */
public class AdminDashboard {

    // ── Shared state (data store) ─────────────────────────────────────────
    private final AppState state = new AppState();

    // ── Page instances ───────────────────────────────────────────────────
    private DashboardPage      dashPage;
    private StudentsPage       studentsPage;
    private TeachersPage       teachersPage;
    private AttendancePage     attendancePage;
    private ClassesPage        classesPage;
    private ExamsPage          examsPage;
    private FeesPage           feesPage;
    private NotificationsPage  notifsPage;
    private ReportsPage        reportsPage;

    // ── UI references ────────────────────────────────────────────────────
    private Stage       stage;
    private String      loggedInUser;
    private BorderPane  root;
    private StackPane   contentArea;
    private Label       pageTitleLbl, pageSubLbl;
    private HBox        topActionArea;
    private Button      activeSidebarBtn;

    // ── Entry point ──────────────────────────────────────────────────────
    public void show(Stage stage, String username) {
        this.stage        = stage;
        this.loggedInUser = username;

        // Instantiate pages — all share the same AppState
        dashPage       = new DashboardPage(state);
        studentsPage   = new StudentsPage(state, stage);
        teachersPage   = new TeachersPage(state, stage);
        attendancePage = new AttendancePage(state, stage);
        classesPage    = new ClassesPage(state, stage);
        examsPage      = new ExamsPage(state, stage);
        feesPage       = new FeesPage(state, stage);
        notifsPage     = new NotificationsPage(state, stage);
        reportsPage    = new ReportsPage(state, stage);

        stage.setTitle("EduManage - Admin Dashboard");
        stage.setMinWidth(1000); stage.setMinHeight(600);

        root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");
        root.setLeft(buildSidebar());
        root.setCenter(buildMain());

        javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        stage.setX(screen.getMinX()); stage.setY(screen.getMinY());
        stage.setWidth(screen.getWidth()); stage.setHeight(screen.getHeight());
        stage.setMaximized(true);

        stage.setScene(new Scene(root));
        stage.show();
        Platform.runLater(() -> navigateTo("dashboard"));
    }

    // ── Sidebar ──────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color:" + C_WHITE + "; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 1 0 0;");

        // Logo
        HBox logo = new HBox(12); logo.setPadding(new Insets(18,16,18,16)); logo.setAlignment(Pos.CENTER_LEFT);
        StackPane logoIcon = new StackPane();
        Rectangle lr = new Rectangle(42,42); lr.setArcWidth(10); lr.setArcHeight(10); lr.setFill(Color.web(C_ACCENT));
        Label le = new Label("E"); le.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:white;");
        logoIcon.getChildren().addAll(lr, le);
        VBox logoText = new VBox(2);
        Label appName = new Label("EduManage"); appName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label appSub  = new Label("Student Management"); appSub.setStyle("-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";");
        logoText.getChildren().addAll(appName, appSub);
        logo.getChildren().addAll(logoIcon, logoText);

        Separator sep1 = new Separator(); sep1.setStyle("-fx-background-color:"+C_BORDER+";");

        String[][] navItems = {
            {"📊  Dashboard","dashboard"},
            {"👥  Students","students"},
            {"👩‍🏫  Teachers","teachers"},
            {"✅  Attendance","attendance"},
            {"🏫  Classes","classes"},
            {"📝  Exams & Grades","exams"},
            {"💰  Fees","fees"},
            {"🔔  Notifications","notifs"},
            {"📈  Reports","reports"}
        };

        VBox navBox = new VBox(0);
        VBox.setVgrow(navBox, Priority.ALWAYS);
        for (String[] item : navItems) {
            Button btn = makeSidebarBtn(item[0], item[1]);
            navBox.getChildren().add(btn);
            if (item[1].equals("dashboard")) activeSidebarBtn = btn;
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Separator sep2 = new Separator(); sep2.setStyle("-fx-background-color:"+C_BORDER+";");

        HBox footer = new HBox(10); footer.setPadding(new Insets(12,16,8,16)); footer.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.getChildren().addAll(new Circle(18, Color.web("#DBEAFE")),
            lbl(loggedInUser.substring(0,1).toUpperCase(), "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";"));
        VBox userInfo = new VBox(2, lbl(loggedInUser,"-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), lbl("Admin","-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"));
        footer.getChildren().addAll(avatar, userInfo);

        Button logoutBtn = new Button("  🚪  Logout");
        logoutBtn.setPrefWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:10 16 10 16;");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(logo, sep1, navBox, spacer, sep2, footer, logoutBtn);
        return sidebar;
    }

    private Button makeSidebarBtn(String label, String action) {
        Button btn = new Button(label);
        btn.setPrefWidth(Double.MAX_VALUE); btn.setPrefHeight(44);
        String idle   = "-fx-background-color:transparent; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        String hover  = "-fx-background-color:#F8FAFF; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        String active = "-fx-background-color:"+C_SEL+"; -fx-text-fill:"+C_ACCENT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 20;";
        btn.setStyle(idle);
        btn.setOnMouseEntered(e -> { if (btn != activeSidebarBtn) btn.setStyle(hover); });
        btn.setOnMouseExited(e  -> { if (btn != activeSidebarBtn) btn.setStyle(idle);  });
        btn.setOnAction(e -> {
            if (activeSidebarBtn != null) activeSidebarBtn.setStyle(idle);
            btn.setStyle(active); activeSidebarBtn = btn;
            navigateTo(action);
        });
        return btn;
    }

    // ── Main area ────────────────────────────────────────────────────────
    private BorderPane buildMain() {
        BorderPane main = new BorderPane();
        main.setStyle("-fx-background-color:" + C_BG + ";");

        HBox topBar = new HBox(); topBar.setPadding(new Insets(16,28,16,28)); topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");

        VBox titleBox = new VBox(4); HBox.setHgrow(titleBox, Priority.ALWAYS);
        pageTitleLbl = new Label("Dashboard"); pageTitleLbl.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        pageSubLbl   = new Label("Welcome back!"); pageSubLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        titleBox.getChildren().addAll(pageTitleLbl, pageSubLbl);

        topActionArea = new HBox(8); topActionArea.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().addAll(titleBox, topActionArea);

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:" + C_BG + ";");

        main.setTop(topBar); main.setCenter(contentArea);
        return main;
    }

    // ── Navigation ───────────────────────────────────────────────────────
    private void navigateTo(String action) {
        topActionArea.getChildren().clear();
        switch (action) {
            case "dashboard":
                pageTitleLbl.setText("Dashboard");
                pageSubLbl.setText("Welcome back, " + loggedInUser + "!");
                // Dashboard re-builds entirely so charts use latest data
                setContent(dashPage.build());
                break;

            case "students":
                pageTitleLbl.setText("Students");
                pageSubLbl.setText("Manage student records and profiles");
                addTopBtn("+ Add Student", () -> studentsPage.showAddDialog(() -> navigateTo("students")));
                setContent(studentsPage.build(() -> navigateTo("students")));
                break;

            case "teachers":
                pageTitleLbl.setText("Teachers");
                pageSubLbl.setText("Manage teacher records and profiles");
                addTopBtn("+ Add Teacher", () -> teachersPage.showAddDialog(() -> navigateTo("teachers")));
                setContent(teachersPage.build());
                break;

            case "attendance":
                pageTitleLbl.setText("Attendance");
                pageSubLbl.setText("View attendance reports and mark teacher attendance");
                setContent(attendancePage.build());
                break;

            case "classes":
                pageTitleLbl.setText("Classes & Courses");
                pageSubLbl.setText("Manage classes, sections, and subjects");
                addTopBtn("+ Add Class", () -> classesPage.showAddDialog(() -> navigateTo("classes")));
                setContent(classesPage.build());
                break;

            case "exams":
                pageTitleLbl.setText("Exams & Grades");
                pageSubLbl.setText("Manage examinations and student grades");
                addOutlineBtn("⬇ Report Card", () -> showAlert("Generating report cards..."));
                addTopBtn("+ Add Exam", () -> examsPage.showAddDialog(() -> navigateTo("exams")));
                setContent(examsPage.build());
                break;

            case "fees":
                pageTitleLbl.setText("Fee Management");
                pageSubLbl.setText("Track and manage fee payments");
                addOutlineBtn("⬇ Export Report", () -> showAlert("Report exported!"));
                addTopBtn("+ Add Fee", () -> feesPage.showAddDialog(() -> navigateTo("fees")));
                setContent(feesPage.build());
                break;

            case "notifs":
                long unread = state.notifData.stream().filter(n -> !(boolean) n[6]).count();
                pageTitleLbl.setText("Notifications");
                pageSubLbl.setText("Manage announcements and reminders (" + unread + " unread)");
                notifsPage.setSubtitleLabel(pageSubLbl);
                addTopBtn("+ Create Notification", () -> notifsPage.showCreateDialog(() -> navigateTo("notifs")));
                setContent(notifsPage.build());
                break;

            case "reports":
                pageTitleLbl.setText("Reports & Analytics");
                pageSubLbl.setText("View comprehensive reports and analytics");
                ComboBox<String> yearBox = new ComboBox<>();
                yearBox.getItems().addAll("2025-2026","2024-2025","2023-2024");
                yearBox.setValue("2025-2026"); yearBox.setPrefHeight(40); yearBox.setStyle("-fx-font-size:13px;");
                topActionArea.getChildren().add(yearBox);
                addTopBtn("⬇ Export All", () -> showAlert("Exporting all reports..."));
                setContent(reportsPage.build());
                break;
        }
    }

    private void setContent(Node node) { contentArea.getChildren().setAll(node); }

    private void addTopBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color:#374151; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;"));
        btn.setOnAction(e -> action.run());
        topActionArea.getChildren().add(btn);
    }

    private void addOutlineBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:10 16 10 16;");
        btn.setOnAction(e -> action.run());
        topActionArea.getChildren().add(btn);
    }

    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.YES,ButtonType.NO); a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) new LoginView().show(stage);
    }
}
