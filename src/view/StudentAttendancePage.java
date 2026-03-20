package view;

import controller.StudentController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static view.StudentUIHelper.*;

/**
 * StudentAttendancePage — overall summary cards, subject-wise table, history.
 */
public class StudentAttendancePage {

    private final StudentController controller;

    public StudentAttendancePage(StudentController controller) {
        this.controller = controller;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 32, 24, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Summary cards ─────────────────────────────────────────────────
        HBox cards = new HBox(16);
        String pct = controller.getAttendancePctStr();
        cards.getChildren().addAll(
            attCard("Overall",   pct,   "This semester", C_BLUE),
            attCard("Present",   "68",  "Days",          C_GREEN),
            attCard("Absent",    "6",   "Days",          C_RED),
            attCard("Late",      "2",   "Days",          C_ORANGE)
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Subject-wise table ────────────────────────────────────────────
        VBox subCard = new VBox(10); subCard.setPadding(new Insets(16));
        subCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label subTitle = lbl("Subject-wise Attendance", "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r->{TableRow<ObservableList<String>> row=new TableRow<>();row.setPrefHeight(46);return row;});
        String[] cols  = {"Subject","Total Classes","Attended","Absent","Attendance %","Status"};
        int[]    widths = {160,120,100,80,110,120};
        for (int i=0;i<cols.length;i++){
            final int fi=i;
            TableColumn<ObservableList<String>,String> col=new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i==5) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    String bg = item.equals("Good")?C_GREEN:item.equals("Average")?C_ORANGE:C_RED;
                    setGraphic(badge(item, bg, "white"));
                }
            });
            if (i==4) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setText(null);return;}
                    try {
                        double v=Double.parseDouble(item.replace("%",""));
                        setStyle("-fx-font-weight:bold; -fx-text-fill:"+(v>=90?C_GREEN:v>=75?C_BLUE:C_ORANGE)+";");
                    } catch (NumberFormatException e) { setStyle(""); }
                    setText(item);
                }
            });
            tv.getColumns().add(col);
        }
        for (String[] r : controller.getAttendanceBySubject())
            tv.getItems().add(FXCollections.observableArrayList(r));
        subCard.getChildren().addAll(subTitle, tv);

        // ── Attendance history ────────────────────────────────────────────
        VBox histCard = new VBox(10); histCard.setPadding(new Insets(16));
        histCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label histTitle = lbl("Attendance History", "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        TableView<ObservableList<String>> htv = new TableView<>();
        htv.setStyle("-fx-font-size:13px;");
        htv.setRowFactory(r->{TableRow<ObservableList<String>> row=new TableRow<>();row.setPrefHeight(44);return row;});
        String[] hcols  = {"Date","Day","Status","Remarks"};
        int[]    hwidths = {130,120,120,200};
        for (int i=0;i<hcols.length;i++){
            final int fi=i;
            TableColumn<ObservableList<String>,String> col=new TableColumn<>(hcols[i]);
            col.setPrefWidth(hwidths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if(i==2) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    String bg=item.equals("Present")?C_GREEN:item.equals("Absent")?C_RED:C_ORANGE;
                    setGraphic(badge(item,bg,"white"));}
            });
            htv.getColumns().add(col);
        }
        for (String[] r : controller.getAttendanceHistory())
            htv.getItems().add(FXCollections.observableArrayList(r));
        histCard.getChildren().addAll(histTitle, htv);

        page.getChildren().addAll(cards, subCard, histCard);
        scroll.setContent(page);
        return scroll;
    }

    private VBox attCard(String label, String value, String sub, String color) {
        VBox card = new VBox(8); card.setPadding(new Insets(18)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12;");
        Label lbl2 = lbl(label, "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        Label val  = lbl(value, "-fx-font-size:30px; -fx-font-weight:bold; -fx-text-fill:"+color+";");
        Label sub2 = lbl(sub,   "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        card.getChildren().addAll(lbl2, val, sub2);
        return card;
    }
}
