
package view;

import controller.StudentController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static view.StudentUIHelper.*;

/**
 * StudentExamsPage — two tabs: Upcoming/Completed exams + Grades.
 */
public class StudentExamsPage {

    private final StudentController controller;
    private final Stage             owner;

    public StudentExamsPage(StudentController controller, Stage owner) {
        this.controller = controller;
        this.owner      = owner;
    }

    public Node build() {
        VBox page = new VBox(0);
        page.setPadding(new Insets(24, 32, 24, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-font-size:13px;");
        Tab examsTab  = buildExamsTab();
        Tab gradesTab = buildGradesTab();
        examsTab.setClosable(false); gradesTab.setClosable(false);
        tabs.getTabs().addAll(examsTab, gradesTab);

        page.getChildren().add(tabs);
        ScrollPane scroll = bgScroll();
        scroll.setContent(page);
        return scroll;
    }

    // ── Exams Tab ─────────────────────────────────────────────────────────
    private Tab buildExamsTab() {
        Tab tab = new Tab("Exams");
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Upcoming Exams ────────────────────────────────────────────────
        VBox upCard = new VBox(12); upCard.setPadding(new Insets(16));
        upCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label upTitle = lbl("Upcoming Exams", "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        VBox upList = new VBox(8);
        for (String[] e : controller.getExams()) {
            if (!e[7].equals("Upcoming")) continue;
            HBox row = new HBox(16); row.setPadding(new Insets(12,14,12,14)); row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:#F8F9FC; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8;");

            // Date badge
            VBox db = new VBox(2); db.setAlignment(Pos.CENTER);
            db.setStyle("-fx-background-color:"+C_ACCENT+"; -fx-background-radius:8;");
            db.setPrefSize(56,56); db.setMinSize(56,56); db.setMaxSize(56,56);
            String[] dateParts = e[2].split("-");
            String month = dateParts.length>1 ? getMonth(Integer.parseInt(dateParts[1])) : "";
            String day   = dateParts.length>2 ? dateParts[2] : "";
            Label mo = lbl(month, "-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:white;");
            Label dy = lbl(day,   "-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:white;");
            db.getChildren().addAll(mo, dy);

            VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
            Label nm = lbl(e[0], "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            Label sub= lbl(e[1]+" • "+e[3]+" • Total: "+e[4]+" marks", "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
            info.getChildren().addAll(nm, sub);

            Label statusBadge = badge("Upcoming", C_ACCENT, "white");
            row.getChildren().addAll(db, info, statusBadge);
            upList.getChildren().add(row);
        }
        if (upList.getChildren().isEmpty()) {
            upList.getChildren().add(lbl("No upcoming exams.", "-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"));
        }
        upCard.getChildren().addAll(upTitle, upList);

        // ── Completed Exams ───────────────────────────────────────────────
        VBox compCard = new VBox(12); compCard.setPadding(new Insets(16));
        compCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label compTitle = lbl("Completed Exams", "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        // Table for completed exams
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(48); return row; });

        String[] cols  = {"Exam Name","Subject","Date","Duration","Marks","Grade","Status"};
        int[]    widths = {160,120,110,90,80,70,110};
        for (int i=0;i<cols.length;i++) {
            final int fi=i;
            TableColumn<ObservableList<String>,String> col=new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i==5) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    Label l=new Label(item); l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:"+(item.startsWith("A")?C_GREEN:item.startsWith("B")?C_BLUE:C_ORANGE)+";");
                    setGraphic(l);}
            });
            if (i==6) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    Label l=badge(item,"#F0FDF4",C_GREEN); setGraphic(l);}
            });
            tv.getColumns().add(col);
        }
        for (String[] e : controller.getExams()) {
            if (!e[7].equals("Completed")) continue;
            tv.getItems().add(FXCollections.observableArrayList(e[0],e[1],e[2],e[3],e[5]+"/"+e[4],e[6],e[7]));
        }
        compCard.getChildren().addAll(compTitle, tv);

        content.getChildren().addAll(upCard, compCard);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp);
        return tab;
    }

    // ── Grades Tab ────────────────────────────────────────────────────────
    private Tab buildGradesTab() {
        Tab tab = new Tab("My Grades");
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));
        content.setStyle("-fx-background-color:" + C_BG + ";");

        // Summary cards per subject
        HBox summaryRow = new HBox(12);
        String[] subjects = {"Mathematics","Physics","Chemistry","Biology","English"};
        String[] summaryGrades = {"A+","B+","A","A","A-"};
        String[] summaryPcts   = {"94%","76%","88%","86%","80%"};
        for (int i=0;i<subjects.length;i++) {
            VBox sc = new VBox(6); sc.setPadding(new Insets(14)); sc.setAlignment(Pos.CENTER);
            sc.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
            HBox.setHgrow(sc, Priority.ALWAYS);
            Label gl = new Label(summaryGrades[i]);
            String col = summaryGrades[i].startsWith("A")?C_GREEN:C_BLUE;
            gl.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:"+col+";");
            Label sl2 = lbl(subjects[i], "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";");
            Label pl = lbl(summaryPcts[i], "-fx-font-size:12px; -fx-text-fill:"+C_TEXT+"; -fx-font-weight:bold;");
            sc.getChildren().addAll(gl, sl2, pl);
            summaryRow.getChildren().add(sc);
        }

        // Grades table
        VBox tableCard = new VBox(10); tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label tl2 = lbl("Detailed Grade Report", "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r->{TableRow<ObservableList<String>> row=new TableRow<>();row.setPrefHeight(46);return row;});
        String[] cols  = {"Subject","Exam","Marks","Total","Percentage","Grade","Remarks"};
        int[]    widths = {140,130,70,70,100,70,120};
        for (int i=0;i<cols.length;i++){
            final int fi=i;
            TableColumn<ObservableList<String>,String> col=new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if(i==5) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    Label l=new Label(item); l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:"+(item.startsWith("A")?C_GREEN:item.startsWith("B")?C_BLUE:C_ORANGE)+";");
                    setGraphic(l);}
            });
            tv.getColumns().add(col);
        }
        for (String[] g : controller.getGrades())
            tv.getItems().add(FXCollections.observableArrayList(g));

        tableCard.getChildren().addAll(tl2, tv);
        content.getChildren().addAll(summaryRow, tableCard);
        ScrollPane sp = bgScroll(); sp.setContent(content); tab.setContent(sp);
        return tab;
    }

    private String getMonth(int m) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return (m>=1&&m<=12) ? months[m-1] : "";
    }
}
