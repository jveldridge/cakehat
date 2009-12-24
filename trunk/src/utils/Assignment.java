package utils;

import java.util.Calendar;

/**
 * Represents an assignment as specified in the configuration file.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class Assignment {

    //Assignment name (e.g. Clock)
    public String Name = "";
    //Assignment number (e.g. 1)
    public int Number;
    //Assignment type (e.g. PROJECT)
    public AssignmentType Type;
    //Points for the assignment, broken down into total and design questions
    public Points Points = new Points();
    //Associated out and in dates for the assignment
    public Calendar Outdate, Early, Ontime, Late;

    /**
     * Package private constructor so that this class is only
     * ever constructed by the ConfigurationManager.
     */
    Assignment() { }

    public String toString() {
        return Name;
    }
}