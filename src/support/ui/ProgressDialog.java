package support.ui;

import cakehat.CakehatMain;
import cakehat.views.shared.ErrorView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import support.utils.LongRunningTask;

/**
 * A dialog which shows the progress of a {@link LongRunningTask} and allows for cancellation.
 *
 * @author jak2
 */
public class ProgressDialog extends JDialog
{   
    private final JProgressBar _progressBar;
    private final JLabel _progressStatusLabel;
    private final JLabel _stepDescriptionLabel;
    private final JButton _cancelButton;
    private final JButton _closeButton;
    
    private volatile long _startedAtTime;
    
    /**
     * Constructs and displays a progress dialog for the provided {@code task}.
     * 
     * @param parent the visual parent of this dialog, may be {@code null}
     * @param displayRelativeTo  the visual to display relative to, does not need to be the same as {@code parent}, may
     * be {@code null}
     * @param title the title of this dialog
     * @param message the message to be displayed to the user
     * @param task the task being displayed, do <strong>not</strong> call {@link LongRunningTask#start()} on it - that
     * will be done by this dialog
     */
    public ProgressDialog(Window parent, Window displayRelativeTo, String title, String message,
            final LongRunningTask task)
    {
        super(parent, title);
        
        //Initialize
        _progressBar = new JProgressBar();
        _progressStatusLabel = new JLabel();
        _stepDescriptionLabel = new JLabel();
        _cancelButton = new JButton("Cancel");
        _closeButton = new JButton("Close");
        initUI(message, task);
        initListening(task);
        
        //Cancel the task on close
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent we)
            {
                task.cancel();
            }
        });
        
        //Display
        this.pack();
        this.setMinimumSize(this.getSize());
        this.setResizable(false);
        this.setLocationRelativeTo(displayRelativeTo);
        this.setVisible(true);
        
        //Start the task
        task.start();
    }
    
    private void initUI(String message, final LongRunningTask task)
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        PaddingPanel mainPanel = new PaddingPanel(contentPanel, 10);
        this.add(mainPanel);
            
        //Message
        JLabel messageLabel = new JLabel();
        messageLabel.setText(message);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(messageLabel);
        
        contentPanel.add(Box.createVerticalStrut(5));
        
        //Step description
        _stepDescriptionLabel.setPreferredSize(new Dimension(500, 20));
        _stepDescriptionLabel.setText(" ");
        _stepDescriptionLabel.setHorizontalAlignment(JLabel.CENTER);
        _stepDescriptionLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(_stepDescriptionLabel);
        
        contentPanel.add(Box.createVerticalStrut(5)); 
        
        //Progress bar
        _progressBar.setPreferredSize(new Dimension(500, 20));
        _progressBar.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(_progressBar);
        
        //Status label
        _progressStatusLabel.setPreferredSize(new Dimension(500, 20));
        _progressStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        _progressStatusLabel.setVisible(false);
        _progressStatusLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(_progressStatusLabel);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        //Cancel
        _cancelButton.setAlignmentX(CENTER_ALIGNMENT);
        _cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _cancelButton.setEnabled(false);
                task.cancel();
            }
        });
        contentPanel.add(_cancelButton);
        
        //Close
        _closeButton.setAlignmentX(CENTER_ALIGNMENT);
        _closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                ProgressDialog.this.dispose();
            }
        });
        _closeButton.setVisible(false);
        contentPanel.add(_closeButton);
    }
    
    /**
     * Listens for events coming from the {@code task}. These events may be coming from any thread so all UI
     * interactions are done through the Swing/AWT Event Queue. 
     * 
     * @param task 
     */
    private void initListening(LongRunningTask task)
    {
        task.addProgressListener(new LongRunningTask.ProgressListener()
        {
            @Override
            public void taskStarted()
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {   
                        _startedAtTime = System.currentTimeMillis();
                        _progressBar.setIndeterminate(true);
                    }
                });
            }

            @Override
            public void taskDetermined(final int totalSteps)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _progressBar.setMinimum(0);
                        _progressBar.setMaximum(totalSteps);
                        _progressBar.setValue(0);
                        _progressBar.setIndeterminate(false);
                    }
                });
            }
            
            @Override
            public void taskStepStarted(final String description)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _stepDescriptionLabel.setText(description);
                    }
                });
            }

            @Override
            public void taskStepCompleted(final int currStep)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _progressBar.setValue(currStep);
                    }
                });
            }

            @Override
            public void taskCompleted()
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _stepDescriptionLabel.setText(" ");
                        _progressBar.setVisible(false);
                        
                        long duration = System.currentTimeMillis() - _startedAtTime;
                        if(CakehatMain.isDeveloperMode())
                        {
                            _progressStatusLabel.setText("Complete (" + duration + "ms)");
                        }
                        else
                        {
                            _progressStatusLabel.setText("Complete");
                        }
                        _progressStatusLabel.setForeground(new Color(0, 179, 0));
                        _progressStatusLabel.setVisible(true);
                        
                        _cancelButton.setVisible(false);
                        _closeButton.setVisible(true);
                    }
                });
            }

            @Override
            public void taskCanceled()
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _stepDescriptionLabel.setText(" ");
                        _progressBar.setVisible(false);
                        
                        _progressStatusLabel.setText("Canceled");
                        _progressStatusLabel.setForeground(new Color(179, 0, 0));
                        _progressStatusLabel.setVisible(true);
                        
                        _cancelButton.setVisible(false);
                        _closeButton.setVisible(true);
                    }
                });
            }

            @Override
            public void taskFailed(final Exception cause, final String msg)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _stepDescriptionLabel.setText(" ");
                        _progressBar.setVisible(false);
                        
                        _progressStatusLabel.setText("Failure Occurred");
                        _progressStatusLabel.setForeground(Color.RED);
                        _progressStatusLabel.setVisible(true);
                        
                        _cancelButton.setVisible(false);
                        _closeButton.setVisible(true);
                        
                        new ErrorView(cause, msg == null ? "Long running task failed" : msg);
                    }
                });
            }
        });
    }
}