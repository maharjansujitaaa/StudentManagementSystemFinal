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
 * StudentLeavePage — apply for leave + view leave history.
 */
public class StudentLeavePage {

    private final StudentController controller;
    private final Stage             owner;

    public StudentLeavePage(StudentController controller, Stage owner) {
        this.controller = controller;
        this.owner      = owner;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 32, 24, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Summary cards ─────────────────────────────────────────────────
        long total    = controller.getLeaveApplications().size();
        long approved = controller.getLeaveApplications().stream().filter(l->l[6].equals("Approved")).count();
        long pending  = controller.getLeaveApplications().stream().filter(l->l[6].equals("Pending")).count();
        long rejected = controller.getLeaveApplications().stream().filter(l->l[6].equals("Rejected")).count();

        HBox summCards = new HBox(16);
        summCards.getChildren().addAll(
            leaveCard("Total Applied",  String.valueOf(total),    "📋", C_BLUE),
            leaveCard("Approved",       String.valueOf(approved), "✅", C_GREEN),
            leaveCard("Pending",        String.valueOf(pending),  "⏳", C_ORANGE),
            leaveCard("Rejected",       String.valueOf(rejected), "❌", C_RED)
        );
        for (Node n : summCards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Apply for leave form card ──────────────────────────────────────
        VBox formCard = buildApplyForm(page, summCards);

        // ── Leave history ─────────────────────────────────────────────────
        VBox histCard = buildHistory();

        page.getChildren().addAll(summCards, formCard, histCard);
        scroll.setContent(page);
        return scroll;
    }

    // ── Apply Form ────────────────────────────────────────────────────────
    private VBox buildApplyForm(VBox page, HBox summCards) {
        VBox card = new VBox(16); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12;");

        HBox titleRow = new HBox(8); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = lbl("📝  Apply for Leave", "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        titleRow.getChildren().add(title);

        // Form grid
        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        // Leave Type
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Sick Leave","Medical Leave","Personal Leave","Family Emergency","Event / Festival","Other");
        typeBox.setValue("Sick Leave"); typeBox.setPrefHeight(38); typeBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(formBlock("Leave Type *", typeBox), 0, 0);

        // Days count label (auto-calc)
        Label daysLbl = lbl("0 day(s)", "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_ACCENT+";");

        // From Date
        DatePicker fromDP = new DatePicker(java.time.LocalDate.now());
        fromDP.setPrefHeight(38); fromDP.setMaxWidth(Double.MAX_VALUE);
        grid.add(formBlock("From Date *", fromDP), 1, 0);

        // To Date
        DatePicker toDP = new DatePicker(java.time.LocalDate.now());
        toDP.setPrefHeight(38); toDP.setMaxWidth(Double.MAX_VALUE);
        VBox toBlock = formBlock("To Date *", toDP);
        grid.add(toBlock, 0, 1);

        // Auto calc days
        Runnable calcDays = () -> {
            if (fromDP.getValue() != null && toDP.getValue() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(fromDP.getValue(), toDP.getValue()) + 1;
                if (days < 1) days = 1;
                daysLbl.setText(days + " day(s)");
            }
        };
        fromDP.setOnAction(e -> calcDays.run());
        toDP.setOnAction(e -> calcDays.run());

        VBox daysBlock = new VBox(4,
            lbl("Number of Days", "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            daysLbl);
        grid.add(daysBlock, 1, 1);

        // Reason
        TextArea reasonTA = new TextArea(); reasonTA.setPromptText("Briefly explain the reason for leave...");
        reasonTA.setPrefRowCount(3); reasonTA.setWrapText(true);
        reasonTA.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-font-size:13px;");
        VBox reasonBlock = new VBox(4,
            lbl("Reason *", "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            reasonTA);
        GridPane.setColumnSpan(reasonBlock, 2);
        grid.add(reasonBlock, 0, 2);

        // Supporting document note
        Label docNote = lbl("📎  For Medical Leave, attach a doctor's certificate at the school office.",
            "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+"; -fx-background-color:#EFF6FF; -fx-padding:8 12 8 12; -fx-background-radius:6;");
        docNote.setWrapText(true);
        GridPane.setColumnSpan(docNote, 2);
        grid.add(docNote, 0, 3);

        // Submit button
        Button submitBtn = primaryBtn("📨  Submit Leave Application");
        submitBtn.setPrefWidth(200);
        submitBtn.setOnAction(e -> {
            if (reasonTA.getText().trim().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please enter a reason for the leave.", ButtonType.OK);
                a.setHeaderText(null); a.showAndWait(); return;
            }
            if (fromDP.getValue() == null || toDP.getValue() == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please select from and to dates.", ButtonType.OK);
                a.setHeaderText(null); a.showAndWait(); return;
            }
            String from  = fromDP.getValue().toString();
            String to    = toDP.getValue().toString();
            String days  = String.valueOf(java.time.temporal.ChronoUnit.DAYS.between(fromDP.getValue(), toDP.getValue()) + 1);
            String today = java.time.LocalDate.now().toString();
            String id    = controller.nextLeaveId();
            controller.addLeaveApplication(new String[]{
                id, typeBox.getValue(), from, to, days,
                reasonTA.getText().trim(), "Pending", today, "Under review"
            });
            reasonTA.clear();
            fromDP.setValue(java.time.LocalDate.now());
            toDP.setValue(java.time.LocalDate.now());
            daysLbl.setText("0 day(s)");
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "✅ Leave application submitted!\nApplication ID: "+id+"\nStatus: Pending review.",
                ButtonType.OK);
            a.setHeaderText(null); a.showAndWait();
            // refresh page
            page.getChildren().setAll(rebuildSummaryCards(), buildApplyForm(page, summCards), buildHistory());
        });

        HBox btnRow = new HBox(submitBtn); btnRow.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().addAll(titleRow, new Separator(), grid, btnRow);
        return card;
    }

    private HBox rebuildSummaryCards() {
        long total    = controller.getLeaveApplications().size();
        long approved = controller.getLeaveApplications().stream().filter(l->l[6].equals("Approved")).count();
        long pending  = controller.getLeaveApplications().stream().filter(l->l[6].equals("Pending")).count();
        long rejected = controller.getLeaveApplications().stream().filter(l->l[6].equals("Rejected")).count();
        HBox row = new HBox(16);
        row.getChildren().addAll(
            leaveCard("Total Applied", String.valueOf(total),    "📋", C_BLUE),
            leaveCard("Approved",      String.valueOf(approved), "✅", C_GREEN),
            leaveCard("Pending",       String.valueOf(pending),  "⏳", C_ORANGE),
            leaveCard("Rejected",      String.valueOf(rejected), "❌", C_RED)
        );
        for (Node n : row.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        return row;
    }

    // ── Leave History Table ───────────────────────────────────────────────
    private VBox buildHistory() {
        VBox card = new VBox(12); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12;");

        HBox titleRow = new HBox(8); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = lbl("📋  Leave History", "-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        HBox.setHgrow(title, Priority.ALWAYS);

        // Filter
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll("All","Approved","Pending","Rejected");
        filter.setValue("All"); filter.setPrefHeight(34);
        titleRow.getChildren().addAll(title, lbl("Filter:", "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"), filter);

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (String[] l : controller.getLeaveApplications())
            data.add(FXCollections.observableArrayList(l));

        TableView<ObservableList<String>> tv = new TableView<>();
        tv.setStyle("-fx-font-size:13px;");
        tv.setRowFactory(r->{TableRow<ObservableList<String>> row=new TableRow<>();row.setPrefHeight(50);return row;});

        String[] cols  = {"ID","Leave Type","From","To","Days","Reason","Status","Applied On","Remarks"};
        int[]    widths = {65,140,100,100,50,150,100,110,150};
        for (int i=0;i<cols.length;i++){
            final int fi=i;
            TableColumn<ObservableList<String>,String> col=new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().size()>fi?d.getValue().get(fi):""));
            if (i==6) col.setCellFactory(c->new TableCell<ObservableList<String>,String>(){
                @Override protected void updateItem(String item,boolean empty){
                    super.updateItem(item,empty); if(empty||item==null){setGraphic(null);return;}
                    String bg=item.equals("Approved")?C_GREEN:item.equals("Pending")?C_ORANGE:C_RED;
                    setGraphic(badge(item,bg,"white"));}
            });
            tv.getColumns().add(col);
        }
        tv.setItems(data);

        filter.setOnAction(e -> {
            data.clear();
            for (String[] l : controller.getLeaveApplications()) {
                if (filter.getValue().equals("All") || l[6].equals(filter.getValue()))
                    data.add(FXCollections.observableArrayList(l));
            }
        });

        card.getChildren().addAll(titleRow, tv);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private VBox leaveCard(String label, String value, String icon, String color) {
        VBox card = new VBox(8); card.setPadding(new Insets(18)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12;");
        HBox top = new HBox(); top.setAlignment(Pos.CENTER_LEFT);
        Label nl = lbl(label, "-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); HBox.setHgrow(nl, Priority.ALWAYS);
        Label il = new Label(icon); il.setStyle("-fx-font-size:18px;");
        top.getChildren().addAll(nl, il);
        Label val = lbl(value, "-fx-font-size:30px; -fx-font-weight:bold; -fx-text-fill:"+color+";");
        card.getChildren().addAll(top, val);
        return card;
    }

    private VBox formBlock(String label, Control field) {
        return new VBox(4,
            lbl(label, "-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"),
            field);
    }
}