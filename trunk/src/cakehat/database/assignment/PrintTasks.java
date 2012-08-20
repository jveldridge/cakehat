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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tasks that print plain text files.
 *
 * @author jak2
 */
class PrintTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "printing";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new Landscape());
    }

    private class Landscape extends MultiGroupTask
    {
        private final TaskProperty EXTENSIONS_PROPERTY =
            new TaskProperty("extensions",
            "The extensions of the files in this part that will be printed. To open files that do not have file " +
            "extensions use an underscore. Regardless of extension, the files must be plain text files. List " +
            "extensions in the following format (without quotation marks):<br>" +
            "single extension - 'java'<br>" +
            "multiple extensions - 'cpp, h'", true);

        private Landscape()
        {
            super(PrintTasks.this, "landscape");
        }

        @Override
        public String getDescription()
        {
            return "Prints plain text files in a space-saving landscape orientation with two columns.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(EXTENSIONS_PROPERTY);
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.PRINT);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
                throws TaskException
        {
            CITPrinter printer = Allocator.getGradingServices().getPrinter();
                    
            //Build print requests
            if(printer != null)
            {
                List<PrintRequest> requests = new ArrayList<PrintRequest>();

                for(Group group : groups)
                {
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);

                    FileFilter extensionsFilter =
                            TaskUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));

                    List<File> filesToPrint;
                    try
                    {
                        Comparator<File> fileComparator = new Comparator<File>()
                        {
                            @Override
                            public int compare(File f1, File f2)
                            {
                                return f1.compareTo(f2);
                            }
                        };
                        filesToPrint = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, extensionsFilter,
                                fileComparator);
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to access files to print", e);
                    }

                    try
                    {
                        PrintRequest request = new PrintRequest(filesToPrint, unarchiveDir,
                                Allocator.getUserServices().getUser(), group);
                        requests.add(request);
                    }
                    catch(FileNotFoundException e)
                    {
                        throw new TaskException("Unable to generate print request for group: " + group.getName(), e);
                    }
                }

                //Issue print command
                try
                {
                    Allocator.getLandscapePrintingService().print(requests, printer);
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to issue print command", e);
                }
            }
        }
    }
}