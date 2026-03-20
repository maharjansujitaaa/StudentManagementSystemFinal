
package view;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import model.DatabaseConnection;
import java.sql.*;
import java.util.*;

import static view.UIHelper.*;

/**
 * Dashboard home page — stat cards + live bar charts + upcoming events.
 * Charts automatically reflect the latest gradesData and feeData from AppState.
 */
public class DashboardPage {

    private final AppState state;

    public DashboardPage(AppState state) {
        this.state = state;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + C_BG + "; -fx-background:" + C_BG + ";");

        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 28, 24, 28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Stat cards (live from DB) ────────────────────────────────────
        HBox cards = new HBox(14);
        cards.getChildren().addAll(
            makeStatCard("Total Students", dbCount("STUDENT"), C_BLUE,   "👥"),
            makeStatCard("Total Teachers", dbCount("TEACHER"), C_PURPLE, "👩‍🏫"),
            makeStatCard("Total Classes",  String.valueOf(state.classDataList.size()), C_GREEN, "🏫"),
            makeStatCard("Total Admins",   dbCount("ADMIN"),   C_ORANGE, "👤")
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Charts row (FULLY LIVE — re-computed on every resize) ────────
        HBox charts = new HBox(14);
        charts.setPrefHeight(280);
        charts.getChildren().addAll(buildAttendanceChart(), buildPerformanceChart());
        for (Node n : charts.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Upcoming events ──────────────────────────────────────────────
        VBox events = buildUpcomingEvents();

        page.getChildren().addAll(cards, charts, events);
        scroll.setContent(page);
        return scroll;
    }

    // ── Attendance chart — live from gradesData ──────────────────────────
    private VBox buildAttendanceChart() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:10; -fx-background-radius:10;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("Class-wise Attendance (Live)");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");

        // Dynamic chart — suppliers read from state.gradesData on every resize
        Pane chartPane = UIHelper.makeDynamicChartPane(
            () -> computeAttLabels(),
            () -> computeAttVals(),
            () -> computeAttColors(),
            "%", 200
        );
        VBox.setVgrow(chartPane, Priority.ALWAYS);

        card.getChildren().addAll(title, chartPane);
        return card;
    }

    private String[] computeAttLabels() {
        Map<String, int[]> map = buildAttMap();
        return map.keySet().toArray(new String[0]);
    }

    private double[] computeAttVals() {
        Map<String, int[]> map = buildAttMap();
        double[] vals = new double[map.size()];
        int i = 0;
        for (int[] v : map.values()) {
            vals[i++] = v[1] > 0 ? v[0] * 100.0 / v[1] : 0;
        }
        return vals;
    }

    private Color[] computeAttColors() {
        double[] vals = computeAttVals();
        Color[] colors = new Color[vals.length];
        for (int i = 0; i < vals.length; i++) {
            colors[i] = vals[i] >= 90 ? Color.web(C_GREEN)
                      : vals[i] >= 75 ? Color.web(C_BLUE)
                      : Color.web(C_ORANGE);
        }
        return colors;
    }

    private Map<String, int[]> buildAttMap() {
        // Build class → [present, total] from gradesData + student roster
        Map<String, int[]> map = new LinkedHashMap<>();
        for (String[] s : state.studentList) {
            map.putIfAbsent(s[3], new int[]{0, 0});
        }
        if (map.isEmpty()) {
            // fallback if studentList is empty
            for (String cls : new String[]{"Class 1-A","Class 1-B","Class 2-A"})
                map.putIfAbsent(cls, new int[]{0, 0});
        }
        for (Object[] g : state.gradesData) {
            String studentName = g[0].toString();
            for (String[] s : state.studentList) {
                if (s[1].equals(studentName)) {
                    int[] arr = map.computeIfAbsent(s[3], k -> new int[]{0, 0});
                    arr[0]++;
                    arr[1]++;
                }
            }
        }
        // Ensure total ≥ 1 so empty classes show 0%
        for (int[] v : map.values()) if (v[1] == 0) v[1] = 1;
        return map;
    }

    // ── Performance chart — live from gradesData ─────────────────────────
    private VBox buildPerformanceChart() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:10; -fx-background-radius:10;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("Avg Score per Exam (Live)");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");

        Color[] palette = {Color.web(C_BLUE), Color.web(C_PURPLE), Color.web(C_GREEN), Color.web(C_ORANGE), Color.web(C_RED)};

        Pane chartPane = UIHelper.makeDynamicChartPane(
            () -> {
                Map<String, List<Integer>> sc = buildExamScores();
                return sc.keySet().toArray(new String[0]);
            },
            () -> {
                Map<String, List<Integer>> sc = buildExamScores();
                double[] vals = new double[sc.size()]; int i = 0;
                for (List<Integer> list : sc.values())
                    vals[i++] = list.stream().mapToInt(v -> v).average().orElse(0);
                return vals;
            },
            () -> {
                int n = buildExamScores().size();
                Color[] cols = new Color[n];
                for (int i = 0; i < n; i++) cols[i] = palette[i % palette.length];
                return cols;
            },
            "", 200
        );
        VBox.setVgrow(chartPane, Priority.ALWAYS);

        card.getChildren().addAll(title, chartPane);
        return card;
    }

    private Map<String, List<Integer>> buildExamScores() {
        Map<String, List<Integer>> map = new LinkedHashMap<>();
        for (Object[] g : state.gradesData) {
            int sc = 0;
            try { sc = Integer.parseInt(g[2].toString()); } catch (Exception ignored) {}
            map.computeIfAbsent(g[1].toString(), k -> new ArrayList<>()).add(sc);
        }
        if (map.isEmpty()) map.put("No Data", List.of(0));
        return map;
    }

    // ── Upcoming events ──────────────────────────────────────────────────
    private VBox buildUpcomingEvents() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:10; -fx-background-radius:10;");

        Label title = new Label("Upcoming Events");
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        card.getChildren().add(title);

        // Build events from upcomingExams in state
        List<String[]> evList = new ArrayList<>();
        for (Object[] ex : state.upcomingExams) {
            String date = ex[3].toString(); // "2026-03-15"
            String month = "MAR", day = "15";
            if (date.length() >= 10) { month = monthAbbr(date.substring(5, 7)); day = date.substring(8, 10); }
            evList.add(new String[]{month, day, ex[0].toString(), ex[1] + " - " + ex[4], "Scheduled"});
        }
        if (evList.isEmpty()) evList.add(new String[]{"MAR","05","Parent-Teacher Meeting","3:00 PM - All Classes","Event"});

        for (String[] ev : evList) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 0, 6, 0));

            VBox badge = new VBox(2);
            badge.setAlignment(Pos.CENTER);
            badge.setStyle("-fx-background-color:#EEF2FF; -fx-background-radius:8;");
            badge.setPrefSize(46, 46); badge.setMinSize(46, 46); badge.setMaxSize(46, 46);
            Label mo = new Label(ev[0]); mo.setStyle("-fx-font-size:9px; -fx-font-weight:bold; -fx-text-fill:" + C_ACCENT + ";");
            Label dy = new Label(ev[1]); dy.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
            badge.getChildren().addAll(mo, dy);

            VBox info = new VBox(2); HBox.setHgrow(info, Priority.ALWAYS);
            Label et = new Label(ev[2]); et.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
            Label es = new Label(ev[3]); es.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
            info.getChildren().addAll(et, es);

            Label statusBadge = new Label(ev[4]);
            statusBadge.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10;");
            row.getChildren().addAll(badge, info, statusBadge);
            card.getChildren().add(row);
        }
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private HBox makeStatCard(String label, String value, String color, String icon) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:" + C_BORDER + "; -fx-border-radius:12;");

        StackPane iconBox = new StackPane();
        Rectangle ir = new Rectangle(50, 50); ir.setArcWidth(10); ir.setArcHeight(10); ir.setFill(Color.web(color));
        Label il = new Label(icon); il.setStyle("-fx-font-size:22px;");
        iconBox.getChildren().addAll(ir, il);

        VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
        Label valLbl = new Label(value); valLbl.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        Label nameLbl = new Label(label); nameLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
        info.getChildren().addAll(nameLbl, valLbl);

        card.getChildren().addAll(info, iconBox);
        return card;
    }

    private String dbCount(String role) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT COUNT(*) FROM users WHERE role=?");
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return "0";
    }

    private String monthAbbr(String mm) {
        String[] names = {"","JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
        try { int m = Integer.parseInt(mm); if (m >= 1 && m <= 12) return names[m]; } catch (Exception ignored) {}
        return "MAR";
    }
}
