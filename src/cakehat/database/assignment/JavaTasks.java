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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import support.ui.DocumentAdapter;
import support.ui.GenericJComboBox;
import support.ui.ModalDialog;
import support.utils.ExternalProcessesUtilities.TerminalStringValidity;
import support.utils.FileCopyingException;
import support.utils.FileExistsException;
import support.utils.FileExtensionFilter;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 * Tasks for the Java programming language. Compilation of Java is done via the {@link javax.tools.JavaCompiler}
 * provided by {@link javax.tools.ToolProvider}. Analysis of Java class files is done using javassist (Java
 * Programming Assistant).
 *
 * @author jak2
 */
class JavaTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "java";
    }
    
    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new RunMain(), new JarDemo(), new ClassDemo(), new CopyTest());
    }

    private class CopyTest extends SingleGroupTask
    {
        private final TaskProperty COPY_PATH_PROPERTY =
            new TaskProperty("copy-path",
            "The fully qualified path to the file or directory that will be copied into the root of the unarchived " +
            "handin directory. If it is a directory the entire contents of the directory will be copied, but not the "+
            "directory itself.", true);
        private final TaskProperty MAIN_PROPERTY =
            new TaskProperty("test-main",
            "The full path to the main class including package: ex. cakehat.gui.Main\n" +
            "It is expected that this class will be a class copied into the handin, but that is not required.", true);
        private final TaskProperty CLASS_PATH_PROPERTY =
            new TaskProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        private final TaskProperty LIBRARY_PATH_PROPERTY =
            new TaskProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);

        private CopyTest()
        {
            super(JavaTasks.this, "copy-compile-run");
        }

        @Override
        public String getDescription()
        {
            return "Copies the specified file or contents of the directory into  the root of the unarchived handin. " +
                   "Recompiles the code and  then runs the specified main in a visible terminal. Once  the copy " +
                   "occurs, the copied files and directories remain, and therefore any other tasks may interact " +
                   "with them.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(COPY_PATH_PROPERTY, MAIN_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY);
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

        //Keeps track of the groups that have already had the files copied
        //private HashSet<Group> _testedGroups = new HashSet<Group>();
        
        @Override
        TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part, Group group)
                throws TaskException
        {
            File unarchiveDir = context.getUnarchiveHandinDir(group);

            //Copy if necessary
            TaskResult result;
            if(!context.getFilesAddedForTask(group).isEmpty())
            {
                File source = new File(properties.get(COPY_PATH_PROPERTY));

                //Validate
                if(!source.exists())
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Does not exist",
                            "Cannot perform test because the directory or file to copy does not exist.\n\n" +
                            "Source: " + source.getAbsoluteFile());
                    result = TaskResult.NO_CHANGES;
                }
                else
                {
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

                        List<File> filesAdded = Allocator.getFileSystemServices().copy(source, destination,
                                OverwriteMode.FAIL_ON_EXISTING, false, FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE);
                        result = new TaskResult(group, new HashSet<File>(filesAdded));
                    }
                    catch(FileCopyingException e)
                    {
                        //If a file that already exists would be overwritten
                        FileExistsException existsException =
                                Allocator.getGeneralUtilities().findInStack(e, FileExistsException.class);
                        
                        if(existsException == null)
                        {
                            throw new TaskException("Unable to perform copy necessary for testing.", e);
                        }
                        else
                        {
                            ModalDialog.showMessage(context.getGraphicalOwner(), "Cannot copy test file",
                                "Cannot perform test because a file to be copied for the test already exists in " +
                                "the unarchived handin.\n\n" +
                                "Test File: " + existsException.getSourceFile().getAbsolutePath() + "\n" +
                                "Handin File: " + existsException.getDestinationFile().getAbsolutePath());
                            result = TaskResult.NO_CHANGES;
                        }
                    }
                }
            }
            else
            {
                result = TaskResult.NO_CHANGES;
            }

            //Deleted any already compiled files
            deleteCompiledFiles(unarchiveDir);

            //Compile, compilation will fail if the code has compilation errors
            if(compileJava(unarchiveDir, properties.get(CLASS_PATH_PROPERTY)))
            {
                //Get all of the main classes
                List<ClassInfo> mainClasses = getMainClasses(unarchiveDir);

                //Check if the main specified actually exists
                String mainName = properties.get(MAIN_PROPERTY);
                ClassInfo mainToRun = null;
                for(ClassInfo info : mainClasses)
                {
                    if(info.getClassName().equals(mainName))
                    {
                        mainToRun = info;
                        break;
                    }
                }

                //If none was found, inform the grader
                if(mainToRun == null)
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Main not present",
                            "The specified main class is not present.\n" +
                            "Specified main: " + mainName);
                }
                //Run main class
                else
                {
                    String terminalName = "Testing " + group.getName() + "'s " + part.getFullDisplayName();
                    executeJavaInVisibleTerminal(mainToRun,
                             properties.get(CLASS_PATH_PROPERTY),
                             properties.get(LIBRARY_PATH_PROPERTY),
                             terminalName,
                             null,
                             unarchiveDir);
                }
            }

            return result;
        }
    }

    private abstract class Demo extends NoGroupTask
    {
        protected final TaskProperty CLASS_PATH_PROPERTY =
            new TaskProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        protected final TaskProperty LIBRARY_PATH_PROPERTY =
            new TaskProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);
        protected final TaskProperty SHOW_TERMINAL_PROPERTY =
            new TaskProperty("show-terminal",
            "By default no terminal is shown when running the jar. Set the value of this property to TRUE to provide " +
            "a terminal the grader can interact with.", false);
        protected final TaskProperty TERMINAL_TITLE_PROPERTY =
            new TaskProperty("terminal-name",
            "If a terminal is shown the value of this property will be displayed as the title of the terminal. If " +
            "this value is not set the terminal's title will be '[Assignment Name] Demo'", false);
        protected final TaskProperty RUN_ARGS_PROPERTY =
             new TaskProperty("provide-args",
             "By default the demo will be run without any arguments. Set this property to TRUE to allow for the " +
             "grader to provide run arguments to the Java program.", false);

        Demo(String taskName)
        {
            super(JavaTasks.this, taskName);
        }

        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.DEMO);
        }

        //cmdEnd must not be null
        protected void runDemo(Map<TaskProperty, String> properties, String classpath, Part part, String cmdEnd)
                throws TaskException
        {
            //Build command
            String cmd = "java ";

            //Add java.library.path component if an arguement was passed in
            if(properties.containsKey(LIBRARY_PATH_PROPERTY))
            {
                cmd += " -Djava.library.path=" + properties.get(LIBRARY_PATH_PROPERTY);
            }

            //Add classpath if it has it
            if(classpath != null)
            {
                cmd += " -classpath " + "'" + classpath + "'";
            }

            cmd += cmdEnd;

            //Determine if this should be run in a visible terminal or not
            File workspace = Allocator.getPathServices().getUserWorkspaceDir();
            if(properties.containsKey(SHOW_TERMINAL_PROPERTY) &&
               properties.get(SHOW_TERMINAL_PROPERTY).equalsIgnoreCase("true"))
            {
                String title = part.getFullDisplayName() + " Demo";
                if(properties.containsKey(TERMINAL_TITLE_PROPERTY))
                {
                    title = properties.get(TERMINAL_TITLE_PROPERTY);
                }
                try
                {
                    Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, cmd, workspace);
                }
                catch (IOException e)
                {
                    throw new TaskException("Unable to run demo in visible terminal for part: " +
                            part.getFullDisplayName(), e);
                }
            }
            else
            {
                try
                {
                    Allocator.getExternalProcessesUtilities().executeAsynchronously(cmd, workspace);
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to run demo for part: " + part.getFullDisplayName(), e);
                }
            }
        }
    }

    private class JarDemo extends Demo
    {
        private final TaskProperty LOCATION_PROPERTY =
            new TaskProperty("location",
            "The absolute path to the jar file.", true);

        private JarDemo()
        {
            super("demo-jar");
        }

        @Override
        public String getDescription()
        {
            return "Runs a Java jar file.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(LOCATION_PROPERTY, SHOW_TERMINAL_PROPERTY,
                    TERMINAL_TITLE_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY,
                    RUN_ARGS_PROPERTY);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }

        @Override
        TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part)
                throws TaskException
        {
            String cmdEnd = " -jar " + properties.get(LOCATION_PROPERTY);
                    
            //run information
            boolean shouldRun = true;

            //get run arguments, if applicable
            boolean provideArgs = "TRUE".equalsIgnoreCase(properties.get(RUN_ARGS_PROPERTY));
            if(provideArgs)
            {
                ExecuteDialog dialog = new ExecuteDialog(context.getGraphicalOwner());
                shouldRun = dialog.shouldRun();
                cmdEnd += " " + dialog.getRunArguments();
            }

            if(shouldRun)
            {
                runDemo(properties, properties.get(CLASS_PATH_PROPERTY), part, cmdEnd);
            }

            return TaskResult.NO_CHANGES;
        }
    }

    private class ClassDemo extends Demo
    {
        private final TaskProperty LOCATION_PROPERTY =
            new TaskProperty("location",
            "The absolute path to the top directory containing the Java class files.", true);
        private final TaskProperty MAIN_PROPERTY =
            new TaskProperty("main",
            "The full path to the main class including package: ex. cakehat.gui.Main", true);

        private ClassDemo()
        {
            super("demo-class");
        }

        @Override
        public String getDescription()
        {
            return "Runs compiled Java class files.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(LOCATION_PROPERTY, MAIN_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY,
                CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY, RUN_ARGS_PROPERTY);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }
        
        
        @Override
        TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part)
                throws TaskException
        {
            String classpath = properties.get(LOCATION_PROPERTY);
            if(properties.containsKey(CLASS_PATH_PROPERTY))
            {
                classpath += ":" + properties.get(CLASS_PATH_PROPERTY);
            }

            String cmdEnd = " " + properties.get(MAIN_PROPERTY);

            //run information
            boolean shouldRun = true;

            //get run arguments, if applicable
            boolean provideArgs = "TRUE".equalsIgnoreCase(properties.get(RUN_ARGS_PROPERTY));
            if(provideArgs)
            {
                ExecuteDialog dialog = new ExecuteDialog(context.getGraphicalOwner());
                shouldRun = dialog.shouldRun();
                cmdEnd += " " + dialog.getRunArguments();
            }

            if(shouldRun)
            {
                runDemo(properties, classpath, part, cmdEnd);
            }

            return TaskResult.NO_CHANGES;
        }
    }

    private class RunMain extends SingleGroupTask
    {
        private final TaskProperty CLASS_PATH_PROPERTY =
            new TaskProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        private final TaskProperty LIBRARY_PATH_PROPERTY =
            new TaskProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);
        private final TaskProperty RUN_ARGS_PROPERTY =
             new TaskProperty("provide-args",
             "By default the main method will be run without any arguments.  Set this property to TRUE to allow for " +
             "the grader to provide run arguments to the Java program.", false);

        private RunMain()
        {
            super(JavaTasks.this, "compile-and-run");
        }

        @Override
        public String getDescription()
        {
            return "Compiles and runs Java code using a visible terminal the grader can interact with.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY, RUN_ARGS_PROPERTY);
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
        TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part, Group group)
                throws TaskException
        {
            File unarchiveDir = context.getUnarchiveHandinDir(group);

            //Deleted any already compiled files
            deleteCompiledFiles(unarchiveDir);

            //Compile, compilation will fail if the code has compilation errors
            if(compileJava(unarchiveDir, properties.get(CLASS_PATH_PROPERTY)))
            {
                List<ClassInfo> mainClasses = getMainClasses(unarchiveDir);

                if(mainClasses.isEmpty())
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Not Available",
                            "No main class is available to run.");
                }
                else
                {
                    // Whether the user should provide the run argument
                    boolean provideArgs = "TRUE".equalsIgnoreCase(properties.get(RUN_ARGS_PROPERTY));

                    // Get run information
                    boolean shouldRun;
                    ClassInfo mainToRun;
                    String runArgs;

                    // If only one main class and no run arguments are to be provided
                    if(mainClasses.size() == 1 && !provideArgs)
                    {
                        shouldRun = true;
                        mainToRun = mainClasses.get(0);
                        runArgs = null;
                    }
                    // Prompt user for the main class and run arguments
                    else
                    {
                        ExecuteDialog dialog = new ExecuteDialog(context.getGraphicalOwner(), mainClasses,
                                provideArgs);
                        shouldRun = dialog.shouldRun();
                        mainToRun = dialog.getSelectedMain();
                        runArgs = dialog.getRunArguments();
                    }

                    // Run compiled code
                    if(shouldRun)
                    {
                        String terminalName = group.getName() + "'s " + part.getFullDisplayName();
                        executeJavaInVisibleTerminal(mainToRun,
                                 properties.get(CLASS_PATH_PROPERTY),
                                 properties.get(LIBRARY_PATH_PROPERTY),
                                 terminalName,
                                 runArgs,
                                 unarchiveDir);
                    }
                }
            }

            return TaskResult.NO_CHANGES;
        }
    }

    /******************************************************************************************************************\
    |*                                                 Shared Methods                                                 *|
    \******************************************************************************************************************/

    /**
     * Deletes all compiled files in a group's project. It is safe to run even if there are no compiled files in the
     * project.
     *
     * @param unarchiveDir
     */
    private static void deleteCompiledFiles(File unarchiveDir) throws TaskException
    {
        try
        {
            Set<File> compiledFiles = Allocator.getFileSystemUtilities().
                getFiles(unarchiveDir, new FileExtensionFilter("class"));
            Allocator.getFileSystemUtilities().deleteFiles(compiledFiles);
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to delete existing class files", e);
        }
    }

    /**
     * Returns a classpath with all of the jars that are in {@code unarchiveDir} appended to {@code classpath}. If there
     * are no jars in {@code unarchiveDir} then unaltered classpath will be returned.
     *
     * @param unarchiveDir
     * @param classpath may be {@code null}
     * @return
     * @throws TaskException
     */
    private static String addJarsToClasspath(File unarchiveDir, String classpath) throws TaskException
    {
        Set<File> jarFiles;
        try
        {
            jarFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, new FileExtensionFilter("jar"));
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to add jars, if present, to classpath", e);
        }

        for(File jarFile : jarFiles)
        {
            if(classpath == null)
            {
                classpath = jarFile.getAbsolutePath();
            }
            else
            {
                classpath += ":" + jarFile.getAbsolutePath();
            }
        }

        return classpath;
    }
    
    /**
     * Compiles Java code, returns whether the code compiled successfully.
     * <br/><br/>
     * Any compiler errors or other messages will be displayed to the grader.
     * <br/><br/>
     * Failure to compile code does not indicate an issue with this method, it may be that code contains errors that
     * prevent it from compiling. If this is the case {@code false} will be returned but no exception will be thrown.
     *
     * @param unarchiveDir Java files in this directory and subdirectories will be compiled
     * @param classpath the classpath to compile this code with, may be null
     *
     * @return success of compilation
     */
    private static boolean compileJava(File unarchiveDir, String classpath) throws TaskException
    {
        //Get java compiler and file manager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //Add any jars in the part to the classpath
        classpath = addJarsToClasspath(unarchiveDir, classpath);

        //Tell the compiler to use the classpath if one is present
        Collection<String> options = null;
        if(classpath != null)
        {
            options = Arrays.asList("-classpath", classpath);
        }

        //Listens to errors
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();

        //Get all of the Java files that are allowed by the inclusion filter
        FileFilter javaFilter = new FileExtensionFilter("java");
        
        Set<File> files;
        try
        {
            files = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, javaFilter);
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to access java files", e);
        }

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        //Compile
        Boolean successObj = compiler.getTask(null, fileManager, collector, options, null, compilationUnits).call();
        boolean success = (successObj != null && successObj);

        //If not successful, display compiler message
        if(!success)
        {
            //Display compiler information
            String msg = "Compiler output: \n\n";
            for(int i = 0; i < collector.getDiagnostics().size(); i++)
            {
                msg += collector.getDiagnostics().get(i).toString();

                //If not the last entry, append a new line
                if(i != collector.getDiagnostics().size() - 1)
                {
                    msg += "\n";
                }
            }
            ModalDialog.showMessage(null, "Compilation Failed", msg);
        }

        try
        {
            fileManager.close();
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to close file manager used by Java  compiler. Please try again, this " +
                    "may be a result of issues with the network file system.", e);
        }

        return success;
    }

    /**
     * Executes Java code in a seperate visible terminal. The code must first be compiled.
     *
     * @param mainClass information about the main class
     * @param classpath the classpath to run this code with respect to, may be {@code null}
     * @param libraryPath the library path to run this code with respect to, may be {@code null}
     * @param termName the title bar displayed by the terminal
     * @param runArgs the arguments provided to the Java program, may be {@code null}
     * @param unarchiveDir the directory the part was unarchived into
     */
    private static void executeJavaInVisibleTerminal(ClassInfo mainClass, String classpath, String libraryPath,
            String termName, String runArgs, File unarchiveDir) throws TaskException
    {
        //Build java.library.path component if an argument was passed in
        String javaLibrary = "";
        if(libraryPath != null)
        {
            javaLibrary = " -Djava.library.path=" + libraryPath;
        }

        //Add to classpath the directory containing the root package of the main class
        if(classpath == null)
        {
            classpath = mainClass.getRootPackageDirectory();
        }
        else
        {
            classpath = mainClass.getRootPackageDirectory() + ":" +  classpath;
        }
        
        //Append to classpath all jars in the part
        classpath = addJarsToClasspath(unarchiveDir, classpath);

        //Build classpath argument of the java command
        String javaClassPath = " -classpath " + "'" + classpath + "'";

        //Build run arguments
        String javaRunArgs = "";
        if(runArgs != null)
        {
            javaRunArgs = " " + runArgs;
        }

        //Main class to run
        String javaMainClass = " " + mainClass.getClassName();

        //Put together entire java comand
        String javaCmd = "java" + javaLibrary + javaClassPath + javaMainClass + javaRunArgs;

        try
        {
            Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(termName, javaCmd, unarchiveDir);
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to execute Java in visible terminal. Command: " + javaCmd, e);
        }
    }

    /**
     * A dialog for selecting the main class, if applicable, and the run arguments,
     * if applicable, for either a JAR or a class file to be run.
     */
    private static class ExecuteDialog extends JDialog
    {
        private GenericJComboBox<ClassInfo> _mainClassesBox;
        private JTextField _runArgsField;
        private JLabel _argsValidationLabel;
        private JButton _runButton;

        private boolean _shouldRun = false;

        private static final int TOTAL_WIDTH = 450;
        private static final int ELEMENT_HEIGHT = 30;
        private static final int PADDING = 10;
        
        /**
         * Constructor for creating an ExecuteDialog that only allows providing run arguments, not selecting a main
         * class.
         */
        public ExecuteDialog(Window owner)
        {
            this(owner, null, true);
        }
        
        /**
         * @param mainClasses if not null, the dialog will include a drop-down to
         *                     select the main class that should be run
         * @param provideRunArgs- if true, the dialog will include a text field for
         *                        entering run arguments
         */
        public ExecuteDialog(Window owner, List<ClassInfo> mainClasses, boolean provideRunArgs)
        {
            super(owner, "Run Options", ModalityType.APPLICATION_MODAL);
            
            this.getContentPane().setLayout(new BorderLayout(0, 0));

            // Determine the number of elements that will be displayed
            int numElements = 1; //For the button panel
            if(mainClasses != null)
            {
                numElements += 2;
            }
            if(provideRunArgs)
            {
                numElements += 3;
            }
            int totalElementHeight = numElements * ELEMENT_HEIGHT;
            int totalHeight = totalElementHeight + 2 * PADDING;
            this.getContentPane().setPreferredSize(new Dimension(TOTAL_WIDTH, totalHeight));

            //Padding
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)), BorderLayout.NORTH);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, totalHeight)), BorderLayout.WEST);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, totalHeight)), BorderLayout.EAST);

            //Content
            JPanel contentPanel = new JPanel(new GridLayout(numElements, 1, 0, 0));
            this.getContentPane().add(contentPanel, BorderLayout.CENTER);

            if(mainClasses != null)
            {
                contentPanel.add(new JLabel("Choose the class containing the mainline to be run:"));

                _mainClassesBox = new GenericJComboBox<ClassInfo>(mainClasses);
                contentPanel.add(_mainClassesBox);
            }

            if(provideRunArgs)
            {
                contentPanel.add(new JLabel("Run arguments:"));
                _runArgsField = new JTextField();
                
                //Validate input
                _runArgsField.getDocument().addDocumentListener(new DocumentAdapter()
                {
                    @Override
                    public void modificationOccurred(DocumentEvent de)
                    {
                        validateInput();
                    }
                });
                //If enter key is pressed, close window if argument is valid
                _runArgsField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "newline");
                _runArgsField.getActionMap().put("newline", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        //If the button is enabled, the arguments are valid
                        if(_runButton.isEnabled())
                        {
                            _shouldRun = true;
                            ExecuteDialog.this.dispose();
                        }
                    }
                });

                _runArgsField.setColumns(30);    
                contentPanel.add(_runArgsField);

                _argsValidationLabel = new JLabel();
                _argsValidationLabel.setForeground(Color.RED);
                contentPanel.add(_argsValidationLabel);
            }

            JPanel buttonPanel = new JPanel();
            contentPanel.add(buttonPanel);
            
            _runButton = new JButton("Run");
            _runButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    _shouldRun = true;
                    ExecuteDialog.this.dispose();
                }
            });
            buttonPanel.add(_runButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ExecuteDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);
                        
            //Padding below the buttons
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)), BorderLayout.SOUTH);

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

        public ClassInfo getSelectedMain()
        {
            return _mainClassesBox.getSelectedItem();
        }

        public String getRunArguments()
        {
            return _runArgsField == null ? "" : _runArgsField.getText();
        }

        /**
         * Arguments must be provided in a way that is acceptable in a terminal
         * If they are not, then running the Java program would fail silently.
         * For details see
         * {@link ExternalProcessesUtilities#checkTerminalValidity(java.lang.String)}.
         */
        private void validateInput()
        {
            String runArgs = _runArgsField.getText();
            boolean valid = true;
            String problemMessage = null;

            if(runArgs != null)
            {
                TerminalStringValidity validity = Allocator
                        .getExternalProcessesUtilities().checkTerminalValidity(runArgs);

                valid = validity.isValid();

                //Only one problem at a time will be shown, so the ordering of
                //this is somewhat arbitrary
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
            }

            _runButton.setEnabled(valid);
            if(valid)
            {
                _argsValidationLabel.setText("");
            }
            else
            {
                _argsValidationLabel.setText(problemMessage);
            }
        }
    }

    /**************************************************************************\
    |*                           Bytecode Parsing                             *|
    \**************************************************************************/

    /**
     * Returns information about all main classes. This list can be empty if there are no main classes.
     * <br/><br/>
     * <strong>Note:</strong> no main classes will be returned if the code is not compiled
     *
     * @param unarchiveDir
     * @return
     * @throws TaskException
     */
    private static List<ClassInfo> getMainClasses(File unarchiveDir) throws TaskException
    {
        Set<File> classFiles;
        try
        {
            classFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, new FileExtensionFilter("class"));
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to access class files", e);
        }

        List<ClassInfo> mainInfo = new ArrayList<ClassInfo>();
        for(File classFile : classFiles)
        {
            ClassInfo info = getClassInfo(classFile);

            if(info.isMainClass())
            {
                mainInfo.add(info);
            }
        }

        return mainInfo;
    }

    /**
     * Returns information on all non-nested classes that have methods annotated with JUnit's {@code org.junit.Test}.
     *
     * @param unarchiveDir
     * @return
     * @throws TaskException
     */
    private static List<ClassInfo> getJUnitClasses(File unarchiveDir) throws TaskException
    {
        Set<File> classFiles;
        try
        {
            classFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, new FileExtensionFilter("class"));
        }
        catch (IOException e)
        {
            throw new TaskException("Unable to access class files", e);
        }

        List<ClassInfo> junitInfo = new ArrayList<ClassInfo>();
        for(File classFile : classFiles)
        {
            ClassInfo info = getClassInfo(classFile);

            if(info.hasJUnitTests())
            {
                junitInfo.add(info);
            }
        }

        return junitInfo;
    }

    private static ClassInfo getClassInfo(File file) throws TaskException
    {
        ClassFile cFile;
        try
        {
            cFile = new ClassFile(new DataInputStream(new FileInputStream(file)));
        }
        catch(IOException e)
        {
            throw new TaskException("Unable to load class information for: " + file.getAbsolutePath(), e);
        }

        //Determine the path of the directory containing the root package of this class
        String relativePath = cFile.getName().replace('.', '/') + ".class";
        String rootPackageDir = file.getAbsolutePath().replace(relativePath, "");

        //It is legal for a class to have a $ in the class name, therefore determining the true
        //fully qualified name of a nested class is more complicated than just replacing all
        //instances of $ to .
        String fullyQualifiedName = cFile.getName();
        boolean isNested = (cFile.getInnerAccessFlags() != -1);
        boolean isAnonymous = false;
        if(isNested)
        {
            isAnonymous = (cFile.getInnerAccessFlags() == 0);

            String packageName = "";
            if(fullyQualifiedName.contains("."))
            {
                packageName = fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.')) + ".";
            }

            String enclosingClass = packageName + cFile.getSourceFile().replace(".java", "");

            String nestedEnding = fullyQualifiedName.replace(enclosingClass, "");
            String convertedEnding = nestedEnding.replace('$', '.');

            fullyQualifiedName = enclosingClass + convertedEnding;
        }

        boolean hasMain = false;
        boolean hasJUnitTest = false;
        
        //This unchecked conversion is required because the javassist API does not use generics
        @SuppressWarnings(value="unchecked")
        List<MethodInfo> methods = cFile.getMethods();
        
        for(MethodInfo method : methods)
        {
            //A nested class cannot be a main class even if it has a main method
            if(!isNested && isMainMethod(method))
            {
                hasMain = true;
            }

            //JUnit cannot run tests in a nested class
            //(If a nested class is provided to JUnit it attempts to run the
            //tests of the outer class)
            if(!isNested && isJUnitTestMethod(method))
            {
                hasJUnitTest = true;
            }
        }

        return new ClassInfo(fullyQualifiedName, rootPackageDir, hasMain, hasJUnitTest, isNested, isAnonymous);
    }

    /**
     * If the method is the main method:
     * <pre>
     * {@code
     * public static void main(String[] args)
     * }
     * </pre>
     *
     * @param method
     * @return
     */
    private static boolean isMainMethod(MethodInfo method)
    {
        int accflags = method.getAccessFlags();
        boolean isStatic = ((accflags & AccessFlag.STATIC) != 0);
        boolean isPublic = AccessFlag.isPublic(accflags);

        boolean isMain = (isStatic && isPublic &&  method.getName().equals("main") &&
                        method.getDescriptor().equals("([Ljava/lang/String;)V"));

        return isMain;
    }

    /**
     * Determines if {@code method} is annotated with JUnit's {@code org.junit.Test} annotation.
     *
     * @param method
     * @return
     */
    private static boolean isJUnitTestMethod(MethodInfo method)
    {
        AnnotationsAttribute attr = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);

        boolean hasJUnitTestAnnotation = false;
        if(attr != null)
        {
            for(Annotation annotation : attr.getAnnotations())
            {
                if(annotation.getTypeName().equals("org.junit.Test"))
                {
                    hasJUnitTestAnnotation = true;
                    break;
                }
            }
        }

        return hasJUnitTestAnnotation;
    }

    /**
     * Information about a Java class based off information from the .class file.
     */
    private static class ClassInfo
    {
        private final String _className;
        private final String _rootPackageDir;
        private final boolean _isMainClass;
        private final boolean _hasJUnitTests;
        private final boolean _isNested;
        private final boolean _isAnonymous;

        public ClassInfo(String className, String rootPackageDir, boolean isMainClass,
        		boolean hasJUnitTests, boolean isNested, boolean isAnonymous)
        {
            _className = className;
            _rootPackageDir = rootPackageDir;
            _isMainClass = isMainClass;
            _hasJUnitTests = hasJUnitTests;
            _isNested = isNested;
            _isAnonymous = isAnonymous;
        }

        /**
         * Fully qualified name of the class, e.g. {@code org.apache.utils.IOHelper}
         *
         * @return
         */
        public String getClassName()
        {
            return _className;
        }

        /**
         * The directory containing the root package for this class.
         *
         * @return
         */
        public String getRootPackageDirectory()
        {
            return _rootPackageDir;
        }

        /**
         * Whether this class is not a nested class and has the main method:
         * <pre>
         * {@code
         * public static void main(String[] args)
         * }
         * </pre>
         *
         * @return
         */
        public boolean isMainClass()
        {
            return _isMainClass;
        }

        /**
         * Whether this class has any JUnit test methods.
         *
         * @return
         */
        public boolean hasJUnitTests()
        {
            return _hasJUnitTests;
        }

        /**
         * If a nested class.
         *
         * @return
         */
        public boolean isNested()
        {
            return _isNested;
        }

        /**
         * If an anonymous class, must be a nested class to be an anonymous class.
         *
         * @return
         */
        public boolean isAnonymous()
        {
            return _isAnonymous;
        }

        @Override
        public String toString()
        {
            return _className;
        }
    }
}