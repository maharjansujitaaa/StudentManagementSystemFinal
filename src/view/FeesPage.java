
package view;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import java.util.Arrays;

import static view.UIHelper.*;

public class FeesPage {

    private final AppState state;
    private final Stage owner;

    public FeesPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + C_BG + "; -fx-background:" + C_BG + ";");

        VBox page = new VBox(16);
        page.setPadding(new Insets(20, 28, 20, 28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // Live summary cards from state.feeData
        int totalExp=0, totalPaid=0, totalPending=0, totalOverdue=0;
        for (Object[] f : state.feeData) {
            int amt=0, paid=0;
            try { amt  = Integer.parseInt(f[2].toString().replace("$","").replace(",","")); } catch (Exception ignored) {}
            try { paid = Integer.parseInt(f[3].toString().replace("$","").replace(",","")); } catch (Exception ignored) {}
            totalExp += amt; totalPaid += paid;
            String st = f[6].toString();
            if (st.equals("Overdue"))   totalOverdue  += (amt - paid);
            else if (!st.equals("Paid")) totalPending += (amt - paid);
        }
        HBox cards = new HBox(14);
        cards.getChildren().addAll(
            makeFeeSummCard("Total Fees",    "$" + totalExp,     C_BLUE,   "$"),
            makeFeeSummCard("Total Paid",    "$" + totalPaid,    C_GREEN,  "✓"),
            makeFeeSummCard("Total Pending", "$" + totalPending, C_ORANGE, "⏱"),
            makeFeeSummCard("Overdue",       "$" + totalOverdue, C_RED,    "!")
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Filter tabs
        TabPane tabs = new TabPane(); tabs.setStyle("-fx-font-size:13px;");
        String[] tabNames = {"All Fees","Pending","Overdue","Paid"};
        for (String tn : tabNames) {
            Tab t = new Tab(tn); t.setClosable(false);
            TableView<ObservableList<String>> tv = buildFeeTable();
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (Object[] f : state.feeData) {
                String st = f[6].toString();
                if (tn.equals("All Fees") || st.equalsIgnoreCase(tn) || (tn.equals("Pending") && st.equals("Partial")))
                    data.add(FXCollections.observableArrayList(Arrays.stream(f).map(Object::toString).toArray(String[]::new)));
            }
            tv.setItems(data);
            VBox tabContent = new VBox(10); tabContent.setPadding(new Insets(10,0,0,0));
            Label tl = new Label(tn.equals("All Fees") ? "All Fee Records" : tn + " Records");
            tl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
            tabContent.getChildren().addAll(tl, tv);
            t.setContent(tabContent);
            tabs.getTabs().add(t);
        }

        page.getChildren().addAll(cards, tabs);
        scroll.setContent(page);
        return scroll;
    }

    public void showAddDialog(Runnable onDone) {
        Stage d = makeDialog(owner, "Add Fee Record", 500, 380);
        BorderPane root = dialogRoot("Add Fee Record", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16,24,20,24));
        TextField studTF = addFormRow(form,"Student Name *","");
        TextField amtTF  = addFormRow(form,"Amount *","e.g. 5000");
        TextField paidTF = addFormRow(form,"Amount Paid","e.g. 0");
        TextField dueTF  = addFormRow(form,"Due Date *","yyyy-mm-dd");
        ComboBox<String> typeBox = makeCombo("Tuition Fee","Tuition Fee","Exam Fee","Library Fee","Sports Fee");
        ComboBox<String> stBox   = makeCombo("Pending","Pending","Partial","Paid","Overdue");
        form.getChildren().addAll(boldLabel("Fee Type *"), typeBox, boldLabel("Status *"), stBox);
        Button save = makePrimaryBtn("Save Record");
        save.setOnAction(e -> {
            if (studTF.getText().trim().isEmpty()) { showAlert("Student name is required."); return; }
            String amt  = "$" + amtTF.getText().trim().replace("$","");
            String paid = "$" + paidTF.getText().trim().replace("$","");
            String bal  = "$0";
            try { int a = Integer.parseInt(amtTF.getText().trim().replace("$","")); int p = Integer.parseInt(paidTF.getText().trim().replace("$","")); bal = "$" + (a-p); } catch (Exception ignored) {}
            state.feeData.add(new Object[]{studTF.getText().trim(), typeBox.getValue(), amt, paid, bal, dueTF.getText().trim(), stBox.getValue()});
            showAlert("Fee record added!"); d.close();
            if (onDone != null) onDone.run();
        });
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        form.getChildren().add(footerBtns(cancel, save));
        root.setCenter(wrapScroll(form)); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private TableView<ObservableList<String>> buildFeeTable() {
        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;"); tv.setPrefHeight(300);
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(52); return row; });

        String[] cols  = {"Student","Fee Type","Amount","Paid","Balance","Due Date","Status"};
        int[]    widths = {140,110,90,90,90,110,130};
        for (int i = 0; i < cols.length; i++) {
            final int fi = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().size() > fi ? d.getValue().get(fi) : ""));
            if (i == 4) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                    Label l = new Label(item); l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:" + (item.equals("$0")?C_TEXT:C_RED) + ";");
                    setGraphic(l);
                }
            });
            if (i == 6) col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                    Label l = new Label(item); l.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10 4 10; -fx-text-fill:" + (item.equals("Paid")||item.equals("Overdue")?"white":C_MUTED) + "; -fx-background-color:" + (item.equals("Paid")?C_TEXT:item.equals("Overdue")?C_RED:item.equals("Partial")?"#F1F5F9":"#FEF3C7") + ";");
                    setGraphic(l);
                }
            });
            tv.getColumns().add(col);
        }

        TableColumn<ObservableList<String>, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(130);
        actCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); if (empty) { setGraphic(null); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                String status = getTableView().getItems().get(idx).size() > 6 ? getTableView().getItems().get(idx).get(6) : "";
                if (status.equals("Paid")) {
                    Button rb = new Button("📄 Receipt");
                    rb.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:5 10 5 10;");
                    rb.setOnAction(e -> showReceiptDialog(getTableView().getItems().get(idx)));
                    setGraphic(rb);
                } else {
                    Button pb = new Button("Pay");
                    pb.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:6 16 6 16;");
                    pb.setOnAction(e -> showPayDialog(getTableView().getItems().get(idx), idx, getTableView().getItems()));
                    setGraphic(pb);
                }
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    private HBox makeFeeSummCard(String label, String value, String color, String icon) {
        HBox card = new HBox(12); card.setPadding(new Insets(16)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-radius:12; -fx-background-radius:12;");
        StackPane ib = new StackPane();
        Rectangle ir = new Rectangle(46,46); ir.setArcWidth(10); ir.setArcHeight(10);
        Color c = Color.web(color); ir.setFill(new Color(c.getRed(),c.getGreen(),c.getBlue(),0.15));
        Label il = new Label(icon); il.setStyle("-fx-font-size:18px; -fx-text-fill:" + color + ";");
        ib.getChildren().addAll(ir, il);
        VBox info = new VBox(2); HBox.setHgrow(info, Priority.ALWAYS);
        Label vl = new Label(value); vl.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        Label ll = new Label(label); ll.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";");
        info.getChildren().addAll(ll, vl);
        card.getChildren().addAll(info, ib);
        return card;
    }

    private void showReceiptDialog(ObservableList<String> row) {
        Stage d = makeDialog(owner, "Fee Receipt", 380, 320);
        VBox body = new VBox(10); body.setPadding(new Insets(24,32,24,32)); body.setStyle("-fx-background-color:white;");
        Label rc = new Label("PAYMENT RECEIPT"); rc.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); rc.setMaxWidth(Double.MAX_VALUE); rc.setAlignment(Pos.CENTER);
        Label sch = new Label("EduManage School"); sch.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); sch.setMaxWidth(Double.MAX_VALUE); sch.setAlignment(Pos.CENTER);
        body.getChildren().addAll(rc, sch, new Separator());
        String[] keys = {"Student:","Fee Type:","Amount Paid:","Due Date:","Status:"};
        String[] vals = {row.size()>0?row.get(0):"", row.size()>1?row.get(1):"", row.size()>2?row.get(2):"", row.size()>5?row.get(5):"","PAID ✓"};
        for (int i = 0; i < keys.length; i++) {
            HBox r = new HBox(); Label k = new Label(keys[i]); k.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"); HBox.setHgrow(k,Priority.ALWAYS);
            Label v = new Label(vals[i]); v.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+(i==4?C_GREEN:C_TEXT)+";");
            r.getChildren().addAll(k,v); body.getChildren().add(r);
        }
        Button closeBtn = makePrimaryBtn("Close"); closeBtn.setMaxWidth(Double.MAX_VALUE); closeBtn.setOnAction(e -> d.close());
        body.getChildren().add(closeBtn);
        d.setScene(new javafx.scene.Scene(body)); d.show();
    }

    private void showPayDialog(ObservableList<String> row, int idx, ObservableList<ObservableList<String>> allData) {
        String student = row.size()>0?row.get(0):"", balance = row.size()>4?row.get(4):"$0";
        Stage d = makeDialog(owner, "Process Payment", 460, 500);
        BorderPane root = dialogRoot("Process Payment", d);
        VBox form = new VBox(12); form.setPadding(new Insets(16,24,20,24)); form.setStyle("-fx-background-color:white;");

        HBox infoBanner = new HBox(12); infoBanner.setPadding(new Insets(12,16,12,16)); infoBanner.setAlignment(Pos.CENTER_LEFT);
        infoBanner.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8;");
        StackPane av = new StackPane(); av.getChildren().addAll(new javafx.scene.shape.Circle(20, Color.web("#DBEAFE")), lbl(student.isEmpty()?"P":student.substring(0,1).toUpperCase(),"-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";"));
        VBox si = new VBox(2, lbl(student,"-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), lbl(row.size()>1?row.get(1):"Fee","-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"));
        infoBanner.getChildren().addAll(av, si);

        HBox balRow = new HBox(8); balRow.setPadding(new Insets(12,16,12,16)); balRow.setStyle("-fx-background-color:#FEF2F2; -fx-border-color:#FCA5A5; -fx-border-radius:8; -fx-background-radius:8;"); balRow.setAlignment(Pos.CENTER_LEFT);
        VBox balInfo = new VBox(2); HBox.setHgrow(balInfo, Priority.ALWAYS);
        balInfo.getChildren().addAll(lbl("Balance Due","-fx-font-size:11px; -fx-text-fill:"+C_RED+";"), lbl(balance,"-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+C_RED+";"));
        balRow.getChildren().addAll(lbl("💳 ","-fx-font-size:16px;"), balInfo);

        ComboBox<String> method = makeCombo("Cash","Cash","Bank Transfer","Online Payment","Cheque");
        method.setPrefHeight(42); method.setPrefWidth(Double.MAX_VALUE);
        TextField amtTF = new TextField(balance.replace("$","").replace(",","")); amtTF.setPrefHeight(42);
        amtTF.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:14px;");
        TextField refTF = new TextField(); refTF.setPromptText("e.g. TXN-12345"); refTF.setPrefHeight(42);
        refTF.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px;");
        TextArea noteTA = new TextArea(); noteTA.setPromptText("Additional notes..."); noteTA.setPrefHeight(80); noteTA.setWrapText(true);
        noteTA.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-font-size:13px;");

        form.getChildren().addAll(infoBanner, balRow, boldLabel("Pay Via *"), method, boldLabel("Amount Paying *"), amtTF, boldLabel("Reference (optional)"), refTF, boldLabel("Note (optional)"), noteTA);

        ScrollPane sp = new ScrollPane(form); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:white; -fx-background:white;"); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(12,24,14,24));
        btns.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        Button confirm = makePrimaryBtn("✓  Confirm Payment");
        confirm.setOnAction(e -> {
            try {
                int balNum = Integer.parseInt(balance.replace("$","").replace(",",""));
                int paying  = Integer.parseInt(amtTF.getText().trim().replace("$","").replace(",",""));
                if (paying <= 0) { showAlert("Please enter a valid amount."); return; }
                if (paying > balNum) { showAlert("Amount cannot exceed balance of " + balance + "."); return; }
                int remaining = balNum - paying;
                if (remaining == 0) { row.set(6,"Paid"); row.set(4,"$0"); if (idx < state.feeData.size()) { state.feeData.get(idx)[6]="Paid"; state.feeData.get(idx)[4]="$0"; } }
                else { row.set(6,"Partial"); row.set(4,"$"+remaining); if (idx < state.feeData.size()) { state.feeData.get(idx)[6]="Partial"; state.feeData.get(idx)[4]="$"+remaining; } }
                allData.set(idx, row);
                showAlert("Payment of $"+paying+" received via "+method.getValue()+".\n"+(remaining==0?"✅ Fully paid!":"Remaining balance: $"+remaining));
                d.close();
            } catch (NumberFormatException ex) { showAlert("Please enter a valid number."); }
        });
        btns.getChildren().addAll(cancel, confirm);
        root.setCenter(sp); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
