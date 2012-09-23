package cakehat.database;

import org.joda.time.Period;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Assignment;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import java.io.IOException;
import org.junit.Before;
import cakehat.assignment.DeadlineInfo;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jak2
 */
public class DataServicesDeadlineInfoTest
{
    private Database _database;
    
    @Before
    public void setUp() throws ServicesException, SQLException, IOException
    {
        _database = new DatabaseImpl(Allocator.getFileSystemUtilities().createTempFile("tempDB", "db"));
        SingletonAllocation<Database> dbioAlloc =
            new SingletonAllocation<Database>()
            {
                @Override
                public Database allocate() { return _database; };
            };
        _database.resetDatabase();
        
        new Allocator.Customizer().setDatabase(dbioAlloc).customize();
    }
    
    @Test
    public void testGetDeadlineInfoForFixedDeadline() throws SQLException, ServicesException
    {
        DateTime onTime = new DateTime(2012, 2, 14, 5, 5, 5);
        DateTime early = new DateTime(2012, 2, 12, 5, 5, 5);
        DateTime late = new DateTime(2012, 2, 16, 5, 5, 5);
        double earlyPoints = 10.0;
        double latePoints = -5.0;
        
        DbAssignment dbAsgn = new DbAssignment("Assignment", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        
        DbGradableEvent dbGe = DbGradableEvent.build(dbAsgn, "Gradable Event", 1);
        dbGe.setDeadlineType(DeadlineInfo.Type.FIXED);
        dbGe.setEarlyDate(early);
        dbGe.setOnTimeDate(onTime);
        dbGe.setLateDate(late);
        dbGe.setLatePoints(latePoints);
        dbGe.setEarlyPoints(earlyPoints);
        _database.putGradableEvents(ImmutableSet.of(dbGe));
        
        DeadlineInfo info = null;
        for(Assignment asgn : Allocator.getDataServices().getAssignments())
        {
            if(asgn.getId() == dbAsgn.getId())
            {
                for(GradableEvent ge : asgn)
                {
                    if(ge.getId() == dbGe.getId())
                    {
                        info = ge.getDeadlineInfo();
                    }
                }
            }
        }
        
        assertNotNull(info);
        assertEquals(DeadlineInfo.Type.FIXED, info.getType()); 
        assertTrue(info.getEarlyDate().equals(early));
        assertTrue(info.getOnTimeDate().equals(onTime));
        assertTrue(info.getLateDate().equals(late));
        assertEquals(earlyPoints, info.getEarlyPoints(), 0.00001);
        assertEquals(latePoints, info.getLatePoints(), 0.00001);  
        assertNull(info.getLatePeriod());
    }
    
    @Test
    public void testGetDeadlineInfoForVariableDeadline() throws SQLException, ServicesException
    {
        DateTime onTime = new DateTime(2012, 2, 14, 5, 5, 5);
        DateTime late = new DateTime(2012, 2, 16, 5, 5, 5);
        Period latePeriod = Period.days(3);
        double latePoints = -5.0;
        
        DbAssignment dbAsgn = new DbAssignment("Assignment", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        
        DbGradableEvent dbGe = DbGradableEvent.build(dbAsgn, "Gradable Event", 1);
        dbGe.setDeadlineType(DeadlineInfo.Type.VARIABLE);
        dbGe.setOnTimeDate(onTime);
        dbGe.setLateDate(late);
        dbGe.setLatePoints(latePoints);
        dbGe.setLatePeriod(latePeriod);
        _database.putGradableEvents(ImmutableSet.of(dbGe));
        
        DeadlineInfo info = null;
        for(Assignment asgn : Allocator.getDataServices().getAssignments())
        {
            if(asgn.getId() == dbAsgn.getId())
            {
                for(GradableEvent ge : asgn)
                {
                    if(ge.getId() == dbGe.getId())
                    {
                        info = ge.getDeadlineInfo();
                    }
                }
            }
        }
        
        assertEquals(DeadlineInfo.Type.VARIABLE, info.getType()); 
        assertNull(info.getEarlyDate());
        assertTrue(info.getOnTimeDate().equals(onTime));
        assertTrue(info.getLateDate().equals(late));
        assertNull(info.getEarlyPoints());
        assertEquals(latePoints, info.getLatePoints(), 0.00001);  
        assertEquals(latePeriod, info.getLatePeriod());
    }
    
    @Test
    public void testGetDeadlineInfoForNoDeadline() throws ServicesException, SQLException
    {
        DbAssignment dbAsgn = new DbAssignment("Assignment", 1);
        _database.putAssignments(ImmutableSet.of(dbAsgn));
        
        DbGradableEvent dbGe = DbGradableEvent.build(dbAsgn, "Gradable Event", 1);
        dbGe.setDeadlineType(DeadlineInfo.Type.NONE);
        _database.putGradableEvents(ImmutableSet.of(dbGe));
        
        DeadlineInfo info = null;
        for(Assignment asgn : Allocator.getDataServices().getAssignments())
        {
            if(asgn.getId() == dbAsgn.getId())
            {
                for(GradableEvent ge : asgn)
                {
                    if(ge.getId() == dbGe.getId())
                    {
                        info = ge.getDeadlineInfo();
                    }
                }
            }
        }
        
        assertEquals(DeadlineInfo.Type.NONE, info.getType());
        assertNull(info.getEarlyDate());
        assertNull(info.getOnTimeDate());
        assertNull(info.getLateDate());
        assertNull(info.getEarlyPoints());
        assertNull(info.getLatePoints());  
        assertNull(info.getLatePeriod());
    }
}