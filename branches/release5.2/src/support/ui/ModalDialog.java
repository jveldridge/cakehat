package support.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Contains static methods to show a modal dialog on screen. Unlike the methods in {@link javax.swing.JOptionPane}, the
 * message is displayed inside of a scroll pane that wraps text as necessary.
 *
 * @author jak2
 */
public class ModalDialog
{
    private ModalDialog() { }
    
    /**
     * Shows a confirmation message.
     * 
     * @param title
     * @param message
     * @param trueChoice
     * @param falseChoice 
     * 
     * @return {@code true} if {@code trueChoice} was selected, {@code false} otherwise.
     */
    public static boolean showConfirmation(String title, String message, String trueChoice, String falseChoice)
    {
        //Use this is a holder for the value to reference from the action listener a final variable is needed
        final AtomicBoolean choice = new AtomicBoolean(false);
        
        final JDialog dialog = createDialog(title, message);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        JButton trueButton = new JButton(trueChoice);
        trueButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                choice.set(true);
                dialog.dispose();
            }
        });
        buttonPanel.add(trueButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        JButton falseButton = new JButton(falseChoice);
        falseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                dialog.dispose();
            }
        });
        buttonPanel.add(falseButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        showDialog(dialog);
        
        return choice.get();
    }
    
    public static void showMessage(String title, String message)
    {
        showMessage(title, message, "OK");
    }
    
    public static void showMessage(String title, String message, String buttonText)   
    {
        final JDialog dialog = createDialog(title, message);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        JButton dismissButton = new JButton(buttonText);
        dismissButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                dialog.dispose();
            }
        });
        buttonPanel.add(dismissButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        showDialog(dialog);
    }
    
    private static JDialog createDialog(String title, String message)
    {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setLayout(new BorderLayout(0, 0));
        
        JPanel messagePanel = new JPanel(new BorderLayout(0, 0));
        messagePanel.setBackground(dialog.getBackground());
        dialog.add(messagePanel, BorderLayout.CENTER);
        messagePanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        messagePanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        messagePanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        
        JTextPane messagePane = new JTextPane();
        StyledDocument doc = messagePane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributes, 14);
        StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
        messagePane.setText(message);
        doc.setParagraphAttributes(0, doc.getLength(), attributes, false);
        messagePane.setEditable(false);
        messagePane.setFocusable(false);
        messagePane.setAutoscrolls(true);
        messagePane.setBackground(dialog.getBackground());
        
        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.getViewport().setBackground(dialog.getBackground());
        scrollPane.setBackground(dialog.getBackground());
        scrollPane.setBorder(null);
        messagePanel.add(scrollPane, BorderLayout.CENTER);
        
        return dialog;
    }
    
    private static void showDialog(JDialog dialog)
    {
        dialog.setMinimumSize(new Dimension(600, 100));
        dialog.setSize(new Dimension(600, 200));
        dialog.setPreferredSize(new Dimension(600, 200));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(getFocusedFrame());
        dialog.setVisible(true);
    }
    
    private static Frame getFocusedFrame()
    {
        Frame focusedFrame = null;
        for(Frame frame : Frame.getFrames())
        {
            if(frame.isFocused())
            {
                focusedFrame = frame;
                break;
            }
        }
        
        return focusedFrame;
    }
}