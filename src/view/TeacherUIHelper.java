package view;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;

/**
 * TeacherUIHelper — shared colour palette and UI utility methods.
 * Every page class uses: import static view.TeacherUIHelper.*;
 */
public class TeacherUIHelper {

    // ── Colour palette ─────────────────────────────────────────────────────
    public static final String C_BG     = "#F8F9FC";
    public static final String C_WHITE  = "#FFFFFF";
    public static final String C_ACCENT = "#4F46E5";
    public static final String C_TEXT   = "#1E293B";
    public static final String C_MUTED  = "#64748B";
    public static final String C_BORDER = "#E2E8F0";
    public static final String C_SEL    = "#EEF2FF";
    public static final String C_BLUE   = "#3B82F6";
    public static final String C_PURPLE = "#7C3AED";
    public static final String C_GREEN  = "#22C55E";
    public static final String C_ORANGE = "#F97316";
    public static final String C_RED    = "#EF4444";
    public static final String C_CHART  = "#7C3AED";

    // ── Dialog helpers ─────────────────────────────────────────────────────
    public static Stage makeDialog(Stage owner, String title, double w, double h) {
        Stage d = new Stage();
        d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(title); d.setWidth(w); d.setHeight(h);
        return d;
    }

    public static BorderPane dialogRoot(String title, Stage d) {
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");
        HBox h = new HBox(); h.setPadding(new Insets(16,24,12,24)); h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        Label tl = new Label(title); tl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        HBox.setHgrow(tl, Priority.ALWAYS);
        Button xb = new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand;");
        xb.setOnAction(e -> d.close());
        h.getChildren().addAll(tl, xb); root.setTop(h); return root;
    }

    // ── Form helpers ───────────────────────────────────────────────────────
    public static TextField addFormRow(VBox form, String label, String val) {
        Label l = new Label(label); l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        VBox.setMargin(l, new Insets(4,0,4,0));
        TextField tf = new TextField(val); tf.setPrefHeight(38);
        tf.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
        form.getChildren().addAll(l, tf); return tf;
    }

    public static Label boldLabel(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        VBox.setMargin(l, new Insets(4,0,4,0)); return l;
    }

    public static ComboBox<String> makeCombo(String def, String... items) {
        ComboBox<String> c = new ComboBox<>(); c.getItems().addAll(items);
        c.setValue(def); c.setPrefHeight(38); c.setMaxWidth(Double.MAX_VALUE); return c;
    }

    // ── Button helpers ─────────────────────────────────────────────────────
    public static Button makePrimaryBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:9 20 9 20;");
        return b;
    }

    public static Button makeSecondaryBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:9 20 9 20;");
        return b;
    }

    // ── Layout helpers ─────────────────────────────────────────────────────
    public static HBox footerBtns(Button... btns) {
        HBox box = new HBox(12); box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(12,24,14,24));
        box.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        box.getChildren().addAll(btns); return box;
    }

    public static ScrollPane wrapScroll(Node n) {
        ScrollPane sp = new ScrollPane(n); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:white; -fx-background:white;"); return sp;
    }

    public static ScrollPane bgScroll() {
        ScrollPane sp = new ScrollPane(); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:"+C_BG+"; -fx-background:"+C_BG+";"); return sp;
    }

    public static BorderPane padPage(Node center) {
        BorderPane p = new BorderPane(); p.setPadding(new Insets(20,28,20,28));
        p.setStyle("-fx-background-color:"+C_BG+";"); p.setCenter(center); return p;
    }

    public static TabPane makeTabs(Tab... tabs) {
        TabPane tp = new TabPane(); tp.setStyle("-fx-font-size:13px;");
        for (Tab t : tabs) { t.setClosable(false); tp.getTabs().add(t); } return tp;
    }

    // ── Label shortcut ─────────────────────────────────────────────────────
    public static Label lbl(String text, String style) {
        Label l = new Label(text); l.setStyle(style); return l;
    }

    // ── Table helper ───────────────────────────────────────────────────────
    public static TableView<ObservableList<String>> buildSimpleTable(String[] cols, int[] widths, String[][] data) {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(44); return row; });
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            tv.getColumns().add(col);
        }
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        for (String[] r : data) items.add(FXCollections.observableArrayList(r));
        tv.setItems(items); return tv;
    }

    // ── Info grid ──────────────────────────────────────────────────────────
    public static VBox makeInfoGrid(String[][] info) {
        VBox panel = new VBox(14); panel.setPadding(new Insets(20,24,20,24)); panel.setStyle("-fx-background-color:white;");
        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(14);
        for (int i = 0; i < info.length; i++) {
            Label lbl2 = new Label(info[i][0]); lbl2.setStyle("-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";");
            Label val  = new Label(info[i][1]); val.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            VBox cell = new VBox(2); cell.getChildren().addAll(lbl2, val); grid.add(cell, i%2, i/2);
        }
        panel.getChildren().add(grid); return panel;
    }

    // ── Card box ───────────────────────────────────────────────────────────
    public static VBox cardBox(String title, Node content) {
        VBox card = new VBox(10); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        Label t = new Label(title); t.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        card.getChildren().addAll(t, content); return card;
    }

    // ── Bar chart ──────────────────────────────────────────────────────────
    public static Pane makeLiveBarChart(String[] labels, double[] scores, String barColor, String suffix, double prefH) {
        Pane chartPane = new Pane(); chartPane.setPrefHeight(prefH);
        Canvas canvas = new Canvas(); chartPane.getChildren().add(canvas);
        canvas.widthProperty().bind(chartPane.widthProperty());
        canvas.heightProperty().bind(chartPane.heightProperty());
        Runnable draw = () -> drawBarChart(canvas, labels, scores, barColor, suffix);
        canvas.widthProperty().addListener(o -> draw.run());
        canvas.heightProperty().addListener(o -> draw.run());
        Platform.runLater(draw); return chartPane;
    }

    public static void drawBarChart(Canvas canvas, String[] labels, double[] scores, String barColor, String suffix) {
        double W = canvas.getWidth(), H = canvas.getHeight(); if (W<10||H<10) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,W,H); gc.setFill(Color.WHITE); gc.fillRect(0,0,W,H);
        double pL=50,pR=20,pT=20,pB=40,cW=W-pL-pR,cH=H-pT-pB;
        double maxVal=0; for (double s:scores) if(s>maxVal) maxVal=s; if(maxVal==0) maxVal=100;
        gc.setStroke(Color.web("#E2E8F0")); gc.setLineDashes(4); gc.setLineWidth(1);
        for (int i=0;i<=4;i++) {
            double y=pT+cH*i/4; gc.strokeLine(pL,y,W-pR,y);
            gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
            gc.fillText(String.valueOf((int)(maxVal*(4-i)/4))+suffix,2,y+4);
        }
        gc.setLineDashes(0);
        int n=labels.length; double sp=cW/n,bw=sp*0.45;
        for (int i=0;i<n;i++) {
            double bh=scores[i]*cH/maxVal,x=pL+sp*i+(sp-bw)/2,y=pT+cH-bh;
            gc.setFill(Color.web(barColor)); gc.fillRoundRect(x,y,bw,bh,6,6);
            gc.setFill(Color.web(C_TEXT)); gc.setFont(Font.font("SansSerif",FontWeight.BOLD,11));
            String st=String.valueOf((int)scores[i])+suffix;
            gc.fillText(st,x+(bw-st.length()*6.0)/2,y-5);
            gc.setFill(Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",11));
            gc.fillText(labels[i],x+(bw-labels[i].length()*5.5)/2,pT+cH+16);
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    public static void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION,msg,ButtonType.OK); a.setHeaderText(null); a.showAndWait();
    }

    public static boolean confirmDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Delete this record?",ButtonType.YES,ButtonType.NO); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO)==ButtonType.YES;
    }

    public static String cap(String s) {
        return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1).toLowerCase();
    }
}
