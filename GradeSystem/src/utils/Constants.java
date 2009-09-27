package utils;

public class Constants {
    //Course directory

    public static final String COURSE_DIR = "/course/cs015/";
    // XML Grading Sheets that will be modified while TAs are grading:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/<studentlogin>.xml
    // Text (.grd) Grading Sheets generated after grading is submitted:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/<studentlogin>.grd
    public static final String GRADER_PATH = COURSE_DIR + "admin/uta/grading/";
    // The hidden folder the code is untarred and compiled in:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/.code/
    public static final String CODE_DIR = ".code/";
    // Configuration file for the grading system
    public static final String CONFIG_FILE_PATH = COURSE_DIR + "admin/grade/current/config.xml";
    // XML Grading Sheets that TAs have submitted after finishing grading:
    // /course/cs015/admin/grade/current/<assignment>/<talogin>/<studentlogin>.xml
    public static final String GRADER_SUBMIT_PATH = COURSE_DIR + "admin/grade/current/";
    // Blank XML Grading Sheets:
    // /course/cs015/<project>/grade/grader_sheet.xml
    public static final String TEMPLATE_GRADE_SHEET_DIR = "grade/";
    public static final String TEMPLATE_GRADE_SHEET_FILENAME = "grader_sheet.xml";
    // Handin tars:
    // /course/cs015/handin/<assignment>/<year>/<studentlogin>.xml
    public static final String HANDIN_DIR = COURSE_DIR + "handin/";
    public static final String TEST_ACCOUNT = "cs015000";

    public static final String GRADES_TA = "jeldridg@cs.brown.edu";
    public static final int MINUTES_OF_LENIENCY = 10;

    public static final String DATABASE_DIR = COURSE_DIR + "tabin/gradesys/cs015Database.db";//"/home/psastras/cs15db.db";///COURSE_DIR + "tabin/gradesys/cs015Database.db";
}