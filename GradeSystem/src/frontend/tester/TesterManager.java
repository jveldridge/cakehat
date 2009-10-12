
package frontend.tester;

import java.io.File;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utils.Project;

/**
 *
 * @author spoletto
 */
public class TesterManager
{

    private Vector<Test> _completedTests;
    private String _asgnName, _studentAcct;

    public TesterManager(String asgnName, String studentAcct)
    {
        _completedTests = new Vector<Test>();
        _asgnName = asgnName;
        _studentAcct = studentAcct;
    }

    public void testComplete(String testName, String status, String details)
    {
        Test testToAdd = new Test();
        testToAdd.Name = testName;
        testToAdd.Status = status;
        testToAdd.Details = details;
        _completedTests.add(testToAdd);
    }

    public void writeToXML() {
        final String XMLFilePath = utils.ProjectManager.getCodeDirectory(Project.getInstance(_asgnName)) + "testResults.xml";

        Document document = createXMLDocument();

        //Create the root node of the XML document
        Element rubricNode = document.createElement("TESTER");

        //Set name, number and status attributes
        rubricNode.setAttribute("PROJECT", _asgnName);
        rubricNode.setAttribute("STUDENT", _studentAcct);

        for (Test test : _completedTests) {
            Element testNode = document.createElement("TEST");
            testNode.setAttribute("NAME", test.Name);
            testNode.setAttribute("STATUS", test.Status);
            testNode.setAttribute("DETAILS", test.Details);
            rubricNode.appendChild(testNode);
        }
        
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
}
