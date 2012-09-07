package cakehat.gml;

import cakehat.database.Extension;
import com.google.common.collect.ImmutableSet;
import cakehat.database.PartGrade;
import java.io.IOException;
import cakehat.database.DeadlineInfo.DeadlineResolution;
import cakehat.database.DeadlineInfo;
import org.joda.time.DateTime;
import cakehat.database.DataServices;
import cakehat.database.GradableEventOccurrence;
import cakehat.database.TA;
import cakehat.assignment.Part;
import cakehat.Allocator;
import cakehat.Allocator.SingletonAllocation;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Assignment;
import cakehat.database.ConfigurationData;
import cakehat.database.Group;
import cakehat.services.PathServices;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import org.junit.AfterClass;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 *
 * @author Hannah
 */
public class GMLGRDWriterTest {
    
    private static boolean DELETE_GENERATED_FILES = true;
    
    private static final File OUTPUT = new File("test/cakehat/gml/output.txt");
    private static final File GML_FILE = new File("test/cakehat/gml/GRDWriter.gml");
    private static final File NO_GML_OUTPUT = new File("test/cakehat/gml/NoGML.txt");
    
    @AfterClass
    public static void cleanupGeneratedFiles()
    {
        if(DELETE_GENERATED_FILES)
        {
            OUTPUT.delete();
            NO_GML_OUTPUT.delete();
        }
    }
    
    @Test
    public void testGRDWriterNoGroups() throws GradingSheetException, ServicesException, IOException {
        
        // mock path services so that it reads in a test gml file
        final PathServices pathServices = createMock(PathServices.class);

        Group group = ConfigurationData.generateNewDatabaseGroup();
        Assignment asgn = group.getAssignment();
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            for (Part p : e.getParts()) {
                expect(pathServices.getGroupGMLFile(p, group)).andReturn(GML_FILE).anyTimes();
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
        final DataServices dataServices = createMock(DataServices.class);
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            GradableEventOccurrence occurrence = createMock(GradableEventOccurrence.class);
            expect(occurrence.getDateRecorded()).andReturn(new DateTime(2012, 1, 25, 6, 37, 38)).anyTimes();
            expect(occurrence.getGradableEvent()).andReturn(e).anyTimes();
            expect(occurrence.getGroup()).andReturn(group).anyTimes();
            expect(occurrence.getOccurrenceDate()).andReturn(new DateTime(2012, 1, 24, 8, 9, 10)).anyTimes();
            replay(occurrence);
            
            DeadlineInfo info = createMock(DeadlineInfo.class);
            
            DeadlineResolution res = createMock(DeadlineResolution.class);
            expect(res.getPenaltyOrBonus(anyDouble())).andReturn(5.0).anyTimes();
            expect(res.getTimeStatus()).andReturn(DeadlineInfo.TimeStatus.LATE).anyTimes();
            replay(res);
            
            expect(info.apply(occurrence.getOccurrenceDate(), null)).andReturn(res).anyTimes();
            replay(info);
            
            expect(dataServices.getGradableEventOccurrences(eq(e), eq(ImmutableSet.of(group))))
                    .andReturn(ImmutableMap.of(group, occurrence)).anyTimes();
            expect(dataServices.getExtensions(eq(e), eq(ImmutableSet.of(group))))
                    .andReturn(ImmutableMap.<Group, Extension>of()).anyTimes();
            
            expect(dataServices.getDeadlineInfo(e)).andReturn(info).anyTimes();
            for (Part p : e.getParts()) {
                PartGrade grade = createMock(PartGrade.class);
                expect(grade.getDateRecorded()).andReturn(new DateTime()).anyTimes();
                double rand = Math.random()*p.getOutOf();
                expect(grade.getEarned()).andReturn(rand).anyTimes();
                expect(grade.getGroup()).andReturn(group).anyTimes();
                expect(grade.getPart()).andReturn(p).anyTimes();
                expect(grade.getTA()).andReturn(ta).anyTimes();
                expect(grade.isSubmitted()).andReturn(true).anyTimes();
                replay(grade);
                
                expect(dataServices.getEarned(group, p)).andReturn(grade).anyTimes();
            }
            
        }
        expect(dataServices.getGrader(anyObject(Part.class), anyObject(Group.class))).andReturn(ta).anyTimes();
        
        replay(dataServices);
        
        SingletonAllocation<DataServices> dataAlloc = 
                new SingletonAllocation<DataServices>()
                {
                    public DataServices allocate() { return dataServices; };
                };
        
        new Allocator.Customizer().setPathServices(pathAlloc).setDataServices(dataAlloc).customize();

        GMLGRDWriter.write(group, OUTPUT);
    }
    
    @Test
    public void testGRDWriterWhenPartHasNoGML() throws ServicesException, GradingSheetException {
        final PathServices pathServices = createMock(PathServices.class);

        Assignment asgn = ConfigurationData.generateAssignmentPartHasNoGML();
        Group group = ConfigurationData.generateNewDatabaseGroupWithAsgn(asgn);
        
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            for (Part p : e.getParts()) {
                expect(pathServices.getGroupGMLFile(p, group)).andReturn(GML_FILE).anyTimes();
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
        final DataServices dataServices = createMock(DataServices.class);
        
        for (GradableEvent e : asgn.getGradableEvents()) {
            GradableEventOccurrence occurrence = createMock(GradableEventOccurrence.class);
            expect(occurrence.getDateRecorded()).andReturn(new DateTime(2012, 1, 25, 6, 37, 38)).anyTimes();
            expect(occurrence.getGradableEvent()).andReturn(e).anyTimes();
            expect(occurrence.getGroup()).andReturn(group).anyTimes();
            expect(occurrence.getOccurrenceDate()).andReturn(new DateTime(2012, 1, 24, 8, 9, 10)).anyTimes();
            replay(occurrence);
            
            DeadlineInfo info = createMock(DeadlineInfo.class);
            
            DeadlineResolution res = createMock(DeadlineResolution.class);
            expect(res.getPenaltyOrBonus(anyDouble())).andReturn(5.0).anyTimes();
            expect(res.getTimeStatus()).andReturn(DeadlineInfo.TimeStatus.LATE).anyTimes();
            replay(res);
            
            expect(info.apply(occurrence.getOccurrenceDate(), null)).andReturn(res).anyTimes();
            replay(info);
            
            expect(dataServices.getGradableEventOccurrences(eq(e), eq(ImmutableSet.of(group))))
                    .andReturn(ImmutableMap.of(group, occurrence)).anyTimes();
            expect(dataServices.getExtensions(eq(e), eq(ImmutableSet.of(group))))
                    .andReturn(ImmutableMap.<Group, Extension>of()).anyTimes();
            
            expect(dataServices.getDeadlineInfo(e)).andReturn(info).anyTimes();
            
        }
        expect(dataServices.getGrader(anyObject(Part.class), anyObject(Group.class))).andReturn(ta).anyTimes();
        expect(dataServices.getEarned(group, part)).andReturn(null).anyTimes();
        
        replay(dataServices);
        
        SingletonAllocation<DataServices> dataAlloc = 
                new SingletonAllocation<DataServices>()
                {
                    public DataServices allocate() { return dataServices; };
                };
        
        new Allocator.Customizer().setPathServices(pathAlloc).setDataServices(dataAlloc).customize();
        
        GMLGRDWriter.write(group, NO_GML_OUTPUT);
    }
}