package model;

import java.util.*;

public class TeacherModel {

    public static final String[] CLASS_NAMES = {
        "Class 1-A","Class 1-B","Class 2-A","Class 2-B","Class 3-A","Class 3-B",
        "Class 4-A","Class 4-B","Class 5-A","Class 5-B","Class 6-A","Class 6-B",
        "Class 7-A","Class 7-B","Class 8-A","Class 8-B","Class 9-A","Class 9-B",
        "Class 10-A","Class 10-B"
    };
    public static final String[] SUBJECTS = {
        "Mathematics","Physics","Chemistry","English","Biology",
        "Computer Science","History","Geography","Art","Music"
    };

    private final String teacherName;
    public TeacherModel(String n) { this.teacherName = n; }
    public String getTeacherName() { return teacherName; }

    // ── Stats ─────────────────────────────────────────────────────────────
    public int    getTotalStudents()   { return studentList.size(); }
    public int    getTotalClasses()    { return classList.size();   }
    public int    getTotalTeachers()   { return 45; }
    public String getStudentsDelta()   { return "+12%"; }
    public String getTeachersDelta()   { return "+3%";  }
    public String getClassesDelta()    { return "+5%";  }
    public String getAttendanceDelta() { return "+2.5%";}

    public String getAttendanceRate() {
        String today = java.time.LocalDate.now().toString();
        int total = 0, present = 0;
        for (String[] s : studentList) {
            String st = getAttendanceStatus(today, s[3], s[0]);
            if (!st.equals("Not Marked")) { total++; if (st.equals("Present")) present++; }
        }
        return total == 0 ? "N/A" : String.format("%.1f%%", present * 100.0 / total);
    }

    // ── Dashboard chart ───────────────────────────────────────────────────
    public String[] getSubjectLabels() { return new String[]{"Math","Physics","English","Biology"}; }
    public double[] getSubjectScores() { return new double[]{87, 78, 83, 90}; }

    public List<String[]> getUpcomingEvents() {
        return Arrays.asList(
            new String[]{"March","15","Mid-Term Examination","Mathematics - 3 Hours","Scheduled"},
            new String[]{"March","15","Mid-Term Examination","Physics - 3 Hours","Scheduled"},
            new String[]{"March","05","Parent-Teacher Meeting","3:00 PM - All Classes","Event"}
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    // STUDENTS
    // ══════════════════════════════════════════════════════════════════════
    private List<String[]> studentList = new ArrayList<>(Arrays.asList(
        new String[]{"001","Emma Johnson",  "emma.j@school.com",  "Class 1-A","555-0101","Active","92%"},
        new String[]{"002","Liam Smith",    "liam.s@school.com",  "Class 1-A","555-0103","Active","85%"},
        new String[]{"003","Olivia Brown",  "olivia.b@school.com","Class 1-B","555-0105","Active","78%"},
        new String[]{"004","Noah Davis",    "noah.d@school.com",  "Class 2-A","555-0107","Active","88%"},
        new String[]{"005","Sophia Wilson", "sophia.w@school.com","Class 2-A","555-0109","Active","95%"},
        new String[]{"006","James Lee",     "james.l@school.com", "Class 2-B","555-0111","Active","70%"},
        new String[]{"007","Mia Clark",     "mia.c@school.com",   "Class 3-A","555-0113","Active","83%"},
        new String[]{"008","Lucas Hall",    "lucas.h@school.com", "Class 3-A","555-0115","Inactive","60%"}
    ));

    public List<String[]> getStudentList()             { return studentList; }
    public void addStudent(String[] s)                 { studentList.add(s); }
    public void updateStudent(int i, String[] s)       { if(i>=0&&i<studentList.size()) studentList.set(i,s); }
    public void deleteStudent(int i)                   { if(i>=0&&i<studentList.size()) studentList.remove(i); }
    public String nextRollNo()                         { return String.format("%03d",studentList.size()+1); }

    public List<String[]> getStudents(String search, String cls) {
        List<String[]> r = new ArrayList<>();
        for (String[] s : studentList) {
            boolean ms = search.isEmpty()||s[1].toLowerCase().contains(search)||s[2].toLowerCase().contains(search)||s[0].contains(search);
            boolean mc = cls.equals("All Classes")||s[3].equals(cls);
            if (ms&&mc) r.add(s);
        }
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ATTENDANCE — persistent Map: "date|class|roll" → status
    // ══════════════════════════════════════════════════════════════════════
    private final Map<String,String> attendanceStore = new HashMap<>();
    private String attKey(String d,String c,String r){ return d+"|"+c+"|"+r; }

    public String getAttendanceStatus(String date,String cls,String roll){
        return attendanceStore.getOrDefault(attKey(date,cls,roll),"Not Marked");
    }
    public void markAttendance(String date,String cls,String roll,String status){
        attendanceStore.put(attKey(date,cls,roll),status);
    }
    public List<String[]> getAttendanceForClass(String date,String cls){
        List<String[]> r=new ArrayList<>();
        for(String[] s:studentList){
            if(!cls.equals("All Classes")&&!s[3].equals(cls)) continue;
            r.add(new String[]{s[0],s[1],s[3],date,getAttendanceStatus(date,s[3],s[0])});
        }
        return r;
    }

    // ── Live attendance summary (used by Reports chart) ───────────────────
    public List<String[]> getAttendanceSummary() {
        String today = java.time.LocalDate.now().toString();
        Map<String,int[]> map = new LinkedHashMap<>();
        for (String[] c : classList) map.put(c[1], new int[4]);
        for (String[] s : studentList) {
            int[] v = map.computeIfAbsent(s[3], k->new int[4]);
            v[0]++;
            String st = getAttendanceStatus(today, s[3], s[0]);
            if(st.equals("Present")) v[1]++;
            else if(st.equals("Absent")) v[2]++;
            else if(st.equals("Late")) v[3]++;
        }
        List<String[]> result = new ArrayList<>();
        for (Map.Entry<String,int[]> e : map.entrySet()) {
            int[] v=e.getValue();
            String rate = v[0]>0?String.format("%.0f%%",(v[1]+v[3])*100.0/v[0]):"0%";
            result.add(new String[]{e.getKey(),String.valueOf(v[0]),String.valueOf(v[1]),String.valueOf(v[2]),String.valueOf(v[3]),rate});
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CLASSES + SCHEDULES
    // ══════════════════════════════════════════════════════════════════════
    private List<String[]> classList = new ArrayList<>(Arrays.asList(
        new String[]{"C1","Class 1-A","Mathematics","30","Mon/Wed/Fri 9:00","Active"},
        new String[]{"C2","Class 1-B","Physics",    "28","Tue/Thu 10:00",  "Active"},
        new String[]{"C3","Class 2-A","Chemistry",  "32","Mon/Wed 11:00",  "Active"},
        new String[]{"C4","Class 2-B","English",    "25","Tue/Fri 9:00",   "Active"},
        new String[]{"C5","Class 3-A","Biology",    "29","Mon/Wed/Fri 14:00","Active"},
        new String[]{"C6","Class 3-B","Mathematics","31","Tue/Thu 13:00",  "Active"}
    ));
    private final Map<String,String[][]> scheduleStore = new HashMap<>();

    public List<String[]> getClasses()           { return classList; }
    public void addClass(String[] c)             { classList.add(c); }
    public void updateClass(int i, String[] c)   { if(i>=0&&i<classList.size()) classList.set(i,c); }
    public void deleteClass(int i)               { if(i>=0&&i<classList.size()) classList.remove(i); }
    public String nextClassId()                  { return "C"+(classList.size()+1); }

    public String[][] getSchedule(String classId) {
        return scheduleStore.computeIfAbsent(classId, k -> new String[][]{
            {"Monday",   "Math","Physics","English","Chem","Bio"},
            {"Tuesday",  "Physics","Math","Chem","English","Math"},
            {"Wednesday","Chem","English","Math","Physics","Bio"},
            {"Thursday", "English","Bio","Physics","Math","Chem"},
            {"Friday",   "Bio","Chem","Physics","English","Math"}
        });
    }
    public void saveSchedule(String classId, String[][] sched) { scheduleStore.put(classId, sched); }

    // ══════════════════════════════════════════════════════════════════════
    // EXAMS & GRADES — mutable
    // ══════════════════════════════════════════════════════════════════════
    // [examName, subject, class, date, duration, totalMarks, status]
    private List<String[]> examList = new ArrayList<>(Arrays.asList(
        new String[]{"Mid-Term Examination","Mathematics","Class 1-A","2026-03-15","3 hours","100","Scheduled"},
        new String[]{"Mid-Term Examination","Physics",    "Class 1-B","2026-03-17","3 hours","100","Scheduled"},
        new String[]{"Unit Test 1",         "Chemistry",  "Class 2-A","2026-02-20","1 hour", "50", "Completed"},
        new String[]{"Unit Test 2",         "Biology",    "Class 3-A","2026-02-10","1 hour", "50", "Completed"}
    ));
    // [student, exam, marks, total, pct, grade, remarks]
    private List<String[]> gradesList = new ArrayList<>(Arrays.asList(
        new String[]{"Emma Johnson", "Unit Test 1","85","100","85.0%","A",  "Excellent"},
        new String[]{"Liam Smith",   "Unit Test 1","78","100","78.0%","B+", "Good"},
        new String[]{"Olivia Brown", "Unit Test 1","92","100","92.0%","A+", "Excellent"},
        new String[]{"Noah Davis",   "Unit Test 1","70","100","70.0%","B",  "Average"},
        new String[]{"Sophia Wilson","Unit Test 2","88","50", "88.0%","A",  "Good"}
    ));

    public List<String[]> getExams()          { return examList;   }
    public List<String[]> getGrades()         { return gradesList; }
    public void addExam(String[] e)           { examList.add(e);   }
    public void updateExam(int i, String[] e) { if(i>=0&&i<examList.size()) examList.set(i,e); }
    public void deleteExam(int i)             { if(i>=0&&i<examList.size()) examList.remove(i); }

    /** An exam is "Completed" only when at least one grade has been saved for it */
    public boolean isExamCompleted(String examName) {
        return gradesList.stream().anyMatch(g -> g[1].equals(examName));
    }

    /** Returns live effective status: "Completed" if marks entered, else stored status */
    public String getExamStatus(String examName, String storedStatus) {
        return isExamCompleted(examName) ? "Completed" : storedStatus;
    }

    /** Save/update grades for an exam (Enter Marks) */
    public void saveGrades(String examName, String[] students, String[] marks, String[] totals) {
        gradesList.removeIf(g -> g[1].equals(examName));
        for (int i = 0; i < students.length; i++) {
            if (marks[i]==null||marks[i].isEmpty()) continue;
            try {
                int m=Integer.parseInt(marks[i]), t=Integer.parseInt(totals[i]);
                double pct=t>0?m*100.0/t:0;
                String grade=pct>=90?"A+":pct>=80?"A":pct>=70?"B+":pct>=60?"B":pct>=50?"C":"F";
                gradesList.add(new String[]{students[i],examName,marks[i],totals[i],String.format("%.1f%%",pct),grade,"-"});
            } catch (NumberFormatException ignored) {}
        }
    }

    // ── Live report chart data ────────────────────────────────────────────
    public String[] getReportSubjectLabels() {
        List<String> subs = new ArrayList<>();
        for (String[] e : examList) if (!subs.contains(e[1])) subs.add(e[1]);
        return subs.isEmpty() ? new String[]{"Math","Physics","English","Biology"} : subs.toArray(new String[0]);
    }

    public double[] getReportSubjectScores() {
        String[] labels = getReportSubjectLabels();
        double[] scores = new double[labels.length];
        int[]    counts = new int[labels.length];
        for (String[] g : gradesList) {
            for (int i=0;i<labels.length;i++) {
                for (String[] e : examList) {
                    if (e[0].equals(g[1])&&e[1].equals(labels[i])) {
                        try { scores[i]+=Double.parseDouble(g[2]); counts[i]++; } catch(NumberFormatException ignored){}
                    }
                }
            }
        }
        for (int i=0;i<scores.length;i++) if(counts[i]>0) scores[i]/=counts[i];
        return scores;
    }

    // ══════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════
    private final List<Object[]> notifications = new ArrayList<>(Arrays.asList(
        new Object[]{"Mid-Term Exams Scheduled","Mid-term examinations begin March 15, 2026.","announcement","High","2026-03-10",false},
        new Object[]{"Staff Meeting","Staff meeting on March 20, 2026 at 10:00 AM.","reminder","Medium","2026-03-12",false},
        new Object[]{"Grade Submission Deadline","Submit all grades by March 25, 2026.","reminder","High","2026-03-15",true}
    ));

    public List<Object[]> getNotifications()  { return notifications; }
    public void addNotification(String title,String msg,String type,String priority,String target){
        String today=new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        notifications.add(0,new Object[]{title,msg,type,priority,today,false});
    }
    public void markRead(int i)               { if(i>=0&&i<notifications.size()) notifications.get(i)[5]=true; }
    public void deleteNotification(int i)     { if(i>=0&&i<notifications.size()) notifications.remove(i); }
    public long getUnreadCount()              { return notifications.stream().filter(n->!(boolean)n[5]).count(); }
}