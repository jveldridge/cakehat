package utils;

import rubric.TimeStatus;
import java.io.File;
import java.util.Calendar;
import java.util.Map;

/**
 * Interface to be implemented by classes providing database interaction; mandates
 * methods such classes must have.
 * 
 * @author jeldridg
 */
public interface DatabaseIO {

    public boolean addAssignment(String assignmentName);

    //may add: public boolean addAssignmentPart(String assignmentName, String partName);

    public boolean addGrader(String taLogin, String taName);

    public boolean addStudent(String studentLogin, String studentName);

    public boolean disableStudent(String studentLogin);

    public boolean enableStudent(String studentLogin);

    public Map<String,String> getStudents();

    public boolean blacklistStudent(String studentLogin, String taLogin);

    public boolean isDistEmpty(String asgn);

    public Iterable<String> getBlacklistedStudents();

    public Iterable<String> getTABlacklist(String taLogin);

    public boolean assignStudentToGrader(String studentLogin, String assignmentName, String taLogin);

    public boolean unassignStudentFromGrader(String studentLogin, String assignmentName, String taLogin);

    //may add public boolean putDistribution(Map<String,String> distribution, String assignmentName);

    public Iterable<String> getStudentsAssigned(String assignmentName, String taLogin);

    //may add: public Map<String,String> getDistribution(String assignmentName);

    public boolean grantExtension(String studentLogin, String assignmentName, Calendar newDate, String note);

    public boolean grantExemption(String studentLogin, String assignmentName, String note);

    public Calendar getExtension(String studentLogin, String assignmentName);

    public String getExtensionNote(String studentLogin, String assignmentName);

    public String getExemptionNote(String studentLogin, String assignmentName);

    public boolean enterGrade(String studentLogin, String assignmentName, double score, TimeStatus status);

    //may add: public boolean enterGrade(String studentLogin, String assignmentName, , String assignmentPart, double score, TimeStatus status);

    //may add: public boolean enterGrade(String studentLogin, String assignmentName, double score);

    public double getStudentScore(String studentLogin, String assignmentName);

    //may add public double getStudentScore(String studentLogin, String assignmentName, String partName);

    public int getNumberOfGrades(String assignmentName);

    public boolean exportDatabase(File exportFile);

    public boolean resetDatabase();
}
