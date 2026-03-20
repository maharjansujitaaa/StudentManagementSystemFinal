
package view;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.DatabaseConnection;
import java.sql.*;

import static view.UIHelper.*;

public class StudentsPage {

    private final AppState state;
    private final Stage owner;
    private Runnable refreshCallback; // called after add/delete to refresh view

    public StudentsPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build(Runnable onDataChanged) {
        this.refreshCallback = onDataChanged;

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(20, 28, 20, 28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        HBox searchRow = new HBox(12);
        searchRow.setPadding(new Insets(12, 16, 12, 16));
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");

        TextField search = new TextField();
        search.setPromptText("Search by name, email, or roll number...");
        search.setPrefHeight(36);
        search.setStyle("-fx-font-size:13px; -fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6;");
        HBox.setHgrow(search, Priority.ALWAYS);

        // Class filter covers classes 1–10
        ComboBox<String> classFilter = new ComboBox<>();
        classFilter.getItems().add("All Classes");
        for (String cls : AppState.CLASS_NAMES) classFilter.getItems().add(cls);
        classFilter.setValue("All Classes");
        classFilter.setPrefHeight(36);
        searchRow.getChildren().addAll(search, classFilter);

        TableView<ObservableList<String>> table = buildStudentTable();
        ObservableList<ObservableList<String>> allData = FXCollections.observableArrayList();
        loadRows(allData, "", "All Classes");
        table.setItems(allData);

        search.textProperty().addListener((obs, o, n) -> { allData.clear(); loadRows(allData, n.toLowerCase(), classFilter.getValue()); });
        classFilter.setOnAction(e -> { allData.clear(); loadRows(allData, search.getText().toLowerCase(), classFilter.getValue()); });

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        tableCard.getChildren().addAll(searchRow, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        page.setCenter(tableCard);
        return page;
    }

    public void showAddDialog(Runnable onDone) {
        Stage d = makeDialog(owner, "Add New Student", 620, 660);
        BorderPane root = dialogRoot("Add New Student", d);
        root.setCenter(wrapScroll(buildAddForm(d, onDone)));
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private TableView<ObservableList<String>> buildStudentTable() {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(52); return row; });

        String[] cols = {"Roll No","Name","Email","Class","Phone","Status"};
        int[] widths   = {80,160,210,110,110,100};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(idx)));
            if (i == 5) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 12 4 12;");
                    setGraphic(lbl);
                }
            });
            tv.getColumns().add(col);
        }

        TableColumn<ObservableList<String>,Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(130);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button viewBtn = new Button("👁");
            final Button editBtn = new Button("✏");
            final Button delBtn  = new Button("🗑");
            {
                viewBtn.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand;");
                editBtn.setStyle("-fx-background-color:transparent; -fx-font-size:14px; -fx-cursor:hand; -fx-text-fill:#ADB5BD;");
                delBtn .setStyle("-fx-background-color:transparent; -fx-font-size:14px; -fx-cursor:hand; -fx-text-fill:#FFCDD2;");
                viewBtn.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex()), getIndex(), getTableView().getItems()));
                delBtn .setOnAction(e -> {
                    if (confirmDelete()) {
                        int i = getIndex();
                        getTableView().getItems().remove(i);
                        if (i < state.studentList.size()) state.studentList.remove(i);
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else { HBox box = new HBox(8, viewBtn, editBtn, delBtn); box.setAlignment(Pos.CENTER); setGraphic(box); }
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    private void loadRows(ObservableList<ObservableList<String>> list, String search, String classFilter) {
        // Load from in-memory state list
        for (String[] r : state.studentList) {
            boolean ms = search.isEmpty() || r[1].toLowerCase().contains(search) || r[2].toLowerCase().contains(search) || r[0].contains(search);
            boolean mc = classFilter.equals("All Classes") || r[3].equals(classFilter);
            if (ms && mc) list.add(FXCollections.observableArrayList(r));
        }
        // Also load from DB (users with role STUDENT not already in state)
        try {
            ResultSet rs = DatabaseConnection.getConnection().prepareStatement(
                "SELECT full_name,email FROM users WHERE role='STUDENT' ORDER BY id").executeQuery();
            int roll = state.studentList.size() + 1;
            while (rs.next()) {
                String name = rs.getString(1), email = rs.getString(2);
                boolean already = state.studentList.stream().anyMatch(s -> s[2].equals(email));
                if (already) continue;
                String cls = "Class 1-A";
                boolean ms = search.isEmpty() || name.toLowerCase().contains(search) || email.toLowerCase().contains(search);
                boolean mc = classFilter.equals("All Classes") || cls.equals(classFilter);
                if (ms && mc) list.add(FXCollections.observableArrayList(
                    String.format("%03d", roll++), name, email, cls, "N/A", "Active"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Node buildAddForm(Stage d, Runnable onDone) {
        VBox outer = new VBox(0);
        outer.setPadding(new Insets(16, 24, 20, 24));
        outer.setStyle("-fx-background-color:white;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(200); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(200); c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        String[] labels = {"Full Name *","Email *","Phone *","Date of Birth *","Blood Group","Section *",
                           "Roll Number *","Admission Date *","Parent Name *","Parent Phone *","Parent Email *","Username *"};
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TextField(); fields[i].setPrefHeight(38); fields[i].setMaxWidth(Double.MAX_VALUE);
            fields[i].setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            form.add(new VBox(5, boldLabel(labels[i]), fields[i]), i % 2, i / 2);
        }

        int nr = labels.length / 2;
        ComboBox<String> gender   = makeCombo("Male","Male","Female","Other");
        ComboBox<String> classBox = new ComboBox<>();
        classBox.getItems().add("Select Class");
        for (String cls : AppState.CLASS_NAMES) classBox.getItems().add(cls);
        classBox.setValue("Class 1-A");
        form.add(new VBox(5, boldLabel("Gender *"), gender), 0, nr);
        form.add(new VBox(5, boldLabel("Class *"), classBox), 1, nr);

        PasswordField pw = new PasswordField(); pw.setPrefHeight(38); pw.setMaxWidth(Double.MAX_VALUE);
        pw.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
        form.add(new VBox(5, boldLabel("Password *"), pw), 0, nr + 1, 2, 1);

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(16, 0, 0, 0));
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        Button save = makePrimaryBtn("Save Student");
        save.setOnAction(e -> {
            String fn = fields[0].getText().trim(), em = fields[1].getText().trim();
            String un = fields[11].getText().trim(), pass = pw.getText().trim();
            if (fn.isEmpty() || em.isEmpty()) { showAlert("Full Name and Email are required."); return; }
            if (un.isEmpty()) un = fn.toLowerCase().replace(" ", "");
            if (pass.isEmpty()) pass = "pass123";
            String roll = fields[6].getText().trim().isEmpty() ? String.format("%03d", state.studentList.size() + 1) : fields[6].getText().trim();
            String cls = classBox.getValue();
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "INSERT INTO users(full_name,email,username,password,role)VALUES(?,?,?,?,?)");
                ps.setString(1, fn); ps.setString(2, em); ps.setString(3, un); ps.setString(4, pass); ps.setString(5, "STUDENT");
                ps.executeUpdate();
            } catch (SQLException ex) { /* ignore duplicate */ }
            state.studentList.add(new String[]{roll, fn, em, cls, fields[2].getText().trim(), "Active"});
            showAlert("Student added successfully!");
            d.close();
            if (onDone != null) onDone.run();
        });
        btns.getChildren().addAll(cancel, save);
        form.add(btns, 0, nr + 2, 2, 1);
        outer.getChildren().add(form);
        return outer;
    }

    private void showEditDialog(ObservableList<String> row, int idx, ObservableList<ObservableList<String>> allData) {
        Stage d = makeDialog(owner, "Edit Student", 520, 560);
        BorderPane root = dialogRoot("Edit Student", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16, 24, 20, 24));
        TextField nameTF  = addFormRow(form, "Full Name",   row.size() > 1 ? row.get(1) : "");
        TextField emailTF = addFormRow(form, "Email",       row.size() > 2 ? row.get(2) : "");
        TextField phoneTF = addFormRow(form, "Phone",       row.size() > 4 ? row.get(4) : "");
        TextField rollTF  = addFormRow(form, "Roll Number", row.size() > 0 ? row.get(0) : "");
        ComboBox<String> clsBox = new ComboBox<>();
        clsBox.getItems().add("Select Class");
        for (String cls : AppState.CLASS_NAMES) clsBox.getItems().add(cls);
        clsBox.setValue(row.size() > 3 ? row.get(3) : "Class 1-A");
        form.getChildren().addAll(boldLabel("Class"), clsBox);
        ComboBox<String> statusBox = makeCombo(row.size() > 5 ? row.get(5) : "Active", "Active", "Inactive");
        form.getChildren().addAll(boldLabel("Status"), statusBox);
        Button save = makePrimaryBtn("Update Student");
        save.setOnAction(e -> {
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE users SET full_name=?,email=? WHERE email=? AND role='STUDENT'");
                ps.setString(1, nameTF.getText().trim()); ps.setString(2, emailTF.getText().trim());
                ps.setString(3, row.size() > 2 ? row.get(2) : ""); ps.executeUpdate();
            } catch (SQLException ex) { /* ignore */ }
            String[] v = {rollTF.getText().trim(), nameTF.getText().trim(), emailTF.getText().trim(),
                          clsBox.getValue(), phoneTF.getText().trim(), statusBox.getValue()};
            for (int i = 0; i < v.length && i < row.size(); i++) row.set(i, v[i]);
            allData.set(idx, row);
            // Also update state list
            if (idx < state.studentList.size()) state.studentList.set(idx, v);
            showAlert("Student updated!"); d.close();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private void showDetails(ObservableList<String> row) {
        Stage d = makeDialog(owner, "Student Details", 600, 460);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");
        HBox header = new HBox(); header.setPadding(new Insets(18, 24, 12, 24)); header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        Label title = new Label("Student Details"); title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        HBox.setHgrow(title, Priority.ALWAYS);
        Button close = new Button("×"); close.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand;");
        close.setOnAction(e -> d.close());
        header.getChildren().addAll(title, close);

        String name  = row.size() > 1 ? row.get(1) : "Student";
        String email = row.size() > 2 ? row.get(2) : "";
        String cls   = row.size() > 3 ? row.get(3) : "";
        String phone = row.size() > 4 ? row.get(4) : "";
        String roll  = row.size() > 0 ? row.get(0) : "";

        StackPane av = new StackPane();
        av.getChildren().addAll(new Circle(35, Color.web("#DBEAFE")),
            lbl(name.isEmpty()?"S":name.substring(0,1), "-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + C_ACCENT + ";"));
        Label status = new Label("Active"); status.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10 3 10;");
        VBox nameInfo = new VBox(4, lbl(name,"-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), lbl("Roll No: "+roll,"-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"), status);
        HBox avatarRow = new HBox(16, av, nameInfo); avatarRow.setAlignment(Pos.CENTER_LEFT);
        VBox panel = makeInfoGrid(new String[][]{
            {"✉  Email",email},{"📞  Phone",phone},{"🏫  Class",cls},{"📅  Admission","2020-08-01"},
            {"👤  Parent","John Johnson"},{"📞  Parent Phone","555-0100"}
        });
        panel.getChildren().add(0, new Separator()); panel.getChildren().add(0, avatarRow);

        root.setTop(header); root.setCenter(panel);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
