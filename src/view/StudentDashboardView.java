package view;

import controller.StudentController;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.StudentModel;

import static view.StudentUIHelper.*;

/**
 * StudentDashboardView — slim coordinator.
 * Sidebar user footer shows Profile / Student ID Card / Change Password popup.
 * Pages: Dashboard, Timetable, Exams, Attendance, Fees, Notifications,
 *        Leave Application, Profile
 */
public class StudentDashboardView {

    private Stage             stage;
    private StudentController controller;
    private String            username;
    private StackPane         contentArea;
    private Label             pageTitleLbl, pageSubLbl;
    private HBox              topActionArea;
    private Button            activeSidebarBtn;

    // Page instances
    private StudentDashboardPage     dashPage;
    private StudentTimetablePage     timetablePage;
    private StudentExamsPage         examsPage;
    private StudentAttendancePage    attendancePage;
    private StudentFeesPage          feesPage;
    private StudentNotificationsPage notifPage;
    private StudentLeavePage         leavePage;
    private StudentProfilePage       profilePage;

    public void show(Stage stage, String username) {
        this.stage    = stage;
        this.username = username;

        StudentModel model = new StudentModel(username);
        this.controller = new StudentController(model, this);

        dashPage      = new StudentDashboardPage(controller);
        timetablePage = new StudentTimetablePage(controller);
        examsPage     = new StudentExamsPage(controller, stage);
        attendancePage= new StudentAttendancePage(controller);
        feesPage      = new StudentFeesPage(controller, stage);
        notifPage     = new StudentNotificationsPage(controller);
        leavePage     = new StudentLeavePage(controller, stage);
        profilePage   = new StudentProfilePage(controller);

        stage.setTitle("EduManage - Student Dashboard");
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
        VBox sb = new VBox(0); sb.setPrefWidth(250);
        sb.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 1 0 0;");

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
            {"📊","Dashboard",      "dashboard"},
            {"📅","My Timetable",   "timetable"},
            {"📝","Exams & Grades", "exams"},
            {"✅","Attendance",     "attendance"},
            {"💰","Fees",           "fees"},
            {"🔔","Notifications",  "notices"},
            {"📨","Leave Application","leave"},
        };
        VBox navBox = new VBox(2); navBox.setPadding(new Insets(8,0,8,0)); VBox.setVgrow(navBox, Priority.ALWAYS);
        for (String[] item : nav) {
            Button btn = makeSidebarBtn(item[0], item[1], item[2]);
            navBox.getChildren().add(btn);
            if (item[2].equals("dashboard")) activeSidebarBtn = btn;
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Separator sep2 = new Separator(); sep2.setStyle("-fx-background-color:"+C_BORDER+";");

        // ── User footer with profile popup ─────────────────────────────
        VBox userSection = buildUserFooter();

        Button logoutBtn = new Button("  🚪  Logout");
        logoutBtn.setPrefWidth(Double.MAX_VALUE);
        String ls="-fx-background-color:transparent; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:10 16 12 16;";
        String lh="-fx-background-color:#FEE2E2; -fx-text-fill:"+C_RED+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:10 16 12 16;";
        logoutBtn.setStyle(ls);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(lh));
        logoutBtn.setOnMouseExited(e  -> logoutBtn.setStyle(ls));
        logoutBtn.setOnAction(e -> controller.handleLogout());

        sb.getChildren().addAll(logo, sep1, navBox, spacer, sep2, userSection, logoutBtn);
        return sb;
    }

    // ── User footer — click to show Profile / ID Card / Change Password ──
    private VBox buildUserFooter() {
        VBox section = new VBox(0);

        HBox footer = new HBox(10); footer.setPadding(new Insets(12,16,10,16)); footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-cursor:hand;");

        StackPane av = new StackPane();
        av.getChildren().addAll(
            new Circle(20, Color.web("#DBEAFE")),
            lbl(username.isEmpty()?"S":username.substring(0,1).toUpperCase(),
                "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";"));

        String displayName = username.contains(" ") ? username : cap(username);
        VBox ui = new VBox(2,
            lbl(displayName, "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            lbl(controller.getStudentClass(), "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"));
        HBox.setHgrow(ui, Priority.ALWAYS);

        // Chevron indicator
        Label chevron = new Label("⌃"); chevron.setStyle("-fx-font-size:14px; -fx-text-fill:"+C_MUTED+"; -fx-rotate:180;");
        footer.getChildren().addAll(av, ui, chevron);

        // ── Popup menu (shown/hidden on click) ─────────────────────────
        VBox popup = new VBox(0);
        popup.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        popup.setVisible(false); popup.setManaged(false);

        String[][] menuItems = {
            {"👤","Profile"},
            {"🪪","Student ID Card"},
            {"🔒","Change Password"},
        };
        for (String[] item : menuItems) {
            Button mb = new Button(item[0]+"  "+item[1]);
            mb.setPrefWidth(Double.MAX_VALUE); mb.setPrefHeight(42);
            String mi="-fx-background-color:transparent; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 24;";
            String mh="-fx-background-color:#F8FAFF; -fx-text-fill:"+C_ACCENT+"; -fx-font-size:13px; -fx-cursor:hand; -fx-alignment:CENTER_LEFT; -fx-padding:0 0 0 24;";
            mb.setStyle(mi);
            mb.setOnMouseEntered(e->mb.setStyle(mh));
            mb.setOnMouseExited(e->mb.setStyle(mi));
            mb.setOnAction(e -> {
                popup.setVisible(false); popup.setManaged(false);
                chevron.setRotate(180);
                showProfile(item[1].contains("ID Card") ? 1 : item[1].contains("Password") ? 2 : 0);
            });
            popup.getChildren().add(mb);
        }

        // Toggle popup on click
        footer.setOnMouseClicked(e -> {
            boolean show = !popup.isVisible();
            popup.setVisible(show); popup.setManaged(show);
            chevron.setRotate(show ? 0 : 180);
        });
        footer.setOnMouseEntered(e -> footer.setStyle("-fx-background-color:#F8FAFF; -fx-cursor:hand;"));
        footer.setOnMouseExited(e  -> footer.setStyle("-fx-cursor:hand;"));

        section.getChildren().addAll(footer, popup);
        return section;
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
            if (activeSidebarBtn!=null) activeSidebarBtn.setStyle(idle);
            btn.setStyle(active); activeSidebarBtn=btn;
            controller.navigate(action);
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    // MAIN AREA
    // ══════════════════════════════════════════════════════════════════════
    private BorderPane buildMainArea() {
        BorderPane main = new BorderPane(); main.setStyle("-fx-background-color:"+C_BG+";");
        HBox topBar = new HBox(); topBar.setPadding(new Insets(16,28,16,28)); topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        VBox titleBox = new VBox(4); HBox.setHgrow(titleBox, Priority.ALWAYS);
        pageTitleLbl = new Label("Dashboard");
        pageTitleLbl.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        pageSubLbl = new Label("Here's your academic overview");
        pageSubLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        titleBox.getChildren().addAll(pageTitleLbl, pageSubLbl);
        topActionArea = new HBox(8); topActionArea.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().addAll(titleBox, topActionArea);
        contentArea = new StackPane(); contentArea.setStyle("-fx-background-color:"+C_BG+";");
        main.setTop(topBar); main.setCenter(contentArea);
        return main;
    }

    private void setPage(String title, String sub) {
        pageTitleLbl.setText(title); pageSubLbl.setText(sub); topActionArea.getChildren().clear();
    }
    private void setContent(javafx.scene.Node node) { contentArea.getChildren().setAll(node); }

    // ══════════════════════════════════════════════════════════════════════
    // PAGE NAVIGATION
    // ══════════════════════════════════════════════════════════════════════
    public void showDashboard() {
        setPage("Dashboard", "Welcome back, " + cap(username) + "!");
        setContent(dashPage.build());
    }
    public void showTimetable() {
        setPage("My Timetable", "Your weekly class schedule");
        setContent(timetablePage.build());
    }
    public void showExams() {
        setPage("Exams & Grades", "View your exams and academic performance");
        setContent(examsPage.build());
    }
    public void showAttendance() {
        setPage("Attendance", "Track your attendance records");
        setContent(attendancePage.build());
    }
    public void showFees() {
        setPage("Fees", "Manage your fee payments");
        setContent(feesPage.build());
    }
    public void showNotices() {
        long unread = controller.getUnreadCount();
        setPage("Notifications", "Announcements and reminders (" + unread + " unread)");
        setContent(notifPage.build());
    }
    public void showLeave() {
        setPage("Leave Application", "Apply for leave and track your applications");
        setContent(leavePage.build());
    }

    /** Opens profile page and jumps to a specific tab (0=Profile, 1=ID Card, 2=Password) */
    private void showProfile(int tabIndex) {
        setPage("My Profile", "View and manage your personal information");
        Node profileNode = profilePage.build();
        setContent(profileNode);
        // Select the right tab
        if (tabIndex > 0 && profileNode instanceof ScrollPane) {
            ScrollPane sp = (ScrollPane) profileNode;
            if (sp.getContent() instanceof VBox) {
                VBox vb = (VBox) sp.getContent();
                if (!vb.getChildren().isEmpty() && vb.getChildren().get(0) instanceof TabPane) {
                    ((TabPane) vb.getChildren().get(0)).getSelectionModel().select(tabIndex);
                }
            }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    public void doLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.YES,ButtonType.NO); a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.NO)==ButtonType.YES) new LoginView().show(stage);
    }
}