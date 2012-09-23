package support.ui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import support.utils.LongRunningTask;

/**
 * A dialog which shows the progress of a {@link LongRunningTask} and allows for cancellation.
 *
 * @author jak2
 */
public class ProgressDialog extends JDialog
{   
    public static interface ExceptionReporter
    {
        public void report(String message, Exception exception);
    }
    
    private final ExceptionReporter _exceptionReporter;
    private final JProgressBar _progressBar;
    private final JTextPane _progressStatusPane;
    private final JLabel _stepDescriptionLabel;
    private final Component _progressPad;
    
    private final JButton _cancelButton;
    private final JButton _closeButton;
    
    /**
     * Constructs and displays a progress dialog for the provided {@code task}.
     * 
     * @param owner the owner of this dialog, may be {@code null}
     * @param positionRelativeTo the window this dialog will be positioned relative to, may be {@code null}
     * @param title the title of this dialog
     * @param task the task being displayed, do <strong>not</strong> call {@link LongRunningTask#start()} on it - that
     * will be done by this dialog
     * @param exceptionReporter exceptions encountered while running the task will be provided to this reporter
     */
    public static void show(Window owner, Window positionRelativeTo, String title, LongRunningTask task,
            ExceptionReporter exceptionReporter)
    {
        new ProgressDialog(owner, positionRelativeTo, title, task, exceptionReporter);
    }
    
    private ProgressDialog(Window owner, Window positionRelativeTo, String title, final LongRunningTask task,
            ExceptionReporter exceptionReporter)
    {
        super(owner, title);
        
        //Initialize
        _exceptionReporter = exceptionReporter;
        _progressBar = new JProgressBar();
        _progressStatusPane = new JTextPane();
        _progressPad = Box.createVerticalStrut(5);
        _stepDescriptionLabel = new JLabel();
        _cancelButton = new JButton("Cancel");
        _closeButton = new JButton("Close");
        initUI(task);
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
        this.setLocationRelativeTo(positionRelativeTo);
        this.setVisible(true);
        
        //Start the task
        task.start();
    }
    
    private void initUI(final LongRunningTask task)
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        this.add(new PaddingPanel(contentPanel));
        
        //Step description
        _stepDescriptionLabel.setPreferredSize(new Dimension(500, 20));
        _stepDescriptionLabel.setText(" ");
        _stepDescriptionLabel.setHorizontalAlignment(JLabel.CENTER);
        _stepDescriptionLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(_stepDescriptionLabel);
        
        contentPanel.add(_progressPad); 
        
        //Progress bar
        _progressBar.setPreferredSize(new Dimension(500, 20));
        _progressBar.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(_progressBar);
        
        //Status pane
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        _progressStatusPane.setEditable(false);
        _progressStatusPane.setEditorKit(htmlEditorKit);
        _progressStatusPane.setDocument(htmlEditorKit.createDefaultDocument());
        _progressStatusPane.setEditable(false);
        _progressStatusPane.setFocusable(false);
        _progressStatusPane.setAutoscrolls(true);
        _progressStatusPane.setBackground(contentPanel.getBackground());
        
        JScrollPane progressStatusScrollPane = new JScrollPane(_progressStatusPane);
        progressStatusScrollPane.getViewport().setBackground(contentPanel.getBackground());
        progressStatusScrollPane.setBackground(contentPanel.getBackground());
        progressStatusScrollPane.setBorder(null);
        progressStatusScrollPane.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(progressStatusScrollPane, BorderLayout.CENTER);
        
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
            public void taskStepFailed(final int currStep, final Exception cause, final String msg)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _progressBar.setValue(currStep);
                        
                        _exceptionReporter.report(msg, cause);
                    }
                });
            }

            @Override
            public void taskCompleted(final String message)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _stepDescriptionLabel.setVisible(false);
                        _progressPad.setVisible(false);
                        _progressBar.setVisible(false);
                        
                        _progressStatusPane.setText(message);
                        _progressStatusPane.setVisible(true);
                        _progressStatusPane.setCaretPosition(0);
                        
                        _cancelButton.setVisible(false);
                        _closeButton.setVisible(true);
                        
                        Dimension size = ProgressDialog.this.getSize();
                        size.height *= 2;
                        ProgressDialog.this.setSize(size);
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
                        _stepDescriptionLabel.setVisible(false);
                        _progressPad.setVisible(false);
                        _progressBar.setVisible(false);
                        
                        _progressStatusPane.setText("<html><center><h2><font face='dialog'>Canceled" +
                                "</font></h2></center></html>");
                        _progressStatusPane.setVisible(true);
                        
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
                        _stepDescriptionLabel.setVisible(false);
                        _progressPad.setVisible(false);
                        _progressBar.setVisible(false);
                        
                        _progressStatusPane.setText("<html><center><h2><font face='dialog'>Failure Occurred" +
                                "</font></h2></center></html>");
                        _progressStatusPane.setVisible(true);
                        
                        _cancelButton.setVisible(false);
                        _closeButton.setVisible(true);
                        
                        _exceptionReporter.report(msg == null ? "Long running task failed" : msg, cause);
                    }
                });
            }
        });
    }
}