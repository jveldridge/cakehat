package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.services.ServicesException;
import cakehat.services.UserServices.ValidityCheck;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import support.ui.ShadowJTextField;
import support.utils.posix.NativeException;

/**
 * A view that allows for adding all students in the student group or an
 * individual student.
 *
 * @author jak2
 */
class AddStudentsView extends JFrame
{
    private final ShadowJTextField _loginField, _firstNameField, _lastNameField;

    public AddStudentsView()
    {
        super("Add Students");

        // Padding
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

        // Panel that holds the other panels
        JPanel overallPanel = new JPanel(new BorderLayout(0, 0));
        this.add(overallPanel, BorderLayout.CENTER);

        // Adding from student group
        JPanel groupPanel = new JPanel(new BorderLayout(0, 0));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Student Group"));
        overallPanel.add(groupPanel, BorderLayout.NORTH);
        groupPanel.add(new JLabel("Add all students in the " +
                Allocator.getCourseInfo().getStudentGroup() + " group"), BorderLayout.NORTH);

        JPanel groupButtonPanel = new JPanel();
        JButton groupButton = new JButton("Add All Students");
        groupPanel.add(groupButtonPanel, BorderLayout.SOUTH);
        groupButtonPanel.add(groupButton);
        groupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AddStudentsView.this.addStudentGroupToDatabase();
            }
        });

        // Gap between student group and individual student
        overallPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        // Adding an individual student
        JPanel individualPanel = new JPanel(new BorderLayout(0, 0));
        individualPanel.setBorder(BorderFactory.createTitledBorder("Individual Student"));
        overallPanel.add(individualPanel, BorderLayout.SOUTH);

        _loginField = new ShadowJTextField("Login (required)");
        individualPanel.add(_loginField, BorderLayout.NORTH);

        JPanel namePanel = new JPanel(new GridLayout(1, 2));
        individualPanel.add(namePanel, BorderLayout.CENTER);

        _firstNameField = new ShadowJTextField("First Name (optional)");
        namePanel.add(_firstNameField);
        _lastNameField = new ShadowJTextField("Last Name (optional)");
        namePanel.add(_lastNameField);

        JPanel individualButtonPanel = new JPanel();
        individualPanel.add(individualButtonPanel, BorderLayout.SOUTH);
        JButton individualButton = new JButton("Add Student");
        individualButtonPanel.add(individualButton);
        individualButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AddStudentsView.this.addIndividualStudentToDatabase();
            }
        });

        this.pack();
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addStudentGroupToDatabase()
    {
        Collection<String> studentsNotAdded = new LinkedList<String>();
        try
        {
            for(String login : Allocator.getUserServices().getStudentLogins())
            {
                try
                {
                    Allocator.getUserServices().addStudent(login, ValidityCheck.BYPASS);
                }
                catch (ServicesException ex)
                {
                    studentsNotAdded.add(login);
                }
            }

            if(!studentsNotAdded.isEmpty())
            {
                new ErrorView("The following students were not added to the database: " +
                              studentsNotAdded + ".");
            }
        }
        catch(NativeException ex)
        {
            new ErrorView(ex, "Unable to add students because student logins could not be retrieved");
        }
    }

    private void addIndividualStudentToDatabase()
    {
        String login = _loginField.getText();

        if(login == null || login.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "No login provided");
        }
        else
        {
           try
            {
                String firstName = _firstNameField.getText();
                String lastName = _lastNameField.getText();
                if(firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty())
                {
                    Allocator.getUserServices().addStudent(login, ValidityCheck.CHECK);
                }
                else
                {
                    Allocator.getUserServices().addStudent(login, firstName, lastName, ValidityCheck.CHECK);
                }
            }
            catch (ServicesException ex)
            {
                new ErrorView(ex, "Adding student " + login + " to the database failed.");
            }
        }
    }

    public static void main(String[] args) throws Throwable
    {
        UIManager.setLookAndFeel(new MetalLookAndFeel());

        AddStudentsView view = new AddStudentsView();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }
}