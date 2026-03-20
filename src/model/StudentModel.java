package model;

import java.util.*;

/**
 * StudentModel — all data + business logic. Pure Java, zero JavaFX.
 */
public class StudentModel {

    // ── Student info ──────────────────────────────────────────────────────
    private final String studentName;
    private final String studentClass = "Class 10-A";
    private final String rollNo       = "001";

    public StudentModel(String studentName) { this.studentName = studentName; }
    public String getStudentName()  { return studentName; }
    public String getStudentClass() { return studentClass; }
    public String getRollNo()       { return rollNo; }

    // ── Full profile ──────────────────────────────────────────────────────
    public String getEmail()         { return studentName.toLowerCase().replace(" ",".")+"@school.edu"; }
    public String getPhone()         { return "+977 9812345678"; }
    public String getDOB()           { return "2008-05-14"; }
    public String getBloodGroup()    { return "B+"; }
    public String getGender()        { return "Female"; }
    public String getAddress()       { return "Kathmandu, Bagmati Province"; }
    public String getParentName()    { return "Ram Maharjan"; }
    public String getParentPhone()   { return "+977 9823456789"; }
    public String getAdmissionDate() { return "2020-04-01"; }
    public String getSection()       { return "A"; }
    public String getMediumOfStudy() { return "English"; }
    public String getSchoolName()    { return "EduManage Secondary School"; }
    public String getAcademicYear()  { return "2025-2026"; }

    // ── Dashboard summary stats ───────────────────────────────────────────
    public String getAttendancePct()   { return "92%"; }
    public String getAttendanceSub()   { return "This semester"; }
    public String getAvgGrade()        { return "A-"; }
    public String getAvgGradeSub()     { return "85% overall"; }
    public String getPendingFees()     { return "$2,500"; }
    public String getPendingFeesSub()  { return "Due: March 1"; }
    public int    getUpcomingExams()   { return 3; }
    public String getUpcomingExamSub() { return "Next: March 15"; }

    // ── Performance chart ─────────────────────────────────────────────────
    public String[] getPerfLabels() { return new String[]{"Math","Physics","English","Biology","Chemistry"}; }
    public double[] getPerfScores() { return new double[]{85, 78, 83, 90, 80}; }

    // ── Recent notifications (dashboard) ─────────────────────────────────
    public List<String[]> getRecentNotifications() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Mid-Term Exams Scheduled",
            "Mid-term examinations will begin from March 15, 2026. Please check the exam schedule.",
            "2026-02-20"});
        list.add(new String[]{"Fee Payment Reminder",
            "Tuition fees for March are due on March 1st. Please ensure timely payment.",
            "2026-02-22"});
        list.add(new String[]{"Parent-Teacher Meeting",
            "Parent-teacher meeting scheduled for March 5, 2026 at 3:00 PM.",
            "2026-02-18"});
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEAVE APPLICATIONS
    // ══════════════════════════════════════════════════════════════════════
    // [id, type, fromDate, toDate, days, reason, status, appliedOn, remarks]
    private final List<String[]> leaveApplications = new ArrayList<>(Arrays.asList(
        new String[]{"L001","Sick Leave",    "2026-03-17","2026-03-17","1","Fever and cold",        "Approved","2026-03-16","Get well soon"},
        new String[]{"L002","Medical Leave", "2026-02-10","2026-02-11","2","Doctor appointment",    "Approved","2026-02-09","Approved by class teacher"},
        new String[]{"L003","Personal Leave","2026-01-25","2026-01-25","1","Family function",       "Rejected","2026-01-24","Insufficient reason"},
        new String[]{"L004","Sick Leave",    "2026-03-20","2026-03-21","2","Stomach infection",     "Pending", "2026-03-19","Under review"}
    ));

    public List<String[]> getLeaveApplications()  { return leaveApplications; }

    public void addLeaveApplication(String[] leave) { leaveApplications.add(0, leave); }

    public String nextLeaveId() {
        return String.format("L%03d", leaveApplications.size() + 1);
    }

    public int getPendingLeaveCount() {
        return (int) leaveApplications.stream().filter(l -> l[6].equals("Pending")).count();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TIMETABLE
    // ══════════════════════════════════════════════════════════════════════
    // [day, period1..period6]
    public String[][] getTimetable() {
        return new String[][]{
            {"Monday",    "Mathematics","Physics",   "English",    "Break","Chemistry","Biology"},
            {"Tuesday",   "Physics",    "Chemistry", "Mathematics","Break","English",  "History"},
            {"Wednesday", "English",    "Biology",   "Physics",    "Break","Mathematics","Computer Sc."},
            {"Thursday",  "Chemistry",  "Mathematics","History",   "Break","Physics",  "English"},
            {"Friday",    "Biology",    "English",   "Chemistry",  "Break","Mathematics","Physics"},
        };
    }
    public String[] getPeriodTimes() {
        return new String[]{"8:00","9:00","10:00","11:00","12:00","1:00","2:00"};
    }

    // ══════════════════════════════════════════════════════════════════════
    // EXAMS & GRADES
    // ══════════════════════════════════════════════════════════════════════
    // [examName, subject, date, duration, totalMarks, marksObtained, grade, status]
    public List<String[]> getExams() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Mid-Term Examination","Mathematics","2026-03-15","3 hours","100","—",  "—",  "Upcoming"});
        list.add(new String[]{"Mid-Term Examination","Physics",    "2026-03-17","3 hours","100","—",  "—",  "Upcoming"});
        list.add(new String[]{"Unit Test 1",         "Chemistry",  "2026-02-20","1 hour", "50", "44", "A+", "Completed"});
        list.add(new String[]{"Unit Test 1",         "Biology",    "2026-02-22","1 hour", "50", "43", "A",  "Completed"});
        list.add(new String[]{"Unit Test 1",         "Mathematics","2026-02-18","1 hour", "50", "47", "A+", "Completed"});
        return list;
    }

    // [subject, examName, marks, total, pct, grade, remarks]
    public List<String[]> getGrades() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Mathematics","Unit Test 1","47","50","94%","A+","Excellent"});
        list.add(new String[]{"Physics",    "Unit Test 1","38","50","76%","B+","Good"});
        list.add(new String[]{"Chemistry",  "Unit Test 1","44","50","88%","A", "Excellent"});
        list.add(new String[]{"Biology",    "Unit Test 1","43","50","86%","A", "Good"});
        list.add(new String[]{"English",    "Unit Test 1","40","50","80%","A-","Good"});
        list.add(new String[]{"Mathematics","Mid-Term",   "85","100","85%","A", "Good"});
        list.add(new String[]{"Physics",    "Mid-Term",   "78","100","78%","B+","Good"});
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ATTENDANCE
    // ══════════════════════════════════════════════════════════════════════
    // [subject, total, attended, absent, pct, status]
    public List<String[]> getAttendanceBySubject() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Mathematics","12","11","1","91.7%","Good"});
        list.add(new String[]{"Physics",    "12","12","0","100%", "Good"});
        list.add(new String[]{"Chemistry",  "12","11","1","91.7%","Good"});
        list.add(new String[]{"English",    "12","10","2","83.3%","Average"});
        list.add(new String[]{"Biology",    "12","11","1","91.7%","Good"});
        list.add(new String[]{"History",    "10","9", "1","90%",  "Good"});
        return list;
    }

    // [date, day, status, remarks]
    public List<String[]> getAttendanceHistory() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"2026-03-19","Wednesday","Present","—"});
        list.add(new String[]{"2026-03-18","Tuesday",  "Present","—"});
        list.add(new String[]{"2026-03-17","Monday",   "Absent", "Sick leave"});
        list.add(new String[]{"2026-03-14","Friday",   "Present","—"});
        list.add(new String[]{"2026-03-13","Thursday", "Present","—"});
        list.add(new String[]{"2026-03-12","Wednesday","Present","—"});
        list.add(new String[]{"2026-03-11","Tuesday",  "Late",   "Traffic"});
        list.add(new String[]{"2026-03-10","Monday",   "Present","—"});
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    // FEES
    // ══════════════════════════════════════════════════════════════════════
    // [feeType, amount, paid, balance, dueDate, status]
    public List<String[]> getFeeHistory() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Tuition Fee - Term 1","$5,000","$5,000","$0",   "2026-01-01","Paid"});
        list.add(new String[]{"Exam Fee",             "$200",  "$200",  "$0",   "2026-02-15","Paid"});
        list.add(new String[]{"Library Fee",          "$100",  "$100",  "$0",   "2026-01-01","Paid"});
        list.add(new String[]{"Tuition Fee - Term 2", "$5,000","$2,500","$2,500","2026-03-01","Pending"});
        list.add(new String[]{"Sports Fee",           "$150",  "$0",    "$150", "2026-03-15","Overdue"});
        return list;
    }

    public String getFeeStatusSummary() { return "You have $2,650 in pending/overdue fees."; }
    public boolean hasPendingFees()     { return true; }

    // ══════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════
    // [title, message, type, priority, date, read]
    private final List<Object[]> notifications = new ArrayList<>(Arrays.asList(
        new Object[]{"Mid-Term Exams Scheduled",
            "Mid-term examinations will begin from March 15, 2026. All students must carry their hall tickets.",
            "announcement","high","2026-02-20", false},
        new Object[]{"Fee Payment Reminder",
            "Tuition fees for March are due on March 1st. Please ensure timely payment.",
            "reminder","medium","2026-02-22", false},
        new Object[]{"Parent-Teacher Meeting",
            "A parent-teacher meeting is scheduled for March 5, 2026 at 3:00 PM in the main hall.",
            "announcement","medium","2026-02-18", true},
        new Object[]{"Library Book Return",
            "All library books must be returned by March 31, 2026 to avoid late fees.",
            "reminder","low","2026-02-25", true},
        new Object[]{"Sports Day Announcement",
            "Annual Sports Day will be held on April 10, 2026. Students can register for events by March 25.",
            "announcement","low","2026-03-01", false}
    ));

    public List<Object[]> getNotifications()     { return notifications; }
    public void markRead(int i)                  { if(i>=0&&i<notifications.size()) notifications.get(i)[5]=true; }
    public void markAllRead()                    { notifications.forEach(n->n[5]=true); }
    public long getUnreadCount()                 { return notifications.stream().filter(n->!(boolean)n[5]).count(); }
}