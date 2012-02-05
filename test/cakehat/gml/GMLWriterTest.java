package cakehat.gml;

import org.junit.AfterClass;
import cakehat.database.assignment.Part;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.gml.InMemoryGML.Section;
import cakehat.database.ConfigurationData;
import cakehat.database.Group;
import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 *
 * @author Hannah
 */
public class GMLWriterTest {
    
    private static boolean DELETE_GENERATED_FILES = true;
    
    private static String COMMENT = "no comments";
    private static String DETAIL1 = "Detail 1";
    private static String DETAIL2 = "Detail 2";
    private static double DELTA = .00001;
    private static double OUTOF1A = 10;
    private static double OUTOF1B = 5;
    private static double OUTOF2 = 15;
    private static double SCORE1A = 0;
    private static double SCORE1B = 2;
    private static double SCORE2 = 0;
    private static String SECTION1 = "Problem 1";
    private static String SECTION2 = "Problem 2";
    private static String SUB1PROB1 = "part a";
    private static String SUB2PROB1 = "part b";
    private static String SUB1PROB2 = "Problem 2";
    private static String TYPE = "ADDITIVE";
    private static String VERSION = "5.0";
    
    private static final File OUTPUT = new File("test/cakehat/gml/output.gml");
    
    @AfterClass
    public static void cleanupGeneratedFiles()
    {
        if(DELETE_GENERATED_FILES)
        {
            OUTPUT.delete();
        }
    }
    
    
    @Test
    public void testWriteGMLFile() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        
        // set up gml to write to file
        InMemoryGML gml = new InMemoryGML(part, group);
        gml.setVersion(VERSION);
        gml.setType(TYPE);
        
        // add sections to gml
        Section section1 = gml.addSection(SECTION1);
        section1.addSubsection(SUB1PROB1, SCORE1A, OUTOF1A);
        section1.addSubsection(SUB2PROB1, SCORE1B, OUTOF1B);
        
        Section section2 = gml.addSection(SECTION2);
        section2.setComment(COMMENT);
        Subsection sub = section2.addSubsection(SUB1PROB2, SCORE2, OUTOF2);
        sub.addDetail(DETAIL1);
        sub.addDetail(DETAIL2);
        
        // write rubric to gml file
        GMLWriter.write(gml, new File("test/cakehat/gml/output.gml"));
        
        // read back gml and check that everything is correct
        InMemoryGML read = GMLParser.parse(OUTPUT, part, group);
        
        assertEquals(part, read.getPart());
        assertEquals(group, read.getGroup());
        
        assertEquals(VERSION, read.getVersion());
        assertEquals(TYPE, read.getType());
        
        // check sections
        assertEquals(2, read.getSections().size());
        assertEquals(SECTION1, read.getSections().get(0).getName());
        assertEquals(SECTION2, read.getSections().get(1).getName());
        
        
        // check section 1
        assertEquals(2, read.getSections().get(0).getSubsections().size());
        assertEquals(SUB1PROB1, read.getSections().get(0).getSubsections().get(0).getName());
        assertEquals(SUB2PROB1, read.getSections().get(0).getSubsections().get(1).getName());
        assertEquals(OUTOF1A, read.getSections().get(0).getSubsections().get(0).getOutOf(), DELTA);
        assertEquals(OUTOF1B, read.getSections().get(0).getSubsections().get(1).getOutOf(), DELTA);
        assertEquals(SCORE1A, read.getSections().get(0).getSubsections().get(0).getEarned(), DELTA);
        assertEquals(SCORE1B, read.getSections().get(0).getSubsections().get(1).getEarned(), DELTA);
        assertNull(read.getSections().get(0).getComment());
        assertEquals(0, read.getSections().get(0).getSubsections().get(0).getDetails().size());
        assertEquals(0, read.getSections().get(0).getSubsections().get(1).getDetails().size());
        
        // check section 2
        assertEquals(1, read.getSections().get(1).getSubsections().size());
        assertEquals(SUB1PROB2, read.getSections().get(1).getSubsections().get(0).getName());
        assertEquals(OUTOF2, read.getSections().get(1).getSubsections().get(0).getOutOf(), DELTA);
        assertEquals(SCORE2, read.getSections().get(1).getSubsections().get(0).getEarned(), DELTA);
        assertEquals(COMMENT, read.getSections().get(1).getComment());
        assertEquals(2, read.getSections().get(1).getSubsections().get(0).getDetails().size());
        assertEquals(DETAIL1, read.getSections().get(1).getSubsections().get(0).getDetails().get(0));
        assertEquals(DETAIL2, read.getSections().get(1).getSubsections().get(0).getDetails().get(1));
    }
}
