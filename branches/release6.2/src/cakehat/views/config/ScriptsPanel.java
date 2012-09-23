package cakehat.views.config;

import cakehat.Allocator;
import cakehat.CakehatMain.TerminalOption;
import cakehat.CakehatReleaseInfo;
import cakehat.CakehatRunMode;
import cakehat.CakehatSession;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import support.ui.FormattedLabel;
import support.utils.FileDeletingException;
import support.utils.posix.FilePermission;

/**
 *
 * @author jak2
 */
class ScriptsPanel extends JPanel
{
    private final ConfigManagerView _configManager;
    private final JPanel _contentPanel;
    
    ScriptsPanel(ConfigManagerView configManager)
    {
        _configManager = configManager;
        
        this.setLayout(new BorderLayout(0, 0));
        _contentPanel = new JPanel();
        this.add(_contentPanel);
        this.initialize();
    }
    
    private void initialize()
    {
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        
        this.addScriptPanel("cakehat Convenience Script",
                "cakehat can always be run by using the <tt>cakehat</tt> script and specifying the course. cakehat " +
                "can generate a script for your TA staff to use which avoids needing to specify your course.",
                CakehatSession.getCourse() + "_cakehat",
                new ScriptCreationAction()
                {
                    @Override
                    public void createScript(File scriptFile)
                    {
                        generateCakehatConvenienceScript(scriptFile);
                    }
                });
        
        _contentPanel.add(Box.createVerticalStrut(20));
        
        this.addScriptPanel("Enter Grade Convenience Script",
                "Grades can be entered into cakehat from the terminal (for instance when checking off students for a " +
                "lab). This can always be done using the <tt>cakehat</tt> script and specifying your course and the " +
                "enter grade mode. cakehat can generate a script for your TA staff to use which avoids needing to " +
                "specify your course and the enter grade mode.",
                CakehatSession.getCourse() + "_cakehatEnterGrade",
                new ScriptCreationAction()
                {
                    @Override
                    public void createScript(File scriptFile)
                    {
                        generateCakehatEnterGradeScript(scriptFile);
                    }
                });
    }
    
    private static interface ScriptCreationAction
    {
        public void createScript(File scriptFile);
    }
    
    private void addScriptPanel(String title, String message, final String defaultScriptName,
            final ScriptCreationAction action)
    {
        _contentPanel.add(FormattedLabel.asHeader(title));
        _contentPanel.add(Box.createVerticalStrut(3));
        _contentPanel.add(FormattedLabel.asContent(message).usePlainFont());
        
        JButton scriptButton = new JButton("Create Script");
        scriptButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {   
                //Configure file chooser to show the default script name and start in the tabin or course directory
                final JFileChooser chooser = new JFileChooser();
                chooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, new PropertyChangeListener()
                {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), defaultScriptName));
                        chooser.updateUI();
                    }
                });
                if(Allocator.getPathServices().getTABinDir().exists())
                {
                    chooser.setCurrentDirectory(Allocator.getPathServices().getTABinDir());
                }
                else
                {
                    chooser.setCurrentDirectory(Allocator.getPathServices().getCourseDir());
                }
                
                if(chooser.showSaveDialog(_configManager) == JFileChooser.APPROVE_OPTION)
                {
                    File file = chooser.getSelectedFile();
                    action.createScript(file);
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
        {
            @Override
            public Dimension getMaximumSize()
            {
                Dimension size = getPreferredSize();
                size.width = Short.MAX_VALUE;

                return size;
            }
        };
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        _contentPanel.add(buttonPanel);
        buttonPanel.add(scriptButton);
    }
    
    private static void generateCakehatConvenienceScript(File scriptFile)
    {
        try
        {
            Allocator.getFileSystemServices().makeDirectory(scriptFile.getParentFile());
            if(scriptFile.exists())
            {
                Allocator.getFileSystemUtilities().deleteFiles(ImmutableSet.of(scriptFile));
            }
            scriptFile.createNewFile();

            //Write script
            PrintWriter writer = new PrintWriter(scriptFile);
            writer.println("#!/bin/bash");
            writer.println("#==============================================================================");
            writer.println("# A course-specific convenience script to run cakehat.");
            writer.println("# ");
            writer.println("# Autogenerated by cakehat");
            writer.println("# Generated for course: " + CakehatSession.getCourse());
            writer.println("# Generated on: " + DateTimeFormat.longDateTime().print(DateTime.now()));
            writer.println("# Generated by: cakehat version " + CakehatReleaseInfo.getVersion());
            writer.println("#==============================================================================");
            //Unescaped, the script is of the form: /contrib/bin/cakehat/ -c <course> -g "$@"
            //This script passes in the course first and then any arguments passed to the script
            writer.println("/contrib/bin/cakehat " +
                    TerminalOption.COURSE.getShortOption() + " " + CakehatSession.getCourse() + " \"$@\"");
            writer.close();

            Allocator.getFileSystemUtilities().chmod(scriptFile, false,
                    ImmutableSet.of(FilePermission.OWNER_READ, FilePermission.OWNER_EXECUTE,
                        FilePermission.GROUP_READ, FilePermission.GROUP_EXECUTE));
            Allocator.getFileSystemUtilities().changeGroup(scriptFile, Allocator.getCourseInfo().getTAGroup(),
                    false);
        }
        catch(ServicesException ex)
        {
            ErrorReporter.report("Unable to create directory for cakehat convenience script", ex);
        }
        catch(FileDeletingException ex)
        {
            ErrorReporter.report("Unable to delete existing cakehat convenience script", ex);
        }
        catch(IOException ex)
        {
            ErrorReporter.report("Unable to create cakehat convenience script", ex);
        }
    }
    
    private static void generateCakehatEnterGradeScript(File scriptFile)
    {
        try
        {
            Allocator.getFileSystemServices().makeDirectory(scriptFile.getParentFile());
            if(scriptFile.exists())
            {
                Allocator.getFileSystemUtilities().deleteFiles(ImmutableSet.of(scriptFile));
            }
            scriptFile.createNewFile();

            //Write script
            PrintWriter writer = new PrintWriter(scriptFile);
            writer.println("#!/bin/bash");
            writer.println("#==============================================================================");
            writer.println("# A course specific convenience script to enter grades into cakehat from the");
            writer.println("# terminal.");
            writer.println("# ");
            writer.println("# Autogenerated by cakehat");
            writer.println("# Generated for course: " + CakehatSession.getCourse());
            writer.println("# Generated on: " + DateTimeFormat.longDateTime().print(DateTime.now()));
            writer.println("# Generated by: cakehat version " + CakehatReleaseInfo.getVersion());
            writer.println("#==============================================================================");
            //Unescaped, the script is of the form: /contrib/bin/cakehat/ -c <course> -m enterGrade -g "$@"
            //This script passes in the course first, then the enter grade mode, then the flag for enter grade
            //commands and then all of the arguments to the script which will be the values for the enter grade
            //arguments option
            writer.println("/contrib/bin/cakehat " +
                    TerminalOption.COURSE.getShortOption() + " " + CakehatSession.getCourse() + " " +
                    TerminalOption.RUN_MODE.getShortOption() + " " + CakehatRunMode.ENTER_GRADE.getTerminalValue()
                    + " " + TerminalOption.ENTER_GRADE_ARGS.getShortOption()+ " \"$@\"");
            writer.close();

            Allocator.getFileSystemUtilities().chmod(scriptFile, false,
                    ImmutableSet.of(FilePermission.OWNER_READ, FilePermission.OWNER_EXECUTE,
                        FilePermission.GROUP_READ, FilePermission.GROUP_EXECUTE));
            Allocator.getFileSystemUtilities().changeGroup(scriptFile, Allocator.getCourseInfo().getTAGroup(),
                    false);
        }
        catch(ServicesException ex)
        {
            ErrorReporter.report("Unable to create directory for cakehat enter grade convenience script", ex);
        }
        catch(FileDeletingException ex)
        {
            ErrorReporter.report("Unable to delete existing cakehat enter grade convenience script", ex);
        }
        catch(IOException ex)
        {
            ErrorReporter.report("Unable to create cakehat enter grade convenience script", ex);
        }
    }
}