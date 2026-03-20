package view;

import controller.TeacherController;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import static view.TeacherUIHelper.*;

/**
 * DashboardPage — Teacher Dashboard home page.
 * Shows stat cards, hover bar chart, and upcoming events.
 */
public class TeacherDashboardPage {

    private final TeacherController controller;
    private final String username;

    public TeacherDashboardPage(TeacherController controller, String username) {
        this.controller = controller;
        this.username   = username;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(20);
        page.setPadding(new Insets(28,32,28,32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Stat cards ──────────────────────────────────────────────────────
        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            makeStatCard("Total Students", String.valueOf(controller.getTotalStudents()), controller.getStudentsDelta(),   C_BLUE,   "👥"),
            makeStatCard("Total Teachers", String.valueOf(controller.getTotalTeachers()), controller.getTeachersDelta(),  C_PURPLE, "🎓"),
            makeStatCard("Total Classes",  String.valueOf(controller.getTotalClasses()),  controller.getClassesDelta(),   C_GREEN,  "📖"),
            makeStatCard("Attendance Rate",controller.getAttendanceRate(),               controller.getAttendanceDelta(), C_ORANGE, "📊")
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Charts row ──────────────────────────────────────────────────────
        HBox chartsRow = new HBox(20);
        chartsRow.setPrefHeight(340);
        VBox chartCard = buildPerformanceChart();
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        VBox eventsCard = buildUpcomingEvents();
        eventsCard.setPrefWidth(340); eventsCard.setMinWidth(320);
        chartsRow.getChildren().addAll(chartCard, eventsCard);

        page.getChildren().addAll(cards, chartsRow);
        scroll.setContent(page);
        return scroll;
    }

    // ── Stat card ────────────────────────────────────────────────────────
    private HBox makeStatCard(String label, String value, String delta, String color, String icon) {
        HBox card = new HBox(14); card.setPadding(new Insets(20)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-border-color:"+C_BORDER+"; -fx-border-radius:14;");
        VBox info = new VBox(6); HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(label); nameLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";");
        Label valLbl  = new Label(value); valLbl.setStyle("-fx-font-size:30px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label dLbl    = new Label(delta);
        dLbl.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:"+C_GREEN+"; -fx-background-color:#DCFCE7; -fx-background-radius:6; -fx-padding:2 6 2 6;");
        info.getChildren().addAll(nameLbl, valLbl, dLbl);
        StackPane ib = new StackPane();
        Rectangle ir = new Rectangle(50,50); ir.setArcWidth(12); ir.setArcHeight(12); ir.setFill(Color.web(color));
        Label il = new Label(icon); il.setStyle("-fx-font-size:22px;");
        ib.getChildren().addAll(ir, il);
        card.getChildren().addAll(info, ib); return card;
    }

    // ── Performance chart with hover tooltip ─────────────────────────────
    private VBox buildPerformanceChart() {
        VBox card = new VBox(12); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-border-color:"+C_BORDER+"; -fx-border-radius:14;");
        VBox.setVgrow(card, Priority.ALWAYS); HBox.setHgrow(card, Priority.ALWAYS);

        Label title = lbl("Average Performance by Subject", "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        String[] labels = controller.getSubjectLabels();
        double[] scores = controller.getSubjectScores();

        // Tooltip popup
        VBox tooltip = new VBox(4); tooltip.setPadding(new Insets(10,14,10,14));
        tooltip.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),10,0,0,3);");
        tooltip.setVisible(false); tooltip.setMouseTransparent(true);
        Label ttName  = new Label(); ttName.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label ttScore = new Label(); ttScore.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_PURPLE+"; -fx-font-weight:bold;");
        tooltip.getChildren().addAll(ttName, ttScore);

        Pane chartPane = new Pane();
        chartPane.setPrefHeight(240); VBox.setVgrow(chartPane, Priority.ALWAYS);
        Canvas canvas = new Canvas();
        chartPane.getChildren().addAll(canvas, tooltip);
        canvas.widthProperty().bind(chartPane.widthProperty());
        canvas.heightProperty().bind(chartPane.heightProperty());

        double[] barX = new double[labels.length];
        double[] barW = new double[labels.length];
        double[] barY = new double[labels.length];

        Runnable draw = () -> {
            double W=canvas.getWidth(), H=canvas.getHeight(); if(W<10||H<10) return;
            GraphicsContext gc=canvas.getGraphicsContext2D();
            gc.clearRect(0,0,W,H); gc.setFill(Color.WHITE); gc.fillRect(0,0,W,H);
            double pL=50,pR=20,pT=20,pB=40,cW=W-pL-pR,cH=H-pT-pB;
            gc.setStroke(Color.web("#E2E8F0")); gc.setLineDashes(4); gc.setLineWidth(1);
            for (int i=0;i<=4;i++) { double y=pT+cH*i/4; gc.strokeLine(pL,y,W-pR,y); gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11)); gc.fillText(String.valueOf((int)(100-i*25)),2,y+4); }
            gc.setLineDashes(0);
            double sp=cW/labels.length,bw=sp*0.45;
            for (int i=0;i<labels.length;i++) {
                double bh=scores[i]*cH/100,x=pL+sp*i+(sp-bw)/2,y=pT+cH-bh;
                barX[i]=x; barW[i]=bw; barY[i]=y;
                gc.setFill(Color.web(C_CHART)); gc.fillRoundRect(x,y,bw,bh,6,6);
                gc.setFill(Color.web(C_TEXT)); gc.setFont(Font.font("SansSerif",FontWeight.BOLD,11));
                String st=String.valueOf((int)scores[i]); gc.fillText(st,x+(bw-st.length()*6.0)/2,y-5);
                gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
                gc.fillText(labels[i],x+(bw-labels[i].length()*5.5)/2,pT+cH+16);
            }
        };
        canvas.widthProperty().addListener(o -> draw.run());
        canvas.heightProperty().addListener(o -> draw.run());
        Platform.runLater(draw);

        // Hover tooltip
        chartPane.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            boolean hit = false;
            for (int i = 0; i < labels.length; i++) {
                if (e.getX()>=barX[i] && e.getX()<=barX[i]+barW[i] && e.getY()>=barY[i]) {
                    double W=canvas.getWidth(),H=canvas.getHeight();
                    GraphicsContext gc=canvas.getGraphicsContext2D();
                    gc.clearRect(0,0,W,H); gc.setFill(Color.WHITE); gc.fillRect(0,0,W,H);
                    double pL=50,pR=20,pT=20,pB=40,cW=W-pL-pR,cH=H-pT-pB;
                    gc.setStroke(Color.web("#E2E8F0")); gc.setLineDashes(4); gc.setLineWidth(1);
                    for (int k=0;k<=4;k++) { double y=pT+cH*k/4; gc.strokeLine(pL,y,W-pR,y); gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11)); gc.fillText(String.valueOf((int)(100-k*25)),2,y+4); }
                    gc.setLineDashes(0);
                    double sp=cW/labels.length,bw2=sp*0.45;
                    final int hi = i;
                    for (int j=0;j<labels.length;j++) {
                        double bh=scores[j]*cH/100,x=pL+sp*j+(sp-bw2)/2,y=pT+cH-bh;
                        gc.setFill(j==hi?Color.web("#9F67F5"):Color.web(C_CHART));
                        gc.fillRoundRect(x,y,bw2,bh,6,6);
                        if (j==hi) { gc.setFill(Color.rgb(200,200,200,0.3)); gc.fillRoundRect(x,pT,bw2,cH-bh,6,6); }
                        gc.setFill(Color.web(C_TEXT)); gc.setFont(Font.font("SansSerif",FontWeight.BOLD,11));
                        String st=String.valueOf((int)scores[j]); gc.fillText(st,x+(bw2-st.length()*6.0)/2,y-5);
                        gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
                        gc.fillText(labels[j],x+(bw2-labels[j].length()*5.5)/2,pT+cH+16);
                    }
                    ttName.setText(labels[i]); ttScore.setText("average : "+(int)scores[i]);
                    double tx=barX[i]+barW[i]/2-50, ty=barY[i]+20;
                    if(tx+140>canvas.getWidth()) tx=canvas.getWidth()-145; if(tx<0) tx=5;
                    tooltip.setLayoutX(tx); tooltip.setLayoutY(ty); tooltip.setVisible(true); hit=true; break;
                }
            }
            if (!hit) { tooltip.setVisible(false); draw.run(); }
        });
        chartPane.addEventHandler(MouseEvent.MOUSE_EXITED, e -> { tooltip.setVisible(false); draw.run(); });

        card.getChildren().addAll(title, chartPane); return card;
    }

    // ── Upcoming events ─────────────────────────────────────────────────
    private VBox buildUpcomingEvents() {
        VBox card = new VBox(0); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-border-color:"+C_BORDER+"; -fx-border-radius:14;");
        Label title = lbl("Upcoming Events","-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        card.getChildren().add(title);
        Separator sep0 = new Separator(); sep0.setStyle("-fx-background-color:"+C_BORDER+";");
        card.getChildren().add(sep0);

        for (String[] ev : controller.getUpcomingEvents()) {
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(10,0,10,0));

            VBox db = new VBox(2); db.setAlignment(Pos.CENTER);
            db.setStyle("-fx-background-color:#EEF2FF; -fx-background-radius:10;");
            db.setPrefSize(60,60); db.setMinSize(60,60); db.setMaxSize(60,60);
            Label mo = new Label(ev[0]); mo.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";");
            Label dy = new Label(ev[1]); dy.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            db.getChildren().addAll(mo, dy);

            VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
            Label et = new Label(ev[2]); et.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); et.setWrapText(true);
            Label es = new Label(ev[3]); es.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); es.setWrapText(true);
            info.getChildren().addAll(et, es);

            Label badge = new Label(ev[4]); boolean sched=ev[4].equals("Scheduled");
            badge.setStyle("-fx-background-color:"+(sched?C_TEXT:"#F1F5F9")+"; -fx-text-fill:"+(sched?"white":C_MUTED)+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10;");

            row.getChildren().addAll(db, info, badge);
            card.getChildren().add(row);
            Separator sep = new Separator(); sep.setStyle("-fx-background-color:"+C_BORDER+"; -fx-opacity:0.5;");
            card.getChildren().add(sep);
        }
        return card;
    }
}
