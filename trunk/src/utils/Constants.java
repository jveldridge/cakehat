package utils;

public class Constants {
    //Course directory
    public static final String COURSE_DIR = "/course/cs015/";
    // XML Grading Sheets that will be modified while TAs are grading:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/<studentlogin>.xml
    // Text (.grd) Grading Sheets generated after grading is submitted:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/<studentlogin>.grd
    //public static final String GRADER_PATH = COURSE_DIR + "admin/uta/grading/";
    // The hidden folder the code is untarred and compiled in:
    // /course/cs015/admin/uta/grading/<talogin>/<assignment>/.code/
    public static final String CODE_DIR = ".code/";
    // Configuration file for the grading system
    //public static final String CONFIG_FILE_PATH = COURSE_DIR + "admin/grade/current/config.xml";
    public static final String DEDUCTIONS_LIST_FILENAME = "grading_guide.txt";
    // XML Grading Sheets that TAs have submitted after finishing grading:
    // /course/cs015/admin/grade/current/<assignment>/<talogin>/<studentlogin>.xml
    //public static final String GRADER_SUBMIT_PATH = COURSE_DIR + "admin/grade/current/";
    // Blank XML Grading Sheets:
    // /course/cs015/<project>/grade/grader_sheet.xml
    //public static final String TEMPLATE_GRADE_SHEET_DIR = "grade/";
    //public static final String TEMPLATE_GRADE_SHEET_FILENAME = "grader_sheet.xml";
    public static final String TEMPLATE_GRADE_SHEET_FILENAME = "rubric.xml";
    // Handin tars:
    // /course/cs015/handin/<assignment>/<year>/<studentlogin>.xml
    public static final String HANDIN_DIR = COURSE_DIR + "handin/";
    public static final String TEST_ACCOUNT = "cs015000";

    public static final String GRADES_TA = "jeldridg";
    public static final String GRADES_HTA = "aunger";
    public static final String EMAIL_DOMAIN = "cs.brown.edu";
    public static final int MINUTES_OF_LENIENCY = 10;

    //Demoes
    public static final String DEMO_DIR = COURSE_DIR + "project_demos/ordinary/";

    //Final project representation in the database
    public static final String FINAL_PROJECT = "Final";

    //public static final String DATABASE_FILE = COURSE_DIR + "tabin/gradesys/cs015Database.db";
    //public static final String DATABASE_BK_DIR = COURSE_DIR + "tabin/gradesys/bak/";

    public static final String TESTER_DIR = COURSE_DIR + "admin/grade/testers/";

    public static final String LAB_DIR = COURSE_DIR + "labs/grades/current/";

    public static final String CURRENT_YEAR = Integer.toString(Utils.getCurrentYear()) + "/";
    public static final String GRADER_PATH = COURSE_DIR + "grading/ta/" + CURRENT_YEAR;
    //TODO: Change to config.xml before pushing out to jar
    public static final String CONFIG_FILE_PATH = COURSE_DIR + "grading/bin/" + CURRENT_YEAR + "config_new_test.xml";
    public static final String GRADER_SUBMIT_PATH = COURSE_DIR + "grading/submitted/" + CURRENT_YEAR;
    public static final String DATABASE_FILE = COURSE_DIR + "grading/bin/" + CURRENT_YEAR + "cs015Database.db";
    public static final String DATABASE_BK_DIR = COURSE_DIR + "grading/bin" + CURRENT_YEAR + "bak/";
    public static final String TEMPLATE_GRADE_SHEET_DIR = COURSE_DIR + "grading/asgn/" + CURRENT_YEAR;

    // cs15's test account
    public static final String EMAIL_ACCOUNT = "cs015000";
    // ldap password for test account
    public static final String EMAIL_PASSWORD = "andyCS15";
    public static final String EMAIL_HOST = "smtps.cs.brown.edu";
    public static final String EMAIL_PORT = "465";

    // generated from the cs department certificate
    // generated with "keytool -import -alias smtps.cs.brown.edu -file browncs-ca.crt -keystore browncscerts.cert"
    public static final String EMAIL_CERT_PATH = COURSE_DIR + "grading/bin/smtp_certs/browncscerts.cert";
    
    // password added to the certificate by keytool
    public static final String EMAIL_CERT_PASSWORD = "andyCS15";

    //Classpath
    public static final String CLASSPATH = ":/course/cs015/lib/cs015.jar:" +
            "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dcore.jar:" +
            "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/j3dutils.jar:" +
            "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/ext/vecmath.jar:";

    //Library path - normally this wouldn't matter - but for Java3D it does (grr... I hate it!) - jak2
    public static final String LIBRARY_PATH = "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386/server:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/lib/i386:" +
                                              "/usr/lib/jvm/java-6-sun-1.6.0.12/jre/../lib/i386::" +
                                              "/pro/java/linux/software/java3d/j3d-1_5_2-linux-i586/lib/i386:" +
                                              "/usr/java/packages/lib/i386:/lib:/usr/lib";
}