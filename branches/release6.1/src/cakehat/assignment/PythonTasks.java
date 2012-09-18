package cakehat.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
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
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import support.ui.DocumentAdapter;
import support.ui.GenericJComboBox;
import support.ui.ModalDialog;
import support.ui.ShadowJTextField;
import support.utils.AlphabeticFileComparator;
import support.utils.ExternalProcessesUtilities.TerminalStringValidity;
import support.utils.FileExtensionFilter;

/**
 * Tasks that interact with Python
 * 
 * @author Yudi
 * @author jak2
 */
class PythonTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "python";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new RunFile(), new CopyTest());
    }

    private class RunFile extends SingleGroupTask
    {
        private final TaskProperty FILE_PATH_PROPERTY =
                new TaskProperty("file-path",
                "Specifies the path relative to handin of the Python file to run.  If this property is not specified " +
                "or the file specified does not exist in the handin then the grader will be asked to select which " +
                "file to run.", false);
        private final TaskProperty EXTENSIONS_PROPERTY =
                new TaskProperty("extensions",
                "The extensions of the files to run in this part. To run files that do not have file extensions use " +
                "an underscore. Regardless of extension, the files must be Python files. List extensions in the " +
                "following format (without quotation marks):<br>" +
                "single extension - 'py'<br>" +
                "multiple extensions - 'py, pyc'<br>" +
                "If this property is not specified, .py files will be picked by default", false);

        private RunFile()
        {
            super(PythonTasks.this, "run-file");
        }

        @Override
        public String getDescription()
        {
            return "Runs a single Python file with grader provided arguments. The grader will either be allowed to " +
                   "select which Python file to run from a list of all Python files that are part of this part or " +
                   "the grader will only be able to run the Python file specified by " + FILE_PATH_PROPERTY.getName() +
                   ".";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(FILE_PATH_PROPERTY, EXTENSIONS_PROPERTY);
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
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
            String terminalName = group.getName() + "'s " + action.getPart().getFullDisplayName();

            //If a specific set of file extentions was specified, retrieve the Python files with those extentions;
            //otherwise retrieve .py files belonging to this part.
            FileFilter extensionsFilter;
            if(properties.containsKey(EXTENSIONS_PROPERTY))
            {
                extensionsFilter = TaskUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
            }
            else
            {
                extensionsFilter = new FileExtensionFilter("py");
            }

            List<File> pythonFiles;

            //If a specific file to run was specified, attempt to run that file, but if it does not exist inform the
            //user and then continue as if the file was not specified
            if(properties.containsKey(FILE_PATH_PROPERTY))
            {
                String relativePath = properties.get(FILE_PATH_PROPERTY);
                File absolutePath = new File(unarchiveDir, relativePath);

                if(!absolutePath.exists())
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Specified File Unavailable",
                            "Specified file to run does not exist.\n\n" +
                            "Expected file: " + absolutePath.getAbsolutePath() + "\n\n" +
                            "You will be asked to select from available Python files.");

                    try
                    {
                        pythonFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, extensionsFilter,
                                new AlphabeticFileComparator());
                    }
                    catch(IOException e)
                    {
                        throw new TaskException("Unable to access Python files", e);
                    }
                }
                else
                {
                    pythonFiles = new ArrayList<File>();
                    pythonFiles.add(absolutePath);
                }
            }
            else
            {
                try
                {
                    pythonFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, extensionsFilter,
                            new AlphabeticFileComparator());
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to access Python files", e);
                }
            }

            if(pythonFiles.isEmpty())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Unable to run",
                        "There are no Python files for this part.");
            }
            else
            {
                RunPythonFilesDialog dialog = new RunPythonFilesDialog(context.getGraphicalOwner(), pythonFiles,
                        unarchiveDir);
                if(dialog.shouldRun())
                {
                    runPythonFile(terminalName, dialog.getPythonFile(), dialog.getRunArgs());
                }
            }
        }
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
                " property. The absolute path to the Python file that will be run. This file must be contained in " +
                "the directory provided for the " + COPY_PATH_PROPERTY.getName() + " property.", false);

        private CopyTest()
        {
            super(PythonTasks.this, "copy-test");
        }

        @Override
        public String getDescription()
        {
            return "Copies the specified file or contents of the directory into the root of a copy of the unarchived " +
                    "handin. Then runs the specified Python file in a visible terminal.";
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
                runPythonFile(group.getName() + "'s " + action.getPart().getFullDisplayName(), dstTestFile, null);
            }
        }
    }

    private void runPythonFile(String title, File pythonFile, String runArgs) throws TaskException
    {
        //put together the python command
        String cmd = "python ./" + pythonFile.getName();
        if(runArgs != null && !runArgs.isEmpty())
        {
            //no argument provided, just run the file
            cmd = cmd + " " + runArgs;
        }

        try
        {
            Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, cmd, pythonFile.getParentFile());
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to run python in visible terminal. Command: " + cmd, e);
        }
    }

    /**
     * A dialog for selecting the Python file to run, and entering the run arguments.
     */
    private static class RunPythonFilesDialog extends JDialog
    {
        private GenericJComboBox<String> _pythonFilesComboBox;
        private JTextField _argsField;
        private JLabel _argsValidationLabel;
        private JButton _runButton;
        private HashMap<String, File> _relativeToAbsolute;
        private boolean _shouldRun = false;
        private static final int PADDING = 10;
        private static final int TOTAL_WIDTH = 450;
        private static final int TOTAL_HEIGHT = 30 * 6 + 2 * PADDING;

        public RunPythonFilesDialog(Window owner, List<File> pythonFiles, File containingDir)
        {
            super(owner, "Run Options");

            this.getContentPane().setLayout(new BorderLayout(0, 0));
            this.getContentPane().setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));

            //Padding
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)), BorderLayout.NORTH);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, TOTAL_HEIGHT)), BorderLayout.WEST);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, TOTAL_HEIGHT)), BorderLayout.EAST);

            //Content
            JPanel contentPanel = new JPanel(new GridLayout(0, 1));
            this.getContentPane().add(contentPanel, BorderLayout.CENTER);

            //Create paths that are relative to the unarchive directory
            //These paths will be dislayed to the grader
            _relativeToAbsolute = new HashMap<String, File>();
            ArrayList<String> relativePaths = new ArrayList<String>();
            for(File pythonFile : pythonFiles)
            {
                String relativePath = pythonFile.getAbsolutePath();
                relativePath = relativePath.replace(containingDir.getAbsolutePath(), "");
                _relativeToAbsolute.put(relativePath, pythonFile);
                relativePaths.add(relativePath);
            }
            contentPanel.add(new JLabel("Choose the Python file to be run:"));
            _pythonFilesComboBox = new GenericJComboBox<String>(relativePaths);
            contentPanel.add(_pythonFilesComboBox);

            //run arguments
            contentPanel.add(new JLabel("Command line arguments:"));
            _argsField = new ShadowJTextField("Command line arguments (optional)");
            _argsField.setColumns(30);
            contentPanel.add(_argsField);

            _argsValidationLabel = new JLabel("");
            _argsValidationLabel.setForeground(Color.RED);
            contentPanel.add(_argsValidationLabel);

            //buttons
            JPanel buttonPanel = new JPanel();
            contentPanel.add(buttonPanel);

            _runButton = new JButton("Run");
            _runButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    _shouldRun = true;
                    RunPythonFilesDialog.this.dispose();
                }
            });
            buttonPanel.add(_runButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    RunPythonFilesDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);

            //Padding below the buttons
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)), BorderLayout.SOUTH);

            //Update the _argsValidationLabel when the argumentsField is updated
            final Runnable validateInput = new Runnable()
            {
                public void run()
                {
                    boolean valid = true;
                    String problemMessage = "";
                    if(_argsField.getText() != null && !_argsField.getText().isEmpty())
                    {
                        String runArgs = _argsField.getText();

                        TerminalStringValidity validity = Allocator.getExternalProcessesUtilities()
                                .checkTerminalValidity(runArgs);

                        valid = validity.isValid();

                        if(!valid)
                        {
                            if(!validity.isTerminatedProperly())
                            {
                                problemMessage = "Cannot end with an unescaped backslash (\\)";
                            }
                            else if(!validity.isSingleQuotedProperly())
                            {
                                problemMessage = "Single quotation (') marks must match or be escaped";
                            }
                            else if(!validity.isDoubleQuotedProperly())
                            {
                                problemMessage = "Double quotation (\") marks must match or be escaped";
                            }
                            else
                            {
                                problemMessage = "The argument provided is not a valid string in a terminal";
                            }
                        }
                    }

                    _argsValidationLabel.setText(problemMessage);
                    _runButton.setEnabled(valid);
                }
            };

            _argsField.getDocument().addDocumentListener(new DocumentAdapter()
            {
                @Override
                public void modificationOccurred(DocumentEvent de)
                {
                    validateInput.run();
                }
            });
            validateInput.run();

            //If enter key is pressed, close window if argument is valid
            _argsField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "newline");
            _argsField.getActionMap().put("newline", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    //If the button is enabled, the arguments are valid
                    if(_runButton.isEnabled())
                    {
                        _shouldRun = true;
                        RunPythonFilesDialog.this.dispose();
                    }
                }
            });

            this.setModal(true);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.pack();
            this.setResizable(false);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }

        public boolean shouldRun()
        {
            return _shouldRun;
        }

        public File getPythonFile()
        {
            return _relativeToAbsolute.get(_pythonFilesComboBox.getSelectedItem());
        }

        public String getRunArgs()
        {
            return _argsField.getText();
        }
    }
}