package gradesystem.rubric;

import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.config.GradeUnits;
import gradesystem.config.LatePolicy;
import gradesystem.config.TA;
import gradesystem.database.CakeHatDBIOException;
import gradesystem.database.Group;
import gradesystem.database.HandinStatus;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import gradesystem.rubric.Rubric.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * Responsible for writing a Rubric instance to a GRD text file.
 *
 * @author spoletto
 * @author jak2
 */
class RubricGRDWriter {
    private final static String GRADER_LBL = "GRADER: ";
    private final static String HANDIN_STATUS_LBL = "HANDIN STATUS: ";
    private final static String ASSIGNMENT_LBL = "Assignment ";
    private final static String GRADER_SHEET_LBL = " Grader Sheet: ";
    private final static String DP_LABEL = "PART: ";

    private final static int SECTION_TEXT_WIDTH = 58;
    private final static int SCORE_TEXT_WIDTH = 7;
    private final static int NUM_DIVIDERS = 3;
    private final static int TOTAL_TEXT_WIDTH = SECTION_TEXT_WIDTH + (SCORE_TEXT_WIDTH * 2) + NUM_DIVIDERS;
    private final static int SECTION_INDENT_WIDTH = 8;
    private final static int DETAIL_INDENT_WIDTH = 12;
    private final static int TOTAL_INDENT_WIDTH = 49;
    private final static int STATIC_TEXT_WIDTH = 28;

    static void write(Handin handin, Group group, HandinStatus status,
                      File grdFile) throws RubricException {

        //Get the writer to write to file
        BufferedWriter output = openGRDFile(grdFile);

        writeAssignmentDetails(handin, group, output);

        Score totalScore = new Score(0, 0);

        for (DistributablePart dp : handin.getAssignment().getDistributableParts()) {
            Score dpScore = writeDistributablePart(handin, status, group, dp, output);
            totalScore._score += dpScore._score;
            totalScore._outOf += dpScore._outOf;
        }

        writeFinalScore(handin, group, totalScore, output);

        closeFile(output);
    }

    private static void writeAssignmentDetails(Handin handin, Group group,
                                               BufferedWriter output) throws RubricException {
        Assignment asgn = handin.getAssignment();

        //For centering the first line
        int emptySpace = TOTAL_TEXT_WIDTH - STATIC_TEXT_WIDTH - asgn.getName().length();
        printSpaces(emptySpace / 2, output);

        writeLine(ASSIGNMENT_LBL + asgn.getNumber() + GRADER_SHEET_LBL + asgn.getName() + '\n', output);

        Map<String, String> students;
        try {
            students = Allocator.getDatabaseIO().getAllStudents();
        } catch (SQLException ex) {
            throw new RubricException("Could not read student names from database. " +
                                      "Rubrics cannot be generated.", ex);
        }

        Iterator<String> members = group.getMembers().iterator();
        if (group.getMembers().size() == 1) {
            String studentLogin = members.next();
            writeLine("STUDENT: " + students.get(studentLogin) + " (" + studentLogin + ")", output);
        }
        else {
            writeLine("GROUP: " + group.getName(), output);
            String member = members.next();
            writeLine("MEMBERS: " + students.get(member) + " (" + member + ")", output);
            members.remove();
            while (members.hasNext()) {
                member = members.next();
                printSpaces(9, output);
                writeLine(students.get(member) + " (" + member + ")", output);
            }
        }

        writeLine("", output);
    }

    private static Score writeDistributablePart(Handin handin, HandinStatus status,
                                               Group group, DistributablePart dp,
                                               BufferedWriter output) throws RubricException {
        File gmlFile = Allocator.getPathServices().getGroupGMLFile(dp, group);
        Rubric rubric = RubricGMLParser.parse(gmlFile, dp, group);

        printStrongDivider(output);

        String line1 = DP_LABEL + dp.getName();
        write(line1, output);
        printSpaces(SECTION_TEXT_WIDTH - line1.length(), output);
        write("| ", output);
        write("YOUR", output);
        printSpaces(SCORE_TEXT_WIDTH - 5, output);
        write("| ", output);
        write("OUT", output);
        printSpaces(SCORE_TEXT_WIDTH - 4, output);
        writeLine("|", output);

        TA grader;
        try {
            grader = Allocator.getDatabaseIO().getGraderForGroup(dp, group);
        } catch (SQLException ex) {
            throw new RubricException("Could not read grader for group " + group + " on " +
                                      "part " + dp + " of assignment " + handin.getAssignment() + ". ", ex);
        } catch (CakeHatDBIOException ex) {
            throw new RubricException("Could not read grader for group " + group + " on " +
                                      "part " + dp + " of assignment " + handin.getAssignment() + ".", ex);
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

        for (Section section : rubric.getSections()) {
            writeSection(section, output, false);
        }

        return writeScores(handin, group, status, rubric, output);
    }

    private static void writeSection(Section section, BufferedWriter output, boolean isEC) throws RubricGMLException
    {
        String name = section.getName();
        if(isEC)
        {
            name = "Extra Credit";
        }
        printWithinBounds(0, SECTION_TEXT_WIDTH, name, output);
        printEnd(null, null, output);
        writeLine("", output);
        for (Subsection subsection : section.getSubsections())
        {
            writeSubsection(subsection, output, isEC);
        }

        //Print notes
        if(section.hasNotes())
        {
            printSpaces(SECTION_TEXT_WIDTH, output);
            printEnd(null, null, output);
            writeLine("", output);

            printWithinBounds(0, SECTION_TEXT_WIDTH, "Notes:", output);
            printEnd(null, null, output);
            writeLine("", output);
            for (String note : section.getNotes())
            {
                writeEntry(note, output);
            }
        }

        //Print comments
        if(section.hasComments())
        {
            printSpaces(SECTION_TEXT_WIDTH, output);
            printEnd(null, null, output);
            writeLine("", output);

            printWithinBounds(0, SECTION_TEXT_WIDTH, "Comments:", output);
            printEnd(null, null, output);
            writeLine("", output);
            for (String comment : section.getComments())
            {
                writeEntry(comment, output);
            }
        }

        double studentScore = 0.0;
        double availScore = 0.0;

        for (Subsection subsection : section.getSubsections())
        {
            studentScore += subsection.getScore();
            availScore += subsection.getOutOf();
        }
        printSectionTotal(studentScore, availScore, isEC, output);
    }

    private static void writeSubsection(Subsection subsection, BufferedWriter output, boolean isEC) throws RubricGMLException
    {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, subsection.getName(), output);
        if(isEC)
        {
            printEndPlusMinus(subsection.getScore(), subsection.getOutOf(), output);
        }
        else
        {
            printEnd(subsection.getScore(), subsection.getOutOf(), output);
        }

        for (Detail detail : subsection.getDetails())
        {
            writeDetail(detail, output);
        }
    }

    private static void writeDetail(Detail detail, BufferedWriter output) throws RubricGMLException
    {
        String msg;
        if (detail.getValue() == 0)
        {
            msg = detail.getName();
        } else
        {
            msg = detail.getName() + " (" + detail.getValue() + " points)";
        }
        printWithinBounds(DETAIL_INDENT_WIDTH, SECTION_TEXT_WIDTH, msg, output);
        printEnd(null, null, output);
        writeLine("", output);
    }

    private static void writeEntry(String entry, BufferedWriter output) throws RubricGMLException
    {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, entry, output);
        printEnd(null, null, output);
        writeLine("", output);
    }

    private static Score writeScores(Handin handin, Group group, HandinStatus status, Rubric rubric, BufferedWriter output) throws RubricGMLException
    {
        // Total score & outof
        double totalScore = 0.0;
        double totalOutOf = 0.0;
        for (Section section : rubric.getSections()) {
            totalScore += section.getSectionScore();
            totalOutOf += section.getSectionOutOf();
        }

        // Extra Credit
        if(rubric.hasExtraCredit())
        {
            printWithinBounds(0, SECTION_TEXT_WIDTH, "Part Points", output);
            printEnd(totalScore, totalOutOf, output);
            printDivider(output);
        
            writeSection(rubric.getExtraCredit(), output, true);
            totalScore += rubric.getExtraCredit().getSectionScore();
        }

        // Print score after extra credit
        printPartTotal(totalScore, totalOutOf, output);

        return new Score(totalScore, totalOutOf);
    }

    private static void printPartTotal(double studentScore, double availScore, BufferedWriter output) throws RubricGMLException
    {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Part Total:", output);
        printEnd(Allocator.getGeneralUtilities().doubleToString(studentScore), Allocator.getGeneralUtilities().doubleToString(availScore), output);
        printDivider(output);
        writeLine("", output);
    }

    private static void closeFile(BufferedWriter output) throws RubricGMLException
    {
        try
        {
            output.close();
        }
        catch (IOException e)
        {
            throw new RubricGMLException("The GML file could not be closed.", e);
        }
    }

    private static void writeFinalScore(Handin handin, Group group, Score totalScore, BufferedWriter output) throws RubricException {
        HandinStatus status;
        try {
            status = Allocator.getDatabaseIO().getHandinStatus(handin, group);
        } catch (SQLException ex) {
            throw new RubricException("Could not get handin status for " +
                                      "group " + group + " on assignment " + handin.getAssignment() + ".", ex);
        }

        double handinPenalty;
        try {
            handinPenalty = Allocator.getRubricManager().getHandinPenaltyOrBonus(handin, group);
        } catch (RubricException ex) {
            throw new RubricGMLException("Could not get early bonus / late penalty for " +
                                         "group " + group + " on assignment " + handin.getAssignment() + ".", ex);
        }

        printStrongDivider(output);
        write("FINAL GRADE", output);
        printSpaces(TOTAL_TEXT_WIDTH - 12, output);
        writeLine("|", output);
        printStrongDivider(output);

        printWithinBounds(0, SECTION_TEXT_WIDTH, "Parts Total: ", output);
        printEnd(totalScore._score, totalScore._outOf, output);
        printDivider(output);

        //Status
        writeStatusPoints(handin, status, handinPenalty, output);

        totalScore._score += handinPenalty;
        printWithinBounds(0, SECTION_TEXT_WIDTH, "FINAL SCORE: ", output);
        printEnd(totalScore._score, totalScore._outOf, output);
        printDivider(output);
    }

    private static void writeStatusPoints(Handin handin, HandinStatus status,
                                          double handinPenalty, BufferedWriter output) throws RubricGMLException
    {
        String msg = status.getTimeStatus().getPrettyPrintName();

        //If early or late, add more information
        if(status.getTimeStatus() == TimeStatus.EARLY || status.getTimeStatus() == TimeStatus.LATE)
        {
            //Build message along with value
            if(status.getTimeStatus() == TimeStatus.EARLY)
            {
                msg += " Bonus (+" + handin.getTimeInformation().getEarlyValue();
            }
            else if(status.getTimeStatus() == TimeStatus.LATE)
            {
                msg += " Penalty (";
                if(handin.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
                {
                    msg += handin.getTimeInformation().getOntimeValue();
                }
                else if(handin.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES)
                {
                    msg += handin.getTimeInformation().getLateValue();
                }
            }

            //percent or points
            if(handin.getTimeInformation().getGradeUnits() == GradeUnits.PERCENTAGE)
            {
                msg += "%";
            }
            else if(handin.getTimeInformation().getGradeUnits() == GradeUnits.POINTS)
            {
                msg += " points";
            }

            //if daily deduction
            if(handin.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
            {
                msg += " per day";
            }

            // end message
            msg += ")";
        }

        printWithinBounds(0, SECTION_TEXT_WIDTH, "Handin Status: " + msg, output);
        printEndPlusMinus(handinPenalty, handinPenalty, output);
        printDivider(output);
    }

    private static BufferedWriter openGRDFile(File grdFile) throws RubricGMLException
    {
        BufferedWriter output = null;
        try
        {
            output = new BufferedWriter(new FileWriter(grdFile));
        }
        catch (IOException e)
        {
            throw new RubricGMLException("Opening the file to write failed.", e);
        }
        return output;
    }

    private static void writeLine(String s, BufferedWriter output) throws RubricGMLException
    {
        try
        {
            output.write(s + '\n');
        }
        catch (IOException e)
        {
            throw new RubricGMLException("Line \"" + s + "\" could not be written.", e);
        }
    }

    private static void write(String s, BufferedWriter output) throws RubricGMLException
    {
        try
        {
            output.write(s);
        }
        catch (IOException e)
        {
            throw new RubricGMLException("String \"" + s + "\" could not be written.", e);
        }
    }

    private static void printWithinBounds(int charsSoFar, int charsPerLine, String message, BufferedWriter output) throws RubricGMLException
    {
        //Split by word
        String[] words = message.split(" ");


        String prefix = "";
        for (int i = 0; i < charsSoFar; i++)
        {
            prefix += " ";
        }
        String currentLine = prefix;

        for (String word : words)
        {
            //If the amount of characters is less than charsPerLine
            if (charsSoFar + word.length() < charsPerLine)
            {
                currentLine += word + " ";
                charsSoFar += word.length() + 1;
            }
            //Otherwise print the line and go to the next one
            else
            {
                write(currentLine, output);
                printSpaces(charsPerLine - charsSoFar, output);
                printEnd(null, null, output);
                writeLine("", output);

                //Indent to the length defined by the prefix
                currentLine = "";
                charsSoFar = 0;
                for (int i = 0; i < prefix.length(); i++)
                {
                    currentLine += " ";
                    charsSoFar++;
                }
                currentLine += word + " ";
                charsSoFar += word.length() + 1;
            }
        }
        //Print the remainder
        while (charsSoFar < charsPerLine)
        {
            currentLine += " ";
            charsSoFar += 1;
        }
        write(currentLine, output);
    }

    private static void printDivider(BufferedWriter output) throws RubricGMLException
    {
        printDashes(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }

    private static void printStrongDivider(BufferedWriter output) throws RubricGMLException
    {
        printEquals(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }

    private static void printSpaces(int numSpaces, BufferedWriter output) throws RubricGMLException
    {
        for (int i = 0; i < numSpaces; i++)
        {
            write(" ", output);
        }
    }

    private static void printDashes(int numDashes, BufferedWriter output) throws RubricGMLException
    {
        for (int i = 0; i < numDashes; i++)
        {
            write("-", output);
        }
    }

    private static void printEquals(int numEquals, BufferedWriter output) throws RubricGMLException
    {
        for (int i = 0; i < numEquals; i++)
        {
            write("=", output);
        }
    }

    private static String formatScore(String score)
    {
        int len = score.length();
        String toReturn = "| " + score;
        int trailingSpaces = SCORE_TEXT_WIDTH - len - 1;
        //Offset 1 back if begins with + or -
        if(score.startsWith("+") || score.startsWith("-"))
        {
            toReturn = "|" + score;
            trailingSpaces++;
        }
        for (int i = 0; i < trailingSpaces; i++)
        {
            toReturn += " ";
        }
        return toReturn;
    }

    private static void printEnd(double score, double outOf, BufferedWriter output) throws RubricGMLException
    {
        printEnd(Allocator.getGeneralUtilities().doubleToString(score),
                 Allocator.getGeneralUtilities().doubleToString(outOf),
                 output);
    }

    private static void printEnd(String score, String outOf, BufferedWriter output) throws RubricGMLException
    {
        if (score == null || outOf == null)
        {
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
        }
        else
        {
            writeLine(formatScore(score) + formatScore(outOf) + "|", output);
        }
    }

    private static void printEndPlusMinus(double score, double outOf, BufferedWriter output) throws RubricGMLException
    {
        if (score > 0)
        {
            printEnd("+" + Allocator.getGeneralUtilities().doubleToString(score), "+" + Allocator.getGeneralUtilities().doubleToString(outOf), output);
        }
        else if (score < 0)
        {
            printEnd(Allocator.getGeneralUtilities().doubleToString(score), Allocator.getGeneralUtilities().doubleToString(outOf), output);
        }
        else
        {
            if (outOf < 0)
            {
                printEnd(Allocator.getGeneralUtilities().doubleToString(score), Allocator.getGeneralUtilities().doubleToString(outOf), output);
            }
            else
            {
                printEnd(Allocator.getGeneralUtilities().doubleToString(score), "+" + Allocator.getGeneralUtilities().doubleToString(outOf), output);
            }
        }
    }

    private static void printSectionTotal(double score, double outOf, boolean isEC, BufferedWriter output) throws RubricGMLException
    {
        printSpaces(SECTION_TEXT_WIDTH, output);
        printEnd(null, null, output);
        writeLine("", output);
        printWithinBounds(TOTAL_INDENT_WIDTH, SECTION_TEXT_WIDTH, "Total", output);
        if(isEC)
        {
            printEndPlusMinus(score, outOf, output);
        }
        else
        {
            printEnd(score, outOf, output);
        }
        printDivider(output);
    }

    private static class Score {
        private double _score;
        private double _outOf;

        public Score(double score, double outOf) {
            _score = score;
            _outOf = outOf;
        }
        
    }

}