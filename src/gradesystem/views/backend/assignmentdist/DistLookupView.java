package gradesystem.views.backend.assignmentdist;

import gradesystem.components.GenericJList;
import gradesystem.config.Assignment;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import gradesystem.Allocator;
import java.util.ArrayList;

/**
 * Provides an interface to allow administrators to view and change who is
 * grading a student for each assignment. All assignments can be viewed at once.
 *
 * @author aunger
 */
public class DistLookupView extends JFrame {

    private static final int STUDENT_LIST_HEIGHT = 300;
    private static final int STUDENT_LIST_WIDTH = 130;
    private static final int DIST_LIST_HEIGHT = 290;
    private static final int DIST_LIST_HEIGHT_NO_BUTTON = 330;
    private static final int DIST_LIST_WIDTH = 325;
    private static final int TEXT_HEIGHT = 25;
    
    private List<String> _students;
    private GenericJList<String> _studentList;
    private Collection<Assignment> _assignments;
    private JPanel _graderGrid;
    private JScrollPane _gradeScrollPane;
    private Map<Assignment, String> _graders;
    private JButton _saveButton;
    private JButton _resetButton;
    private JTextField _studentFilterBox;
    private Collection<AsgnRow> _asgnRows;
    private JLabel _studentStatus;
    private JButton _enableButton;
    private JPanel _studentStatusPanel;
    private Collection<String> _tas;

    public DistLookupView() {

        _students = new ArrayList<String>(Allocator.getDatabaseIO().getAllStudents().keySet());
        Collections.sort(_students);

        _assignments = Allocator.getCourseInfo().getHandinAssignments();

        _tas = Allocator.getDatabaseIO().getAllTAs().keySet();

        this.setLayout(new BorderLayout());
        this.add(this.getStudentPanel(), BorderLayout.WEST);
        this.add(this.getGraderPanel(), BorderLayout.EAST);
        this.add(this.getSaveResetPanel(), BorderLayout.SOUTH);

        //initialize starting selection state
        _studentList.setSelectedIndex(0);

        this.pack();
        this.setVisible(true);

        _studentFilterBox.requestFocus();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private JPanel getStudentPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(5, 5));

        _studentList = new GenericJList<String>(_students);
        _studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _studentList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (_studentList.getSelectedValue() == null) {
                    _studentList.setSelectedIndex(0);
                } else {
                    updateGraderList();
                }
            }
        });

        JScrollPane studentSP = new JScrollPane();

        studentSP.setViewportView(_studentList);
        studentSP.setPreferredSize(new Dimension(STUDENT_LIST_WIDTH, STUDENT_LIST_HEIGHT));

        _studentFilterBox = new JTextField();
        _studentFilterBox.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                filterStudentLogins(e);
            }
        });
        _studentFilterBox.setPreferredSize(new Dimension(STUDENT_LIST_WIDTH, TEXT_HEIGHT));

        JPanel studentPanel = new JPanel();
        studentPanel.setPreferredSize(new Dimension(STUDENT_LIST_WIDTH, STUDENT_LIST_HEIGHT));
        studentPanel.add(studentSP);

        leftPanel.add(studentPanel, BorderLayout.CENTER);
        leftPanel.add(_studentFilterBox, BorderLayout.NORTH);

        return leftPanel;
    }

    private JPanel getGraderPanel() {
        _asgnRows = new ArrayList<AsgnRow>();

        JPanel graderPanel = new JPanel();
        graderPanel.setLayout(new BorderLayout());

        _studentStatusPanel = new JPanel();
        _studentStatus = new JLabel();
        _studentStatusPanel.add(_studentStatus);

        _enableButton = new JButton("Enable");
        _enableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handleEnable();
            }
        });
        _studentStatusPanel.add(_enableButton);

        _graderGrid = new JPanel();
        _graderGrid.setLayout(new GridLayout(_assignments.size(), 1));

        _gradeScrollPane = new JScrollPane(_graderGrid);

        _gradeScrollPane.setSize(DIST_LIST_WIDTH, DIST_LIST_HEIGHT);
        _gradeScrollPane.setPreferredSize(new Dimension(DIST_LIST_WIDTH, DIST_LIST_HEIGHT));

        graderPanel.add(_studentStatusPanel, BorderLayout.NORTH);
        graderPanel.add(_gradeScrollPane, BorderLayout.CENTER);

        return graderPanel;
    }

    private JPanel getSaveResetPanel() {
        JPanel savePanel = new JPanel();
        _saveButton = new JButton("Save Changes");
        _saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handleSaveChangesClicked();
            }
        });
        savePanel.add(_saveButton);

        _resetButton = new JButton("Reset Graders");
        _resetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handleResetGradersClicked();
            }
        });
        savePanel.add(_resetButton);
        return savePanel;
    }

    private void updateGraderList() {
        _gradeScrollPane.setVisible(true);
        _saveButton.setEnabled(true);
        _resetButton.setEnabled(true);

        _graderGrid.removeAll();
        _asgnRows.clear();

        String student = _studentList.getSelectedValue();

        //if the student is null don't do anything.
        if (student == null) {
            return;
        }

        _graders = Allocator.getDatabaseIO().getAllGradersForStudent(student);

        boolean studentEnabled = Allocator.getDatabaseIO().isStudentEnabled(student);

        if (studentEnabled || _studentList.getSelectedValue() == null) {
            _studentStatusPanel.setVisible(false);
            _gradeScrollPane.setSize(DIST_LIST_WIDTH, DIST_LIST_HEIGHT_NO_BUTTON);
            _gradeScrollPane.setPreferredSize(new Dimension(DIST_LIST_WIDTH, DIST_LIST_HEIGHT_NO_BUTTON));
            _studentStatus.setText("");
        } else {
            _studentStatusPanel.setVisible(true);
            _gradeScrollPane.setSize(DIST_LIST_WIDTH, DIST_LIST_HEIGHT);
            _gradeScrollPane.setPreferredSize(new Dimension(DIST_LIST_WIDTH, DIST_LIST_HEIGHT));
            _studentStatus.setText(student + ": disabled");
        }

        for (Assignment asgn : _assignments) {
            AsgnRow row = new AsgnRow(asgn, _graders.get(asgn), studentEnabled);
            _graderGrid.add(row);
            _asgnRows.add(row);
        }

        //need so that the new new content of the scroll pane shows up when updateGraderList is called
        //without it the scroll pane remains blank for some reason.
        _gradeScrollPane.updateUI();
    }

    public void handleEnable() {
        if (_studentList.getSelectedValue() != null) {
            Allocator.getDatabaseIO().enableStudent(_studentList.getSelectedValue());
        }
        this.updateGraderList();
    }

    public void handleResetGradersClicked() {
        this.updateGraderList();
    }

    private void handleSaveChangesClicked() {
        String student = _studentList.getSelectedValue();

        if (student == null) {
            return;
        }

        for (AsgnRow asgnRow : _asgnRows) {
            if (asgnRow.hasNewTA() && this.isOkToDistribute(student, asgnRow.getNewTA(), asgnRow.getAsgn())) {
                // already assigned
                if (_graders.containsKey(asgnRow.getAsgn())) {

                    //modify the distribution
                    Allocator.getDatabaseIO().unassignStudentFromGrader(student, asgnRow.getAsgn().getHandinPart(), _graders.get(asgnRow.getAsgn()));
                    Allocator.getDatabaseIO().assignStudentToGrader(student, asgnRow.getAsgn().getHandinPart(), asgnRow.getNewTA());
                } // not assigned
                else {
                    //modify the distribution
                    Allocator.getDatabaseIO().assignStudentToGrader(student, asgnRow.getAsgn().getHandinPart(), asgnRow.getNewTA());

                    //assign the rubrics
                    Map<String, Collection<String>> distribution = new HashMap<String, Collection<String>>();
                    ArrayList<String> students = new ArrayList<String>();
                    students.add(student);
                    distribution.put(asgnRow.getNewTA(), students);
                    Allocator.getRubricManager().distributeRubrics(asgnRow.getAsgn().getHandinPart(), distribution,
                            Allocator.getCourseInfo().getMinutesOfLeniency(),
                            DistributionRequester.DO_NOTHING_REQUESTER);

                }
            }
        }
        this.updateGraderList();
        _studentFilterBox.requestFocus();
    }

    private boolean isOkToDistribute(String student, String ta, Assignment asgn) {
        if (Allocator.getGradingServices().groupMemberOnTAsBlacklist(student, asgn.getHandinPart(), ta)) {
            int shouldContinue = JOptionPane.showConfirmDialog(null, "A member of group " + student + " is on TA "
                    + ta + "'s blacklist.  Continue?",
                    "Distribute Blacklisted Student?",
                    JOptionPane.YES_NO_OPTION);
            return (shouldContinue == JOptionPane.YES_OPTION);
        }
        return true;
    }

    private void filterStudentLogins(KeyEvent evt) {
        //term to filter against
        String filterTerm = _studentFilterBox.getText();

        List<String> matchingLogins = new ArrayList<String>();

        for (String login : _students) {
            if (login.startsWith(filterTerm)) {
                matchingLogins.add(login);
            }
        }
        Collections.sort(matchingLogins);

        _studentList.setListData(matchingLogins);

        if (matchingLogins.size() > 0) {
            _studentList.setSelectedIndex(0);
        } else {
            _gradeScrollPane.setVisible(false);
            _studentStatusPanel.setVisible(false);
            _saveButton.setEnabled(false);
            _resetButton.setEnabled(false);
        }

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (matchingLogins.size() > 0) {
                _studentFilterBox.setText(matchingLogins.get(0));
                _saveButton.requestFocus();
            }
        }
    }

    public static void main(String[] argv) {
        new DistLookupView();
    }

    private class AsgnRow extends JPanel {
        private JComboBox _taList;
        private Assignment _rowAsgn;
        private String _origTA;

        public AsgnRow(Assignment asgn, String taLogin, boolean studentEnabled) {
            _rowAsgn = asgn;
            _origTA = taLogin;

            this.setLayout(new GridLayout(1, 2));

            this.add(new JLabel(_rowAsgn.getName()));

            if (asgn.getHandinPart().hasHandin(_studentList.getSelectedValue())) {
                _taList = new JComboBox(_tas.toArray());
                _taList.addActionListener(new NewTAListener(this));
                _taList.setSelectedItem(taLogin);
                _taList.setEnabled(studentEnabled);
                this.add(_taList);
            } else {
                this.add(new JLabel("No Handin"));
            }

            this.setBackground(Color.WHITE);
        }

        public boolean hasNewTA() {
            return ((_taList != null) &&
                    (_origTA != _taList.getSelectedItem())) ||
                   ((_origTA != null) &&
                    (!_origTA.equals(_taList.getSelectedItem())));
        }

        public String getNewTA() {
            if (_taList == null) {
                return "nologin";
            }
            return (String) _taList.getSelectedItem();
        }

        public Assignment getAsgn() {
            return _rowAsgn;
        }

        private class NewTAListener implements ActionListener {

            private JPanel _asgnRow;

            public NewTAListener(JPanel asgnRow) {
                _asgnRow = asgnRow;
            }

            public void actionPerformed(ActionEvent ae) {
                if (hasNewTA()) {
                    _asgnRow.setBackground(new Color(224, 90, 90));
                } else {
                    _asgnRow.setBackground(new Color(255, 255, 255));
                }
            }
        }
    }
}
