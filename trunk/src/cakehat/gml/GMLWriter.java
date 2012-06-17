package cakehat.gml;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import cakehat.Allocator;
import cakehat.services.ServicesException;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.gml.InMemoryGML.Section;
import org.w3c.dom.Element;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import static cakehat.gml.GMLConstants.*;

/**
 *
 * @author Hannah
 */
public class GMLWriter {
    
    private GMLWriter() { }
    
    public static void write(InMemoryGML gml, File gmlFile) throws GradingSheetException {
        Document document = createXMLDocument();
        
        //Create the root node of the XML document
        Element gmlNode = document.createElement(GRADING_SHEET);
        gmlNode.setAttribute(GML_VERSION, gml.getVersion());
        gmlNode.setAttribute(TYPE, gml.getType());
        
        for (Section section : gml.getSections()) {
            Element sectionNode = document.createElement(SECTION);
            sectionNode.setAttribute(NAME, section.getName());
            
            for (Subsection subsection : section.getSubsections()) {
                Element subsectionNode = document.createElement(SUBSECTION);
                subsectionNode.setAttribute(NAME, subsection.getName());
                subsectionNode.setAttribute(EARNED, Double.toString(subsection.getEarned()));
                subsectionNode.setAttribute(OUTOF, Double.toString(subsection.getOutOf()));
                
                for (String detail : subsection.getDetails()) {
                    Element detailNode = document.createElement(DETAIL);
                    detailNode.setTextContent(detail);
                    
                    subsectionNode.appendChild(detailNode);
                }
                
                sectionNode.appendChild(subsectionNode);
            }
            
            String comment = section.getComment();
            if (comment != null) {
                Element commentNode = document.createElement(COMMENTS);
                commentNode.setTextContent(comment);        
                sectionNode.appendChild(commentNode);
            }
            
            gmlNode.appendChild(sectionNode);
        }
        
        document.appendChild(gmlNode);
        
        saveXMLFile(document, gmlFile);
    }
    
    private static Document createXMLDocument() throws GradingSheetException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return builder.newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new GradingSheetException("Unable to create XML document", e);

        }
    }
    
    private static void saveXMLFile(Document document, File gmlFile) throws GradingSheetException {
        // determine if we are creating the file or overwriting it
        boolean willCreateFile = !gmlFile.exists();

        // dom source and transformer
        DOMSource source = new DOMSource(document);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new GradingSheetException("Unable to save GML file.\nGML location: " + gmlFile.getAbsolutePath(), e);
        }

        // properties
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // ensure directory where the file is going to be saved exists
        try {
            Allocator.getFileSystemServices().makeDirectory(gmlFile.getParentFile());
        }
        catch(ServicesException e) {
            throw new GradingSheetException("Unable to make directory to save GML file in.\n" +
                    "GML location is: " + gmlFile.getAbsolutePath(), e);
        }

        // write file
        StreamResult result = new StreamResult(gmlFile);
        try {
            transformer.transform(source, result);
        }
        catch(TransformerException e) {
            throw new GradingSheetException("Unable to save GML file.\n" +
                    "GML location: " + gmlFile.getAbsolutePath(), e);
        }

        // if the file was just created by this user, then ensure it has the correct permissions and group
        if(willCreateFile) {
            try {
                Allocator.getFileSystemServices().sanitize(gmlFile);
            }
            catch(ServicesException e) {
                throw new GradingSheetException("Unable to set correct permissions and group for GML file.", e);
            }
        }
    }
}