package gradesystem.handin;

import utils.AlphabeticFileComparator;
import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.handin.file.AndFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * TODO: Come up with a better name for this class / namespace. It likely
 * makes sense to also have PDF viewing ability housed here as well as a mode
 * that just opens as much as it possibly can. Or potentially have two different
 * namespaces, one continue to be "editors" and have the other one be "viewers"
 * and "viewers" can contain a mode that just tries to view everything, which
 * could include opening files in kate.
 * 
 * @author jak2
 */
class EditorActions implements ActionProvider
{
    public String getNamespace()
    {
        return "editors";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        ArrayList<DistributableActionDescription> descriptions = new
                ArrayList<DistributableActionDescription>();

        descriptions.add(new KateAction());

        return descriptions;
    }

    private class KateAction implements DistributableActionDescription
    {
        private final DistributableActionProperty EXTENSIONS_PROPERTY =
            new DistributableActionProperty("extensions",
            "The extensions of the files in this distributable part that will be opened. " +
            "To open files that do not have file extensions use an underscore. " +
            "Regardless of extension, the files must be plain text files. " +
            "List extensions in the following format (without quotation marks): \n" +
            "single extension - 'java' \n" +
            "multiple extensions - 'cpp, h'", true);

        public ActionProvider getProvider()
        {
            return EditorActions.this;
        }

        public String getName()
        {
            return "kate";
        }

        public String getDescription()
        {
            return "Opens plain text files in kate.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[] { EXTENSIONS_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.OPEN });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.OPEN, ActionMode.RUN, ActionMode.TEST });
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    //Get files to open
                    FileFilter inclusionFilter = part.getInclusionFilter(group);
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                    FileFilter extensionsFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
                    FileFilter combinedFilter = new AndFileFilter(inclusionFilter, extensionsFilter);
                    List<File> filesToOpen =
                            Allocator.getFileSystemUtilities().getFiles(unarchiveDir,
                            combinedFilter, new AlphabeticFileComparator());

                    try
                    {
                        Allocator.getExternalProcessesUtilities().kate(filesToOpen);
                    }
                    catch(IOException e)
                    {
                        throw new ActionException("Unable to open " +
                                group.getName() + "'s handin in kate", e);
                    }
                }
            };

            return action;
        }
    }
}