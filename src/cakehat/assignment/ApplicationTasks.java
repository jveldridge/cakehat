package cakehat.assignment;

import support.utils.AlphabeticFileComparator;
import cakehat.Allocator;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import support.ui.ModalDialog;
import support.utils.FileExtensionFilter;

/**
 * Tasks that launch other applications such as text editors.
 * 
 * @author jak2
 */
class ApplicationTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "applications";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new PDFViewer(), new TextEditor(), new Terminal());
    }

    private class PDFViewer extends SingleGroupTask
    {
        private final String DEFAULT = "evince";

        //xpdf only pays attention to the first file argument
        private final Set<String> SINGLE_FILE = ImmutableSet.of("xpdf");

        //acroread & evince can accept any number of file arguments
        private final Set<String> MULTI_FILE = ImmutableSet.of("acroread", "evince");
        
        private final Set<String> SUPPORTED = new ImmutableSet.Builder<String>().addAll(SINGLE_FILE)
                .addAll(MULTI_FILE).build();

        private final TaskProperty APPLICATION_PROPERTY =
            new TaskProperty("application",
            "The name of the PDF application to be used. If this property is not set then " + DEFAULT + " will be " +
            "used. Valid values for this property are: " + SUPPORTED, false);

        private PDFViewer()
        {
            super(ApplicationTasks.this, "pdf-viewer");
        }

        @Override
        public String getDescription()
        {
            return "Opens all pdf files that belong to this part in a PDF viewer. By default the PDF viewer is " +
                    DEFAULT + ", but which viewer is used can be customized by using the " +
                    APPLICATION_PROPERTY.getName() + " property.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(APPLICATION_PROPERTY);
        }

        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.OPEN);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Group group)
                throws TaskException
        {
            String application = DEFAULT;

            //Check application property is valid if present
            if(properties.containsKey(APPLICATION_PROPERTY))
            {
                application = properties.get(APPLICATION_PROPERTY);

                if(!SUPPORTED.contains(application))
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Invalid PDF application",
                            "The PDF application specified in the configuration is not supported: " + application +
                            ". " + DEFAULT + " will be used instead.\n\n" +
                            "Supported applications: " + SUPPORTED);
                    application = DEFAULT;
                }
            }

            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
            FileFilter pdfFilter = new FileExtensionFilter("pdf");

            List<File> pdfFiles;
            try
            {
                pdfFiles = Allocator.getFileSystemUtilities()
                    .getFiles(unarchiveDir, pdfFilter, new AlphabeticFileComparator());
            }
            catch(IOException e)
            {
                throw new TaskException("Unable to access PDF files", e);
            }

            if(pdfFiles.isEmpty())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "No PDF files", "There are no PDF files to open.");
            }
            else
            {
                //Build commands
                List<String> commands = new ArrayList<String>();

                if(MULTI_FILE.contains(application))
                {
                    String command = application;

                    for(File pdfFile : pdfFiles)
                    {
                        command += " " + "'" + pdfFile.getAbsolutePath() + "'";
                    }

                    commands.add(command);
                }
                else if(SINGLE_FILE.contains(application))
                {
                    for(File pdfFile : pdfFiles)
                    {
                        String command = application + " " + "'" + pdfFile.getAbsolutePath() + "'";
                        commands.add(command);
                    }
                }

                //Run commands
                try
                {
                    Allocator.getExternalProcessesUtilities().executeAsynchronously(commands, unarchiveDir);
                }
                catch (IOException e)
                {
                    throw new TaskException("Unable to open PDF viewer: " + application + " for " +
                            group.getName() + "'s handin.", e);
                }
            }
        }
    }

    private class TextEditor extends SingleGroupTask
    {
        private final String DEFAULT = "kate";

        //Text editors with a GUI
        private final Set<String> GUI = ImmutableSet.of("kate","gedit","bluefish","nedit","emacs","kwrite");
        
        //Text editors that have a CLI and therefore need to be run in a visible terminal
        private final Set<String> CLI = ImmutableSet.of("vi","vim","nano","pico");

        private final Set<String> SUPPORTED = new ImmutableSet.Builder<String>().addAll(GUI).addAll(CLI).build();

        private final TaskProperty APPLICATION_PROPERTY =
            new TaskProperty("application",
            "The name of the text editor to be used. If this property is not set then " + DEFAULT + " will be " + 
            "used. If an invalid editor is provided then " + DEFAULT + " will be used.<br>" +
            "Valid values for this property are: " + SUPPORTED, false);

        private final TaskProperty ENV_PROPERTY =
            new TaskProperty("use-environment",
            "If set to TRUE, attempts to open files using the grader's EDITOR environment variable. If this editor " +
            "is not one of the supported editors, then it will not be used. In that case the editor will either " +
            "be the default (" + DEFAULT + ") or the specified editor if the " + APPLICATION_PROPERTY.getName() +
            " property is set.<br>" +
            "Supported editors: " + SUPPORTED, false);

        private final TaskProperty EXTENSIONS_PROPERTY =
            new TaskProperty("extensions",
            "The extensions of the files in this part that will be opened. To open files that do not have file " +
            "extensions use an underscore. Regardless of extension, the files must be plain text files. List " +
            "extensions in the following format (without quotation marks):<br>" +
            "single extension - 'java'<br>" +
            "multiple extensions - 'cpp, h'", true);

        private TextEditor()
        {
            super(ApplicationTasks.this, "text-editor");
        }

        @Override
        public String getDescription()
        {
            return "Opens plain text files in a text editor. This text editor may be specified by the " +
                   APPLICATION_PROPERTY.getName() + " property. By default, " + DEFAULT + " is used.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(APPLICATION_PROPERTY, ENV_PROPERTY, EXTENSIONS_PROPERTY);
        }

        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.OPEN);
        }
                
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }
        
        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Group group)
            throws TaskException
        {
            String application = null;

            //Try to read out EDITOR environment variable
            //If running cakehat from a terminal this variable will probably be set
            //If running from an IDE, it is very unlikely the variable will be set
            if("TRUE".equalsIgnoreCase(properties.get(ENV_PROPERTY)))
            {
                String editor = System.getenv("EDITOR");

                if(editor != null && SUPPORTED.contains(editor))
                {
                    application = editor;
                }
            }

            //Check application property is valid if present
            if(application == null && properties.containsKey(APPLICATION_PROPERTY))
            {
                application = properties.get(APPLICATION_PROPERTY);

                if(!SUPPORTED.contains(application))
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Invalid text editor",
                            "The text editor specified in the configuration is not supported: " + application +
                            ". " + DEFAULT + " will be used instead.\n\n" +
                            "Supported applications: " + SUPPORTED);
                    application = DEFAULT;
                }
            }

            if(application == null)
            {
                application = DEFAULT;
            }

            //Get files to open
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
                    
            FileFilter extensionsFilter = TaskUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));

            List<File> textFiles;
            try
            {
                textFiles = Allocator.getFileSystemUtilities()
                    .getFiles(unarchiveDir, extensionsFilter, new AlphabeticFileComparator());
            }
            catch(IOException e)
            {
                throw new TaskException("Unable to access text files", e);
            }

            if(textFiles.isEmpty())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "No text files",
                        "There are no text files to open.\n\n" +
                        "Extensions to open are: " + properties.get(EXTENSIONS_PROPERTY));
            }
            else
            {
                //Build run command
                String command = application;
                for(File file : textFiles)
                {
                    command += " " + "'" + file.getAbsolutePath() + "'";
                }

                if(GUI.contains(application))
                {
                    try
                    {
                        Allocator.getExternalProcessesUtilities().executeAsynchronously(command, unarchiveDir);
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to open text editor: " + application + " for " +
                                group.getName() + "'s handin.", e);
                    }
                }
                else if(CLI.contains(application))
                {
                    String title = group.getName() + "'s " + action.getPart().getFullDisplayName();

                    try
                    {
                        Allocator.getExternalProcessesUtilities()
                                .executeInVisibleTerminal(title, command, unarchiveDir);
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to open text editor: " + application + " for " +
                                group.getName() + "'s handin.", e);
                    }
                }
            }
        }
    }

    private class Terminal extends SingleGroupTask
    {
        private Terminal()
        {
            super(ApplicationTasks.this, "terminal");
        }

        @Override
        public String getDescription()
        {
            return "Opens a terminal that is in the root directory of the unarchived handin.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of();
        }

        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.RUN, ActionDescription.OPEN);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }
        
        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Group group)
            throws TaskException
        {
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);

            String terminalName = group.getName() + "'s " + action.getPart().getFullDisplayName();
            try
            {
                Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(terminalName, null, unarchiveDir);
            }
            catch(IOException e)
            {
                throw new TaskException("Unable to open terminal\n" +
                        "Group: " + group.getName() + "\n" +
                        "Action: " + action.getDebugName() + "\n" +
                        "Unarchive Directory: " + unarchiveDir.getAbsolutePath(), e);
            }
        }
    }
}