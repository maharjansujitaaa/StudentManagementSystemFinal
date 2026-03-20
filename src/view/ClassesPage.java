
package view;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.util.*;

import static view.UIHelper.*;

public class ClassesPage {

    private final AppState state;
    private final Stage owner;

    public ClassesPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + C_BG + "; -fx-background:" + C_BG + ";");

        FlowPane grid = new FlowPane(16, 16);
        grid.setPadding(new Insets(20, 28, 20, 28));
        grid.setStyle("-fx-background-color:" + C_BG + ";");

        for (String[] c : state.classDataList)
            grid.getChildren().add(buildCard(c, grid));

        scroll.setContent(grid);
        return scroll;
    }

    public void showAddDialog(Runnable onDone) {
        Stage d = makeDialog(owner, "Add New Class", 480, 400);
        BorderPane root = dialogRoot("Add New Class", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16, 24, 20, 24));

        TextField idTF   = addFormRow(form, "Class ID *", "e.g. C7");
        TextField nameTF = addFormRow(form, "Class Name *", "e.g. Class 5 - C");
        TextField studTF = addFormRow(form, "No. of Students", "");
        TextField subjTF = addFormRow(form, "Subjects (comma separated)", "Math,Physics,...");

        // Teacher combo from state
        ComboBox<String> tchrBox = new ComboBox<>();
        tchrBox.getItems().add("Select Teacher");
        for (String[] t : state.teacherList) tchrBox.getItems().add(t[0]);
        for (String t : AppState.TEACHERS) if (!tchrBox.getItems().contains(t)) tchrBox.getItems().add(t);
        tchrBox.setValue("Select Teacher");
        tchrBox.setPrefHeight(38); tchrBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Class Teacher *"), tchrBox);

        Button save = makePrimaryBtn("Save Class");
        save.setOnAction(e -> {
            String cn = nameTF.getText().trim();
            if (cn.isEmpty()) { showAlert("Class name required."); return; }
            String nid = idTF.getText().trim().isEmpty() ? "C" + (state.classDataList.size() + 1) : idTF.getText().trim();
            state.classDataList.add(new String[]{
                nid, cn,
                tchrBox.getValue() != null && !tchrBox.getValue().equals("Select Teacher") ? tchrBox.getValue() : "TBD",
                studTF.getText().trim().isEmpty() ? "0" : studTF.getText().trim(),
                subjTF.getText().trim().isEmpty() ? "General" : subjTF.getText().trim()
            });
            showAlert("Class added!"); d.close();
            if (onDone != null) onDone.run();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private VBox buildCard(String[] data, FlowPane parent) {
        String id       = data[0];
        String name     = data[1];
        String teacher  = data[2];
        String students = data[3];
        // subjects — split by comma, show chips
        String[] subjects = data[4].split(",");

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:12; -fx-background-radius:12;");

        // Top row: icon + name + edit/delete
        HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
        StackPane icon = new StackPane();
        icon.getChildren().addAll(new Circle(22, Color.web("#DBEAFE")), lbl("📖", "-fx-font-size:18px;"));
        VBox nameBox = new VBox(2); HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nl = new Label(name); nl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        Label idl = new Label("ID: " + id); idl.setStyle("-fx-font-size:11px; -fx-text-fill:" + C_MUTED + ";");
        nameBox.getChildren().addAll(nl, idl);

        Button eb = new Button("✏"); eb.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:" + C_MUTED + ";");
        Button db = new Button("🗑"); db.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:" + C_RED + ";");
        eb.setOnAction(e -> showEditDialog(data, parent));
        db.setOnAction(e -> { if (confirmDelete()) { state.classDataList.remove(data); parent.getChildren().remove(card); } });
        topRow.getChildren().addAll(icon, nameBox, new HBox(8, eb, db));

        Label tl = new Label("🎓  " + teacher); tl.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
        Label sl = new Label("👥  " + students + " Students"); sl.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
        Label subTitle = new Label("Subjects:"); subTitle.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");

        // Subject chips — show current subjects, update when data[4] changes
        FlowPane chips = new FlowPane(6, 4);
        Runnable refreshChips = () -> {
            chips.getChildren().clear();
            for (String s : data[4].split(",")) {
                Label chip = new Label(s.trim());
                chip.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:" + C_TEXT + "; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:4 10 4 10; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6;");
                chips.getChildren().add(chip);
            }
        };
        refreshChips.run();

        // Store refresh reference so edit dialog can call it
        Button scheduleBtn = new Button("📅  View / Edit Schedule");
        scheduleBtn.setPrefWidth(Double.MAX_VALUE);
        scheduleBtn.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:8 0 8 0;");
        scheduleBtn.setOnAction(e -> showSchedule(id, name, teacher));

        card.getChildren().addAll(topRow, tl, sl, subTitle, chips, scheduleBtn);

        // When edit updates data[], also refresh chips on card
        eb.setOnAction(e -> showEditDialog(data, parent, nl, tl, sl, chips, refreshChips));

        return card;
    }

    private void showEditDialog(String[] data, FlowPane parent) {
        showEditDialog(data, parent, null, null, null, null, null);
    }

    private void showEditDialog(String[] data, FlowPane parent,
                                Label cardNameLbl, Label cardTeacherLbl, Label cardStudentsLbl,
                                FlowPane chips, Runnable refreshChips) {
        Stage d = makeDialog(owner, "Edit Class", 500, 440);
        BorderPane root = dialogRoot("Edit Class — " + data[1], d);
        VBox form = new VBox(10); form.setPadding(new Insets(16, 24, 20, 24));

        TextField idTF   = addFormRow(form, "Class ID",          data[0]);
        TextField nameTF = addFormRow(form, "Class Name",        data[1]);
        TextField studTF = addFormRow(form, "No. of Students",   data[3]);
        TextField subjTF = addFormRow(form, "Subjects (comma separated)", data[4]);

        ComboBox<String> tchrBox = new ComboBox<>();
        tchrBox.getItems().add("Select Teacher");
        for (String[] t : state.teacherList) tchrBox.getItems().add(t[0]);
        for (String t : AppState.TEACHERS) if (!tchrBox.getItems().contains(t)) tchrBox.getItems().add(t);
        tchrBox.setValue(data[2]);
        tchrBox.setPrefHeight(38); tchrBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Class Teacher"), tchrBox);

        Button save = makePrimaryBtn("Update Class");
        save.setOnAction(e -> {
            // Update data[] in place so the card and classDataList both reflect the change
            data[0] = idTF.getText().trim();
            data[1] = nameTF.getText().trim();
            data[2] = tchrBox.getValue();
            data[3] = studTF.getText().trim();
            data[4] = subjTF.getText().trim();
            // Update card labels
            if (cardNameLbl    != null) cardNameLbl.setText(data[1]);
            if (cardTeacherLbl != null) cardTeacherLbl.setText("🎓  " + data[2]);
            if (cardStudentsLbl!= null) cardStudentsLbl.setText("👥  " + data[3] + " Students");
            if (refreshChips   != null) refreshChips.run();
            showAlert("Class updated!"); d.close();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private void showSchedule(String classId, String className, String teacher) {
        Stage d = new Stage();
        d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(className + " - Schedule"); d.setWidth(700); d.setHeight(500);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");
        root.setTop(makeDialogHeader(className + " — Weekly Schedule (double-click to edit)", d));

        // Ensure schedule exists
        if (!state.classSchedules.containsKey(classId)) {
            state.classSchedules.put(classId, new String[][]{
                {"Monday",   "Math",    "Physics", "Chem",    "English","Bio"},
                {"Tuesday",  "Physics", "Math",    "English", "Bio",    "Chem"},
                {"Wednesday","Chem",    "English", "Math",    "Physics","Bio"},
                {"Thursday", "English", "Bio",     "Physics", "Chem",   "Math"},
                {"Friday",   "Bio",     "Chem",    "Math",    "Physics","English"}
            });
        }
        String[][] sched = state.classSchedules.get(classId);

        String[] cols  = {"Day","Period 1","Period 2","Period 3","Period 4","Period 5"};
        int[]    widths = {110, 108, 108, 108, 108, 108};

        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        for (String[] row : sched) items.add(FXCollections.observableArrayList(row));

        TableView<ObservableList<String>> tv = new TableView<>(items);
        tv.setEditable(true);
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(48); return row; });

        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]); col.setSortable(false);
            col.setCellValueFactory(dd -> new javafx.beans.property.SimpleStringProperty(
                dd.getValue().size() > fi ? dd.getValue().get(fi) : ""));
            if (i == 0) {
                col.setCellFactory(c -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        setStyle("-fx-font-weight:bold; -fx-text-fill:" + C_TEXT + "; -fx-background-color:#F8F9FA;");
                    }
                });
            } else {
                col.setEditable(true);
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(ev -> {
                    ev.getRowValue().set(fi, ev.getNewValue());
                    int rowIdx = items.indexOf(ev.getRowValue());
                    if (rowIdx >= 0 && rowIdx < sched.length && fi < sched[rowIdx].length)
                        sched[rowIdx][fi] = ev.getNewValue();
                });
            }
            tv.getColumns().add(col);
        }

        Label hint = new Label("💡 Double-click any period cell to edit it");
        hint.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + "; -fx-padding:0 0 0 24;");

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT);
        btns.setPadding(new Insets(12, 24, 12, 24));
        btns.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");
        Button cancel = makeSecondaryBtn("Close"); cancel.setOnAction(e -> d.close());
        Button save = makePrimaryBtn("Save Schedule");
        save.setOnAction(e -> {
            tv.getSelectionModel().clearSelection();
            for (int i = 0; i < items.size() && i < sched.length; i++)
                for (int j = 0; j < items.get(i).size() && j < sched[i].length; j++)
                    sched[i][j] = items.get(i).get(j);
            showAlert("✅ Schedule saved for " + className + "!"); d.close();
        });
        btns.getChildren().addAll(cancel, save);

        VBox body = new VBox(8, hint, tv); body.setPadding(new Insets(8, 0, 0, 0));
        VBox.setVgrow(tv, Priority.ALWAYS);
        root.setCenter(body); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
