package view;

import controller.TeacherController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.TeacherModel;

import java.time.LocalDate;
import java.util.List;

import static view.TeacherUIHelper.*;

/**
 * TeacherAttendancePage — persistent attendance with classes 1-10 and On Leave option.
 * Marks are stored in TeacherModel's Map and survive page navigation.
 */
public class TeacherAttendancePage {

    private final TeacherController controller;
    private final Stage             owner;

    public TeacherAttendancePage(TeacherController controller, Stage owner) {
        this.controller = controller;
        this.owner      = owner;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(16); page.setPadding(new Insets(20,28,20,28)); page.setStyle("-fx-background-color:"+C_BG+";");

        // Filter row
        HBox filterRow = new HBox(12); filterRow.setAlignment(Pos.CENTER_LEFT);
        DatePicker datePicker = new DatePicker(LocalDate.now()); datePicker.setPrefHeight(36);

        ComboBox<String> clsFilter = new ComboBox<>();
        clsFilter.getItems().add("All Classes");
        for (String c : TeacherModel.CLASS_NAMES) clsFilter.getItems().add(c);
        clsFilter.setValue("Class 1-A"); clsFilter.setPrefHeight(36);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All","Present","Absent","Late","On Leave");
        statusFilter.setValue("All"); statusFilter.setPrefHeight(36);

        filterRow.getChildren().addAll(
            lbl("Date:", "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), datePicker,
            lbl("Class:","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), clsFilter,
            lbl("Status:","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), statusFilter
        );

        // Live summary cards
        Label[] sumVals = new Label[4];
        String[] sumColors = {C_PURPLE, C_GREEN, C_RED, C_ORANGE};
        String[] sumLabels = {"Total","Present","Absent","On Leave"};
        GridPane sumCards = new GridPane(); sumCards.setHgap(12); sumCards.setVgap(12);
        for (int i = 0; i < 4; i++) {
            VBox c = new VBox(4); c.setPadding(new Insets(12,14,12,14));
            c.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
            sumVals[i] = new Label("0"); sumVals[i].setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+sumColors[i]+";");
            Label sl = new Label(sumLabels[i]); sl.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
            c.getChildren().addAll(sl, sumVals[i]); sumCards.add(c, i, 0);
        }

        ObservableList<ObservableList<String>> attData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> table = buildTable(attData, datePicker, clsFilter, sumVals);
        table.setItems(attData);

        Runnable reload = () -> {
            String selDate = datePicker.getValue()!=null?datePicker.getValue().toString():LocalDate.now().toString();
            String selCls  = clsFilter.getValue(), selStat = statusFilter.getValue();
            attData.clear();
            for (String[] r : controller.getAttendanceForClass(selDate, selCls))
                if (selStat.equals("All")||r[4].equalsIgnoreCase(selStat))
                    attData.add(FXCollections.observableArrayList(r));
            updateSummary(selDate, selCls, sumVals);
        };
        reload.run();
        datePicker.setOnAction(e -> reload.run());
        clsFilter.setOnAction(e -> reload.run());
        statusFilter.setOnAction(e -> reload.run());

        Button saveBtn = makePrimaryBtn("💾  Save Attendance");
        saveBtn.setOnAction(e -> showAlert("✅ Attendance saved successfully!"));
        HBox saveRow = new HBox(saveBtn); saveRow.setAlignment(Pos.CENTER_RIGHT);

        VBox tableCard = new VBox(12); tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        Label tl = new Label("Student Attendance"); tl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        tableCard.getChildren().addAll(tl, table); VBox.setVgrow(table, Priority.ALWAYS);

        page.getChildren().addAll(filterRow, sumCards, tableCard, saveRow);
        scroll.setContent(page); return scroll;
    }

    private TableView<ObservableList<String>> buildTable(ObservableList<ObservableList<String>> attData,
            DatePicker datePicker, ComboBox<String> clsFilter, Label[] sumVals) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(54); return row; });

        String[] cols  = {"Roll No","Student Name","Class","Date","Status"};
        int[]    widths = {80,180,110,120,100};
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i == 4) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    Label l = new Label(item);
                    String bg = item.equals("Present")?C_TEXT:item.equals("Absent")?C_RED:item.equals("Late")?C_ORANGE:item.equals("On Leave")?C_BLUE:"#F1F5F9";
                    String fg = item.equals("Not Marked")?C_MUTED:"white";
                    l.setStyle("-fx-background-color:"+bg+"; -fx-text-fill:"+fg+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10;");
                    setGraphic(l);
                }
            });
            tv.getColumns().add(col);
        }

        // Mark Attendance column — persists to model on click
        TableColumn<ObservableList<String>,Void> markCol = new TableColumn<>("Mark Attendance");
        markCol.setPrefWidth(310);
        markCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                int rowIdx = getIndex();
                if (rowIdx<0||rowIdx>=getTableView().getItems().size()) { setGraphic(null); return; }
                ObservableList<String> row = getTableView().getItems().get(rowIdx);
                String current = row.get(4);
                HBox btns = new HBox(6); btns.setAlignment(Pos.CENTER_LEFT); btns.setPadding(new Insets(0,0,0,6));
                String[][] opts = {{"Present",C_TEXT},{"Absent",C_RED},{"Late",C_ORANGE},{"On Leave",C_BLUE}};
                for (String[] opt : opts) {
                    boolean active = current.equals(opt[0]);
                    Button b = new Button(opt[0]);
                    b.setStyle("-fx-background-color:"+(active?opt[1]:"#F1F5F9")+"; -fx-text-fill:"+(active?"white":C_MUTED)+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10 5 10; -fx-cursor:hand;");
                    b.setOnAction(e -> {
                        String selDate = datePicker.getValue()!=null?datePicker.getValue().toString():LocalDate.now().toString();
                        controller.markAttendance(selDate, row.get(2), row.get(0), opt[0]);
                        row.set(4, opt[0]);
                        getTableView().refresh();
                        updateSummary(selDate, clsFilter.getValue(), sumVals);
                    });
                    btns.getChildren().add(b);
                }
                setGraphic(btns);
            }
        });
        tv.getColumns().add(markCol);
        return tv;
    }

    private void updateSummary(String date, String cls, Label[] sumVals) {
        int total=0,present=0,absent=0,leave=0;
        for (String[] r : controller.getAttendanceForClass(date, cls)) {
            total++;
            if(r[4].equals("Present")) present++;
            else if(r[4].equals("Absent")) absent++;
            else if(r[4].equals("On Leave")) leave++;
        }
        sumVals[0].setText(String.valueOf(total)); sumVals[1].setText(String.valueOf(present));
        sumVals[2].setText(String.valueOf(absent)); sumVals[3].setText(String.valueOf(leave));
    }
}
