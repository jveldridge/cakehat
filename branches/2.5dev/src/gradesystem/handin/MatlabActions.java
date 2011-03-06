package gradesystem.handin;

import com.google.common.collect.ImmutableList;
import utils.AlphabeticFileComparator;
import gradesystem.Allocator;
import gradesystem.components.GenericJComboBox;
import gradesystem.components.ShadowJTextField;
import gradesystem.database.Group;
import gradesystem.resources.icons.IconLoader;
import gradesystem.resources.icons.IconLoader.IconImage;
import gradesystem.resources.icons.IconLoader.IconSize;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabConnectionListener;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.RemoteMatlabProxy;
import matlabcontrol.RemoteMatlabProxyFactory;
import utils.FileCopyingException;
import utils.FileExistsException;
import utils.FileExtensionFilter;

/**
 * Actions that interact with MATLAB. These actions make use of the
 * matlabcontrol library to launch and interact with the MATLAB application such
 * that a new instance of MATLAB need not be launched each time an action is
 * performed.
 *
 * @author jak2
 */
class MatlabActions implements ActionProvider
{
    public String getNamespace()
    {
        return "matlab";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        return ImmutableList.of(new CopyTest(), new DemoFile(), new OpenFiles(), new RunFile());
    }

    private class CopyTest implements DistributableActionDescription
    {
        private final DistributableActionProperty COPY_PATH_PROPERTY =
            new DistributableActionProperty("copy-path",
            "The fully qualified path to the directory whose entire contents " +
            "will be copied into the root of the unarchived handin directory.",
            true);

        private final DistributableActionProperty TEST_FILE_PROPERTY =
            new DistributableActionProperty("test-file",
            "This property must be specified if a directory is provided for the " +
            COPY_PATH_PROPERTY.getName() + " property. The m-file that will be " +
            "run. The path must be relative to directory specified by the " +
            COPY_PATH_PROPERTY.getName() + " property.", false);

        public ActionProvider getProvider()
        {
            return MatlabActions.this;
        }

        public String getName()
        {
            return "copy-test";
        }

        public String getDescription()
        {
            return "Copies the specified file or contents of the directory into " +
                    "the root of the unarchived handin. Then runs the specified " +
                    "m-file in MATLAB. Once the copy occurs, it remains, and " +
                    "therefore any other actions may interact with the copied " +
                    "files.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(COPY_PATH_PROPERTY, TEST_FILE_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.TEST);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.TEST, ActionMode.OPEN, ActionMode.RUN);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                //Keeps track of the groups that have already had the files copied
                private HashSet<Group> _testedGroups = new HashSet<Group>();

                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

                    //Copy if necessary
                    if(!_testedGroups.contains(group))
                    {
                        File source = new File(properties.get(COPY_PATH_PROPERTY));

                        //Validate
                        if(!source.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because the directory \n" +
                                    "or file to copy does not exist. \n" +
                                    "Source: " + source.getAbsoluteFile(),
                                    "Does not exist", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        if(source.isFile())
                        {
                            if(!source.getName().endsWith(".m"))
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot perform test because the test file \n" +
                                        "is not an m-file. \n" +
                                        "File: " + source.getAbsoluteFile(),
                                        "Test file not m-file", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                        else if(source.isDirectory())
                        {
                            String relativePath = properties.get(TEST_FILE_PROPERTY);

                            if(relativePath == null)
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot perform test because the " +
                                        TEST_FILE_PROPERTY.getName() +
                                        "property was not set. It must be set when\n" +
                                        "copying test files from a directory.",
                                        "Property not set", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            File testFile = new File(source, relativePath);
                            if(!testFile.exists())
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot perform test because the test file \n" +
                                        "does not exist. \n" +
                                        "File: " + testFile.getAbsoluteFile(),
                                        "Test file does not exist", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            if(!testFile.isFile() || !testFile.getName().endsWith(".m"))
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot perform test because the test file \n" +
                                        "is not an m-file. \n" +
                                        "File: " + testFile.getAbsoluteFile(),
                                        "Test file not m-file", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }

                        try
                        {
                            File destination;
                            if(source.isFile())
                            {
                                destination = new File(unarchiveDir, source.getName());
                            }
                            else
                            {
                                destination = unarchiveDir;
                            }

                            Allocator.getFileSystemUtilities().copy(source, destination);
                        }
                        catch(FileCopyingException e)
                        {
                            //If a file that already exists would be overwritten
                            FileExistsException existsException =
                                    Allocator.getGeneralUtilities().findInStack(e, FileExistsException.class);
                            if(existsException != null)
                            {
                                JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because a file to be\n" +
                                    "copied for the test already exists in the\n" +
                                    "unarchived handin.\n\n" +
                                    "Test File: " + existsException.getSourceFile().getAbsolutePath() + "\n" +
                                    "Handin File: " + existsException.getDestinationFile().getAbsolutePath() + "\n",
                                    "Cannot copy test file", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            throw new ActionException("Unable to perform copy " +
                                    "necessary for testing.", e);
                        }
                    }

                    try
                    {
                        RemoteMatlabProxy proxy = getMatlabProxy();

                        //Determine the location of the test file once it has been copied
                        File testFile;
                        File source = new File(properties.get(COPY_PATH_PROPERTY));
                        if(source.isFile())
                        {
                            testFile = new File(unarchiveDir, source.getName());
                        }
                        else
                        {
                            testFile = new File(unarchiveDir, properties.get(TEST_FILE_PROPERTY));
                        }

                        //Move to test file's directory
                        String testDir = testFile.getParent();
                        try
                        {
                            proxy.feval("cd", new String[] { testDir });
                        }
                        catch(MatlabInvocationException e)
                        {
                            throw new ActionException("Unable to make MATLAB change directory: " + testDir, e);
                        }

                        //Run test file
                        String testFunction = testFile.getName().split("\\.")[0];
                        try
                        {
                            proxy.eval(testFunction);
                        }
                        //This exception might be because the of an issue communicating with MATLAB
                        //but it also could arise from calling the function wrong
                        //(for instance, the function is expected to take no arguments, but
                        //if it requires them that would cause an exception)
                        catch(MatlabInvocationException e)
                        {
                            throw new ActionException("Unable to run test function: " +
                                    testFunction, e);
                        }
                    }
                    catch(MatlabConnectionException e)
                    {
                        throw new ActionException(e);
                    }
                }
            };

            return action;
        }
    }

    private class DemoFile implements DistributableActionDescription
    {
        private final DistributableActionProperty DIRECTORY_PATH_PROPERTY =
            new DistributableActionProperty("directory-path",
            "Specifies the absolute path to the directory containing the m-file(s) " +
            "that make up the demo. All files in the specified directory and " +
            "sub-directories are considered included.", true);

        private final DistributableActionProperty FILE_PATH_PROPERTY =
            new DistributableActionProperty("file-path",
            "Specifies the path relative to DIRECTORY_PATH_PROPERTY. " +
            "If this property is not specified then the grader will be asked " +
            "to select which file to run.", false);

        public ActionProvider getProvider()
        {
            return MatlabActions.this;
        }

        public String getName()
        {
            return "demo-file";
        }

        public String getDescription()
        {
            return "Runs a single m-file with grader provided arguments. The " +
                    "grader will either be allowed to select which m-file to run " +
                    "from a list of all m-files in the demo, or if FILE_PATH_PROPERTY " +
                    "is specified then only the file specified by that property.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(DIRECTORY_PATH_PROPERTY, FILE_PATH_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.DEMO);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.DEMO, ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    //Ensure specified directory is valid
                    File demoDir = new File(properties.get(DIRECTORY_PATH_PROPERTY));
                    if(!demoDir.exists())
                    {
                        JOptionPane.showMessageDialog(null,
                                "Directory specified by '" +
                                DIRECTORY_PATH_PROPERTY.getName() +
                                "' does not exist.\n" +
                                "Directory: " + demoDir.getAbsolutePath(),
                                "Directory does not exist",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if(!demoDir.isDirectory())
                    {
                        JOptionPane.showMessageDialog(null,
                                "Directory specified by '" +
                                DIRECTORY_PATH_PROPERTY.getName() +
                                "' is not a directory.\n" +
                                "Directory: " + demoDir.getAbsolutePath(),
                                "Not a directory",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    //Build list of m-files that are to be demo
                    List<File> mFiles;

                    //If a specific file to run was specified, attempt to demo
                    //that file, but if it does not exist inform and abort
                    if(properties.containsKey(FILE_PATH_PROPERTY))
                    {
                        String relativePath = properties.get(FILE_PATH_PROPERTY);
                        File absolutePath = new File(demoDir, relativePath);

                        if(!absolutePath.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "File specified by '" +
                                    FILE_PATH_PROPERTY.getName() +
                                    "' does not exist.\n" +
                                    "File: " +
                                    absolutePath.getAbsolutePath(),
                                    "File does not exist",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        else
                        {
                            mFiles = new ArrayList<File>();
                            mFiles.add(absolutePath);
                        }
                    }
                    else
                    {
                        try
                        {
                            mFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(demoDir, new FileExtensionFilter("m"),
                                    new AlphabeticFileComparator());
                        }
                        catch(IOException e)
                        {
                            throw new ActionException("Unable to access m files", e);
                        }
                    }

                    if(mFiles.isEmpty())
                    {
                        JOptionPane.showMessageDialog(null,
                                "There are no m-files to demo.",
                                "Unable to demo", JOptionPane.WARNING_MESSAGE);
                    }
                    else
                    {
                        runMFiles(mFiles, demoDir);
                    }
                }
            };

            return action;
        }
    }

    private class RunFile implements DistributableActionDescription
    {
        private final DistributableActionProperty FILE_PATH_PROPERTY =
            new DistributableActionProperty("file-path",
            "Specifies the path relative to handin of the m-file to run. " +
            "If this property is not specified or the file specified does not " +
            "exist in the handin then the grader will be asked to select which " +
            "file to run.", false);

        public ActionProvider getProvider()
        {
            return MatlabActions.this;
        }

        public String getName()
        {
            return "run-file";
        }

        public String getDescription()
        {
            return "Runs a single m-file with grader provided arguments. The " +
                    "grader will either be allowed to select which m-file to run " +
                    "from a list of all m-files that are part of this distributable " +
                    "part or the grader will only be able to run the m-file " +
                    "specified by FILE_PATH_PROPERTY.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(FILE_PATH_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.RUN);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    //Retrieve the m-files belonging to this distributable part
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                    FileFilter mFilter = new FileExtensionFilter("m");
                    List<File> mFiles;

                    //If a specific file to run was specified, attempt to run
                    //that file, but if it does not exist inform the user and
                    //then continue as if the file was not specified
                    if(properties.containsKey(FILE_PATH_PROPERTY))
                    {
                        String relativePath = properties.get(FILE_PATH_PROPERTY);
                        File absolutePath = new File(unarchiveDir, relativePath);

                        if(!absolutePath.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Specified file to run does not exist. \n" +
                                    "Expected file: \n" +
                                    absolutePath.getAbsolutePath() + "\n\n" +
                                    "You will be asked to select from available m-files.",
                                    "Specified File Unavailable",
                                    JOptionPane.WARNING_MESSAGE);

                            try
                            {
                                mFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(unarchiveDir, mFilter, new AlphabeticFileComparator());
                            }
                            catch(IOException e)
                            {
                                throw new ActionException("Unable to access m files", e);
                            }
                        }
                        else
                        {
                            mFiles = new ArrayList<File>();
                            mFiles.add(absolutePath);
                        }
                    }
                    else
                    {
                        try
                        {
                            mFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(unarchiveDir, mFilter, new AlphabeticFileComparator());
                        }
                        catch(IOException e)
                        {
                            throw new ActionException("Unable to access m files", e);
                        }
                    }

                    if(mFiles.isEmpty())
                    {
                        JOptionPane.showMessageDialog(null,
                                "There are no m-files for this distributable part.",
                                "Unable to run", JOptionPane.WARNING_MESSAGE);
                    }
                    else
                    {
                        runMFiles(mFiles, unarchiveDir);
                    }
                }
            };

            return action;
        } 
    }

    private class OpenFiles implements DistributableActionDescription
    {
        private final DistributableActionProperty EXTENSIONS_PROPERTY =
            new DistributableActionProperty("extensions",
            "The extensions of the files in this distributable part that will be opened. " +
            "To open files that do not have file extensions use an underscore. " +
            "If this property is not specified then only m-files will be opened. \n" +
            "List extensions in the following format (without quotation marks): \n" +
            "single extension - 'm' \n" +
            "multiple extensions - 'm, csv'", false);

        public ActionProvider getProvider()
        {
            return MatlabActions.this;
        }

        public String getName()
        {
            return "open";
        }

        public String getDescription()
        {
            return "Opens all files in the distributable part in MATLAB.";
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
            return Arrays.asList(new ActionMode[] { ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN });
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    try
                    {
                        //Retrieve proxy used to communicate with MATLAB
                        RemoteMatlabProxy proxy = getMatlabProxy();

                        //Close all currently open files
                        //Closing the MATLAB editor is undocumented functionality
                        //The command is verified to work in MATLAB R2010a and MATLAB R2010b
                        proxy.eval("com.mathworks.mlservices.MLEditorServices.getEditorApplication.closeNoPrompt");

                        //Change to root directory of handin
                        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                        proxy.feval("cd", new String[] { unarchiveDir.getAbsolutePath() });

                        //Determine which files to open in MATLAB
                        FileFilter fileExtensionFilter;
                        if(properties.containsKey(EXTENSIONS_PROPERTY))
                        {
                            fileExtensionFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
                        }
                        else
                        {
                            fileExtensionFilter = new FileExtensionFilter("m");
                        }
                        
                        List<File> filesToOpen;
                        try
                        {
                            filesToOpen = Allocator.getFileSystemUtilities()
                                .getFiles(unarchiveDir, fileExtensionFilter, new AlphabeticFileComparator());
                        }
                        catch(IOException e)
                        {
                            throw new ActionException("Unable to access files", e);
                        }

                        //Open files in MATLAB
                        String[] openPaths = new String[filesToOpen.size()];
                        for(int i = 0; i < filesToOpen.size(); i++)
                        {
                            openPaths[i] = filesToOpen.get(i).getAbsolutePath();
                        }
                        proxy.feval("edit", openPaths);
                    }
                    catch(MatlabConnectionException e)
                    {
                        throw new ActionException(e);
                    }
                    catch(MatlabInvocationException e)
                    {
                        throw new ActionException("Unable to have MATLAB open handin for group: " + group.getName(), e);
                    }
                }
            };

            return action;
        }
    }
    
    private RemoteMatlabProxy _proxy; //Only getMatlabProxy() should use this variable
    private RemoteMatlabProxy getMatlabProxy() throws MatlabConnectionException
    {
        if(_proxy == null)
        {
            RemoteMatlabProxyFactory factory = new RemoteMatlabProxyFactory();
            factory.addConnectionListener(new MatlabConnectionListener()
            {
                public void connectionEstablished(RemoteMatlabProxy proxy) {}

                public void connectionLost(RemoteMatlabProxy proxy)
                {
                    _proxy = null;
                }
            });
            _proxy = factory.getProxy();
        }

        return _proxy;
    }

    private void runMFiles(List<File> mFiles, File containingDir) throws ActionException
    {
        //Create paths that are relative to the unarchive directory
        //These paths will be dislayed to the grader
        ArrayList<String> relativePaths = new ArrayList<String>();
        final HashMap<String, File> relativeToAbsolute = new HashMap<String, File>();
        for(File mFile : mFiles)
        {
            String relativePath = mFile.getAbsolutePath();
            relativePath = relativePath.replace(containingDir.getAbsolutePath(), "");
            relativeToAbsolute.put(relativePath, mFile);
            relativePaths.add(relativePath);
        }

        //Create a GUI for the grader to select which m-file to run
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        final GenericJComboBox<String> functionsComboBox = new GenericJComboBox<String>(relativePaths);
        final JTextField argumentsField = new ShadowJTextField("Function arguments (optional)");
        final JLabel evalLabel = new JLabel("");

        panel.add(functionsComboBox);
        panel.add(argumentsField);
        panel.add(evalLabel);

        //Update the evalLabel when either the argumentsField is
        //updated or a new selection from functionComboBox is made
        final Runnable updateEval = new Runnable()
        {
            public void run()
            {
                File mFile = relativeToAbsolute.get(functionsComboBox.getSelectedItem());
                //Convert file name to functionName
                String evalCmd = mFile.getName().split("\\.")[0];
                //Add function arguments if provided
                if(argumentsField.getText() != null && !argumentsField.getText().isEmpty())
                {
                    evalCmd += "(" + argumentsField.getText() + ")";
                }
                evalLabel.setText(evalCmd);
            }
        };
        argumentsField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent de) { updateEval.run(); }
            public void removeUpdate(DocumentEvent de) { updateEval.run(); }
            public void changedUpdate(DocumentEvent de){ updateEval.run(); }
        });
        functionsComboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) { updateEval.run(); }
        });
        updateEval.run();

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.GO_NEXT);
        int result = JOptionPane.showConfirmDialog(null, panel,
                "Run m-file", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, icon);
        if(result == JOptionPane.OK_OPTION)
        {
            try
            {
                RemoteMatlabProxy proxy = getMatlabProxy();

                //Move to parent file's directory
                String parentDir = relativeToAbsolute.get(functionsComboBox.getSelectedItem()).getParent();
                try
                {
                    proxy.feval("cd", new String[] { parentDir });
                }
                catch(MatlabInvocationException e)
                {
                    throw new ActionException("Unable to make MATLAB change directory: " + parentDir, e);
                }

                //An exception in this case is likely due to invalid arguments
                //supplied to the MATLAB function
                try
                {
                    proxy.eval(evalLabel.getText());
                }
                catch(MatlabInvocationException e)
                {
                    JOptionPane.showMessageDialog(null,
                            "Invalid MATLAB command, please see MATLAB for more information.",
                            "Invalid MATLAB Command",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            catch(MatlabConnectionException e)
            {
                throw new ActionException(e);
            }
        }
    }
}