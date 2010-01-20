package utils;

import config.Assignment;
import java.util.Comparator;

/**
 * AssignmentComparator compares assignments by their numbers.
 * 
 * @author jeldridg
 */
public class AssignmentComparator implements Comparator<Assignment> {
    public int compare(Assignment a1, Assignment a2) {
        return ((Integer)a1.getNumber()).compareTo(a2.getNumber());
    }
}