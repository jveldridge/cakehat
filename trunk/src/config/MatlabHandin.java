package config;

import java.awt.GridLayout;
import java.io.File;
import matlab.MatlabClient;
import utils.*;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import matlab.SetupScriptWriter;

/**
 * Adds support for MATLAB student handins.
 *
 * @author spoletto
 */

class MatlabHandin extends CodeHandin
{
    private static final String[] _sourceFileTypes = { "m" };
    private static MatlabClient _client = null;

    //TODO: Fill in my properties
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("Matlab", null, null, null);

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
        try
        {
            //close the MATLAB text editor
            _client.sendCommand("Editor = com.mathworks.mlservices.MLEditorServices;");
            _client.sendCommand("Editor.closeAll;");
            _client.sendCommand("cd " + super.getStudentHandinDirectory(studentLogin));
            //get all .m files from current working directory
            //open up all .m files in MATLAB editor with 'edit' command
            Collection<String> files = this.getMFiles(studentLogin);
            for (String s : files ) {
                _client.sendCommand("edit " + s + ".m");
            }
        }
        catch(Exception e) {
            new ErrorView(e, "Could not connect to MATLAB server. If you were " +
                    "running an active session of MATLAB before pressing the run " +
                    "button, please close MATLAB and try again");
        }
    }

    private Collection<String> getMFiles(String studentLogin)
    {
        Collection<File> fileList = Allocator.getGeneralUtilities().getFiles
                (super.getStudentHandinDirectory(studentLogin), "m");
        Collection<String> mFiles = new Vector<String>();
        for(File f : fileList)
        {
            mFiles.add(f.getName().split("\\.")[0]);
        }
        return mFiles;
    }

    private void setupRun(String studentLogin) {
        if (!SetupScriptWriter.exists()) {
            SetupScriptWriter.createScript();
        }
        super.untar(studentLogin);
        //ps -u graderlogin | grep matlab
        Collection<String> response = BashConsole.write("ps -u " +
                Allocator.getGeneralUtilities().getUserLogin() + " | grep matlab");
        if (response.isEmpty()) { //MATLAB is not currently running
            String terminalCmd = "/usr/bin/xterm -title " + "\"" + "MATLAB" +
                    "\"" + " -e " + "\"" + "cd " + Allocator.getCourseInfo().getGradingDir() +
                    "bin ; matlab -r setup " + "; read" + "\"";
            //Execute the command in a seperate thread
            BashConsole.writeThreaded(terminalCmd);
            boolean serverExists = false;
            long timeout = 60000;
            long startTime = System.currentTimeMillis();
            while (!serverExists && System.currentTimeMillis() < startTime + timeout) {
                try {
                    _client = new MatlabClient();
                    serverExists = true;
                } catch (Exception e) {
                    serverExists = false;
                }
                long now = System.currentTimeMillis();
                while (System.currentTimeMillis() < now + 1000);
            }
            if (!serverExists) {
                new ErrorView(new Exception(), "Could not set up MATLAB server within specified timeout");
            }
        }
    }


    @Override //TODO: dynamically create setup.m script for use in
    //courses other than CS4
    public void run(String studentLogin)
    {
        this.setupRun(studentLogin);
        try
        {
            _client.sendCommand("cd " + super.getStudentHandinDirectory(studentLogin));
            //get all .m files from current working directory
            //open up all .m files in MATLAB editor with 'edit' command
        }
        catch(Exception e) {
            new ErrorView(e, "Could not connect to MATLAB server. If you were " +
                    "running an active session of MATLAB before pressing the run " +
                    "button, please close MATLAB and try again");
        }
    
        //create the GUI to select which function to run
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));
        JComboBox cb = new JComboBox();
        Collection<String> files = this.getMFiles(studentLogin);
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
                _client.sendCommand((String) cb.getSelectedItem());
        }
        
    }

    @Override
    public boolean hasDemo()
    {
        return false;
    }

    @Override
    public void runDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTester()
    {
        return false;
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasRun()
    {
        return true;
    }

}