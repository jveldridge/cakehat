package cakehat.logging;

import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.email.EmailManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import support.ui.FormattedLabel;
import support.ui.PaddingPanel;

/**
 *
 * @author jak2
 */
class DefaultThrowableView extends JDialog
{
    /**
     * Displays the {@code message} and stack trace of {@code error}.
     *
     * @param message
     * @param error
     */
    static void display(final String message, final Throwable error)
    {
        //If on the UI thread show the view immediately
        if(EventQueue.isDispatchThread())
        {
            new DefaultThrowableView(message, error);
        }
        //Otherwise show the view on the UI thread when next possible
        else
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    new DefaultThrowableView(message, error);
                }
            });
        }
    }

    private DefaultThrowableView(String message, Throwable error)
    {
        super(null, "[cakehat] Error Encountered", ModalityType.APPLICATION_MODAL);

        this.initComponents(message, error);

        this.pack();
        this.setMinimumSize(new Dimension(650, 500));
        this.setSize(new Dimension(800, 500));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    private void initComponents(final String message, final Throwable error)
    {
        //Panel to hold everything
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.add(new PaddingPanel(panel, PaddingPanel.DEFAULT_PAD, panel.getBackground()));

        //Description
        panel.add(Box.createVerticalStrut(5));
        panel.add(FormattedLabel.asContent("cakehat has encountered an error, please report this if you have " +
                "not already."));

        //Message
        if(message != null || (error != null && error.getMessage() != null))
        {   
            panel.add(Box.createVerticalStrut(10));
            panel.add(FormattedLabel.asHeader("Message"));
            panel.add(Box.createVerticalStrut(5));
            final JTextArea messageArea = new JTextArea();
            messageArea.setAlignmentX(LEFT_ALIGNMENT);
            messageArea.setCaretPosition(0);
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEnabled(false);
            messageArea.setFocusable(false);
            messageArea.setBackground(Color.LIGHT_GRAY);
            messageArea.setDisabledTextColor(Color.BLACK);

            JScrollPane messageScrollPane = new JScrollPane(messageArea);
            messageScrollPane.setAlignmentX(LEFT_ALIGNMENT);
            messageScrollPane.setMinimumSize(new Dimension(0, 65));
            panel.add(messageScrollPane);
            
            if(message != null)
            {
                messageArea.setText(message);
            }
            else
            {
                messageArea.setText(error.getMessage());
            }
        }

        //Error
        if(error != null)
        {
            panel.add(Box.createVerticalStrut(10));
            panel.add(FormattedLabel.asHeader("Stack Trace"));
            panel.add(Box.createVerticalStrut(5));

            JTextArea errorTextArea = new JTextArea(DefaultThrowableReporter.getStackTraceAsString(error));
            errorTextArea.setFont(new Font("Monospaced", Font.PLAIN, errorTextArea.getFont().getSize()));
            errorTextArea.setTabSize(4);
            errorTextArea.setCaretPosition(0);
            errorTextArea.setEditable(false);
            errorTextArea.setEnabled(false);
            errorTextArea.setFocusable(false);
            errorTextArea.setBackground(Color.LIGHT_GRAY);
            errorTextArea.setDisabledTextColor(Color.BLACK);

            JScrollPane errorScrollPane = new JScrollPane(errorTextArea);
            errorScrollPane.setAlignmentX(LEFT_ALIGNMENT);
            errorScrollPane.setMinimumSize(new Dimension(0, 150));
            panel.add(errorScrollPane);
        }

        //Comments
        panel.add(Box.createVerticalStrut(10));
        panel.add(FormattedLabel.asHeader("Comments"));
        panel.add(FormattedLabel.asContent("Please provide any additional information that will help us to reproduce " +
                "this issue").grayOut());
        panel.add(Box.createVerticalStrut(5));

        final JTextArea commentsTextArea = new JTextArea();
        commentsTextArea.setTabSize(4);
        JScrollPane commentsScrollPane = new JScrollPane(commentsTextArea);
        commentsScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        commentsScrollPane.setMinimumSize(new Dimension(0, 65));
        panel.add(commentsScrollPane);

        //Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        panel.add(buttonPanel);

        //Send error report
        final JButton sendButton = new JButton("Send Error Report");
        buttonPanel.add(sendButton);
        if (Allocator.getEmailManager().getEmailAccountStatus() != EmailManager.EmailAccountStatus.AVAILABLE)
        {
            sendButton.setEnabled(false);
            sendButton.setToolTipText("Sending email is not available");
        }
        sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                sendButton.setEnabled(false);
                sendButton.setText("Sending...");
                
                //Email on the UI thread on the next loop through, this will allow the button to be visually updated
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultThrowableReporter.emailErrorReport(message, error, commentsTextArea.getText());
                        DefaultThrowableView.this.dispose();
                    }
                });
            }
        });

        //Close
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                DefaultThrowableView.this.dispose();
            }
        });
    }
    
    //Test main
    public static void main(String args[]) throws CakehatException
    {
        CakehatMain.initializeForTesting();

        try
        {
            throw new RuntimeException("Mo' code, mo' problems");
        }
        catch(Exception e)
        {
            DefaultThrowableView.display("A message that is quite long because it is going to require wrapping due " +
                    "to its long length and that is just a good test of what is occurring.", e);
        }
    }
}