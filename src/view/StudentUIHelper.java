package view;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;

/**
 * StudentUIHelper — shared colour palette and UI utilities for all student pages.
 * Every page uses: import static view.StudentUIHelper.*;
 */
public class StudentUIHelper {

    // ── Colours (matches screenshot: white sidebar, light bg) ─────────────
    public static final String C_BG     = "#F8F9FC";
    public static final String C_WHITE  = "#FFFFFF";
    public static final String C_ACCENT = "#2563EB";   // blue (active nav)
    public static final String C_TEXT   = "#1E293B";
    public static final String C_MUTED  = "#64748B";
    public static final String C_BORDER = "#E2E8F0";
    public static final String C_SEL    = "#EFF6FF";   // active nav bg
    public static final String C_BLUE   = "#3B82F6";
    public static final String C_GREEN  = "#22C55E";
    public static final String C_ORANGE = "#F97316";
    public static final String C_RED    = "#EF4444";
    public static final String C_PURPLE = "#7C3AED";

    // ── Scroll pane ────────────────────────────────────────────────────────
    public static ScrollPane bgScroll() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:"+C_BG+"; -fx-background:"+C_BG+";");
        return sp;
    }

    // ── Card ───────────────────────────────────────────────────────────────
    public static VBox card(String title, Node content) {
        VBox c = new VBox(10); c.setPadding(new Insets(16));
        c.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        if (title != null) {
            Label t = new Label(title);
            t.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            c.getChildren().add(t);
        }
        c.getChildren().add(content);
        return c;
    }

    // ── Label shortcut ─────────────────────────────────────────────────────
    public static Label lbl(String text, String style) {
        Label l = new Label(text); l.setStyle(style); return l;
    }

    // ── Badge ──────────────────────────────────────────────────────────────
    public static Label badge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:"+bg+"; -fx-text-fill:"+fg+"; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10;");
        return l;
    }

    // ── Simple table ───────────────────────────────────────────────────────
    public static TableView<javafx.collections.ObservableList<String>> simpleTable(
            String[] cols, int[] widths, String[][] data) {
        TableView<javafx.collections.ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r -> {
            TableRow<javafx.collections.ObservableList<String>> row = new TableRow<>();
            row.setPrefHeight(46); return row;
        });
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<javafx.collections.ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().size() > fi ? d.getValue().get(fi) : ""));
            tv.getColumns().add(col);
        }
        for (String[] r : data)
            tv.getItems().add(javafx.collections.FXCollections.observableArrayList(r));
        return tv;
    }

    // ── Dialog ─────────────────────────────────────────────────────────────
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

    // ── Buttons ────────────────────────────────────────────────────────────
    public static Button primaryBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:"+C_ACCENT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:9 20 9 20;");
        return b;
    }

    public static Button secondaryBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-font-size:13px; -fx-cursor:hand; -fx-padding:9 20 9 20;");
        return b;
    }

    // ── Alerts ─────────────────────────────────────────────────────────────
    public static boolean confirmDialog(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
