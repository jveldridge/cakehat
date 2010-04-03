package config;

import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import utils.Allocator;
import utils.BashConsole;

/**
 * A C subclass of CodeHandin.
 *
 * Handles both gcc compilation and makefiles.
 *
 * RUN
 *      MODE
 *          makefile
 *          gcc
 *
 * DEMO
 *      MODE
 *          makefile
 *          gcc
 *
 * @author spolett
 */
class CHandin extends CodeHandin
{
    private static final String[] _sourceFileTypes = { "c" };
    
    private static final String
    MAKE_RUN = "make-run", GCC_RUN = "gcc-run",
    MAKE_DEMO = "make-demo", GCC_DEMO = "gcc-demo",
    MAKE_EXEC_NAME = "exec-name";

    //Run modes
    private static final LanguageSpecification.Mode
    RUN_MAKE_MODE = new LanguageSpecification.Mode(MAKE_RUN,
            new LanguageSpecification.Property(MAKE_EXEC_NAME, true)),
    RUN_GCC_MODE = new LanguageSpecification.Mode(GCC_RUN),
    //Demo modes
    DEMO_MAKE_MODE = new LanguageSpecification.Mode(MAKE_DEMO, 
            new LanguageSpecification.Property(MAKE_EXEC_NAME, true)),
    DEMO_GCC_MODE = new LanguageSpecification.Mode(GCC_DEMO);

    //Specification of how this handin can be configured
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("C",
                new LanguageSpecification.Mode[] {RUN_MAKE_MODE, RUN_GCC_MODE },
                new LanguageSpecification.Mode[] {DEMO_MAKE_MODE, DEMO_GCC_MODE }, 
                null); //no tester support yet

    CHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

    @Override
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
             BashConsole.writeThreaded("cd " + 
                     super.getStudentHandinDirectory(studentLogin) + "; make clean; "
                     + "make; ./" + this.getRunProperty(MAKE_EXEC_NAME));
        }
    }

    private Collection<String> getCFiles(String directoryPath) {
        Collection<File> fileList = Allocator.getGeneralUtilities().getFiles
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

    private void compileAndRun(String dirPath, String filename)
    {
        String loc = dirPath + filename;
        BashConsole.writeThreaded("gcc -Wall -o " + loc + " " + loc + ".c");
        BashConsole.writeThreaded("./" + loc);
    }


    @Override
    public void runDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}