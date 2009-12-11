package frontend.grader.rubric;

import utils.ConfigurationManager;
import utils.Project;
import utils.ProjectManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.HashMap;
import utils.Constants;
import utils.Utils;
import backend.DatabaseIO;
import utils.ErrorView;

public class RubricManager {

    /**
     * Converts XML to a Rubric.
     *
     * @param XMLFilePath
     * @return
     */
    public static Rubric processXML(String XMLFilePath) {
        Rubric rubric = new Rubric();

        //Get XML as a document
        Document document = getDocument(XMLFilePath);

        //Get root node
        Node rubricNode = getRootNode(document);

        //Root atrributes
        assignRootAttributes(rubricNode, rubric);

        //Children
        assignChildrenAttributes(rubricNode, rubric);

        return rubric;
    }

    private static Document getDocument(String XMLFilePath) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLFilePath));
        } catch (Exception e) {
            throw new Error("Could not create document from XML file. Be sure path specified is accurate.  Path is " + XMLFilePath);
        }

        return document;
    }

    private static Node getRootNode(Document document) {
        Node rubricNode = document.getDocumentElement();
        if (!rubricNode.getNodeName().equals("RUBRIC")) {
            throw new Error("XML not formatted properly, encountered node of name = " + rubricNode.getNodeName());
        }

        return rubricNode;
    }

    private static void assignRootAttributes(Node rubricNode, Rubric rubric) {
        NamedNodeMap rubricMap = rubricNode.getAttributes();
        rubric.Name = rubricMap.getNamedItem("NAME").getNodeValue();
        rubric.Number = Integer.valueOf(rubricMap.getNamedItem("NUMBER").getNodeValue());
        rubric.Status = rubricMap.getNamedItem("STATUS").getNodeValue();
    }

    private static void assignChildrenAttributes(Node rubricNode, Rubric rubric) {
        NodeList rubricList = rubricNode.getChildNodes();
        for (int i = 0; i < rubricList.getLength(); i++) {
            Node currNode = rubricList.item(i);
            //Skip if an empty text node
            if (currNode.getNodeName().equals("#text") || currNode.getNodeName().equals("#comment")) {
                continue;
            } //Student
            else if (currNode.getNodeName().equals("STUDENT")) {
                assignStudentAttributes(currNode.getAttributes(), rubric);
            } //Grader
            else if (currNode.getNodeName().equals("GRADER")) {
                assignGraderAttributes(currNode.getAttributes(), rubric);
            } //Section
            else if (currNode.getNodeName().equals("SECTION")) {
                //Create section
                Section section = new Section();
                rubric.Sections.add(section);
                //Get name
                NamedNodeMap sectionMap = currNode.getAttributes();
                section.Name = sectionMap.getNamedItem("NAME").getNodeValue();
                //Read section parts
                assignSectionAttributes(currNode.getChildNodes(), section);
            } //Extra credit
            else if (currNode.getNodeName().equals("EXTRACREDIT")) {
                //Assign properties
                NamedNodeMap extraMap = currNode.getAttributes();
                ExtraCredit extraCredit = new ExtraCredit();
                rubric.ExtraCredit.add(extraCredit);
                extraCredit.Text = extraMap.getNamedItem("TEXT").getNodeValue();
                extraCredit.Score = Double.valueOf(extraMap.getNamedItem("SCORE").getNodeValue());
                extraCredit.OutOf = Double.valueOf(extraMap.getNamedItem("OUTOF").getNodeValue());
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + currNode.getNodeName());
            }
        }
    }

    private static void assignStudentAttributes(NamedNodeMap map, Rubric rubric) {
        rubric.Student.Name = map.getNamedItem("NAME").getNodeValue();
        rubric.Student.Acct = map.getNamedItem("ACCT").getNodeValue();
    }

    private static void assignGraderAttributes(NamedNodeMap map, Rubric rubric) {
        rubric.Grader.Name = map.getNamedItem("NAME").getNodeValue();
        rubric.Grader.Acct = map.getNamedItem("ACCT").getNodeValue();
        rubric.Grader.Hours = map.getNamedItem("HOURS").getNodeValue();
    }

    private static void assignSectionAttributes(NodeList subsectionList, Section section) {
        for (int i = 0; i < subsectionList.getLength(); i++) {
            Node subsectionNode = subsectionList.item(i);
            if (subsectionNode.getNodeName().equals("#text") || subsectionNode.getNodeName().equals("#comment")) {
                continue;
            } else if (subsectionNode.getNodeName().equals("SUBSECTION")) {
                //Create subsection
                Subsection subsection = new Subsection();
                section.Subsections.add(subsection);

                //Add properties
                NamedNodeMap subsectionMap = subsectionNode.getAttributes();
                subsection.Name = subsectionMap.getNamedItem("NAME").getNodeValue();
                subsection.Score = Double.valueOf(subsectionMap.getNamedItem("SCORE").getNodeValue());
                subsection.OutOf = Double.valueOf(subsectionMap.getNamedItem("OUTOF").getNodeValue());

                //Add details
                assignDetailAttributes(subsectionNode.getChildNodes(), subsection);
            } else if (subsectionNode.getNodeName().equals("NOTES")) {
                assignNotesDetails(subsectionNode.getChildNodes(), section);
            } else if (subsectionNode.getNodeName().equals("COMMENTS")) {
                assignCommentsDetails(subsectionNode.getChildNodes(), section);
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + subsectionNode.getNodeName());
            }
        }
    }

    private static void assignDetailAttributes(NodeList detailsList, Subsection subsection) {
        for (int i = 0; i < detailsList.getLength(); i++) {
            Node detailNode = detailsList.item(i);
            if (detailNode.getNodeName().equals("#text") || detailNode.getNodeName().equals("#comment")) {
                continue;
            } else if (detailNode.getNodeName().equals("DETAILS")) {
                //Create new detail
                Detail detail = new Detail();
                subsection.Details.add(detail);

                //Add attributes
                NamedNodeMap detailMap = detailNode.getAttributes();
                detail.Name = detailMap.getNamedItem("NAME").getNodeValue();
                detail.Value = Double.valueOf(detailMap.getNamedItem("VALUE").getNodeValue());
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + detailNode.getNodeName());
            }
        }
    }

    private static void assignNotesDetails(NodeList notesList, Section section) {
        for (int i = 0; i < notesList.getLength(); i++) {
            Node note = notesList.item(i);
            if (note.getNodeName().equals("#text") || note.getNodeName().equals("#comment")) {
                continue;
            } else if (note.getNodeName().equals("ENTRY")) {
                //Create entry
                Entry entry = new Entry();
                section.Notes.add(entry);

                //Add text
                NamedNodeMap entryMap = note.getAttributes();
                entry.Text = entryMap.getNamedItem("TEXT").getNodeValue();
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + note.getNodeName());
            }
        }
    }

    private static void assignCommentsDetails(NodeList commentsList, Section section) {
        for (int i = 0; i < commentsList.getLength(); i++) {
            Node comment = commentsList.item(i);
            if (comment.getNodeName().equals("#text") || comment.getNodeName().equals("#comment")) {
                continue;
            } else if (comment.getNodeName().equals("ENTRY")) {
                //Create entry
                Entry entry = new Entry();
                section.Comments.add(entry);

                //Add text
                NamedNodeMap entryMap = comment.getAttributes();
                entry.Text = entryMap.getNamedItem("TEXT").getNodeValue();
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + comment.getNodeName());
            }
        }
    }

    /**
     * Takes the template XML file and copies it into the directory for the grader.
     * Fills in the student login and name, grader login and name, time status,
     * and design check score.
     *
     * @param prj
     * @param studentAcct
     * @param graderAcct
     * @param designCheckScore
     * @param minutesOfLeniency for determining if the assignment is early, on time, late, or nc late
     */
    public static void assignXMLToGrader(Project prj, String studentAcct, String graderAcct,
            double designCheckScore, int minutesOfLeniency) {
        String XMLTemplateFilePath = Constants.COURSE_DIR + "asgn/" + prj.getName() + "/" + Constants.TEMPLATE_GRADE_SHEET_DIR + Constants.TEMPLATE_GRADE_SHEET_FILENAME;

        String XMLGraderFilePath = Constants.GRADER_PATH + graderAcct + "/" + prj.getName() + "/" + studentAcct + ".xml";

        TimeStatus status = ProjectManager.getTimeStatus(studentAcct, prj, minutesOfLeniency);

        assignXMLToGrader(XMLTemplateFilePath, XMLGraderFilePath, status, studentAcct, graderAcct, designCheckScore);
    }

    public static void reassignXML(Project prj, String oldGraderAcct, String studentAcct, String newGraderAcct) {

        String XMLOriginalGraderPath = Constants.GRADER_PATH + oldGraderAcct + "/" + prj.getName() + "/" + studentAcct + ".xml";
        String XMLNewGraderPath = Constants.GRADER_PATH + newGraderAcct + "/" + prj.getName() + "/" + studentAcct + ".xml";

        reassignXML(XMLOriginalGraderPath, XMLNewGraderPath, newGraderAcct);
    }

    private static void reassignXML(String XMLOriginalGraderPath, String XMLNewGraderPath, String newGraderAcct) {
        //Get rubric from the template
        Rubric rubric = processXML(XMLOriginalGraderPath);

        rubric.Grader.Name = Utils.getUserName(newGraderAcct);
        rubric.Grader.Acct = newGraderAcct;

        //Write to XML
        System.out.println("from: " + XMLOriginalGraderPath);
        System.out.println("to: " + XMLNewGraderPath);
        writeToXML(rubric, XMLNewGraderPath);
    }

    /**
     * Uses a template XML file and puts in the initial information and
     * writes a new XML file to the location specified by XMLGraderFilePath.
     *
     * @param XMLTemplateFilePath - path to the template XML file
     * @param XMLGraderFilePath - path for the file to be created
     * @param status - the time status of the handin
     * @param studentAcct - student's account
     * @param graderAcct - grader's account
     * @param designCheckScore
     */
    private static void assignXMLToGrader(String XMLTemplateFilePath, String XMLGraderFilePath,
            TimeStatus status, String studentAcct, String graderAcct,
            double designCheckScore) {
        //Get rubric from the template
        Rubric rubric = processXML(XMLTemplateFilePath);

        //Assign attributes based on what was passed in
        rubric.Status = status.toString();

        rubric.Student.Name = Utils.getUserName(studentAcct);
        rubric.Student.Acct = studentAcct;

        rubric.Grader.Name = Utils.getUserName(graderAcct);
        rubric.Grader.Acct = graderAcct;

        //Find the design check attribute if applicable, and store value
        for (Section section : rubric.Sections) {
            for (Subsection subsection : section.Subsections) {
                if (subsection.Name.equals("Design Check")) {
                    subsection.Score = designCheckScore;
                }
            }
        }

        //Write to XML
        writeToXML(rubric, XMLGraderFilePath);
    }

    /**
     * Writes a Rubric to XML.
     *
     * @param rubric
     * @param XMLFilePath
     */
    public static void writeToXML(Rubric rubric, String XMLFilePath) {
        Document document = createXMLDocument();

        //Create the root node of the XML document
        Element rubricNode = document.createElement("RUBRIC");

        //Set name, number and status attributes
        rubricNode.setAttribute("NAME", rubric.Name);
        rubricNode.setAttribute("NUMBER", Integer.toString(rubric.Number));
        rubricNode.setAttribute("STATUS", rubric.Status);

        //Create the student node of the XML document
        Element student = document.createElement("STUDENT");
        student.setAttribute("NAME", rubric.Student.Name);
        student.setAttribute("ACCT", rubric.Student.Acct);

        //Add the student node to the root node
        rubricNode.appendChild(student);

        //Create the grader node and set attributes
        Element grader = document.createElement("GRADER");
        grader.setAttribute("NAME", rubric.Grader.Name);
        grader.setAttribute("ACCT", rubric.Grader.Acct);
        grader.setAttribute("HOURS", rubric.Grader.Hours);

        //Add the grader node to the root node
        rubricNode.appendChild(grader);

        for (Section section : rubric.Sections) {
            Element sectionNode = document.createElement("SECTION");
            sectionNode.setAttribute("NAME", section.Name);

            for (Subsection subsection : section.Subsections) {
                Element subsectionNode = document.createElement("SUBSECTION");
                subsectionNode.setAttribute("NAME", subsection.Name);
                subsectionNode.setAttribute("SCORE", Double.toString(subsection.Score));
                subsectionNode.setAttribute("OUTOF", Double.toString(subsection.OutOf));

                for (Detail detail : subsection.Details) {
                    Element detailNode = document.createElement("DETAILS");
                    detailNode.setAttribute("NAME", detail.Name);
                    detailNode.setAttribute("VALUE", Double.toString(detail.Value));

                    subsectionNode.appendChild(detailNode);
                }

                sectionNode.appendChild(subsectionNode);
            }

            //Add notes to the XML document
            Element noteNode = document.createElement("NOTES");
            for (Entry note : section.Notes) {
                Element entryNode = document.createElement("ENTRY");
                entryNode.setAttribute("TEXT", note.Text);

                noteNode.appendChild(entryNode);
            }
            sectionNode.appendChild(noteNode);

            //Add comments to the XML document
            Element commentNode = document.createElement("COMMENTS");
            for (Entry comment : section.Comments) {
                Element entryNode = document.createElement("ENTRY");
                entryNode.setAttribute("TEXT", comment.Text);

                commentNode.appendChild(entryNode);
            }
            sectionNode.appendChild(commentNode);

            rubricNode.appendChild(sectionNode);
        }

        for (ExtraCredit extraCreditNode : rubric.ExtraCredit) {
            //Create the extra credit node and set attributes
            Element extraCredit = document.createElement("EXTRACREDIT");
            extraCredit.setAttribute("TEXT", extraCreditNode.Text);
            extraCredit.setAttribute("SCORE", Double.toString(extraCreditNode.Score));
            extraCredit.setAttribute("OUTOF", Double.toString(extraCreditNode.OutOf));

            //Add the extra credit node to the root node
            rubricNode.appendChild(extraCredit);
        }

        document.appendChild(rubricNode);

        saveXMLFile(document, XMLFilePath);
    }

    private static Document createXMLDocument() {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        } catch (Exception e) {
            throw new Error("Exception thrown in creating document.");
        }
        return document;
    }

    private static void saveXMLFile(Document document, String XMLFilePath) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(XMLFilePath));
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new Error("Exception thrown in saving document.");
        }
    }
    /**
     * Writes a Rubric to GRD.
     *
     * @param rubric
     * @param GRDFilePath
     */
    private final static String STUDENTS_ACCOUNT_LBL = "STUDENT's ACCT: ";
    private final static String STUDENTS_NAME_LBL = "STUDENT's NAME: ";
    private final static String GRADERS_ACCOUNT_LBL = "GRADER'S ACCT: ";
    private final static String GRADERS_NAME_LBL = "GRADER'S NAME: ";
    private final static String GRADERS_HOURS_LBL = "GRADER'S HOURS: ";
    private final static int SECTION_TEXT_WIDTH = 58;
    private final static int SCORE_TEXT_WIDTH = 7;
    private final static int NUM_DIVIDERS = 3;
    private final static int TOTAL_TEXT_WIDTH = SECTION_TEXT_WIDTH + (SCORE_TEXT_WIDTH * 2) + NUM_DIVIDERS;
    private final static int SECTION_INDENT_WIDTH = 8;
    private final static int DETAIL_INDENT_WIDTH = 12;
    private final static int TOTAL_INDENT_WIDTH = 49;

    public static void writeToGRD(Rubric rubric, String GRDFilePath) {
        //Get the writer to write to file
        BufferedWriter output = openGRDFile(GRDFilePath);

        writeAssignmentDetails(rubric, output);

        for (int i = 0; i < rubric.Sections.size(); i++) {
            writeSection(rubric.Sections.get(i), output);
        }

        writeScores(rubric, output);
    }
    private final static int STATIC_TEXT_WIDTH = 28;

    private static void writeAssignmentDetails(Rubric rubric, BufferedWriter output) {
        //For centering the first line
        int emptySpace = TOTAL_TEXT_WIDTH - STATIC_TEXT_WIDTH - rubric.Name.length();
        printSpaces(emptySpace / 2, output);

        writeLine("Assignment " + rubric.Number + " Grader Sheet : " + rubric.Name + '\n' + '\n', output);
        writeLine("HANDIN STATUS: " + TimeStatus.getStatus(rubric.Status).getPrettyPrintName() + '\n', output);
        writeLine(STUDENTS_ACCOUNT_LBL + rubric.Student.Acct + '\n', output);
        writeLine(STUDENTS_NAME_LBL + rubric.Student.Name + '\n', output);
        writeLine(GRADERS_NAME_LBL + rubric.Grader.Name + '\n', output);
        writeLine(GRADERS_ACCOUNT_LBL + rubric.Grader.Acct + '\n', output);
        //writeLine(GRADERS_HOURS_LBL + rubric.Grader.Hours + '\n', output);
        printHeader(output);
    }

    private static void writeSection(Section section, BufferedWriter output) {
        printWithinBounds(0, SECTION_TEXT_WIDTH, section.Name, output);
        printEnd(null, null, output);
        writeLine("", output);
        for (int i = 0; i < section.Subsections.size(); i++) {
            writeSubsection(section.Subsections.get(i), output);
        }

        //Print notes
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Notes:", output);
        printEnd(null, null, output);
        writeLine("", output);
        for (int i = 0; i < section.Notes.size(); i++) {
            writeEntry(section.Notes.get(i), output);
        }

        //Print comments
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Comments:", output);
        printEnd(null, null, output);
        writeLine("", output);
        for (int i = 0; i < section.Comments.size(); i++) {
            writeEntry(section.Comments.get(i), output);
        }

        double studentScore = 0.0;
        double availScore = 0.0;

        for (Subsection subsection : section.Subsections) {
            studentScore += subsection.Score;
            availScore += subsection.OutOf;
        }
        printSectionTotal(studentScore, availScore, output);
    }

    private static void writeSubsection(Subsection subsection, BufferedWriter output) {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, subsection.Name, output);
        printEnd(Double.toString(subsection.Score), Double.toString(subsection.OutOf), output);
        for (int i = 0; i < subsection.Details.size(); i++) {
            writeDetail(subsection.Details.get(i), output);
        }
    }

    private static void writeDetail(Detail detail, BufferedWriter output)
    {
        String msg;
        if(detail.Value == 0)
        {
            msg = detail.Name;
        }
        else
        {
            msg = detail.Name + " (" + detail.Value + " points)";
        }
        printWithinBounds(DETAIL_INDENT_WIDTH, SECTION_TEXT_WIDTH, msg, output);
        printEnd(null, null, output);
        writeLine("", output);
    }

    private static void writeEntry(Entry note, BufferedWriter output) {
        printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, note.Text, output);
        printEnd(null, null, output);
        writeLine("", output);
    }

    private static void writeScores(Rubric rubric, BufferedWriter output) {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Total Points", output);

        double studentScore = 0.0;
        double availScore = 0.0;

        for (Section section : rubric.Sections) {
            for (Subsection subsection : section.Subsections) {
                studentScore += subsection.Score;
                availScore += subsection.OutOf;
            }
        }

        String stringScore = Utils.doubleToString(studentScore);
        String stringOutOf = Utils.doubleToString(availScore);
        printEnd(stringScore, stringOutOf, output);
        printDivider(output);

        studentScore += writeStatusPoints(TimeStatus.getStatus(rubric.Status), availScore, output);
        studentScore += writeExtraCredit(rubric, output);

        if (TimeStatus.getStatus(rubric.Status).getPrettyPrintName().equals("NC Late")) {
            studentScore = 0.0;
        }

        printTotal(studentScore, availScore, output);

        closeFile(output);

        System.out.println("File  written");
    }

    private static void printTotal(double studentScore, double availScore, BufferedWriter output) {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Final Grade", output);
        printEnd(Utils.doubleToString(studentScore), Utils.doubleToString(availScore), output);
        printWithinBounds(0, SECTION_TEXT_WIDTH, "", output);
        printEnd(null, null, output);
        writeLine("", output);
        printDivider(output);
    }

    private static void closeFile(BufferedWriter output) {
        try {
            output.close();
        } catch (IOException e) {
            new ErrorView(e);
        }
    }

    private static double writeStatusPoints(TimeStatus status, double availScore, BufferedWriter output) {
        //If early
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Early Bonus (+4% if early)", output);
        printEndPlusMinus(status.getEarlyBonus(availScore), status.getEarlyOutOf(availScore), output);
        printDivider(output);

        //If late
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Late Penalty (-8% if late)", output);
        printEndPlusMinus(status.getLatePenalty(availScore), status.getLateOutOf(availScore), output);
        printDivider(output);

        //Return number of points added/subtracted
        return status.getLatePenalty(availScore) + status.getEarlyBonus(availScore);
    }

    private static double writeExtraCredit(Rubric rubric, BufferedWriter output) {
        printWithinBounds(0, SECTION_TEXT_WIDTH, "Bells & Whistles", output);
        printEnd(null, null, output);
        writeLine("", output);
        double totalExtra = 0;
        for (ExtraCredit extraCreditNode : rubric.ExtraCredit) {
            printWithinBounds(SECTION_INDENT_WIDTH, SECTION_TEXT_WIDTH, extraCreditNode.Text, output);
            printEndPlusMinus(extraCreditNode.Score, extraCreditNode.OutOf, output);
            totalExtra += extraCreditNode.Score;
        }
        printDivider(output);

        //Return number of extra credit points
        return totalExtra;
    }

    private static BufferedWriter openGRDFile(String GRDFilePath) {
        File grdFile = new File(GRDFilePath);
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(grdFile));
        } catch (IOException e) {
            new ErrorView(e);
        }
        return output;
    }

    private static void writeLine(String s, BufferedWriter output) {
        try {
            output.write(s + '\n');
        } catch (IOException e) {
            new ErrorView(e);
        }
    }

    private static void write(String s, BufferedWriter output) {
        try {
            output.write(s);
        } catch (IOException e) {
            new ErrorView(e);
        }
    }

    private static void printWithinBounds(int charsSoFar, int charsPerLine, String message, BufferedWriter output) {
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
            } //Otherwise print the line and go to the next one
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

    private static void printDivider(BufferedWriter output) {
        printDashes(TOTAL_TEXT_WIDTH, output);
        writeLine("", output);
    }

    private static void printSpaces(int numSpaces, BufferedWriter output) {
        for (int i = 0; i < numSpaces; i++) {
            write(" ", output);
        }
    }

    private static void printDashes(int numDashes, BufferedWriter output) {
        for (int i = 0; i < numDashes; i++) {
            write("-", output);
        }
    }

    private static void printHeader(BufferedWriter output) {
        printSpaces(SECTION_TEXT_WIDTH - 1, output);
        write("YOUR", output);
        printSpaces(SCORE_TEXT_WIDTH - 2, output);
        writeLine("OUT", output);
        write("SECTION", output);
        printSpaces(SECTION_TEXT_WIDTH - 8, output);
        write("SCORE", output);
        printSpaces(SCORE_TEXT_WIDTH - 3, output);
        writeLine("OF", output);
        printDivider(output);
    }

    private static String formatScore(String score) {
        int len = score.length();
        String toReturn = "| " + score;
        int trailingSpaces = SCORE_TEXT_WIDTH - len - 1;
        for (int i = 0; i < trailingSpaces; i++) {
            toReturn += " ";
        }
        return toReturn;
    }

    private static void printEnd(String score, String outOf, BufferedWriter output) {
        if (score == null || outOf == null) {
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
            printSpaces(SCORE_TEXT_WIDTH, output);
            write("|", output);
        } else {
            writeLine(formatScore(score) + formatScore(outOf) + "|", output);
        }
    }

    private static void printEndPlusMinus(double score, double outOf, BufferedWriter output) {
        if (score > 0) {
            printEnd("+" + Utils.doubleToString(score), "+" + Utils.doubleToString(outOf), output);
        } else if (score < 0) {
            printEnd(Utils.doubleToString(score), Utils.doubleToString(outOf), output);
        } else {
            if (outOf < 0) {
                printEnd(Utils.doubleToString(score), Utils.doubleToString(outOf), output);
            } else {
                printEnd(Utils.doubleToString(score), "+" + Utils.doubleToString(outOf), output);
            }
        }
    }

    private static void printSectionTotal(double score, double outOf, BufferedWriter output) {
        String stringScore = Utils.doubleToString(score);
        String stringOutOf = Utils.doubleToString(outOf);
        printSpaces(SECTION_TEXT_WIDTH, output);
        printEnd(null, null, output);
        writeLine("", output);
        printWithinBounds(TOTAL_INDENT_WIDTH, SECTION_TEXT_WIDTH, "Total", output);
        printEnd(stringScore, stringOutOf, output);
        printDivider(output);
    }

    /**
     * Converts an XML file to GRD.
     *
     * @param XMLFilePath
     * @param GRDFilePath
     */
    public static void convertToGRD(String XMLFilePath, String GRDFilePath) {
        Rubric rubric = processXML(XMLFilePath);
        writeToGRD(rubric, GRDFilePath);
    }

    /**
     * Converts an XML file to GRD.
     *
     * @param asgn Assignment name
     * @param graderAcct The grader's login
     * @param studentAcct The student's login
     */
    public static void convertToGRD(String asgn, String graderAcct, String studentAcct) {
        String path = Constants.GRADER_PATH + graderAcct + "/" + asgn + "/" + studentAcct;
        String XMLFilePath = path + ".xml";
        String GRDFilePath = path + ".grd";

        convertToGRD(XMLFilePath, GRDFilePath);
    }

    /**
     * Converts all of a grader's XML files to GRD.
     *
     * @param asgn Assignment name
     * @param graderAcct The grader's login
     */
    public static void convertAllToGrd(String asgn, String graderAcct) {
        String dirPath =  Constants.GRADER_PATH + graderAcct + "/" + asgn + "/";

        Collection<File> xmlFiles = Utils.getFiles(dirPath, "xml");

        for (File file : xmlFiles) {
            String XMLFilePath = file.getAbsolutePath();
            String GRDFilePath = XMLFilePath.split("\\.")[0] + ".grd";
            if(file.getAbsolutePath().contains(".metadata")) continue;
            //@TODO: THIS IS A COMPLETE HACK FIX THIS!!!! - psastras
            System.out.print("Processing: " + file.getAbsolutePath() + ". ");
            convertToGRD(XMLFilePath, GRDFilePath);
        }
    }

    public static HashMap<String, Double> getAllScores(String asgn) {
        HashMap<String, Double> scoresTable = new HashMap<String, Double>();
        for (String g : ConfigurationManager.getGraderLogins()) {
            for (String s : DatabaseIO.getStudentsToGrade(g, asgn)) {
                scoresTable.put(s, getTotalSubmittedScore(asgn, g, s));
            }
        }
        return scoresTable;
    }

    public static double getTotalSubmittedScore(String asgn, String graderAcct, String studentAcct) {
        String path = Constants.GRADER_SUBMIT_PATH + asgn + "/" + graderAcct + "/" + studentAcct;
        String XMLFilePath = path + ".xml";
        System.out.println("XMLFilePath is " + XMLFilePath);
        Rubric rubric = processXML(XMLFilePath);
        return rubric.getTotalScore();
    }

    //shouldn't need this method, but not deleting it yet just in case
    public static double getTotalScore(String asgn, String graderAcct, String studentAcct) {
        String path = Constants.GRADER_PATH + graderAcct + "/" + asgn + "/" + studentAcct;
        String XMLFilePath = path + ".xml";
        Rubric rubric = processXML(XMLFilePath);
        return rubric.getTotalScore();
    }
}