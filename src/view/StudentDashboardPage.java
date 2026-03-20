
package view;

import controller.StudentController;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import static view.StudentUIHelper.*;

/**
 * StudentDashboardPage — home with 4 stat cards, bar chart, recent notifications.
 * Matches the screenshot exactly.
 */
public class StudentDashboardPage {

    private final StudentController controller;

    public StudentDashboardPage(StudentController controller) {
        this.controller = controller;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(24);
        page.setPadding(new Insets(28, 32, 28, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // ── 4 stat cards ──────────────────────────────────────────────────
        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            statCard("Attendance",     controller.getAttendancePct(),  controller.getAttendanceSub(),  "📅", C_BLUE),
            statCard("Average Grade",  controller.getAvgGrade(),       controller.getAvgGradeSub(),    "📋", C_GREEN),
            statCard("Pending Fees",   controller.getPendingFees(),     controller.getPendingFeesSub(), "$",  C_ORANGE),
            statCard("Upcoming Exams", String.valueOf(controller.getUpcomingExams()), controller.getUpcomingExamSub(), "📋", C_PURPLE)
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Chart + notifications row ─────────────────────────────────────
        HBox bottomRow = new HBox(20);
        VBox chartCard  = buildPerformanceChart();
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        VBox notifCard  = buildRecentNotifications();
        notifCard.setPrefWidth(360); notifCard.setMinWidth(320);
        bottomRow.getChildren().addAll(chartCard, notifCard);

        page.getChildren().addAll(cards, bottomRow);
        scroll.setContent(page);
        return scroll;
    }

    // ── Stat card (matches screenshot style) ─────────────────────────────
    private VBox statCard(String label, String value, String sub, String icon, String color) {
        VBox card = new VBox(8); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");
        HBox top = new HBox(); top.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(label); nameLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        // Icon circle
        StackPane iconBox = new StackPane();
        Circle ic = new Circle(18, Color.web(color, 0.12));
        Label il = new Label(icon); il.setStyle("-fx-font-size:14px; -fx-text-fill:"+color+";");
        iconBox.getChildren().addAll(ic, il);
        top.getChildren().addAll(nameLbl, iconBox);
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size:32px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        card.getChildren().addAll(top, valLbl, subLbl);
        return card;
    }

    // ── Performance bar chart ─────────────────────────────────────────────
    private VBox buildPerformanceChart() {
        VBox card = new VBox(12); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = lbl("Your Performance", "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        String[] labels = controller.getPerfLabels();
        double[] scores = controller.getPerfScores();

        Pane chartPane = new Pane();
        chartPane.setPrefHeight(260); VBox.setVgrow(chartPane, Priority.ALWAYS);
        Canvas canvas = new Canvas();
        chartPane.getChildren().add(canvas);
        canvas.widthProperty().bind(chartPane.widthProperty());
        canvas.heightProperty().bind(chartPane.heightProperty());

        Runnable draw = () -> {
            double W = canvas.getWidth(), H = canvas.getHeight();
            if (W < 10 || H < 10) return;
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0,W,H); gc.setFill(Color.WHITE); gc.fillRect(0,0,W,H);
            double pL=50, pR=20, pT=20, pB=40, cW=W-pL-pR, cH=H-pT-pB;
            // Grid lines + Y labels
            gc.setStroke(Color.web("#E2E8F0")); gc.setLineDashes(4); gc.setLineWidth(1);
            for (int i=0;i<=4;i++) {
                double y=pT+cH*i/4;
                gc.strokeLine(pL,y,W-pR,y);
                gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
                gc.fillText(String.valueOf(100-i*25), 2, y+4);
            }
            gc.setLineDashes(0);
            // Bars
            int n=labels.length; double sp=cW/n, bw=sp*0.45;
            for (int i=0;i<n;i++) {
                double bh=scores[i]*cH/100, x=pL+sp*i+(sp-bw)/2, y=pT+cH-bh;
                gc.setFill(Color.web(C_BLUE)); gc.fillRoundRect(x,y,bw,bh,6,6);
                gc.setFill(Color.web(C_TEXT)); gc.setFont(Font.font("SansSerif", FontWeight.BOLD,11));
                String st=String.valueOf((int)scores[i]);
                gc.fillText(st, x+(bw-st.length()*6.0)/2, y-5);
                gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
                gc.fillText(labels[i], x+(bw-labels[i].length()*5.5)/2, pT+cH+16);
            }
        };
        canvas.widthProperty().addListener(o -> draw.run());
        canvas.heightProperty().addListener(o -> draw.run());
        Platform.runLater(draw);

        card.getChildren().addAll(title, chartPane);
        return card;
    }

    // ── Recent notifications panel (matching screenshot) ──────────────────
    private VBox buildRecentNotifications() {
        VBox card = new VBox(0); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");

        Label title = lbl("Recent Notifications", "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        card.getChildren().add(title);
        Separator sep = new Separator(); sep.setStyle("-fx-background-color:"+C_BORDER+"; -fx-padding:4 0 0 0;");
        card.getChildren().add(sep);

        for (String[] n : controller.getRecentNotifications()) {
            HBox row = new HBox(12); row.setPadding(new Insets(14, 0, 14, 0)); row.setAlignment(Pos.TOP_LEFT);

            // Bell icon
            StackPane bell = new StackPane();
            Circle bc = new Circle(18, Color.web(C_ACCENT, 0.10));
            Label bl = new Label("🔔"); bl.setStyle("-fx-font-size:14px;");
            bell.getChildren().addAll(bc, bl);
            bell.setMinSize(36,36); bell.setMaxSize(36,36);

            VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
            Label tl = new Label(n[0]); tl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); tl.setWrapText(true);
            Label body = new Label(n[1]); body.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); body.setWrapText(true);
            Label date = new Label(n[2]); date.setStyle("-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";");
            info.getChildren().addAll(tl, body, date);
            row.getChildren().addAll(bell, info);
            card.getChildren().add(row);
            Separator s2 = new Separator(); s2.setStyle("-fx-background-color:"+C_BORDER+"; -fx-opacity:0.5;");
            card.getChildren().add(s2);
        }
        return card;
    }
}
