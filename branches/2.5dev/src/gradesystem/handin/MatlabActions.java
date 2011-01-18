package gradesystem.handin;

import utils.AlphabeticFileComparator;
import gradesystem.Allocator;
import gradesystem.components.GenericJComboBox;
import gradesystem.components.ShadowJTextField;
import gradesystem.database.Group;
import gradesystem.handin.file.AndFileFilter;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();

        descriptions.add(new CopyTest());
        descriptions.add(new DemoFile());
        descriptions.add(new OpenFiles());
        descriptions.add(new RunFile());

        return descriptions;
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
            "Specifies the path relative to the " + COPY_PATH_PROPERTY.getName() +
            " property of the m-file that will be run. The file will be run " +
            "without any arguments provided.", true);

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
            return "Copies the contents of the specified directory into the " +
                    "root of the unarchived handin. Then runs the specified " +
                    "m-file in MATLAB. Once the copy occurs, it remains, and " +
                    "therefore any other actions may interact with the copied " +
                    "files.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[] { COPY_PATH_PROPERTY, TEST_FILE_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.TEST });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.TEST, ActionMode.OPEN, ActionMode.RUN });
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                //Keeps track of the groups that have already had the files copied
                private HashSet<Group> _testedGroups = new HashSet<Group>();

                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                    //Copy if necessary
                    if(!_testedGroups.contains(group))
                    {
                        File sourceDir = new File(properties.get(COPY_PATH_PROPERTY));

                        //Validate
                        if(!sourceDir.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because the directory \n" +
                                    "to copy test files from does not exist. \n" +
                                    "Directory: " + sourceDir.getAbsoluteFile(),
                                    "Directory does not exist", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if(!sourceDir.isDirectory())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because the directory \n" +
                                    "to copy test files from does not exist. \n" +
                                    "Directory: " + sourceDir.getAbsoluteFile(),
                                    "Not a directory", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        File sourceTestFile = new File(sourceDir, properties.get(TEST_FILE_PROPERTY));
                        if(!sourceTestFile.exists())
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because the test file \n" +
                                    "does not exist. \n" +
                                    "File: " + sourceTestFile.getAbsoluteFile(),
                                    "Test file does not exist", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if(!sourceTestFile.isFile() || !sourceTestFile.getName().endsWith(".m"))
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because the test file \n" +
                                    "is not an m-file. \n" +
                                    "File: " + sourceTestFile.getAbsoluteFile(),
                                    "Test file not m-file", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        try
                        {
                            Allocator.getFileSystemUtilities().copy(sourceDir, unarchiveDir);
                        }
                        catch(FileCopyingException e)
                        {
                            //If a file that already exists would be overwritten
                            if(e.getCause() instanceof FileExistsException)
                            {
                                FileExistsException cause = (FileExistsException) e.getCause();
                                JOptionPane.showMessageDialog(null,
                                    "Cannot perform test because a file to be\n" +
                                    "copied for the test already exists in the\n" +
                                    "unarchived handin.\n\n" +
                                    "Test File: " + cause.getSourceFile().getAbsolutePath() + "\n" +
                                    "Handin File: " + cause.getDestinationFile().getAbsolutePath() + "\n",
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

                        File testFile = new File(unarchiveDir, properties.get(TEST_FILE_PROPERTY));

                        //Move to test file's directory
                        String testDir = testFile.getParent();
                        try
                        {
                            proxy.eval("cd " + testDir);
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
            return Arrays.asList(new DistributableActionProperty[] { DIRECTORY_PATH_PROPERTY,
                FILE_PATH_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.DEMO });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.DEMO,
                ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN });
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
                        mFiles = Allocator.getFileSystemUtilities()
                                .getFiles(demoDir, new FileExtensionFilter("m"),
                                new AlphabeticFileComparator());
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
            return Arrays.asList(new DistributableActionProperty[] { FILE_PATH_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN });
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
                    //Retrieve the m-files belonging to this distributable part
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);
                    FileFilter inclusionFilter = part.getInclusionFilter(group);
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
                            mFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(unarchiveDir, inclusionFilter, new AlphabeticFileComparator());
                        }
                        else
                        {
                            mFiles = new ArrayList<File>();
                            mFiles.add(absolutePath);
                        }
                    }
                    else
                    {
                        mFiles = Allocator.getFileSystemUtilities()
                                .getFiles(unarchiveDir, inclusionFilter, new AlphabeticFileComparator());
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
                        File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);
                        proxy.eval("cd " + unarchiveDir.getAbsolutePath());

                        //Determine which files to open in MATLAB
                        FileFilter inclusionFilter = part.getInclusionFilter(group);
                        FileFilter fileExtensionFilter;
                        if(properties.containsKey(EXTENSIONS_PROPERTY))
                        {
                            fileExtensionFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
                        }
                        else
                        {
                            fileExtensionFilter = new FileExtensionFilter("m");
                        }
                        FileFilter toOpenFilter = new AndFileFilter(inclusionFilter, fileExtensionFilter);
                        List<File> filesToOpen = Allocator.getFileSystemUtilities()
                                .getFiles(unarchiveDir, toOpenFilter, new AlphabeticFileComparator());

                        //Open files in MATLAB
                        String openCmd = "edit ";
                        for(File file : filesToOpen)
                        {
                            openCmd += file.getAbsolutePath() + " ";
                        }
                        proxy.eval(openCmd);
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

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Run m-file", JOptionPane.OK_CANCEL_OPTION);
        if(result == JOptionPane.OK_OPTION)
        {
            try
            {
                RemoteMatlabProxy proxy = getMatlabProxy();

                //Move to parent file's directory
                String parentDir = relativeToAbsolute.get(functionsComboBox.getSelectedItem()).getParent();
                try
                {
                    proxy.eval("cd " + parentDir);
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