package gradesystem.rubric;

import gradesystem.Allocator;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import gradesystem.rubric.Rubric.Detail;
import gradesystem.rubric.Rubric.Section;
import gradesystem.rubric.Rubric.Subsection;
import gradesystem.services.ServicesException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import gradesystem.views.shared.ErrorView;
import javax.xml.transform.TransformerException;

/**
 *
 * @author spoletto
 * @author jak2
 */
class RubricGMLWriter implements RubricConstants
{
    /**
     * Writes a Rubric to GML.
     *
     * @param rubric
     * @param gmlFile the file that the XML will be written to
     */
    static void write(Rubric rubric, File gmlFile) throws RubricException
    {
        Document document = createXMLDocument();

        //Create the root node of the XML document
        Element rubricNode = document.createElement(RUBRIC);

        for (Section section : rubric.getSections())
        {
            Element sectionNode = document.createElement(SECTION);
            sectionNode.setAttribute(NAME, section.getName());

            for (Subsection subsection : section.getSubsections())
            {
                Element subsectionNode = document.createElement(SUBSECTION);
                subsectionNode.setAttribute(NAME, subsection.getName());
                subsectionNode.setAttribute(SCORE, Double.toString(subsection.getScore()));
                subsectionNode.setAttribute(OUTOF, Double.toString(subsection.getOutOf()));
                if(subsection.hasSource())
                {
                    subsectionNode.setAttribute(SOURCE, subsection.getSource());
                }

                for (Detail detail : subsection.getDetails())
                {
                    Element detailNode = document.createElement(DETAIL);
                    detailNode.setAttribute(NAME, detail.getName());
                    detailNode.setAttribute(VALUE, Double.toString(detail.getValue()));

                    subsectionNode.appendChild(detailNode);
                }

                sectionNode.appendChild(subsectionNode);
            }

            //Add notes to the XML document
            if(section.hasNotes())
            {
                Element noteNode = document.createElement(NOTES);
                for (String note : section.getNotes())
                {
                    Element entryNode = document.createElement(ENTRY);
                    entryNode.setAttribute(TEXT, note);

                    noteNode.appendChild(entryNode);
                }
                sectionNode.appendChild(noteNode);
            }

            //Add comments to the XML document
            if(section.hasComments())
            {
                Element commentNode = document.createElement(COMMENTS);
                for (String comment : section.getComments())
                {
                    Element entryNode = document.createElement(ENTRY);
                    entryNode.setAttribute(TEXT, comment);

                    commentNode.appendChild(entryNode);
                }
                sectionNode.appendChild(commentNode);
            }

            rubricNode.appendChild(sectionNode);
        }

        //Extra credit
        Element extraCreditNode = document.createElement(EXTRA_CREDIT);

        for (Subsection subsection : rubric.getExtraCredit().getSubsections())
        {
            Element subsectionNode = document.createElement(SUBSECTION);
            subsectionNode.setAttribute(NAME, subsection.getName());
            subsectionNode.setAttribute(SCORE, Double.toString(subsection.getScore()));
            subsectionNode.setAttribute(OUTOF, Double.toString(subsection.getOutOf()));
            if(subsection.hasSource())
            {
                subsectionNode.setAttribute(SOURCE, subsection.getSource());
            }

            for (Detail detail : subsection.getDetails())
            {
                Element detailNode = document.createElement(DETAIL);
                detailNode.setAttribute(NAME, detail.getName());
                detailNode.setAttribute(VALUE, Double.toString(detail.getValue()));

                subsectionNode.appendChild(detailNode);
            }

            extraCreditNode.appendChild(subsectionNode);
        }
        //Add notes to extra credit node
        if(rubric.getExtraCredit().hasNotes())
        {
            Element noteNode = document.createElement(NOTES);
            for (String note : rubric.getExtraCredit().getNotes())
            {
                Element entryNode = document.createElement(ENTRY);
                entryNode.setAttribute(TEXT, note);

                noteNode.appendChild(entryNode);
            }
            extraCreditNode.appendChild(noteNode);
        }

        //Add comments to extra credit node
        if(rubric.getExtraCredit().hasComments())
        {
            Element commentNode = document.createElement(COMMENTS);
            for (String comment : rubric.getExtraCredit().getComments())
            {
                Element entryNode = document.createElement(ENTRY);
                entryNode.setAttribute(TEXT, comment);

                commentNode.appendChild(entryNode);
            }
            extraCreditNode.appendChild(commentNode);
        }

        rubricNode.appendChild(extraCreditNode);

        //Append rubric to document
        document.appendChild(rubricNode);

        saveXMLFile(document, gmlFile);
    }

    private static Document createXMLDocument()
    {
        Document document = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }
        catch (Exception e)
        {
            new ErrorView(e);
        }
        return document;
    }

    private static void saveXMLFile(Document document, File gmlFile) throws RubricException
    {
        // dom source and transformer
        DOMSource source = new DOMSource(document);
        Transformer transformer;
        try
        {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        catch (TransformerConfigurationException e)
        {
            throw new RubricException("Unable to save rubric (gml file).\n" +
                    "Rubric location: " + gmlFile.getAbsolutePath(), e);
        }

        // properties
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // ensure directory where the file is going to be saved exists
        try
        {
            Allocator.getFileSystemServices().makeDirectory(gmlFile.getParentFile());
        }
        catch(ServicesException e)
        {
            throw new RubricException("Unable to make directory to save rubric (gml file) in. \n" +
                    "Rubric location is: " + gmlFile.getAbsolutePath(), e);
        }

        // write file
        StreamResult result = new StreamResult(gmlFile);
        try
        {
            transformer.transform(source, result);
        }
        catch(TransformerException e)
        {
            throw new RubricException("Unable to save rubric (gml file).\n" +
                    "Rubric location: " + gmlFile.getAbsolutePath(), e);
        }

        // ensure the file written has the correct permissions and group
        try
        {
            Allocator.getFileSystemServices().sanitize(gmlFile);
        }
        catch(ServicesException e)
        {
            throw new RubricException("Unable to set correct permissions and " +
                    "group for rubric (gml file).", e);
        }
    }   
}