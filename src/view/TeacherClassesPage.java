package view;

import controller.TeacherController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import model.TeacherModel;
import java.util.List;

import static view.TeacherUIHelper.*;

public class TeacherClassesPage {

    private final TeacherController controller;
    private final Stage             owner;
    private       Runnable          onRefresh;

    public TeacherClassesPage(TeacherController c, Stage owner) { this.controller=c; this.owner=owner; }
    public void setOnRefresh(Runnable r) { this.onRefresh = r; }

    public Node build() {
        ScrollPane scroll = bgScroll();
        FlowPane grid = new FlowPane(16,16);
        grid.setPadding(new Insets(20,28,20,28));
        grid.setStyle("-fx-background-color:"+C_BG+";");
        buildCards(grid);
        scroll.setContent(grid);
        return scroll;
    }

    // ── Add Class Dialog ───────────────────────────────────────────────────
    public void showAddDialog() {
        Stage d = makeDialog(owner,"Add New Class",500,440);
        BorderPane root = dialogRoot("Add New Class", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF = addFormRow(form,"Class Name *","e.g. Class 5 - A");
        TextField studTF = addFormRow(form,"No. Students","");
        TextField schTF  = addFormRow(form,"Schedule","e.g. Mon/Wed/Fri 9:00");

        ComboBox<String> clsBox = new ComboBox<>();
        for (String cls : TeacherModel.CLASS_NAMES) clsBox.getItems().add(cls);
        clsBox.setValue("Class 1-A"); clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Assign to Class"), clsBox);

        ComboBox<String> subjBox = new ComboBox<>();
        for (String s : TeacherModel.SUBJECTS) subjBox.getItems().add(s);
        subjBox.setValue("Mathematics"); subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject *"), subjBox);

        Button save = makePrimaryBtn("Save Class");
        save.setOnAction(e -> {
            if (nameTF.getText().trim().isEmpty()) { showAlert("Class name is required."); return; }
            controller.addClass(new String[]{controller.nextClassId(), nameTF.getText().trim(),
                subjBox.getValue(), studTF.getText().trim().isEmpty()?"0":studTF.getText().trim(),
                schTF.getText().trim().isEmpty()?"TBD":schTF.getText().trim(), "Active"});
            showAlert("Class added!"); d.close();
            if (onRefresh != null) onRefresh.run();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form));
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Build cards ────────────────────────────────────────────────────────
    private void buildCards(FlowPane grid) {
        grid.getChildren().clear();
        List<String[]> classes = controller.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            final int idx = i;
            grid.getChildren().add(buildCard(classes.get(i), idx, grid));
        }
    }

    private VBox buildCard(String[] data, int idx, FlowPane grid) {
        VBox card = new VBox(12); card.setPrefWidth(320); card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");

        // Top row
        HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
        StackPane icon = new StackPane();
        Rectangle ir = new Rectangle(44,44); ir.setArcWidth(10); ir.setArcHeight(10); ir.setFill(Color.web("#DBEAFE"));
        Label il = new Label("📖"); il.setStyle("-fx-font-size:20px;");
        icon.getChildren().addAll(ir, il);
        VBox nameBox = new VBox(2); HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nl = new Label(data[1]); nl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Label sl = new Label(data[2]); sl.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        nameBox.getChildren().addAll(nl, sl);
        Label badge = new Label(data[5]);
        badge.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 8 3 8;");

        Button editBtn = new Button("✏"); editBtn.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:"+C_MUTED+";");
        Button delBtn  = new Button("🗑"); delBtn.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:"+C_RED+";");
        editBtn.setOnAction(e -> showEditDialog(data, idx, grid, nl, sl));
        delBtn.setOnAction(e -> { if (confirmDelete()) { controller.deleteClass(idx); buildCards(grid); } });
        topRow.getChildren().addAll(icon, nameBox, badge, new HBox(4, editBtn, delBtn));

        HBox i1 = infoRow("👥  Students:", data[3]);
        HBox i2 = infoRow("📅  Schedule:", data[4]);
        HBox i3 = infoRow("🆔  Class ID:", data[0]);

        Label subTitle = new Label("Subject:");
        subTitle.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        FlowPane chips = new FlowPane(6,4);
        for (String s : data[2].split(",")) {
            Label chip = new Label(s.trim());
            chip.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:"+C_TEXT+"; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:4 10 4 10; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6;");
            chips.getChildren().add(chip);
        }

        // Two action buttons: View Details + View/Edit Schedule
        Button viewBtn = new Button("👁  View Class Details");
        viewBtn.setPrefWidth(Double.MAX_VALUE);
        viewBtn.setStyle("-fx-background-color:"+C_SEL+"; -fx-text-fill:"+C_ACCENT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:8 0 8 0;");
        viewBtn.setOnAction(e -> showDetailsDialog(data));

        Button schedBtn = new Button("📅  View / Edit Schedule");
        schedBtn.setPrefWidth(Double.MAX_VALUE);
        schedBtn.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:8 0 8 0;");
        schedBtn.setOnAction(e -> showScheduleDialog(data[0], data[1]));

        card.getChildren().addAll(topRow, new Separator(), i1, i2, i3, subTitle, chips, viewBtn, schedBtn);
        return card;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label(label); lbl2.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); lbl2.setMinWidth(100);
        Label val  = new Label(value); val.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); val.setWrapText(true);
        row.getChildren().addAll(lbl2, val); return row;
    }

    // ── View Details Dialog ────────────────────────────────────────────────
    private void showDetailsDialog(String[] data) {
        Stage d = makeDialog(owner,"Class Details — "+data[1],460,380);
        BorderPane root = dialogRoot("Class Details — "+data[1], d);
        VBox grid = makeInfoGrid(new String[][]{
            {"🆔  Class ID",  data[0]}, {"🏫  Class Name", data[1]},
            {"📚  Subject",   data[2]}, {"👥  Students",   data[3]},
            {"📅  Schedule",  data[4]}, {"📊  Status",     data[5]}
        });
        Button close = makePrimaryBtn("Close"); close.setOnAction(e->d.close());
        HBox foot = new HBox(close); foot.setAlignment(Pos.CENTER_RIGHT); foot.setPadding(new Insets(8,24,16,24));
        root.setCenter(new VBox(grid, foot));
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Edit Class Dialog ──────────────────────────────────────────────────
    private void showEditDialog(String[] data, int idx, FlowPane grid, Label cardNameLbl, Label cardSubjLbl) {
        Stage d = makeDialog(owner,"Edit Class",500,460);
        BorderPane root = dialogRoot("Edit Class — "+data[1], d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField idTF   = addFormRow(form,"Class ID",    data[0]);
        TextField nameTF = addFormRow(form,"Class Name",  data[1]);
        TextField studTF = addFormRow(form,"No. Students",data[3]);
        TextField schTF  = addFormRow(form,"Schedule",    data[4]);

        ComboBox<String> subjBox = new ComboBox<>();
        for (String s : TeacherModel.SUBJECTS) subjBox.getItems().add(s);
        subjBox.setValue(subjBox.getItems().contains(data[2]) ? data[2] : TeacherModel.SUBJECTS[0]);
        subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject"), subjBox);

        ComboBox<String> statBox = makeCombo(data[5],"Active","Inactive");
        form.getChildren().addAll(boldLabel("Status"), statBox);

        Button save = makePrimaryBtn("Update Class");
        save.setOnAction(e -> {
            data[0]=idTF.getText().trim(); data[1]=nameTF.getText().trim();
            data[2]=subjBox.getValue();    data[3]=studTF.getText().trim();
            data[4]=schTF.getText().trim();data[5]=statBox.getValue();
            controller.updateClass(idx, data);
            if (cardNameLbl!=null) cardNameLbl.setText(data[1]);
            if (cardSubjLbl!=null) cardSubjLbl.setText(data[2]);
            showAlert("Class updated!"); d.close(); buildCards(grid);
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form));
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Schedule Dialog (editable timetable) ──────────────────────────────
    private void showScheduleDialog(String classId, String className) {
        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(className + " - Weekly Schedule"); d.setWidth(700); d.setHeight(480);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");

        HBox hdr = new HBox(); hdr.setPadding(new Insets(16,24,12,24)); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        Label tl = new Label(className + " — Weekly Schedule"); tl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(tl,Priority.ALWAYS);
        Button xb = new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand;"); xb.setOnAction(e->d.close());
        hdr.getChildren().addAll(tl,xb); root.setTop(hdr);

        String[][] sched = controller.getSchedule(classId);

        String[] cols  = {"Day","Period 1","Period 2","Period 3","Period 4","Period 5"};
        int[]    widths = {110,108,108,108,108,108};

        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        for (String[] row : sched) items.add(FXCollections.observableArrayList(row));

        TableView<ObservableList<String>> tv = new TableView<>(items);
        tv.setEditable(true);
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(48); return row; });

        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]); col.setSortable(false);
            col.setCellValueFactory(dd -> new javafx.beans.property.SimpleStringProperty(
                dd.getValue().size()>fi ? dd.getValue().get(fi) : ""));
            if (i == 0) {
                col.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty||item==null){setText(null);setStyle("");return;}
                        setText(item);
                        setStyle("-fx-font-weight:bold; -fx-text-fill:"+C_TEXT+"; -fx-background-color:#F8F9FA;");
                    }
                });
            } else {
                col.setEditable(true);
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(ev -> {
                    ev.getRowValue().set(fi, ev.getNewValue());
                    int rowIdx = items.indexOf(ev.getRowValue());
                    if (rowIdx>=0 && rowIdx<sched.length && fi<sched[rowIdx].length)
                        sched[rowIdx][fi] = ev.getNewValue();
                });
            }
            tv.getColumns().add(col);
        }

        Label hint = new Label("💡 Double-click any period cell to edit it");
        hint.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+"; -fx-padding:0 0 0 8;");

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT);
        btns.setPadding(new Insets(12,24,12,24));
        btns.setStyle("-fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button close = makeSecondaryBtn("Close"); close.setOnAction(e -> d.close());
        Button save  = makePrimaryBtn("Save Schedule");
        save.setOnAction(e -> {
            tv.getSelectionModel().clearSelection();
            for (int i=0;i<items.size()&&i<sched.length;i++)
                for (int j=0;j<items.get(i).size()&&j<sched[i].length;j++)
                    sched[i][j] = items.get(i).get(j);
            controller.saveSchedule(classId, sched);
            showAlert("✅ Schedule saved for "+className+"!"); d.close();
        });
        btns.getChildren().addAll(close, save);

        VBox body = new VBox(8, hint, tv); body.setPadding(new Insets(8,0,0,0));
        VBox.setVgrow(tv, Priority.ALWAYS);
        root.setCenter(body); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
