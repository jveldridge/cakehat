package config;

import java.awt.GridLayout;
import java.io.File;
import matlab.MatlabConnectionException;
import utils.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import matlab.MatlabProxyController;

/**
 * Adds support for MATLAB student handins.
 *
 * Below are the valid modes and properties. ( ) indicate the argument is optional.
 *
 * RUN
 *      MODE
 *          select_file
 *          specify_file
 *              path
 *
 * DEMO
 *      MODE
 *          directory
 *              path
 *          mfile
 *
 * TESTER
 *      MODE
 *          mfile
 *              path
 *          directory
 *              path
 *
 * @author spoletto
 * @author jeldridg
 */

class MatlabHandin extends CodeHandin {
    private static final String[] _sourceFileTypes = { "m" };

    private static final String 
    SELECT_FILE = "select-file", SPECIFY_FILE = "specify-file", PATH="path",
    MFILE="m-file", DIRECTORY="directory";

    private static final LanguageSpecification.Mode
    //run modes
    RUN_SELECT_FILE_MODE = new LanguageSpecification.Mode(SELECT_FILE),
    RUN_SPECIFY_FILE_MODE = new LanguageSpecification.Mode(SPECIFY_FILE,
                                new LanguageSpecification.Property(PATH, true)),

    //demo modes
    DEMO_MFILE_MODE = new LanguageSpecification.Mode((MFILE),
                                new LanguageSpecification.Property(PATH, true)),
    DEM0_DIRECTORY_MODE = new LanguageSpecification.Mode((DIRECTORY),
                                new LanguageSpecification.Property(PATH, true)),

    //tester modes
    TEST_MFILE_MODE = new LanguageSpecification.Mode((MFILE),
                                new LanguageSpecification.Property(PATH, true));


    public static final LanguageSpecification SPECIFICATION =
    new LanguageSpecification("Matlab",
                              new LanguageSpecification.Mode[]{ RUN_SELECT_FILE_MODE, RUN_SPECIFY_FILE_MODE },
                              new LanguageSpecification.Mode[]{ DEMO_MFILE_MODE, DEM0_DIRECTORY_MODE },
                              new LanguageSpecification.Mode[]{ TEST_MFILE_MODE });

    MatlabHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    @Override
    protected String[] getSourceFileTypes()
    {
        return _sourceFileTypes;
    }

    @Override
    public void openCode(String studentLogin)
    {
        this.setupRun(studentLogin);

        //close all open code and get new M-files to open
        MatlabProxyController.eval("com.mathworks.mlservices.MLEditorServices.closeAll");
        List<String> files = this.getMFiles(super.getStudentHandinDirectory(studentLogin));

        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);

        String openCommand = "edit ";
        for (String s : files ) {
            openCommand += s + " ";
        }

        //open up all .m files in MATLAB editor with 'edit' command
        MatlabProxyController.eval(openCommand);
    }


    @Override
    public void run(String studentLogin) {
        this.setupRun(studentLogin);
    
        if (_runMode.equalsIgnoreCase(SELECT_FILE)) {
            //create the GUI to select which function to run
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,1));
            JComboBox cb = new JComboBox();
            Collection<String> files = this.getMFiles(super.getStudentHandinDirectory(studentLogin));
            for (String s : files ) {
                cb.insertItemAt(s, cb.getItemCount());
            }
            if (cb.getModel().getSize() > 0) {
                cb.setSelectedIndex(0);
            }

            JLabel label = new JLabel("Enter any function arguments here: ");
            JTextField tf = new JTextField();

            panel.add(cb);
            panel.add(label);
            panel.add(tf);

            if (JOptionPane.showConfirmDialog(null, panel, "Select file to run:", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                   String cmd = (String) cb.getSelectedItem();
                   if(tf.getText() != null && !tf.getText().isEmpty()) {
                       cmd += "(" + tf.getText() + ")";
                   }
                    MatlabProxyController.eval(cmd);
            }
        }
        else if (_runMode.equalsIgnoreCase(SPECIFY_FILE)) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,1));

            String function = this.getRunProperty(PATH);

            JLabel label = new JLabel("Enter here any arguments for function " + function + ": ");
            JTextField tf = new JTextField();

            panel.add(label);
            panel.add(tf);

            if (JOptionPane.showConfirmDialog(null, panel, "Enter arguments", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                   String cmd = function;
                   if(tf.getText() != null && !tf.getText().isEmpty()) {
                       cmd += "(" + tf.getText() + ")";
                   }
                   MatlabProxyController.eval(cmd);
            }
        }
        else {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
            return;
        }
        
    }

    @Override
    public void runDemo() {
        if (_demoMode.equalsIgnoreCase(DIRECTORY)) {
            String path = this.getDemoProperty(PATH);
            this.setupRun();
            MatlabProxyController.eval("cd " + path);
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0,1));
            JComboBox cb = new JComboBox();
            Collection<String> files = this.getMFiles(path);
            for (String s : files ) {
                cb.insertItemAt(s, cb.getItemCount());
            }
            if (cb.getModel().getSize() > 0) {
                cb.setSelectedIndex(0);
            }

            JLabel label = new JLabel("Enter any function arguments here: ");
            JTextField tf = new JTextField();

            panel.add(cb);
            panel.add(label);
            panel.add(tf);

            if (JOptionPane.showConfirmDialog(null, panel, "Select file to run:", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                   String cmd = (String) cb.getSelectedItem();
                   if(tf.getText() != null && !tf.getText().isEmpty()) {
                       cmd += "(" + tf.getText() + ")";
                   }
                    MatlabProxyController.eval(cmd);
            }
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
            return;
        }
    }

    @Override
    public void runTester(String studentLogin) {
        this.setupRun(studentLogin);

        if (_testerMode.equalsIgnoreCase(MFILE)) {
            this.getDemoProperty(PATH);
            //Get name of tester file from path to tester
            String testerPath = this.getTesterProperty(PATH);
            String testerName = testerPath.substring(testerPath.lastIndexOf("/")+1);

            String copyPath = this.getStudentHandinDirectory(studentLogin) + testerName;

            //Copy file into student's code directory, print error and bail if copy fails
            if(!Allocator.getGeneralUtilities().copyFile(testerPath, copyPath))
            {
                System.err.println("Could not test " + studentLogin + "'s " + this.getName());
                System.err.println("Error in copying " + testerPath + " to " + copyPath);
                return;
            }

            //run the tester from the directory with the student's code
            //(splitting gets rid of .m)
            MatlabProxyController.eval(testerName.split("\\.")[0]);
        }
        else
        {
            System.err.println(this.getClass().getName() +
                               " does not support this run mode: " + _runMode);
            return;
        }
    }

    private void setupRun(String studentLogin) {
        this.setupRun();

        super.untar(studentLogin);
        MatlabProxyController.eval("cd " + super.getStudentHandinDirectory(studentLogin));
    }

    private void setupRun() {
        if (!MatlabProxyController.isConnected()) {
            try {
                MatlabProxyController.createConnection();
            } catch (MatlabConnectionException ex) {
                new ErrorView(ex, "Could not create connection to MATLAB");
            }
        }
    }

    private List<String> getMFiles(String directoryPath) {
        Collection<File> fileList = Allocator.getGeneralUtilities().getFiles
                (directoryPath, "m");
        List<String> mFiles = new Vector<String>();
        for(File f : fileList)
        {
            mFiles.add(f.getName().split("\\.")[0]);
        }
        return mFiles;
    }

}