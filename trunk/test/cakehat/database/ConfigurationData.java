package cakehat.database;

import cakehat.config.Assignment;
import cakehat.config.LabPart;
import cakehat.config.NonHandinPart;
import cakehat.config.Part;
import cakehat.config.TA;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
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

    public static Handin generateHandin()
    {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Difficult Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getDBID()).andReturn(name).anyTimes();

        Handin handin = createMock(Handin.class);
        expect(handin.getAssignment()).andReturn(asgn).anyTimes();
        replay(handin);

        expect(asgn.getHandin()).andReturn(handin).anyTimes();
        replay(asgn);

        return handin;
    }

    public static Assignment generateAssignmentWithTwoDistributableParts()
    {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Amazing Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getDBID()).andReturn(name).anyTimes();

        DistributablePart dp1 = createMock(DistributablePart.class);
        expect(dp1.getAssignment()).andReturn(asgn).anyTimes();
        expect(dp1.getName()).andReturn("The Hard Part").anyTimes();
        expect(dp1.getDBID()).andReturn("Amazing Assignment - The Hard Part").anyTimes();
        expect(dp1.getNumber()).andReturn(1).anyTimes();
        expect(dp1.getPoints()).andReturn(97).anyTimes();
        replay(dp1);

        DistributablePart dp2 = createMock(DistributablePart.class);
        expect(dp2.getAssignment()).andReturn(asgn).anyTimes();
        expect(dp2.getName()).andReturn("The Easy Part").anyTimes();
        expect(dp2.getDBID()).andReturn("Amazing Assignment - The Easy Part").anyTimes();
        expect(dp2.getNumber()).andReturn(2).anyTimes();
        expect(dp2.getPoints()).andReturn(10).anyTimes();
        replay(dp2);

        ArrayList<Part> parts = new ArrayList<Part>();
        parts.add(dp1);
        parts.add(dp2);
        expect(asgn.getParts()).andReturn(parts).anyTimes();

        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp1, dp2)).anyTimes();

        replay(asgn);

        return asgn;
    }

    public static Assignment generateAssignmentNotJustDistributableParts()
    {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Mediocre Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getDBID()).andReturn(name).anyTimes();

        DistributablePart dp = createMock(DistributablePart.class);
        expect(dp.getAssignment()).andReturn(asgn).anyTimes();
        expect(dp.getName()).andReturn("The Only Distributable Part").anyTimes();
        expect(dp.getDBID()).andReturn("Mediocre Assignment - The Only Distributable Part").anyTimes();
        expect(dp.getNumber()).andReturn(1).anyTimes();
        expect(dp.getPoints()).andReturn(55).anyTimes();
        replay(dp);

        NonHandinPart nonHandin = createMock(NonHandinPart.class);
        expect(nonHandin.getAssignment()).andReturn(asgn).anyTimes();
        expect(nonHandin.getName()).andReturn("Photo Collage").anyTimes();
        expect(nonHandin.getDBID()).andReturn("Mediocre Assignment - Photo Collage").anyTimes();
        expect(nonHandin.getNumber()).andReturn(2).anyTimes();
        expect(nonHandin.getPoints()).andReturn(200).anyTimes();
        replay(nonHandin);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getAssignment()).andReturn(asgn).anyTimes();
        expect(lab.getName()).andReturn("Chemistry Lab").anyTimes();
        expect(lab.getDBID()).andReturn("Mediocre Assignment - Chemistry Lab").anyTimes();
        expect(lab.getNumber()).andReturn(3).anyTimes();
        expect(lab.getPoints()).andReturn(10).anyTimes();
        replay(lab);

        ArrayList<Part> parts = new ArrayList<Part>();
        parts.add(dp);
        parts.add(nonHandin);
        parts.add(lab);
        expect(asgn.getParts()).andReturn(parts).anyTimes();

        expect(asgn.getDistributableParts()).andReturn(Arrays.asList(dp)).anyTimes();

        replay(asgn);

        return asgn;
    }

    public static Assignment generateAssignmentWithMutuallyExclusive()
    {
        Assignment asgn = createMock(Assignment.class);
        final String name = "Boring Assignment";
        expect(asgn.getName()).andReturn(name).anyTimes();
        expect(asgn.getDBID()).andReturn(name).anyTimes();

        NonHandinPart nonHandin = createMock(NonHandinPart.class);
        expect(nonHandin.getAssignment()).andReturn(asgn).anyTimes();
        expect(nonHandin.getName()).andReturn("Math Paper").anyTimes();
        expect(nonHandin.getDBID()).andReturn("Boring Assignment - Math Paper").anyTimes();
        expect(nonHandin.getNumber()).andReturn(1).anyTimes();
        expect(nonHandin.getPoints()).andReturn(15).anyTimes();
        replay(nonHandin);

        LabPart lab = createMock(LabPart.class);
        expect(lab.getAssignment()).andReturn(asgn).anyTimes();
        expect(lab.getName()).andReturn("Math Lab").anyTimes();
        expect(lab.getDBID()).andReturn("Boring Assignment - Math Lab").anyTimes();
        expect(lab.getNumber()).andReturn(1).anyTimes();
        expect(lab.getPoints()).andReturn(15).anyTimes();
        replay(lab);

        ArrayList<Part> parts = new ArrayList<Part>();
        parts.add(nonHandin);
        parts.add(lab);
        expect(asgn.getParts()).andReturn(parts).anyTimes();

        expect(asgn.getDistributableParts()).andReturn(new ArrayList<DistributablePart>()).anyTimes();

        replay(asgn);

        return asgn;
    }

    public static Group generateRandomGroup() {
        Random rand = new Random();
        int numMembers = rand.nextInt(5) + 1;
        ArrayList<String> members = new ArrayList<String>();
        for (int i = 0; i < numMembers; i++) {
            members.add(generateRandomString());
        }
        return new Group(generateRandomString(), members);
    }

}