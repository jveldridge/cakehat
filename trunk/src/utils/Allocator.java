package utils;

import utils.printing.EnscriptPrinter;
import utils.printing.Printer;
import utils.printing.LprPrinter;
import java.io.File;
import java.util.HashMap;

/**
 * Used to statically get references to utility classes.
 * 
 * This class will need to be modified for each CS course so
 * that makes use of the appropriate subclasses that are specific
 * to that course and the programming languages it uses.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class Allocator {

    //Constants
    private static Constants CONSTANTS = null;
    public static Constants getConstants(){
        if(CONSTANTS == null){
            CONSTANTS = new CS015Constants(); //Change as appropriate
        }

        return CONSTANTS;
    }

    //New constants
    //TODO: Eventually remove above Constants and replace with this
    private static config.Constants NEW_CONSTANTS = null;
    public static config.Constants getNewConstants(){
        if(NEW_CONSTANTS == null){
            NEW_CONSTANTS = new config.Constants();
        }

        return NEW_CONSTANTS;
    }


    //General Utilities
    private static GeneralUtilities GENERAL_UTILITIES = null;
    public static GeneralUtilities getGeneralUtilities(){
        if(GENERAL_UTILITIES == null){
            GENERAL_UTILITIES = new GeneralUtilities();
        }

        return GENERAL_UTILITIES;
    }

    //Frontend Utilities
    private static FrontendUtilities FRONTEND_UTILITIES = null;
    public static FrontendUtilities getFrontendUtilities(){
        if(FRONTEND_UTILITIES == null){
            FRONTEND_UTILITIES = new FrontendUtilities();
        }

        return FRONTEND_UTILITIES;
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
    private static Printer LANDSCAPE_PRINTER = null;
    public static Printer getLandscapePrinter() {
        if(LANDSCAPE_PRINTER == null){
            LANDSCAPE_PRINTER = new EnscriptPrinter();
        }
        return LANDSCAPE_PRINTER;
    }

    //Portrait Printer
    private static Printer PORTRAIT_PRINTER = null;
    public static Printer getPortraitPrinter() {
        if(PORTRAIT_PRINTER == null){
            PORTRAIT_PRINTER = new LprPrinter();
        }
        return PORTRAIT_PRINTER;
    }
    
    //Projects
    //All projects that have been loaded so far, to avoid loading a project more than once
    private static HashMap<String, Project> PROJECTS = new HashMap<String, Project>();

    /**
     * TODO: Consider having this method throw a ProjectNotFoundException instead of returning null if a project isn't found
     *
     * Only way to get an instance of Project - there is no public constructor.
     * If a name has been passed in that does not have a directory in the course's
     * handin directory then an error will be printed and null will be returned.
     *
     * @param name
     * @return
     */
    public static Project getProject(String name) {
        Project prj = null;
        //Check if the project has been created yet
        if (PROJECTS.containsKey(name)) {
            prj = PROJECTS.get(name);
        }
        //Otherwise create the project and store it
        else {
            //Build path to handin directory
            String prjPath = Allocator.getConstants().getHandinDir() + name + "/";
            //Check if directory exists
            if(new File(prjPath).exists()) {
                prj = new CS015Project(name); //Change as appropriate
                PROJECTS.put(name, prj);
            }
            //Otherwise print an error and null will be returned
            else {
                System.err.println("Cannot create project for " + name);
                System.err.println(prjPath + " does not exist.");
            }
        }

        return prj;
    }

}