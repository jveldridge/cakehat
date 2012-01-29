package cakehat.gml;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.DeadlineInfo;
import cakehat.assignment.DeadlineInfo.DeadlineResolution;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.gml.InMemoryGML.Section;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.newdatabase.Group;
import cakehat.newdatabase.HandinTime;
import cakehat.newdatabase.Student;
import cakehat.newdatabase.TA;
import cakehat.services.ServicesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.joda.time.DateTime;

/**
 *
 * @author Hannah
 */
public class GMLGRDWriter {
    
    private final static String GRADER_LBL = "GRADER: ";
    private final static String DEADLINE_RES_LBL = "Deadline Resolution: ";
    private final static String PART_LABEL = "PART: ";
    private final static String UNKNOWN = "???";
    private final static String SCORE = "Score:";
    
    private final static int SECTION_TEXT_WIDTH = 58;
    private final static int SCORE_TEXT_WIDTH = 7;
    private final static int NUM_DIVIDERS = 3;
    private final static int TOTAL_TEXT_WIDTH = SECTION_TEXT_WIDTH + (SCORE_TEXT_WIDTH * 2) + NUM_DIVIDERS;
    private final static int SECTION_INDENT_WIDTH = 8;
    private final static int DETAIL_INDENT_WIDTH = 12;
    private final static int TOTAL_INDENT_WIDTH = 49;
    private final static int STATIC_TEXT_WIDTH = 28;
    
    public static void write(Group group, File grdFile) throws GradingSheetException {
        
        //Get the writer to write to file
        BufferedWriter output = openGRDFile(grdFile);
        
        writeAssignmentDetails(group, output);
        
        Score totalScore = new Score(0, 0);
        
        for (GradableEvent gradableEvent : group.getAssignment().getGradableEvents()) {
            Score eventScore = writeGradableEvent(gradableEvent, group, output);
            totalScore._earned += eventScore._earned;
            totalScore._outOf += eventScore._outOf;
        }
        
        writeFinalScore(group, totalScore , output);

        closeFile(output);
    }
    
    private static BufferedWriter openGRDFile(File grdFile) throws GradingSheetException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(grdFile));
        }
        catch (IOException e) {
            throw new GradingSheetException("Opening the file to write failed.", e);
        }
        return output;
    }
    
    private static void writeAssignmentDetails(Group group, BufferedWriter output) throws GradingSheetException {
        
        Assignment asgn = group.getAssignment();
        
        //For centering the first line
        int emptySpace = TOTAL_TEXT_WIDTH - asgn.getName().length();
        printSpaces(emptySpace / 2, output);
        
        writeLine(asgn.getName() + '\n', output);
        
        Iterator<Student> members = group.getMembers().iterator();
        if (group.getMembers().size() == 1) {
            Student student = members.next();
            writeLine("STUDENT: " + student.getName() + " (" + student.getLogin() + ")", output);
        }
        else {
            writeLine("GROUP: " + group.getName(), output);
            Student member = members.next();
            writeLine("MEMBERS: " + member.getName() + " (" + member.getLogin() + ")", output);
            while (members.hasNext()) {
                member = members.next();
                printSpaces(9, output);
                writeLine(member.getName() + " (" + member.getLogin() + ")", output);
            }
        }
        
        writeLine("", output);
    }
    
    private static Score writeGradableEvent(GradableEvent event, Group group, 
                                            BufferedWriter output) throws GradingSheetException {
        printStrongDivider(output);
        
        String line1 = event.getName();
        write(line1, output);
        printSpaces(SECTION_TEXT_WIDTH - line1.length(), output);
        printSpaces(SCORE_TEXT_WIDTH + SCORE_TEXT_WIDTH + 2, output);
        writeLine("|", output);
        
        Score totalScore = new Score(0, 0);
        
        for (Part part : event.getParts()) {
            Score dpScore = writePart(group, part, output);
            totalScore._earned += dpScore._earned;
            totalScore._outOf += dpScore._outOf;
        }
        writeGradableEventScore(event, group, totalScore, output);
        return totalScore;
    }
    
    private static Score writePart(Group group, Part part,
                                    BufferedWriter output) throws GradingSheetException {
        
        printStrongDivider(output);
        
        String line1 = PART_LABEL + part.getName();
        write(line1, output);
        printSpaces(SECTION_TEXT_WIDTH - line1.length(), output);
        write("| ", output);
        write("YOUR", output);
        printSpaces(SCORE_TEXT_WIDTH - 5, output);
        write("| ", output);
        write("OUT", output);
        printSpaces(SCORE_TEXT_WIDTH - 4, output);
        writeLine("|", output);
        
        TA grader = null;
        try {
            grader = Allocator.getDataServicesV5().getGrader(part, group);
        } catch (ServicesException ex) {
            throw new GradingSheetException("Could not read grader for group " + group + " on " +
                                      "part " + part + " of assignment " + group.getAssignment().getName() + ". ", ex);
        }
        String line2 = GRADER_LBL;
        if (grader != null) {
            line2 += grader.getName() + " (" + grader.getLogin() + ")";
        }
        else {
            line2 += "Grader Unknown";
        }
        write(line2, output);
        printSpaces(SECTION_TEXT_WIDTH - line2.length(), output);
        write("| ", output);
        write("SCORE", output);
        printSpaces(SCORE_TEXT_WIDTH - 6, output);
        write("| ", output);
        write("OF", output);
        printSpaces(SCORE_TEXT_WIDTH - 3, output);
        writeLine("|", output);

        printStrongDivider(output);
        
        if (part.hasSpecifiedGMLTemplate()) {
            File gmlFile = Allocator.getPathServices().getGroupGMLFile(part, group);
            if (gmlFile.exists()) {
                InMemoryGML gml = GMLParser.parse(gmlFile, part, group);

                for (Section section : gml.getSections()) {
                    writeSection(section, output);
                }
                return writeScores(gml, output);
            }
            else {
                if (part.getGMLTemplate().exists()) {
                    InMemoryGML gml = GMLParser.parse(part.getGMLTemplate(), part, group);
                    
                    for (Section section : gml.getSections()) {
                        writeSection(section, output);
                    }
                    return writeScores(gml, output);
                }
                else {
                    writeLine("No grading sheet available.", output);
                    return new Score(0, 0);
                }

            }
        }
        else {
            double totalScore;
            try {
                Double earned = Allocator.getDataServicesV5().getEarned(group, part);
                if (earned == null) {
                    totalScore = 0;
                }
                else {
                    totalScore = earned;
                }
            }
            catch(ServicesException ex) {
                throw new GradingSheetException("Unable to retrieve score for group " + group + " on assignment " + group.getAssignment() + ".");
            }
            printWithinBounds(0, SECTION_TEXT_WIDTH, "Earned: ", output);
            printEnd(totalScore, part.getOutOf(), output);
            return new Score(totalScore, part.getOutOf());
        }
    }
    
    private static void writeSection(Section section, BufferedWriter output) throws GradingSheetException {
        String name = section.getName();
        
        printWithinBounds(0, SECTION_TEXT_WIDTH, name, output);
        printEnd(null, null, output);
        writeLine("", output);
        
        for (Subsection subsection : section.getSubsections()) {
            writeSubsection(subsection, output);
        }
        
        //Print comments
        if(section.getComment() != null) {
            printSpaces(SECTION_TEXT_WIDTH, output);
            printEnd(null, null, output);
            writeLine("", output);

            printWithinBounds(0, SECTION_TEXT_WIDTH, "Comments:", output);
            printEnd(null, null, output);
            writeLine("", output);
            writeEntry(section.getComment(), output);
        }

        double groupEarned = 0.0;
        double outOf = 0.0;

        for (Subsection subsection : section.getSubsections()) {
            groupEarned += subsection.getEarned();
            outOf += subsection.getOutOf();
        }
        
        printSectionTotal(groupEarned, outOf, output);
    }
    
    private static void writeSubsection(Subsection subsection, BufferedWriter output) throws GradingSheetException {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, subsection.getName(), output);
        printEnd(subsection.getEarned(), subsection.getOutOf(), output);
        
        for (String detail : subsection.getDetails()) {
            writeDetail(detail, output);
        }
    }
    
    private static void writeFinalScore(Group group, Score totalScore, BufferedWriter output) throws GradingSheetException {
        
        printStrongDivider(output);
        write("FINAL GRADE", output);
        printSpaces(TOTAL_TEXT_WIDTH - 12, output);
        writeLine("|", output);
        printStrongDivider(output);

        printWithinBounds(0, SECTION_TEXT_WIDTH, "Final Score: ", output);
        printEnd(totalScore._earned, totalScore._outOf, output);
        printDivider(output);
    }
    
    private static void closeFile(BufferedWriter output) throws GradingSheetException {
        try {
            output.close();
        }
        catch (IOException e) {
            throw new GradingSheetException("The GRD file could not be closed.", e);
        }
    }
    
    private static void printDashes(int numDashes, BufferedWriter output) throws GradingSheetException {
        for (int i = 0; i < numDashes; i++)
        {
            write("-", output);
        }
    }
    
    private static void printDivider(BufferedWriter output) throws GradingSheetException {
        printDashes(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }
    
    private static void printEquals(int numEquals, BufferedWriter output) throws GradingSheetException {
        for (int i = 0; i < numEquals; i++) {
            write("=", output);
        }
    }
    
    private static void printEnd(double earned, double outOf, BufferedWriter output) throws GradingSheetException {
        printEnd(Allocator.getGeneralUtilities().doubleToString(earned),
                 Allocator.getGeneralUtilities().doubleToString(outOf),
                 output);
    }
    
    private static void printEnd(String score, String outOf, BufferedWriter output) throws GradingSheetException {
        if (score == null || outOf == null) {
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
        }
        else {
            writeLine(formatScore(score) + formatScore(outOf) + "|", output);
        }
    }
    
    private static String formatScore(String score) {
        int len = score.length();
        String toReturn = "| " + score;
        int trailingSpaces = SCORE_TEXT_WIDTH - len - 1;
        //Offset 1 back if begins with + or -
        if(score.startsWith("+") || score.startsWith("-")) {
            toReturn = "|" + score;
            trailingSpaces++;
        }
        for (int i = 0; i < trailingSpaces; i++) {
            toReturn += " ";
        }
        return toReturn;
    }
    
    private static void writeDetail(String detail, BufferedWriter output) throws GradingSheetException {
        printWithinBounds(DETAIL_INDENT_WIDTH, SECTION_TEXT_WIDTH, detail, output);
        printEnd(null, null, output);
        writeLine("", output);
    }
    
    private static Score writeScores(InMemoryGML gml, BufferedWriter output) throws GradingSheetException {
        
        // Total score & outof
        double totalScore = 0.0;
        double totalOutOf = 0.0;
        for (Section section : gml.getSections()) {
            for (Subsection sub : section.getSubsections()) {
                totalScore += sub.getEarned();
                totalOutOf += sub.getOutOf();
            }
        }

        printPartTotal(totalScore, totalOutOf, output);
        return new Score(totalScore, totalOutOf);
    }
    
    private static void writeGradableEventScore(GradableEvent event, Group group, Score totalScore, BufferedWriter output) throws GradingSheetException {

        DeadlineInfo info;
        try {
            info = Allocator.getDataServicesV5().getDeadlineInfo(event);
        } catch (ServicesException ex) {
            throw new GradingSheetException("Could not get early bonus / late penalty for " +
                                         "group " + group + " on gradable event " + event.getName() + ".", ex);
        }
        
        DeadlineResolution res;
        if (event.hasDigitalHandins()) {
            File handin;
            try {
                handin = event.getDigitalHandin(group);
            }
            catch(IOException e) {
                throw new GradingSheetException("Could not retrieve handin file for " + "group " + group
                        + " on assignment " + event.getName() + ".", e);
            }
            res = info.apply(new DateTime(handin.lastModified()), null, null);
        }
        else {
            HandinTime time;
            try {
               time = Allocator.getDataServicesV5().getHandinTime(event, group); 
            } catch (ServicesException ex) {
                throw new GradingSheetException("Could not get handin status for " +
                                          "group " + group + " on assignment " + event.getName() + ".", ex);
            }
            res = info.apply(time.getHandinTime(), null, null);
        }
        
        double penalty = res.getPenaltyOrBonus(totalScore._earned);

        printStrongDivider(output);

        printWithinBounds(0, SECTION_TEXT_WIDTH, "Parts Total: ", output);
        printEnd(totalScore._earned, totalScore._outOf, output);
        printDivider(output);

        //Status
        writeStatusPoints(res, penalty, output);

        totalScore._earned += penalty;
        printWithinBounds(0, SECTION_TEXT_WIDTH, event.getName() + " " + SCORE, output);
        printEnd(totalScore._earned, totalScore._outOf, output);
        printDivider(output);
        
        writeLine("", output);
    }
    
    private static void writeStatusPoints(DeadlineResolution res, double handinPenalty, BufferedWriter output)
            throws GradingSheetException {
        String msg = "";
        if (res.getTimeStatus() == null) {
            msg += UNKNOWN;
        }
        else {
            msg += res.getTimeStatus().toString();
        }

        //Build message along with value
        if(handinPenalty > 0) {
            msg += " Bonus (+" + handinPenalty + " points)";
        }
        else if(handinPenalty < 0) {
            msg += " Penalty (" + handinPenalty + " points)";
        }

        printWithinBounds(0, SECTION_TEXT_WIDTH, DEADLINE_RES_LBL + msg, output);
        printEndPlusMinus(handinPenalty, handinPenalty, output);
        printDivider(output);
    }
    
    private static void printEndPlusMinus(double score, double outOf, BufferedWriter output) throws GradingSheetException {
        if (score > 0) {
            printEnd("+" + Allocator.getGeneralUtilities().doubleToString(score), "+" + Allocator.getGeneralUtilities().doubleToString(outOf), output);
        }
        else if (score < 0) {
            printEnd(Allocator.getGeneralUtilities().doubleToString(score), Allocator.getGeneralUtilities().doubleToString(outOf), output);
        }
        else {
            if (outOf < 0) {
                printEnd(Allocator.getGeneralUtilities().doubleToString(score), Allocator.getGeneralUtilities().doubleToString(outOf), output);
            }
            else {
                printEnd(Allocator.getGeneralUtilities().doubleToString(score), "+" + Allocator.getGeneralUtilities().doubleToString(outOf), output);
            }
        }
    }
    
    private static void printPartTotal(double earned, double outOf, BufferedWriter output) throws GradingSheetException {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Part Total:", output);
        printEnd(Allocator.getGeneralUtilities().doubleToString(earned), Allocator.getGeneralUtilities().doubleToString(outOf), output);
    }
    
    private static void printSectionTotal(double score, double outOf, BufferedWriter output) throws GradingSheetException {
        printSpaces(SECTION_TEXT_WIDTH, output);
        printEnd(null, null, output);
        writeLine("", output);
        printWithinBounds(TOTAL_INDENT_WIDTH, SECTION_TEXT_WIDTH, "Total", output);
        printEnd(score, outOf, output);
    }
    
    private static void printSpaces(int numSpaces, BufferedWriter output) throws GradingSheetException {
        for (int i = 0; i < numSpaces; i++) {
            write(" ", output);
        }
    }
    
    private static void printStrongDivider(BufferedWriter output) throws GradingSheetException {
        printEquals(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }
    
    private static void printWithinBounds(int charsSoFar, int charsPerLine, String message, BufferedWriter output) throws GradingSheetException {
        //Split by word
        String[] words = message.split(" ");


        String prefix = "";
        for (int i = 0; i < charsSoFar; i++) {
            prefix += " ";
        }
        String currentLine = prefix;

        for (String word : words) {
            //If the amount of characters is less than charsPerLine
            if (charsSoFar + word.length() < charsPerLine) {
                currentLine += word + " ";
                charsSoFar += word.length() + 1;
            }
            //Otherwise print the line and go to the next one
            else {
                write(currentLine, output);
                printSpaces(charsPerLine - charsSoFar, output);
                printEnd(null, null, output);
                writeLine("", output);

                //Indent to the length defined by the prefix
                currentLine = "";
                charsSoFar = 0;
                for (int i = 0; i < prefix.length(); i++) {
                    currentLine += " ";
                    charsSoFar++;
                }
                currentLine += word + " ";
                charsSoFar += word.length() + 1;
            }
        }
        //Print the remainder
        while (charsSoFar < charsPerLine) {
            currentLine += " ";
            charsSoFar += 1;
        }
        write(currentLine, output);
    }
    
    private static void write(String s, BufferedWriter output) throws GradingSheetException {
        try {
            output.write(s);
        }
        catch (IOException e) {
            throw new GradingSheetException("String \"" + s + "\" could not be written.", e);
        }
    }
    
    private static void writeEntry(String entry, BufferedWriter output) throws GradingSheetException {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, entry, output);
        printEnd(null, null, output);
        writeLine("", output);
    }
    
    private static void writeLine(String s, BufferedWriter output) throws GradingSheetException {
        try {
            output.write(s + '\n');
        }
        catch (IOException e) {
            throw new GradingSheetException("Line \"" + s + "\" could not be written.", e);
        }
    }
    
    private static class Score {
        private double _earned;
        private double _outOf;

        public Score(double earned, double outOf) {
            _earned = earned;
            _outOf = outOf;
        }   
    }
    
}
