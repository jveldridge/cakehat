package utils;

import config.Assignment;
import java.util.Comparator;

/**
 * AssignmentComparator compares assignments by their numbers
 * 
 * @author jeldridg
 */
public class AssignmentComparator implements Comparator{

    public int compare(Object o1, Object o2) {
                Assignment a1 = (Assignment) o1;
                Assignment a2 = (Assignment) o2;
                return ((Integer)a1.getNumber()).compareTo(a2.getNumber());
            }
    
}
