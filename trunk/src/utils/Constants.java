package utils;

/**
 * @deprecated to be replaced by config.CourseInfo
 *
 * Constants that are used throughout the grading system.
 *
 * @author jak2 (Joshua Kaplan)
 */
public abstract class Constants {

    /**
     * Course
     *
     * @return
     */
    public abstract String getCourse();

    /**
     * Course directory
     *
     * @return
     */
    public abstract String getCourseDir();

    /**
     * Name of the hidden folder the code is untarred and compiled in
     *
     * @return
     */
    public abstract String getCodeDir();

    /**
     * Name of the file that contains deductions for a project
     *
     * @return
     */
    public abstract String getDeductionsListFilename();

    /**
     * Name of XML template for a project
     *
     * @return
     */
    public abstract String getTemplateGradeSheetFilename();

    /**
     * Directory containing rubric.xml and grading_guide.txt
     * 
     * @return
     */
    public abstract String getAssignmentDir();

    /**
     * Get handin directory for a course
     *
     * @return
     */
    public abstract String getHandinDir();

    /**
     * Course's test account
     * @return
     */
    public abstract String getTestAccount();

    /**
     * TODO: Change this to be in the config file
     *
     * Login of ta in charge of grades
     *
     * @return
     */
    public abstract String getGradesTA();

    /**
     * TODO: Change this to be in the config file
     * 
     * Login of hta in charge of grades
     *
     * @return
     */
    public abstract String getGradesHTA();

    /**
     * Email domain
     *
     * @return
     */
    public abstract String getEmailDomain();

    /**
     * Default number of minutes to extend deadline by
     *
     * @return
     */
    public abstract int getMinutesOfLeniency();

    /**
     * Student group
     *
     * @return
     */
    public abstract String getStudentGroup();

    /**
     * Top level directory that contains demos
     *
     * @return
     */
    public abstract String getDemoDir();

    /**
     * Hack to support final projects
     *
     * @return
     */
    public abstract String getFinal();

    /**
     * Top level directory that contains testers
     * 
     * @return
     */
    public abstract String getTesterDir();

    /**
     * Directory containing files that track those checked off for lab
     *
     * @return
     */
    public abstract String getLabsDir();

    /**
     * Path to a grading TA's directory
     *
     * @return
     */
    public abstract String getGraderPath();

    /**
     * Path to the configuration file
     *
     * @return
     */
    public abstract String getConfigFilePath();

     /**
     * Path to the directory where student rubrics are stored
     *
     * @return
     */
    public abstract String getRubricDirectoryPath();

    /**
     * Path to the database file
     *
     * @return
     */
    public abstract String getDatabaseFilePath();

    /**
     * Directory to backup the database in
     *
     * @return
     */
    public abstract String getDatabaseBackupDir();

    /**
     * Login of cs account that will actually send email
     * Suggestion: use course's test account
     *
     * @return
     */
    public abstract String getEmailAccount();

    /**
     * Email password of cs account that will send email
     *
     * @return
     */
    public abstract String getEmailPassword();

    /**
     * Email host
     * 
     * @return
     */
    public abstract String getEmailHost();

    /**
     * Email port
     *
     * @return
     */
    public abstract String getEmailPort();

    /**
     * Path to the certification file used for sending email
     * Generated with "keytool -import -alias smtps.cs.brown.edu -file browncs-ca.crt -keystore browncscerts.cert"
     * 
     * @return
     */
    public abstract String getEmailCertPath();

    /**
     * Password for the certification
     *
     * @return
     */
    public abstract String getEmailCertPassword();

}