package gradesystem.handin;

import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.handin.file.AndFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ImageIcon;
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
import utils.FileExtensionFilter;

/**
 * Actions for the Java programming language. Compilation of Java is done via
 * the {@link javax.tools.JavaCompiler} provided by
 * {@link javax.tools.ToolProvider}. Analysis of Java class files is done by
 * with BCEL (Byte Code Engineering Library) to determine main classes and a
 * class's package.
 *
 * @author jak2
 */
class JavaActions implements ActionProvider
{
    public List<DistributableActionDescription> getActionDescriptions()
    {
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();

        descriptions.add(new RunMain());
        descriptions.add(new JarDemo());
        descriptions.add(new ClassDemo());

        return descriptions;
    }

    public String getNamespace()
    {
        return "java";
    }

    //TODO: Test action that copies content of a config-specified directory into
    //handin, compiles code, and then runs a config-specified main (presumably a
    //class that was copied in, but that's not going to be enforced.)

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
            return Arrays.asList(new ActionMode[] { ActionMode.DEMO });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.DEMO,
                ActionMode.RUN, ActionMode.TEST, ActionMode.OPEN });
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
                cmd += " -classpath " + classpath;
            }

            cmd += cmdEnd;

            //Determine if this should be run in a visible terminal or not
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
                    Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, cmd);
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
                    Allocator.getExternalProcessesUtilities().executeAsynchronously(cmd);
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
            return Arrays.asList(new DistributableActionProperty[]
            { LOCATION_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY,
              CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY });
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
            return Arrays.asList(new DistributableActionProperty[]
            { LOCATION_PROPERTY, MAIN_PROPERTY, SHOW_TERMINAL_PROPERTY,
              TERMINAL_TITLE_PROPERTY, CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY });
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
            return Arrays.asList(new DistributableActionProperty[] { MAIN_PROPERTY,
                CLASS_PATH_PROPERTY, LIBRARY_PATH_PROPERTY});
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
                    FileFilter inclusionFilter = part.getInclusionFilter(group);
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                    //Deleted any already compiled files
                    deleteCompiledFiles(unarchiveDir);

                    //Compile, compilation will fail if the code has compilation
                    //errors
                    if(compileJava(unarchiveDir, inclusionFilter, properties.get(CLASS_PATH_PROPERTY)))
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
                            JOptionPane.showMessageDialog(null,
                                    "The specified main class is not present in the handin. \n" +
                                    "Specified main: " + specifiedMain + "\n\n" +
                                    "If more than one main class is present you will \n" +
                                    "be prompted to select from all main classes, \n" +
                                    "otherwise the main class present will be run.",
                                    "Specified main class not present", JOptionPane.ERROR_MESSAGE);
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
                                                    new ImageIcon(JavaActions.class.getResource("/gradesystem/resources/icons/32x32/go-next.png")),
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
                            String terminalName = group.getName() + "'s " + part.getAssignment().getName();
                            executeJavaInVisibleTerminal(mainToRun,
                                     properties.get(CLASS_PATH_PROPERTY),
                                     properties.get(LIBRARY_PATH_PROPERTY),
                                     terminalName);
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
        List<File> classFiles = Allocator.getFileSystemUtilities().getFiles(unarchiveDir,
                new FileExtensionFilter("class"));
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
        //Do NOT use the inclusion filter as these are generated files
        Collection<File> compiledFiles = Allocator.getFileSystemUtilities().
                getFiles(unarchiveDir, new FileExtensionFilter("class"));

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
     * @param inclusionFilter describes what files are allowed to be included
     * @param classpath the classpath to compile this code with, may be null
     *
     * @return success of compilation
     */
    private static boolean compileJava(File unarchiveDir,
            FileFilter inclusionFilter, String classpath) throws ActionException
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
        FileFilter combinedFilter = new AndFileFilter(inclusionFilter, new FileExtensionFilter("java"));
        List<File> files = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, combinedFilter);
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
     * @param termName what the title bar of the terminal will display
     */
    private static void executeJavaInVisibleTerminal(ClassInfo mainClass,
            String classpath, String libraryPath, String termName) throws ActionException
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

        //Location of java
        String javaLoc = "/usr/bin/java";
        //Add java.library.path component if an argument was passed in
        String javaLibrary = "";
        if(libraryPath != null)
        {
            javaLibrary = " -Djava.library.path=" + libraryPath;
        }
        //Add classpath
        String javaClassPath = " -classpath " + classpath;
        //Put together entire java comand
        String javaCmd = javaLoc + javaLibrary + javaClassPath + " " + mainClass.getClassName();

        try
        {
            Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(termName, javaCmd);
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