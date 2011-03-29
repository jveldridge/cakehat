package cakehat.handin;

import com.google.common.collect.ImmutableList;
import cakehat.Allocator;
import support.ui.GenericJComboBox;
import support.ui.ShadowJTextField;
import cakehat.database.Group;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import support.utils.ExternalProcessesUtilities;
import support.utils.ExternalProcessesUtilities.TerminalStringValidity;
import support.utils.FileCopyingException;
import support.utils.FileExistsException;
import support.utils.FileExtensionFilter;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 * Actions for the Java programming language. Compilation of Java is done via
 * the {@link javax.tools.JavaCompiler} provided by
 * {@link javax.tools.ToolProvider}. Analysis of Java class files is done using
 * BCEL (Byte Code Engineering Library) to determine main classes and a
 * class's package.
 *
 * @author jak2
 */
class JavaActions implements ActionProvider
{
    public List<DistributableActionDescription> getActionDescriptions()
    {
        return ImmutableList.of(new RunMain(), new JarDemo(), new ClassDemo(), new CopyTest());
    }

    public String getNamespace()
    {
        return "java";
    }

    private class CopyTest implements DistributableActionDescription
    {
        private final DistributableActionProperty COPY_PATH_PROPERTY =
            new DistributableActionProperty("copy-path",
            "The fully qualified path to the file or directory that will be" +
            "copied into the root of the unarchived handin directory. If it is" +
            "a directory the entire contents of the directory will be copied," +
            "but not the directory itself.", true);
        private final DistributableActionProperty MAIN_PROPERTY =
            new DistributableActionProperty("test-main",
            "The full path to the main class including package: ex. cakehat.gui.Main \n" +
            "It is expected that this class will be a class copied into the " +
            "handin, but that is not required.", true);
        private final DistributableActionProperty CLASS_PATH_PROPERTY =
            new DistributableActionProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        private final DistributableActionProperty LIBRARY_PATH_PROPERTY =
            new DistributableActionProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, " +
            "the property.", false);

        public ActionProvider getProvider()
        {
            return JavaActions.this;
        }

        public String getName()
        {
            return "copy-compile-run";
        }

        public String getDescription()
        {
            return "Copies the specified file or contents of the directory into " +
                    "the root of the unarchived handin. Recompiles the code and " +
                    "then runs the specified main in a visible terminal. Once " +
                    "the copy occurs, the copied files and directories remain, " +
                    "and therefore any other actions may interact with them.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(COPY_PATH_PROPERTY, MAIN_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY);
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

                            Allocator.getFileSystemServices().copy(source, destination,
                                    OverwriteMode.FAIL_ON_EXISTING, false,
                                    FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE);

                            _testedGroups.add(group);
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
                            JOptionPane.showMessageDialog(null,
                                    "The specified main class is not present.\n" +
                                    "Specified main: " + mainName,
                                    "Main not present",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        //Run main class
                        else
                        {
                            String terminalName = "Testing " + group.getName() + "'s " +
                                    part.getAssignment().getName() + " - " +
                                    part.getName();
                            executeJavaInVisibleTerminal(mainToRun,
                                     properties.get(CLASS_PATH_PROPERTY),
                                     properties.get(LIBRARY_PATH_PROPERTY),
                                     terminalName,
                                     null,
                                     unarchiveDir);
                        }
                    }
                }
            };

            return action;
        }
    }

    private abstract class Demo implements DistributableActionDescription
    {
        protected final DistributableActionProperty CLASS_PATH_PROPERTY =
            new DistributableActionProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        protected final DistributableActionProperty LIBRARY_PATH_PROPERTY =
            new DistributableActionProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);
        protected final DistributableActionProperty SHOW_TERMINAL_PROPERTY =
            new DistributableActionProperty("show-terminal",
            "By default no terminal is shown when running the jar. " +
            "Set the value of this property to TRUE to provide a " +
            "terminal the grader can interact with.", false);
        protected final DistributableActionProperty TERMINAL_TITLE_PROPERTY =
            new DistributableActionProperty("terminal-name",
            "If a terminal is shown the value of this property will " +
            "be displayed as the title of the terminal. If this value " +
            "is not set the terminal's title will be '[Assignment Name] Demo'", false);

        public ActionProvider getProvider()
        {
            return JavaActions.this;
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.DEMO);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.DEMO, ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN);
        }

        //cmdEnd must not be null
        protected void runDemo(Map<DistributableActionProperty, String> properties,
                String classpath, DistributablePart part, String cmdEnd)
                throws ActionException
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
                String title = part.getAssignment().getName() + " Demo";
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
                    throw new ActionException("Unable to run demo in " +
                            "visible terminal for assignment: " +
                            part.getAssignment().getName(), e);
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
                    throw new ActionException("Unable to run demo for " +
                            "assignment: " + part.getAssignment().getName(), e);
                }
            }
        }
    }

    private class JarDemo extends Demo
    {
        private final DistributableActionProperty LOCATION_PROPERTY =
            new DistributableActionProperty("location",
            "The absolute path to the jar file.", true);

        public String getName()
        {
            return "demo-jar";
        }

        public String getDescription()
        {
            return "Runs a Java jar file.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(LOCATION_PROPERTY, SHOW_TERMINAL_PROPERTY,
                    TERMINAL_TITLE_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String cmdEnd = " -jar " + properties.get(LOCATION_PROPERTY);
                    runDemo(properties, properties.get(CLASS_PATH_PROPERTY), part, cmdEnd);
                }
            };

            return action;
        }
    }

    private class ClassDemo extends Demo
    {
        private final DistributableActionProperty LOCATION_PROPERTY =
            new DistributableActionProperty("location",
            "The absolute path to the top directory containing the Java " +
            "class files.", true);
        private final DistributableActionProperty MAIN_PROPERTY =
            new DistributableActionProperty("main",
            "The full path to the main class including package: ex. cakehat.gui.Main", true);

        public String getName()
        {
            return "demo-class";
        }

        public String getDescription()
        {
            return "Runs compiled Java class files.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(LOCATION_PROPERTY, MAIN_PROPERTY, SHOW_TERMINAL_PROPERTY,
              TERMINAL_TITLE_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String classpath = properties.get(LOCATION_PROPERTY);
                    if(properties.containsKey(CLASS_PATH_PROPERTY))
                    {
                        classpath += ":" + properties.get(CLASS_PATH_PROPERTY);
                    }
                    
                    String cmdEnd = " " + properties.get(MAIN_PROPERTY);

                    runDemo(properties, classpath, part, cmdEnd);
                }
            };

            return action;
        }
    }

    private class RunMain implements DistributableActionDescription
    {
        private final DistributableActionProperty CLASS_PATH_PROPERTY =
            new DistributableActionProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        private final DistributableActionProperty LIBRARY_PATH_PROPERTY =
            new DistributableActionProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);
        private final DistributableActionProperty RUN_ARGS_PROPERTY =
             new DistributableActionProperty("provide-args",
             "By default the main method will be run without any arguments. " +
             "Set this property to TRUE to allow for the grader to provide run " +
             "arguments to the Java program.", false);

        public ActionProvider getProvider()
        {
            return JavaActions.this;
        }

        public String getName()
        {
            return "compile-and-run";
        }

        public String getDescription()
        {
            return "Compiles and runs Java code using a visible terminal the " +
                    "grader can interact with.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY, RUN_ARGS_PROPERTY);
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
                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

                    //Deleted any already compiled files
                    deleteCompiledFiles(unarchiveDir);

                    //Compile, compilation will fail if the code has compilation errors
                    if(compileJava(unarchiveDir, properties.get(CLASS_PATH_PROPERTY)))
                    {
                        List<ClassInfo> mainClasses = getMainClasses(unarchiveDir);

                        if(mainClasses.size() == 0)
                        {
                            JOptionPane.showMessageDialog(null, "No main class is available to run.");
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
                                CompileAndRunDialog dialog = new CompileAndRunDialog(mainClasses, provideArgs);
                                shouldRun = dialog.shouldRun();
                                mainToRun = dialog.getSelectedMain();
                                runArgs = dialog.getRunArguments();
                            }

                            // Run compiled code
                            if(shouldRun)
                            {
                                String terminalName = group.getName() + "'s " +
                                        part.getAssignment().getName() + " - " +
                                        part.getName();
                                executeJavaInVisibleTerminal(mainToRun,
                                         properties.get(CLASS_PATH_PROPERTY),
                                         properties.get(LIBRARY_PATH_PROPERTY),
                                         terminalName,
                                         runArgs,
                                         unarchiveDir);
                            }
                        }
                    }
                }
            };

            return action;
        }
    }

    /**************************************************************************\
    |*                             Shared Methods                             *|
    \**************************************************************************/

    /**
     * Returns information about all main classes. This list can be empty if
     * there are no main classes.
     * <br/><br/>
     * <strong>Note:</strong> no main classes will be returned if the code is
     * not compiled
     *
     * @param unarchiveDir
     * @return
     * @throws ActionException
     */
    private static List<ClassInfo> getMainClasses(File unarchiveDir) throws ActionException
    {
        //Get files
        List<File> classFiles;
        try
        {
            classFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir,
                new FileExtensionFilter("class"));
        }
        catch(IOException e)
        {
            throw new ActionException("Unable to access class files", e);
        }

        List<ClassInfo> entryPoints = new ArrayList<ClassInfo>();

        for(File classFile : classFiles)
        {
            ClassInfo info = getClassInfo(classFile);
            if(info.isMainClass())
            {
                entryPoints.add(info);
            }
        }

        return entryPoints;
    }

    /**
     * Returns information about the class file
     *
     * @param file
     * @return
     * @throws ActionException
     */
    private static ClassInfo getClassInfo(File classFile) throws ActionException
    {
        JavaClass jClass;
        try
        {
            jClass = new ClassParser(new FileInputStream(classFile), classFile.getName()).parse();
        }
        catch (IOException e)
        {
            throw new ActionException(e);
        }
        catch(ClassFormatException e)
        {
            throw new ActionException("BCEL cannot understand the class file " +
                    "that was generated by the Java compiler. Please contact " +
                    "the cakehat developers, the BCEL implementation may be " +
                    "out of date.", e);
        }

        //Determine the path of the directory containing the root package of this class
        String relativePath = jClass.getPackageName().replace('.', '/') + "/" + classFile.getName();
        String rootPackageDir = classFile.getAbsolutePath().replace(relativePath, "");

        boolean hasMain = false;
        for(Method method : jClass.getMethods())
        {
            if(method.isStatic() && method.getName().equals("main") &&
                    method.getSignature().equals("([Ljava/lang/String;)V"))
            {
                hasMain = true;
                break;
            }
        }

        return new ClassInfo(jClass.getClassName(), rootPackageDir, hasMain);
    }

    /**
     * Deletes all compiled files in a student's project. It is safe to run
     * even if there are no compiled files in the project.
     *
     * @param unarchiveDir
     */
    private static void deleteCompiledFiles(File unarchiveDir) throws ActionException
    {
        //Get all compiled files
        Collection<File> compiledFiles;
        try
        {
            compiledFiles = Allocator.getFileSystemUtilities().
                getFiles(unarchiveDir, new FileExtensionFilter("class"));
        }
        catch(IOException e)
        {
            throw new ActionException("Unable to access class files", e);
        }

        //Keep track of success of deleting all of the files
        ArrayList<File> undeleteableFiles = new ArrayList<File>();
        for (File file : compiledFiles)
        {
            if(!file.delete())
            {
                undeleteableFiles.add(file);
            }
        }

        if(!undeleteableFiles.isEmpty())
        {
            String msg = "Unable to delete the following class files: ";
            for(File file : undeleteableFiles)
            {
                msg += "\n" + file.getAbsolutePath();
            }

            throw new ActionException(msg);
        }
    }
    
    /**
     * Compiles Java code, returns whether the code compiled successfully.
     * <br/><br/>
     * Any compiler errors or other messages will be displayed to the grader.
     * <br/><br/>
     * Failure to compile code does not indicate an issue with this method, it
     * may be that code contains errors that prevent it from compiling. If this
     * is the case <code>false</code> will be returned but no exception will be
     * thrown.
     *
     * @param unarchiveDir Java files in this directory and subdirectories will
     * be compiled
     * @param classpath the classpath to compile this code with, may be null
     *
     * @return success of compilation
     */
    private static boolean compileJava(File unarchiveDir, String classpath) throws ActionException
    {
        //Get java compiler and file manager
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        //Tell the compiler to use the classpath if one was passed in
        Collection<String> options = null;
        if(classpath != null)
        {
            options = new Vector<String>();
            options.addAll(Arrays.asList("-classpath", classpath));
        }

        //Listens to errors
        DiagnosticCollector collector = new DiagnosticCollector();

        //Get all of the Java files that are allowed by the inclusion filter
        FileFilter javaFilter = new FileExtensionFilter("java");
        
        List<File> files;
        try
        {
            files = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, javaFilter);
        }
        catch(IOException e)
        {
            throw new ActionException("Unable to access java files", e);
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
            JOptionPane.showMessageDialog(null, msg, "Compilation Failed", JOptionPane.ERROR_MESSAGE);
        }

        try
        {
            fileManager.close();
        }
        catch(IOException e)
        {
            throw new ActionException("Unable to close file manager used by Java " +
                    "compiler. Please try again, this may be a result of issues " +
                    "with the network file system.", e);
        }

        return success;
    }

    /**
     * Executes Java code in a seperate visible terminal. The code must first
     * be compiled.
     *
     * @param mainClass information about the main class
     * @param classpath the classpath to run this code with respect to,
     * may be <code>null</code>
     * @param libraryPath the library path to run this code with respect to,
     * may be <code>null</code>
     * @param termName the title bar displayed by the terminal
     * @param runArgs the arguments provided to the Java program, may be
     * <code>null</code>
     * @param directory the directory the terminal will be in
     */
    private static void executeJavaInVisibleTerminal(ClassInfo mainClass,
            String classpath, String libraryPath, String termName,
            String runArgs, File directory) throws ActionException
    {
        //Adds the packageDir to the classpath
        if(classpath == null)
        {
            classpath = mainClass.getRootPackageDirectory();
        }
        else
        {
            classpath = mainClass.getRootPackageDirectory() + ":" +  classpath;
        }

        //Build java.library.path component if an argument was passed in
        String javaLibrary = "";
        if(libraryPath != null)
        {
            javaLibrary = " -Djava.library.path=" + libraryPath;
        }

        //Build classpath
        String javaClassPath = " -classpath " + "'" + classpath + "'";

        //Build run arguments
        String javaRunArgs = "";
        if(runArgs != null)
        {
            javaRunArgs = " " + runArgs;
        }

        //Put together entire java comand
        String javaCmd = "java " + javaLibrary + javaClassPath + " " +
                mainClass.getClassName() + javaRunArgs;

        try
        {
            Allocator.getExternalProcessesUtilities()
                    .executeInVisibleTerminal(termName, javaCmd, directory);
        }
        catch(IOException e)
        {
            throw new ActionException("Unable to execute Java in visible terminal. " +
                    "Command: " + javaCmd, e);
        }
    }

    /**
     * Information about a Java class based off information from the .class file.
     */
    private static class ClassInfo
    {
        public final String _className;
        public final String _rootPackageDir;
        public final boolean _isMainClass;

        public ClassInfo(String className, String rootPackageDir, boolean isMainClass)
        {
            _className = className;
            _rootPackageDir = rootPackageDir;
            _isMainClass = isMainClass;
        }

        /**
         * Fully qualified name of the class, e.g. org.apache.utils.IOHelper
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
         * Whether this class has the main method:
         *
         * public static void main(String[] args)
         *
         * @return
         */
        public boolean isMainClass()
        {
            return _isMainClass;
        }

        @Override
        public String toString()
        {
            return _className;
        }
    }

    /**
     * A dialog for selecting the main class, and potentially the run arguments.
     */
    private static class CompileAndRunDialog extends JDialog
    {
        private GenericJComboBox<ClassInfo> _mainClassesBox;
        private JTextField _runArgsField;
        private JLabel _argsValidationLabel;
        private JButton _runButton;

        private boolean _shouldRun = false;

        private static final int TOTAL_WIDTH = 450;
        private static final int ELEMENT_HEIGHT = 30;
        private static final int PADDING = 10;

        public CompileAndRunDialog(List<ClassInfo> mainClasses, boolean provideRunArgs)
        {
            this.setTitle("Run Options");
            this.getContentPane().setLayout(new BorderLayout(0, 0));

            // Determine the number of elements that will be displayed
            int numElements = 1; //For the button panel
            if(mainClasses != null)
            {
                numElements += 2;
            }
            if(provideRunArgs)
            {
                numElements += 2;
            }
            int totalElementHeight = numElements * ELEMENT_HEIGHT;
            int totalHeight = totalElementHeight + 2 * PADDING;
            this.getContentPane().setPreferredSize(new Dimension(TOTAL_WIDTH, totalHeight));

            //Padding
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)),
                    BorderLayout.NORTH);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, totalHeight)),
                    BorderLayout.WEST);
            this.getContentPane().add(Box.createRigidArea(new Dimension(PADDING, totalHeight)),
                    BorderLayout.EAST);

            //Content
            JPanel contentPanel = new JPanel(new GridLayout(numElements, 1, 0, 0));
            this.getContentPane().add(contentPanel, BorderLayout.CENTER);

            if(mainClasses != null)
            {
                contentPanel.add(new JLabel("Choose the class containing the mainline to be run"));

                _mainClassesBox = new GenericJComboBox<ClassInfo>(mainClasses);
                contentPanel.add(_mainClassesBox);
            }

            if(provideRunArgs)
            {
                _runArgsField = new ShadowJTextField("Run arguments");

                //Validate input
                _runArgsField.getDocument().addDocumentListener(new DocumentListener()
                {
                    public void insertUpdate(DocumentEvent de) { validateInput(); }
                    public void removeUpdate(DocumentEvent de) { validateInput(); }
                    public void changedUpdate(DocumentEvent de){ validateInput(); }
                });
                //If enter key is pressed, close window is argument is valid
                _runArgsField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "newline");
                _runArgsField.getActionMap().put("newline", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        //If the button is enabled, the arguments are valid
                        if(_runButton.isEnabled())
                        {
                            _shouldRun = true;
                            CompileAndRunDialog.this.dispose();
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
                    CompileAndRunDialog.this.dispose();
                }
            });
            buttonPanel.add(_runButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CompileAndRunDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);

            //Padding below the buttons
            this.getContentPane().add(Box.createRigidArea(new Dimension(TOTAL_WIDTH, PADDING)),
                    BorderLayout.SOUTH);

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

        public ClassInfo getSelectedMain()
        {
            return _mainClassesBox.getSelectedItem();
        }

        public String getRunArguments()
        {
            return _runArgsField == null ? null : _runArgsField.getText();
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
}