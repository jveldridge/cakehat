package cakehat.database;

import cakehat.assignment.GradableEvent;
import cakehat.assignment.Assignment;
import cakehat.assignment.Part;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.ArrayList;
import java.util.UUID;

import static org.easymock.EasyMock.*;

/**
 * Creates and returns mocked data that under normal operation is built from the data in the configuration file.
 *
 * @author jak2
 */
public class ConfigurationData
{
    public static TA generateRandomTA()
    {
        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn(generateRandomString()).anyTimes();
        expect(ta.getName()).andReturn(generateRandomName()).anyTimes();
        replay(ta);

        return ta;
    }
    
    /**
     * This method is solely for the purpose of testing things where the attributes of the group do not matter for
     * example, the gml parser.
     * 
     * @return 
     */
    public static Group generateGroupWithNoAttributes() {
        Group group = createMock(Group.class);
        return group;
    }
    
    /**
     * This method is solely for the purpose of testing things where the attributes of a part don't matter.
     * 
     * @return 
     */
    public static Part generatePartWithNoAttributes() {
        Part part = createMock(Part.class);
        return part;
    }

    public static Assignment generateNewNonGroupAssignment() {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Some Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        GradableEvent gradableEvent1 = createMock(GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("Gradable Event 1").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        GradableEvent gradableEvent2 = createMock(GradableEvent.class);
        expect(gradableEvent2.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent2.getId()).andReturn(2).anyTimes();
        expect(gradableEvent2.getName()).andReturn("Gradable Event 2").anyTimes();
        expect(gradableEvent2.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        ArrayList<GradableEvent> gradableEvents = new ArrayList<GradableEvent>();
        gradableEvents.add(gradableEvent1);
        gradableEvents.add(gradableEvent2);
        
        expect(asgn.getGradableEvents()).andReturn(gradableEvents).anyTimes();
        
        Part part1 = createMock(Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        Part part2 = createMock(Part.class);
        expect(part2.getName()).andReturn("The Easy Part").anyTimes();
        expect(part2.getId()).andReturn(2).anyTimes();
        expect(part2.getGradableEvent()).andReturn(gradableEvent2).anyTimes();
        
        replay(part2);
        
        
        Part part3 = createMock(Part.class);
        expect(part3.getName()).andReturn("The Middle Part").anyTimes();
        expect(part3.getId()).andReturn(3).anyTimes();
        
        expect(part3.getGradableEvent()).andReturn(gradableEvent2).anyTimes();
        replay(part3);

        ArrayList<Part> partsGE1 = new ArrayList<Part>();
        partsGE1.add(part1);
        
        ArrayList<Part> partsGE2 = new ArrayList<Part>();
        partsGE2.add(part2);
        partsGE2.add(part3);
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        expect(gradableEvent2.getParts()).andReturn(partsGE2).anyTimes();
        
        replay(gradableEvent1);
        replay(gradableEvent2);

        replay(asgn);

        return asgn;
    }
    
    public static Assignment generateAsgnWithQuickNamePart() {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Some Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        GradableEvent gradableEvent1 = createMock(GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("Gradable Event 1").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        Part part1 = createMock(Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        expect(part1.getQuickName()).andReturn("Lab0").anyTimes();
        expect(part1.hasQuickName()).andReturn(true).anyTimes();
        expect(part1.getFullDisplayName()).andReturn("Some Assignment - Gradable Event 1 - The Hard Part").anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        
        ArrayList<Part> partsGE1 = new ArrayList<Part>();
        partsGE1.add(part1);
        expect(gradableEvent1.iterator()).andReturn(partsGE1.iterator()).anyTimes();
        
        ArrayList<GradableEvent> ges = new ArrayList<GradableEvent>();
        ges.add(gradableEvent1);
        expect(asgn.getGradableEvents()).andReturn(ges).anyTimes(); 
        expect(asgn.iterator()).andReturn(ges.iterator()).anyTimes();
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        
        replay(gradableEvent1);
        replay(asgn);
        
        return asgn;
    }
    
    public static Assignment generateAssignmentPartHasNoGML() {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Some lab or something";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        GradableEvent gradableEvent1 = createMock(GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("No GML").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        ArrayList<GradableEvent> gradableEvents = new ArrayList<GradableEvent>();
        gradableEvents.add(gradableEvent1);
        
        expect(asgn.getGradableEvents()).andReturn(gradableEvents).anyTimes();
        
        Part part1 = createMock(Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        ArrayList<Part> partsGE1 = new ArrayList<Part>();
        partsGE1.add(part1);
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        
        replay(gradableEvent1);
        
        replay(asgn);
        
        return asgn;
    }
    
    public static Group generateNewDatabaseGroupWithAsgn(Assignment asgn) {
        Random rand = new Random();
        int numMembers = rand.nextInt(5) + 1;
        Set<Student> members = new HashSet<Student>(numMembers);
        for (int i = 0; i < numMembers; i++) {
            members.add(generateNewDatabaseStudent());
        }

        String name = generateRandomString();
        
        Group group = createMock(Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(1).anyTimes();
        expect(group.getMembers()).andReturn(members).anyTimes();
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        
        return group;
    }
    
    public static Group generateNewDatabaseGroup() {
        Random rand = new Random();
        int numMembers = rand.nextInt(5) + 1;
        Set<Student> members = new HashSet<Student>(numMembers);
        for (int i = 0; i < numMembers; i++) {
            members.add(generateNewDatabaseStudent());
        }
        
        cakehat.assignment.Assignment asgn = generateNewNonGroupAssignment();
        String name = generateRandomString();
        
        Group group = createMock(Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(1).anyTimes();
        expect(group.getMembers()).andReturn(members).anyTimes();
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        
        return group;
    }
    
    public static cakehat.database.Student generateNewDatabaseStudent() {
        String login = generateRandomString();
        String firstName = generateRandomString();
        String lastName = generateRandomString();
        
        int studentID = 1;
        
        Student student = createMock(Student.class);
        expect(student.getLogin()).andReturn(login).anyTimes();
        expect(student.getFirstName()).andReturn(firstName).anyTimes();
        expect(student.getLastName()).andReturn(lastName).anyTimes();
        expect(student.getName()).andReturn(firstName + " " + lastName);
        expect(student.getId()).andReturn(studentID).anyTimes();
        replay(student);

    return student;
    }
    
    public static cakehat.database.Group generateGroupWithStudent(Student stud, Assignment asgn) {
        String name = generateRandomString();
        
        int groupID = 1;
        HashSet<Student> set = new HashSet<Student>();
        set.add(stud);
        
        Group group = createMock(Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(groupID).anyTimes();
        expect(group.getOnlyMember()).andReturn(stud);
        expect(group.getMembers()).andReturn(set).anyTimes();
        expect(group.isGroupOfOne()).andReturn(true);
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        return group;
    }
    
    public static Group generateGroupWithStudents(Assignment asgn, Student... members) {
        String name = generateRandomString();
        
        int groupID = 1;
        HashSet<cakehat.database.Student> set = new HashSet<cakehat.database.Student>();
        for (cakehat.database.Student s : members) {
            set.add(s);
        }
        
        Group group = createMock(Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(groupID).anyTimes();
        expect(group.getMembers()).andReturn(set).anyTimes();
        expect(group.isGroupOfOne()).andReturn(false);
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        return group;
    }
    
    public static Student generateNewStudent(String login, String first, String last, int ID) {
        Student student = createMock(Student.class);
        expect(student.getLogin()).andReturn(login).anyTimes();
        expect(student.getFirstName()).andReturn(first).anyTimes();
        expect(student.getLastName()).andReturn(last).anyTimes();
        expect(student.getName()).andReturn(first + " " + last);
        expect(student.getId()).andReturn(ID).anyTimes();
        replay(student);
        
        return student;
    }

    private static String generateRandomName()
    {
        String name = "";
        name += generateRandomString();
        name += " ";
        name += generateRandomString();
        return name;
    }

    public static String generateRandomString()
    {
        String uid = UUID.randomUUID().toString();
        uid.replaceAll("-", "");
        uid = uid.substring(8);
        return uid;
    }
}