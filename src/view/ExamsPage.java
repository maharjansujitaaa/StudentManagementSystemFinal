
package view;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.util.Arrays;

import static view.UIHelper.*;

public class ExamsPage {

    private final AppState state;
    private final Stage owner;

    public ExamsPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build() {
        return padPage(makeTabs(buildExamsTab(), buildGradesTab(), buildReportCardTab()));
    }

    public void showAddDialog(Runnable onDone) {
        Stage d = makeDialog(owner, "Add New Exam", 500, 400);
        BorderPane root = dialogRoot("Add New Exam", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16, 24, 20, 24));
        TextField nameTF = addFormRow(form, "Exam Name *", "");
        TextField dateTF = addFormRow(form, "Date *", "yyyy-mm-dd");
        TextField durTF  = addFormRow(form, "Duration", "e.g. 3 hours");
        TextField mrkTF  = addFormRow(form, "Total Marks", "100");

        ComboBox<String> subjBox = new ComboBox<>();
        subjBox.getItems().addAll(AppState.SUBJECTS);
        subjBox.setValue("Mathematics"); subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject *"), subjBox);

        ComboBox<String> clsBox = new ComboBox<>();
        for (String cls : AppState.CLASS_NAMES) clsBox.getItems().add(cls);
        clsBox.setValue("Class 1-A"); clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Class *"), clsBox);

        Button save = makePrimaryBtn("Save Exam");
        save.setOnAction(e -> {
            if (nameTF.getText().trim().isEmpty()) { showAlert("Exam name is required."); return; }
            state.upcomingExams.add(new Object[]{
                nameTF.getText().trim(), subjBox.getValue(), clsBox.getValue(),
                dateTF.getText().trim(), durTF.getText().trim(),
                mrkTF.getText().trim().isEmpty() ? "100" : mrkTF.getText().trim(), "Scheduled"
            });
            showAlert("Exam added!"); d.close();
            if (onDone != null) onDone.run();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Exams Tab ────────────────────────────────────────────────────────
    private Tab buildExamsTab() {
        Tab tab = new Tab("Exams");
        VBox content = new VBox(16); content.setPadding(new Insets(16, 0, 0, 0)); content.setStyle("-fx-background-color:" + C_BG + ";");

        // Upcoming
        VBox upCard = new VBox(10); upCard.setPadding(new Insets(16));
        upCard.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        Label upTitle = new Label("Upcoming Exams"); upTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        TableView<ObservableList<String>> upTable = buildExamTable(true);
        ObservableList<ObservableList<String>> upData = FXCollections.observableArrayList();
        for (Object[] e : state.upcomingExams) upData.add(FXCollections.observableArrayList(Arrays.stream(e).map(Object::toString).toArray(String[]::new)));
        upTable.setItems(upData); upTable.setPrefHeight(160);
        upCard.getChildren().addAll(upTitle, upTable);

        // Completed
        VBox compCard = new VBox(10); compCard.setPadding(new Insets(16));
        compCard.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        Label compTitle = new Label("Completed Exams"); compTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        TableView<ObservableList<String>> compTable = buildExamTable(false);
        ObservableList<ObservableList<String>> compData = FXCollections.observableArrayList();
        for (Object[] e : state.completedExams) compData.add(FXCollections.observableArrayList(Arrays.stream(e).map(Object::toString).toArray(String[]::new)));
        compTable.setItems(compData); compTable.setPrefHeight(160);
        compCard.getChildren().addAll(compTitle, compTable);

        content.getChildren().addAll(upCard, compCard);
        tab.setContent(bgScroll(content)); return tab;
    }

    private TableView<ObservableList<String>> buildExamTable(boolean upcoming) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(48); return row; });

        String[] cols  = upcoming ? new String[]{"Exam Name","Subject","Class","Date","Duration","Total Marks","Status"} : new String[]{"Exam Name","Subject","Class","Date","Status"};
        int[]    widths = upcoming ? new int[]{160,120,110,110,90,100,100} : new int[]{160,120,110,110,100};
        for (int i = 0; i < cols.length; i++) {
            final int fi = i; int w = widths[i];
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(w);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size() > fi ? d.getValue().get(fi) : ""));
            int statusIdx = upcoming ? 6 : 4;
            if (i == statusIdx) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    String bg = item.equals("Scheduled") ? C_TEXT : "#F1F5F9";
                    String fg = item.equals("Scheduled") ? "white" : C_MUTED;
                    l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 12 4 12;");
                    setGraphic(l);
                }
            });
            tv.getColumns().add(col);
        }

        TableColumn<ObservableList<String>, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(140);
        actCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                int rowIdx = getIndex();
                if (rowIdx < 0 || rowIdx >= getTableView().getItems().size()) { setGraphic(null); return; }
                HBox box = new HBox(8); box.setAlignment(Pos.CENTER);
                if (!upcoming) {
                    Button eb = new Button("Enter Marks");
                    eb.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:5 12 5 12;");
                    eb.setOnAction(e -> showEnterMarksDialog(getTableView().getItems().get(rowIdx).get(0)));
                    box.getChildren().add(eb);
                } else {
                    Button eb = new Button("✏"); eb.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:5 10 5 10;");
                    Button db = new Button("🗑"); db.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:" + C_RED + ";");
                    eb.setOnAction(e -> showEditExamDialog(getTableView().getItems().get(rowIdx), rowIdx, getTableView().getItems()));
                    db.setOnAction(e -> { if (confirmDelete()) { if (rowIdx < state.upcomingExams.size()) state.upcomingExams.remove(rowIdx); getTableView().getItems().remove(rowIdx); } });
                    box.getChildren().addAll(eb, db);
                }
                setGraphic(box);
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    // ── Grades Tab ───────────────────────────────────────────────────────
    private Tab buildGradesTab() {
        Tab tab = new Tab("Grades");
        TableView<ObservableList<String>> tv = buildSimpleTable(
            new String[]{"Student","Exam","Marks","Total","Percentage","Grade","Remarks"},
            new int[]{160,130,110,100,100,80,80}, new String[0][]);
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(44); return row; });

        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>, String> gradeCol = (TableColumn<ObservableList<String>, String>) tv.getColumns().get(5);
        gradeCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:" + (item.startsWith("A") ? C_GREEN : item.startsWith("B") ? C_BLUE : C_ORANGE) + ";");
                setGraphic(l);
            }
        });

        ObservableList<ObservableList<String>> gData = FXCollections.observableArrayList();
        for (Object[] g : state.gradesData)
            gData.add(FXCollections.observableArrayList(Arrays.stream(g).map(Object::toString).toArray(String[]::new)));
        tv.setItems(gData);

        VBox content = new VBox(10); content.setPadding(new Insets(16, 0, 0, 0));
        VBox card = new VBox(10); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        Label title = new Label("Student Grades (Live Data)"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        card.getChildren().addAll(title, tv);
        content.getChildren().add(card);
        tab.setContent(bgScroll(content)); return tab;
    }

    // ── Report Cards Tab ─────────────────────────────────────────────────
    private Tab buildReportCardTab() {
        Tab tab = new Tab("Report Cards");
        VBox content = new VBox(16); content.setPadding(new Insets(16, 0, 0, 0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        VBox card = new VBox(16); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        Label title = new Label("Generate Report Card"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");

        HBox selRow = new HBox(12); selRow.setAlignment(Pos.CENTER_LEFT);
        Label sl = new Label("Select Student:"); sl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        ComboBox<String> studBox = new ComboBox<>();
        studBox.getItems().add("Select student");
        for (String[] s : state.studentList) studBox.getItems().add(s[1]);
        studBox.setValue("Select student"); studBox.setPrefHeight(36);
        ComboBox<String> termBox = makeCombo("Term 1","Term 1","Term 2","Term 3","Full Year");
        termBox.setPrefHeight(36);
        Button genBtn = makePrimaryBtn("Generate");
        selRow.getChildren().addAll(sl, studBox, termBox, genBtn);

        VBox preview = new VBox(10); preview.setPadding(new Insets(20));
        preview.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8;");
        preview.setMinHeight(200);
        Label ph = new Label("Select a student and click Generate to view report card");
        ph.setStyle("-fx-font-size:13px; -fx-text-fill:" + C_MUTED + ";");
        preview.getChildren().add(ph);

        genBtn.setOnAction(e -> {
            if (studBox.getValue().equals("Select student")) { showAlert("Please select a student."); return; }
            String student = studBox.getValue(), term = termBox.getValue();
            preview.getChildren().clear();
            Label rc = new Label("REPORT CARD — " + student); rc.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
            Label tl = new Label(term + " | EduManage School"); tl.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
            TableView<ObservableList<String>> rcTable = buildSimpleTable(
                new String[]{"Exam","Marks","Total","Grade"}, new int[]{140,80,80,80}, new String[0][]);
            ObservableList<ObservableList<String>> rcData = FXCollections.observableArrayList();
            for (Object[] g : state.gradesData)
                if (g[0].toString().equals(student))
                    rcData.add(FXCollections.observableArrayList(g[1].toString(), g[2].toString(), g[3].toString(), g[5].toString()));
            if (rcData.isEmpty()) rcData.add(FXCollections.observableArrayList("No data","—","—","—"));
            rcTable.setItems(rcData); rcTable.setPrefHeight(160);
            preview.getChildren().addAll(rc, tl, new Separator(), rcTable);
        });

        card.getChildren().addAll(title, selRow, preview);
        content.getChildren().add(card);
        tab.setContent(bgScroll(content)); return tab;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────
    private void showEnterMarksDialog(String examName) {
        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Enter Marks — " + examName); d.setWidth(620); d.setHeight(480);
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setStyle("-fx-background-color:white;");
        root.setTop(makeDialogHeader("Enter Marks — " + examName, d));

        java.util.Map<String, String> existing = new java.util.HashMap<>();
        for (Object[] g : state.gradesData) if (g[1].toString().equals(examName)) existing.put(g[0].toString(), g[2].toString());

        String[] students = state.studentList.stream().map(s -> s[1]).toArray(String[]::new);
        if (students.length == 0) students = new String[]{"Emma Johnson","Liam Smith","Olivia Brown","Noah Davis","Sophia Wilson"};

        VBox formBody = new VBox(0); formBody.setStyle("-fx-background-color:white;"); formBody.setPadding(new Insets(8,24,8,24));
        HBox hdr = new HBox(0); hdr.setPadding(new Insets(8,0,8,0));
        hdr.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 1 0;");
        String[] hNames = {"Student","Marks Obtained","Total","Percentage","Grade"};
        int[] hW = {200,130,80,110,80};
        for (int i = 0; i < hNames.length; i++) {
            Label hl = new Label(hNames[i]); hl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + C_MUTED + "; -fx-padding:0 0 0 8;");
            hl.setMinWidth(hW[i]); hl.setPrefWidth(hW[i]); hdr.getChildren().add(hl);
        }
        formBody.getChildren().add(hdr);

        java.util.List<TextField> markFields = new java.util.ArrayList<>();
        java.util.List<Label[]>   resultLbls = new java.util.ArrayList<>();
        for (String student : students) {
            HBox row = new HBox(0); row.setPadding(new Insets(10,0,10,0)); row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color:" + C_BORDER + "; -fx-border-width:0 0 1 0;");
            Label nameLbl = new Label(student); nameLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + "; -fx-padding:0 0 0 8;");
            nameLbl.setMinWidth(200); nameLbl.setPrefWidth(200);
            TextField tf = new TextField(existing.getOrDefault(student,"")); tf.setPrefHeight(36); tf.setMinWidth(120); tf.setPrefWidth(120);
            tf.setStyle("-fx-background-color:#FFFBEB; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            HBox.setMargin(tf, new Insets(0,8,0,8));
            Label totalLbl = lbl("100","-fx-font-size:13px; -fx-text-fill:" + C_MUTED + "; -fx-padding:0 0 0 8;");
            totalLbl.setMinWidth(72); totalLbl.setPrefWidth(72);
            Label pctLbl = lbl("-","-fx-font-size:13px; -fx-text-fill:" + C_MUTED + "; -fx-padding:0 0 0 8;");
            pctLbl.setMinWidth(102); pctLbl.setPrefWidth(102);
            Label gradeLbl = lbl("-","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_GREEN + "; -fx-padding:0 0 0 8;");
            gradeLbl.setMinWidth(72); gradeLbl.setPrefWidth(72);
            tf.textProperty().addListener((obs, oldV, newV) -> {
                try {
                    int m = Integer.parseInt(newV.trim());
                    double pct = m * 100.0 / 100;
                    String grade = pct>=90?"A+":pct>=80?"A":pct>=70?"B+":pct>=60?"B":pct>=50?"C":"F";
                    pctLbl.setText(String.format("%.1f%%",pct)); gradeLbl.setText(grade);
                    String col = grade.startsWith("A")?C_GREEN:grade.startsWith("B")?C_BLUE:C_ORANGE;
                    gradeLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+col+"; -fx-padding:0 0 0 8;");
                } catch (NumberFormatException ex) {
                    pctLbl.setText("-"); gradeLbl.setText("-");
                    gradeLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_MUTED+"; -fx-padding:0 0 0 8;");
                }
            });
            row.getChildren().addAll(nameLbl, tf, totalLbl, pctLbl, gradeLbl);
            formBody.getChildren().add(row);
            markFields.add(tf); resultLbls.add(new Label[]{pctLbl,gradeLbl});
        }
        ScrollPane sp = new ScrollPane(formBody); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:white; -fx-background:white;");

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(12,24,12,24));
        btns.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        Button save   = makePrimaryBtn("Save Marks");
        final String[] finalStudents = students;
        save.setOnAction(e -> {
            int saved = 0;
            for (int i = 0; i < finalStudents.length; i++) {
                String marks = markFields.get(i).getText().trim(); if (marks.isEmpty()) continue;
                try { Integer.parseInt(marks); } catch (NumberFormatException ex) { showAlert("Invalid marks for " + finalStudents[i]); return; }
                final String sn = finalStudents[i], en = examName;
                state.gradesData.removeIf(g -> g[0].toString().equals(sn) && g[1].toString().equals(en));
                String pct   = resultLbls.get(i)[0].getText().equals("-") ? "0%" : resultLbls.get(i)[0].getText();
                String grade = resultLbls.get(i)[1].getText().equals("-") ? "F"  : resultLbls.get(i)[1].getText();
                state.gradesData.add(new Object[]{sn, en, marks, "100", pct, grade, "-"});
                saved++;
            }
            showAlert(saved + " student mark(s) saved!"); d.close();
        });
        btns.getChildren().addAll(cancel, save);
        root.setCenter(sp); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private void showEditExamDialog(ObservableList<String> row, int idx, ObservableList<ObservableList<String>> allData) {
        Stage d = makeDialog(owner, "Edit Exam", 500, 420);
        BorderPane root = dialogRoot("Edit Exam", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF = addFormRow(form,"Exam Name *", row.size()>0?row.get(0):"");
        TextField dateTF = addFormRow(form,"Date *",      row.size()>3?row.get(3):"");
        TextField durTF  = addFormRow(form,"Duration",    row.size()>4?row.get(4):"");
        TextField mrkTF  = addFormRow(form,"Total Marks", row.size()>5?row.get(5):"100");
        ComboBox<String> subjBox = new ComboBox<>(); subjBox.getItems().addAll(AppState.SUBJECTS);
        String curSubj = row.size()>1?row.get(1):"Mathematics";
        subjBox.setValue(subjBox.getItems().contains(curSubj)?curSubj:"Mathematics");
        subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> clsBox = new ComboBox<>();
        for (String cls : AppState.CLASS_NAMES) clsBox.getItems().add(cls);
        String curCls = row.size()>2?row.get(2):"Class 1-A";
        clsBox.setValue(clsBox.getItems().contains(curCls)?curCls:"Class 1-A");
        clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject"), subjBox, boldLabel("Class"), clsBox);
        Button save = makePrimaryBtn("Update Exam");
        save.setOnAction(e -> {
            if (nameTF.getText().trim().isEmpty()) { showAlert("Exam name required."); return; }
            String[] v = {nameTF.getText().trim(), subjBox.getValue(), clsBox.getValue(), dateTF.getText().trim(), durTF.getText().trim(), mrkTF.getText().trim()};
            for (int i = 0; i < v.length && i < row.size(); i++) row.set(i, v[i]);
            allData.set(idx, row);
            if (idx < state.upcomingExams.size()) for (int i = 0; i < v.length && i < state.upcomingExams.get(idx).length; i++) state.upcomingExams.get(idx)[i] = v[i];
            showAlert("Exam updated!"); d.close();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
