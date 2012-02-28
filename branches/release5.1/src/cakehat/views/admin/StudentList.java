package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import support.ui.DescriptionProvider;
import support.ui.DocumentAdapter;
import support.ui.GenericJList;

/**
 *
 * @author jak2
 */
class StudentList extends JPanel
{
    private final JTextField _filterField;
    private final Set<Student> _students;
    private final GenericJList<Student> _studentList;
    private final List<StudentListListener> _listeners = new CopyOnWriteArrayList<StudentListListener>();

    StudentList()
    {
        this.setLayout(new BorderLayout(0, 0));

        //Commands
        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
        this.add(commandPanel, BorderLayout.NORTH);

        JLabel studentsLabel = new JLabel("Students");
        studentsLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        studentsLabel.setAlignmentX(LEFT_ALIGNMENT);
        commandPanel.add(studentsLabel);

        commandPanel.add(Box.createVerticalStrut(5));

        JPanel selectionButtonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        selectionButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
        commandPanel.add(selectionButtonPanel);

        JButton selectAll = new JButton("All");
        selectAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _studentList.selectAll();
            }
        });
        selectionButtonPanel.add(selectAll);

        JButton selectNone = new JButton("None");
        selectNone.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                _studentList.clearSelection();;
            }
        });
        selectionButtonPanel.add(selectNone);

        commandPanel.add(Box.createVerticalStrut(5));

        JPanel filterPanel = new JPanel(new GridLayout(1, 1, 0, 0));
        filterPanel.setAlignmentX(LEFT_ALIGNMENT);
        _filterField = new JTextField();
        filterPanel.setPreferredSize(selectionButtonPanel.getPreferredSize());
        filterPanel.add(_filterField);
        _filterField.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            public void modificationOccurred(DocumentEvent de)
            {
                applyFilterTerm();
            }
        });
        commandPanel.add(filterPanel);

        commandPanel.add(Box.createVerticalStrut(5));

        // Students
        Set<Student> students = ImmutableSet.of();
        try
        {
            //Intentionally hold on to this copy because this method returns a live view of enabled students
            students = Allocator.getDataServices().getEnabledStudents();
        }
        catch(ServicesException e)
        {
            new ErrorView(e, "Unable to retrieve list of students");
        }
        _students = students;

        List<Student> sortedStudents = new ArrayList<Student>(_students);
        Collections.sort(sortedStudents);
        _studentList = new GenericJList<Student>(sortedStudents);
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _studentList.setDescriptionProvider(new DescriptionProvider<Student>()
        {
            @Override
            public String getDisplayText(Student student)
            {
                return student.getLogin();
            }

            @Override
            public String getToolTipText(Student student)
            {
                return student.getName();
            }
        });
        JScrollPane studentPane = new JScrollPane(_studentList);
        this.add(studentPane);

        _studentList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent lse)
            {
                if(!lse.getValueIsAdjusting())
                {
                    List<Student> selected = _studentList.getGenericSelectedValues();
                    for(StudentListListener listener : _listeners)
                    {
                        listener.selectionChanged(selected);
                    }
                }
            }
        });
    }
    
    void applyFilterTerm()
    {
        String filterTerm = _filterField.getText();
        List<Student> matchingStudents = new ArrayList<Student>();

        if(filterTerm == null || filterTerm.isEmpty())
        {
            List<Student> allStudentsSorted = new ArrayList<Student>(_students);
            Collections.sort(allStudentsSorted);
            matchingStudents.addAll(allStudentsSorted);
        }
        else
        {
            for(Student student : _students)
            {
                if(student.getLogin().startsWith(filterTerm))
                {
                    matchingStudents.add(student);
                }
            }
        }

        _studentList.setListData(matchingStudents);
    }

    JTextField getFilterField()
    {
        return _filterField;
    }

    GenericJList<Student> getList()
    {
        return _studentList;
    }

    List<Student> getSelection()
    {
        return _studentList.getGenericSelectedValues();
    }

    void addSelectionListener(StudentListListener listener)
    {
        _listeners.add(listener);
    }

    void removeSelectionListener(StudentListListener listener)
    {
        _listeners.remove(listener);
    }

    static interface StudentListListener
    {
        public void selectionChanged(List<Student> selection);
    }
}