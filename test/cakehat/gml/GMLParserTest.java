package cakehat.gml;

import java.util.ArrayList;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.gml.InMemoryGML.Section;
import cakehat.assignment.Part;
import cakehat.database.CakeHatDBIOException;
import cakehat.database.ConfigurationData;
import cakehat.newdatabase.Group;
import java.io.File;
import java.sql.SQLException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Hannah
 */
public class GMLParserTest {
    
    private double DELTA = .00001;
    
    @Test
    public void testParseValidGMLFile() throws GradingSheetException, SQLException, CakeHatDBIOException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML validGML = GMLParser.parse(new File("test/cakehat/gml/ValidGMLFile.gml"), part, group);
        
        assertEquals(group, validGML.getGroup());
        assertEquals(part, validGML.getPart());
        
        assertEquals("ADDITIVE", validGML.getType());
        assertEquals("5.0", validGML.getVersion());
        
        // check sections
        assertEquals(2, validGML.getSections().size());
        Section section1 = validGML.getSections().get(0);
        Section section2 = validGML.getSections().get(1);
        
        // check section 1
        assertEquals("Problem 1", section1.getName());
        assertEquals(2, section1.getSubsections().size());
        Subsection p1a = section1.getSubsections().get(0);
        Subsection p1b = section1.getSubsections().get(1);
        
        // 1st subsection
        assertEquals("part a", p1a.getName());
        assertEquals(10, p1a.getOutOf(), DELTA);
        assertEquals(0, p1a.getEarned(), DELTA);
        assertEquals(0, p1a.getDetails().size());
        
        // 2nd subsection
        assertEquals("part b", p1b.getName());
        assertEquals(5, p1b.getOutOf(), DELTA);
        assertEquals(2, p1b.getEarned(), DELTA);
        assertEquals(0, p1b.getDetails().size());
        
        // check section 2
        assertEquals("Problem 2", section2.getName());
        assertEquals(1, section2.getSubsections().size());
        
        // check comments
        assertEquals("no comments", section2.getComment());
        
        // subsection
        Subsection p2 = section2.getSubsections().get(0);
        assertEquals("Problem 2", p2.getName());
        assertEquals(15, p2.getOutOf(), DELTA);
        assertEquals(0, p2.getEarned(), DELTA);
        assertEquals(2, p2.getDetails().size());
        
        // check details
        ArrayList<String> details = p2.getDetails();
        assertEquals("Detail 1", details.get(0));
        assertEquals("Detail 2", details.get(1));
        
    }
    
    @Test(expected=GradingSheetException.class)
    public void testNoFileExists() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("FakeFile.gml"), part, group);
    }
    
    
    @Test(expected=GMLException.class)
    public void testInvalidRootNode() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/InvalidRootNode.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testNoVersionAttribute() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoVersion.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testNoTypeAttribute() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoType.gml"), part, group);
    }
    
    @Test
    public void testNoSections() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoSections.gml"), part, group);
    }
    
    @Test
    public void testNoSubsections() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoSubsections.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testNoNameSubsection() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoNameSubsection.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testNoOutOfSubsection() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/NoOutOfSubsection.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testMultipleCommentsForOneSubsection() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/MultipleComments.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testInvalidRootNodeAttribute() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/InvalidRootAttribute.gml"), part, group);
    }
    
    @Test(expected=GMLException.class)
    public void testInvalidSectionNodeChild() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/IllegalSectionChildNode.gml"), part, group);
    }

    @Test(expected=GMLException.class)
    public void testInvalidSubsectionAttrNoEarned() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/IllegalSubsectionAttributeNoEarned.gml"), part, group);
    }

    @Test(expected=GMLException.class)
    public void testInvalidSubsectionAttrWithEarned() throws GradingSheetException {
        Part part = ConfigurationData.generatePartWithNoAttributes();
        Group group = ConfigurationData.generateGroupWithNoAttributes();
        InMemoryGML gml = GMLParser.parse(new File("test/cakehat/gml/IllegalSubsectionAttributeWithEarned.gml"), part, group);
    }

}
