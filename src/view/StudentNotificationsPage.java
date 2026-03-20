package view;

import controller.StudentController;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

import static view.StudentUIHelper.*;

/**
 * StudentNotificationsPage — filterable notification list with mark-read.
 */
public class StudentNotificationsPage {

    private final StudentController controller;

    public StudentNotificationsPage(StudentController controller) {
        this.controller = controller;
    }

    public Node build() {
        ScrollPane scroll = bgScroll();
        VBox page = new VBox(12);
        page.setPadding(new Insets(24, 32, 24, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // Filter tabs
        String[] filters = {"All","Unread","Announcements","Reminders"};
        HBox tabBar = new HBox(8); tabBar.setPadding(new Insets(0,0,8,0));
        VBox listBox = new VBox(10);
        Runnable[] refresh = {null};
        String[] activeFilter = {"All"};

        refresh[0] = () -> {
            listBox.getChildren().clear();
            List<Object[]> notifs = controller.getNotifications();
            for (int i = 0; i < notifs.size(); i++) {
                final int idx = i; Object[] n = notifs.get(i);
                String type = n[2].toString(), f = activeFilter[0];
                boolean read = (boolean) n[5];
                if (f.equals("Unread") && read) continue;
                if (f.equals("Announcements") && !type.equals("announcement")) continue;
                if (f.equals("Reminders") && !type.equals("reminder")) continue;
                listBox.getChildren().add(buildCard(n, idx, refresh));
            }
        };

        Button[] tabBtns = new Button[filters.length];
        for (int i = 0; i < filters.length; i++) {
            final String f = filters[i];
            Button btn = new Button(f);
            String act   = "-fx-background-color:"+C_ACCENT+"; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            String inact = "-fx-background-color:#F1F5F9; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-background-radius:8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            btn.setStyle(i == 0 ? act : inact);
            btn.setOnAction(e -> {
                activeFilter[0] = f;
                for (Button b : tabBtns) b.setStyle(inact);
                btn.setStyle(act);
                refresh[0].run();
            });
            tabBtns[i] = btn; tabBar.getChildren().add(btn);
        }

        // Mark all read button
        Button markAll = secondaryBtn("Mark All Read");
        markAll.setOnAction(e -> { controller.markAllRead(); refresh[0].run(); });
        HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(tabBar, spacer, markAll);

        refresh[0].run();
        page.getChildren().addAll(topRow, listBox);
        scroll.setContent(page);
        return scroll;
    }

    private VBox buildCard(Object[] n, int idx, Runnable[] refresh) {
        String priority = n[3].toString(); boolean read = (boolean) n[5];
        String bc = priority.equals("high") ? C_RED : priority.equals("medium") ? C_ORANGE : C_BORDER;

        VBox card = new VBox(8); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:"+(read?C_WHITE:"#FFFBEB")+"; -fx-border-color:"+bc+" "+C_BORDER+" "+C_BORDER+" "+bc+"; -fx-border-width:1 1 1 4; -fx-border-radius:8; -fx-background-radius:8;");

        HBox hdr = new HBox(8); hdr.setAlignment(Pos.CENTER_LEFT);
        Label tl = new Label(n[0].toString());
        tl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        HBox.setHgrow(tl, Priority.ALWAYS);
        hdr.getChildren().add(tl);
        if (!read) {
            Label nb = badge("New","#1D4ED8","white");
            hdr.getChildren().add(nb);
        }

        HBox acts = new HBox(8); acts.setAlignment(Pos.CENTER_RIGHT);
        if (!read) {
            Button mb = new Button("✓ Mark Read");
            mb.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6; -fx-font-size:12px; -fx-cursor:hand; -fx-padding:5 12 5 12; -fx-text-fill:"+C_GREEN+";");
            mb.setOnAction(e -> { controller.markRead(idx); refresh[0].run(); });
            acts.getChildren().add(mb);
        }

        HBox headerRow = new HBox(); HBox.setHgrow(hdr, Priority.ALWAYS);
        headerRow.getChildren().addAll(hdr, acts);

        Label body = new Label(n[1].toString());
        body.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); body.setWrapText(true);

        HBox tags = new HBox(8);
        // type badge
        String typeBg = n[2].toString().equals("announcement") ? C_BLUE : C_ORANGE;
        tags.getChildren().addAll(
            badge(n[2].toString(), typeBg, "white"),
            badge(priority, priority.equals("high")?C_RED:priority.equals("medium")?C_ORANGE:"#9CA3AF", "white"),
            lbl(n[4].toString(), "-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";")
        );

        card.getChildren().addAll(headerRow, body, tags);
        return card;
    }
}
