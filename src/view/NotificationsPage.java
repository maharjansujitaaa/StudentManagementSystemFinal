
package view;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import static view.UIHelper.*;

public class NotificationsPage {

    private final AppState state;
    private final Stage owner;
    private Label subtitleLabel; // injected so we can update unread count

    public NotificationsPage(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public void setSubtitleLabel(Label lbl) { this.subtitleLabel = lbl; }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + C_BG + "; -fx-background:" + C_BG + ";");

        VBox page = new VBox(0);
        page.setPadding(new Insets(20, 28, 20, 28));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        String[] tabNames = {"All","Unread","Announcements","Reminders","Alerts"};
        HBox tabBar = new HBox(8); tabBar.setPadding(new Insets(0,0,16,0));
        VBox listBox = new VBox(10); listBox.setStyle("-fx-background-color:" + C_BG + ";");

        Runnable[] refresh = {null};
        String[] activeFilter = {"All"};

        refresh[0] = () -> {
            listBox.getChildren().clear();
            for (int i = 0; i < state.notifData.size(); i++) {
                final int idx = i;
                Object[] n = state.notifData.get(i);
                String type = n[2].toString(), filter = activeFilter[0];
                boolean read = (boolean) n[6];
                if (!filter.equals("All")) {
                    if (filter.equals("Unread") && read) continue;
                    if (filter.equals("Announcements") && !type.equals("announcement")) continue;
                    if (filter.equals("Reminders")     && !type.equals("reminder"))     continue;
                    if (filter.equals("Alerts")        && !type.equals("alert"))        continue;
                }
                listBox.getChildren().add(buildCard(n, idx, refresh));
            }
            long u = state.notifData.stream().filter(n -> !(boolean) n[6]).count();
            if (subtitleLabel != null) subtitleLabel.setText("Manage announcements and reminders (" + u + " unread)");
        };

        Button[] tabBtns = new Button[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            final String f = tabNames[i];
            Button btn = new Button(f);
            String activeStyle  = "-fx-background-color:white; -fx-text-fill:" + C_TEXT + "; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            String inactiveStyle = "-fx-background-color:#F1F5F9; -fx-text-fill:" + C_MUTED + "; -fx-font-size:13px; -fx-background-radius:8; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            btn.setStyle(i == 0 ? activeStyle : inactiveStyle);
            btn.setOnAction(e -> {
                activeFilter[0] = f;
                for (Button b : tabBtns) b.setStyle(inactiveStyle);
                btn.setStyle(activeStyle);
                refresh[0].run();
            });
            tabBtns[i] = btn;
            tabBar.getChildren().add(btn);
        }

        refresh[0].run();
        page.getChildren().addAll(tabBar, listBox);
        scroll.setContent(page);
        return scroll;
    }

    public void showCreateDialog(Runnable onDone) {
        showNotifForm(null, -1, onDone);
    }

    private VBox buildCard(Object[] n, int idx, Runnable[] refresh) {
        String title    = n[0].toString();
        String body     = n[1].toString();
        String type     = n[2].toString();
        String priority = n[3].toString();
        String target   = n[4].toString();
        String date     = n[5].toString();
        boolean read    = (boolean) n[6];

        String borderColor = priority.equalsIgnoreCase("high") ? C_RED : priority.equalsIgnoreCase("medium") ? C_ORANGE : C_BORDER;
        String bgColor     = read ? C_WHITE : "#FFFBEB";

        VBox card = new VBox(8); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + bgColor + "; -fx-border-color:" + borderColor + " " + C_BORDER + " " + C_BORDER + " " + borderColor + "; -fx-border-width:1 1 1 4; -fx-border-radius:8; -fx-background-radius:8;");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Title + New badge
        HBox titleRow = new HBox(8); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        titleRow.getChildren().add(titleLbl);
        if (!read) {
            Label newBadge = new Label(" New "); newBadge.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:2 6 2 6;");
            titleRow.getChildren().add(newBadge);
        }

        // Action buttons
        HBox actions = new HBox(8); actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button("✏"); editBtn.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:" + C_MUTED + ";");
        editBtn.setOnAction(e -> { n[6] = true; showNotifForm(n, idx, refresh[0]); });
        if (!read) {
            Button markBtn = new Button("✓"); markBtn.setStyle("-fx-background-color:transparent; -fx-font-size:16px; -fx-cursor:hand; -fx-text-fill:" + C_GREEN + ";");
            markBtn.setOnAction(e -> { n[6] = true; refresh[0].run(); });
            actions.getChildren().add(markBtn);
        }
        Button delBtn = new Button("🗑"); delBtn.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:" + C_RED + ";");
        delBtn.setOnAction(e -> { if (confirmDelete()) { state.notifData.remove(idx); refresh[0].run(); } });
        actions.getChildren().addAll(editBtn, delBtn);

        HBox headerRow = new HBox(); HBox.setHgrow(titleRow, Priority.ALWAYS); headerRow.getChildren().addAll(titleRow, actions);

        Label bodyLbl = new Label(body); bodyLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + C_MUTED + ";"); bodyLbl.setWrapText(true);

        HBox tags = new HBox(8); tags.setAlignment(Pos.CENTER_LEFT);
        tags.getChildren().addAll(
            makeTag(type, null),
            makeTag(priority, priority.equalsIgnoreCase("high") ? C_RED : priority.equalsIgnoreCase("medium") ? C_ORANGE : null),
            makeTag("For: " + target, null),
            lbl(date, "-fx-font-size:11px; -fx-text-fill:" + C_MUTED + ";")
        );

        card.getChildren().addAll(headerRow, bodyLbl, tags);
        card.setOnMouseClicked(e -> { n[6] = true; showNotifForm(n, idx, refresh[0]); });
        return card;
    }

    private Label makeTag(String text, String bgColor) {
        Label l = new Label(text);
        if (bgColor != null) l.setStyle("-fx-background-color:" + bgColor + "; -fx-text-fill:white; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:3 8 3 8;");
        else l.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:" + C_TEXT + "; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:3 8 3 8; -fx-border-color:" + C_BORDER + "; -fx-border-radius:6;");
        return l;
    }

    private void showNotifForm(Object[] existing, int idx, Runnable onDone) {
        boolean isEdit = existing != null;
        Stage d = new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL);
        d.setTitle(isEdit ? "Edit Notification" : "Create New Notification");
        d.setWidth(540); d.setHeight(520);

        BorderPane root = new BorderPane(); root.setStyle("-fx-background-color:white;");
        root.setTop(makeDialogHeader(isEdit ? "Edit Notification" : "Create New Notification", d));

        VBox form = new VBox(0); form.setPadding(new Insets(0,24,20,24)); form.setStyle("-fx-background-color:white;");

        Label tl = new Label("Title *"); tl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";"); VBox.setMargin(tl, new Insets(16,0,6,0));
        TextField titleTF = new TextField(isEdit ? existing[0].toString() : "");
        titleTF.setPrefHeight(44); titleTF.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-background-radius:8; -fx-font-size:13px; -fx-padding:0 12 0 12;");

        Label ml = new Label("Message *"); ml.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";"); VBox.setMargin(ml, new Insets(14,0,6,0));
        TextArea msgTA = new TextArea(isEdit ? existing[1].toString() : "");
        msgTA.setPrefRowCount(4); msgTA.setWrapText(true);
        msgTA.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:" + C_BORDER + "; -fx-border-radius:8; -fx-font-size:13px;");

        // Type + Priority in a GridPane (50/50)
        GridPane typeRow = new GridPane(); typeRow.setHgap(14); VBox.setMargin(typeRow, new Insets(14,0,0,0));
        ColumnConstraints tc1 = new ColumnConstraints(); tc1.setPercentWidth(50); tc1.setHgrow(Priority.ALWAYS);
        ColumnConstraints tc2 = new ColumnConstraints(); tc2.setPercentWidth(50); tc2.setHgrow(Priority.ALWAYS);
        typeRow.getColumnConstraints().addAll(tc1, tc2); typeRow.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> typeBox = new ComboBox<>(); typeBox.getItems().addAll("Select type","announcement","reminder","alert");
        typeBox.setValue(isEdit ? existing[2].toString() : "Select type"); typeBox.setPrefHeight(44); typeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> priBox = new ComboBox<>(); priBox.getItems().addAll("Select priority","High","Medium","Low");
        priBox.setValue(isEdit ? existing[3].toString() : "Select priority"); priBox.setPrefHeight(44); priBox.setMaxWidth(Double.MAX_VALUE);

        VBox typeCol = new VBox(6, lbl("Type *","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), typeBox);
        VBox priCol  = new VBox(6, lbl("Priority *","-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"), priBox);
        typeRow.add(typeCol, 0, 0); typeRow.add(priCol, 1, 0);

        Label tarLbl = new Label("Target Audience *"); tarLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + C_TEXT + ";"); VBox.setMargin(tarLbl, new Insets(14,0,6,0));
        ComboBox<String> tarBox = new ComboBox<>(); tarBox.getItems().addAll("Select audience","All","Students only","Teachers only","Students & Teachers","Parents");
        tarBox.setValue(isEdit ? existing[4].toString() : "Select audience"); tarBox.setPrefHeight(44); tarBox.setPrefWidth(Double.MAX_VALUE);

        form.getChildren().addAll(tl, titleTF, ml, msgTA, typeRow, tarLbl, tarBox);
        ScrollPane sp = new ScrollPane(form); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:white; -fx-background:white;");

        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(12,24,14,24));
        btns.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");
        Button cancel = makeSecondaryBtn("Cancel"); cancel.setOnAction(e -> d.close());
        Button send = new Button(isEdit ? "  ✓  Update" : "  ➤  Send Notification");
        send.setStyle("-fx-background-color:" + C_TEXT + "; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20 10 20;");
        send.setOnAction(e -> {
            if (titleTF.getText().trim().isEmpty())         { showAlert("Title is required."); return; }
            if (msgTA.getText().trim().isEmpty())           { showAlert("Message is required."); return; }
            if (typeBox.getValue().startsWith("Select"))   { showAlert("Please select a type."); return; }
            if (priBox.getValue().startsWith("Select"))    { showAlert("Please select a priority."); return; }
            if (tarBox.getValue().startsWith("Select"))    { showAlert("Please select a target audience."); return; }
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            if (isEdit) {
                existing[0]=titleTF.getText().trim(); existing[1]=msgTA.getText().trim();
                existing[2]=typeBox.getValue(); existing[3]=priBox.getValue();
                existing[4]=tarBox.getValue(); existing[5]=today;
                showAlert("Notification updated!");
            } else {
                state.notifData.add(0, new Object[]{titleTF.getText().trim(), msgTA.getText().trim(),
                    typeBox.getValue(), priBox.getValue(), tarBox.getValue(), today, false});
                showAlert("Notification sent!");
            }
            d.close();
            if (onDone != null) onDone.run();
        });
        btns.getChildren().addAll(cancel, send);
        root.setCenter(sp); root.setBottom(btns);
        d.setScene(new javafx.scene.Scene(root)); d.show();
    }
}
