package cakehat.database.assignment;

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
import java.util.HashSet;
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
import support.utils.FileCopyingException;
import support.utils.FileExistsException;
import support.utils.FileExtensionFilter;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 * Actions that interact with Python
 * 
 * @author Yudi
 */
public class PythonActions implements ActionProvider {
    
    @Override
    public String getNamespace() {
        return "python";
    }

    @Override
    public Set<? extends PartActionDescription> getActionDescriptions() {
        return ImmutableSet.of(new CopyTest(), new RunFile());
    }
    
    private class RunFile extends PartActionDescription {
        private final PartActionProperty FILE_PATH_PROPERTY =
            new PartActionProperty("file-path",
            "Specifies the path relative to handin of the Python file to run.  If this property is not specified or the " +
            "file specified does not exist in the handin then the grader will be asked to select which file to run.",
            false);
        private final PartActionProperty EXTENSIONS_PROPERTY =
            new PartActionProperty("extensions",
            "The extensions of the files to run in this part. To run files that do not have file extensions use an " +
            "underscore. Regardless of extension, the files must be Python files. List extensions in the following " +
            "format (without quotation marks):\n " + 
            "single extension - 'py' \n" +
            "multiple extensions - 'py, pyc' \n" + 
            "If this property is not specified, .py files will be picked by default", false);

        private RunFile() {
            super(PythonActions.this, "run-file");
        }

        @Override
        public String getDescription() {
            return "Runs a single Python file with grader provided arguments. The grader will either be allowed to " +
                   "select which Python file to run from a list of all Python files that are part of this part or the " +
                   "grader will only be able to run the Python file specified by " + FILE_PATH_PROPERTY.getName() + ".";
        }

        @Override
        public Set<PartActionProperty> getProperties() {
            return ImmutableSet.of(FILE_PATH_PROPERTY, EXTENSIONS_PROPERTY);
        }

        @Override
        public Set<ActionType> getSuggestedTypes() {
            return ImmutableSet.of(ActionType.RUN);
        }
        
        @Override
        public boolean requiresDigitalHandin() {
            return true;
        }

        @Override
        public PartAction getAction(final Map<PartActionProperty, String> properties) {
            PartAction action = new SingleGroupPartAction() {
                
                @Override
                public ActionResult performAction(ActionContext context, Part part, Group group) throws ActionException {
                    File unarchiveDir = context.getUnarchiveHandinDir(group);
                    String terminalName = group.getName() + "'s " + part.getFullDisplayName();
                    
                    //If a specific set of file extentions was specified, retrieve the Python files with those 
                    //extentions; otherwise retrieve .py files belonging to this part.
                    FileFilter extensionsFilter;
                    if(properties.containsKey(EXTENSIONS_PROPERTY)) {
                        extensionsFilter = ActionUtilities.parseFileExtensions(properties.get(EXTENSIONS_PROPERTY));
                    }
                    else {
                        extensionsFilter = new FileExtensionFilter("py");
                    }
                    
                    List<File> pythonFiles;

                    //If a specific file to run was specified, attempt to run
                    //that file, but if it does not exist inform the user and
                    //then continue as if the file was not specified
                    if(properties.containsKey(FILE_PATH_PROPERTY)) {
                        String relativePath = properties.get(FILE_PATH_PROPERTY);
                        File absolutePath = new File(unarchiveDir, relativePath);

                        if(!absolutePath.exists()) {
                            ModalDialog.showMessage(context.getGraphicalOwner(), "Specified File Unavailable",
                                    "Specified file to run does not exist.\n\n" +
                                    "Expected file: " + absolutePath.getAbsolutePath() + "\n\n" +
                                    "You will be asked to select from available Python files.");

                            try {
                                pythonFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(unarchiveDir, extensionsFilter, new AlphabeticFileComparator());
                            }
                            catch(IOException e) {
                                throw new ActionException("Unable to access Python files", e);
                            }
                        }
                        else {
                            pythonFiles = new ArrayList<File>();
                            pythonFiles.add(absolutePath);
                        }
                    }
                    else {
                        try {
                            pythonFiles = Allocator.getFileSystemUtilities()
                                    .getFiles(unarchiveDir, extensionsFilter, new AlphabeticFileComparator());
                        }
                        catch(IOException e) {
                            throw new ActionException("Unable to access Python files", e);
                        }
                    }

                    if(pythonFiles.isEmpty()) {
                        ModalDialog.showMessage(context.getGraphicalOwner(), "Unable to run",
                                "There are no Python files for this part.");
                    }
                    else {
                        RunPythonFilesDialog dialog = new RunPythonFilesDialog(context.getGraphicalOwner(),
                                pythonFiles, unarchiveDir);
                        if (dialog.shouldRun()) {
                            runPythonFile(terminalName, dialog.getPythonFile(), dialog.getRunArgs());
                        }
                    }
                    
                    return ActionResult.NO_CHANGES;
                }
            };

            return action;
        } 
    }
    
    private class CopyTest extends PartActionDescription {
        private final PartActionProperty COPY_PATH_PROPERTY =
            new PartActionProperty("copy-path",
            "The fully qualified path to the directory whose entire contents will be copied into the root of the " +
            "unarchived handin directory.",
            true);

        private final PartActionProperty TEST_FILE_PROPERTY =
            new PartActionProperty("test-file",
            "This property must be specified if a directory is provided for the " + COPY_PATH_PROPERTY.getName() +
            " property. The Python file that will be run. The path must be relative to directory specified by the " +
            COPY_PATH_PROPERTY.getName() + " property.",
            false);

        private CopyTest() {
            super(PythonActions.this, "copy-test");
        }

        @Override
        public String getDescription() {
            return "Copies the specified file or contents of the directory into the root of the unarchived handin. " +
                   "Then runs the specified Python file. Once the copy occurs, it remains, and therefore any " +
                   "other actions may interact with the copied files.";
        }

        @Override
        public Set<PartActionProperty> getProperties() {
            return ImmutableSet.of(COPY_PATH_PROPERTY, TEST_FILE_PROPERTY);
        }

        @Override
        public Set<ActionType> getSuggestedTypes() {
            return ImmutableSet.of(ActionType.TEST);
        }
        
        @Override
        public boolean requiresDigitalHandin() {
            return true;
        }

        @Override
        public PartAction getAction(final Map<PartActionProperty, String> properties) {
            PartAction action = new SingleGroupPartAction() {
                //Keeps track of the groups that have already had the files copied
                private HashSet<Group> _testedGroups = new HashSet<Group>();

                @Override
                public ActionResult performAction(ActionContext context, Part part, Group group) throws ActionException {
                    File unarchiveDir = context.getUnarchiveHandinDir(group);
                    String terminalName = group.getName() + "'s " + part.getFullDisplayName();
                    
                    //Copy if necessary
                    if(!_testedGroups.contains(group)) {
                        File source = new File(properties.get(COPY_PATH_PROPERTY));

                        //Validate
                        if(!source.exists()) {
                            ModalDialog.showMessage(context.getGraphicalOwner(), "Does not exist",
                                    "Cannot perform test because the directory or file to copy does not exist.\n\n" +
                                    "Source: " + source.getAbsoluteFile());
                            return ActionResult.NO_CHANGES;
                        }

                        if(source.isDirectory()) {
                            String relativePath = properties.get(TEST_FILE_PROPERTY);

                            if(relativePath == null) {
                                ModalDialog.showMessage(context.getGraphicalOwner(), "Property not set",
                                        "Cannot perform test because the " + TEST_FILE_PROPERTY.getName() +
                                        " property was not set. It must be set when copying test files from a " +
                                        "directory.");
                                return ActionResult.NO_CHANGES;
                            }

                            File testFile = new File(source, relativePath);
                            if(!testFile.exists()) {
                                ModalDialog.showMessage(context.getGraphicalOwner(), "Test file does not exist",
                                        "Cannot perform test because the test file does not exist.\n\n" +
                                        "File: " + testFile.getAbsoluteFile());
                                return ActionResult.NO_CHANGES;
                            }
                            if(!testFile.isFile()) {
                                ModalDialog.showMessage(context.getGraphicalOwner(), "Test file not a file",
                                        "Cannot perform test because the test file is not a file.\n\n" +
                                        "File: " + testFile.getAbsoluteFile());
                                return ActionResult.NO_CHANGES;
                            }
                        }

                        try {
                            File destination;
                            if(source.isFile()) {
                                destination = new File(unarchiveDir, source.getName());
                            }
                            else {
                                destination = unarchiveDir;
                            }

                            Allocator.getFileSystemServices().copy(source, destination,
                                    OverwriteMode.FAIL_ON_EXISTING, false,
                                    FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE);
                            
                            _testedGroups.add(group);
                        }
                        catch(FileCopyingException e) {
                            //If a file that already exists would be overwritten
                            FileExistsException existsException =
                                    Allocator.getGeneralUtilities().findInStack(e, FileExistsException.class);
                            if(existsException != null) {
                                ModalDialog.showMessage(context.getGraphicalOwner(), "Cannot copy test file",
                                    "Cannot perform test because a file to be copied for the test already exists in "+
                                    "the unarchived handin.\n\n" +
                                    "Test File: " + existsException.getSourceFile().getAbsolutePath() + "\n" +
                                    "Handin File: " + existsException.getDestinationFile().getAbsolutePath());
                                return ActionResult.NO_CHANGES;
                            }

                            throw new ActionException("Unable to perform copy necessary for testing.", e);
                        }
                    }

                    //Determine the location of the test file once it has been copied
                    File testFile;
                    File source = new File(properties.get(COPY_PATH_PROPERTY));
                    if(source.isFile()) {
                        testFile = new File(unarchiveDir, source.getName());
                    }
                    else {
                        testFile = new File(unarchiveDir, properties.get(TEST_FILE_PROPERTY));
                    }
                        
                    //Run test file
                    runPythonFile(terminalName, testFile, null);
                    
                    return ActionResult.NO_CHANGES;
                }
            };

            return action;
        }
    }
    
    private void runPythonFile(String title, File pythonFile, String runArgs) throws ActionException {
        //put together the python command
        String cmd = "python ./" + pythonFile.getName();
        if (runArgs != null && !runArgs.isEmpty()) {
            //no argument provided, just run the file
            cmd = cmd + " " + runArgs; 
        }
        
        try {
            Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, cmd, pythonFile.getParentFile());
        }
        catch (IOException e) {
            throw new ActionException("Unable to run python in visible terminal. Command: " + cmd, e);
        }
    }
    
    /**
     * A dialog for selecting the Python file to run, and entering the run arguments.
     */
    private static class RunPythonFilesDialog extends JDialog {
        private GenericJComboBox<String> _pythonFilesComboBox;
        private JTextField _argsField;
        private JLabel _argsValidationLabel;
        private JButton _runButton;
        
        private HashMap<String, File> _relativeToAbsolute;
        private boolean _shouldRun = false;

        private static final int PADDING = 10;
        private static final int TOTAL_WIDTH = 450;
        private static final int TOTAL_HEIGHT = 30*6+2*PADDING;
        
        public RunPythonFilesDialog(Window owner, List<File> pythonFiles, File containingDir) {
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
            for(File pythonFile : pythonFiles) {
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
            _runButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _shouldRun = true;
                    RunPythonFilesDialog.this.dispose();
                }
            });
            buttonPanel.add(_runButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RunPythonFilesDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);
            
            //Padding below the buttons
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)), BorderLayout.SOUTH);

            //Update the _argsValidationLabel when the argumentsField is updated
            final Runnable validateInput = new Runnable() {
                public void run() {
                    boolean valid = true;
                    String problemMessage = "";
                    if(_argsField.getText() != null && !_argsField.getText().isEmpty()) {
                        String runArgs = _argsField.getText();

                        TerminalStringValidity validity = Allocator
                                .getExternalProcessesUtilities().checkTerminalValidity(runArgs);

                        valid = validity.isValid();
                        
                        if (!valid) {
                            if(!validity.isTerminatedProperly()) {
                                problemMessage = "Cannot end with an unescaped backslash (\\)";
                            }
                            else if(!validity.isSingleQuotedProperly()) {
                                problemMessage = "Single quotation (') marks must match or be escaped";
                            }
                            else if(!validity.isDoubleQuotedProperly()) {
                                problemMessage = "Double quotation (\") marks must match or be escaped";
                            }
                            else {
                                problemMessage = "The argument provided is not a valid string in a terminal";
                            }
                        }
                    }
                    
                    _argsValidationLabel.setText(problemMessage);
                    _runButton.setEnabled(valid);
                }
            };
            
            _argsField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                public void modificationOccurred(DocumentEvent de) {
                    validateInput.run();
                }
            });
            validateInput.run();
            
            //If enter key is pressed, close window if argument is valid
            _argsField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "newline");
            _argsField.getActionMap().put("newline", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    //If the button is enabled, the arguments are valid
                    if(_runButton.isEnabled()) {
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
    
        public boolean shouldRun() {
            return _shouldRun;
        }
        
        public File getPythonFile() {
            return _relativeToAbsolute.get(_pythonFilesComboBox.getSelectedItem());
        }
        
        public String getRunArgs() {
            return _argsField.getText();
        }
    }
}