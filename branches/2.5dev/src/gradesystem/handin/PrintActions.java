package gradesystem.handin;

import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.handin.file.AndFileFilter;
import gradesystem.printing.PrintRequest;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Actions that print plain text files.
 *
 * TODO: Consider PDF printing support. Would any course actually use this?
 *
 * @author jak2
 */
class PrintActions implements ActionProvider
{
    public String getNamespace()
    {
        return "printing";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();
        descriptions.add(new Landscape());

        return descriptions;
    }

    private class Landscape implements DistributableActionDescription
    {
        private final DistributableActionProperty EXTENSIONS_PROPERTY =
            new DistributableActionProperty("extensions",
            "The extensions of the files in this distributable part that will be printed. " +
            "To open files that do not have file extensions use an underscore. " +
            "Regardless of extension, the files must be plain text files. " +
            "List extensions in the following format (without quotation marks): \n" +
            "single extension - 'java' \n" +
            "multiple extensions - 'cpp, h'", true);

        public ActionProvider getProvider()
        {
            return PrintActions.this;
        }

        public String getName()
        {
            return "landscape";
        }

        public String getDescription()
        {
            return "Prints plain text files in a space-saving landscape " +
                    "orientation with two columns.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[] { EXTENSIONS_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.PRINT });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.PRINT,
                ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN });
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new DistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    performAction(part, Arrays.asList(new Group[] { group }));
                }

                public void performAction(DistributablePart part, Collection<Group> groups) throws ActionException
                {
                    String printerName = Allocator.getGradingServices().getPrinter();
                    
                    //Build print requests
                    if(printerName != null)
                    {
                        ArrayList<PrintRequest> requests = new ArrayList<PrintRequest>();

                        for(Group group : groups)
                        {
                            FileFilter inclusionFilter = part.getInclusionFilter(group);
                            File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                            FileFilter extensionsFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
                            FileFilter combinedFilter = new AndFileFilter(inclusionFilter, extensionsFilter);
                            Collection<File> filesToPrint = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, combinedFilter);

                            try
                            {
                                PrintRequest request = new PrintRequest(filesToPrint,
                                        Allocator.getUserUtilities().getUserLogin(),
                                        group.getName());
                                requests.add(request);
                            }
                            catch (FileNotFoundException e)
                            {
                                throw new ActionException("Unable to generate " +
                                        "print request for group: " + group.getName(), e);
                            }
                        }

                        //Issue print command
                        try
                        {
                            Allocator.getLandscapePrinter().print(requests, printerName);
                        }
                        catch (IOException e)
                        {
                            throw new ActionException("Unable to issue print command", e);
                        }
                    }
                }
            };

            return action;
        }
    }
}