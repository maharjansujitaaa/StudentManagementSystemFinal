package view;

import controller.StudentController;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static view.StudentUIHelper.*;

/**
 * StudentTimetablePage — weekly colour-coded timetable.
 * Fix: header and data columns derived from same source so Period 6 always shows.
 */
public class StudentTimetablePage {

    private static final String[] BG   = {"#DBEAFE","#DCF8E6","#FEF9C3","#FCE7F3","#EDE9FE","#FFEDD5","#CFFAFE","#FEE2E2"};
    private static final String[] FG   = {"#1D4ED8","#15803D","#854D0E","#9D174D","#5B21B6","#9A3412","#0E7490","#991B1B"};

    private final StudentController controller;

    public StudentTimetablePage(StudentController c) { this.controller = c; }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(20);
        page.setPadding(new Insets(24,32,24,32));
        page.setStyle("-fx-background-color:"+C_BG+";");

        // Info badges
        HBox info = new HBox(12); info.setAlignment(Pos.CENTER_LEFT);
        Label classLbl = new Label("Class: "+controller.getStudentClass());
        classLbl.setStyle("-fx-background-color:#EFF6FF; -fx-text-fill:"+C_ACCENT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14 6 14; -fx-border-color:"+C_ACCENT+"; -fx-border-radius:6;");
        Label rollLbl = new Label("Roll No: "+controller.getRollNo());
        rollLbl.setStyle("-fx-background-color:#F8F9FA; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-background-radius:6; -fx-padding:6 14 6 14; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6;");
        info.getChildren().addAll(classLbl, rollLbl);

        // Card
        VBox card = new VBox(12); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");
        card.getChildren().add(lbl("Weekly Timetable","-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"));

        String[][] tt    = controller.getTimetable();
        String[]   times = controller.getPeriodTimes();
        // tt[row] = { Day, P1, P2, P3, Break, P4, P5, P6 }  → 8 elements
        // times   = { "8:00","9:00","10:00","11:00","12:00","1:00","2:00" } → 7 times but Break takes slot 4

        GridPane grid = new GridPane(); grid.setHgap(6); grid.setVgap(6);
        final int NUM_COLS = tt.length > 0 ? tt[0].length : 8; // should be 8

        // ── Build header using FIRST data row as blueprint ─────────────────
        grid.add(styledLabel("", 100, "#FFFFFF", C_TEXT), 0, 0); // corner

        if (tt.length > 0) {
            String[] sample = tt[0];
            int periodCounter = 0; // counts non-break periods
            for (int c = 1; c < sample.length; c++) {
                boolean isBreak = sample[c].equalsIgnoreCase("Break");
                VBox hdr = new VBox(2); hdr.setAlignment(Pos.CENTER);
                hdr.setPadding(new Insets(8,4,8,4)); hdr.setMinWidth(112); hdr.setPrefWidth(112);
                if (isBreak) {
                    hdr.setStyle("-fx-background-color:#F1F5F9; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6;");
                    hdr.getChildren().add(lbl("Break","-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:"+C_MUTED+";"));
                } else {
                    periodCounter++;
                    // time: period 1=index 0, period 2=index 1 ... period 4=index 4 (skip break slot)
                    // map period number to times array index accounting for the break slot
                    int timeIdx = c - 1; // direct index into times (break slot at index 3 = "11:00" = lunch time)
                    String timeStr = (timeIdx >= 0 && timeIdx < times.length) ? times[timeIdx] : "";
                    hdr.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6;");
                    hdr.getChildren().addAll(
                        lbl("Period "+periodCounter, "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
                        lbl(timeStr, "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";")
                    );
                }
                grid.add(hdr, c, 0);
            }
        }

        // ── Data rows ──────────────────────────────────────────────────────
        for (int r = 0; r < tt.length; r++) {
            String[] row = tt[r];

            // Day cell
            Label day = new Label(row[0]);
            day.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            day.setMinWidth(100); day.setPrefWidth(100);
            day.setPadding(new Insets(12,4,12,4));
            grid.add(day, 0, r+1);

            // Subject / Break cells — iterate ALL columns 1..row.length-1
            for (int c = 1; c < row.length; c++) {
                String subj = row[c];
                if (subj.equalsIgnoreCase("Break")) {
                    Label bl = new Label("🍽  Break");
                    bl.setAlignment(Pos.CENTER);
                    bl.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:"+C_MUTED+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:14 6 14 6; -fx-alignment:CENTER;");
                    bl.setMinWidth(112); bl.setPrefWidth(112);
                    grid.add(bl, c, r+1);
                } else {
                    int idx = Math.abs(subj.hashCode()) % BG.length;
                    Label sl = new Label(subj);
                    sl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+FG[idx]+"; -fx-background-color:"+BG[idx]+"; -fx-background-radius:6; -fx-padding:14 8 14 8; -fx-alignment:CENTER;");
                    sl.setAlignment(Pos.CENTER);
                    sl.setMinWidth(112); sl.setPrefWidth(112);
                    grid.add(sl, c, r+1);
                }
            }
        }

        ScrollPane gs = new ScrollPane(grid);
        gs.setFitToHeight(true);
        gs.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gs.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gs.setStyle("-fx-background-color:white; -fx-background:white; -fx-border-color:transparent;");
        gs.setPrefHeight(320);

        card.getChildren().add(gs);
        page.getChildren().addAll(info, card);
        scroll.setContent(page);
        return scroll;
    }

    private Label styledLabel(String text, double minW, String bg, String fg) {
        Label l = new Label(text);
        l.setMinWidth(minW); l.setPrefWidth(minW);
        l.setStyle("-fx-background-color:"+bg+"; -fx-text-fill:"+fg+"; -fx-font-size:13px;");
        return l;
    }
}