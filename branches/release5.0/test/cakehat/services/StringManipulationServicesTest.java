package cakehat.services;

import com.google.common.collect.ImmutableList;
import cakehat.database.assignment.Assignment;
import cakehat.database.Group;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests the methods of {@link StringManipulationServices}.
 *
 * @author jeldridg
 */
public class StringManipulationServicesTest {

    private StringManipulationServices _services;

    @Before
    public void setup() {
        _services = new StringManipulationServicesImpl();
    }

    @Test
    public void localizeTextOneGroup() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(true).anyTimes();
        replay(asgn);

        Group group = createMock(Group.class);
        replay(group);

        Collection<Group> groups = ImmutableList.of(group);

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There is 1 group.";

        assertEquals("Incorrect result for a single group on assignment with groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextOneGroupCapitalized() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(true).anyTimes();
        replay(asgn);

        Group group = createMock(Group.class);
        replay(group);

        Collection<Group> groups = ImmutableList.of(group);

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.CAP_UNIT_TAG + ".";
        String expected = "There is 1 Group.";

        assertEquals("Incorrect result for a single group on assignment with groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextNoGroups() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(true).anyTimes();
        replay(asgn);
        
        Collection<Group> groups = Collections.emptyList();

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There are 0 groups.";

        assertEquals("Incorrect result for no groups on assignment with groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextMultipleGroups() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(true).anyTimes();
        replay(asgn);

        Group group1 = createMock(Group.class);
        replay(group1);

        Group group2 = createMock(Group.class);
        replay(group2);

        Group group3 = createMock(Group.class);
        replay(group3);

        Collection<Group> groups = ImmutableList.of(group1, group2, group3);

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There are 3 groups.";

        assertEquals("Incorrect result for multiple groups on assignment with groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextOneStudent() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        replay(asgn);

        Group group = createMock(Group.class);
        replay(group);

        Collection<Group> groups = ImmutableList.of(group);

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There is 1 student.";

        assertEquals("Incorrect result for a single group on assignment without groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextNoStudents() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        replay(asgn);

        Collection<Group> groups = Collections.emptyList();

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There are 0 students.";

        assertEquals("Incorrect result for no groups on assignment without groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }

    @Test
    public void localizeTextMultipleStudents() {
        Assignment asgn = createMock(Assignment.class);
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        replay(asgn);

        Group group1 = createMock(Group.class);
        replay(group1);

        Group group2 = createMock(Group.class);
        replay(group2);

        Group group3 = createMock(Group.class);
        replay(group3);

        Collection<Group> groups = ImmutableList.of(group1, group2, group3);

        String text = "There " + StringManipulationServices.BE_TAG + " " + StringManipulationServices.NUM_TAG + " " + StringManipulationServices.UNIT_TAG + ".";
        String expected = "There are 3 students.";

        assertEquals("Incorrect result for multiple groups on assignment without groups.",
                     expected, _services.localizeText(text, asgn, groups));
    }
}