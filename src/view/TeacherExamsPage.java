
package view;

import controller.TeacherController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import model.TeacherModel;
import java.util.*;

import static view.TeacherUIHelper.*;

public class TeacherExamsPage {

    private final TeacherController controller;
    private final Stage             owner;

    TeacherExamsPage(TeacherController c, Stage owner) { this.controller=c; this.owner=owner; }

    Node build() { return padPage(makeTabs(examsTab(), gradesTab(), reportCardsTab())); }

    // ── Report Card Quick Dialog ──────────────────────────────────────────
    void showReportCardQuickDialog() {
        Stage d = makeDialog(owner,"Report Card",580,480);
        BorderPane root = dialogRoot("Generate Report Card", d);
        VBox content = new VBox(16); content.setPadding(new Insets(16,24,20,24));
        HBox selRow = new HBox(12); selRow.setAlignment(Pos.CENTER_LEFT);
        Label sl = new Label("Student:"); sl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        ComboBox<String> studBox = new ComboBox<>();
        studBox.getItems().add("Select student");
        for (String[] s : controller.getStudentList()) studBox.getItems().add(s[1]);
        studBox.setValue("Select student"); studBox.setPrefHeight(36);
        ComboBox<String> termBox = makeCombo("Term 1","Term 1","Term 2","Term 3","Full Year"); termBox.setPrefHeight(36);
        Button genBtn = makePrimaryBtn("Generate");
        selRow.getChildren().addAll(sl, studBox, termBox, genBtn);
        VBox preview = new VBox(10); preview.setPadding(new Insets(16));
        preview.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        preview.setMinHeight(250);
        Label ph = new Label("Select a student and click Generate.");
        ph.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"); preview.getChildren().add(ph);
        genBtn.setOnAction(e -> {
            if (studBox.getValue().equals("Select student")) { showAlert("Please select a student."); return; }
            String student = studBox.getValue(), term = termBox.getValue();
            preview.getChildren().clear();
            Label rc = new Label("REPORT CARD — "+student); rc.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            Label tl2 = new Label(term+" | EduManage School"); tl2.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
            javafx.collections.ObservableList<javafx.collections.ObservableList<String>> rcData = javafx.collections.FXCollections.observableArrayList();
            for (String[] g : controller.getGrades()) if (g[0].equals(student)) rcData.add(javafx.collections.FXCollections.observableArrayList(g[1],g[2],g[3],g[5]));
            if (rcData.isEmpty()) rcData.add(javafx.collections.FXCollections.observableArrayList("No grades recorded","—","—","—"));
            TableView<javafx.collections.ObservableList<String>> rcTable = buildSimpleTable(
                new String[]{"Exam","Marks","Total","Grade"},new int[]{200,80,80,80},new String[0][]);
            rcTable.setItems(rcData); rcTable.setPrefHeight(180);
            preview.getChildren().addAll(rc, tl2, new Separator(), rcTable);
        });
        content.getChildren().addAll(selRow, preview);
        Button close = makeSecondaryBtn("Close"); close.setOnAction(e->d.close());
        HBox foot = new HBox(close); foot.setAlignment(Pos.CENTER_RIGHT);
        foot.setPadding(new Insets(8,24,12,24));
        foot.setStyle("-fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        root.setCenter(wrapScroll(content)); root.setBottom(foot);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Add Exam Dialog ───────────────────────────────────────────────────
    void showAddExamDialog(Runnable onDone) {
        Stage d = makeDialog(owner,"Add New Exam",500,400);
        BorderPane root = dialogRoot("Add New Exam", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF = addFormRow(form,"Exam Name *","");
        TextField dateTF = addFormRow(form,"Date (yyyy-mm-dd) *","");
        TextField durTF  = addFormRow(form,"Duration","e.g. 3 hours");
        TextField mrkTF  = addFormRow(form,"Total Marks","100");
        ComboBox<String> subjBox = new ComboBox<>();
        for (String s : TeacherModel.SUBJECTS) subjBox.getItems().add(s);
        subjBox.setValue("Mathematics"); subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject *"), subjBox);
        ComboBox<String> clsBox = new ComboBox<>();
        for (String c : TeacherModel.CLASS_NAMES) clsBox.getItems().add(c);
        clsBox.setValue("Class 1-A"); clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Class *"), clsBox);
        Button save = makePrimaryBtn("Save Exam");
        save.setOnAction(e -> {
            if (nameTF.getText().trim().isEmpty()||dateTF.getText().trim().isEmpty()) { showAlert("Exam name and date are required."); return; }
            controller.addExam(new String[]{nameTF.getText().trim(), subjBox.getValue(), clsBox.getValue(),
                dateTF.getText().trim(), durTF.getText().trim().isEmpty()?"3 hours":durTF.getText().trim(),
                mrkTF.getText().trim().isEmpty()?"100":mrkTF.getText().trim(), "Scheduled"});
            showAlert("Exam added!"); d.close();
            if (onDone != null) onDone.run();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Exams Tab ─────────────────────────────────────────────────────────
    private Tab examsTab() {
        Tab tab = new Tab("Exams");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        // Upcoming Exams card
        VBox upCard = new VBox(10); upCard.setPadding(new Insets(16));
        upCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        Label upTitle = new Label("Upcoming Exams"); upTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        ObservableList<ObservableList<String>> upData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> upTable = buildExamTable(true, upData);
        upTable.setPrefHeight(180);
        refreshExamTable(upData, true);
        upCard.getChildren().addAll(upTitle, upTable);

        // Completed Exams card
        VBox compCard = new VBox(10); compCard.setPadding(new Insets(16));
        compCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        Label compTitle = new Label("Completed Exams"); compTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        ObservableList<ObservableList<String>> compData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> compTable = buildExamTable(false, compData);
        compTable.setPrefHeight(180);
        refreshExamTable(compData, false);
        compCard.getChildren().addAll(compTitle, compTable);

        content.getChildren().addAll(upCard, compCard);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp); return tab;
    }

    private void refreshExamTable(ObservableList<ObservableList<String>> data, boolean upcoming) {
        data.clear();
        for (String[] e : controller.getExams()) {
            // Live status: completed only when marks have been entered
            String liveStatus = controller.getExamStatus(e[0], e[6]);
            boolean isCompleted = liveStatus.equals("Completed");
            if (upcoming && isCompleted) continue;
            if (!upcoming && !isCompleted) continue;
            // Build row with live status
            ObservableList<String> row = FXCollections.observableArrayList(e);
            row.set(6, liveStatus);
            data.add(row);
        }
    }

    private TableView<ObservableList<String>> buildExamTable(boolean upcoming,
            ObservableList<ObservableList<String>> data) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(50); return row; });

        String[] cols  = {"Exam Name","Subject","Class","Date","Duration","Total Marks","Status"};
        int[]    widths = {160,120,100,110,90,100,110};
        for (int i=0;i<cols.length;i++) {
            final int fi=i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(dd -> new javafx.beans.property.SimpleStringProperty(dd.getValue().size()>fi?dd.getValue().get(fi):""));
            // Class chip on col 2
            if (i==2) col.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    Label l=new Label(item); l.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:"+C_TEXT+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10 3 10;"); setGraphic(l);
                }
            });
            // Status badge on col 6
            if (i==6) col.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    Label l=new Label(item); boolean sch=item.equals("Scheduled");
                    l.setStyle("-fx-background-color:"+(sch?C_TEXT:"#F1F5F9")+"; -fx-text-fill:"+(sch?"white":C_MUTED)+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 12 4 12;"); setGraphic(l);
                }
            });
            tv.getColumns().add(col);
        }

        // Actions column
        TableColumn<ObservableList<String>,Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(upcoming ? 130 : 160);
        final boolean isUpcoming = upcoming;
        final ObservableList<ObservableList<String>> capturedData = data;
        final List<String[]> capturedExams = controller.getExams();

        actCol.setCellFactory(c -> new TableCell<ObservableList<String>,Void>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                int rowIdx = getIndex();
                if (rowIdx<0||rowIdx>=getTableView().getItems().size()) { setGraphic(null); return; }
                HBox box = new HBox(8); box.setAlignment(Pos.CENTER);

                if (!isUpcoming) {
                    // Completed: Enter Marks button
                    Button em = new Button("Enter Marks");
                    em.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:5 12 5 12;");
                    em.setOnAction(e -> {
                        String examName = getTableView().getItems().get(rowIdx).get(0);
                        showEnterMarksDialog(examName);
                    });
                    box.getChildren().add(em);
                } else {
                    // Upcoming: edit + delete
                    Button eb = new Button("✏"); eb.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:5 10 5 10;");
                    Button db = new Button("🗑"); db.setStyle("-fx-background-color:transparent; -fx-font-size:16px; -fx-cursor:hand; -fx-text-fill:"+C_RED+";");
                    eb.setOnAction(e -> showEditExamDialog(getTableView().getItems().get(rowIdx), rowIdx, capturedData, capturedExams));
                    db.setOnAction(e -> {
                        if (confirmDelete()) {
                            // Find actual index in model
                            ObservableList<String> row2 = getTableView().getItems().get(rowIdx);
                            List<String[]> all = controller.getExams();
                            for (int i=0;i<all.size();i++) {
                                if(all.get(i)[0].equals(row2.get(0))&&all.get(i)[3].equals(row2.get(3))) {
                                    controller.deleteExam(i); break;
                                }
                            }
                            capturedData.remove(rowIdx);
                        }
                    });
                    box.getChildren().addAll(eb, db);
                }
                setGraphic(box);
            }
        });
        tv.getColumns().add(actCol);
        tv.setItems(data);
        return tv;
    }

    private void showEditExamDialog(ObservableList<String> row, int idx,
            ObservableList<ObservableList<String>> tableData, List<String[]> examList) {
        Stage d = makeDialog(owner,"Edit Exam",500,420);
        BorderPane root = dialogRoot("Edit Exam", d);
        VBox form = new VBox(10); form.setPadding(new Insets(16,24,20,24));
        TextField nameTF = addFormRow(form,"Exam Name *", row.size()>0?row.get(0):"");
        TextField dateTF = addFormRow(form,"Date",        row.size()>3?row.get(3):"");
        TextField durTF  = addFormRow(form,"Duration",    row.size()>4?row.get(4):"");
        TextField mrkTF  = addFormRow(form,"Total Marks", row.size()>5?row.get(5):"100");
        ComboBox<String> subjBox = new ComboBox<>();
        for (String s : TeacherModel.SUBJECTS) subjBox.getItems().add(s);
        String cs=row.size()>1?row.get(1):"Mathematics";
        subjBox.setValue(subjBox.getItems().contains(cs)?cs:"Mathematics");
        subjBox.setPrefHeight(38); subjBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> clsBox = new ComboBox<>();
        for (String c : TeacherModel.CLASS_NAMES) clsBox.getItems().add(c);
        String cc=row.size()>2?row.get(2):"Class 1-A";
        clsBox.setValue(clsBox.getItems().contains(cc)?cc:"Class 1-A");
        clsBox.setPrefHeight(38); clsBox.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(boldLabel("Subject"),subjBox,boldLabel("Class"),clsBox);
        Button save = makePrimaryBtn("Update Exam");
        save.setOnAction(e -> {
            if(nameTF.getText().trim().isEmpty()){showAlert("Exam name required.");return;}
            String[] v={nameTF.getText().trim(),subjBox.getValue(),clsBox.getValue(),
                dateTF.getText().trim(),durTF.getText().trim(),mrkTF.getText().trim(),"Scheduled"};
            for(int i=0;i<v.length&&i<row.size();i++) row.set(i,v[i]);
            tableData.set(idx,row);
            // update model
            for(int i=0;i<examList.size();i++) if(examList.get(i)[0].equals(row.get(0))) { controller.updateExam(i,v); break; }
            showAlert("Exam updated!"); d.close();
        });
        Button cancel=makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        form.getChildren().add(footerBtns(cancel,save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private void showEnterMarksDialog(String examName) {
        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Enter Marks — "+examName); d.setWidth(620); d.setHeight(480);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");
        HBox hdr=new HBox(); hdr.setPadding(new Insets(16,24,12,24)); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        Label tl=new Label("Enter Marks — "+examName); tl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(tl,Priority.ALWAYS);
        Button xb=new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand;"); xb.setOnAction(e->d.close());
        hdr.getChildren().addAll(tl,xb); root.setTop(hdr);

        String[] students = controller.getStudentList().stream().map(s->s[1]).toArray(String[]::new);
        if (students.length==0) students=new String[]{"Emma Johnson","Liam Smith","Olivia Brown","Noah Davis","Sophia Wilson"};
        final String[] finalStudents = students;

        // Get existing marks for this exam
        Map<String,String[]> existing = new HashMap<>();
        for (String[] g : controller.getGrades()) if (g[1].equals(examName)) existing.put(g[0],g);

        VBox formBody = new VBox(0); formBody.setStyle("-fx-background-color:white;"); formBody.setPadding(new Insets(8,24,8,24));
        HBox hdrRow=new HBox(0); hdrRow.setPadding(new Insets(8,0,8,0));
        hdrRow.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 1 0;");
        String[] hNames={"Student","Marks Obtained","Total Marks","Percentage","Grade"};
        int[] hW={200,130,100,110,80};
        for(int i=0;i<hNames.length;i++){Label hl=new Label(hNames[i]); hl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_MUTED+"; -fx-padding:0 0 0 8;"); hl.setMinWidth(hW[i]); hl.setPrefWidth(hW[i]); hdrRow.getChildren().add(hl);}
        formBody.getChildren().add(hdrRow);

        List<TextField> markFields = new ArrayList<>();
        List<TextField> totalFields = new ArrayList<>();
        List<Label[]>   resultLbls = new ArrayList<>();

        for (String student : students) {
            HBox row=new HBox(0); row.setPadding(new Insets(10,0,10,0)); row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
            Label nl=new Label(student); nl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+"; -fx-padding:0 0 0 8;"); nl.setMinWidth(200); nl.setPrefWidth(200);
            String existMark = existing.containsKey(student)?existing.get(student)[2]:"";
            String existTotal= existing.containsKey(student)?existing.get(student)[3]:"100";
            TextField marksTF = new TextField(existMark); marksTF.setPrefHeight(36); marksTF.setMinWidth(120); marksTF.setPrefWidth(120);
            marksTF.setStyle("-fx-background-color:#FFFBEB; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            HBox.setMargin(marksTF,new Insets(0,8,0,8));
            TextField totalTF = new TextField(existTotal); totalTF.setPrefHeight(36); totalTF.setMinWidth(90); totalTF.setPrefWidth(90);
            totalTF.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            HBox.setMargin(totalTF,new Insets(0,8,0,0));
            Label pctLbl=new Label("-"); pctLbl.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+"; -fx-padding:0 0 0 8;"); pctLbl.setMinWidth(102); pctLbl.setPrefWidth(102);
            Label gradeLbl=new Label("-"); gradeLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_GREEN+"; -fx-padding:0 0 0 8;"); gradeLbl.setMinWidth(72); gradeLbl.setPrefWidth(72);
            Runnable calc=()->{
                try{int m=Integer.parseInt(marksTF.getText().trim()),t=Integer.parseInt(totalTF.getText().trim().isEmpty()?"100":totalTF.getText().trim());
                    double pct=t>0?m*100.0/t:0; String grade=pct>=90?"A+":pct>=80?"A":pct>=70?"B+":pct>=60?"B":pct>=50?"C":"F";
                    pctLbl.setText(String.format("%.1f%%",pct)); gradeLbl.setText(grade);
                    String col=grade.startsWith("A")?C_GREEN:grade.startsWith("B")?C_BLUE:C_ORANGE;
                    gradeLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+col+"; -fx-padding:0 0 0 8;");
                }catch(NumberFormatException ex){pctLbl.setText("-");gradeLbl.setText("-"); gradeLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_MUTED+"; -fx-padding:0 0 0 8;");}
            };
            marksTF.textProperty().addListener((obs,o,n)->calc.run());
            totalTF.textProperty().addListener((obs,o,n)->calc.run());
            if (!existMark.isEmpty()) calc.run();
            row.getChildren().addAll(nl,marksTF,totalTF,pctLbl,gradeLbl);
            formBody.getChildren().add(row);
            markFields.add(marksTF); totalFields.add(totalTF); resultLbls.add(new Label[]{pctLbl,gradeLbl});
        }
        ScrollPane sp=new ScrollPane(formBody); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:white; -fx-background:white;");

        HBox btns=new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(12,24,12,24));
        btns.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button cancel=makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        Button save=makePrimaryBtn("Save Marks");
        save.setOnAction(e -> {
            String[] marksArr = new String[finalStudents.length];
            String[] totalsArr = new String[finalStudents.length];
            for(int i=0;i<finalStudents.length;i++){
                marksArr[i]=markFields.get(i).getText().trim();
                totalsArr[i]=totalFields.get(i).getText().trim().isEmpty()?"100":totalFields.get(i).getText().trim();
            }
            controller.saveGrades(examName, finalStudents, marksArr, totalsArr);
            showAlert("Marks saved!"); d.close();
        });
        btns.getChildren().addAll(cancel,save);
        root.setCenter(sp); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Grades Tab ────────────────────────────────────────────────────────
    private Tab gradesTab() {
        Tab tab = new Tab("Grades");
        VBox content = new VBox(14); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        // Filter row
        HBox filterRow = new HBox(12); filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(0,0,8,0));

        Label clsLbl = new Label("Class:"); clsLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        ComboBox<String> clsBox = new ComboBox<>();
        clsBox.getItems().add("All Classes");
        for (String c : TeacherModel.CLASS_NAMES) clsBox.getItems().add(c);
        clsBox.setValue("All Classes"); clsBox.setPrefHeight(36);

        Label examLbl = new Label("Exam:"); examLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        ComboBox<String> examBox = new ComboBox<>();
        examBox.getItems().add("All Exams");
        for (String[] e : controller.getExams()) {
            String name = e[0]+" ("+e[2]+")";
            if (!examBox.getItems().contains(name)) examBox.getItems().add(name);
        }
        examBox.setValue("All Exams"); examBox.setPrefHeight(36);

        filterRow.getChildren().addAll(clsLbl, clsBox, examLbl, examBox);

        // Table
        ObservableList<ObservableList<String>> gData = FXCollections.observableArrayList();
        TableView<ObservableList<String>> tv = buildSimpleTable(
            new String[]{"Student","Exam","Marks","Total","Percentage","Grade","Remarks"},
            new int[]{150,130,70,70,95,65,90}, new String[0][]);
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(46); return row; });
        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>,String> gc = (TableColumn<ObservableList<String>,String>) tv.getColumns().get(5);
        gc.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setGraphic(null);return;}
                Label l = new Label(item);
                l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:"+(item.startsWith("A")?C_GREEN:item.startsWith("B")?C_BLUE:C_ORANGE)+";");
                setGraphic(l);
            }
        });

        // Reload grades filtered by class + exam
        Runnable reloadGrades = () -> {
            gData.clear();
            String selCls  = clsBox.getValue();
            String selExam = examBox.getValue();
            for (String[] g : controller.getGrades()) {
                // Find which class this exam belongs to
                String examClass = "All Classes";
                for (String[] e : controller.getExams()) if (e[0].equals(g[1])) { examClass = e[2]; break; }
                boolean matchCls  = selCls.equals("All Classes")  || examClass.equals(selCls);
                boolean matchExam = selExam.equals("All Exams") || selExam.startsWith(g[1]+" (");
                if (matchCls && matchExam) gData.add(FXCollections.observableArrayList(g));
            }
        };
        reloadGrades.run();
        tv.setItems(gData);

        clsBox.setOnAction(e  -> reloadGrades.run());
        examBox.setOnAction(e -> reloadGrades.run());

        VBox tableCard = new VBox(10); tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        Label title = new Label("Student Grades"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        tableCard.getChildren().addAll(title, filterRow, tv);
        content.getChildren().add(tableCard);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp); return tab;
    }

    // ── Report Cards Tab ──────────────────────────────────────────────────
    private Tab reportCardsTab() {
        Tab tab = new Tab("Report Cards");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");
        VBox card = new VBox(16); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        Label title = new Label("Generate Report Card"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        HBox selRow = new HBox(12); selRow.setAlignment(Pos.CENTER_LEFT);
        Label sl = new Label("Student:"); sl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        ComboBox<String> studBox = new ComboBox<>();
        studBox.getItems().add("Select student");
        for (String[] s : controller.getStudentList()) studBox.getItems().add(s[1]);
        studBox.setValue("Select student"); studBox.setPrefHeight(36);
        ComboBox<String> termBox = makeCombo("Term 1","Term 1","Term 2","Term 3","Full Year"); termBox.setPrefHeight(36);
        Button genBtn = makePrimaryBtn("Generate");
        selRow.getChildren().addAll(sl, studBox, termBox, genBtn);
        VBox preview = new VBox(10); preview.setPadding(new Insets(20));
        preview.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        preview.setMinHeight(200);
        Label ph = new Label("Select a student and click Generate to view report card.");
        ph.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"); preview.getChildren().add(ph);
        genBtn.setOnAction(e -> {
            if (studBox.getValue().equals("Select student")) { showAlert("Please select a student."); return; }
            String student=studBox.getValue(), term=termBox.getValue();
            preview.getChildren().clear();
            Label rc=new Label("REPORT CARD — "+student); rc.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            Label tl2=new Label(term+" | EduManage School"); tl2.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
            TableView<ObservableList<String>> rcTable = buildSimpleTable(
                new String[]{"Exam","Marks","Total","Grade"},new int[]{160,80,80,80},new String[0][]);
            ObservableList<ObservableList<String>> rcData=FXCollections.observableArrayList();
            for(String[] g:controller.getGrades()) if(g[0].equals(student)) rcData.add(FXCollections.observableArrayList(g[1],g[2],g[3],g[5]));
            if(rcData.isEmpty()) rcData.add(FXCollections.observableArrayList("No data","—","—","—"));
            rcTable.setItems(rcData); rcTable.setPrefHeight(160);
            preview.getChildren().addAll(rc,tl2,new Separator(),rcTable);
        });
        card.getChildren().addAll(title,selRow,preview);
        content.getChildren().add(card);
        ScrollPane sp=bgScroll(); sp.setContent(content); tab.setContent(sp); return tab;
    }
}