package rubric;

import config.Assignment;
import config.HandinPart;
import java.io.File;

import java.util.Collection;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import rubric.Rubric.Detail;
import rubric.Rubric.Person;
import rubric.Rubric.Section;
import rubric.Rubric.Subsection;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Allocator;

/**
 *
 * @author jak2
 */
class RubricXMLParser implements RubricConstants
{
    /**
     * Test main.
     * 
     * @param args
     * @throws RubricException
     */
    public static void main(String[] args) throws RubricException
    {
        //Hack for testing to get the Adventure assignment
        Assignment asgn = null;
        for(Assignment a : Allocator.getCourseInfo().getHandinAssignments())
        {
            if(a.getName().equals("Cartoon"))
            {
                asgn = a;
            }
        }

        Rubric rubric = parse("/Users/nonother/Desktop/gml_test.gml", asgn.getHandinPart());

        RubricGRDWriter.write(rubric, "/Users/nonother/Desktop/grd_test.grd");
        
        GradingVisualizer.testView(rubric, "/Users/nonother/Desktop/gml_test.gml");
        //new PreviewVisualizer(rubric);
    }

    /**
     * Converts XML to a Rubric.
     *
     * @param XMLFilePath
     * @return
     */
    static Rubric parse(String XMLFilePath, HandinPart part) throws RubricException
    {
        Rubric rubric = new Rubric(part);

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

    private static Document getDocument(String XMLFilePath) throws RubricException
    {
        //Check if file exists
        File file = new File(XMLFilePath);
        if(!file.exists())
        {
            throw new RubricException("Rubric could not be read, location specified: " + XMLFilePath);
        }

        Document document = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file);
        }
        catch (Exception e)
        {
            throw new RubricException("Exception thrown during parsing, " + XMLFilePath + " is illegally formatted. \n" + e.getMessage());
        }

        return document;
    }


    /**
     * Indicates whether a node should be skipped or not. A node should not be
     * processed if it is a text node or a comment node.
     *
     * @param node
     * @return whether a node should be skipped
     */
    private static boolean skipNode(Node node)
    {
        return (node.getNodeName().equals(TEXT_NODE) || node.getNodeName().equals(COMMENT_NODE));
    }

    private static Node getRootNode(Document document) throws RubricException
    {
        Node rubricNode = document.getDocumentElement();
        if (!rubricNode.getNodeName().equals(RUBRIC))
        {
            throw new RubricException("Expected root node " + RUBRIC + ", encountered: " + rubricNode.getNodeName());
        }

        return rubricNode;
    }

    private static void assignRootAttributes(Node rubricNode, Rubric rubric) throws RubricException
    {
        //Default a time status if not provided
        rubric.setStatus(TimeStatus.ON_TIME);

        //Attributes
        NamedNodeMap rubricMap = rubricNode.getAttributes();
        for(int i = 0; i < rubricMap.getLength(); i++)
        {
            Node attrNode = rubricMap.item(i);

            if(skipNode(attrNode))
            {
                continue;
            }
            else if(attrNode.getNodeName().equals(NAME))
            {
                rubric.setName(attrNode.getNodeValue());
            }
            else if(attrNode.getNodeName().equals(NUMBER))
            {
                rubric.setNumber(Integer.valueOf(attrNode.getNodeValue()));
            }
            else if(attrNode.getNodeName().equals(STATUS))
            {
                rubric.setStatus(TimeStatus.getStatus(attrNode.getNodeValue()));
            }
            else if(attrNode.getNodeName().equals(DAYS_LATE))
            {
                rubric.setDaysLate(Integer.valueOf(attrNode.getNodeValue()));
            }
            else
            {
                throw new RubricException("Unsupported attribute node: " + attrNode.getNodeName() + ", only " +
                                          " [" + NAME + ", " + NUMBER + ", " + STATUS + ", " + DAYS_LATE + "]" +
                                          " are supported.");
            }
        }
    }

    private static void assignChildrenAttributes(Node rubricNode, Rubric rubric) throws RubricException
    {
        NodeList rubricList = rubricNode.getChildNodes();
        for (int i = 0; i < rubricList.getLength(); i++)
        {
            Node currNode = rubricList.item(i);
            //Skip if necessary
            if(skipNode(currNode))
            {
                continue;
            }
            //Student
            else if (currNode.getNodeName().equals(STUDENT))
            {
                rubric.setStudent(parsePerson(currNode.getAttributes()));
            }
            //Grader
            else if (currNode.getNodeName().equals(GRADER))
            {
                rubric.setGrader(parsePerson(currNode.getAttributes()));
            }
            //Section
            else if (currNode.getNodeName().equals(SECTION))
            {
                rubric.addSection(parseSection(currNode));
            }
            //Extra credit
            else if (currNode.getNodeName().equals(EXTRA_CREDIT))
            {
                rubric.setExtraCredit(parseSection(currNode));
            }
            else
            {
                throw new RubricException(RUBRIC, currNode, STUDENT, GRADER, SECTION, EXTRA_CREDIT);
            }
        }
    }
    
    private static Person parsePerson(NamedNodeMap map)
    {
        Person person = new Person();

        for (int i = 0; i < map.getLength(); i++)
        {
            Node node = map.item(i);
            if (skipNode(node))
            {
                continue;
            }
            else if (node.getNodeName().equals(NAME))
            {
                person.setName(node.getNodeValue());
            }
            else if (node.getNodeName().equals(ACCT))
            {
                person.setAccount(node.getNodeValue());
            }
            else
            {

            }
        }
        
        return person;
    }

    private static Section parseSection(Node sectionNode) throws RubricException
    {
        Section section = new Section();

        //Parse name if it exists (if EXTRA-CREDIT it won't have a name)
        Node nameNode = sectionNode.getAttributes().getNamedItem(NAME);
        if(nameNode != null)
        {
            section.setName(nameNode.getNodeValue());
        }

        NodeList subsectionList = sectionNode.getChildNodes();
        for (int i = 0; i < subsectionList.getLength(); i++)
        {
            Node subsectionNode = subsectionList.item(i);
            if (skipNode(subsectionNode))
            {
                continue;
            }
            else if (subsectionNode.getNodeName().equals(SUBSECTION))
            {
                section.addSubsection(parseSubsection(subsectionNode));
            }
            else if (subsectionNode.getNodeName().equals(NOTES))
            {
                section.setNotes(parseNotesComments(subsectionNode));
            }
            else if (subsectionNode.getNodeName().equals(COMMENTS))
            {
                section.setComments(parseNotesComments(subsectionNode));
            }
            else
            {
                throw new RubricException(SECTION, subsectionNode, SUBSECTION, NOTES, COMMENTS);
            }
        }

        return section;
    }

    private static Collection<String> parseNotesComments(Node node) throws RubricException
    {
        Vector<String> entries = new Vector<String>();

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node entry = nodeList.item(i);
            if (skipNode(entry))
            {
                continue;
            }
            else if (entry.getNodeName().equals(ENTRY))
            {
                entries.add(entry.getAttributes().getNamedItem(TEXT).getNodeValue());
            }
            else
            {
                throw new RubricException(COMMENTS, entry, ENTRY);
            }
        }

        return entries;
    }

    private static Subsection parseSubsection(Node subsectionNode) throws RubricException
    {
        Subsection subsection = new Subsection();

        //Subsection attributes
        NamedNodeMap attrMap = subsectionNode.getAttributes();
        for(int i = 0; i < attrMap.getLength(); i++)
        {
            Node attrNode = attrMap.item(i);

            if(skipNode(attrNode))
            {
                continue;
            }
            else if (attrNode.getNodeName().equals(NAME))
            {
                subsection.setName(attrNode.getNodeValue());
            }
            else if (attrNode.getNodeName().equals(SCORE))
            {
                subsection.setScore(Double.valueOf(attrNode.getNodeValue()));
            }
            else if (attrNode.getNodeName().equals(OUTOF))
            {
                subsection.setOutOf(Double.valueOf(attrNode.getNodeValue()));
            }
            else if (attrNode.getNodeName().equals(SOURCE))
            {
                subsection.setSource(attrNode.getNodeValue());
            }
            else
            {
                
            }
        }

        //Detail children (if they exist)

        NodeList nodeList = subsectionNode.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            Node detailNode = nodeList.item(i);

            if(skipNode(detailNode))
            {
                continue;
            }
            else if (detailNode.getNodeName().equals(DETAIL))
            {
                subsection.addDetail(processDetail(detailNode));
            }
            else
            {
                
            }
        }

        return subsection;
    }

    private static Detail processDetail(Node detailNode)
    {
        Detail detail = new Detail();

        //Subsection attributes
        NamedNodeMap attrMap = detailNode.getAttributes();
        for(int i = 0; i < attrMap.getLength(); i++)
        {
            Node attrNode = attrMap.item(i);

            if(skipNode(attrNode))
            {
                continue;
            }
            else if (attrNode.getNodeName().equals(NAME))
            {
                detail.setName(attrNode.getNodeValue());
            }
            else if (attrNode.getNodeName().equals(VALUE))
            {
                detail.setValue(Double.parseDouble(attrNode.getNodeValue()));
            }
            else
            {

            }
        }

        return detail;
    }
}