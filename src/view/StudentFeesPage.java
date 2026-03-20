package view;

import controller.StudentController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static view.StudentUIHelper.*;

/**
 * StudentFeesPage — fee status + Pay Now (full form with card/UPI details) + Receipt viewer.
 */
public class StudentFeesPage {

    private final StudentController controller;
    private final Stage             owner;

    // Track which fees have been paid this session
    private final java.util.Set<Integer> paidThisSession = new java.util.HashSet<>();

    public StudentFeesPage(StudentController controller, Stage owner) {
        this.controller = controller;
        this.owner      = owner;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(20);
        page.setPadding(new Insets(24,32,24,32));
        page.setStyle("-fx-background-color:"+C_BG+";");

        // ── Status banner ─────────────────────────────────────────────────
        boolean hasPending = controller.hasPendingFees();
        HBox banner = makeBanner(hasPending);

        // ── Summary cards ─────────────────────────────────────────────────
        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            feeCard("Total Fees",  "$10,250","This academic year", C_BLUE),
            feeCard("Paid",        "$7,800",  "Cleared",           C_GREEN),
            feeCard("Pending",     "$2,500",  "Due March 1",       C_ORANGE),
            feeCard("Overdue",     "$150",    "Sports Fee",        C_RED)
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Fee table ─────────────────────────────────────────────────────
        VBox tableCard = new VBox(10); tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10;");
        Label tableTitle = lbl("Fee Details","-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        for (String[] r : controller.getFeeHistory())
            tableData.add(FXCollections.observableArrayList(r));

        TableView<ObservableList<String>> tv = buildFeeTable(tableData, page, banner);
        tableCard.getChildren().addAll(tableTitle, tv);
        page.getChildren().addAll(banner, cards, tableCard);
        scroll.setContent(page);
        return scroll;
    }

    // ── Banner ────────────────────────────────────────────────────────────
    private HBox makeBanner(boolean hasPending) {
        HBox banner = new HBox(14); banner.setPadding(new Insets(16,20,16,20)); banner.setAlignment(Pos.CENTER_LEFT);
        String bg    = hasPending ? "#FEF3C7" : "#DCFCE7";
        String border= hasPending ? C_ORANGE  : C_GREEN;
        String title = hasPending ? controller.getFeeStatusSummary() : "All fees are paid for this term!";
        String sub   = hasPending ? "Please make payment to avoid late fees." : "Next payment due: June 1, 2026";
        String icon  = hasPending ? "⚠️" : "✅";
        String tc    = hasPending ? "#92400E" : "#166534";
        String sc    = hasPending ? "#B45309" : "#16A34A";
        banner.setStyle("-fx-background-color:"+bg+"; -fx-border-color:"+border+"; -fx-border-width:0 0 0 4; -fx-border-radius:8; -fx-background-radius:8;");
        Label bi = new Label(icon); bi.setStyle("-fx-font-size:22px;");
        VBox bt = new VBox(3, lbl(title,"-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+tc+";"),
                              lbl(sub,  "-fx-font-size:12px; -fx-text-fill:"+sc+";"));
        banner.getChildren().addAll(bi, bt);
        return banner;
    }

    // ── Fee table with Pay Now + View Receipt ────────────────────────────
    private TableView<ObservableList<String>> buildFeeTable(
            ObservableList<ObservableList<String>> tableData, VBox page, HBox banner) {

        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;"); tv.setItems(tableData);
        tv.setRowFactory(r->{TableRow<ObservableList<String>> row=new TableRow<>();row.setPrefHeight(52);return row;});

        // Data columns
        String[] cols  = {"Fee Type","Amount","Paid","Balance","Due Date","Status"};
        int[]    widths = {210,90,90,90,110,110};
        for (int i=0;i<cols.length;i++){
            final int fi=i;
            TableColumn<ObservableList<String>,String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i==5) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    String bg=item.equals("Paid")?C_GREEN:item.equals("Overdue")?C_RED:C_ORANGE;
                    setGraphic(badge(item,bg,"white"));}
            });
            tv.getColumns().add(col);
        }

        // Actions column — Pay Now / View Receipt
        TableColumn<ObservableList<String>,Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(200);
        actCol.setCellFactory(c -> new TableCell<ObservableList<String>,Void>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx<0||idx>=tableData.size()) { setGraphic(null); return; }
                ObservableList<String> row = tableData.get(idx);
                String status = row.get(5);
                HBox box = new HBox(8); box.setAlignment(Pos.CENTER_LEFT);

                if (status.equals("Paid") || paidThisSession.contains(idx)) {
                    // Show receipt button
                    Button rb = new Button("🧾 View Receipt");
                    rb.setStyle("-fx-background-color:#F0FDF4; -fx-text-fill:"+C_GREEN+"; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:"+C_GREEN+"; -fx-border-radius:6; -fx-cursor:hand; -fx-padding:6 12 6 12;");
                    rb.setOnAction(e -> showReceipt(row, idx));
                    box.getChildren().add(rb);
                } else {
                    // Pay Now button
                    Button pay = new Button("💳 Pay Now");
                    pay.setStyle("-fx-background-color:"+C_ACCENT+"; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand; -fx-padding:6 14 6 14;");
                    pay.setOnAction(e -> showPaymentDialog(row, idx, tableData, page, banner));
                    box.getChildren().add(pay);
                }
                setGraphic(box);
            }
        });
        tv.getColumns().add(actCol);
        return tv;
    }

    // ════════════════════════════════════════════════════════════════════════
    // PAYMENT DIALOG — full form with method-specific fields
    // ════════════════════════════════════════════════════════════════════════
    private void showPaymentDialog(ObservableList<String> row, int idx,
            ObservableList<ObservableList<String>> tableData, VBox page, HBox banner) {

        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Fee Payment"); d.setWidth(540); d.setHeight(620);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");

        // Header
        HBox hdr = new HBox(); hdr.setPadding(new Insets(16,24,12,24)); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color:"+C_ACCENT+"; ");
        Label htl = lbl("💳  Pay Fee","-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        HBox.setHgrow(htl, Priority.ALWAYS);
        Button xb = new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand; -fx-text-fill:white;");
        xb.setOnAction(e->d.close()); hdr.getChildren().addAll(htl,xb); root.setTop(hdr);

        VBox body = new VBox(0); body.setStyle("-fx-background-color:white;");

        // Fee summary strip
        HBox strip = new HBox(20); strip.setPadding(new Insets(14,24,14,24)); strip.setAlignment(Pos.CENTER_LEFT);
        strip.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        VBox feeInfo = new VBox(4); HBox.setHgrow(feeInfo, Priority.ALWAYS);
        feeInfo.getChildren().addAll(
            lbl(row.get(0), "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            lbl("Due Date: "+row.get(4), "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";")
        );
        Label amtLbl = lbl(row.get(3),"-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+C_RED+";");
        strip.getChildren().addAll(feeInfo, amtLbl);

        // Payment method selector
        VBox formArea = new VBox(14); formArea.setPadding(new Insets(18,24,16,24));
        Label methodLbl = lbl("Payment Method *","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        // Method toggle buttons
        String[] methods = {"Credit Card","Debit Card","Net Banking","UPI","Cash"};
        String[] icons   = {"💳","💳","🏦","📱","💵"};
        HBox methodRow = new HBox(8); methodRow.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup tg = new ToggleGroup();
        for (int i=0;i<methods.length;i++) {
            ToggleButton tb = new ToggleButton(icons[i]+" "+methods[i]);
            tb.setToggleGroup(tg);
            tb.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:"+C_TEXT+"; -fx-font-size:12px; -fx-background-radius:8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:8 12 8 12;");
            tb.selectedProperty().addListener((obs,o,n)->tb.setStyle(n?"-fx-background-color:"+C_ACCENT+"; -fx-text-fill:white; -fx-font-size:12px; -fx-background-radius:8; -fx-border-color:"+C_ACCENT+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:8 12 8 12;":"-fx-background-color:#F1F5F9; -fx-text-fill:"+C_TEXT+"; -fx-font-size:12px; -fx-background-radius:8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:8 12 8 12;"));
            if (i==0) tb.setSelected(true);
            methodRow.getChildren().add(tb);
        }

        // Dynamic form area (changes based on selected method)
        VBox dynamicForm = new VBox(12);
        dynamicForm.setPadding(new Insets(12,0,0,0));
        showCardForm(dynamicForm); // default: Credit Card

        tg.selectedToggleProperty().addListener((obs,o,n) -> {
            if (n==null){tg.selectToggle(o);return;}
            String sel = ((ToggleButton)n).getText();
            dynamicForm.getChildren().clear();
            if (sel.contains("Net Banking")) showNetBankingForm(dynamicForm);
            else if (sel.contains("UPI"))    showUPIForm(dynamicForm);
            else if (sel.contains("Cash"))   showCashForm(dynamicForm);
            else                             showCardForm(dynamicForm);
        });

        formArea.getChildren().addAll(methodLbl, methodRow, dynamicForm);
        body.getChildren().addAll(strip, formArea);
        ScrollPane sp = new ScrollPane(body); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:white; -fx-background:white;");
        root.setCenter(sp);

        // Footer buttons
        HBox footer = new HBox(12); footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12,24,14,24));
        footer.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button cancel = secondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        Button confirm = primaryBtn("✅  Confirm Payment of "+row.get(3));
        confirm.setOnAction(e -> {
            // Validate: if card method selected, check card number filled
            ToggleButton selBtn = (ToggleButton) tg.getSelectedToggle();
            String selMethod = selBtn != null ? selBtn.getText() : "Cash";
            if (!validatePayment(dynamicForm, selMethod)) return;
            // Generate transaction ID
            String txnId = "TXN"+String.format("%08d", (int)(Math.random()*100000000));
            String method = selMethod.replace("💳 ","").replace("🏦 ","").replace("📱 ","").replace("💵 ","");
            d.close();
            // Mark as paid in table
            row.set(2, row.get(1)); // paid = amount
            row.set(3, "$0");       // balance = 0
            row.set(5, "Paid");     // status
            paidThisSession.add(idx);
            // Show receipt
            showReceipt(row, idx, txnId, method);
        });
        footer.getChildren().addAll(cancel, confirm);
        root.setBottom(footer);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Dynamic form sections ─────────────────────────────────────────────
    private void showCardForm(VBox form) {
        // Card Number
        form.getChildren().add(fieldBlock("Card Number *", "1234  5678  9012  3456", false));
        // Cardholder Name
        form.getChildren().add(fieldBlock("Cardholder Name *", "As printed on card", false));
        form.getChildren().add(fieldBlock("Expiry Date *", "MM/YY", false));
        // Billing Address
        form.getChildren().add(fieldBlock("Billing Address", "Optional", false));
    }

    private void showNetBankingForm(VBox form) {
        form.getChildren().addAll(
            fieldBlock("User ID / Customer ID *", "Enter your net banking user ID", false),
            fieldBlock("Password *",              "Enter your net banking password", true)
        );
        Label note = lbl("🔒  You will be redirected to your bank's secure portal.","-fx-font-size:12px; -fx-text-fill:"+C_MUTED+"; -fx-background-color:#EFF6FF; -fx-padding:10 12 10 12; -fx-background-radius:6;");
        note.setWrapText(true); form.getChildren().add(note);
    }

    private void showUPIForm(VBox form) {
        form.getChildren().add(fieldBlock("UPI ID *", "yourname@upi", false));
        Label note = lbl("📱  Enter your UPI ID (e.g., 9876543210@paytm) and confirm payment in your UPI app.","-fx-font-size:12px; -fx-text-fill:"+C_MUTED+"; -fx-background-color:#F0FDF4; -fx-padding:10 12 10 12; -fx-background-radius:6;");
        note.setWrapText(true); form.getChildren().add(note);
    }

    private void showCashForm(VBox form) {
        Label note = lbl("💵  Cash payment must be made at the school accounts office.\nBring this confirmation slip along with the exact amount.","-fx-font-size:13px; -fx-text-fill:"+C_TEXT+"; -fx-background-color:#FFFBEB; -fx-padding:14 16 14 16; -fx-background-radius:8; -fx-border-color:"+C_ORANGE+"; -fx-border-width:0 0 0 3; -fx-border-radius:8;");
        note.setWrapText(true);
        Label officeHrs = lbl("🕘  Office hours: Mon–Fri, 9:00 AM – 3:00 PM","-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";");
        form.getChildren().addAll(note, officeHrs);
    }

    /** Single labelled field block — returns a VBox ready to add to any layout */
    private VBox fieldBlock(String label, String prompt, boolean isPassword) {
        VBox box = new VBox(4);
        Label lbl2 = lbl(label, "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        Control field;
        if (isPassword) {
            PasswordField pf = new PasswordField(); pf.setPromptText(prompt); pf.setPrefHeight(38);
            pf.setMaxWidth(Double.MAX_VALUE);
            pf.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            field = pf;
        } else {
            TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefHeight(38);
            tf.setMaxWidth(Double.MAX_VALUE);
            tf.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
            field = tf;
        }
        box.getChildren().addAll(lbl2, field);
        return box;
    }

    private HBox formField(String label, String prompt, boolean isPassword) {
        HBox wrap = new HBox(fieldBlock(label, prompt, isPassword));
        HBox.setHgrow(wrap.getChildren().get(0), Priority.ALWAYS);
        wrap.setMaxWidth(Double.MAX_VALUE);
        return wrap;
    }

    private boolean validatePayment(VBox form, String method) {
        if (method.contains("Cash") || method.contains("Net Banking") || method.contains("UPI")) return true;
        // Card validation: find the Card Number fieldBlock (first TextField in form)
        for (javafx.scene.Node n : form.getChildren()) {
            if (n instanceof VBox) {
                VBox block = (VBox) n;
                for (javafx.scene.Node child : block.getChildren()) {
                    if (child instanceof TextField) {
                        TextField tf = (TextField) child;
                        if (tf.getPromptText().contains("1234") && tf.getText().trim().isEmpty()) {
                            showValidationError("Please enter your card number.");
                            return false;
                        }
                    }
                }
            }
            // GridPane row (Expiry/CVV)
            if (n instanceof GridPane) {
                for (javafx.scene.Node gn : ((GridPane) n).getChildren()) {
                    if (gn instanceof VBox) {
                        for (javafx.scene.Node inner : ((VBox) gn).getChildren()) {
                            if (inner instanceof TextField) {
                                TextField tf = (TextField) inner;
                                if (tf.getPromptText().equals("MM/YY") && tf.getText().trim().isEmpty()) {
                                    showValidationError("Please enter the expiry date.");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void showValidationError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK); a.setHeaderText(null); a.showAndWait();
    }

    // ════════════════════════════════════════════════════════════════════════
    // RECEIPT DIALOG
    // ════════════════════════════════════════════════════════════════════════
    private void showReceipt(ObservableList<String> row, int idx) {
        showReceipt(row, idx, "TXN" + String.format("%08d", idx * 12345 + 67890), "Previously Paid");
    }

    private void showReceipt(ObservableList<String> row, int idx, String txnId, String method) {
        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Payment Receipt"); d.setWidth(480); d.setHeight(560);
        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");

        // Header bar
        HBox hdr = new HBox(); hdr.setPadding(new Insets(16,24,12,24)); hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color:"+C_GREEN+";");
        Label htl = lbl("🧾  Payment Receipt","-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        HBox.setHgrow(htl, Priority.ALWAYS);
        Button xb = new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand; -fx-text-fill:white;");
        xb.setOnAction(e->d.close()); hdr.getChildren().addAll(htl,xb); root.setTop(hdr);

        VBox body = new VBox(0);

        // Success banner
        VBox successBox = new VBox(6); successBox.setAlignment(Pos.CENTER); successBox.setPadding(new Insets(24,24,20,24));
        successBox.setStyle("-fx-background-color:#F0FDF4;");
        Label checkIcon = new Label("✅"); checkIcon.setStyle("-fx-font-size:42px;");
        Label successLbl = lbl("Payment Successful!", "-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#15803D;");
        Label amtPaid = lbl(row.get(1), "-fx-font-size:32px; -fx-font-weight:bold; -fx-text-fill:"+C_GREEN+";");
        successBox.getChildren().addAll(checkIcon, successLbl, amtPaid);

        // Receipt details
        VBox details = new VBox(0); details.setPadding(new Insets(16,24,16,24));
        details.setStyle("-fx-background-color:white;");

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String[][] receiptData = {
            {"Transaction ID", txnId},
            {"Fee Type",       row.get(0)},
            {"Amount Paid",    row.get(1)},
            {"Payment Method", method},
            {"Student",        controller.getStudentName()},
            {"Class",          controller.getStudentClass()},
            {"Roll No.",       controller.getRollNo()},
            {"Payment Date",   date},
            {"Status",         "✅  Paid"},
        };
        for (String[] item : receiptData) {
            HBox row2 = new HBox(); row2.setPadding(new Insets(10,0,10,0));
            row2.setStyle("-fx-border-color:transparent transparent "+C_BORDER+" transparent; -fx-border-width:0 0 1 0;");
            Label key = lbl(item[0], "-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"); key.setMinWidth(160);
            Label val = lbl(item[1], "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
            if (item[0].equals("Status")) val.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_GREEN+";");
            HBox.setHgrow(val, Priority.ALWAYS);
            row2.getChildren().addAll(key, val);
            details.getChildren().add(row2);
        }

        // Note
        Label note = lbl("Keep this receipt for your records. For queries contact the accounts office.",
            "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+"; -fx-wrap-text:true;");
        note.setWrapText(true);
        VBox noteBox = new VBox(note); noteBox.setPadding(new Insets(12,24,8,24));

        body.getChildren().addAll(successBox, details, noteBox);
        ScrollPane sp = new ScrollPane(body); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:white; -fx-background:white;");
        root.setCenter(sp);

        // Footer: Close + Print (simulated)
        HBox footer = new HBox(12); footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12,24,14,24));
        footer.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button print = secondaryBtn("🖨  Print Receipt");
        print.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION,"Printing receipt...\nTransaction ID: "+txnId,ButtonType.OK);
            a.setHeaderText(null); a.showAndWait();
        });
        Button close = primaryBtn("Done"); close.setOnAction(e->d.close());
        footer.getChildren().addAll(print, close);
        root.setBottom(footer);

        d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private VBox feeCard(String label, String value, String sub, String color) {
        VBox card = new VBox(8); card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12;");
        card.getChildren().addAll(
            lbl(label,"-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"),
            lbl(value, "-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:"+color+";"),
            lbl(sub,   "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";")
        );
        return card;
    }
}