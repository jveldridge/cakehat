package utils;

public class CS015Constants extends Constants {

    /**
     * Implementations of methods specified by abstract superclass Constants
     */

    public String getCourse(){
        return "cs015";
    }

    public String getCourseDir(){
        return "/course/" + getCourse() + "/";
    }

    public String getCodeDir(){
        return ".code/";
    }

    public String getDeductionsListFilename(){
        return "grading_guide.txt";
    }

    public String getTemplateGradeSheetFilename(){
        return "rubric.xml";
    }

    public String getAssignmentDir(){
        return getCourseDir() + "grading/asgn/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
    }

    public String getHandinDir(){
        return getCourseDir() + "handin/";
    }

    public String getTestAccount(){
        return "cs015000";
    }

    public String getGradesTA(){
        return "jeldridg";
    }

    public String getGradesHTA(){
        return "aunger";
    }

    public String getEmailDomain(){
        return "cs.brown.edu";
    }

    public int getMinutesOfLeniency(){
        return 10;
    }

    public String getStudentGroup(){
        return getCourse() + "student";
    }

    public String getDemoDir(){
        return getCourseDir() + "project_demos/ordinary/";
    }

    public String getFinal(){
        return "Final";
    }

    public String getTesterDir(){
        return getCourseDir() + "grading/testers/";
    }

    public String getLabsDir(){
        return getCourseDir() + "labs/grades/current/";
    }

    public String getGraderPath(){
        return getCourseDir() + "grading/";
    }

    //TODO: Change back to config.xml
    public String getConfigFilePath(){
        return getCourseDir() + "grading/bin/" + Allocator.getGeneralUtilities().getCurrentYear() + "/" + "config_new_test.xml";
    }

    public String getRubricDirectoryPath() {
        return getCourseDir() + "grading/rubrics/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
    }

    public String getDatabaseFilePath(){
        return getCourseDir() + "grading/bin/" + Allocator.getGeneralUtilities().getCurrentYear() + "/cs015Database.db";
    }

    public String getDatabaseBackupDir(){
        return getCourseDir() + "grading/bin" + Allocator.getGeneralUtilities().getCurrentYear() + "/bak/";
    }

    public String getEmailAccount(){
        return "cs015000";
    }

    public String getEmailPassword(){
        return "andyCS15";
    }

    public String getEmailHost(){
        return "smtps.cs.brown.edu";
    }

    public String getEmailPort(){
        return "465";
    }

    public String getEmailCertPath(){
         return getCourseDir() + "grading/bin/smtp_certs/browncscerts.cert";
    }

    public String getEmailCertPassword(){
        return "andyCS15";
    }
}