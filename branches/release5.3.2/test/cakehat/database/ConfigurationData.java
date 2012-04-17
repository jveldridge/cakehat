package cakehat.database;

import cakehat.database.Student;
import cakehat.database.TA;
import java.util.HashSet;
import java.util.Set;
import java.sql.SQLException;
import java.util.Random;
import java.util.ArrayList;
import java.util.UUID;

import static org.easymock.EasyMock.*;

/**
 * Creates and returns mocked data that under normal operation is built from
 * the data in the configuration file.
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
     * This method is solely for the purpose of testing things where the attributes of the 
     * group do not matter for example, the gml parser.
     * 
     * @return 
     */
    public static cakehat.database.Group generateGroupWithNoAttributes() {
        cakehat.database.Group group = createMock(cakehat.database.Group.class);
        return group;
    }
    
    /**
     * This method is solely for the purpose of testing things where the attributes of a 
     * part don't matter.
     * 
     * @return 
     */
    public static cakehat.database.assignment.Part generatePartWithNoAttributes() {
        cakehat.database.assignment.Part part = createMock(cakehat.database.assignment.Part.class);
        return part;
    }

    public static cakehat.database.assignment.Assignment generateNewNonGroupAssignment() {
        cakehat.database.assignment.Assignment asgn = createMock(cakehat.database.assignment.Assignment.class);
        final String name = "Some Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        cakehat.database.assignment.GradableEvent gradableEvent1 = createMock(cakehat.database.assignment.GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("Gradable Event 1").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        cakehat.database.assignment.GradableEvent gradableEvent2 = createMock(cakehat.database.assignment.GradableEvent.class);
        expect(gradableEvent2.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent2.getId()).andReturn(2).anyTimes();
        expect(gradableEvent2.getName()).andReturn("Gradable Event 2").anyTimes();
        expect(gradableEvent2.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        ArrayList<cakehat.database.assignment.GradableEvent> gradableEvents = new ArrayList<cakehat.database.assignment.GradableEvent>();
        gradableEvents.add(gradableEvent1);
        gradableEvents.add(gradableEvent2);
        
        expect(asgn.getGradableEvents()).andReturn(gradableEvents).anyTimes();
        
        cakehat.database.assignment.Part part1 = createMock(cakehat.database.assignment.Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        expect(part1.getOutOf()).andReturn(100.0).anyTimes();
        expect(part1.hasSpecifiedGMLTemplate()).andReturn(true).anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        cakehat.database.assignment.Part part2 = createMock(cakehat.database.assignment.Part.class);
        expect(part2.getName()).andReturn("The Easy Part").anyTimes();
        expect(part2.getId()).andReturn(2).anyTimes();
        expect(part2.getOutOf()).andReturn(80.0).anyTimes();
        expect(part2.getGradableEvent()).andReturn(gradableEvent2).anyTimes();
        expect(part2.hasSpecifiedGMLTemplate()).andReturn(true).anyTimes();
        
        replay(part2);
        
        
        cakehat.database.assignment.Part part3 = createMock(cakehat.database.assignment.Part.class);
        expect(part3.getName()).andReturn("The Middle Part").anyTimes();
        expect(part3.getId()).andReturn(3).anyTimes();
        expect(part3.getOutOf()).andReturn(20.0).anyTimes();
        expect(part3.hasSpecifiedGMLTemplate()).andReturn(true).anyTimes();
        
        expect(part3.getGradableEvent()).andReturn(gradableEvent2).anyTimes();
        replay(part3);

        ArrayList<cakehat.database.assignment.Part> partsGE1 = new ArrayList<cakehat.database.assignment.Part>();
        partsGE1.add(part1);
        
        ArrayList<cakehat.database.assignment.Part> partsGE2 = new ArrayList<cakehat.database.assignment.Part>();
        partsGE2.add(part2);
        partsGE2.add(part3);
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        expect(gradableEvent2.getParts()).andReturn(partsGE2).anyTimes();
        
        replay(gradableEvent1);
        replay(gradableEvent2);

        replay(asgn);

        return asgn;
    }
    
    public static cakehat.database.assignment.Assignment generateAsgnWithQuickNamePart() {
        cakehat.database.assignment.Assignment asgn = createMock(cakehat.database.assignment.Assignment.class);
        final String name = "Some Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        cakehat.database.assignment.GradableEvent gradableEvent1 = createMock(cakehat.database.assignment.GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("Gradable Event 1").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        
        cakehat.database.assignment.Part part1 = createMock(cakehat.database.assignment.Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        expect(part1.getOutOf()).andReturn(10.0).anyTimes();
        expect(part1.hasSpecifiedGMLTemplate()).andReturn(true).anyTimes();
        expect(part1.getQuickName()).andReturn("Lab0").anyTimes();
        expect(part1.hasQuickName()).andReturn(true).anyTimes();
        expect(part1.getFullDisplayName()).andReturn("Some Assignment - Gradable Event 1 - The Hard Part").anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        
        ArrayList<cakehat.database.assignment.Part> partsGE1 = new ArrayList<cakehat.database.assignment.Part>();
        partsGE1.add(part1);
        expect(gradableEvent1.iterator()).andReturn(partsGE1.iterator()).anyTimes();
        
        ArrayList<cakehat.database.assignment.GradableEvent> ges = new ArrayList<cakehat.database.assignment.GradableEvent>();
        ges.add(gradableEvent1);
        expect(asgn.getGradableEvents()).andReturn(ges).anyTimes(); 
        expect(asgn.iterator()).andReturn(ges.iterator()).anyTimes();
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        
        replay(gradableEvent1);
        replay(asgn);
        
        return asgn;
    }
    
    public static cakehat.database.assignment.Assignment generateAssignmentPartHasNoGML() {
        cakehat.database.assignment.Assignment asgn = createMock(cakehat.database.assignment.Assignment.class);
        final String name = "Some lab or something";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getId()).andReturn(1).anyTimes();
        expect(asgn.hasGroups()).andReturn(false).anyTimes();
        
        cakehat.database.assignment.GradableEvent gradableEvent1 = createMock(cakehat.database.assignment.GradableEvent.class);
        expect(gradableEvent1.getAssignment()).andReturn(asgn).anyTimes();
        expect(gradableEvent1.getId()).andReturn(1).anyTimes();
        expect(gradableEvent1.getName()).andReturn("No GML").anyTimes();
        expect(gradableEvent1.hasDigitalHandins()).andReturn(false).anyTimes();
        
        ArrayList<cakehat.database.assignment.GradableEvent> gradableEvents = new ArrayList<cakehat.database.assignment.GradableEvent>();
        gradableEvents.add(gradableEvent1);
        
        expect(asgn.getGradableEvents()).andReturn(gradableEvents).anyTimes();
        
        cakehat.database.assignment.Part part1 = createMock(cakehat.database.assignment.Part.class);
        expect(part1.getName()).andReturn("The Hard Part").anyTimes();
        expect(part1.getId()).andReturn(1).anyTimes();
        expect(part1.getOutOf()).andReturn(100.0).anyTimes();
        expect(part1.hasSpecifiedGMLTemplate()).andReturn(false).anyTimes();
        
        expect(part1.getGradableEvent()).andReturn(gradableEvent1).anyTimes();
        replay(part1);
        
        ArrayList<cakehat.database.assignment.Part> partsGE1 = new ArrayList<cakehat.database.assignment.Part>();
        partsGE1.add(part1);
        
        expect(gradableEvent1.getParts()).andReturn(partsGE1).anyTimes();
        
        replay(gradableEvent1);
        
        replay(asgn);
        
        return asgn;
    }
    
    public static cakehat.database.Group generateNewDatabaseGroupWithAsgn(cakehat.database.assignment.Assignment asgn) {
        Random rand = new Random();
        int numMembers = rand.nextInt(5) + 1;
        Set<cakehat.database.Student> members = new HashSet<cakehat.database.Student>(numMembers);
        for (int i = 0; i < numMembers; i++) {
            members.add(generateNewDatabaseStudent());
        }

        String name = generateRandomString();
        
        cakehat.database.Group group = createMock(cakehat.database.Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(1).anyTimes();
        expect(group.getMembers()).andReturn(members).anyTimes();
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        
        return group;
    }
    
    public static cakehat.database.Group generateNewDatabaseGroup() {
        Random rand = new Random();
        int numMembers = rand.nextInt(5) + 1;
        Set<cakehat.database.Student> members = new HashSet<cakehat.database.Student>(numMembers);
        for (int i = 0; i < numMembers; i++) {
            members.add(generateNewDatabaseStudent());
        }
        
        cakehat.database.assignment.Assignment asgn = generateNewNonGroupAssignment();
        String name = generateRandomString();
        
        cakehat.database.Group group = createMock(cakehat.database.Group.class);
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
        
        cakehat.database.Student student = createMock(cakehat.database.Student.class);
        expect(student.getLogin()).andReturn(login).anyTimes();
        expect(student.getFirstName()).andReturn(firstName).anyTimes();
        expect(student.getLastName()).andReturn(lastName).anyTimes();
        expect(student.getName()).andReturn(firstName + " " + lastName);
        expect(student.getId()).andReturn(studentID).anyTimes();
        replay(student);

    return student;
    }
    
    public static cakehat.database.Group generateGroupWithStudent(cakehat.database.Student stud, cakehat.database.assignment.Assignment asgn) {
        String name = generateRandomString();
        
        int groupID = 1;
        HashSet<cakehat.database.Student> set = new HashSet<cakehat.database.Student>();
        set.add(stud);
        
        cakehat.database.Group group = createMock(cakehat.database.Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(groupID).anyTimes();
        expect(group.getOnlyMember()).andReturn(stud);
        expect(group.getMembers()).andReturn(set).anyTimes();
        expect(group.isGroupOfOne()).andReturn(true);
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        return group;
    }
    
    public static cakehat.database.Group generateGroupWithStudents(cakehat.database.assignment.Assignment asgn, cakehat.database.Student... members) {
        String name = generateRandomString();
        
        int groupID = 1;
        HashSet<cakehat.database.Student> set = new HashSet<cakehat.database.Student>();
        for (cakehat.database.Student s : members) {
            set.add(s);
        }
        
        cakehat.database.Group group = createMock(cakehat.database.Group.class);
        expect(group.getAssignment()).andReturn(asgn).anyTimes();
        expect(group.getId()).andReturn(groupID).anyTimes();
        expect(group.getMembers()).andReturn(set).anyTimes();
        expect(group.isGroupOfOne()).andReturn(false);
        expect(group.getName()).andReturn(name).anyTimes();
        
        replay(group);
        return group;
    }
    
    public static cakehat.database.Student generateNewStudent(String login, String first, String last, int ID) {
        cakehat.database.Student student = createMock(cakehat.database.Student.class);
        expect(student.getLogin()).andReturn(login).anyTimes();
        expect(student.getFirstName()).andReturn(first).anyTimes();
        expect(student.getLastName()).andReturn(last).anyTimes();
        expect(student.getName()).andReturn(first + " " + last);
        expect(student.getId()).andReturn(ID).anyTimes();
        replay(student);
        
        return student;
    }
    
//    /**
//     * Adds a random student to the given database and generates and returns the
//     * corresponding Student object.
//     * 
//     * @return 
//     */
//    public static Student generateRandomStudent(Database db) throws SQLException
//    {
//        String login = generateRandomString();
//        String firstName = generateRandomString();
//        String lastName = generateRandomString();
//        
//        int studentID = db.addStudent(login, firstName, lastName);
//
//        return new Student(studentID, login, firstName, lastName, true);
//    }
//    
//    public static Student generateStudent(int id, String login, String firstName,
//                                          String lastName, String email, boolean isEnabled) {
//        return new Student(id, login, firstName, lastName, isEnabled);
//    }

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

//    public static Handin generateHandin()
//    {
//        Assignment asgn = createMock(Assignment.class);
//        final String name = "Difficult Assignment";
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//
//        Handin handin = createMock(Handin.class);
//        expect(handin.getAssignment()).andReturn(asgn).anyTimes();
//        replay(handin);
//
//        expect(asgn.getHandin()).andReturn(handin).anyTimes();
//        replay(asgn);
//
//        return handin;
//    }
//    
//    public static Assignment generateNonGroupAssignment() {
//        Assignment asgn = createMock(Assignment.class);
//        final String name = "Some Assignment";
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//        expect(asgn.hasGroups()).andReturn(false).anyTimes();
//
//        DistributablePart dp1 = createMock(DistributablePart.class);
//        expect(dp1.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp1.getName()).andReturn("The Hard Part").anyTimes();
//        expect(dp1.getDBID()).andReturn("Amazing Assignment - The Hard Part").anyTimes();
//        expect(dp1.getNumber()).andReturn(1).anyTimes();
//        expect(dp1.getPoints()).andReturn(97).anyTimes();
//        replay(dp1);
//
//        ArrayList<Part> parts = new ArrayList<Part>();
//        parts.add(dp1);
//        expect(asgn.getParts()).andReturn(parts).anyTimes();
//        expect(asgn.getPartIDs()).andReturn(ImmutableList.of(dp1.getDBID())).anyTimes();
//        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp1)).anyTimes();
//        
//        Handin handin = createMock(Handin.class);
//        expect(handin.getAssignment()).andReturn(asgn).anyTimes();
//        replay(handin);
//
//        expect(asgn.getHandin()).andReturn(handin).anyTimes();
//
//        replay(asgn);
//
//        return asgn;
//    }
//    
//    public static Assignment generateRandomGroupAssignment() {
//        int choice = (int) Math.random() * 3;
//        switch (choice) {
//            case 0:
//                return generateGroupAssignmentNotJustDistributableParts();
//            case 1:
//                return generateGroupAssignmentWithMutuallyExclusive();
//            default:
//                return generateGroupAssignmentWithTwoDistributableParts();
//        }          
//    }
//    
//    public static Assignment generateGroupAssignmentWithNameWithTwoDPs(String name) {
//        Assignment asgn = createMock(Assignment.class);
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//        expect(asgn.hasGroups()).andReturn(true).anyTimes();
//    
//        DistributablePart dp1 = createMock(DistributablePart.class);
//        expect(dp1.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp1.getName()).andReturn("Part 1").anyTimes();
//        expect(dp1.getDBID()).andReturn("Part 1").anyTimes();
//        expect(dp1.getNumber()).andReturn(1).anyTimes();
//        expect(dp1.getPoints()).andReturn(97).anyTimes();
//        replay(dp1);
//
//        DistributablePart dp2 = createMock(DistributablePart.class);
//        expect(dp2.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp2.getName()).andReturn("Part 2").anyTimes();
//        expect(dp2.getDBID()).andReturn("Part 2").anyTimes();
//        expect(dp2.getNumber()).andReturn(2).anyTimes();
//        expect(dp2.getPoints()).andReturn(10).anyTimes();
//        replay(dp2);
//
//        ArrayList<Part> parts = new ArrayList<Part>();
//        parts.add(dp1);
//        parts.add(dp2);
//        expect(asgn.getParts()).andReturn(parts).anyTimes();
//        expect(asgn.getPartIDs()).andReturn(ImmutableList.of(dp1.getDBID(), dp2.getDBID())).anyTimes();
//        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp1, dp2)).anyTimes();
//
//        replay(asgn);
//
//        return asgn;
//    }
//
//    public static Assignment generateGroupAssignmentWithTwoDistributableParts()
//    {
//        Assignment asgn = createMock(Assignment.class);
//        final String name = "Amazing Assignment";
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//        expect(asgn.hasGroups()).andReturn(true).anyTimes();
//
//        DistributablePart dp1 = createMock(DistributablePart.class);
//        expect(dp1.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp1.getName()).andReturn("The Hard Part").anyTimes();
//        expect(dp1.getDBID()).andReturn("Amazing Assignment - The Hard Part").anyTimes();
//        expect(dp1.getNumber()).andReturn(1).anyTimes();
//        expect(dp1.getPoints()).andReturn(97).anyTimes();
//        replay(dp1);
//
//        DistributablePart dp2 = createMock(DistributablePart.class);
//        expect(dp2.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp2.getName()).andReturn("The Easy Part").anyTimes();
//        expect(dp2.getDBID()).andReturn("Amazing Assignment - The Easy Part").anyTimes();
//        expect(dp2.getNumber()).andReturn(2).anyTimes();
//        expect(dp2.getPoints()).andReturn(10).anyTimes();
//        replay(dp2);
//
//        ArrayList<Part> parts = new ArrayList<Part>();
//        parts.add(dp1);
//        parts.add(dp2);
//        expect(asgn.getParts()).andReturn(parts).anyTimes();
//        expect(asgn.getPartIDs()).andReturn(ImmutableList.of(dp1.getDBID(), dp2.getDBID())).anyTimes();
//        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp1, dp2)).anyTimes();
//
//        replay(asgn);
//
//        return asgn;
//    }
//
//    public static Assignment generateGroupAssignmentNotJustDistributableParts()
//    {
//        Assignment asgn = createMock(Assignment.class);
//        final String name = "Mediocre Assignment";
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//        expect(asgn.hasGroups()).andReturn(true).anyTimes();
//
//        DistributablePart dp = createMock(DistributablePart.class);
//        expect(dp.getAssignment()).andReturn(asgn).anyTimes();
//        expect(dp.getName()).andReturn("The Only Distributable Part").anyTimes();
//        expect(dp.getDBID()).andReturn("Mediocre Assignment - The Only Distributable Part").anyTimes();
//        expect(dp.getNumber()).andReturn(1).anyTimes();
//        expect(dp.getPoints()).andReturn(55).anyTimes();
//        replay(dp);
//
//        NonHandinPart nonHandin = createMock(NonHandinPart.class);
//        expect(nonHandin.getAssignment()).andReturn(asgn).anyTimes();
//        expect(nonHandin.getName()).andReturn("Photo Collage").anyTimes();
//        expect(nonHandin.getDBID()).andReturn("Mediocre Assignment - Photo Collage").anyTimes();
//        expect(nonHandin.getNumber()).andReturn(2).anyTimes();
//        expect(nonHandin.getPoints()).andReturn(200).anyTimes();
//        replay(nonHandin);
//
//        LabPart lab = createMock(LabPart.class);
//        expect(lab.getAssignment()).andReturn(asgn).anyTimes();
//        expect(lab.getName()).andReturn("Chemistry Lab").anyTimes();
//        expect(lab.getDBID()).andReturn("Mediocre Assignment - Chemistry Lab").anyTimes();
//        expect(lab.getNumber()).andReturn(3).anyTimes();
//        expect(lab.getPoints()).andReturn(10).anyTimes();
//        replay(lab);
//
//        ArrayList<Part> parts = new ArrayList<Part>();
//        parts.add(dp);
//        parts.add(nonHandin);
//        parts.add(lab);
//        expect(asgn.getParts()).andReturn(parts).anyTimes();
//        expect(asgn.getPartIDs()).andReturn(ImmutableList.of(dp.getDBID(), nonHandin.getDBID(), lab.getDBID())).anyTimes();
//        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp)).anyTimes();
//
//        replay(asgn);
//
//        return asgn;
//    }
//
//    public static Assignment generateGroupAssignmentWithMutuallyExclusive()
//    {
//        Assignment asgn = createMock(Assignment.class);
//        final String name = "Boring Assignment";
//        expect(asgn.getName()).andReturn(name).anyTimes();
//        expect(asgn.getDBID()).andReturn(name).anyTimes();
//
//        NonHandinPart nonHandin = createMock(NonHandinPart.class);
//        expect(nonHandin.getAssignment()).andReturn(asgn).anyTimes();
//        expect(nonHandin.getName()).andReturn("Math Paper").anyTimes();
//        expect(nonHandin.getDBID()).andReturn("Boring Assignment - Math Paper").anyTimes();
//        expect(nonHandin.getNumber()).andReturn(1).anyTimes();
//        expect(nonHandin.getPoints()).andReturn(15).anyTimes();
//        replay(nonHandin);
//
//        LabPart lab = createMock(LabPart.class);
//        expect(lab.getAssignment()).andReturn(asgn).anyTimes();
//        expect(lab.getName()).andReturn("Math Lab").anyTimes();
//        expect(lab.getDBID()).andReturn("Boring Assignment - Math Lab").anyTimes();
//        expect(lab.getNumber()).andReturn(1).anyTimes();
//        expect(lab.getPoints()).andReturn(15).anyTimes();
//        replay(lab);
//
//        ArrayList<Part> parts = new ArrayList<Part>();
//        parts.add(nonHandin);
//        parts.add(lab);
//        expect(asgn.getParts()).andReturn(parts).anyTimes();
//
//        expect(asgn.getDistributableParts()).andReturn(new ArrayList<DistributablePart>()).anyTimes();
//
//        replay(asgn);
//
//        return asgn;
//    }
//
//    public static NewGroup generateNewGroup(Assignment asgn, Student student) {
//        return new NewGroup(asgn, student);
//    }
//    
//    public static NewGroup generateNewGroup(Assignment asgn, String name, Student... members) {
//        return new NewGroup(asgn, name, members);
//    }
//    
//    public static NewGroup generateNewGroup(Assignment asgn, String name, Collection<Student> members) {
//        return new NewGroup(asgn, name, members);
//    }
//    
//    public static Group generateGroup(int dbID, Assignment asgn, String name, Student... members) {
//        return new Group(dbID, asgn, name, members);
//    }
//    
//    /**
//     * Creates and returns a random NewGroup consisting of new random Students
//     * who have been added to the give Database.
//     * 
//     * @param db
//     * @return
//     * @throws SQLException 
//     */
//    public static NewGroup generateRandomGroup(Database db) throws SQLException {
//        Random rand = new Random();
//        int numMembers = rand.nextInt(5) + 1;
//        List<Student> members = new ArrayList<Student>(numMembers);
//        for (int i = 0; i < numMembers; i++) {
//            members.add(generateRandomStudent(db));
//        }
//        
//        Assignment asgn = generateRandomGroupAssignment();
//        String name = generateRandomString();
//        
//        return new NewGroup(asgn, name, members);
//    }
//    
//    /**
//     * Creates and returns a NewGroup with the specified name and assignment consisting
//     * of new random Students who have been added to the given Database.
//     * 
//     * @param name
//     * @param asgn
//     * @param db
//     * @return
//     * @throws SQLException 
//     */
//    public static NewGroup generateRandomNewGroupWithNameAndAsgn(String name, Assignment asgn, Database db) throws SQLException {
//        Random rand = new Random();
//        int numMembers = rand.nextInt(5) + 1;
//        List<Student> members = new ArrayList<Student>(numMembers);
//        for (int i = 0; i < numMembers; i++) {
//            members.add(generateRandomStudent(db));
//        }
//        
//        return new NewGroup(asgn, name, members);
//    }

}