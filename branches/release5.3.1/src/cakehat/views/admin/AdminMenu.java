package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatAboutBox;
import cakehat.services.CSVExportTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import support.ui.ProgressDialog;

/**
 *
 * @author jak2
 */
class AdminMenu extends JMenuBar
{
    private final AdminView _adminView;
    
    AdminMenu(AdminView adminView)
    {
        _adminView = adminView;
        
        //File menu
        JMenu fileMenu = new JMenu("File");
        this.add(fileMenu);

        //Refresh item
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        refreshItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _adminView.refresh();
            }
        });
        fileMenu.add(refreshItem);

        //Quit item
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _adminView.dispose();
            }
        });
        fileMenu.add(quitItem);
        
        //Grades menu
        JMenu gradesMenu = new JMenu("Grades");
        this.add(gradesMenu);
        
        //Grades Report item
        JMenuItem gradesReportItem = new JMenuItem("Send Grade Reports");
        gradesReportItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                GradeReportView.display(_adminView);
            }
        });
        gradesMenu.add(gradesReportItem);
        
        //CSV Export item
        JMenuItem csvExportItem = new JMenuItem("CSV Export");
        csvExportItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                JFileChooser chooser = new JFileChooser(Allocator.getPathServices().getCourseDir());
                chooser.setFileFilter(new FileNameExtensionFilter("Comma-separated values", "csv"));
                if(chooser.showSaveDialog(_adminView) == JFileChooser.APPROVE_OPTION)
                {
                    new ProgressDialog(_adminView, "CSV Export",
                            "<html><center><h2>Exporting student grades</h2></center></html>",
                            new CSVExportTask(chooser.getSelectedFile()));
                }
            }
        });
        gradesMenu.add(csvExportItem);

        //Help menu
        JMenu helpMenu = new JMenu("Help");
        this.add(helpMenu);

        //About
        JMenuItem aboutItem = new JMenuItem("About cakehat");
        aboutItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                CakehatAboutBox.display(_adminView);
            }
        });
        helpMenu.add(aboutItem); 
    }
}