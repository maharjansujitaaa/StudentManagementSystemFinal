
package view;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.DatabaseConnection;
import java.sql.*;
import java.util.Calendar;

import static view.UIHelper.*;

public class AttendancePage {

    private final AppState state;
    private final Stage owner;

    public AttendancePage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build() {
        return padPage(makeTabs(buildClassReportsTab(), buildTeacherAttendanceTab()));
    }

    // ── Class Reports Tab ────────────────────────────────────────────────
    private Tab buildClassReportsTab() {
        Tab tab = new Tab("Class Reports");
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        HBox topRow = new HBox(16);
        topRow.setPrefHeight(240);

        VBox cal = buildMiniCalendar();
        cal.setPrefWidth(240);
        topRow.getChildren().add(cal);

        // Class summary cards — built from classDataList in state
        GridPane classCards = new GridPane();
        classCards.setHgap(12); classCards.setVgap(12);
        HBox.setHgrow(classCards, Priority.ALWAYS);

        // Build a card for each class (up to 6 shown)
        int shown = Math.min(state.classDataList.size(), 6);
        String[] pcts = {"95%","88%","92%","85%","97%","90%"};
        String[] abs  = {"1","3","2","4","1","3"};
        for (int i = 0; i < shown; i++) {
            String[] cl = state.classDataList.get(i);
            VBox card = buildClassAttCard(cl[1], cl[3], pcts[i % pcts.length], abs[i % abs.length]);
            classCards.add(card, i % 3, i / 3);
        }
        topRow.getChildren().add(classCards);

        // Build attendance table from state classes
        String[][] tableData = new String[shown][7];
        String[] statuses = {"Good","Average","Good","Average","Excellent","Good"};
        for (int i = 0; i < shown; i++) {
            String[] cl = state.classDataList.get(i);
            int total = 30; try { total = Integer.parseInt(cl[3]); } catch (Exception ignored) {}
            int present = (int)(total * 0.9), absent = total - present - 1, late = 1;
            tableData[i] = new String[]{cl[1], String.valueOf(total), String.valueOf(present),
                String.valueOf(absent), String.valueOf(late), pcts[i % pcts.length], statuses[i % statuses.length]};
        }

        TableView<ObservableList<String>> table = buildSimpleTable(
            new String[]{"Class","Total Students","Present","Absent","Late","Attendance %","Status"},
            new int[]{120, 120, 80, 80, 80, 110, 100},
            tableData
        );
        table.setPrefHeight(280);

        content.getChildren().addAll(topRow, table);
        tab.setContent(bgScroll(content));
        return tab;
    }

    // ── Teacher Attendance Tab ───────────────────────────────────────────
    private Tab buildTeacherAttendanceTab() {
        Tab tab = new Tab("Teacher Attendance");
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        HBox topRow = new HBox(16);
        topRow.setPrefHeight(240);
        VBox cal = buildMiniCalendar();
        cal.setPrefWidth(240);

        // Build teacher data from state.teacherList
        ObservableList<ObservableList<String>> tData = FXCollections.observableArrayList();
        for (int i = 0; i < state.teacherList.size(); i++) {
            String[] t = state.teacherList.get(i);
            tData.add(FXCollections.observableArrayList(
                String.valueOf(i + 1), t[0], t[2], "N/A", "Not Marked"));
        }
        // Also load from DB if not in state
        try {
            ResultSet rs = DatabaseConnection.getConnection().prepareStatement(
                "SELECT full_name FROM users WHERE role='TEACHER' ORDER BY id").executeQuery();
            int idx = state.teacherList.size() + 1;
            while (rs.next()) {
                String n = rs.getString(1);
                boolean already = state.teacherList.stream().anyMatch(t -> t[0].equals(n));
                if (!already) tData.add(FXCollections.observableArrayList(String.valueOf(idx++), n, "General", "N/A", "Not Marked"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Live summary labels
        Label[] summaryVals = new Label[4];
        String[] summaryColors  = {C_PURPLE, C_GREEN, C_RED, C_ORANGE};
        String[] summaryLabels  = {"Total Teachers","Present Today","Absent Today","On Leave"};

        GridPane summCards = new GridPane();
        summCards.setHgap(12); summCards.setVgap(12);
        HBox.setHgrow(summCards, Priority.ALWAYS);
        for (int i = 0; i < 4; i++) {
            VBox c = new VBox(4); c.setPadding(new Insets(14, 16, 14, 16));
            c.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:10; -fx-background-radius:10;");
            summaryVals[i] = new Label(i == 0 ? String.valueOf(tData.size()) : "0");
            summaryVals[i].setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:" + summaryColors[i] + ";");
            Label l = new Label(summaryLabels[i]); l.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
            c.getChildren().addAll(l, summaryVals[i]);
            summCards.add(c, i % 2, i / 2);
        }
        topRow.getChildren().addAll(cal, summCards);

        // Live summary recalculator
        Runnable updateSummary = () -> {
            int present = 0, absent = 0, leave = 0;
            for (ObservableList<String> r : tData) {
                String s = r.get(4);
                if (s.equals("Present")) present++;
                else if (s.equals("Absent")) absent++;
                else if (s.equals("On Leave")) leave++;
            }
            summaryVals[0].setText(String.valueOf(tData.size()));
            summaryVals[1].setText(String.valueOf(present));
            summaryVals[2].setText(String.valueOf(absent));
            summaryVals[3].setText(String.valueOf(leave));
        };

        // Teacher table
        String[] tcols  = {"#","Teacher Name","Subject","Phone","Status","Mark Attendance"};
        int[]    twidths = {50, 180, 120, 100, 120, 260};
        TableView<ObservableList<String>> table = new TableView<>();
        table.setStyle("-fx-font-size:13px;");
        table.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(56); return row; });

        for (int i = 0; i < 5; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(tcols[i]);
            col.setPrefWidth(twidths[i]);
            col.setCellValueFactory(dd -> new javafx.beans.property.SimpleStringProperty(dd.getValue().get(fi)));
            if (i == 4) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    String bg = item.equals("Present") ? C_TEXT : item.equals("Absent") ? C_RED : item.equals("Late") ? C_ORANGE : item.equals("On Leave") ? C_BLUE : "#F1F5F9";
                    String fg = item.equals("Not Marked") ? C_MUTED : "white";
                    l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10;");
                    setGraphic(l);
                }
            });
            table.getColumns().add(col);
        }

        TableColumn<ObservableList<String>, Void> markCol = new TableColumn<>(tcols[5]);
        markCol.setPrefWidth(twidths[5]);
        markCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                int rowIdx = getIndex();
                if (rowIdx < 0 || rowIdx >= getTableView().getItems().size()) { setGraphic(null); return; }
                String status = getTableView().getItems().get(rowIdx).get(4);
                HBox btns = new HBox(6); btns.setAlignment(Pos.CENTER_LEFT); btns.setPadding(new Insets(0, 0, 0, 8));
                String[][] opts = {{"Present",C_TEXT},{"Absent",C_RED},{"Late",C_ORANGE},{"On Leave",C_BLUE}};
                for (String[] opt : opts) {
                    boolean active = status.equals(opt[0]);
                    Button b = new Button(opt[0]);
                    b.setStyle("-fx-background-color:" + (active ? opt[1] : "#F1F5F9") + "; -fx-text-fill:" + (active ? "white" : C_MUTED) + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 8 5 8; -fx-cursor:hand;");
                    b.setOnAction(e -> {
                        getTableView().getItems().get(rowIdx).set(4, opt[0]);
                        getTableView().refresh();
                        updateSummary.run();
                    });
                    btns.getChildren().add(b);
                }
                setGraphic(btns);
            }
        });
        table.getColumns().add(markCol);
        table.setItems(tData);
        table.setPrefHeight(280);

        Button saveBtn = makePrimaryBtn("Save Attendance");
        saveBtn.setOnAction(e -> showAlert("Teacher attendance saved!"));
        HBox saveRow = new HBox(saveBtn); saveRow.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(topRow, table, saveRow);
        tab.setContent(bgScroll(content));
        return tab;
    }

    // ── Mini Calendar ────────────────────────────────────────────────────
    private VBox buildMiniCalendar() {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:10; -fx-background-radius:10;");

        Calendar cal = Calendar.getInstance();
        int[] yr = {cal.get(Calendar.YEAR)}, mo = {cal.get(Calendar.MONTH)};
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int[] sel = {today};
        String[] monthNames = {"January","February","March","April","May","June","July","August","September","October","November","December"};

        Label monthLbl = new Label(monthNames[mo[0]] + " " + yr[0]);
        monthLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");

        GridPane grid = new GridPane(); grid.setHgap(2); grid.setVgap(2);
        Runnable[] rebuild = {null};
        rebuild[0] = () -> {
            grid.getChildren().clear();
            monthLbl.setText(monthNames[mo[0]] + " " + yr[0]);
            String[] days = {"S","M","T","W","T","F","S"};
            for (int i = 0; i < 7; i++) {
                Label dl = new Label(days[i]);
                dl.setStyle("-fx-font-size:9px; -fx-font-weight:bold; -fx-text-fill:" + C_MUTED + ";");
                dl.setMinWidth(26); dl.setAlignment(Pos.CENTER);
                grid.add(dl, i, 0);
            }
            Calendar tmp = Calendar.getInstance(); tmp.set(yr[0], mo[0], 1);
            int start = tmp.get(Calendar.DAY_OF_WEEK) - 1, max = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < start; i++) grid.add(new Label(""), i, 1);
            for (int d = 1; d <= max; d++) {
                final int fd = d;
                boolean isToday = (d == today && mo[0] == cal.get(Calendar.MONTH) && yr[0] == cal.get(Calendar.YEAR));
                boolean isSel   = (d == sel[0]);
                Label dl = new Label(String.valueOf(d));
                dl.setMinSize(24,22); dl.setMaxSize(24,22); dl.setAlignment(Pos.CENTER);
                dl.setStyle("-fx-font-size:10px; -fx-cursor:hand; -fx-background-radius:5; -fx-background-color:" + (isSel ? C_TEXT : isToday ? C_SEL : "transparent") + "; -fx-text-fill:" + (isSel ? "white" : isToday ? C_ACCENT : C_TEXT) + ";");
                dl.setOnMouseClicked(e -> { sel[0] = fd; rebuild[0].run(); });
                grid.add(dl, (start + d - 1) % 7, (start + d - 1) / 7 + 1);
            }
        };
        rebuild[0].run();

        Button prev = new Button("<"); prev.setStyle("-fx-background-color:transparent; -fx-font-size:11px; -fx-cursor:hand; -fx-text-fill:" + C_MUTED + ";");
        Button next = new Button(">"); next.setStyle("-fx-background-color:transparent; -fx-font-size:11px; -fx-cursor:hand; -fx-text-fill:" + C_MUTED + ";");
        prev.setOnAction(e -> { mo[0]--; if (mo[0] < 0) { mo[0] = 11; yr[0]--; } rebuild[0].run(); });
        next.setOnAction(e -> { mo[0]++; if (mo[0] > 11) { mo[0] = 0; yr[0]++; } rebuild[0].run(); });
        HBox header = new HBox(8); header.setAlignment(Pos.CENTER); header.getChildren().addAll(prev, monthLbl, next);

        card.getChildren().addAll(header, grid);
        return card;
    }

    private VBox buildClassAttCard(String name, String students, String pct, String absent) {
        VBox card = new VBox(6); card.setPadding(new Insets(12,14,12,14));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");
        card.setPrefWidth(160);
        Label nl = new Label(name); nl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        Label sl = new Label(students + " Students"); sl.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_MUTED + ";");
        int p = 0; try { p = Integer.parseInt(pct.replace("%","")); } catch (Exception ignored) {}
        Label pl = new Label(pct); pl.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + (p>=95?C_GREEN:p>=88?C_BLUE:C_ORANGE) + ";");
        Label al = new Label(absent + " Absent"); al.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_RED + ";");
        card.getChildren().addAll(nl, sl, pl, al);
        return card;
    }
}
