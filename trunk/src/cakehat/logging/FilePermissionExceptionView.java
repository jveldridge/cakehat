package cakehat.logging;

import cakehat.Allocator;
import cakehat.email.EmailManager;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import support.ui.FormattedLabel;
import support.ui.PaddingPanel;
import support.utils.FilePermissionException;

/**
 *
 * @author jak2
 */
class FilePermissionExceptionView extends JDialog
{
    static void display(final FilePermissionException ex)
    {
        //If on the UI thread show the view immediately
        if(EventQueue.isDispatchThread())
        {
            new FilePermissionExceptionView(ex.getFiles());
        }
        //Otherwise show the view on the UI thread when next possible
        else
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    new FilePermissionExceptionView(ex.getFiles());
                }
            });
        }
    }
    
    private FilePermissionExceptionView(Set<File> files)
    {
        super(null, "[cakehat] File Permission Issue", ModalityType.APPLICATION_MODAL);

        this.initComponents(files);

        this.pack();
        this.setMinimumSize(new Dimension(650, 200));
        this.setSize(new Dimension(650, 200));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    private void initComponents(final Set<File> files)
    {
        //Panel to hold everything
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.add(new PaddingPanel(panel, PaddingPanel.DEFAULT_PAD, panel.getBackground()));

        //Description
        panel.add(FormattedLabel.asHeader("Your course has specified files with incorrect permissions")
                .showAsErrorMessage());
        panel.add(FormattedLabel.asContent("cakehat has been configured by your course to access the following " +
                "file(s), but cannot do so.").showAsErrorMessage());
        
        //Files with issues
        panel.add(Box.createVerticalStrut(5));
        StringBuilder fileMessage = new StringBuilder("<html><font face='dialog'>");
        fileMessage.append("<ul style='list-style-type:none; margin-top:0px; margin-bottom:0px; margin-left:0px'>");
        for(File file : files)
        {
            fileMessage.append("<li>").append(file.getAbsolutePath()).append("</li>");
        }
        fileMessage.append("</ul></font></html>");
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        JTextPane fileIssueTextPane = new JTextPane(); 
        fileIssueTextPane.setEditable(false);
        fileIssueTextPane.setEditorKit(htmlEditorKit);
        fileIssueTextPane.setDocument(htmlEditorKit.createDefaultDocument());
        fileIssueTextPane.setEditable(false);
        fileIssueTextPane.setFocusable(false);
        fileIssueTextPane.setAutoscrolls(true);
        fileIssueTextPane.setBackground(panel.getBackground());
        fileIssueTextPane.setText(fileMessage.toString());
        fileIssueTextPane.setCaretPosition(0);

        JScrollPane fileScrollPane = new JScrollPane(fileIssueTextPane);
        fileScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        fileScrollPane.setBorder(null);
        fileScrollPane.setMinimumSize(new Dimension(0, 80));
        panel.add(fileScrollPane);
        
        //Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        panel.add(buttonPanel);

        //Send report about file permissions issues
        final JButton notifyButton = new JButton("Notify HTAs");
        buttonPanel.add(notifyButton);
        if(Allocator.getEmailManager().getEmailAccountStatus() != EmailManager.EmailAccountStatus.AVAILABLE)
        {
            notifyButton.setEnabled(false);
            notifyButton.setToolTipText("Sending email is not available");
        }
        notifyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                notifyButton.setEnabled(false);
                notifyButton.setText("Notifying...");
                
                //Email on the UI thread on the next loop through, this will allow the button to be visually updated
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        FilePermissionExceptionReporter.sendEmail(files);
                        FilePermissionExceptionView.this.dispose();
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
                FilePermissionExceptionView.this.dispose();
            }
        });
    }
}