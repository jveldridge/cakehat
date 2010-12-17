package gradesystem.config;

import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import gradesystem.utils.Allocator;
import gradesystem.utils.BashConsole;

/**
 * A C subclass of CodeHandin.
 *
 * Handles both gcc compilation and makefiles.
 *
 * RUN
 *      MODE
 *          make-run
 *              exec-name
 *          gcc-run
 *
 * DEMO
 *      MODE
 *          exec-demo
 *              exec-loc
 *
 * @author spoletto
 */
class CHandin extends CodeHandin
{
    private static final String[] _sourceFileTypes = { "c" };
    
    private static final String
    MAKE_RUN = "make-run", EXEC_DEMO = "exec-demo",
    GCC_RUN = "gcc-run", MAKE_EXEC_NAME = "exec-name",
    EXEC_LOC = "exec-loc";

    //Run modes
    private static final LanguageSpecification.Mode
    RUN_MAKE_MODE = new LanguageSpecification.Mode(MAKE_RUN,
            new LanguageSpecification.Property(MAKE_EXEC_NAME, true)),
    RUN_GCC_MODE = new LanguageSpecification.Mode(GCC_RUN),
    //Demo modes
    DEMO_EXEC_MODE = new LanguageSpecification.Mode(EXEC_DEMO,
            new LanguageSpecification.Property(EXEC_LOC, true));
    
    //Specification of how this handin can be configured
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("C",
                new LanguageSpecification.Mode[] {RUN_MAKE_MODE, RUN_GCC_MODE },
                new LanguageSpecification.Mode[] {DEMO_EXEC_MODE},
                null); //no tester support yet

    CHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

    public void run(String studentLogin)
    {
        //Untar student files
        this.untar(studentLogin);

        if(_runMode.equalsIgnoreCase(GCC_RUN))
        {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,1));
            JComboBox cb = new JComboBox();
            Collection<String> files = this.getCFiles(super.getStudentHandinDirectory(studentLogin));
            for (String s : files ) {
                cb.insertItemAt(s, cb.getItemCount());
            }
            if (cb.getModel().getSize() > 0) {
                cb.setSelectedIndex(0);
            }
            panel.add(cb);
            if (JOptionPane.showConfirmDialog(null, panel, "Select file to run:", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String cFile = (String) cb.getSelectedItem();
                //Remove compiled file
                this.deleteExecutableFile(studentLogin, cFile);
                this.compileAndRun(studentLogin, cFile);
            }
        }
        else if(_runMode.equalsIgnoreCase(MAKE_RUN))
        {
            Vector<String> cmd = new Vector<String>();
            cmd.add("cd " +
                     super.getStudentHandinDirectory(studentLogin));
            cmd.add("make clean");
            cmd.add("make");
            Collection<String> success = BashConsole.writeErrorStream(cmd);
            if(success.isEmpty()) { //if make compilation successful
                BashConsole.write("xterm -hold -e " +
                     super.getStudentHandinDirectory(studentLogin) +
                     this.getRunProperty(MAKE_EXEC_NAME));
            }
            else { //display compilation errors
             BashConsole.write("xterm -hold -e 'cd " +
                     super.getStudentHandinDirectory(studentLogin) + "; make clean; "
                     + "make; exit && /bin/bash'");
            }
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
        }
    }

    private Collection<String> getCFiles(String directoryPath) {
        Collection<File> fileList = Allocator.getFileSystemUtilities().getFiles
                (directoryPath, "c");
        Collection<String> cFiles = new Vector<String>();
        for(File f : fileList)
        {
            cFiles.add(f.getName().split("\\.")[0]);
        }
        return cFiles;
    }

    private boolean deleteExecutableFile(String studentLogin, String filename)
    {
        String dir = this.getStudentHandinDirectory(studentLogin) + filename;
        File entry = new File(dir);
        if(entry.exists() && entry.isFile()) {
            return entry.delete();
        }
        return false;
    }

    private void compileAndRun(String studentLogin, String filename)
    {
        String loc = super.getStudentHandinDirectory(studentLogin) + filename;
        Vector<String> cmd = new Vector<String>();
        cmd.add("gcc -Wall -o " + loc + " " + loc + ".c -lm");
        Collection<String> success = BashConsole.writeErrorStream(cmd);
        if (success.isEmpty()) { //if compilation successful
            BashConsole.writeThreaded("xterm -hold -e " + loc);
        }
        else { //display compiler errors in visible terminal
            BashConsole.writeThreaded("xterm -hold -e gcc -Wall -o " + loc + " " + loc + ".c -lm");
        }
    }

    public void runDemo()
    {
        if(_demoMode.equalsIgnoreCase(EXEC_DEMO))
        {
            BashConsole.writeThreaded("./" + this.getDemoProperty(EXEC_LOC));
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this demo mode: " + _demoMode);
        }
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}