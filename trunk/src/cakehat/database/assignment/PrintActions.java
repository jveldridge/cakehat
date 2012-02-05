package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.printing.CITPrinter;
import cakehat.printing.PrintRequest;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Actions that print plain text files.
 *
 * @author jak2
 */
class PrintActions implements ActionProvider
{
    public String getNamespace()
    {
        return "printing";
    }

    public Set<? extends PartActionDescription> getActionDescriptions()
    {
        return ImmutableSet.of(new Landscape());
    }

    private class Landscape extends PartActionDescription
    {
        private final PartActionProperty EXTENSIONS_PROPERTY =
            new PartActionProperty("extensions",
            "The extensions of the files in this part that will be printed.  To open files that do not have file " +
            "extensions use an underscore. Regardless of extension, the files must be plain text files. List " +
            "extensions in the following format (without quotation marks): \n" +
            "single extension - 'java' \n" +
            "multiple extensions - 'cpp, h'", true);

        private Landscape()
        {
            super(PrintActions.this, "landscape");
        }

        public String getDescription()
        {
            return "Prints plain text files in a space-saving landscape orientation with two columns.";
        }

        public Set<PartActionProperty> getProperties()
        {
            return ImmutableSet.of(EXTENSIONS_PROPERTY);
        }

        public Set<ActionType> getSuggestedTypes()
        {
            return ImmutableSet.of(ActionType.PRINT);
        }

        public Set<ActionType> getCompatibleTypes()
        {
            return ImmutableSet.of(ActionType.PRINT, ActionType.RUN, ActionType.TEST, ActionType.OPEN);
        }

        public PartAction getAction(final Map<PartActionProperty, String> properties)
        {
            PartAction action = new PartAction()
            {
                public void performAction(Part part, Group group) throws ActionException
                {
                    performAction(part, Arrays.asList(new Group[] { group }));
                }

                public void performAction(Part part, Collection<Group> groups) throws ActionException
                {
                    CITPrinter printer = Allocator.getGradingServices().getPrinter();
                    
                    //Build print requests
                    if(printer != null)
                    {
                        List<PrintRequest> requests = new ArrayList<PrintRequest>();

                        for(Group group : groups)
                        {
                            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

                            FileFilter extensionsFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));

                            List<File> filesToPrint;
                            try
                            {
                                filesToPrint = Allocator.getFileSystemUtilities()
                                        .getFiles(unarchiveDir, extensionsFilter);
                            }
                            catch(IOException e)
                            {
                                throw new ActionException("Unable to access files to print", e);
                            }

                            try
                            {
                                Collections.sort(filesToPrint);
                                PrintRequest request = new PrintRequest(filesToPrint, unarchiveDir,
                                        Allocator.getUserServices().getUser(), group);
                                requests.add(request);
                            }
                            catch (FileNotFoundException e)
                            {
                                throw new ActionException("Unable to generate print request for group: " +
                                        group.getName(), e);
                            }
                        }

                        //Issue print command
                        try
                        {
                            Allocator.getLandscapePrintingService().print(requests, printer);
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