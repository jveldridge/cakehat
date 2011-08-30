package cakehat.rubric;

import cakehat.database.HandinStatus;
import cakehat.services.ServicesException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import cakehat.config.LatePolicy;
import cakehat.rubric.Rubric.*;
import cakehat.Allocator;
import support.ui.GenericJComboBox;
import support.ui.StringConverter;
import cakehat.config.Assignment;
import cakehat.config.TA;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.database.Student;
import cakehat.views.shared.ErrorView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A graphical view of a rubric.
 *
 * @author jak2
 * @author spoletto
 */
class RubricPanel extends JPanel
{
    private Rubric _rubric;
    private SpringLayout _mainLayout;
    private JPanel _abovePanel = null;
    private int _height = 0;

    private Vector<NumberField> _totals = new Vector<NumberField>();
    private Vector<Section> _sections = new Vector<Section>();
    private NumberField _totalScoreField;
    private Vector<NumberField> _ecFields = new Vector<NumberField>();
    private Vector<Component> _tabOrder = new Vector<Component>(7);
    private StateManager _stateManager;
    private boolean _isAdmin;
    private HandinStatus _status;

    /**
     * Constructs a panel that displays the content of the passed in rubric.
     * Uses the StateManager to determine if unsaved changes have been made
     * to the rubric. If this functionality is not needed, null can be passed
     * in.
     *
     * @param rubric Rubric to visualize
     * @param manager StateManager that tracks unsaved changes, can be null
     */
    public RubricPanel(Rubric rubric, StateManager manager, boolean isAdmin)
    {
        _rubric = rubric;
        _stateManager = manager;
        _isAdmin = isAdmin;

        //if _stateManager is null, template rubric; no student or grader
        if (_stateManager != null) {
            try {
                _status = Allocator.getDataServices().getHandinStatus(_rubric.getGroup());
            } catch (ServicesException ex) {
                new ErrorView(ex, "Could not get handin status.  On-time status will be assumed.");
                _status = new HandinStatus(TimeStatus.ON_TIME, 0);
            }
        }

        this.setBackground(Color.white);

        //Set width
        //height will be set after everything else is populated
        Dimension size = new Dimension(600,0);
        this.setSize(size);
        this.setPreferredSize(size);

        //Create the layout
        _mainLayout = new SpringLayout();
        this.setLayout(_mainLayout);

        //Display the elements of the rubric
        this.displayAssignment();

        //if _stateManager is null, template rubric; no student or grader
        if (_stateManager != null) {
            this.displayStudentAndGrader();
        }

        for(Section section : _rubric.getSections())
        {
            this.displaySection(section);
        }

        if (_stateManager != null && _isAdmin) {
            this.displayStatus();
        }

        if(_rubric.hasExtraCredit())
        {
            this.displayExtraCredit();
        }
        this.displayTotalScore();

        //Adjust the height based on the panels added
        this.setPreferredSize(new Dimension(this.getWidth(), _height));
    }

    /**
     * Adds a panel below the last added panel.
     *
     * ONLY use this method to add a JPanel,
     * do not use add(...)
     *
     * @param panel
     */
    private void addPanelBelow(JPanel panel)
    {
        int vGap = 5;
        _height += panel.getPreferredSize().height + vGap;
        panel.setBackground(this.getBackground());

        if(_abovePanel == null)
        {
            _abovePanel = new JPanel();
            _abovePanel.setSize(new Dimension(this.getWidth(),0));
            _abovePanel.setPreferredSize(new Dimension(this.getWidth(),0));
            this.add(_abovePanel);
        }

        _mainLayout.putConstraint(SpringLayout.NORTH, panel, vGap, SpringLayout.SOUTH, _abovePanel);
        _mainLayout.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, _abovePanel);
        this.add(panel);

        _abovePanel = panel;
    }

    private void displaySection(Section section)
    {
        int height = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        //Section name
        JLabel sectionName = new JLabel("<html><b>" + section.getName() + "</b></html>");
        int vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, sectionName, vGap, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, sectionName, 2, SpringLayout.WEST, panel);
        panel.add(sectionName);
        JComponent elemAbove = sectionName;
        height += sectionName.getPreferredSize().height + vGap;

        //For each subsection
        for(Subsection subsection : section.getSubsections())
        {
            //Section name
            JTextArea subsectionName = new JTextArea(subsection.getName());
            subsectionName.setSize(new Dimension(450,200));
            subsectionName.setEditable(false);
            subsectionName.setLineWrap(true);
            subsectionName.setWrapStyleWord(true);
            subsectionName.setBackground(this.getBackground());
            vGap = 10;
            layout.putConstraint(SpringLayout.NORTH, subsectionName, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, subsectionName, 5, SpringLayout.WEST, panel);
            panel.add(subsectionName);
            elemAbove = subsectionName;
            height += subsectionName.getPreferredSize().height + vGap;

            //Score field
            NumberField scoreField;
            if(subsection.hasSource())
            {
                //Design check score should not be modifiable
                scoreField = NumberField.getAsUneditable(subsection.getScore());
                scoreField.setFocusable(false);
            }
            else
            {
                scoreField = NumberField.getAsScore(subsection, this, _stateManager);
                _tabOrder.add(scoreField);
            }
            layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, subsectionName);
            layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, subsectionName);
            panel.add(scoreField);

            //OutOf field
            NumberField outOfField = NumberField.getAsUneditable(subsection.getOutOf());
            outOfField.setFocusable(false);
            layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
            layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, subsectionName);
            panel.add(outOfField);

            //For each detail
            for(Detail detail : subsection.getDetails())
            {
                //The detail's name, and the point values associated with
                JTextArea detailName;
                if(detail.getValue() == 0)
                {
                    detailName = new JTextArea(detail.getName());
                }
                else
                {
                    detailName = new JTextArea(detail.getName() + " (" + detail.getValue() + " points)");
                }
                detailName.setSize(new Dimension(425,200));
                detailName.setForeground(Color.GRAY);
                detailName.setEditable(false);
                detailName.setLineWrap(true);
                detailName.setWrapStyleWord(true);
                detailName.setBackground(this.getBackground());
                vGap = 3;
                layout.putConstraint(SpringLayout.NORTH, detailName, 3, SpringLayout.SOUTH, elemAbove);
                layout.putConstraint(SpringLayout.WEST, detailName, 25, SpringLayout.WEST, subsectionName);
                panel.add(detailName);
                elemAbove = detailName;
                height += detailName.getPreferredSize().height + vGap;
            }
        }

        //Notes
        if(section.hasNotes())
        {
            JLabel notesLbl = new JLabel("Notes");
            vGap = 5;
            layout.putConstraint(SpringLayout.NORTH, notesLbl, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, notesLbl, 5, SpringLayout.WEST, panel);
            elemAbove = notesLbl;
            height += notesLbl.getPreferredSize().height + vGap;
            panel.add(notesLbl);

            TextField notes = TextField.getAsNotesField(section);
            notes.setSize(new Dimension(400,500));
            new JScrollPane(notes);
            notes.setFocusable(false);
            vGap = 3;
            layout.putConstraint(SpringLayout.NORTH, notes, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, notes, 0, SpringLayout.WEST, notesLbl);
            height += notes.getPreferredSize().height + vGap;
            elemAbove = notes;
            panel.add(notes);
        }

        //Comments
        JLabel commentLbl = new JLabel("Comments");
        vGap = 5;
        layout.putConstraint(SpringLayout.NORTH, commentLbl, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, commentLbl, 5, SpringLayout.WEST, panel);
        elemAbove = commentLbl;
        height += commentLbl.getPreferredSize().height + vGap;
        panel.add(commentLbl);

        TextField comments = TextField.getAsCommentField(section, _stateManager);
        comments.setSize(new Dimension(400,500));
        JScrollPane scroll = new JScrollPane(comments);
        vGap = 3;
        layout.putConstraint(SpringLayout.NORTH, scroll, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, commentLbl);
        height += scroll.getPreferredSize().height + vGap;
        elemAbove = scroll;
        panel.add(scroll);

        //Display section totals
        JTextArea sectionTotal = new JTextArea("Section Total");
        sectionTotal.setSize(new Dimension(450,200));
        sectionTotal.setEditable(false);
        sectionTotal.setLineWrap(true);
        sectionTotal.setWrapStyleWord(true);
        sectionTotal.setBackground(this.getBackground());
        vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, sectionTotal, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, sectionTotal, 5, SpringLayout.WEST, panel);
        panel.add(sectionTotal);
        elemAbove = sectionTotal;
        height += sectionTotal.getPreferredSize().height + vGap + 5;

        //Score field
        NumberField scoreField = NumberField.getAsUneditable(section.getSectionScore());
        scoreField.setFocusable(false);
        _totals.add(scoreField);
        _sections.add(section);
        layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, sectionTotal);
        layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, sectionTotal);
        panel.add(scoreField);

        //OutOf field
        NumberField outOfField = NumberField.getAsUneditable(section.getSectionOutOf());
        outOfField.setFocusable(false);
        layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
        layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, sectionTotal);
        panel.add(outOfField);

        panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
        this.addPanelBelow(panel);
    }

    private boolean isECEditable()
    {
        //if no status, template rubric
        if (_status == null) {
            return true;
        }
        
        TimeStatus timeStatus = _status.getTimeStatus();

        // Determine if extra credit is editable
        // editable if early or ontime, or late AND extra credit is allowable if late
        boolean editable = (timeStatus == TimeStatus.EARLY || timeStatus == TimeStatus.ON_TIME)
                            || (timeStatus == TimeStatus.LATE && _rubric.getDistributablePart().getHandin().getTimeInformation().ecIfLate());

        return editable;
    }

    private void displayExtraCredit()
    {
        int height = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        //ExtraCredit header
        JLabel extraCreditName = new JLabel("<html><b>Extra Credit</b></html>");
        int vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, extraCreditName, vGap, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, extraCreditName, 2, SpringLayout.WEST, panel);
        panel.add(extraCreditName);
        JComponent elemAbove = extraCreditName;
        height += extraCreditName.getPreferredSize().height + vGap;

        // Determine if extra credit is editable
        boolean editable = this.isECEditable();

        Section extraCredit = _rubric.getExtraCredit();
        //For each ExtraCredit section
        for(Subsection ecSection : extraCredit.getSubsections())
        {
            //Section name
            JTextArea extraCreditText = new JTextArea(ecSection.getName());
            extraCreditText.setSize(new Dimension(450,200));
            extraCreditText.setEditable(false);
            extraCreditText.setLineWrap(true);
            extraCreditText.setWrapStyleWord(true);
            extraCreditText.setBackground(this.getBackground());
            vGap = 10;
            layout.putConstraint(SpringLayout.NORTH, extraCreditText, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, extraCreditText, 5, SpringLayout.WEST, panel);
            panel.add(extraCreditText);
            elemAbove = extraCreditText;
            height += extraCreditText.getPreferredSize().height + vGap + 5;

            //Score field
            NumberField scoreField = NumberField.getAsScore(ecSection, this, _stateManager);
            scoreField.setEditable(editable);
            _tabOrder.add(scoreField);
            _ecFields.add(scoreField);
            layout.putConstraint(SpringLayout.WEST, scoreField, 5, SpringLayout.EAST, extraCreditText);
            layout.putConstraint(SpringLayout.NORTH, scoreField, 0, SpringLayout.NORTH, extraCreditText);
            panel.add(scoreField);

            //OutOf field
            NumberField outOfField = NumberField.getAsUneditable(ecSection.getOutOf());
            outOfField.setFocusable(false);
            layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, scoreField);
            layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, extraCreditText);
            panel.add(outOfField);
        }

        //Notes
        if(extraCredit.hasNotes())
        {
            JLabel notesLbl = new JLabel("Notes");
            vGap = 5;
            layout.putConstraint(SpringLayout.NORTH, notesLbl, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, notesLbl, 5, SpringLayout.WEST, panel);
            elemAbove = notesLbl;
            height += notesLbl.getPreferredSize().height + vGap;
            panel.add(notesLbl);

            TextField notes = TextField.getAsNotesField(extraCredit);
            notes.setSize(new Dimension(400,500));
            new JScrollPane(notes);
            notes.setFocusable(false);
            vGap = 3;
            layout.putConstraint(SpringLayout.NORTH, notes, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, notes, 0, SpringLayout.WEST, notesLbl);
            height += notes.getPreferredSize().height + vGap;
            elemAbove = notes;
            panel.add(notes);
        }

        //Comments
        JLabel commentLbl = new JLabel("Comments");
        vGap = 5;
        layout.putConstraint(SpringLayout.NORTH, commentLbl, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, commentLbl, 5, SpringLayout.WEST, panel);
        elemAbove = commentLbl;
        height += commentLbl.getPreferredSize().height + vGap;
        panel.add(commentLbl);

        TextField comments = TextField.getAsCommentField(extraCredit, _stateManager);
        comments.setSize(new Dimension(400,500));
        JScrollPane scroll = new JScrollPane(comments);
        vGap = 3;
        layout.putConstraint(SpringLayout.NORTH, scroll, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, commentLbl);
        height += scroll.getPreferredSize().height + vGap;
        elemAbove = scroll;
        panel.add(scroll);

        panel.setPreferredSize(new Dimension(this.getWidth(), height));
        this.addPanelBelow(panel);
    }

    private void displayTotalScore()
    {
        int height = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        //Total score header
        JLabel totalScoreName = new JLabel("<html><b>Total Score</b></html>");
        int vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, totalScoreName, vGap, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, totalScoreName, 2, SpringLayout.WEST, panel);
        panel.add(totalScoreName);
        JComponent elemAbove = totalScoreName;
        height += totalScoreName.getPreferredSize().height + vGap;

        //Final grade header
        JTextArea finalGrade = new JTextArea("Final Grade");
        finalGrade.setSize(new Dimension(450,200));
        finalGrade.setEditable(false);
        finalGrade.setLineWrap(true);
        finalGrade.setWrapStyleWord(true);
        finalGrade.setBackground(this.getBackground());
        vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, finalGrade, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, finalGrade, 5, SpringLayout.WEST, panel);
        panel.add(finalGrade);
        elemAbove = finalGrade;
        height += finalGrade.getPreferredSize().height + vGap + 15;

        //TotalScore field
        _totalScoreField = NumberField.getAsUneditable(_rubric.getTotalRubricScore());
        _totalScoreField.setFocusable(false);
        layout.putConstraint(SpringLayout.WEST, _totalScoreField, 5, SpringLayout.EAST, finalGrade);
        layout.putConstraint(SpringLayout.NORTH, _totalScoreField, 0, SpringLayout.NORTH, finalGrade);
        panel.add(_totalScoreField);

        //TotalOutOf field
        NumberField totalOutOfField = NumberField.getAsUneditable(_rubric.getTotalRubricOutOf());
        totalOutOfField.setFocusable(false);
        layout.putConstraint(SpringLayout.WEST, totalOutOfField, 2, SpringLayout.EAST, _totalScoreField);
        layout.putConstraint(SpringLayout.NORTH, totalOutOfField, 0, SpringLayout.NORTH, finalGrade);
        panel.add(totalOutOfField);

        panel.setPreferredSize(new Dimension(this.getWidth(), height));
        this.addPanelBelow(panel);
    }
	
    void updateTotals()
    {   
        for(int i = 0; i < _totals.size(); i++)
        {
            Section section = _sections.elementAt(i);
            NumberField score = _totals.elementAt(i);
            score.setValue(section.getSectionScore());
        }

        _totalScoreField.setValue(_rubric.getTotalRubricScore());
    }
	
    Vector<Component> getTabOrder()
    {
        return _tabOrder;
    }

    private void displayAssignment()
    {
        JPanel panel = new JPanel();
        Assignment asgn = _rubric.getDistributablePart().getAssignment();
        JLabel asgnLabel = new JLabel("<html><b>Assignment " + asgn.getNumber() +
                                      " Grader Sheet: " + asgn.getName() + " - " +
                                      _rubric.getDistributablePart().getName() + "</b></html>", JLabel.CENTER);
        panel.add(asgnLabel);
        
        panel.setPreferredSize(new Dimension(this.getWidth(), asgnLabel.getPreferredSize().height + 10));
        this.addPanelBelow(panel);
    }

    private void displayStudentAndGrader()
    {
        int height = 0;
        int vGap = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        List<Student> groupMembers = _rubric.getGroup().getMembers();
        String studentNames = groupMembers.get(0).getName();
        String studentLogins = groupMembers.get(0).getLogin();
        for (int i = 1; i < groupMembers.size(); i++) {
            studentLogins += ", " + groupMembers.get(i).getLogin();
            studentNames += ", " + groupMembers.get(i).getName();
        }

        //Student
        JLabel studentAcct = new JLabel(" Student account(s): " + studentLogins);
        panel.add(studentAcct);
        height += studentAcct.getPreferredSize().height + vGap;


        JLabel studentName = new JLabel(" Student name(s): " + studentNames);
        vGap = 2;
        layout.putConstraint(SpringLayout.NORTH, studentName, vGap, SpringLayout.SOUTH, studentAcct);
        panel.add(studentName);
        height += studentName.getPreferredSize().height + vGap;

        TA grader = null;
        try {
            grader = Allocator.getDataServices().getGrader(_rubric.getDistributablePart(), _rubric.getGroup());
        } catch (ServicesException e) {
            new ErrorView(e, "Could not get the grading TA.");
        }

        if (_isAdmin) {
            vGap = 10;
            int graderComponentHeight = 20;

            JLabel graderLabel = new JLabel(" Grader:");
            layout.putConstraint(SpringLayout.NORTH, graderLabel, vGap, SpringLayout.SOUTH, studentName);
            graderLabel.setPreferredSize(new Dimension(graderLabel.getPreferredSize().width, graderComponentHeight));
            panel.add(graderLabel);

            //create StringConverter that will show TAs' names and logins and show null as "UNASSIGNED"
            StringConverter<TA> graderConverter = new StringConverter<TA>() {
                @Override
                public String convertToString(TA ta) {
                    if (ta == null) {
                        return "UNASSIGNED";
                    }

                    return ta.getLogin() + " (" + ta.getName() + ")";
                }
            };

            List<TA> graders = new ArrayList<TA>(Allocator.getConfigurationInfo().getTAs());
            Collections.sort(graders);
            graders.add(0, null);

            final GenericJComboBox<TA> graderBox = new GenericJComboBox<TA>(graders, graderConverter);
            graderBox.setGenericSelectedItem(grader);
            _stateManager.setGrader(grader);
            _stateManager.graderSaved();
            graderBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TA newGrader = graderBox.getSelectedItem();
                    try {
                        if (newGrader != _stateManager.getGrader()) {
                            if (newGrader == null || Allocator.getGradingServices().isOkToDistribute(_rubric.getGroup(), newGrader)) {
                                _stateManager.setGrader(graderBox.getSelectedItem());
                            }
                            else {
                                //if the group should not be distributed, set the combo box selection to the
                                //value it had before the most recent change
                                graderBox.setGenericSelectedItem(_stateManager.getGrader());
                            }
                        }
                    } catch (ServicesException ex) {
                        new ErrorView(ex, "Could not determine whether it is OK to distribute group " +
                                          _rubric.getGroup() + " to grader " + newGrader + ".");
                    }
                }
            });

            graderBox.setPreferredSize(new Dimension(graderBox.getPreferredSize().width, graderComponentHeight));
            layout.putConstraint(SpringLayout.NORTH, graderBox, vGap, SpringLayout.SOUTH, studentName);
            layout.putConstraint(SpringLayout.WEST, graderBox, 10, SpringLayout.EAST, graderLabel);
            layout.putConstraint(SpringLayout.BASELINE, graderBox, 0, SpringLayout.BASELINE, graderLabel);
            panel.add(graderBox);

            height += graderComponentHeight + vGap;
        }
        else {
            String graderLogin = Allocator.getUserServices().getSanitizedTALogin(grader);
            String graderName = Allocator.getUserServices().getSanitizedTAName(grader);

            //Grader
            JLabel graderAcctLabel = new JLabel(" Grader's account: " + graderLogin);
            vGap = 10;
            layout.putConstraint(SpringLayout.NORTH, graderAcctLabel, vGap, SpringLayout.SOUTH, studentName);
            panel.add(graderAcctLabel);
            height += graderAcctLabel.getPreferredSize().height + vGap;

            JLabel graderNameLabel = new JLabel(" Grader's name: " + graderName);
            vGap = 2;
            layout.putConstraint(SpringLayout.NORTH, graderNameLabel, vGap, SpringLayout.SOUTH, graderAcctLabel);
            panel.add(graderNameLabel);
            height += graderNameLabel.getPreferredSize().height + vGap;
        }

        panel.setPreferredSize(new Dimension(this.getWidth(), height));
        this.addPanelBelow(panel);
    }

    private Vector<RubricHandinStatusPair> makeHandinStatusPairs(Iterable<TimeStatus> timeStatuses, DistributablePart distPart) {
        Vector<RubricHandinStatusPair> pairs = new Vector<RubricHandinStatusPair>();
        Handin handin = distPart.getHandin();

        for (TimeStatus t : timeStatuses) {
            switch (t) {
                case ON_TIME:
                    pairs.add(new RubricHandinStatusPair(t, handin.getTimeInformation().getOntimeDate()));
                    break;
                case LATE:
                    if (handin.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION) {
                        pairs.add(new RubricHandinStatusPair(t, null));
                    } else if (handin.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES) {
                        pairs.add(new RubricHandinStatusPair(t, handin.getTimeInformation().getLateDate()));
                    }
                    break;
                case EARLY:
                    pairs.add(new RubricHandinStatusPair(t, handin.getTimeInformation().getEarlyDate()));
                    break;
                case NC_LATE:
                    pairs.add(new RubricHandinStatusPair(t, null));
                    break;
            }
        }

        return pairs;
    }

    private void displayStatus()
    {
        int height = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        //Handin status
        String handinInfo = "";
        if (_isAdmin) {
            try {
                File handin = _rubric.getDistributablePart().getHandin().getHandin(_rubric.getGroup());
                Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(handin);
                handinInfo = String.format(" ( Received at: <b>%s</b> )",
                        Allocator.getCalendarUtilities().getCalendarAsHandinTime(handinTime));
            } catch (IOException e) {
                new ErrorView(e);
            }
        }
        JLabel handinStatusLabel = new JLabel(String.format("<html><b>Handin Status</b>%s</html>", handinInfo));

        int vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, handinStatusLabel, vGap, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, handinStatusLabel, 2, SpringLayout.WEST, panel);
        panel.add(handinStatusLabel);
        JComponent elemAbove = handinStatusLabel;
        height += handinStatusLabel.getPreferredSize().height + vGap;

        //Status
        try {
            _status = Allocator.getDataServices().getHandinStatus(_rubric.getGroup());
        } catch (ServicesException ex) {
            new ErrorView(ex, "Could not get time status; assuming on-time handin.");
            _status = new HandinStatus(TimeStatus.ON_TIME, 0);
        }

        //If admin, display a drop down list
        List<RubricHandinStatusPair> possibleStatuses = this.makeHandinStatusPairs(_rubric.getDistributablePart().getHandin().getTimeInformation().getLatePolicy().getAvailableStatuses(), _rubric.getDistributablePart());
        //Displays status
        final GenericJComboBox<RubricHandinStatusPair> statusBox = new GenericJComboBox<RubricHandinStatusPair>(possibleStatuses);

        RubricHandinStatusPair toSelect = null;
        for (RubricHandinStatusPair pair : possibleStatuses) {
            if (pair._timeStatus == _status.getTimeStatus()) {
                toSelect = pair;
                break;
            }
        }
        statusBox.setGenericSelectedItem(toSelect);
        vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, statusBox, vGap, SpringLayout.SOUTH, elemAbove);
        layout.putConstraint(SpringLayout.WEST, statusBox, 5, SpringLayout.WEST, panel);
        panel.add(statusBox);
        elemAbove = statusBox;
        height += statusBox.getPreferredSize().height + vGap + 5;

        //If the LatePolicy is DAILY_DEDUCTION then have a box for the amount of days late
        final JFormattedTextField daysLateField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        if (_rubric.getDistributablePart().getHandin().getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION) {
            panel.add(daysLateField);
            layout.putConstraint(SpringLayout.WEST, daysLateField, 2, SpringLayout.EAST, statusBox);
            layout.putConstraint(SpringLayout.NORTH, daysLateField, 0, SpringLayout.NORTH, statusBox);


            daysLateField.setHorizontalAlignment(SwingConstants.CENTER);
            daysLateField.setPreferredSize(new Dimension(50, statusBox.getPreferredSize().height));
            daysLateField.setText(_status.getDaysLate() + "");

            //If current status is LATE, editable, otherwise uneditable
            boolean editable = (_status.getTimeStatus() == TimeStatus.LATE);
            daysLateField.setEditable(editable);
            daysLateField.setVisible(editable);

            //Listen for changes
            daysLateField.getDocument().addDocumentListener(new DocumentListener() {

                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    //Update with new values
                    int daysLate = Integer.parseInt(daysLateField.getText());
                    _status = new HandinStatus(TimeStatus.LATE, daysLate);
                    _stateManager.setHandinStatus(_status);

                    //Recalculate score
                    RubricPanel.this.updateTotals();
                }

                public void removeUpdate(DocumentEvent e) {
                    insertUpdate(e);
                }
            });
        }

        //Listener for TimeStatus box
        statusBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //Set new status
                TimeStatus newStatus = statusBox.getSelectedItem()._timeStatus;
                if (newStatus != _status.getTimeStatus()) {
                    _status = new HandinStatus(newStatus, 0);
                    _stateManager.setHandinStatus(_status);

                    //If the new time status is LATE and LatePolicy is DAILY_DEDUCTION
                    //then enable the text box, otherwise disable it
                    if (_rubric.getDistributablePart().getHandin().getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION) {
                        boolean editable = (newStatus == TimeStatus.LATE);
                        daysLateField.setEditable(editable);
                        daysLateField.setVisible(editable);
                    }

                    //Update editability of extra credit
                    boolean ecEditable = RubricPanel.this.isECEditable();
                    for (NumberField ecField : _ecFields) {
                        ecField.setEditable(ecEditable);
                        //If not editable, clear score
                        if (!ecEditable) {
                            ecField.setValue(0);
                        }
                    }

                    double oldScore = RubricPanel.this._rubric.getTotalRubricScore();
                    //Recalculate score
                    RubricPanel.this.updateTotals();
                    double newScore = RubricPanel.this._rubric.getTotalRubricScore();

                    if (newScore != oldScore) {
                        _stateManager.rubricChanged();
                    }
                }
            }
        });
        
        panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
        this.addPanelBelow(panel);
    }

    private class RubricHandinStatusPair {

        private TimeStatus _timeStatus;
        private Calendar _calendar;

        private RubricHandinStatusPair(TimeStatus timeStatus, Calendar calendar) {
            _timeStatus = timeStatus;
            _calendar = calendar;
        }

        @Override
        public String toString() {
            return _timeStatus +
                    ((_calendar == null) ? "" : " - " + Allocator.getCalendarUtilities()
                                                                    .getCalendarAsHandinTime(_calendar));
        }
    }
}