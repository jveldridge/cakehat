package gradesystem.views.shared;

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
import gradesystem.Allocator;
import java.awt.Color;

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
    private static final Dimension ERROR_AREA_SIZE = new Dimension(TEXT_AREA_WIDTH, 200);
    private static final Dimension COMMENT_AREA_SIZE = new Dimension(TEXT_AREA_WIDTH, 100);
    private static final Dimension BUTTON_AREA_SIZE = new Dimension(PANEL_WIDTH, 30);
    private static final Dimension SIZE = new Dimension(PANEL_WIDTH, ERROR_AREA_SIZE.height +
                                                                     COMMENT_AREA_SIZE.height +
                                                                     BUTTON_AREA_SIZE.height + 100);
    private static final String DESCRIPTION =
            "<html><b>You have encountered an error.</b><br/>" +
            "Things should still work, but they may not. " +
            "If you have not already, you may want to report this.</html>";

    public ErrorView(String customMessage)
    {
        super("Error encountered");

        this.initComponentsWithErrorMessage(customMessage);

        this.pack();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public ErrorView()
    {
        this("");
    }

    public ErrorView(Throwable t)
    {
        this(throwableAsString(t));
    }

    public ErrorView(Throwable t, String customMessage)
    {
        this(customMessage + "\n\n" + throwableAsString(t));
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

    private void initComponentsWithErrorMessage(String errorMessage)
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, HORIZONTAL_GAP, 0));
        panel.setPreferredSize(SIZE);
        this.add(panel);

        panel.add(Box.createRigidArea(new Dimension(SIZE.width, 5)));
        panel.add(new JLabel(DESCRIPTION));

        // Error message
        panel.add(Box.createRigidArea(new Dimension(SIZE.width, 10)));
        panel.add(new JLabel("Error Output:"));

        final JTextArea errorTextArea = new JTextArea(errorMessage);
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

        // Comments
        panel.add(Box.createRigidArea(new Dimension(SIZE.width, 10)));
        panel.add(new JLabel("Comments:"));

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

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(BUTTON_AREA_SIZE);
        panel.add(buttonPanel);

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
                String subject = "Grading System Error";
                String body = "[This is an autogenerated message] \n\n" +
                              commentsTextArea.getText() +
                              "\n\n" + errorTextArea.getText();
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
    }

    //For testing
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                String s = null;
                try
                {
                    s.equals("hi"); //will result in NullPointerException
                }
                catch (Exception e)
                {
                    new ErrorView(e, "ErrorView test");
                }
            }
        });
    }
}