
package view;

import controller.TeacherController;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import java.util.*;

import static view.TeacherUIHelper.*;

public class TeacherReportsPage {
    private final TeacherController controller;
    TeacherReportsPage(TeacherController c) { this.controller=c; }

    Node build() {
        ScrollPane scroll=bgScroll();
        VBox page=new VBox(20); page.setPadding(new Insets(20,28,20,28)); page.setStyle("-fx-background-color:"+C_BG+";");

        // Summary cards
        HBox cards=new HBox(14);
        cards.getChildren().addAll(
            reportCard("My Students", String.valueOf(controller.getTotalStudents()), C_BLUE,   "👥"),
            reportCard("My Classes",  String.valueOf(controller.getTotalClasses()),  C_GREEN,  "📖"),
            reportCard("Avg Grade",   computeAvgGrade(),                             C_PURPLE, "📊"),
            reportCard("Attendance",  controller.getAttendanceRate(),               C_ORANGE, "✅")
        );
        for (javafx.scene.Node n : cards.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Two live charts side by side
        HBox chartsRow=new HBox(16); chartsRow.setPrefHeight(300);
        chartsRow.getChildren().addAll(buildSubjectChart(), buildAttChart());
        for (javafx.scene.Node n : chartsRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // Top performers per class
        VBox topCard = buildTopPerformersCard();

        page.getChildren().addAll(cards, chartsRow, topCard);
        scroll.setContent(page); return scroll;
    }

    // ── Live subject performance chart ────────────────────────────────────
    private VBox buildSubjectChart() {
        VBox card=new VBox(12); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        HBox.setHgrow(card, Priority.ALWAYS);
        Label title=new Label("Avg Score by Subject (Live)"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        // Dynamic chart — re-reads from controller on every resize
        Pane chartPane = makeDynamicBarChart(
            () -> controller.getReportSubjectLabels(),
            () -> controller.getReportSubjectScores(),
            () -> { int n=controller.getReportSubjectLabels().length; Color[] c=new Color[n]; for(int i=0;i<n;i++) c[i]=javafx.scene.paint.Color.web(C_CHART); return c; },
            "", 240
        );
        VBox.setVgrow(chartPane, Priority.ALWAYS);
        card.getChildren().addAll(title, chartPane); return card;
    }

    // ── Live attendance chart ─────────────────────────────────────────────
    private VBox buildAttChart() {
        VBox card=new VBox(12); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");
        HBox.setHgrow(card, Priority.ALWAYS);
        Label title=new Label("Class Attendance % (Live)"); title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");

        // Dynamic chart — re-reads attendance summary on every resize
        Pane chartPane = makeDynamicBarChart(
            () -> controller.getAttendanceSummary().stream().map(r->r[0].replace("Class ","")).toArray(String[]::new),
            () -> { List<String[]> s=controller.getAttendanceSummary(); double[] v=new double[s.size()]; for(int i=0;i<s.size();i++){try{v[i]=Double.parseDouble(s.get(i)[5].replace("%",""));}catch(Exception e){v[i]=0;}} return v; },
            () -> { List<String[]> s=controller.getAttendanceSummary(); Color[] c=new Color[s.size()]; for(int i=0;i<s.size();i++){try{double pct=Double.parseDouble(s.get(i)[5].replace("%",""));c[i]=pct>=90?javafx.scene.paint.Color.web(C_GREEN):pct>=75?javafx.scene.paint.Color.web(C_BLUE):javafx.scene.paint.Color.web(C_ORANGE);}catch(Exception e){c[i]=javafx.scene.paint.Color.web(C_BLUE);}} return c; },
            "%", 240
        );
        VBox.setVgrow(chartPane, Priority.ALWAYS);
        card.getChildren().addAll(title, chartPane); return card;
    }

    /** Creates a Pane with a Canvas that re-queries suppliers on every resize → always live */
    private Pane makeDynamicBarChart(
            java.util.function.Supplier<String[]> labelsSupplier,
            java.util.function.Supplier<double[]> valsSupplier,
            java.util.function.Supplier<Color[]>  colorsSupplier,
            String suffix, double prefH) {
        Pane pane = new Pane(); pane.setPrefHeight(prefH);
        Canvas canvas = new Canvas(); pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        Runnable draw = () -> drawBarChart(canvas, labelsSupplier.get(), valsSupplier.get(), colorsSupplier.get(), suffix);
        canvas.widthProperty().addListener(o->draw.run());
        canvas.heightProperty().addListener(o->draw.run());
        Platform.runLater(draw);
        return pane;
    }

    private void drawBarChart(Canvas canvas, String[] labels, double[] vals, Color[] colors, String suffix) {
        double W=canvas.getWidth(),H=canvas.getHeight(); if(W<10||H<10||labels.length==0) return;
        javafx.scene.canvas.GraphicsContext gc=canvas.getGraphicsContext2D();
        gc.clearRect(0,0,W,H); gc.setFill(javafx.scene.paint.Color.WHITE); gc.fillRect(0,0,W,H);
        double pL=50,pR=20,pT=20,pB=40,cW=W-pL-pR,cH=H-pT-pB;
        double maxVal=0; for(double v:vals) if(v>maxVal) maxVal=v; if(maxVal==0) maxVal=100;
        gc.setStroke(javafx.scene.paint.Color.web("#E2E8F0")); gc.setLineDashes(4); gc.setLineWidth(1);
        for(int i=0;i<=4;i++){double y=pT+cH*i/4;gc.strokeLine(pL,y,W-pR,y);gc.setFill(javafx.scene.paint.Color.web(C_MUTED));gc.setFont(Font.font("SansSerif",11));gc.fillText(String.valueOf((int)(maxVal*(4-i)/4))+suffix,2,y+4);}
        gc.setLineDashes(0);
        int n=labels.length; double sp=cW/n,bw=sp*0.5;
        for(int i=0;i<n;i++){
            double bh=vals[i]*cH/maxVal,x=pL+sp*i+(sp-bw)/2,y=pT+cH-bh;
            gc.setFill(colors[i%colors.length]); gc.fillRoundRect(x,y,bw,Math.max(bh,2),6,6);
            gc.setFill(javafx.scene.paint.Color.web(C_TEXT)); gc.setFont(Font.font("SansSerif",FontWeight.BOLD,11));
            String st=String.valueOf((int)vals[i])+suffix; gc.fillText(st,x+(bw-st.length()*6.0)/2,y-5);
            gc.setFill(javafx.scene.paint.Color.web(C_MUTED)); gc.setFont(Font.font("SansSerif",9));
            String xl=labels[i].length()>8?labels[i].substring(0,7)+"…":labels[i];
            gc.fillText(xl,x+(bw-xl.length()*4.5)/2,pT+cH+14);
        }
    }

    // ── Top Performers per Class ──────────────────────────────────────────
    private VBox buildTopPerformersCard() {
        VBox card = new VBox(12); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:10; -fx-background-radius:10;");

        HBox titleRow = new HBox(8); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("🏆  Top Performer per Class");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"+C_TEXT+";");
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(title);

        // Build: for each class, find student with highest avg % across all graded exams
        // Map<className, Map<studentName, List<pct>>>
        Map<String, Map<String, List<Double>>> classStudentScores = new LinkedHashMap<>();

        // Initialise classes
        for (String[] cls : controller.getClasses())
            classStudentScores.put(cls[1], new LinkedHashMap<>());

        // Fill from grades + exams (join by exam name to get class)
        for (String[] g : controller.getGrades()) {
            String examName = g[1];
            // Find class for this exam
            String examClass = null;
            for (String[] e : controller.getExams()) if (e[0].equals(examName)) { examClass = e[2]; break; }
            if (examClass == null) continue;
            // Find student class directly
            Map<String,List<Double>> studentMap = classStudentScores.computeIfAbsent(examClass, k -> new LinkedHashMap<>());
            try {
                double pct = Double.parseDouble(g[4].replace("%",""));
                studentMap.computeIfAbsent(g[0], k -> new ArrayList<>()).add(pct);
            } catch (NumberFormatException ignored) {}
        }

        // Build rows: [rank, class, student, avg%, grade]
        String[][] rows = classStudentScores.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .map(e -> {
                String cls = e.getKey();
                Map<String,List<Double>> sm = e.getValue();
                // Find top student
                String topStudent = "—"; double topAvg = -1;
                for (Map.Entry<String,List<Double>> se : sm.entrySet()) {
                    double avg = se.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    if (avg > topAvg) { topAvg = avg; topStudent = se.getKey(); }
                }
                String grade = topAvg>=90?"A+":topAvg>=80?"A":topAvg>=70?"B+":topAvg>=60?"B":topAvg>=50?"C":"F";
                return new String[]{"🥇", cls, topStudent, String.format("%.1f%%", topAvg), grade};
            })
            .toArray(String[][]::new);

        if (rows.length == 0) {
            Label empty = new Label("No grades entered yet. Enter marks in Exams & Grades to see top performers.");
            empty.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_MUTED+";"); empty.setWrapText(true);
            card.getChildren().addAll(titleRow, empty);
            return card;
        }

        TableView<ObservableList<String>> tv = buildSimpleTable(
            new String[]{"","Class","Top Student","Avg Score","Grade"},
            new int[]{40, 140, 200, 100, 70}, rows);
        tv.setRowFactory(r -> { TableRow<ObservableList<String>> row = new TableRow<>(); row.setPrefHeight(48); return row; });

        // Colour grade column
        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>,String> gradeCol =
            (TableColumn<ObservableList<String>,String>) tv.getColumns().get(4);
        gradeCol.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                Label l = new Label(item);
                l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:"
                    +(item.startsWith("A")?C_GREEN:item.startsWith("B")?C_BLUE:C_ORANGE)+";");
                setGraphic(l);
            }
        });

        // Colour avg score column
        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>,String> avgCol =
            (TableColumn<ObservableList<String>,String>) tv.getColumns().get(3);
        avgCol.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                Label l = new Label(item);
                try {
                    double v = Double.parseDouble(item.replace("%",""));
                    l.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:"
                        +(v>=90?C_GREEN:v>=75?C_BLUE:C_ORANGE)+";");
                } catch (NumberFormatException e) {
                    l.setStyle("-fx-font-size:13px; -fx-text-fill:"+C_TEXT+";");
                }
                setGraphic(l);
            }
        });

        // Set trophy column style
        @SuppressWarnings("unchecked")
        TableColumn<ObservableList<String>,String> trophyCol =
            (TableColumn<ObservableList<String>,String>) tv.getColumns().get(0);
        trophyCol.setCellFactory(c -> new TableCell<ObservableList<String>,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if(empty||item==null){setGraphic(null);return;}
                Label l = new Label(item); l.setStyle("-fx-font-size:16px;"); setGraphic(l);
            }
        });

        card.getChildren().addAll(titleRow, tv);
        return card;
    }

    private String computeAvgGrade() {
        List<String[]> grades = controller.getGrades();
        if (grades.isEmpty()) return "N/A";
        double sum=0; int cnt=0;
        for(String[] g:grades){try{sum+=Double.parseDouble(g[2]);cnt++;}catch(NumberFormatException ignored){}}
        return cnt>0?String.format("%.1f%%",sum/cnt):"N/A";
    }

    private HBox reportCard(String label,String value,String color,String icon){
        HBox card=new HBox(12); card.setPadding(new Insets(16)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-border-color:"+C_BORDER+"; -fx-border-radius:12; -fx-background-radius:12;");
        javafx.scene.layout.StackPane ib=new javafx.scene.layout.StackPane();
        Rectangle ir=new Rectangle(46,46); ir.setArcWidth(10); ir.setArcHeight(10);
        Color c=Color.web(color); ir.setFill(new Color(c.getRed(),c.getGreen(),c.getBlue(),0.15));
        Label il=new Label(icon); il.setStyle("-fx-font-size:18px; -fx-text-fill:"+color+";"); ib.getChildren().addAll(ir,il);
        VBox info=new VBox(2); HBox.setHgrow(info,Priority.ALWAYS);
        info.getChildren().addAll(lbl(label,"-fx-font-size:12px; -fx-text-fill:"+C_MUTED+";"),lbl(value,"-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:"+color+";"));
        card.getChildren().addAll(info,ib); return card;
    }
}