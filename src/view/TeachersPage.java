
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

public class TeachersPage {

    private final AppState state;
    private final Stage    owner;

    public TeachersPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build() {
        BorderPane page = new BorderPane();
        page.setPadding(new Insets(20, 28, 20, 28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        HBox searchRow = new HBox(12);
        searchRow.setPadding(new Insets(12, 16, 12, 16));
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");

        TextField search = new TextField();
        search.setPromptText("Search teachers...");
        search.setPrefHeight(36);
        search.setStyle("-fx-font-size:13px; -fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6;");
        HBox.setHgrow(search, Priority.ALWAYS);

        ComboBox<String> subFilter = new ComboBox<>();
        subFilter.getItems().add("All Subjects");
        for (String s : AppState.SUBJECTS) subFilter.getItems().add(s);
        subFilter.setValue("All Subjects");
        subFilter.setPrefHeight(36);
        searchRow.getChildren().addAll(search, subFilter);

        // Capture as final for use inside lambdas
        final ObservableList<ObservableList<String>> allData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> table = buildTable(allData);
        loadRows(allData, "", "All Subjects");
        table.setItems(allData);

        search.textProperty().addListener((obs, o, n) -> { allData.clear(); loadRows(allData, n.toLowerCase(), subFilter.getValue()); });
        subFilter.setOnAction(e -> { allData.clear(); loadRows(allData, search.getText().toLowerCase(), subFilter.getValue()); });

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8;");
        tableCard.getChildren().addAll(searchRow, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        page.setCenter(tableCard);
        return page;
    }

    public void showAddDialog(Runnable onDone) {
        Stage d = makeDialog(owner, "Add New Teacher", 520, 500);
        BorderPane root = dialogRoot("Add New Teacher", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16, 24, 20, 24));
        TextField fnTF   = addFormRow(form, "Full Name *", "");
        TextField emTF   = addFormRow(form, "Email *", "");
        TextField phTF   = addFormRow(form, "Phone *", "");
        TextField unTF   = addFormRow(form, "Username *", "");
        TextField qualTF = addFormRow(form, "Qualification", "");
        TextField expTF  = addFormRow(form, "Experience (years)", "");
        PasswordField pwTF = new PasswordField(); pwTF.setPrefHeight(38);
        pwTF.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-font-size:13px;");
        ComboBox<String> subBox = new ComboBox<>();
        subBox.getItems().add("Select Subject");
        for (String s : AppState.SUBJECTS) subBox.getItems().add(s);
        subBox.setValue("Mathematics"); subBox.setPrefHeight(38); subBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Password *"), pwTF, boldLabel("Subject *"), subBox);
        Button save   = makePrimaryBtn("Save Teacher");
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        save.setOnAction(e -> {
            String fn = fnTF.getText().trim(), em = emTF.getText().trim();
            if (fn.isEmpty() || em.isEmpty()) { showAlert("Required fields missing."); return; }
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "INSERT INTO users(full_name,email,username,password,role) VALUES(?,?,?,?,?)");
                ps.setString(1,fn); ps.setString(2,em); ps.setString(3,unTF.getText().trim().toLowerCase());
                ps.setString(4,pwTF.getText().trim()); ps.setString(5,"TEACHER"); ps.executeUpdate();
            } catch (SQLException ex) { /* ignore */ }
            state.teacherList.add(new String[]{fn, em, subBox.getValue(), qualTF.getText().trim(), expTF.getText().trim()+" years", "Active"});
            showAlert("Teacher added!"); d.close();
            if (onDone != null) onDone.run();
        });
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new Scene(root)); d.show();
    }

    private TableView<ObservableList<String>> buildTable(final ObservableList<ObservableList<String>> allData) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(52); return row; });

        String[] cols  = {"Name","Email","Subject","Qualification","Experience","Status"};
        int[]    widths = {170, 190, 130, 170, 100, 90};
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size() > fi ? d.getValue().get(fi) : ""));
            if (i == 5) {
                col.setCellFactory(c -> new TableCell<ObservableList<String>, String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); return; }
                        Label l = new Label(item);
                        l.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 12 4 12;");
                        setGraphic(l);
                    }
                });
            }
            tv.getColumns().add(col);
        }

        // Capture these as effectively-final locals so inner class lambdas can access them
        final AppState capturedState = this.state;
        final Stage capturedOwner   = this.owner;

        TableColumn<ObservableList<String>, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(190);
        actCol.setCellFactory(c -> new TableCell<ObservableList<String>, Void>() {
            final Button vb = new Button("👁 View");
            final Button eb = new Button("✏ Edit");
            final Button db = new Button("🗑");
            {
                String bs = "-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-font-size:11px; -fx-cursor:hand; -fx-padding:5 8 5 8;";
                vb.setStyle(bs); eb.setStyle(bs);
                db.setStyle("-fx-background-color:transparent; -fx-font-size:14px; -fx-cursor:hand; -fx-text-fill:" + C_RED + ";");
                vb.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size())
                        showTeacherDetails(getTableView().getItems().get(idx), capturedOwner);
                });
                eb.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size())
                        showEditDialog(getTableView().getItems().get(idx), idx, allData, capturedState, capturedOwner);
                });
                db.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    if (confirmDelete()) {
                        getTableView().getItems().remove(idx);
                        if (idx < capturedState.teacherList.size()) capturedState.teacherList.remove(idx);
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else { HBox b = new HBox(6, vb, eb, db); b.setAlignment(Pos.CENTER); setGraphic(b); }
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    private void loadRows(ObservableList<ObservableList<String>> list, String search, String subFilter) {
        for (String[] r : state.teacherList) {
            boolean ms = search.isEmpty() || r[0].toLowerCase().contains(search) || r[1].toLowerCase().contains(search) || r[2].toLowerCase().contains(search);
            boolean mf = subFilter.equals("All Subjects") || r[2].equals(subFilter);
            if (ms && mf) list.add(FXCollections.observableArrayList(r));
        }
        try {
            ResultSet rs = DatabaseConnection.getConnection().prepareStatement(
                "SELECT full_name,email FROM users WHERE role='TEACHER' ORDER BY id").executeQuery();
            while (rs.next()) {
                String n = rs.getString(1), em = rs.getString(2);
                boolean already = state.teacherList.stream().anyMatch(t -> t[1].equals(em));
                if (already) continue;
                boolean ms = search.isEmpty() || n.toLowerCase().contains(search) || em.toLowerCase().contains(search);
                if (ms && subFilter.equals("All Subjects"))
                    list.add(FXCollections.observableArrayList(n, em, "General", "N/A", "N/A", "Active"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void showTeacherDetails(ObservableList<String> row, Stage dlgOwner) {
        Stage d = makeDialog(dlgOwner, "Teacher Details", 500, 400);
        BorderPane root = dialogRoot("Teacher Details", d);
        String n = row.size() > 0 ? row.get(0) : "T";
        StackPane av = new StackPane();
        av.getChildren().addAll(new Circle(32, Color.web("#DBEAFE")),
            lbl(n.substring(0,1).toUpperCase(), "-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:" + C_ACCENT + ";"));
        Label badge = new Label("Active");
        badge.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10 3 10;");
        VBox nameBox = new VBox(4,
            lbl(n, "-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";"),
            lbl(row.size() > 2 ? row.get(2) : "", "-fx-font-size:13px; -fx-text-fill:" + C_MUTED + ";"),
            badge);
        HBox hdr = new HBox(16, av, nameBox);
        hdr.setPadding(new Insets(20,24,16,24)); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
        VBox grid = makeInfoGrid(new String[][]{
            {"✉  Email",          row.size()>1?row.get(1):"N/A"},
            {"📚  Subject",       row.size()>2?row.get(2):"N/A"},
            {"🎓  Qualification", row.size()>3?row.get(3):"N/A"},
            {"⏱  Experience",    row.size()>4?row.get(4):"N/A"},
            {"📊  Status",        row.size()>5?row.get(5):"Active"},
            {"📞  Phone",         "N/A"}
        });
        Button close = makeSecondaryBtn("Close"); close.setOnAction(e -> d.close());
        HBox foot = new HBox(close); foot.setAlignment(Pos.CENTER_RIGHT); foot.setPadding(new Insets(0,24,16,24));
        root.setCenter(new VBox(hdr, grid, foot));
        d.setScene(new Scene(root)); d.show();
    }

    private void showEditDialog(ObservableList<String> row, int idx,
            ObservableList<ObservableList<String>> allData, AppState st, Stage dlgOwner) {
        Stage d = makeDialog(dlgOwner, "Edit Teacher", 520, 520);
        BorderPane root = dialogRoot("Edit Teacher", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF  = addFormRow(form,"Full Name",     row.size()>0?row.get(0):"");
        TextField emailTF = addFormRow(form,"Email",         row.size()>1?row.get(1):"");
        TextField qualTF  = addFormRow(form,"Qualification", row.size()>3?row.get(3):"");
        TextField expTF   = addFormRow(form,"Experience",    row.size()>4?row.get(4):"");
        ComboBox<String> subjBox = new ComboBox<>();
        subjBox.getItems().addAll(AppState.SUBJECTS);
        String curSubj = row.size()>2?row.get(2):"Mathematics";
        subjBox.setValue(subjBox.getItems().contains(curSubj)?curSubj:"Mathematics");
        subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> statBox = makeCombo(row.size()>5?row.get(5):"Active","Active","Inactive","On Leave");
        form.getChildren().addAll(boldLabel("Subject"),subjBox,boldLabel("Status"),statBox);
        Button save = makePrimaryBtn("Update Teacher");
        save.setOnAction(e -> {
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE users SET full_name=?,email=? WHERE email=? AND role='TEACHER'");
                ps.setString(1,nameTF.getText().trim()); ps.setString(2,emailTF.getText().trim());
                ps.setString(3,row.size()>1?row.get(1):""); ps.executeUpdate();
            } catch (SQLException ex) { /* ignore */ }
            String[] v = {nameTF.getText().trim(), emailTF.getText().trim(),
                subjBox.getValue(), qualTF.getText().trim(), expTF.getText().trim(), statBox.getValue()};
            for (int i = 0; i < v.length && i < row.size(); i++) row.set(i, v[i]);
            allData.set(idx, row);
            if (idx < st.teacherList.size()) st.teacherList.set(idx, v);
            showAlert("Teacher updated!"); d.close();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new Scene(root)); d.show();
    }
}