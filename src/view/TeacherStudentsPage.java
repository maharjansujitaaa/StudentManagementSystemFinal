package view;

import controller.TeacherController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.TeacherModel;

import java.util.List;

import static view.TeacherUIHelper.*;

/**
 * TeacherStudentsPage — full CRUD: view, add, edit, delete.
 * Classes 1-A through 10-B in all dropdowns.
 */
public class TeacherStudentsPage {

    private final TeacherController controller;
    private final Stage             owner;
    private       Runnable          onRefresh; // set by TeacherDashboardView when navigating

    public TeacherStudentsPage(TeacherController controller, Stage owner) {
        this.controller = controller;
        this.owner      = owner;
    }

    public void setOnRefresh(Runnable r) { this.onRefresh = r; }

    public Node build() {
        BorderPane page = new BorderPane();
        page.setPadding(new Insets(20,28,20,28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // Search + class filter
        HBox searchRow = new HBox(12); searchRow.setPadding(new Insets(12,16,12,16)); searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8;");
        TextField search = new TextField(); search.setPromptText("Search by name, email or roll no...");
        search.setPrefHeight(36); search.setStyle("-fx-font-size:13px; -fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6;");
        HBox.setHgrow(search, Priority.ALWAYS);

        ComboBox<String> clsFilter = new ComboBox<>();
        clsFilter.getItems().add("All Classes");
        for (String c : TeacherModel.CLASS_NAMES) clsFilter.getItems().add(c);
        clsFilter.setValue("All Classes"); clsFilter.setPrefHeight(36);
        searchRow.getChildren().addAll(search, clsFilter);

        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> table = buildTable(tableData);
        reload(tableData, "", "All Classes");
        table.setItems(tableData);

        search.textProperty().addListener((obs,o,n) -> reload(tableData, n, clsFilter.getValue()));
        clsFilter.setOnAction(e -> reload(tableData, search.getText(), clsFilter.getValue()));

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8;");
        tableCard.getChildren().addAll(searchRow, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        page.setCenter(tableCard);
        return page;
    }

    public void showAddDialog() {
        Stage d = makeDialog(owner, "Add New Student", 560, 580);
        BorderPane root = dialogRoot("Add New Student", d);
        VBox form = new VBox(0); form.setPadding(new Insets(12,24,16,24)); form.setStyle("-fx-background-color:white;");

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints(200); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(200); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        String[] labels = {"Full Name *","Email *","Phone *","Date of Birth","Blood Group","Section"};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TextField(); fields[i].setPrefHeight(36); fields[i].setMaxWidth(Double.MAX_VALUE);
            fields[i].setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            grid.add(new VBox(4, boldLabel(labels[i]), fields[i]), i%2, i/2);
        }
        int nr = labels.length / 2;
        ComboBox<String> clsBox = new ComboBox<>();
        for (String cls : TeacherModel.CLASS_NAMES) clsBox.getItems().add(cls);
        clsBox.setValue("Class 1-A"); clsBox.setPrefHeight(36); clsBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active","Inactive"); statusBox.setValue("Active");
        statusBox.setPrefHeight(36); statusBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(new VBox(4, boldLabel("Class *"), clsBox), 0, nr);
        grid.add(new VBox(4, boldLabel("Status"), statusBox), 1, nr);
        form.getChildren().add(grid);

        Button save = makePrimaryBtn("Save Student");
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        save.setOnAction(e -> {
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                showAlert("Full Name and Email are required."); return;
            }
            controller.addStudent(new String[]{controller.nextRollNo(), fields[0].getText().trim(),
                fields[1].getText().trim(), clsBox.getValue(), fields[2].getText().trim(), statusBox.getValue(), "N/A"});
            showAlert("Student added successfully!"); d.close();
            if (onRefresh != null) onRefresh.run();
        });
        HBox btns = new HBox(12, cancel, save); btns.setAlignment(Pos.CENTER_RIGHT);
        btns.setPadding(new Insets(12,24,14,24));
        btns.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        root.setCenter(wrapScroll(form)); root.setBottom(btns);
        d.setScene(new Scene(root)); d.show();
    }

    private void reload(ObservableList<ObservableList<String>> data, String search, String cls) {
        data.clear();
        for (String[] s : controller.getStudents(search.toLowerCase(), cls))
            data.add(FXCollections.observableArrayList(s));
    }

    private TableView<ObservableList<String>> buildTable(ObservableList<ObservableList<String>> tableData) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(52); return row; });

        String[] cols  = {"Roll No","Name","Email","Class","Phone","Status","Performance"};
        int[]    widths = {75,150,190,100,100,90,100};
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i == 5) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    Label l = new Label(item); boolean active = item.equals("Active");
                    l.setStyle("-fx-background-color:"+(active?C_TEXT:"#F1F5F9")+"; -fx-text-fill:"+(active?"white":C_MUTED)+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 12 4 12;");
                    setGraphic(l);
                }
            });
            if (i == 6) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    int pct=0; try{pct=Integer.parseInt(item.replace("%",""));}catch(Exception ignored){}
                    Label l = new Label(item);
                    l.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+(pct>=90?C_GREEN:pct>=75?C_BLUE:C_ORANGE)+";");
                    setGraphic(l);
                }
            });
            tv.getColumns().add(col);
        }

        // Actions column
        TableColumn<ObservableList<String>,Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(140);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button vb = new Button("👁");
            final Button eb = new Button("✏");
            final Button db = new Button("🗑");
            {
                vb.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand;");
                eb.setStyle("-fx-background-color:transparent; -fx-font-size:14px; -fx-cursor:hand; -fx-text-fill:"+C_MUTED+";");
                db.setStyle("-fx-background-color:transparent; -fx-font-size:14px; -fx-cursor:hand; -fx-text-fill:#FFCDD2;");
                vb.setOnAction(e -> { int idx=getIndex(); if(idx>=0&&idx<getTableView().getItems().size()) showDetails(getTableView().getItems().get(idx)); });
                eb.setOnAction(e -> { int idx=getIndex(); if(idx>=0&&idx<getTableView().getItems().size()) showEditDialog(getTableView().getItems().get(idx),idx,tableData); });
                db.setOnAction(e -> {
                    int idx=getIndex(); if(idx<0||idx>=getTableView().getItems().size()) return;
                    if (confirmDelete()) {
                        String roll=getTableView().getItems().get(idx).get(0);
                        List<String[]> all=controller.getStudentList();
                        for(int i=0;i<all.size();i++) if(all.get(i)[0].equals(roll)){controller.deleteStudent(i);break;}
                        tableData.remove(idx);
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else { HBox box=new HBox(8,vb,eb,db); box.setAlignment(Pos.CENTER); setGraphic(box); }
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    private void showDetails(ObservableList<String> row) {
        Stage d = makeDialog(owner, "Student Details", 520, 400);
        BorderPane root = dialogRoot("Student Details", d);
        String name = row.size()>1?row.get(1):"Student";
        StackPane av = new StackPane();
        av.getChildren().addAll(new Circle(36,Color.web("#DBEAFE")),
            lbl(name.isEmpty()?"S":name.substring(0,1),"-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";"));
        Label status = new Label("Active"); status.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10 3 10;");
        VBox nb = new VBox(4, lbl(name,"-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            lbl("Roll No: "+(row.size()>0?row.get(0):""),"-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"), status);
        HBox avRow = new HBox(16,av,nb); avRow.setAlignment(Pos.CENTER_LEFT);
        avRow.setPadding(new Insets(16,24,12,24));
        avRow.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        VBox grid = makeInfoGrid(new String[][]{
            {"✉  Email",row.size()>2?row.get(2):""},{"🏫  Class",row.size()>3?row.get(3):""},
            {"📞  Phone",row.size()>4?row.get(4):""},{"📊  Status",row.size()>5?row.get(5):""},
            {"📈  Performance",row.size()>6?row.get(6):""},{"📅  Admission","2024-01-15"}
        });
        Button close = makePrimaryBtn("Close"); close.setOnAction(e->d.close());
        HBox foot = new HBox(close); foot.setAlignment(Pos.CENTER_RIGHT); foot.setPadding(new Insets(8,24,16,24));
        root.setCenter(new VBox(avRow,grid,foot)); d.setScene(new Scene(root)); d.show();
    }

    private void showEditDialog(ObservableList<String> row, int tableIdx, ObservableList<ObservableList<String>> tableData) {
        Stage d = makeDialog(owner, "Edit Student", 520, 520);
        BorderPane root = dialogRoot("Edit Student", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF  = addFormRow(form,"Full Name",  row.size()>1?row.get(1):"");
        TextField emailTF = addFormRow(form,"Email",      row.size()>2?row.get(2):"");
        TextField phoneTF = addFormRow(form,"Phone",      row.size()>4?row.get(4):"");
        ComboBox<String> clsBox = new ComboBox<>();
        for (String cls : TeacherModel.CLASS_NAMES) clsBox.getItems().add(cls);
        String curCls = row.size()>3?row.get(3):"Class 1-A";
        clsBox.setValue(clsBox.getItems().contains(curCls)?curCls:"Class 1-A");
        clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Class"), clsBox);
        ComboBox<String> statBox = makeCombo(row.size()>5?row.get(5):"Active","Active","Inactive");
        form.getChildren().addAll(boldLabel("Status"), statBox);
        TextField perfTF = addFormRow(form,"Performance (e.g. 85%)",row.size()>6?row.get(6):"");
        Button save = makePrimaryBtn("Update Student");
        save.setOnAction(e -> {
            String[] updated = {row.get(0),nameTF.getText().trim(),emailTF.getText().trim(),
                clsBox.getValue(),phoneTF.getText().trim(),statBox.getValue(),perfTF.getText().trim()};
            String roll=row.get(0); List<String[]> all=controller.getStudentList();
            for(int i=0;i<all.size();i++) if(all.get(i)[0].equals(roll)){controller.updateStudent(i,updated);break;}
            tableData.set(tableIdx, FXCollections.observableArrayList(updated));
            showAlert("Student updated!"); d.close();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new Scene(root)); d.show();
    }
}
