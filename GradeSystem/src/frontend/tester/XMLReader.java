/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.tester;

import java.io.File;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Project;

/**
 *
 * @author spoletto
 */
public class XMLReader {

    public static TestResults readXML(String asgnName, String studentAcct)
    {
        TestResults results = new TestResults();
        results.Tests = new Vector<Test>();
        final String XMLFilePath = utils.ProjectManager.getStudentSpecificDirectory(Project.getInstance(asgnName), studentAcct) + "testResults.xml";
        Document document = getDocument(XMLFilePath);
        Node testerNode = getRootNode(document);
        assignRootAttributes(testerNode, results);
        assignChildrenAttributes(testerNode, results);
        return results;
    }

    private static Document getDocument(String XMLFilePath) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLFilePath));
        } catch (Exception e) {
            throw new Error("Could not create document from XML file. Be sure path specified is accurate.");
        }

        return document;
    }

    private static Node getRootNode(Document document) {
        Node rubricNode = document.getDocumentElement();
        if (!rubricNode.getNodeName().equals("TESTER")) {
            throw new Error("XML not formatted properly, encountered node of name = " + rubricNode.getNodeName());
        }

        return rubricNode;
    }

    private static void assignRootAttributes(Node testerNode, TestResults results) {
        NamedNodeMap testerMap = testerNode.getAttributes();
        results.Assignment = testerMap.getNamedItem("PROJECT").getNodeValue();
        results.StudentAcct = testerMap.getNamedItem("STUDENT").getNodeValue();
    }

    private static void assignChildrenAttributes(Node testerNode, TestResults results) {
        NodeList rubricList = testerNode.getChildNodes();
        for (int i = 0; i < rubricList.getLength(); i++) {
            Node currNode = rubricList.item(i);
            //Skip if an empty text node
            if (currNode.getNodeName().equals("#text") || currNode.getNodeName().equals("#comment")) {
                continue;
            }
            else if (currNode.getNodeName().equals("TEST")) {
                NamedNodeMap map = currNode.getAttributes();
                Test toAdd = new Test();
                toAdd.Name = map.getNamedItem("NAME").getNodeValue();
                toAdd.Status = map.getNamedItem("STATUS").getNodeValue();
                toAdd.Details = map.getNamedItem("DETAILS").getNodeValue();
                results.Tests.add(toAdd);
            }

        }
    }

}
