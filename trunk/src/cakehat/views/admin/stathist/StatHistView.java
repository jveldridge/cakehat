package cakehat.views.admin.stathist;

import cakehat.config.Assignment;
import cakehat.config.Part;
import cakehat.Allocator;
import cakehat.CakehatMain;
import support.ui.GenericJComboBox;
import cakehat.database.Group;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import cakehat.views.shared.ErrorView;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author psastras
 * @author jeldridg
 * @author jak2
 */
public class StatHistView extends JFrame {

    private static final String BY_ASSIGNMENT = "By Assignment";
    private static final String BY_ASSIGNMENT_PART = "By Assignment Part";
    private static final String BY_STUDENT = "By Student";

    private JPanel _chartPanel;
    private GenericJComboBox<String> _selectViewBox;
    
    private final Map<Assignment, AssignmentChartPanel> _asgnChartMap;
    private final Map<Assignment, Component> _asgnPaddingMap;
    private final Map<Part, AssignmentChartPanel> _partChartMap;
    private final Map<Part, Component> _partPaddingMap;
    private final Map<String, StudentChartPanel> _studChartMap;
    private final Map<String, Component> _studPaddingMap;
    private final Collection<String> _enabledStudents;
    private final List<Assignment> _assignments;
    private final Collection<Part> _parts;

    public StatHistView(Collection<Assignment> assignments) {
        super("Charts and Histograms");
        try {
            this.setIconImage(IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.X_OFFICE_DRAWING));
        } catch (Exception e) {}

        _assignments  = new ArrayList<Assignment>(assignments);
        Collections.sort(_assignments);
        _parts = new ArrayList<Part>();

        _asgnChartMap = new HashMap<Assignment, AssignmentChartPanel>();
        _partChartMap = new HashMap<Part, AssignmentChartPanel>();
        _studChartMap = new HashMap<String, StudentChartPanel>();
        _asgnPaddingMap = new HashMap<Assignment, Component>();
        _partPaddingMap = new HashMap<Part, Component>();
        _studPaddingMap = new HashMap<String, Component>();

        Collection<String> enabledStudents;
        try {
            enabledStudents = Allocator.getDatabaseIO().getEnabledStudents().keySet();
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not retrieve enabled students from the database.");
            enabledStudents = Collections.emptyList();
        }
        _enabledStudents = enabledStudents;

        this.initComponents();
        this.updateCharts();
        this.pack();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(0, 0));

        //combo box to select from viewing by: assignment, assignment parts, students
        _selectViewBox = new GenericJComboBox<String>(BY_ASSIGNMENT, BY_ASSIGNMENT_PART, BY_STUDENT);
        _selectViewBox.setBorder(BorderFactory.createTitledBorder("Chart Type"));
        _selectViewBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updateCharts();
            }
        });
        this.add(_selectViewBox, BorderLayout.NORTH);

        //Panel to hold charts, inside of a scroll pane
        _chartPanel = new JPanel();
        //Use a layout that will align the charts vertically (along the y axis)
        _chartPanel.setLayout(new BoxLayout(_chartPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(_chartPanel);
        scrollPane.setPreferredSize(new Dimension(700, 330));
        this.add(scrollPane, BorderLayout.CENTER);

        //Create AssignmentChartPanels for each Assignment and Part
        for (Assignment a : _assignments) {
            AssignmentChartPanel asgnPanel = new AssignmentChartPanel();
            _chartPanel.add(asgnPanel);
            _asgnChartMap.put(a, asgnPanel);

            Component asgnPadding = Box.createVerticalStrut(10);
            _chartPanel.add(asgnPadding);
            _asgnPaddingMap.put(a, asgnPadding);

            for (Part p : a.getParts()) {
                AssignmentChartPanel partPanel = new AssignmentChartPanel();
                _chartPanel.add(partPanel);
                _partChartMap.put(p, partPanel);
                _parts.add(p);

                Component partPadding = Box.createVerticalStrut(10);
                _chartPanel.add(partPadding);
                _partPaddingMap.put(p, partPadding);
            }
        }

        //Create StudentChartPanels for each student
        List<String> sortedStudents = new ArrayList(_enabledStudents);
        Collections.sort(sortedStudents);
        for (String studentLogin : sortedStudents) {
            StudentChartPanel studentPanel = new StudentChartPanel();
            _chartPanel.add(studentPanel);
            _studChartMap.put(studentLogin, studentPanel);

            Component studentPadding = Box.createVerticalStrut(10);
            _chartPanel.add(studentPadding);
            _studPaddingMap.put(studentLogin, studentPadding);
        }
     }

    private void updateCharts() {
        //assignment histograms
        if (_selectViewBox.getSelectedItem().equals(BY_ASSIGNMENT) ||
            _selectViewBox.getSelectedItem().equals(BY_ASSIGNMENT_PART)) {
            for (String studentLogin : _enabledStudents) {
                _studChartMap.get(studentLogin).setVisible(false);
                _studPaddingMap.get(studentLogin).setVisible(false);
            }
        }

        //show assignments
        if (_selectViewBox.getSelectedItem().equals(BY_ASSIGNMENT)) {
            for (Part p : _parts) {
                _partChartMap.get(p).setVisible(false);
                _partPaddingMap.get(p).setVisible(false);
            }
            for (Assignment a : _assignments) {
                Collection<Group> groups;
                try {
                    groups = Allocator.getDatabaseIO().getGroupsForAssignment(a);
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not get groups for assignment " + a + ".");
                    groups = Collections.emptyList();
                }

                AssignmentChartPanel chart = _asgnChartMap.get(a);
                chart.updateChartData(a, groups);
                chart.setVisible(true);

                _asgnPaddingMap.get(a).setVisible(true);
            }
        }
        //show parts
        else if (_selectViewBox.getSelectedItem().equals(BY_ASSIGNMENT_PART)) {
            for (Assignment a : _assignments) {
                _asgnChartMap.get(a).setVisible(false);
                _asgnPaddingMap.get(a).setVisible(false);
            }
            for (Part p : _parts) {
                Collection<Group> groups;
                try {
                    groups = Allocator.getDatabaseIO().getGroupsForAssignment(p.getAssignment());
                } catch (SQLException ex) {
                    new ErrorView(ex, "Could not get groups for assignment " + p.getAssignment() + ".");
                    groups = Collections.emptyList();
                }

                AssignmentChartPanel chart = _partChartMap.get(p);
                chart.updateChartData(p, groups);
                chart.setVisible(true);

                _partPaddingMap.get(p).setVisible(true);
            }
        }
        //see student performance
        else if (_selectViewBox.getSelectedItem().equals(BY_STUDENT)) {
            for (Assignment a : _assignments) {
                _asgnChartMap.get(a).setVisible(false);
                _asgnPaddingMap.get(a).setVisible(false);
            }
            for (Part p : _parts) {
                _partChartMap.get(p).setVisible(false);
                _partPaddingMap.get(p).setVisible(false);
            }

            for (String student : _enabledStudents) {
                StudentChartPanel chart = _studChartMap.get(student);
                chart.updateChart(student, _assignments.toArray(new Assignment[0]));
                chart.setVisible(true);

                _studPaddingMap.get(student).setVisible(true);
            }
        }
    }

    public static void main(String args[]) {
        CakehatMain.applyLookAndFeel();
        
        StatHistView view = new StatHistView(Allocator.getConfigurationInfo().getHandinAssignments());
        view.setLocationRelativeTo(null);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setVisible(true);
    }
}