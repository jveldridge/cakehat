package cakehat.services;

import cakehat.assignment.Assignment;
import cakehat.database.Student;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jeldridg
 */
public interface GrdGenerator {
    
    public Map<Student, String> generateGRD(Assignment asgn, Set<Student> students) throws ServicesException;
    
}
