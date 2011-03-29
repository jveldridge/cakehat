package gradesystem.handin;

import com.google.common.collect.ImmutableList;
import utils.AlphabeticFileComparator;
import gradesystem.Allocator;
import gradesystem.database.Group;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import utils.FileExtensionFilter;

/**
 * Actions that launch other applications such as text editors.
 * 
 * @author jak2
 */
class ApplicationActions implements ActionProvider
{
    public String getNamespace()
    {
        return "applications";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        return ImmutableList.of(new PDFViewer(), new TextEditor(), new Terminal());
    }

    private class PDFViewer implements DistributableActionDescription
    {
        private final String DEFAULT = "acroread";

        //xpdf only pays attention to the first file argument
        private final HashSet<String> SINGLE_FILE =
                new HashSet<String>(Arrays.asList(
                new String[] {"xpdf"}));

        //acroread, kdpf & evince can accept any number of file arguments
        private final HashSet<String> MULTI_FILE =
                new HashSet<String>(Arrays.asList(
                new String[] {"acroread", "evince", "kpdf"}));
        
        private final HashSet<String> SUPPORTED = new HashSet<String>();
        {
            SUPPORTED.addAll(SINGLE_FILE);
            SUPPORTED.addAll(MULTI_FILE);
        }

        private final DistributableActionProperty APPLICATION_PROPERTY =
            new DistributableActionProperty("application",
            "The name of the PDF application to be used. If this property is " +
            "not set then " + DEFAULT + " will be used. Valid values " +
            "for this property are: " + SUPPORTED, false);

        public ActionProvider getProvider()
        {
            return ApplicationActions.this;
        }

        public String getName()
        {
            return "pdf-viewer";
        }

        public String getDescription()
        {
            return "Opens all pdf files that belong to this distributable part " +
                    "in a PDF viewer. By default the PDF viewer is " + DEFAULT +
                    ", but which viewer is used can be customized by using the " +
                    APPLICATION_PROPERTY.getName() + " property.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(APPLICATION_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.OPEN);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.OPEN, ActionMode.RUN, ActionMode.TEST);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String application = DEFAULT;

                    //Check application property is valid if present
                    if(properties.containsKey(APPLICATION_PROPERTY))
                    {
                        application = properties.get(APPLICATION_PROPERTY);

                        if(!SUPPORTED.contains(application))
                        {
                            JOptionPane.showConfirmDialog(null,
                                    "The PDF application specified in the configuration\n" +
                                    "file is not supported: " + application + ".\n" +
                                    "Supported applications: " + SUPPORTED,
                                    "Invalid PDF application",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                    FileFilter pdfFilter = new FileExtensionFilter("pdf");

                    List<File> pdfFiles;
                    try
                    {
                        pdfFiles = Allocator.getFileSystemUtilities()
                            .getFiles(unarchiveDir, pdfFilter, new AlphabeticFileComparator());
                    }
                    catch(IOException e)
                    {
                        throw new ActionException("Unable to access PDF files", e);
                    }

                    if(pdfFiles.isEmpty())
                    {
                        JOptionPane.showConfirmDialog(null,
                                "There are no PDF files to open.",
                                "No PDF files",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

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
                        throw new ActionException("Unable to open PDF viewer: " +
                                application + " for " + group.getName() +
                                "'s handin.", e);
                    }
                }
            };

            return action;
        }
    }

    private class TextEditor implements DistributableActionDescription
    {
        private final String DEFAULT = "kate";

        //Text editors with a GUI
        private final HashSet<String> GUI = new HashSet<String>(Arrays.asList(
                new String[] {"kate","gedit","bluefish","nedit","emacs","kwrite"}));
        
        //Text editors that have a CLI and therefore need to be run in a 
        //visible terminal
        private final HashSet<String> CLI = new HashSet<String>(Arrays.asList(
                new String[] {"vi","vim","nano","pico"}));

        private final HashSet<String> SUPPORTED = new HashSet<String>();
        {
            SUPPORTED.addAll(GUI);
            SUPPORTED.addAll(CLI);
        }

        private final DistributableActionProperty APPLICATION_PROPERTY =
            new DistributableActionProperty("application",
            "The name of the text editor to be used. If this property is " +
            "not set then kate will be used.\n " +
            "Valid values for this property are: " + SUPPORTED, false);

        private final DistributableActionProperty ENV_PROPERTY =
            new DistributableActionProperty("use-environment",
            "If set to TRUE, attempts to open files using the grader's EDITOR " +
            "environment variable. If this editor is not one of the supported editors, " +
            "then it will not be used. In that case the editor will either " +
            "be the default (" + DEFAULT + ") or the specified editor if the " +
            APPLICATION_PROPERTY.getName() + " property is set.\n" +
            "Supported editors: " + SUPPORTED, false);

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
            return ApplicationActions.this;
        }

        public String getName()
        {
            return "text-editor";
        }

        public String getDescription()
        {
            return "Opens plain text files in a text editor. This text editor " +
                    "may be specified by the " + APPLICATION_PROPERTY.getName() +
                    " property. By default, " + DEFAULT + "is used.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(APPLICATION_PROPERTY, ENV_PROPERTY, EXTENSIONS_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.OPEN);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.OPEN, ActionMode.RUN, ActionMode.TEST);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String application = null;

                    //Try to read out EDITOR environment variable
                    //If running cakehat from a terminal this variable will probably be set
                    //If running from an IDE, it is very unlikely the variable will be set
                    if(properties.containsKey(ENV_PROPERTY) &&
                            properties.get(ENV_PROPERTY).equalsIgnoreCase("TRUE"))
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
                            JOptionPane.showConfirmDialog(null,
                                    "The text editor specified in the configuration\n" +
                                    "file is not supported: " + application + ".\n" +
                                    "Supported applications: " + SUPPORTED,
                                    "Invalid PDF application",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    if(application == null)
                    {
                        application = DEFAULT;
                    }

                    //Get files to open
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

                    FileFilter extensionsFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));

                    List<File> textFiles;
                    try
                    {
                        textFiles = Allocator.getFileSystemUtilities()
                            .getFiles(unarchiveDir, extensionsFilter, new AlphabeticFileComparator());
                    }
                    catch(IOException e)
                    {
                        throw new ActionException("Unable to access text files", e);
                    }

                    if(textFiles.isEmpty())
                    {
                        JOptionPane.showMessageDialog(null,
                                "There are no text files to open.\n" +
                                "Extensions to open are: " + properties.get(EXTENSIONS_PROPERTY),
                                "No text files",
                                JOptionPane.INFORMATION_MESSAGE);

                        return;
                    }

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
                            throw new ActionException("Unable to open text editor: " +
                                application + " for " + group.getName() +
                                "'s handin.", e);
                        }
                    }
                    else if(CLI.contains(application))
                    {
                        String title = group.getName() + "'s " +
                                part.getAssignment().getName() + " - " + part.getName();

                        try
                        {
                            Allocator.getExternalProcessesUtilities()
                                    .executeInVisibleTerminal(title, command, unarchiveDir);
                        }
                        catch(IOException e)
                        {
                            throw new ActionException("Unable to open text editor: " +
                                application + " for " + group.getName() +
                                "'s handin.", e);
                        }
                    }
                }
            };

            return action;
        }
    }

    private class Terminal implements DistributableActionDescription
    {
        public ActionProvider getProvider()
        {
            return ApplicationActions.this;
        }

        public String getName()
        {
            return "terminal";
        }

        public String getDescription()
        {
            return "Opens a terminal that is in the root directory of the " +
                    "unarchived handin";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Collections.emptyList();
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.OPEN);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST);
        }

        public DistributableAction getAction(Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

                    String terminalName = group.getName() + "'s " + part.getAssignment().getName();
                    try
                    {
                        Allocator.getExternalProcessesUtilities()
                                .executeInVisibleTerminal(terminalName, null, unarchiveDir);
                    }
                    catch(IOException e)
                    {
                        throw new ActionException("Unable to open terminal for " + group.getName(), e);
                    }
                }
            };

            return action;
        }
    }
}