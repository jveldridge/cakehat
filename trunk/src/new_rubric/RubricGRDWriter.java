package new_rubric;

import config.GradeUnits;
import config.LatePolicy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import new_rubric.Rubric.*;
import utils.Allocator;
import utils.ErrorView;

/**
 *
 * @author jak2
 */
class RubricGRDWriter
{
    private final static String STUDENT_LBL = "STUDENT: ";
    private final static String GRADER_LBL = "GRADER: ";
    private final static String HANDIN_STATUS_LBL = "HANDIN STATUS: ";
    private final static String ASSIGNMENT_LBL = "Assignment ";
    private final static String GRADER_SHEET_LBL = " Grader Sheet: ";

    private final static int SECTION_TEXT_WIDTH = 58;
    private final static int SCORE_TEXT_WIDTH = 7;
    private final static int NUM_DIVIDERS = 3;
    private final static int TOTAL_TEXT_WIDTH = SECTION_TEXT_WIDTH + (SCORE_TEXT_WIDTH * 2) + NUM_DIVIDERS;
    private final static int SECTION_INDENT_WIDTH = 8;
    private final static int DETAIL_INDENT_WIDTH = 12;
    private final static int TOTAL_INDENT_WIDTH = 49;
    private final static int STATIC_TEXT_WIDTH = 28;

    static void write(Rubric rubric, String GRDFilePath)
    {
        //Get the writer to write to file
        BufferedWriter output = openGRDFile(GRDFilePath);

        writeAssignmentDetails(rubric, output);

        for (Section section : rubric.getSections())
        {
            writeSection(section, output, false);
        }

        writeScores(rubric, output);
    }

    private static void writeAssignmentDetails(Rubric rubric, BufferedWriter output)
    {
        //For centering the first line
        int emptySpace = TOTAL_TEXT_WIDTH - STATIC_TEXT_WIDTH - rubric.getName().length();
        printSpaces(emptySpace / 2, output);

        writeLine(ASSIGNMENT_LBL + rubric.getNumber() + GRADER_SHEET_LBL + rubric.getName() + '\n', output);
        
        String status = rubric.getStatus().getPrettyPrintName();
        //If daily deduction and late, print the number of days late
        if(rubric.getStatus() == TimeStatus.LATE &&
           rubric.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
        {
            String day = rubric.getDaysLate() == 1 ? "day" : "days";
            status += " (" + rubric.getDaysLate() + " " + day + " late)";
        }
        
        writeLine(HANDIN_STATUS_LBL + status + '\n', output);

        writeLine(STUDENT_LBL + rubric.getStudentName() + " (" + rubric.getStudentAccount() + ")", output);
        writeLine(GRADER_LBL + rubric.getGraderName() + " (" + rubric.getGraderAccount() + ")", output);

        printHeader(output);
    }

    private static void writeSection(Section section, BufferedWriter output, boolean isEC)
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

    private static void writeSubsection(Subsection subsection, BufferedWriter output, boolean isEC)
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

    private static void writeDetail(Detail detail, BufferedWriter output)
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

    private static void writeEntry(String entry, BufferedWriter output)
    {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, entry, output);
        printEnd(null, null, output);
        writeLine("", output);
    }

    private static void writeScores(Rubric rubric, BufferedWriter output)
    {
        // Total score & outof
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Total Points", output);
        double totalScore = 0.0;
        double totalOutOf = 0.0;
        for (Section section : rubric.getSections())
        {
            totalScore += section.getSectionScore();
            totalOutOf += section.getSectionOutOf();
        }
        printEnd(totalScore, totalOutOf, output);
        printDivider(output);

        // Status
        writeStatusPoints(rubric, output);
        totalScore += rubric.getDeduction();

        // Extra Credit
        if(rubric.hasExtraCredit())
        {
            writeSection(rubric.getExtraCredit(), output, true);
            totalScore += rubric.getExtraCredit().getSectionScore();
        }

        // Print score after extra credit and status
        printTotal(totalScore, totalOutOf, output);

        // Close file
        closeFile(output);
    }

    private static void printTotal(double studentScore, double availScore, BufferedWriter output)
    {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Final Grade", output);
        printEnd(Allocator.getGeneralUtilities().doubleToString(studentScore), Allocator.getGeneralUtilities().doubleToString(availScore), output);
        printDivider(output);
    }

    private static void closeFile(BufferedWriter output)
    {
        try
        {
            output.close();
        }
        catch (IOException e)
        {
            new ErrorView(e);
        }
    }

    private static void writeStatusPoints(Rubric rubric, BufferedWriter output)
    {
        if(rubric.getStatus() == TimeStatus.ON_TIME)
        {
            return;
        }

        //NC Late

        String msg = rubric.getStatus().getPrettyPrintName();

        //If early or late, add more information
        if(rubric.getStatus() == TimeStatus.EARLY || rubric.getStatus() == TimeStatus.LATE)
        {
            //Build message along with value
            if(rubric.getStatus() == TimeStatus.EARLY)
            {
                msg += " Bonus (+" + rubric.getTimeInformation().getEarlyValue();
            }
            else if(rubric.getStatus() == TimeStatus.LATE)
            {
                msg += " Penalty (";
                if(rubric.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
                {
                    msg += rubric.getTimeInformation().getOntimeValue();
                }
                else if(rubric.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES)
                {
                    msg += rubric.getTimeInformation().getLateValue();
                }
            }

            //percent or points
            if(rubric.getTimeInformation().getGradeUnits() == GradeUnits.PERCENTAGE)
            {
                msg += "%";
            }
            else if(rubric.getTimeInformation().getGradeUnits() == GradeUnits.POINTS)
            {
                msg += " points";
            }

            //if daily deduction
            if(rubric.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
            {
                msg += " per day";
            }

            // end message
            msg += ")";
        }

        printWithinBounds(0, SECTION_TEXT_WIDTH, msg, output);
        double deduction = rubric.getDeduction();
        printEndPlusMinus(deduction, deduction, output);
        printDivider(output);
    }

    private static BufferedWriter openGRDFile(String GRDFilePath)
    {
        File grdFile = new File(GRDFilePath);
        BufferedWriter output = null;
        try
        {
            output = new BufferedWriter(new FileWriter(grdFile));
        }
        catch (IOException e)
        {
            new ErrorView(e);
        }
        return output;
    }

    private static void writeLine(String s, BufferedWriter output)
    {
        try
        {
            output.write(s + '\n');
        }
        catch (IOException e)
        {
            new ErrorView(e);
        }
    }

    private static void write(String s, BufferedWriter output)
    {
        try
        {
            output.write(s);
        }
        catch (IOException e)
        {
            new ErrorView(e);
        }
    }

    private static void printWithinBounds(int charsSoFar, int charsPerLine, String message, BufferedWriter output)
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

    private static void printDivider(BufferedWriter output)
    {
        printDashes(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }

    private static void printSpaces(int numSpaces, BufferedWriter output)
    {
        for (int i = 0; i < numSpaces; i++)
        {
            write(" ", output);
        }
    }

    private static void printDashes(int numDashes, BufferedWriter output)
    {
        for (int i = 0; i < numDashes; i++)
        {
            write("-", output);
        }
    }

    private static void printHeader(BufferedWriter output)
    {
        printSpaces(SECTION_TEXT_WIDTH + 1, output);
        write("YOUR", output);
        printSpaces(SCORE_TEXT_WIDTH - 3, output);
        writeLine("OUT", output);
        write("SECTION", output);
        printSpaces(SECTION_TEXT_WIDTH - 6, output);
        write("SCORE", output);
        printSpaces(SCORE_TEXT_WIDTH - 4, output);
        writeLine("OF", output);
        printDivider(output);
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

    private static void printEnd(double score, double outOf, BufferedWriter output)
    {
        printEnd(Allocator.getGeneralUtilities().doubleToString(score),
                 Allocator.getGeneralUtilities().doubleToString(outOf),
                 output);
    }

    private static void printEnd(String score, String outOf, BufferedWriter output)
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

    private static void printEndPlusMinus(double score, double outOf, BufferedWriter output)
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

    private static void printSectionTotal(double score, double outOf, boolean isEC, BufferedWriter output)
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
}