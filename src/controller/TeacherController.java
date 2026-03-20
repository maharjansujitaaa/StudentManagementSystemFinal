package controller;

import model.TeacherModel;
import view.TeacherDashboardView;
import java.util.List;

public class TeacherController {
    private final TeacherModel model;
    private final TeacherDashboardView view;

    public TeacherController(TeacherModel model, TeacherDashboardView view) { this.model=model; this.view=view; }

    public void navigate(String page) {
        switch(page){
            case "dashboard":     view.showDashboard();    break;
            case "students":      view.showStudents();     break;
            case "attendance":    view.showAttendance();   break;
            case "classes":       view.showClasses();      break;
            case "exams":         view.showExams();        break;
            case "notifications": view.showNotifications();break;
            case "reports":       view.showReports();      break;
            default:              view.showDashboard();    break;
        }
    }

    public int    getTotalStudents()   { return model.getTotalStudents();   }
    public int    getTotalTeachers()   { return model.getTotalTeachers();   }
    public int    getTotalClasses()    { return model.getTotalClasses();    }
    public String getAttendanceRate()  { return model.getAttendanceRate();  }
    public String getStudentsDelta()   { return model.getStudentsDelta();   }
    public String getTeachersDelta()   { return model.getTeachersDelta();   }
    public String getClassesDelta()    { return model.getClassesDelta();    }
    public String getAttendanceDelta() { return model.getAttendanceDelta(); }
    public String[]       getSubjectLabels()  { return model.getSubjectLabels();  }
    public double[]       getSubjectScores()  { return model.getSubjectScores();  }
    public List<String[]> getUpcomingEvents() { return model.getUpcomingEvents(); }

    public List<String[]> getStudents(String s,String c){ return model.getStudents(s,c); }
    public List<String[]> getStudentList()              { return model.getStudentList(); }
    public void addStudent(String[] s)                  { model.addStudent(s); }
    public void updateStudent(int i,String[] s)         { model.updateStudent(i,s); }
    public void deleteStudent(int i)                    { model.deleteStudent(i); }
    public String nextRollNo()                          { return model.nextRollNo(); }

    public List<String[]> getAttendanceForClass(String d,String c){ return model.getAttendanceForClass(d,c); }
    public void markAttendance(String d,String c,String r,String s){ model.markAttendance(d,c,r,s); }
    public String getAttendanceStatus(String d,String c,String r)  { return model.getAttendanceStatus(d,c,r); }

    public List<String[]> getClasses()           { return model.getClasses(); }
    public void addClass(String[] c)             { model.addClass(c); }
    public void updateClass(int i,String[] c)    { model.updateClass(i,c); }
    public void deleteClass(int i)               { model.deleteClass(i); }
    public String nextClassId()                  { return model.nextClassId(); }
    public String[][] getSchedule(String id)     { return model.getSchedule(id); }
    public void saveSchedule(String id,String[][] s){ model.saveSchedule(id,s); }

    public List<String[]> getExams()             { return model.getExams();  }
    public List<String[]> getGrades()            { return model.getGrades(); }
    public void addExam(String[] e)              { model.addExam(e); }
    public void updateExam(int i,String[] e)     { model.updateExam(i,e); }
    public void deleteExam(int i)                { model.deleteExam(i); }
    public void saveGrades(String exam,String[] students,String[] marks,String[] totals){ model.saveGrades(exam,students,marks,totals); }
    public boolean isExamCompleted(String name)              { return model.isExamCompleted(name); }
    public String  getExamStatus(String name, String stored) { return model.getExamStatus(name, stored); }

    public String[]       getReportSubjectLabels() { return model.getReportSubjectLabels(); }
    public double[]       getReportSubjectScores() { return model.getReportSubjectScores(); }
    public List<String[]> getAttendanceSummary()   { return model.getAttendanceSummary(); }

    public List<Object[]> getNotifications()    { return model.getNotifications(); }
    public void addNotification(String t,String m,String tp,String p,String tg){ model.addNotification(t,m,tp,p,tg); view.showNotifications(); }
    public void markNotificationRead(int i)     { model.markRead(i); }
    public void deleteNotification(int i)       { model.deleteNotification(i); view.showNotifications(); }
    public long getUnreadCount()                { return model.getUnreadCount(); }

    public String getTeacherName() { return model.getTeacherName(); }
    public void handleLogout()     { view.doLogout(); }
}