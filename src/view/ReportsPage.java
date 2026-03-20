
package view;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import model.DatabaseConnection;
import java.sql.*;
import java.util.*;

import static view.UIHelper.*;

public class ReportsPage {

    private final AppState state;
    private final Stage owner;

    public ReportsPage(AppState state, Stage owner) {
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

        // Summary cards
        HBox cards = new HBox(14);
        cards.getChildren().addAll(
            makeReportCard("Avg Attendance",  "92.5%",   "This semester", C_BLUE,   "↑"),
            makeReportCard("Avg Performance", computeAvgPerformance(), "All exams", C_PURPLE, "🏅"),
            makeReportCard("Fee Collection",  computeFeeRate(),        "This month", C_GREEN,  "$"),
            makeReportCard("Active Students", dbCount("STUDENT"),      "Enrolled",   C_ORANGE, "👥")
        );
        for (Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Report tabs
        TabPane tabs = new TabPane(); tabs.setStyle("-fx-font-size:13px;");
        tabs.getTabs().addAll(buildAttendanceTab(), buildPerformanceTab(), buildFeesTab(), buildGradesTab());
        tabs.getTabs().forEach(t -> t.setClosable(false));

        page.getChildren().addAll(cards, tabs);
        scroll.setContent(page);
        return scroll;
    }

    // ── Attendance Report ────────────────────────────────────────────────
    private Tab buildAttendanceTab() {
        Tab tab = new Tab("Attendance");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        // Live chart — class-wise attendance from gradesData + studentList
        VBox chartCard = new VBox(10); chartCard.setPadding(new Insets(16));
        chartCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        HBox ch = new HBox(); Label ct = new Label("Class-wise Attendance (Live)"); ct.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(ct,Priority.ALWAYS);
        Button exp = makeOutlineSmallBtn("⬇ Export"); exp.setOnAction(e -> showAlert("Exported!")); ch.getChildren().addAll(ct, exp);

        Pane chartPane = makeDynamicChartPane(
            () -> computeClassAttLabels(),
            () -> computeClassAttVals(),
            () -> computeClassAttColors(),
            "%", 200
        );
        chartCard.getChildren().addAll(ch, chartPane);

        // Student attendance table
        List<String[]> rows = new ArrayList<>();
        Map<String,Integer> counts = new LinkedHashMap<>();
        for (Object[] g : state.gradesData) counts.merge(g[0].toString(), 1, Integer::sum);
        int max = counts.values().stream().mapToInt(v->v).max().orElse(1); if (max==0) max=1;
        for (int i = 0; i < state.studentList.size(); i++) {
            String[] s = state.studentList.get(i);
            int present = counts.getOrDefault(s[1], 0), total = Math.max(max, present);
            double rate = total>0 ? present*100.0/total : 0;
            rows.add(new String[]{String.format("%03d",i+1), s[1], s[3], String.valueOf(present), String.format("%.1f%%",rate), rate>=90?"Excellent":rate>=75?"Average":"Poor"});
        }
        if (rows.isEmpty()) rows.add(new String[]{"001","No data","—","0","0%","—"});
        VBox tableCard = buildReportTableCard("Student Attendance (Live Data)", new String[]{"Roll No","Student Name","Class","Present","Rate","Status"}, new int[]{70,160,110,80,90,90}, rows.toArray(new String[0][]));

        content.getChildren().addAll(chartCard, tableCard);
        tab.setContent(bgScroll(content)); return tab;
    }

    private String[] computeClassAttLabels() {
        return buildClassAttMap().keySet().toArray(new String[0]);
    }
    private double[] computeClassAttVals() {
        Map<String,int[]> map = buildClassAttMap(); double[] vals = new double[map.size()]; int i=0;
        for (int[] v : map.values()) vals[i++] = v[1]>0 ? v[0]*100.0/v[1] : 0; return vals;
    }
    private Color[] computeClassAttColors() {
        double[] vals = computeClassAttVals(); Color[] cols = new Color[vals.length];
        for (int i=0;i<vals.length;i++) cols[i] = vals[i]>=95?Color.web(C_GREEN):vals[i]>=80?Color.web(C_BLUE):Color.web(C_ORANGE); return cols;
    }
    private Map<String,int[]> buildClassAttMap() {
        Map<String,int[]> map = new LinkedHashMap<>();
        for (String[] s : state.studentList) map.putIfAbsent(s[3], new int[]{0,0});
        if (map.isEmpty()) { map.put("Class 1-A", new int[]{0,1}); return map; }
        for (Object[] g : state.gradesData) for (String[] s : state.studentList) if (s[1].equals(g[0].toString())) { int[] arr=map.computeIfAbsent(s[3],k->new int[]{0,0}); arr[0]++; arr[1]++; }
        for (int[] v : map.values()) if (v[1]==0) v[1]=1; return map;
    }

    // ── Performance Report ───────────────────────────────────────────────
    private Tab buildPerformanceTab() {
        Tab tab = new Tab("Performance");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        // Avg score per exam — live
        Color[] palette = {Color.web(C_BLUE),Color.web(C_PURPLE),Color.web(C_GREEN),Color.web(C_ORANGE),Color.web(C_RED)};
        Pane chartPane = makeDynamicChartPane(
            () -> buildExamScoreMap().keySet().toArray(new String[0]),
            () -> { Map<String,List<Integer>> m=buildExamScoreMap(); double[] v=new double[m.size()]; int i=0; for(List<Integer> l:m.values()) v[i++]=l.stream().mapToInt(x->x).average().orElse(0); return v; },
            () -> { int n=buildExamScoreMap().size(); Color[] c=new Color[n]; for(int i=0;i<n;i++) c[i]=palette[i%palette.length]; return c; },
            "", 200
        );
        VBox chartCard = new VBox(10); chartCard.setPadding(new Insets(16)); chartCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        HBox ch=new HBox(); Label ct=new Label("Avg Score per Exam (Live)"); ct.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(ct,Priority.ALWAYS);
        Button exp=makeOutlineSmallBtn("⬇ Export"); exp.setOnAction(e->showAlert("Exported!")); ch.getChildren().addAll(ct,exp);
        chartCard.getChildren().addAll(ch, chartPane);

        // Top performers table — live
        List<String[]> topRows = new ArrayList<>();
        Map<String,List<Integer>> esMap = buildExamScoreMap();
        for (String exam : esMap.keySet()) {
            String top="",grade="",total="100"; int topScore=-1;
            for (Object[] g : state.gradesData) { if(!g[1].toString().equals(exam)) continue; int sc=0; try{sc=Integer.parseInt(g[2].toString());}catch(Exception ignored){} if(sc>topScore){topScore=sc;top=g[0].toString();grade=g[5].toString();total=g[3].toString();} }
            if (!top.isEmpty()) topRows.add(new String[]{exam,top,topScore+"/"+total,grade,String.format("%.0f%%",(double)topScore/Integer.parseInt(total)*100)});
        }
        if (topRows.isEmpty()) topRows.add(new String[]{"No data","—","—","—","—"});
        VBox topCard = buildReportTableCard("Top Performers by Exam (Live)", new String[]{"Exam","Top Student","Score","Grade","Avg"}, new int[]{150,180,90,80,80}, topRows.toArray(new String[0][]));

        content.getChildren().addAll(chartCard, topCard);
        tab.setContent(bgScroll(content)); return tab;
    }

    private Map<String,List<Integer>> buildExamScoreMap() {
        Map<String,List<Integer>> map = new LinkedHashMap<>();
        for (Object[] g : state.gradesData) { int sc=0; try{sc=Integer.parseInt(g[2].toString());}catch(Exception ignored){} map.computeIfAbsent(g[1].toString(),k->new ArrayList<>()).add(sc); }
        if (map.isEmpty()) map.put("No Data", List.of(0)); return map;
    }

    // ── Fees Report ──────────────────────────────────────────────────────
    private Tab buildFeesTab() {
        Tab tab = new Tab("Fees");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        // Summary mini cards — live
        int totalExp=0,totalPaid=0,totalPending=0,totalOverdue=0;
        for (Object[] f : state.feeData) { int a=0,p=0; try{a=Integer.parseInt(f[2].toString().replace("$","").replace(",",""));}catch(Exception ignored){} try{p=Integer.parseInt(f[3].toString().replace("$","").replace(",",""));}catch(Exception ignored){} totalExp+=a; totalPaid+=p; String st=f[6].toString(); if(st.equals("Overdue")) totalOverdue+=(a-p); else if(!st.equals("Paid")) totalPending+=(a-p); }
        HBox cards2 = new HBox(12);
        String[][] sc = {{"Expected","$"+totalExp,C_BLUE},{"Collected","$"+totalPaid,C_GREEN},{"Pending","$"+totalPending,C_ORANGE},{"Overdue","$"+totalOverdue,C_RED}};
        for (String[] s : sc) { VBox c=new VBox(4); c.setPadding(new Insets(14,16,14,16)); c.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8; -fx-background-radius:8;"); HBox.setHgrow(c,Priority.ALWAYS); Label v=new Label(s[1]); v.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:"+s[2]+";"); Label l=new Label(s[0]); l.setStyle("-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"); c.getChildren().addAll(l,v); cards2.getChildren().add(c); }

        // Fee status bar chart — live
        Map<String,Integer> statusCount = new LinkedHashMap<>();
        statusCount.put("Paid",0); statusCount.put("Partial",0); statusCount.put("Pending",0); statusCount.put("Overdue",0);
        for (Object[] f : state.feeData) statusCount.merge(f[6].toString(), 1, Integer::sum);
        String[] stN = {"Paid","Partial","Pending","Overdue"};
        double[] stV = {statusCount.get("Paid"),statusCount.get("Partial"),statusCount.get("Pending"),statusCount.get("Overdue")};
        Color[]  stC  = {Color.web(C_GREEN),Color.web(C_BLUE),Color.web(C_ORANGE),Color.web(C_RED)};
        VBox chartCard = new VBox(10); chartCard.setPadding(new Insets(16)); chartCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        HBox ch=new HBox(); Label ct=new Label("Fee Status (Live)"); ct.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(ct,Priority.ALWAYS); Button exp=makeOutlineSmallBtn("⬇ Export"); exp.setOnAction(e->showAlert("Exported!")); ch.getChildren().addAll(ct,exp);
        chartCard.getChildren().addAll(ch, makeLiveChartPane(stN, stV, stC, "", 200));

        // Fee records table — live
        String[][] fRows = state.feeData.stream().map(f->Arrays.stream(f).map(Object::toString).toArray(String[]::new)).toArray(String[][]::new);
        if (fRows.length==0) fRows=new String[][]{{"No data","—","—","—","—","—","—"}};
        VBox tableCard = buildReportTableCard("Fee Records (Live)", new String[]{"Student","Fee Type","Amount","Paid","Balance","Due Date","Status"}, new int[]{140,110,90,90,90,110,120}, fRows);

        content.getChildren().addAll(cards2, chartCard, tableCard);
        tab.setContent(bgScroll(content)); return tab;
    }

    // ── Grades Report ────────────────────────────────────────────────────
    private Tab buildGradesTab() {
        Tab tab = new Tab("Grades");
        VBox content = new VBox(16); content.setPadding(new Insets(16,0,0,0)); content.setStyle("-fx-background-color:"+C_BG+";");

        String[] gradeOrder = {"A+","A","B+","B","C","D","F"};
        Map<String,Map<String,Integer>> examGradeDist = new LinkedHashMap<>();
        for (Object[] g : state.gradesData) examGradeDist.computeIfAbsent(g[1].toString(),k->new LinkedHashMap<>()).merge(g[5].toString(),1,Integer::sum);
        String[] examList = examGradeDist.keySet().toArray(new String[0]);
        Color[] examBarColors = {Color.web(C_BLUE),Color.web(C_PURPLE),Color.web(C_GREEN),Color.web(C_ORANGE),Color.web(C_RED)};

        VBox chartCard = new VBox(10); chartCard.setPadding(new Insets(16)); chartCard.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        HBox ch=new HBox(); Label ct=new Label("Grade Distribution Across Exams (Live)"); ct.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(ct,Priority.ALWAYS);
        Button exp=makeOutlineSmallBtn("⬇ Export"); exp.setOnAction(e->showAlert("Exported!")); ch.getChildren().addAll(ct,exp);

        Pane chartPane4 = new Pane(); chartPane4.setPrefHeight(220);
        Canvas canvas = new Canvas(); chartPane4.getChildren().add(canvas);
        canvas.widthProperty().bind(chartPane4.widthProperty()); canvas.heightProperty().bind(chartPane4.heightProperty());
        Runnable drawGrades = () -> {
            double W=canvas.getWidth(),H=canvas.getHeight(); if(W<10||H<10) return;
            GraphicsContext gc=canvas.getGraphicsContext2D(); gc.clearRect(0,0,W,H); gc.setFill(Color.WHITE); gc.fillRect(0,0,W,H);
            double pL=45,pR=20,pT=20,pB=55; int n=gradeOrder.length; double cH=H-pT-pB,grpW=(W-pL-pR)/n;
            int barsPerGroup=Math.min(examList.length,5);
            if(barsPerGroup==0){gc.setFill(Color.web(C_MUTED));gc.setFont(Font.font("SansSerif",13));gc.fillText("No grade data yet. Enter marks to see chart.",20,H/2);return;}
            double bw=Math.max(8,grpW*0.7/barsPerGroup); int maxCount=1;
            for(Map<String,Integer> m:examGradeDist.values()) for(int v:m.values()) maxCount=Math.max(maxCount,v);
            gc.setStroke(Color.web(C_BORDER));gc.setLineDashes(4);gc.setLineWidth(1);
            for(int i=0;i<=5;i++){double y=pT+cH*i/5;gc.strokeLine(pL,y,W-pR,y);gc.setFill(Color.web(C_MUTED));gc.setFont(Font.font("SansSerif",9));gc.fillText(String.valueOf(maxCount-i*maxCount/5),2,y+4);}
            gc.setLineDashes(0);
            for(int gi=0;gi<n;gi++){
                String grade=gradeOrder[gi]; double grpX=pL+grpW*gi+(grpW-(barsPerGroup*bw+(barsPerGroup-1)*2))/2;
                for(int ei=0;ei<barsPerGroup;ei++){
                    int cnt=examGradeDist.getOrDefault(examList[ei],Collections.emptyMap()).getOrDefault(grade,0);
                    double bh=maxCount>0?cnt*cH/(double)maxCount:0,x=grpX+ei*(bw+2),y=pT+cH-bh;
                    gc.setFill(examBarColors[ei%examBarColors.length]); gc.fillRoundRect(x,y,bw,Math.max(bh,cnt>0?2:0),4,4);
                    if(cnt>0){gc.setFill(Color.web(C_TEXT));gc.setFont(Font.font("SansSerif",FontWeight.BOLD,9));gc.fillText(String.valueOf(cnt),x+bw/2-4,y-3);}
                }
                gc.setFill(Color.web(C_TEXT));gc.setFont(Font.font("SansSerif",FontWeight.BOLD,11));gc.fillText(grade,pL+grpW*gi+grpW/2-5,pT+cH+16);
            }
            double lx=pL,ly=pT+cH+30;
            for(int ei=0;ei<barsPerGroup;ei++){gc.setFill(examBarColors[ei%examBarColors.length]);gc.fillRoundRect(lx,ly,10,10,3,3);gc.setFill(Color.web(C_MUTED));gc.setFont(Font.font("SansSerif",9));String en=examList[ei].length()>12?examList[ei].substring(0,11)+"…":examList[ei];gc.fillText(en,lx+14,ly+9);lx+=Math.max(70,en.length()*6+16);}
        };
        canvas.widthProperty().addListener(o->drawGrades.run()); canvas.heightProperty().addListener(o->drawGrades.run()); Platform.runLater(drawGrades);
        chartCard.getChildren().addAll(ch, chartPane4);

        // Grade summary table
        List<String[]> gRows = new ArrayList<>();
        for(String exam:examGradeDist.keySet()){Map<String,Integer> dist=examGradeDist.get(exam);int total=dist.values().stream().mapToInt(v->v).sum(),fail=dist.getOrDefault("D",0)+dist.getOrDefault("F",0);String pr=total>0?String.format("%.0f%%",(total-fail)*100.0/total):"—";gRows.add(new String[]{exam,String.valueOf(dist.getOrDefault("A+",0)),String.valueOf(dist.getOrDefault("A",0)),String.valueOf(dist.getOrDefault("B+",0)),String.valueOf(dist.getOrDefault("B",0)),String.valueOf(dist.getOrDefault("C",0)),String.valueOf(fail),String.valueOf(total),pr});}
        if(gRows.isEmpty()) gRows.add(new String[]{"No data","—","—","—","—","—","—","—","—"});
        VBox tableCard = buildReportTableCard("Grade Summary by Exam (Live)", new String[]{"Exam","A+","A","B+","B","C","D/F","Total","Pass Rate"}, new int[]{140,60,60,60,60,60,60,70,90}, gRows.toArray(new String[0][]));

        content.getChildren().addAll(chartCard, tableCard);
        tab.setContent(bgScroll(content)); return tab;
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private VBox buildReportTableCard(String title, String[] cols, int[] widths, String[][] data) {
        VBox card = new VBox(10); card.setPadding(new Insets(16)); card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:8;");
        HBox hdr = new HBox(); Label t = new Label(title); t.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";"); HBox.setHgrow(t,Priority.ALWAYS);
        Button exp = makeOutlineSmallBtn("⬇ Export"); exp.setOnAction(e->showAlert("Exported!")); hdr.getChildren().addAll(t,exp);
        TableView<ObservableList<String>> tv = buildSimpleTable(cols,widths,data); tv.setPrefHeight(200);
        card.getChildren().addAll(hdr,tv); return card;
    }

    private HBox makeReportCard(String label, String value, String sub, String color, String icon) {
        HBox card = new HBox(10); card.setPadding(new Insets(14,16,14,16)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        javafx.scene.shape.Rectangle ir = new javafx.scene.shape.Rectangle(44,44); ir.setArcWidth(10); ir.setArcHeight(10);
        Color c = Color.web(color); ir.setFill(new Color(c.getRed(),c.getGreen(),c.getBlue(),0.15));
        javafx.scene.layout.StackPane ib = new javafx.scene.layout.StackPane(); ib.getChildren().addAll(ir, lbl(icon,"-fx-font-size:16px; -fx-text-fill:"+color+";"));
        VBox info = new VBox(2); HBox.setHgrow(info,Priority.ALWAYS);
        info.getChildren().addAll(lbl(label,"-fx-font-size:11px; -fx-text-fill:"+C_MUTED+";"), lbl(value,"-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:"+color+";"), lbl(sub,"-fx-font-size:10px; -fx-text-fill:"+C_MUTED+";"));
        card.getChildren().addAll(info,ib); return card;
    }

    private String computeAvgPerformance() {
        if (state.gradesData.isEmpty()) return "N/A";
        double sum = 0; int cnt = 0;
        for (Object[] g : state.gradesData) { try { sum += Integer.parseInt(g[2].toString()); cnt++; } catch (Exception ignored) {} }
        return cnt > 0 ? String.format("%.1f%%", sum / cnt) : "N/A";
    }

    private String computeFeeRate() {
        int exp=0, paid=0;
        for (Object[] f : state.feeData) { try{exp+=Integer.parseInt(f[2].toString().replace("$","").replace(",",""));}catch(Exception ignored){} try{paid+=Integer.parseInt(f[3].toString().replace("$","").replace(",",""));}catch(Exception ignored){} }
        return exp > 0 ? String.format("%.0f%%", paid * 100.0 / exp) : "N/A";
    }

    private String dbCount(String role) {
        try { PreparedStatement ps=DatabaseConnection.getConnection().prepareStatement("SELECT COUNT(*) FROM users WHERE role=?"); ps.setString(1,role); ResultSet rs=ps.executeQuery(); if(rs.next()) return String.valueOf(rs.getInt(1)); } catch(SQLException e){ e.printStackTrace(); } return "0";
    }
}
