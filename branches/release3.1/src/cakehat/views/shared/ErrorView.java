package cakehat.views.shared;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import cakehat.Allocator;
import cakehat.CakehatMain;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Displays an error that occurs during runtime. Allows for sending reporting
 * the error via email to cakehat@cs.brown.edu.
 *
 * @author jak2
 */
public class ErrorView extends JFrame
{
    private static final int TEXT_AREA_WIDTH = 650;
    private static final int HORIZONTAL_GAP = 15;
    private static final int PANEL_WIDTH = TEXT_AREA_WIDTH + HORIZONTAL_GAP * 2;
    private static final Dimension MESSAGE_AREA_SIZE = new Dimension(TEXT_AREA_WIDTH, 65);
    private static final Dimension ERROR_AREA_SIZE = new Dimension(TEXT_AREA_WIDTH, 150);
    private static final Dimension COMMENT_AREA_SIZE = new Dimension(TEXT_AREA_WIDTH, 65);
    private static final Dimension BUTTON_AREA_SIZE = new Dimension(PANEL_WIDTH, 30);

    private static final String DESCRIPTION =
            "<html><b>cakehat has encountered an error.</b><br/>" +
            "Things should still work, but they may not. " +
            "If you have not already, you may want to report this.</html>";

    /**
     * Displays the <code>message</code> and stack trace of <code>t</code>.
     *
     * @param t
     * @param message
     */
    public ErrorView(Throwable t, String message)
    {
        super("[cakehat] Error Encountered");

        String stackTrace = null;
        if(t != null)
        {
            stackTrace = throwableAsString(t);
            printThrowableForDeveloper(t);
        }
        this.initComponents(message, stackTrace);

        this.pack();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Displays the <code>message</code>. No space will be used to display an
     * empty area for a stack trace.
     * 
     * @param message
     */
    public ErrorView(String message)
    {
        this(null, message);
    }

    /**
     * Displays {@link Throwable#getMessage()} as the message and the stack
     * trace of <code>t</code>.
     *
     * @param t
     */
    public ErrorView(Throwable t)
    {
        this(t, t.getMessage());
    }

    private static String throwableAsString(Throwable throwable)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        try
        {
            stringWriter.close();
        }
        //Well...if we encounter an exception while trying to show an exception
        //we are just screwed so forget about doing anything about it
        catch (IOException ex) { }

        return stringWriter.toString();
    }

    private static void printThrowableForDeveloper(Throwable throwable)
    {
        //If cakehat is running in developer mode or cakehat is not running
        //normally, then print the stack trace to aid debugging (most IDEs
        //allow for clicking inside the stack trace to navigate to the
        //corresponding file and line)
        if(CakehatMain.isDeveloperMode() || !CakehatMain.didStartNormally())
        {
            System.err.println("Throwable encountered. During normal operation " +
                    "cakehat will not print the stack trace to the terminal.");
            throwable.printStackTrace();
        }
    }

    private void initComponents(final String message, final String stackTrace)
    {
        // Panel to hold everything
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, HORIZONTAL_GAP, 0));
        this.add(panel);
        int totalHeight = 0;

        // Description
        panel.add(Box.createRigidArea(new Dimension(PANEL_WIDTH, 5)));
        totalHeight += 5;
        JLabel descriptionLabel = new JLabel(DESCRIPTION);
        descriptionLabel.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        totalHeight += 30;
        panel.add(descriptionLabel);

        // Message
        panel.add(Box.createRigidArea(new Dimension(PANEL_WIDTH, 10)));
        totalHeight += 10;
        JLabel messageLabel = new JLabel("Message");
        messageLabel.setPreferredSize(new Dimension(TEXT_AREA_WIDTH, 15));
        totalHeight += 15;
        panel.add(messageLabel);
        final JTextArea messageArea = new JTextArea(message);
        messageArea.setCaretPosition(0);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEnabled(false);
        messageArea.setFocusable(false);
        messageArea.setBackground(Color.LIGHT_GRAY);
        messageArea.setDisabledTextColor(Color.BLACK);

        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setPreferredSize(MESSAGE_AREA_SIZE);
        panel.add(messageScrollPane);
        totalHeight += MESSAGE_AREA_SIZE.height;

        // Stack Trace
        if(stackTrace != null)
        {
            panel.add(Box.createRigidArea(new Dimension(PANEL_WIDTH, 10)));
            totalHeight += 10;
            JLabel stackTraceLabel = new JLabel("Stack Trace");
            stackTraceLabel.setPreferredSize(new Dimension(TEXT_AREA_WIDTH, 15));
            totalHeight += 15;
            panel.add(stackTraceLabel);

            final JTextArea errorTextArea = new JTextArea(stackTrace);
            errorTextArea.setTabSize(4);
            errorTextArea.setCaretPosition(0);
            errorTextArea.setEditable(false);
            errorTextArea.setEnabled(false);
            errorTextArea.setFocusable(false);
            errorTextArea.setBackground(Color.LIGHT_GRAY);
            errorTextArea.setDisabledTextColor(Color.BLACK);

            JScrollPane errorScrollPane = new JScrollPane(errorTextArea);
            errorScrollPane.setPreferredSize(ERROR_AREA_SIZE);
            panel.add(errorScrollPane);
            totalHeight += ERROR_AREA_SIZE.height;
        }

        // Comments
        panel.add(Box.createRigidArea(new Dimension(PANEL_WIDTH, 10)));
        totalHeight += 10;
        JLabel commentsLabel = new JLabel("Comments");
        commentsLabel.setPreferredSize(new Dimension(TEXT_AREA_WIDTH, 15));
        totalHeight += 15;
        panel.add(commentsLabel);

        final JTextArea commentsTextArea = new JTextArea();

        // Make the comments box properly handle receiving new lines
        commentsTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "newline");
        commentsTextArea.setLineWrap(true);
        commentsTextArea.setWrapStyleWord(true);
        commentsTextArea.getActionMap().put("newline", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                commentsTextArea.append("\n");
            }
        });

        final JScrollPane commentsScrollPane = new JScrollPane(commentsTextArea);
        commentsScrollPane.setPreferredSize(COMMENT_AREA_SIZE);
        panel.add(commentsScrollPane);
        totalHeight += COMMENT_AREA_SIZE.height;

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(BUTTON_AREA_SIZE);
        panel.add(buttonPanel);
        totalHeight += BUTTON_AREA_SIZE.height;

        // Send error report
        JButton sendButton = new JButton("Send Error Report");
        buttonPanel.add(sendButton);
        sendButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                String from = Allocator.getUserUtilities().getUserLogin() + "@" +
                              Allocator.getConstants().getEmailDomain();
                String[] to = new String[] { Allocator.getConstants().getCakehatEmailAddress() };
                String subject = "[cakehat] Error Report";

                String body = "[This is an autogenerated error report] \n\n";
                body += "<strong>Comments</strong>\n" + commentsTextArea.getText() + "\n\n";
                body += "<strong>Message</strong>\n" + message + "\n\n";
                if(stackTrace != null)
                {
                    body += "<strong>Stack Trace</strong>\n" + stackTrace;
                }
                body = body.replace(System.getProperty("line.separator"), "<br/>");

                Allocator.getConfigurationInfo().getEmailAccount().sendMail(from, to, null, null, subject, body, null);

                ErrorView.this.dispose();
            }
        });

        // Cancel
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                ErrorView.this.dispose();
            }
        });

        // Set the size of the panel based on its contents
        totalHeight += 5;
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, totalHeight));
    }

    //For testing
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                    
                    throw new RuntimeException("Mo' code, mo' problems");
                }
                catch(Exception e)
                {
                    new ErrorView(e, "A message that is quite long because it is " +
                            "going to require wrapping due to its long length " +
                            "and that is just a good test of what is occurring.");
                }
            }
        });
    }
}