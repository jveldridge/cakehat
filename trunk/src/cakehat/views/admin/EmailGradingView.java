package cakehat.views.admin;

import cakehat.CakehatSession;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Student;
import cakehat.logging.ErrorReporter;
import cakehat.services.EmailGradingTask;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import support.ui.DocumentAdapter;
import support.ui.FormattedLabel;
import support.ui.GenericJComboBox;
import support.ui.PaddingPanel;
import support.ui.ProgressDialog;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
class EmailGradingView extends JDialog
{
    private static final String EMAIL_STUDENTS = "Students", EMAIL_ALTERNATE = "Alternate address";
    
    private final Window _owner;
    
    private final AssignmentTreeSelection _asgnSelection;
    private final Set<Part> _parts;
    private final Set<Student> _students;
    
    private final GenericJComboBox<String> _toComboBox;
    private final JTextField _toStudentsField;
    private final EmailAddressField _toAlternateAddressField;
    private final JTextField _subjectField;
    private final JCheckBox _attachHandinsCheckBox;
    private final JTextArea _bodyArea;
    private final JButton _sendButton;
    
    EmailGradingView(AssignmentTreeSelection asgnSelection, Set<Student> students, Window owner)
    {
        super(owner, "Email Grading", ModalityType.MODELESS);
        
        _owner = owner;
        
        _asgnSelection = asgnSelection;
        _students = students;
        
        _toComboBox = new GenericJComboBox<String>();
        _toStudentsField = new JTextField();
        _toAlternateAddressField = new EmailAddressField();
        _subjectField = new JTextField();
        _attachHandinsCheckBox = new JCheckBox();
        _bodyArea = new JTextArea();
        _sendButton = new JButton();
        
        ImmutableSet.Builder<Part> partsBuilder = ImmutableSet.builder();
        if(_asgnSelection.getPart() != null)
        {
            partsBuilder.add(_asgnSelection.getPart());
        }
        else if(_asgnSelection.getGradableEvent() != null)
        {
            for(Part part : _asgnSelection.getGradableEvent())
            {
                partsBuilder.add(part);
            }
        }
        else
        {
            for(GradableEvent ge : _asgnSelection.getAssignment().getGradableEvents())
            {
                for(Part part : ge)
                {
                    partsBuilder.add(part);
                }
            }
        }
        _parts = partsBuilder.build();
        
        this.initUI();
        this.pack();
        this.setMinimumSize(new Dimension(640, 360));
        this.setPreferredSize(new Dimension(640, 360));
        this.setLocationRelativeTo(owner);
        this.setVisible(true);
    }
    
    private void initUI()
    {
        JPanel contentPanel = new JPanel();
        this.setContentPane(new PaddingPanel(contentPanel, 10, contentPanel.getBackground()));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        final JPanel toPanel = new JPanel();
        toPanel.setAlignmentX(LEFT_ALIGNMENT);
        toPanel.setLayout(new BorderLayout(0, 0));
        toPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        contentPanel.add(toPanel);
        
        JPanel toChoicePanel = new JPanel();
        toChoicePanel.setLayout(new BoxLayout(toChoicePanel, BoxLayout.X_AXIS));
        toChoicePanel.add(FormattedLabel.asSubheader("To"));
        toChoicePanel.add(Box.createHorizontalStrut(38));
        toPanel.add(toChoicePanel, BorderLayout.WEST);
        
        _toComboBox.setAlignmentX(LEFT_ALIGNMENT);
        _toComboBox.setPreferredSize(new Dimension(140, 20));
        _toComboBox.setItems(ImmutableSet.of(EMAIL_STUDENTS, EMAIL_ALTERNATE));
        _toComboBox.addSelectionListener(new SelectionListener<String>()
        {
            @Override
            public void selectionPerformed(String currValue, String newValue, SelectionAction action)
            {
                if(newValue == EMAIL_STUDENTS)
                {
                    toPanel.remove(_toAlternateAddressField);
                    toPanel.add(_toStudentsField, BorderLayout.CENTER);
                    _sendButton.setEnabled(true);
                    _sendButton.setToolTipText(null);
                }
                else
                {
                    toPanel.remove(_toStudentsField);
                    toPanel.add(_toAlternateAddressField, BorderLayout.CENTER);
                    _sendButton.setEnabled(_toAlternateAddressField.isValidEmailAddress());
                }
                
                toPanel.repaint();
                toPanel.revalidate();
            }
        });
        toChoicePanel.add(_toComboBox);
        
        toChoicePanel.add(Box.createHorizontalStrut(3));
        
        _toStudentsField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        _toStudentsField.setEnabled(false);
        StringBuilder toStudentsBuilder = new StringBuilder();
        List<Student> sortedStudents = new ArrayList<Student>(_students);
        Collections.sort(sortedStudents);
        Iterator<Student> studentIter = sortedStudents.iterator();
        while(studentIter.hasNext())
        {
            toStudentsBuilder.append(studentIter.next().getLogin());
            if(studentIter.hasNext())
            {
                toStudentsBuilder.append(", ");
            }
        }
        _toStudentsField.setText(toStudentsBuilder.toString());
        toPanel.add(_toStudentsField, BorderLayout.CENTER);
        
        _toAlternateAddressField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        _toAlternateAddressField.setBorder(BorderFactory.createEtchedBorder());
        _toAlternateAddressField.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            public void modificationOccurred(DocumentEvent de)
            {
                if(_toAlternateAddressField.isVisible())
                {
                    _sendButton.setEnabled(_toAlternateAddressField.isValidEmailAddress());
                }
            }
        });
        
        contentPanel.add(Box.createVerticalStrut(5));
        
        JPanel subjectPanel = new JPanel(new BorderLayout(0, 0));
        subjectPanel.setAlignmentX(LEFT_ALIGNMENT);
        subjectPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        contentPanel.add(subjectPanel);
        JPanel subjectTitlePanel = new JPanel();
        subjectTitlePanel.setLayout(new BoxLayout(subjectTitlePanel, BoxLayout.X_AXIS));
        subjectPanel.add(subjectTitlePanel, BorderLayout.WEST);
        
        subjectTitlePanel.add(FormattedLabel.asSubheader("Subject"));
        subjectTitlePanel.add(Box.createHorizontalStrut(5));
        
        _subjectField.setText("[" + CakehatSession.getCourse() + "] " + _asgnSelection.getAssignment().getName() +
                " Graded");
        _subjectField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        _subjectField.setBorder(BorderFactory.createEtchedBorder());
        subjectPanel.add(_subjectField, BorderLayout.CENTER);
        
        JPanel attachPanel = new JPanel(new BorderLayout(0, 0));
        attachPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        attachPanel.setAlignmentX(LEFT_ALIGNMENT);
        attachPanel.add(Box.createHorizontalStrut(53), BorderLayout.WEST);
        _attachHandinsCheckBox.setText("Attach digital handins");
        _attachHandinsCheckBox.setEnabled(false);
        for(Part part : _parts)
        {
            if(part.getGradableEvent().hasDigitalHandins())
            {
                _attachHandinsCheckBox.setEnabled(true);
                break;
            }
        }
        attachPanel.add(_attachHandinsCheckBox, BorderLayout.CENTER);
        contentPanel.add(attachPanel);
        
        _bodyArea.setText("Grading for " + _asgnSelection.getAssignment().getName() + " is included below.");
        //Override tab behavior to move focus instead of inserting the tab character
        _bodyArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("pressed TAB")));
        _bodyArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("shift pressed TAB")));
        _bodyArea.setAlignmentX(LEFT_ALIGNMENT);
        _bodyArea.setBorder(BorderFactory.createEtchedBorder());
        //Setting minimum size is needed to work around issue with turning on line wrapping preventing text area from
        //decreasing in size once it has increased in size
        _bodyArea.setMinimumSize(new Dimension(0, 0));
        _bodyArea.setLineWrap(true);
        _bodyArea.setWrapStyleWord(true);
        contentPanel.add(_bodyArea);
        
        contentPanel.add(Box.createVerticalStrut(5));
        
        JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sendPanel.setAlignmentX(LEFT_ALIGNMENT);
        _sendButton.setText("Send email");
        _sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                sendEmail();
            }
        });
        sendPanel.add(_sendButton);
        contentPanel.add(sendPanel);
    }
    
    private void sendEmail()
    {
        InternetAddress alternateAddress = null;
        if(_toComboBox.getSelectedItem() == EMAIL_ALTERNATE)
        {
            alternateAddress = _toAlternateAddressField.getAddress();
        }
        
        EmailGradingTask task = new EmailGradingTask(_parts, _students, alternateAddress, _subjectField.getText(),
                _bodyArea.getText(), _attachHandinsCheckBox.isSelected(), true);
        
        ProgressDialog.show(_owner, this, "Emailing Grading", task, ErrorReporter.getExceptionReporter());
        
        this.dispose();
    }
    
    private static class EmailAddressField extends JTextField
    {   
        private EmailAddressField()
        {
            this.getDocument().addDocumentListener(new DocumentAdapter()
            {
                @Override
                public void modificationOccurred(DocumentEvent de)
                {
                    updateAppearance();
                }
            });
            
            this.setBorder(BorderFactory.createEtchedBorder());
            this.updateAppearance();
        }
        
        private void updateAppearance()
        {
            if(isEnabled() && !isValidEmailAddress())
            {
                setBackground(new Color(255, 204, 204));
                setToolTipText("Invalid email address");
            }
            else
            {
                setBackground(Color.WHITE);
                setToolTipText(null);
            }
        }
        
        boolean isValidEmailAddress()
        {
            boolean valid = true;
            try
            {
                new InternetAddress(getText(), true);
            }
            catch(AddressException ex)
            {
                valid = false;
            }
            
            return valid;
        }
        
        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);
            
            this.updateAppearance();
        }
        
        /**
         * Returns the email address in this field if a valid email address and {@code null} otherwise.
         * 
         * @return 
         */
        InternetAddress getAddress()
        {
            InternetAddress address = null;
            
            String text = getText();
            if(!text.isEmpty())
            {
                try
                {
                    address = new InternetAddress(text);
                }
                //Catching this exception is ok because the caller should always call isValidEmailAddress() first
                catch(AddressException ex) { }
            }
            
            return address;
        }
    }
}