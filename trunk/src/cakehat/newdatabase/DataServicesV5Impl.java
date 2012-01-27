package cakehat.newdatabase;

import cakehat.assignment.DeadlineInfo;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import java.util.Set;
import org.joda.time.DateTime;

/**
 *
 * @author Hannah
 */
public class DataServicesV5Impl implements DataServicesV5 {

    public TA getGrader(Part part, Group group) throws ServicesException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGrader(Part part, Group group, TA ta) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Double getEarned(Group group, Part part) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Double setEarned(Group group, Part part, double earned) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<TA> getTAs() throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DeadlineInfo getDeadlineInfo(GradableEvent gradableEvent) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HandinTime getHandinTime(GradableEvent gradableEvent, Part part) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHandinTime(GradableEvent gradableEvent, Part part, DateTime handinTime) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
