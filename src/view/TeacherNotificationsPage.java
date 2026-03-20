
package view;

import controller.TeacherController;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.util.*;

import static view.TeacherUIHelper.*;

public class TeacherNotificationsPage {
    private final TeacherController controller;
    private final Stage             owner;
    private       Label             subtitleLbl;

    TeacherNotificationsPage(TeacherController c, Stage owner) { this.controller=c; this.owner=owner; }
    void setSubtitleLbl(Label l) { this.subtitleLbl=l; }

    Node build() {
        ScrollPane scroll=bgScroll();
        VBox page=new VBox(12); page.setPadding(new Insets(20,28,20,28)); page.setStyle("-fx-background-color:"+C_BG+";");
        String[] filters={"All","Unread","Announcements","Reminders"};
        HBox tabBar=new HBox(8); tabBar.setPadding(new Insets(0,0,8,0));
        VBox listBox=new VBox(10); listBox.setStyle("-fx-background-color:"+C_BG+";");
        Runnable[] refresh={null}; String[] af={"All"};
        refresh[0]=()->{
            listBox.getChildren().clear();
            List<Object[]> notifs=controller.getNotifications();
            for(int i=0;i<notifs.size();i++){
                final int idx=i; Object[] n=notifs.get(i);
                String type=n[2].toString(),f=af[0]; boolean read=(boolean)n[5];
                if(!f.equals("All")){if(f.equals("Unread")&&read)continue;if(f.equals("Announcements")&&!type.equals("announcement"))continue;if(f.equals("Reminders")&&!type.equals("reminder"))continue;}
                listBox.getChildren().add(buildCard(n,idx,refresh));
            }
            long u=controller.getUnreadCount(); if(subtitleLbl!=null) subtitleLbl.setText("Manage announcements ("+u+" unread)");
        };
        Button[] tabBtns=new Button[filters.length];
        for(int i=0;i<filters.length;i++){final String f=filters[i];Button btn=new Button(f);
            String act="-fx-background-color:white; -fx-text-fill:"+C_TEXT+"; -fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            String inact="-fx-background-color:#F1F5F9; -fx-text-fill:"+C_MUTED+"; -fx-font-size:13px; -fx-background-radius:8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-cursor:hand; -fx-padding:7 16 7 16;";
            btn.setStyle(i==0?act:inact);
            btn.setOnAction(e->{af[0]=f;for(Button b:tabBtns)b.setStyle(inact);btn.setStyle(act);refresh[0].run();}); tabBtns[i]=btn; tabBar.getChildren().add(btn);}
        refresh[0].run(); page.getChildren().addAll(tabBar,listBox); scroll.setContent(page); return scroll;
    }

    void showCreateDialog(){
        Stage d=new Stage(); d.initOwner(owner); d.initModality(Modality.WINDOW_MODAL); d.setTitle("Create Notification"); d.setWidth(520); d.setHeight(460);
        BorderPane root=new BorderPane(); root.setStyle("-fx-background-color:white;");
        HBox hdr=new HBox(); hdr.setPadding(new Insets(16,24,12,24)); hdr.setAlignment(Pos.CENTER_LEFT); hdr.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:0 0 1 0;");
        Label tl=new Label("Create Notification"); tl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(tl,Priority.ALWAYS);
        Button xb=new Button("×"); xb.setStyle("-fx-background-color:transparent; -fx-font-size:20px; -fx-cursor:hand;"); xb.setOnAction(e->d.close()); hdr.getChildren().addAll(tl,xb); root.setTop(hdr);
        VBox form=new VBox(10); form.setPadding(new Insets(16,24,16,24));
        TextField titleTF=addFormRow(form,"Title *","");
        TextArea msgTA=new TextArea(); msgTA.setPrefRowCount(3); msgTA.setWrapText(true); msgTA.setStyle("-fx-background-color:#F8F9FA; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-font-size:13px;");
        form.getChildren().addAll(boldLabel("Message *"),msgTA);
        GridPane row2=new GridPane(); row2.setHgap(12); row2.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints cc1=new ColumnConstraints(); cc1.setPercentWidth(50); cc1.setHgrow(Priority.ALWAYS);
        ColumnConstraints cc2=new ColumnConstraints(); cc2.setPercentWidth(50); cc2.setHgrow(Priority.ALWAYS);
        row2.getColumnConstraints().addAll(cc1,cc2);
        ComboBox<String> typeBox=new ComboBox<>(); typeBox.getItems().addAll("announcement","reminder","alert"); typeBox.setValue("announcement"); typeBox.setPrefHeight(38); typeBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> priBox=new ComboBox<>(); priBox.getItems().addAll("High","Medium","Low"); priBox.setValue("Medium"); priBox.setPrefHeight(38); priBox.setMaxWidth(Double.MAX_VALUE);
        row2.add(new VBox(4,boldLabel("Type *"),typeBox),0,0); row2.add(new VBox(4,boldLabel("Priority *"),priBox),1,0); form.getChildren().add(row2);
        ScrollPane sp=new ScrollPane(form); sp.setFitToWidth(true); sp.setStyle("-fx-background-color:white; -fx-background:white;"); root.setCenter(sp);
        HBox btns=new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT); btns.setPadding(new Insets(12,24,14,24)); btns.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-width:1 0 0 0;");
        Button cancel=makeSecondaryBtn("Cancel"); cancel.setOnAction(e->d.close());
        Button send=makePrimaryBtn("➤  Send");
        send.setOnAction(e->{if(titleTF.getText().trim().isEmpty()){showAlert("Title required.");return;}if(msgTA.getText().trim().isEmpty()){showAlert("Message required.");return;} controller.addNotification(titleTF.getText().trim(),msgTA.getText().trim(),typeBox.getValue(),priBox.getValue(),"All"); d.close();});
        btns.getChildren().addAll(cancel,send); root.setBottom(btns); d.setScene(new javafx.scene.Scene(root)); d.show();
    }

    private VBox buildCard(Object[] n,int idx,Runnable[] refresh){
        String p=n[3].toString(); boolean read=(boolean)n[5];
        String bc=p.equalsIgnoreCase("High")?C_RED:p.equalsIgnoreCase("Medium")?C_ORANGE:C_BORDER;
        VBox card=new VBox(8); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:"+(read?C_WHITE:"#FFFBEB")+"; -fx-border-color:"+bc+" "+C_BORDER+" "+C_BORDER+" "+bc+"; -fx-border-width:1 1 1 4; -fx-border-radius:8; -fx-background-radius:8;");
        HBox hdr=new HBox(8); hdr.setAlignment(Pos.CENTER_LEFT);
        Label tl=new Label(n[0].toString()); tl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(tl,Priority.ALWAYS); hdr.getChildren().add(tl);
        if(!read){Label nb=new Label(" New "); nb.setStyle("-fx-background-color:"+C_TEXT+"; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:2 6 2 6;"); hdr.getChildren().add(nb);}
        HBox acts=new HBox(8); acts.setAlignment(Pos.CENTER_RIGHT);
        if(!read){Button mb=new Button("✓"); mb.setStyle("-fx-background-color:transparent; -fx-font-size:16px; -fx-cursor:hand; -fx-text-fill:"+C_GREEN+";"); mb.setOnAction(e->{controller.markNotificationRead(idx);refresh[0].run();}); acts.getChildren().add(mb);}
        Button db=new Button("🗑"); db.setStyle("-fx-background-color:transparent; -fx-font-size:15px; -fx-cursor:hand; -fx-text-fill:"+C_RED+";");
        db.setOnAction(e->{if(confirmDelete()) controller.deleteNotification(idx);});
        acts.getChildren().add(db);
        HBox headerRow=new HBox(); HBox.setHgrow(hdr,Priority.ALWAYS); headerRow.getChildren().addAll(hdr,acts);
        Label body=new Label(n[1].toString()); body.setStyle("-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"); body.setWrapText(true);
        HBox tags=new HBox(8); tags.getChildren().addAll(makeTag(n[2].toString(),null),makeTag(p,p.equalsIgnoreCase("High")?C_RED:p.equalsIgnoreCase("Medium")?C_ORANGE:null),lbl(n[4].toString(),"-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"));
        card.getChildren().addAll(headerRow,body,tags); return card;
    }
    private Label makeTag(String text,String bg){Label l=new Label(text);if(bg!=null)l.setStyle("-fx-background-color:"+bg+"; -fx-text-fill:white; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:3 8 3 8;");else l.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:"+C_TEXT+"; -fx-font-size:11px; -fx-background-radius:6; -fx-padding:3 8 3 8; -fx-border-color:"+C_BORDER+"; -fx-border-radius:6;");return l;}
}