package gradesystem.config;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import static gradesystem.config.XMLParsingUtilities.*;
import static gradesystem.config.ConfigurationParserHelper.*;

/**
 * A very lightweight parser that is used to just get a {@link #LabPart}.
 *
 * @author jak2
 */
public class LabConfigurationParser
{
    public static LabPart getLabPart(final int labNumber) throws ConfigurationException
    {
        Node root = getConfigurationRoot();

        Map<String, Node> rootChildren = getUniqueChildren(root,
                new String[] { ASSIGNMENTS },
                new String[] { DEFAULTS, EMAIL, ASSIGNMENTS, TAS });

        Map<String, List<Node>> assignments = getChildren(rootChildren.get(ASSIGNMENTS),
                new String[] { ASSIGNMENT } );

        for(Node asgnNode : assignments.get(ASSIGNMENT))
        {
            //Get all of the labs for the assignment
            Map<String, List<Node>> asgnChildren =
                getChildren(asgnNode, new String[] { LAB, NON_HANDIN, HANDIN });

            for(Node labNode : asgnChildren.get(LAB))
            {
                AttributeMap labAttrs = getAttributes(labNode,
                    new String[] { NAME, POINTS, NUMBER, LAB_NUMBER },
                    new String[] { NAME, POINTS, NUMBER, LAB_NUMBER });

                //If this lab is the lab we are looking for
                if(labAttrs.getInt(LAB_NUMBER) == labNumber)
                {
                    //Get information the assignment needs
                    AttributeMap asgnAttrs = getAttributes(asgnNode,
                            new String[] { NAME, NUMBER }, new String[] { NAME, NUMBER });
                    Assignment asgn = new Assignment(asgnAttrs.getString(NAME), asgnAttrs.getInt(NUMBER));

                    //Build the lab
                    LabPart part = new LabPart(asgn,
                            labAttrs.getString(NAME), labAttrs.getInt(NUMBER),
                            labAttrs.getInt(POINTS), labAttrs.getInt(LAB_NUMBER));

                    return part;
                }
            }
        }

        return null;
    }
}