package cakehat.rubric;

import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import java.io.File;
import java.util.Collection;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import cakehat.rubric.Rubric.Detail;
import cakehat.rubric.Rubric.Section;
import cakehat.rubric.Rubric.Subsection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses out a GML file and constructs a Rubric.
 *
 * @author spoletto
 * @author jak2
 */
@Deprecated
class RubricGMLParser implements RubricConstants
{
    /**
     * Converts GML to a Rubric.
     *
     * @param gmlFile the GML file
     * @param part the DistributablePart associated with this GML file
     * @param the Group associated with this GML file; null if the file is a template
     * @return
     */
    static Rubric parse(File gmlFile, DistributablePart part, Group group) throws RubricException
    {
        Rubric rubric = new Rubric(part, group);

        //Get XML as a document
        Document document = getDocument(gmlFile);

        //Get root node
        Node rubricNode = getRootNode(document);

        //Children
        assignChildrenAttributes(rubricNode, rubric);

        return rubric;
    }

    private static Document getDocument(File gmlFile) throws RubricException
    {
        //Check if file exists
        if(!gmlFile.exists())
        {
            throw new RubricException("Rubric could not be read, location specified: " + gmlFile.getAbsolutePath());
        }

        Document document = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(gmlFile);
        }
        catch (Exception e)
        {
            throw new RubricGMLException("Exception thrown during parsing, " +
                    gmlFile.getAbsolutePath() + " is illegally formatted.", e);
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

    private static Node getRootNode(Document document) throws RubricGMLException
    {
        Node rubricNode = document.getDocumentElement();
        if (!rubricNode.getNodeName().equals(RUBRIC))
        {
            throw new RubricGMLException("Expected root node " + RUBRIC + ", encountered: " + rubricNode.getNodeName());
        }

        return rubricNode;
    }

    private static void assignChildrenAttributes(Node rubricNode, Rubric rubric) throws RubricGMLException, RubricException
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
            //Section
            else if (currNode.getNodeName().equals(SECTION))
            {
                parseSection(currNode, rubric, false);
            }
            //Extra credit
            else if (currNode.getNodeName().equals(EXTRA_CREDIT))
            {
                parseSection(currNode, rubric, true);
            }
            else
            {
                throw new RubricGMLException(RUBRIC, currNode, SECTION, EXTRA_CREDIT);
            }
        }
    }

    private static void parseSection(Node sectionNode, Rubric rubric, boolean isEC) throws RubricGMLException, RubricException
    {
        Section section;
        
        if(isEC)
        {
            section = rubric.addExtraCredit();
        }
        else
        {
            section = rubric.addSection();
        }

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
                parseSubsection(subsectionNode, section);
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
                throw new RubricGMLException(SECTION, subsectionNode, SUBSECTION, NOTES, COMMENTS);
            }
        }
    }

    private static Collection<String> parseNotesComments(Node node) throws RubricGMLException
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
                throw new RubricGMLException(COMMENTS, entry, ENTRY);
            }
        }

        return entries;
    }

    private static void parseSubsection(Node subsectionNode, Section section) throws RubricGMLException, RubricException
    {
        //Subsection attributes
        String name = "", source = "";
        double outof = 0, score = 0;

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
                name = attrNode.getNodeValue();
                //subsection.setName(attrNode.getNodeValue());
            }
            else if (attrNode.getNodeName().equals(SCORE))
            {
                score = Double.valueOf(attrNode.getNodeValue());
                //subsection.setScore(Double.valueOf(attrNode.getNodeValue()));
            }
            else if (attrNode.getNodeName().equals(OUTOF))
            {
                outof = Double.valueOf(attrNode.getNodeValue());
                //subsection.setOutOf(Double.valueOf(attrNode.getNodeValue()));
            }
            else if (attrNode.getNodeName().equals(SOURCE))
            {
                source = attrNode.getNodeValue();
                //subsection.setSource(attrNode.getNodeValue());
            }
            else
            {
                
            }
        }

        //Add subsections
        Subsection subsection = section.addSubsection(name, score, outof, source);

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