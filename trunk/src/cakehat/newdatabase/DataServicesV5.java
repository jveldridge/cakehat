package cakehat.newdatabase;

import cakehat.assignment.Part;
import cakehat.services.ServicesException;

/**
 *
 * @author Hannah
 */
public interface DataServicesV5 {
    
    public TA getGrader(Part part, Group group) throws ServicesException;
    
}
