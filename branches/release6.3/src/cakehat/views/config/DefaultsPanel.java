package cakehat.views.config;

import cakehat.Allocator;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import cakehat.logging.ErrorReporter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import support.ui.FormattedLabel;
import support.ui.PreferredHeightJPanel;

/**
 *
 * @author jak2
 */
class DefaultsPanel extends JPanel
{
    private static final String WORKER_TAG = "DEFAULTS";
    
    private final UniqueElementSingleThreadWorker _worker;
    
    private final JPanel _contentPanel;
    
        
    DefaultsPanel(UniqueElementSingleThreadWorker worker)
    {
        _worker = worker;
                
        this.setLayout(new BorderLayout(0, 0));
        
        _contentPanel = new JPanel();
        this.add(_contentPanel);
        
        this.initialize();
    }
    
    private void initialize()
    {
        _contentPanel.removeAll();
        _contentPanel.setLayout(new BorderLayout(0, 0));
        
        _contentPanel.add(FormattedLabel.asHeader("Initializing...").centerHorizontally(), BorderLayout.CENTER);
        
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        _contentPanel.add(loadingBar, BorderLayout.SOUTH);
        
        
        _worker.submit(null, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //Retrieve properties database, create new values if not in database
                    DbPropertyValue<Boolean> attachHandinDb = Allocator.getDatabase()
                            .getPropertyValue(DbPropertyKey.ATTACH_DIGITAL_HANDIN);
                    final DbPropertyValue<Boolean> attachHandinProp =
                            (attachHandinDb == null ? new DbPropertyValue<Boolean>(false) : attachHandinDb);
                    
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {   
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));

                            _contentPanel.add(new DbBooleanPropertyValuePanel("Attach Digital Handins", "If enabled, " +
                                    "then by default when emailing grading for a gradable event with digital " +
                                    "handins, the handins along with any modifications made will be included. TAs " +
                                    "will still have the option of manually overriding this default.",
                                    DbPropertyKey.ATTACH_DIGITAL_HANDIN, attachHandinProp));

                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();
                        }
                    });
                }
                catch(final SQLException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BorderLayout(0, 0));

                            //Error message
                            _contentPanel.add(FormattedLabel.asHeader("Unable to retrieve defaults information from " +
                                    "the database").centerHorizontally().showAsErrorMessage(), BorderLayout.CENTER);

                            //Option to retry
                            JButton retryButton = new JButton("Retry");
                            retryButton.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent ae)
                                {
                                    initialize();
                                }
                            });
                            JPanel retryPanel = new JPanel();
                            retryPanel.add(retryButton);
                            _contentPanel.add(retryPanel, BorderLayout.SOUTH);

                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();

                            //Show error report
                            ErrorReporter.report(e);
                        }
                    });
                }
            }
        });
    }
    
    private class DbBooleanPropertyValuePanel extends PreferredHeightJPanel
    {
        DbBooleanPropertyValuePanel(String name, String description, final DbPropertyKey<Boolean> key,
                final DbPropertyValue<Boolean> value)
        {   
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            //Descriptive text
            JPanel descriptionPanel = new JPanel(new BorderLayout(0, 0));
            
            final JCheckBox headerCheckBox = new JCheckBox(name, value.getValue());
            headerCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerCheckBox.setFont(new Font("Dialog", Font.BOLD, 16));
            descriptionPanel.add(headerCheckBox, BorderLayout.NORTH);
            
            descriptionPanel.add(Box.createVerticalStrut(3), BorderLayout.CENTER);
            
            JLabel descriptionLabel = FormattedLabel.asContent(description).usePlainFont();
            descriptionPanel.add(descriptionLabel, BorderLayout.SOUTH);
            this.add(descriptionPanel);
         
            //Vertical space
            this.add(Box.createVerticalStrut(10));
            
            headerCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    final boolean selected = headerCheckBox.isSelected();
                    
                    _worker.submit(WORKER_TAG, new DbRunnable(_worker, value)
                     {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            value.setValue(selected);
                            Allocator.getDatabase().putPropertyValue(key, value);
                        }

                        @Override
                        public void onDbCallFailure()
                        {
                            value.setValue(!selected);
                            headerCheckBox.setSelected(value.getValue());
                        }

                        @Override
                        public void onFinalFailureNow()
                        {
                            _worker.cancel(WORKER_TAG);
                        }

                        @Override
                        public void onFinalFailureLater()
                        {
                            initialize();
                        }
                    });
                }
            });
        }
    }
}