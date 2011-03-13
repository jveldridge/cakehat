package gradesystem.handin;

import com.google.common.collect.ImmutableList;
import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.resources.icons.IconLoader;
import gradesystem.resources.icons.IconLoader.IconImage;
import gradesystem.resources.icons.IconLoader.IconSize;
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
import javax.swing.JOptionPane;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import utils.FileCopyingException;
import utils.FileExistsException;
import utils.FileExtensionFilter;
import utils.FileSystemUtilities.FileCopyPermissions;
import utils.FileSystemUtilities.OverwriteMode;

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
        //TODO: Have property to allow for specifying run arguments

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
        //TODO: Have property to allow for specifying run arguments

        private final DistributableActionProperty MAIN_PROPERTY =
            new DistributableActionProperty("main",
            "The full path to the main class including package: ex. cakehat.gui.Main \n" +
            "If no main class is specified or the main class does not exist in " +
            "the distributable part, then the grader will be asked to choose " +
            "from one of the main classes found.", false);
        private final DistributableActionProperty CLASS_PATH_PROPERTY =
            new DistributableActionProperty("classpath",
            "The classpath; use colons to separate each entry.", false);
        private final DistributableActionProperty LIBRARY_PATH_PROPERTY =
            new DistributableActionProperty("librarypath",
            "The java.library.path property. This values replaces, not appends, the property.", false);

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
            return ImmutableList.of(MAIN_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY);
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

                    //Compile, compilation will fail if the code has compilation
                    //errors
                    if(compileJava(unarchiveDir, properties.get(CLASS_PATH_PROPERTY)))
                    {
                        //Get all of the main classes
                        List<ClassInfo> mainClasses = getMainClasses(unarchiveDir);

                        //Check if the main specified actually exists
                        ClassInfo mainToRun = null;
                        if(properties.containsKey(MAIN_PROPERTY))
                        {
                            String specifiedMain = properties.get(MAIN_PROPERTY);
                            for(ClassInfo info : mainClasses)
                            {
                                if(info.getClassName().equals(specifiedMain))
                                {
                                    mainToRun = info;
                                    break;
                                }
                            }

                            //If none was found, inform the grader
                            if(mainToRun == null)
                            {
                                JOptionPane.showMessageDialog(null,
                                        "The specified main class is not present in the handin. \n" +
                                        "Specified main: " + specifiedMain + "\n\n" +
                                        "If more than one main class is present you will \n" +
                                        "be prompted to select from all main classes, \n" +
                                        "otherwise the main class present will be run.",
                                        "Specified main class not present", JOptionPane.WARNING_MESSAGE);
                            }
                        }

                        if(mainToRun == null)
                        {
                            if(mainClasses.size() == 1)
                            {
                                mainToRun =  mainClasses.get(0);
                            }
                            //If more than one entry, pop up a dialog letting the user pick one
                            else
                            {
                                Object[] possibilities = mainClasses.toArray();
                                mainToRun = (ClassInfo) JOptionPane.showInputDialog(
                                                    null,
                                                    "Choose mainline class:",
                                                    "Main class",
                                                    JOptionPane.OK_OPTION,
                                                    IconLoader.loadIcon(IconSize.s32x32, IconImage.GO_NEXT),
                                                    possibilities,
                                                    possibilities[0]);
                            }
                        }

                        if(mainToRun == null)
                        {
                            JOptionPane.showMessageDialog(null, "No main class is available to run.");
                        }
                        //Run compiled code
                        else
                        {
                            String terminalName = group.getName() + "'s " +
                                    part.getAssignment().getName() + " - " +
                                    part.getName();
                            executeJavaInVisibleTerminal(mainToRun,
                                     properties.get(CLASS_PATH_PROPERTY),
                                     properties.get(LIBRARY_PATH_PROPERTY),
                                     terminalName,
                                     unarchiveDir);
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
     * @param classpath the classpath to run this code with respect to, may be null
     * @param libraryPath the library path to run this code with respect to, may be null
     * @param termName the title bar displayed by the terminal
     * @param directory the directory the terminal will be in
     */
    private static void executeJavaInVisibleTerminal(ClassInfo mainClass,
            String classpath, String libraryPath, String termName,
            File directory) throws ActionException
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

        //Add java.library.path component if an argument was passed in
        String javaLibrary = "";
        if(libraryPath != null)
        {
            javaLibrary = " -Djava.library.path=" + libraryPath;
        }

        //Add classpath
        String javaClassPath = " -classpath " + "'" + classpath + "'";
        
        //Put together entire java comand
        String javaCmd = "java " + javaLibrary + javaClassPath + " " + mainClass.getClassName();

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
}