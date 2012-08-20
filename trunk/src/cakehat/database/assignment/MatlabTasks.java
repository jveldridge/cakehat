package cakehat.database.assignment;

import support.utils.AlphabeticFileComparator;
import cakehat.Allocator;
import cakehat.CakehatSession;
import support.ui.GenericJComboBox;
import support.ui.ShadowJTextField;
import cakehat.database.Group;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import com.google.common.collect.ImmutableSet;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabConnectionListener;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.RemoteMatlabProxy;
import matlabcontrol.RemoteMatlabProxyFactory;
import support.ui.DocumentAdapter;
import support.ui.ModalDialog;
import support.utils.FileExtensionFilter;

/**
 * Tasks that interact with MATLAB. These tasks make use of the matlabcontrol library to launch and interact with
 * the MATLAB application such that a new instance of MATLAB need not be launched each time a task is performed.
 *
 * @author jak2
 */
class MatlabTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "matlab";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new CopyTest(), new DemoFile(), new OpenFiles(), new RunFile());
    }

    private class CopyTest extends SingleGroupTask
    {
        private final TaskProperty COPY_PATH_PROPERTY =
            new TaskProperty("copy-path",
            "The absolute path to the file or directory whose entire contents will be copied into the root of a copy " +
            "of the unarchived handin directory. If a directory is specified then the entire contents of the " +
            "directory will be copied, but not the directory itself.", true);
        
        private final TaskProperty TEST_FILE_PROPERTY =
                new TaskProperty("test-file",
                "This property must be specified if a directory is provided for the " + COPY_PATH_PROPERTY.getName() +
                " property. The absolute path to the m-file that will be run. This file must be contained in " +
                "the directory provided for the " + COPY_PATH_PROPERTY.getName() + " property.", false);

        private CopyTest()
        {
            super(MatlabTasks.this, "copy-test");
        }

        @Override
        public String getDescription()
        {
            return "Copies the specified file or contents of the directory into the root of a copy of the unarchived " +
                   "handin. Then runs the specified m-file in MATLAB.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(COPY_PATH_PROPERTY, TEST_FILE_PROPERTY);
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.TEST);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }
        
        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Group group)
                throws TaskException, TaskConfigurationIssue
        {
            File copyPath = new File(properties.get(COPY_PATH_PROPERTY));
            
            //Validate
            if(copyPath.isDirectory())
            {
                if(!properties.containsKey(TEST_FILE_PROPERTY))
                {
                    throw new TaskConfigurationIssue(COPY_PATH_PROPERTY.getName() + " property is configured to a " + 
                            "directory and the " + TEST_FILE_PROPERTY.getName() + " is not set.\n" + 
                            COPY_PATH_PROPERTY.getName() + ": " + copyPath.getAbsolutePath());
                }
                
                File testFile = new File(properties.get(TEST_FILE_PROPERTY));
                if(!testFile.isFile())
                {
                    throw new TaskConfigurationIssue(TEST_FILE_PROPERTY.getName() + " property is not configured to " +
                            "be a file.\n" + 
                            TEST_FILE_PROPERTY.getName() + ": " + testFile.getAbsolutePath());
                }
                
                if(!testFile.getAbsolutePath().startsWith(copyPath.getAbsolutePath()))
                {
                    throw new TaskConfigurationIssue(TEST_FILE_PROPERTY.getName() + " property is not configured to " +
                            "be contained in the directory specified by the " + COPY_PATH_PROPERTY.getName() + ".\n" +
                            COPY_PATH_PROPERTY.getName() + ": " + copyPath.getAbsolutePath() + "\n" + 
                            TEST_FILE_PROPERTY.getName() + ": " + testFile.getAbsolutePath());
                }
            }
            
            TaskUtilities.copyUnarchivedHandinToTemp(action, group);
            if(TaskUtilities.copyForTest(copyPath, context, action, group))
            {
                //Determine the location of the test file once it has been copied
                File tempDir = Allocator.getPathServices().getActionTempDir(action, group);
                File dstTestFile;
                if(copyPath.isDirectory())
                {
                    File srcTestFile = new File(properties.get(TEST_FILE_PROPERTY));
                    String relativeTestFile = srcTestFile.getAbsolutePath()
                            .replaceFirst(copyPath.getAbsolutePath(), "");
                    dstTestFile = new File(tempDir, relativeTestFile);
                }
                else
                {
                    dstTestFile = new File(tempDir, copyPath.getName());
                }

                //Run test file
                try
                {
                    RemoteMatlabProxy proxy = getMatlabProxy();

                    //Move to test file's directory
                    String testDir = dstTestFile.getParent();
                    try
                    {
                        proxy.feval("cd", new String[] { testDir });
                    }
                    catch(MatlabInvocationException e)
                    {
                        throw new TaskException("Unable to make MATLAB change directory: " + testDir, e);
                    }

                    //Run test file
                    String testFunction = dstTestFile.getName().split("\\.")[0];
                    try
                    {
                        proxy.eval(testFunction);
                    }
                    //This exception might be because the of an issue communicating with MATLAB but it also could arise
                    //from calling the function wrong (for instance, the function is expected to take no arguments, but
                    //if it requires them that would cause an exception)
                    catch(MatlabInvocationException e)
                    {
                        throw new TaskException("Unable to run test function: " + testFunction, e);
                    }
                }
                catch(MatlabConnectionException e)
                {
                    throw new TaskException("Unable to connect to MATLAB", e);
                }
            }
        }
    }

    private class DemoFile extends NoGroupTask
    {
        private final TaskProperty DIRECTORY_PATH_PROPERTY =
            new TaskProperty("directory-path",
            "Specifies the absolute path to the directory containing the m-file(s) that make up the demo. All files " +
            "in the specified directory and sub-directories are considered included.",
            true);

        private final TaskProperty FILE_PATH_PROPERTY =
            new TaskProperty("file-path",
            "Specifies the path relative to " + DIRECTORY_PATH_PROPERTY.getName() + ". If this property is not " +
            "specified then the grader will be asked to select which file to run.",
            false);

        private DemoFile()
        {
            super(MatlabTasks.this, "demo-file");
        }

        @Override
        public String getDescription()
        {
            return "Runs a single m-file with grader provided arguments. The grader will either be allowed to select " +
                   "which m-file to run from a list of all m-files in the demo, or if " + FILE_PATH_PROPERTY.getName() +
                   " is specified then only the file specified by that property.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(DIRECTORY_PATH_PROPERTY, FILE_PATH_PROPERTY);
        }

        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.DEMO);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action) throws TaskException
        {
            //Ensure specified directory is valid
            File demoDir = new File(properties.get(DIRECTORY_PATH_PROPERTY));
            if(!demoDir.exists())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Directory does not exist",
                        "Directory specified by '" + DIRECTORY_PATH_PROPERTY.getName() + "' does not " +
                        "exist.\n\n" +
                        "Directory: " + demoDir.getAbsolutePath());

                return;
            }
            if(!demoDir.isDirectory())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Not a directory",
                        "Directory specified by '" + DIRECTORY_PATH_PROPERTY.getName() + "' is not a " +
                        "directory.\n\n" +
                        "Directory: " + demoDir.getAbsolutePath());

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
                    ModalDialog.showMessage(context.getGraphicalOwner(), "File does not exist",
                            "File specified by '" + FILE_PATH_PROPERTY.getName() + "' does not exist.\n\n" +
                            "File: " + absolutePath.getAbsolutePath());

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
                            .getFiles(demoDir, new FileExtensionFilter("m"), new AlphabeticFileComparator());
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to access m files", e);
                }
            }

            if(mFiles.isEmpty())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Unable to demo",
                        "There are no m-files to demo.");
            }
            else
            {
                runMFiles(mFiles, demoDir);
            }
        }
    }

    private class RunFile extends SingleGroupTask
    {
        private final TaskProperty FILE_PATH_PROPERTY =
            new TaskProperty("file-path",
            "Specifies the path relative to handin of the m-file to run.  If this property is not specified or the " +
            "file specified does not exist in the handin then the grader will be asked to select which file to run.",
            false);

        private RunFile()
        {
            super(MatlabTasks.this, "run-file");
        }

        @Override
        public String getDescription()
        {
            return "Runs a single m-file with grader provided arguments. The grader will either be allowed to select " +
                   "which m-file to run from a list of all m-files that are part of this part or the grader will " +
                   "only be able to run the m-file specified by " + FILE_PATH_PROPERTY.getName() + ".";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(FILE_PATH_PROPERTY);
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.RUN);
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
            //Retrieve the m-files belonging to this part
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
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
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Specified File Unavailable",
                            "Specified file to run does not exist.\n\n" +
                            "Expected file: " + absolutePath.getAbsolutePath() + "\n\n" +
                            "You will be asked to select from available m-files.");

                    try
                    {
                        mFiles = Allocator.getFileSystemUtilities()
                            .getFiles(unarchiveDir, mFilter, new AlphabeticFileComparator());
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to access m files", e);
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
                    throw new TaskException("Unable to access m files", e);
                }
            }

            if(mFiles.isEmpty())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Unable to run",
                        "There are no m-files for this part.");
            }
            else
            {
                runMFiles(mFiles, unarchiveDir);
            }
        }
    }

    private class OpenFiles extends SingleGroupTask
    {
        private final TaskProperty EXTENSIONS_PROPERTY =
            new TaskProperty("extensions",
            "The extensions of the files in this part that will be opened. To open files that do not have file " +
            "extensions use an underscore. If this property is not specified then only m-files will be opened.<br>" +
            "List extensions in the following format (without quotation marks):<br>" +
            "single extension - 'm'<br>" +
            "multiple extensions - 'm, csv'", false);

        private OpenFiles()
        {
            super(MatlabTasks.this, "open");
        }

        @Override
        public String getDescription()
        {
            return "Opens all files in the part in MATLAB.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(EXTENSIONS_PROPERTY);
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
            try
            {
                //Retrieve proxy used to communicate with MATLAB
                RemoteMatlabProxy proxy = getMatlabProxy();

                //Close all currently open files
                //Closing the MATLAB editor is undocumented functionality
                //The command is verified to work in MATLAB R2010a and MATLAB R2010b
                proxy.eval("com.mathworks.mlservices.MLEditorServices.getEditorApplication.closeNoPrompt");

                //Change to root directory of handin
                File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
                proxy.feval("cd", new String[] { unarchiveDir.getAbsolutePath() });

                //Determine which files to open in MATLAB
                FileFilter fileExtensionFilter;
                if(properties.containsKey(EXTENSIONS_PROPERTY))
                {
                    fileExtensionFilter = TaskUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
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
                    throw new TaskException("Unable to access files", e);
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
                throw new TaskException("Unable to have MATLAB open handin for group: " + group.getName(), e);
            }
            catch(MatlabInvocationException e)
            {
                throw new TaskException("Unable to have MATLAB open handin for group: " + group.getName(), e);
            }
        }
    }
    
    private RemoteMatlabProxy _proxy; //Only getMatlabProxy() should use this variable
    private RemoteMatlabProxy getMatlabProxy() throws MatlabConnectionException, TaskException
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
            
            //Have a timeout of 90000 milliseconds = 1.5 minutes
            //If over ssh, double the timeout to 3 minutes
            long timeout = 90000;
            if(CakehatSession.getUserConnectionType() == CakehatSession.ConnectionType.REMOTE)
            {
                timeout *= 2;
            }

            _proxy = factory.getProxy(timeout);
        }

        return _proxy;
    }

    private void runMFiles(List<File> mFiles, File containingDir) throws TaskException
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
        argumentsField.getDocument().addDocumentListener(new DocumentAdapter()
        {
            public void modificationOccurred(DocumentEvent de) { updateEval.run(); }
        });
        functionsComboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) { updateEval.run(); }
        });
        updateEval.run();

        Icon icon = IconLoader.loadIcon(IconSize.s32x32, IconImage.GO_NEXT);
        int result = JOptionPane.showConfirmDialog(null, panel,
                "Run m-file", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
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
                    throw new TaskException("Unable to make MATLAB change directory: " + parentDir, e);
                }

                //An exception in this case is likely due to invalid arguments
                //supplied to the MATLAB function
                try
                {
                    proxy.eval(evalLabel.getText());
                }
                catch(MatlabInvocationException e)
                {
                    ModalDialog.showMessage(null, "Invalid MATLAB Command",
                            "Invalid MATLAB command, please see MATLAB for more information.");;
                }
            }
            catch(MatlabConnectionException e)
            {
                throw new TaskException(e);
            }
        }
    }
}