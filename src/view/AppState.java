
package view;

import java.util.*;

/**
 * Shared mutable data store — one instance passed to every page.
 * When any page modifies feeData, gradesData etc., all other pages
 * that read those lists will automatically get the updated data.
 */
public class AppState {

    // ── Classes 1-10, two sections each ────────────────────────────────────
    public static final String[] CLASS_NAMES = {
        "Class 1-A","Class 1-B",
        "Class 2-A","Class 2-B",
        "Class 3-A","Class 3-B",
        "Class 4-A","Class 4-B",
        "Class 5-A","Class 5-B",
        "Class 6-A","Class 6-B",
        "Class 7-A","Class 7-B",
        "Class 8-A","Class 8-B",
        "Class 9-A","Class 9-B",
        "Class 10-A","Class 10-B"
    };

    public static final String[] SUBJECTS = {
        "Mathematics","Physics","Chemistry","English","Biology",
        "Computer Science","History","Geography","Art","Music"
    };

    public static final String[] TEACHERS = {
        "Dr. Sarah Mitchell","Prof. John Anderson","Ms. Jennifer Lee",
        "Mr. Robert Taylor","Dr. Emily Clark","Prof. James White"
    };

    // ── Fee data: [student, feeType, amount, paid, balance, dueDate, status]
    public List<Object[]> feeData = new ArrayList<>(Arrays.asList(
        new Object[]{"Emma Johnson",  "Tuition Fee","$5,000","$5,000","$0",    "2026-03-01","Paid"},
        new Object[]{"Liam Smith",    "Tuition Fee","$5,000","$2,500","$2,500","2026-03-01","Partial"},
        new Object[]{"Olivia Brown",  "Tuition Fee","$5,000","$0",    "$5,000","2026-02-01","Overdue"},
        new Object[]{"Noah Davis",    "Tuition Fee","$5,000","$0",    "$5,000","2026-03-15","Pending"}
    ));

    // ── Grades data: [student, exam, marks, total, pct, grade, remarks]
    public List<Object[]> gradesData = new ArrayList<>(Arrays.asList(
        new Object[]{"Emma Johnson", "Unit Test 1","85","100","85.0%","A",  "-"},
        new Object[]{"Liam Smith",   "Unit Test 1","78","100","78.0%","B+", "-"},
        new Object[]{"Olivia Brown", "Unit Test 1","92","100","92.0%","A+", "-"},
        new Object[]{"Noah Davis",   "Unit Test 1","70","100","70.0%","B",  "-"}
    ));

    // ── Exams
    public List<Object[]> upcomingExams = new ArrayList<>(Arrays.asList(
        new Object[]{"Mid-Term Examination","Mathematics","Class 1-A","2026-03-15","3 hours","100","Scheduled"},
        new Object[]{"Mid-Term Examination","Physics",    "Class 1-A","2026-03-17","3 hours","100","Scheduled"}
    ));
    public List<Object[]> completedExams = new ArrayList<>(Arrays.asList(
        new Object[]{"Unit Test 1","Chemistry","Class 2-A","2026-02-20","Completed"},
        new Object[]{"Unit Test 2","Biology",  "Class 2-A","2026-02-10","Completed"}
    ));

    // ── Notifications: [title, message, type, priority, target, date, read]
    public List<Object[]> notifData = new ArrayList<>(Arrays.asList(
        new Object[]{"Mid-Term Exams Scheduled","Mid-term examinations will begin from March 15, 2026.",
                     "announcement","High","student,teacher","2026-02-20",false},
        new Object[]{"Fee Payment Reminder","Tuition fees for March are due on March 1st.",
                     "reminder","Medium","student","2026-02-22",false},
        new Object[]{"Parent-Teacher Meeting","Meeting scheduled for March 5, 2026 at 3:00 PM.",
                     "announcement","Medium","all","2026-02-18",true}
    ));

    // ── Classes: [id, name, teacher, studentCount, subjects]
    public List<String[]> classDataList = new ArrayList<>(Arrays.asList(
        new String[]{"C1","Class 10 - A","Dr. Sarah Mitchell","35","Mathematics,Physics,Chemistry,English,Biology"},
        new String[]{"C2","Class 10 - B","Prof. John Anderson","32","Mathematics,Physics,Chemistry,English,Biology"},
        new String[]{"C3","Class 9 - A", "Ms. Jennifer Lee",   "38","Mathematics,Physics,Chemistry,English,Biology"},
        new String[]{"C4","Class 9 - B", "Mr. Robert Taylor",  "30","Mathematics,Physics,Chemistry,English"},
        new String[]{"C5","Class 11 - A","Dr. Emily Clark",    "28","Mathematics,Physics,Chemistry,Biology"},
        new String[]{"C6","Class 11 - B","Prof. James White",  "33","Mathematics,Physics,English,Biology"}
    ));

    // ── Schedules per class id
    public Map<String, String[][]> classSchedules = new HashMap<>(Map.of(
        "C1", new String[][]{
            {"Monday",   "Math",    "Physics", "Chem",    "English","Bio"},
            {"Tuesday",  "Physics", "Math",    "English", "Bio",    "Chem"},
            {"Wednesday","Chem",    "English", "Math",    "Physics","Bio"},
            {"Thursday", "English", "Bio",     "Physics", "Chem",   "Math"},
            {"Friday",   "Bio",     "Chem",    "Math",    "Physics","English"}},
        "C2", new String[][]{
            {"Monday",   "Physics","Math",    "English","Bio",    "Chem"},
            {"Tuesday",  "Math",   "Physics", "Chem",   "English","Bio"},
            {"Wednesday","English","Chem",    "Physics","Math",   "Bio"},
            {"Thursday", "Bio",    "Math",    "English","Physics","Chem"},
            {"Friday",   "Chem",   "Bio",     "Math",   "English","Physics"}}
    ));

    // ── Student roster: [rollNo, name, email, class, phone, status]
    public List<String[]> studentList = new ArrayList<>(Arrays.asList(
        new String[]{"001","Emma Johnson", "emma.johnson@school.com","Class 1-A","555-0101","Active"},
        new String[]{"002","Liam Smith",   "liam.smith@school.com",  "Class 1-A","555-0103","Active"},
        new String[]{"003","Olivia Brown", "olivia.brown@school.com","Class 1-B","555-0105","Active"},
        new String[]{"004","Noah Davis",   "noah.davis@school.com",  "Class 2-A","555-0107","Active"},
        new String[]{"005","Sophia Wilson","sophia.wilson@school.com","Class 2-A","555-0109","Active"}
    ));

    // ── Teacher roster: [name, email, subject, qualification, experience, status]
    public List<String[]> teacherList = new ArrayList<>(Arrays.asList(
        new String[]{"Dr. Sarah Mitchell", "sarah.m@school.com",  "Mathematics","PhD Mathematics","8 years", "Active"},
        new String[]{"Prof. John Anderson","john.a@school.com",   "Physics",    "MSc Physics",   "12 years","Active"},
        new String[]{"Ms. Jennifer Lee",   "jennifer.l@school.com","English",   "MA English",    "6 years", "Active"},
        new String[]{"Mr. Robert Taylor",  "robert.t@school.com", "Chemistry",  "MSc Chemistry", "10 years","Active"},
        new String[]{"Dr. Emily Clark",    "emily.c@school.com",  "Biology",    "PhD Biology",   "9 years", "Active"},
        new String[]{"Prof. James White",  "james.w@school.com",  "Computer Science","MCS","7 years","Active"}
    ));
}
