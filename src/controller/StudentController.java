package controller;

import model.StudentModel;
import view.StudentDashboardView;
import java.util.List;

/**
 * StudentController — pure coordinator. No JavaFX imports.
 */
public class StudentController {

    private final StudentModel       model;
    private final StudentDashboardView view;

    public StudentController(StudentModel model, StudentDashboardView view) {
        this.model = model;
        this.view  = view;
    }

    // ── Navigation ─────────────────────────────────────────────────────────
    public void navigate(String page) {
        switch (page) {
            case "dashboard":  view.showDashboard();  break;
            case "timetable":  view.showTimetable();  break;
            case "exams":      view.showExams();      break;
            case "attendance": view.showAttendance(); break;
            case "fees":       view.showFees();       break;
            case "notices":    view.showNotices();    break;
            case "leave":      view.showLeave();      break;
            default:           view.showDashboard();  break;
        }
    }

    // ── Student info ────────────────────────────────────────────────────────
    public String getStudentName()     { return model.getStudentName(); }
    public String getStudentClass()    { return model.getStudentClass(); }
    public String getRollNo()          { return model.getRollNo(); }

    // ── Full profile ────────────────────────────────────────────────────────
    public String getEmail()           { return model.getEmail();         }
    public String getPhone()           { return model.getPhone();         }
    public String getDOB()             { return model.getDOB();           }
    public String getBloodGroup()      { return model.getBloodGroup();    }
    public String getGender()          { return model.getGender();        }
    public String getAddress()         { return model.getAddress();       }
    public String getParentName()      { return model.getParentName();    }
    public String getParentPhone()     { return model.getParentPhone();   }
    public String getAdmissionDate()   { return model.getAdmissionDate(); }
    public String getSection()         { return model.getSection();       }
    public String getMediumOfStudy()   { return model.getMediumOfStudy(); }
    public String getSchoolName()      { return model.getSchoolName();    }
    public String getAcademicYear()    { return model.getAcademicYear();  }

    // ── Dashboard stats ─────────────────────────────────────────────────────
    public String getAttendancePct()   { return model.getAttendancePct();   }
    public String getAttendanceSub()   { return model.getAttendanceSub();   }
    public String getAvgGrade()        { return model.getAvgGrade();        }
    public String getAvgGradeSub()     { return model.getAvgGradeSub();     }
    public String getPendingFees()     { return model.getPendingFees();     }
    public String getPendingFeesSub()  { return model.getPendingFeesSub();  }
    public int    getUpcomingExams()   { return model.getUpcomingExams();   }
    public String getUpcomingExamSub() { return model.getUpcomingExamSub();}

    // ── Performance chart ───────────────────────────────────────────────────
    public String[] getPerfLabels()    { return model.getPerfLabels(); }
    public double[] getPerfScores()    { return model.getPerfScores(); }

    // ── Recent notifications ────────────────────────────────────────────────
    public List<String[]> getRecentNotifications() { return model.getRecentNotifications(); }

    // ── Timetable ───────────────────────────────────────────────────────────
    public String[][] getTimetable()   { return model.getTimetable();   }
    public String[]   getPeriodTimes() { return model.getPeriodTimes(); }

    // ── Exams & Grades ──────────────────────────────────────────────────────
    public List<String[]> getExams()   { return model.getExams();  }
    public List<String[]> getGrades()  { return model.getGrades(); }

    // ── Attendance ──────────────────────────────────────────────────────────
    public List<String[]> getAttendanceBySubject() { return model.getAttendanceBySubject(); }
    public List<String[]> getAttendanceHistory()   { return model.getAttendanceHistory();   }
    public String         getAttendancePctStr()    { return model.getAttendancePct();       }

    // ── Fees ────────────────────────────────────────────────────────────────
    public List<String[]> getFeeHistory()     { return model.getFeeHistory();     }
    public String         getFeeStatusSummary(){ return model.getFeeStatusSummary(); }
    public boolean        hasPendingFees()    { return model.hasPendingFees();    }

    // ── Notifications ───────────────────────────────────────────────────────
    public List<Object[]> getNotifications()  { return model.getNotifications(); }
    public void           markRead(int i)     { model.markRead(i);               }
    public void           markAllRead()       { model.markAllRead();             }
    public long           getUnreadCount()    { return model.getUnreadCount();   }

    // ── Logout ──────────────────────────────────────────────────────────────
    public void handleLogout()                { view.doLogout(); }

    // ── Leave Applications ──────────────────────────────────────────────────
    public java.util.List<String[]> getLeaveApplications() { return model.getLeaveApplications(); }
    public void addLeaveApplication(String[] leave)        { model.addLeaveApplication(leave); }
    public String nextLeaveId()                            { return model.nextLeaveId(); }
    public int getPendingLeaveCount()                      { return model.getPendingLeaveCount(); }
}