package gradesystem;

import gradesystem.services.GradingServices;
import gradesystem.database.DBWrapper;
import gradesystem.database.DatabaseIO;
import gradesystem.services.StudentServices;
import gradesystem.utils.ArchiveUtilities;
import gradesystem.utils.CalendarUtilities;
import gradesystem.utils.ExternalProcessesUtilities;
import gradesystem.utils.FileSystemUtilities;
import gradesystem.utils.GeneralUtilities;
import gradesystem.utils.UserUtilities;

/**
 * Used to statically get references to utility classes.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class Allocator {

    //Course info and constants
    private static gradesystem.config.CourseInfo COURSE_INFO = null;
    public static gradesystem.config.CourseInfo getCourseInfo(){
        if(COURSE_INFO == null){
            COURSE_INFO = new gradesystem.config.CourseInfo();
        }

        return COURSE_INFO;
    }

    //RubricManager
    private static gradesystem.rubric.RubricMananger RUBRIC_MANAGER = null;
    public static gradesystem.rubric.RubricMananger getRubricManager(){
        if(RUBRIC_MANAGER == null){
            RUBRIC_MANAGER = new gradesystem.rubric.RubricMananger();
        }

        return RUBRIC_MANAGER;
    }

    //General Utilities
    private static GeneralUtilities GENERAL_UTILITIES = null;
    public static GeneralUtilities getGeneralUtilities(){
        if(GENERAL_UTILITIES == null){
            GENERAL_UTILITIES = new GeneralUtilities();
        }

        return GENERAL_UTILITIES;
    }

    //Archive Utilities
    private static ArchiveUtilities ARCHIVE_UTILITIES = null;
    public static ArchiveUtilities getArchiveUtilities(){
        if(ARCHIVE_UTILITIES == null){
            ARCHIVE_UTILITIES = new ArchiveUtilities();
        }

        return ARCHIVE_UTILITIES;
    }

    //Calendar Utilities
    private static CalendarUtilities CALENDAR_UTILITIES = null;
    public static CalendarUtilities getCalendarUtilities(){
        if(CALENDAR_UTILITIES == null){
            CALENDAR_UTILITIES = new CalendarUtilities();
        }

        return CALENDAR_UTILITIES;
    }

    //External Processes Utilities
    private static ExternalProcessesUtilities EXTERNAL_PROCESSES_UTILITIES = null;
    public static ExternalProcessesUtilities getExternalProcessesUtilities(){
        if(EXTERNAL_PROCESSES_UTILITIES == null){
            EXTERNAL_PROCESSES_UTILITIES = new ExternalProcessesUtilities();
        }

        return EXTERNAL_PROCESSES_UTILITIES;
    }

    //File System Utilities
    private static FileSystemUtilities FILE_SYSTEM_UTILITIES = null;
    public static FileSystemUtilities getFileSystemUtilities(){
        if(FILE_SYSTEM_UTILITIES == null){
            FILE_SYSTEM_UTILITIES = new FileSystemUtilities();
        }

        return FILE_SYSTEM_UTILITIES;
    }

    //User Utilities
    private static UserUtilities USER_UTILITIES = null;
    public static UserUtilities getUserUtilities(){
        if(USER_UTILITIES == null){
            USER_UTILITIES = new UserUtilities();
        }

        return USER_UTILITIES;
    }

    //Grading Services
    private static GradingServices GRADING_SERVICES = null;
    public static GradingServices getGradingServices(){
        if(GRADING_SERVICES == null){
            GRADING_SERVICES = new GradingServices();
        }

        return GRADING_SERVICES;
    }

    //Student Services
    private static StudentServices STUDENT_SERVICS = null;
    public static StudentServices getStudentServices(){
        if(STUDENT_SERVICS == null){
            STUDENT_SERVICS = new StudentServices();
        }

        return STUDENT_SERVICS;
    }

    //DatabaseIO
    private static DatabaseIO DATABASE_IO = null;
    public static DatabaseIO getDatabaseIO() {
        if (DATABASE_IO == null) {
            DATABASE_IO = new DBWrapper();
        }
        return DATABASE_IO;
    }

    //Landscape Printer
    private static gradesystem.services.printing.Printer LANDSCAPE_PRINTER = null;
    public static gradesystem.services.printing.Printer getLandscapePrinter() {
        if(LANDSCAPE_PRINTER == null){
            LANDSCAPE_PRINTER = new gradesystem.services.printing.EnscriptPrinter();
        }
        return LANDSCAPE_PRINTER;
    }

    //Portrait Printer
    private static gradesystem.services.printing.Printer PORTRAIT_PRINTER = null;
    public static gradesystem.services.printing.Printer getPortraitPrinter() {
        if(PORTRAIT_PRINTER == null){
            PORTRAIT_PRINTER = new gradesystem.services.printing.LprPrinter();
        }
        return PORTRAIT_PRINTER;
    }

    //Exporter
    private static gradesystem.export.Exporter CSV_EXPORTER = null;
    public static gradesystem.export.Exporter getCSVExporter() {
        if(CSV_EXPORTER == null){
            CSV_EXPORTER = new gradesystem.export.CSVExporter();
        }
        return CSV_EXPORTER;
    }
    
}