package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Student;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class StudentInfoPanel extends JPanel
{
    private final JPanel _contentPanel;
    private final FormattedLabel _headerLabel;
    
    StudentInfoPanel()
    {
        this.setBackground(Color.WHITE);
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        this.add(centerPanel, BorderLayout.CENTER);
        
        _headerLabel = FormattedLabel.asHeader("");
        centerPanel.add(_headerLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        
        _contentPanel = new JPanel();
        _contentPanel.setBackground(Color.WHITE);
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        _contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(_contentPanel);
    }
    
    
    void displayFor(Set<Student> students)
    {
        _contentPanel.removeAll();
        
        try
        {
            Set<Student> studentsWithCollab = Allocator.getDataServices().getStudentsWithCollaborationContracts();


            if(students.size() == 1)
            {
                Student student = Iterables.get(students, 0);
                displayForSingleStudent(student, studentsWithCollab.contains(student));
            }
            else
            {
                displayForMultipleStudents(students, studentsWithCollab);
            }
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to load student data", e);
            _contentPanel.add(FormattedLabel.asSubheader("Unable to load student data").showAsErrorMessage()
                .centerHorizontally());
        }
        
        this.repaint();
        this.revalidate();
    }
    
    private void displayForSingleStudent(Student student, boolean hasCollab)
    {
        _headerLabel.setText(student.getName() + " (" + student.getLogin() + ")");
        
        _contentPanel.add(new CollabCheckBox("Collaboration Contract", student, hasCollab, _contentPanel.getBackground()));
    }
    
    private void displayForMultipleStudents(Set<Student> students, Set<Student> studentsWithCollab)
    {
        _headerLabel.setText("Collaboration Contract");
        
        ArrayList<Student> sortedStudents = new ArrayList<Student>(students);
        Collections.sort(sortedStudents);
        for(Student student : sortedStudents)
        {
            _contentPanel.add(new CollabCheckBox(student.getLogin() + " (" + student.getName() + ")", student,
                    studentsWithCollab.contains(student), _contentPanel.getBackground()));
        }
    }
    
    private class CollabCheckBox extends JCheckBox
    {
        CollabCheckBox(String text, final Student student, boolean hasCollab, Color background)
        {
            super(text, hasCollab);
            
            this.setBackground(background);
            this.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        Allocator.getDataServices().setStudentsHasCollaborationContract(ImmutableMap.of(student, isSelected()));
                    }
                    catch(ServicesException e)
                    {
                        setSelected(!isSelected());
                        ErrorReporter.report("Unable to update collaboration contract status", e);
                    }
                }
            });
        }
    }
}