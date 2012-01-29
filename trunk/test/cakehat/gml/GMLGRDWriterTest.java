/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cakehat.gml;

import java.io.IOException;
import cakehat.assignment.DeadlineInfo.DeadlineResolution;
import cakehat.assignment.DeadlineInfo;
import org.joda.time.DateTime;
import cakehat.newdatabase.DataServicesV5;
import cakehat.newdatabase.HandinTime;
import cakehat.newdatabase.TA;
import cakehat.assignment.Part;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Assignment;
import cakehat.database.ConfigurationData;
import cakehat.newdatabase.Group;
import cakehat.services.PathServices;
import cakehat.services.ServicesException;
import java.io.File;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 *
 * @author Hannah
 */
public class GMLGRDWriterTest {
    
    private static final String OUTPUT = "test/cakehat/gml/ouput.txt";
    private static final String GML_FILE = "test/cakehat/gml/GRDWriter.gml";
    private static final String NO_GML_OUTPUT = "test/cakehat/gml/NoGML.txt";
    
    @Test
    public void testGRDWriterNoGroups() throws GradingSheetException, ServicesException, IOException {
        
        // mock path services so that it reads in a test gml file
        final PathServices pathServices = createMock(PathServices.class);

        Group group = ConfigurationData.generateNewDatabaseGroup();
        Assignment asgn = group.getAssignment();
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            for (Part p : e.getParts()) {
                expect(pathServices.getGroupGMLFile(p, group)).andReturn(new File(GML_FILE)).anyTimes();
            }
        } 
        replay(pathServices);
        
        SingletonAllocation<PathServices> pathAlloc = 
                new SingletonAllocation<PathServices>()
                {
                    public PathServices allocate() { return pathServices; };
                };
        
        
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("hdrosen").anyTimes();
        expect(ta.getName()).andReturn("Hannah Rosen").anyTimes();
        expect(ta.getFirstName()).andReturn("Hannah").anyTimes();
        expect(ta.getLastName()).andReturn("Rosen").anyTimes();
        replay(ta);
        
        // mock dataservices
        final DataServicesV5 dataServices = createMock(DataServicesV5.class);
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            HandinTime time = createMock(HandinTime.class);
            expect(time.getDateRecorded()).andReturn(new DateTime(2012, 1, 25, 6, 37, 38)).anyTimes();
            expect(time.getGradableEvent()).andReturn(e).anyTimes();
            expect(time.getGroup()).andReturn(group).anyTimes();
            expect(time.getHandinTime()).andReturn(new DateTime(2012, 1, 24, 8, 9, 10)).anyTimes();
            replay(time);
            
            DeadlineInfo info = createMock(DeadlineInfo.class);
            expect(info.getEarlyDate()).andReturn(new DateTime(2012, 1, 23, 11, 59, 59));
            expect(info.getOnTimeDate()).andReturn(new DateTime(2012, 1, 24, 11, 59, 59));
            expect(info.getLateDate()).andReturn(new DateTime(2012, 1, 28, 11, 59, 59));
            
            DeadlineResolution res = createMock(DeadlineResolution.class);
            expect(res.getPenaltyOrBonus(anyDouble())).andReturn(5.0).anyTimes();
            expect(res.getTimeStatus()).andReturn(DeadlineInfo.TimeStatus.LATE).anyTimes();
            replay(res);
            
            expect(info.apply(time.getHandinTime(), null, null)).andReturn(res).anyTimes();
            replay(info);
            
            expect(dataServices.getHandinTime(e, group)).andReturn(time).anyTimes();
            expect(dataServices.getDeadlineInfo(e)).andReturn(info).anyTimes();
            
        }
        expect(dataServices.getGrader(anyObject(Part.class), anyObject(Group.class))).andReturn(ta).anyTimes();
        
        replay(dataServices);
        
        SingletonAllocation<DataServicesV5> dataAlloc = 
                new SingletonAllocation<DataServicesV5>()
                {
                    public DataServicesV5 allocate() { return dataServices; };
                };
        
        new Allocator.Customizer().setPathServices(pathAlloc).setDataServicesV5(dataAlloc).customize();

        GMLGRDWriter.write(group, new File(OUTPUT));
    }
    
    @Test
    public void testGRDWriterWhenPartHasNoGML() throws ServicesException, GradingSheetException {
        final PathServices pathServices = createMock(PathServices.class);

        Assignment asgn = ConfigurationData.generateAssignmentPartHasNoGML();
        Group group = ConfigurationData.generateNewDatabaseGroupWithAsgn(asgn);
        
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            for (Part p : e.getParts()) {
                expect(pathServices.getGroupGMLFile(p, group)).andReturn(new File(GML_FILE)).anyTimes();
            }
        } 
        replay(pathServices);
        
        SingletonAllocation<PathServices> pathAlloc = 
                new SingletonAllocation<PathServices>()
                {
                    public PathServices allocate() { return pathServices; };
                };
        
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("hdrosen").anyTimes();
        expect(ta.getName()).andReturn("Hannah Rosen").anyTimes();
        expect(ta.getFirstName()).andReturn("Hannah").anyTimes();
        expect(ta.getLastName()).andReturn("Rosen").anyTimes();
        replay(ta);

        Part part = asgn.getGradableEvents().get(0).getParts().get(0);
        
        // mock dataservices
        final DataServicesV5 dataServices = createMock(DataServicesV5.class);
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            HandinTime time = createMock(HandinTime.class);
            expect(time.getDateRecorded()).andReturn(new DateTime(2012, 1, 25, 6, 37, 38)).anyTimes();
            expect(time.getGradableEvent()).andReturn(e).anyTimes();
            expect(time.getGroup()).andReturn(group).anyTimes();
            expect(time.getHandinTime()).andReturn(new DateTime(2012, 1, 24, 8, 9, 10)).anyTimes();
            replay(time);
            
            DeadlineInfo info = createMock(DeadlineInfo.class);
            expect(info.getEarlyDate()).andReturn(new DateTime(2012, 1, 23, 11, 59, 59));
            expect(info.getOnTimeDate()).andReturn(new DateTime(2012, 1, 24, 11, 59, 59));
            expect(info.getLateDate()).andReturn(new DateTime(2012, 1, 28, 11, 59, 59));
            
            DeadlineResolution res = createMock(DeadlineResolution.class);
            expect(res.getPenaltyOrBonus(anyDouble())).andReturn(5.0).anyTimes();
            expect(res.getTimeStatus()).andReturn(DeadlineInfo.TimeStatus.LATE).anyTimes();
            replay(res);
            
            expect(info.apply(time.getHandinTime(), null, null)).andReturn(res).anyTimes();
            replay(info);
            
            expect(dataServices.getHandinTime(e, group)).andReturn(time).anyTimes();
            expect(dataServices.getDeadlineInfo(e)).andReturn(info).anyTimes();
            
        }
        expect(dataServices.getGrader(anyObject(Part.class), anyObject(Group.class))).andReturn(ta).anyTimes();
        expect(dataServices.getEarned(group, part)).andReturn(80.0).anyTimes();
        
        replay(dataServices);
        
        SingletonAllocation<DataServicesV5> dataAlloc = 
                new SingletonAllocation<DataServicesV5>()
                {
                    public DataServicesV5 allocate() { return dataServices; };
                };
        
        new Allocator.Customizer().setPathServices(pathAlloc).setDataServicesV5(dataAlloc).customize();
        
        GMLGRDWriter.write(group, new File(NO_GML_OUTPUT));
        
    }

}
