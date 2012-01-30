package cakehat.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.Part;
import cakehat.database.Student;

/**
 * Displays the progress of exporting.
 *
 * @author jak2
 */
public class ProgressView extends JFrame
{
    private JLabel _statusLabel;
    private JProgressBar _progressBar;
    private int _numSteps;
    private static Dimension SIZE = new Dimension(450,200);
    private JButton _button;
    private Exporter _exporter;
    private boolean _attemptingToCancel = false;
    private boolean _exportComplete = false;

    public ProgressView(int steps, Exporter exporter)
    {
        super("Grade Export Progress");

        _numSteps = steps;
        _exporter = exporter;

        this.initializeComponents();

        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent we)
            {
                if(!_exportComplete)
                {
                    _exporter.cancelExport();
                }
                ProgressView.this.dispose();
            }
        });
    }

    private void initializeComponents()
    {
        this.setSize(SIZE);
        this.setPreferredSize(SIZE);

        JPanel panel = new JPanel(new BorderLayout());

        _statusLabel = new JLabel("Export status");
        _progressBar = new JProgressBar(0, _numSteps);
        _button = new JButton("Cancel Export");
        _button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if(_exportComplete)
                {
                    ProgressView.this.dispose();
                }
                else
                {
                    _exporter.cancelExport();
                    _attemptingToCancel = true;
                }
            }
        });

        panel.add(_statusLabel, BorderLayout.NORTH);
        panel.add(_progressBar, BorderLayout.CENTER);
        panel.add(_button, BorderLayout.SOUTH);

        this.add(panel);

        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    public void updateProgress(Student currStudent, Assignment currAssignment, Part currPart, int currStep)
    {
        //Percent so far
        double percentComplete = ( (double) currStep ) / ( (double) _numSteps ) * 100.0;
        percentComplete = Allocator.getGeneralUtilities().round(percentComplete, 2);

        //Current status
        String status = "";
        if(_attemptingToCancel)
        {
            status = "Attempting to cancel export";
        }
        else
        {
            status = "Export in progress";
        }

        _progressBar.setValue(currStep);
        _statusLabel.setText("<html>" +
                             "<b>" + status + "</b><br/>" +
                             "<b>Student: </b>"+ currStudent.getLogin() + "<br/>" +
                             "<b>Assignment: </b>" + currAssignment.getName() + "<br/>" +
                             "<b>Part: </b>" + currPart.getName() + "<br/> <br/>" +
                             "<b>Completed " + percentComplete + "%" +
                             "</b></html>");
    }

    public void notifyCancel()
    {
        _progressBar.setValue(0);
        _statusLabel.setText("<html><b>Export canceled</b></html>");
    }

    public void notifyComplete()
    {
        _progressBar.setValue(_progressBar.getMaximum());
        _statusLabel.setText("<html><b>Export complete</b></html>");
        _exportComplete = true;
        _button.setText("Close");
    }
}