package gradesystem.rubric;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComboBox;
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

import gradesystem.config.HandinPart;
import gradesystem.config.LatePolicy;
import gradesystem.rubric.Rubric.*;
import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.config.TA;

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
    private NumberField _totalScoreField, _statusPointsField;
    private Vector<NumberField> _ecFields = new Vector<NumberField>();
    private Vector<Component> _tabOrder = new Vector<Component>(7);
    private StateManager _stateManager;
    private boolean _isAdmin;
    private HandinPart _handinPart;

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
        _handinPart = rubric._handinPart;

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
        this.displayStudentAndGrader();
        for(Section section : _rubric.getSections())
        {
            this.displaySection(section);
        }
        this.displayStatus();
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
                    detailName = new JTextArea(detail.getName() + " (" + detail.getName() +" points)");
                }
                detailName.setSize(new Dimension(250,200));
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
        // Determine if extra credit is editable
        // editable if early or ontime, or late AND extra credit is allowable if late
        boolean editable = (_rubric.getStatus() == TimeStatus.EARLY || _rubric.getStatus() == TimeStatus.ON_TIME)
                            || (_rubric.getStatus() == TimeStatus.LATE && _rubric.getTimeInformation().ecIfLate());

        return editable;
    }

    //TODO: Display each subsection's details
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

        _statusPointsField.setValue(Allocator.getGeneralUtilities().round(_rubric.getDeduction(), 2));
        _totalScoreField.setValue(_rubric.getTotalRubricScore());
    }
	
    Vector<Component> getTabOrder()
    {
        return _tabOrder;
    }

    private void displayAssignment()
    {
        JPanel panel = new JPanel();

        JLabel asgn = new JLabel("<html><b>Assignment " + _rubric.getNumber() + " Grader Sheet: " + _rubric.getName() + "</b></html>", JLabel.CENTER);
        panel.add(asgn);

        panel.setPreferredSize(new Dimension(this.getWidth(), asgn.getPreferredSize().height + 10));
        this.addPanelBelow(panel);
    }

    private void displayStudentAndGrader()
    {
        int height = 0;
        int vGap = 0;

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);

        //Get all students in the group
        Collection<String> group = Allocator.getDatabaseIO().getGroup(_rubric._handinPart, _rubric.getStudentAccount());
        Map<String, String> allLogins = Allocator.getDatabaseIO().getAllStudents();
        String studentNames = "";
        String studentLogins = "";

        int i = 0;
        for (String student : group) {
            if (i != 0) {
                studentLogins += ", ";
                studentNames += ", ";
            }
            studentLogins += student;
            studentNames += allLogins.get(student);
            i++;
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

        Assignment asgn = _rubric._handinPart.getAssignment();
        TA ta = Allocator.getDatabaseIO().getAllGradersForStudent(_rubric.getStudentAccount()).get(asgn);
        String graderLogin = Allocator.getUserServices().getSanitizedTALogin(ta);
        String graderName = Allocator.getUserServices().getSanitizedTAName(ta);

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

        panel.setPreferredSize(new Dimension(this.getWidth(), height));

        this.addPanelBelow(panel);
    }

    private Vector<HandinStatusPair> makeHandinStatusPairs(TimeStatus[] timeStatuses, HandinPart handinPart) {
        Vector<HandinStatusPair> pairs = new Vector<HandinStatusPair>();

        for (TimeStatus t : timeStatuses) {
            switch (t) {
                case ON_TIME:
                    pairs.add(new HandinStatusPair(t, handinPart.getTimeInformation().getOntimeDate()));
                    break;
                case LATE:
                    if (handinPart.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION) {
                        pairs.add(new HandinStatusPair(t, null));
                    } else if (handinPart.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES) {
                        pairs.add(new HandinStatusPair(t, handinPart.getTimeInformation().getLateDate()));
                    }
                    break;
                case EARLY:
                    pairs.add(new HandinStatusPair(t, handinPart.getTimeInformation().getEarlyDate()));
                    break;
                case NC_LATE:
                    pairs.add(new HandinStatusPair(t, null));
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
        java.util.Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(_handinPart.getHandin(_rubric.getStudentAccount()));
        String handinInfo = "";
        if (_isAdmin) {
            handinInfo = String.format(" ( Received at: <b>%s</b> )",
                    Allocator.getCalendarUtilities().getCalendarAsHandinTime(handinTime));
        }
        JLabel handinStatusLabel = new JLabel(String.format("<html><b>Handin Status</b>%s</html>", handinInfo));

        int vGap = 10;
        layout.putConstraint(SpringLayout.NORTH, handinStatusLabel, vGap, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, handinStatusLabel, 2, SpringLayout.WEST, panel);
        panel.add(handinStatusLabel);
        JComponent elemAbove = handinStatusLabel;
        height += handinStatusLabel.getPreferredSize().height + vGap;

        //Status
        TimeStatus status = _rubric.getStatus();
        JComponent elemLeft = null;
        //If admin, display a drop down list
        if(_isAdmin)
        {
            //Displays status
            final JComboBox statusBox = new JComboBox(this.makeHandinStatusPairs(status.getAvailableStatuses(_handinPart.getTimeInformation().getLatePolicy()), _handinPart));
            elemLeft = statusBox;
            statusBox.setSelectedItem(status);
            vGap = 10;
            layout.putConstraint(SpringLayout.NORTH, statusBox, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, statusBox, 5, SpringLayout.WEST, panel);
            panel.add(statusBox);
            elemAbove = statusBox;
            height += statusBox.getPreferredSize().height + vGap + 5;

            //If the LatePolicy is DAILY_DEDUCTION then have a box for the amount of days late
            final JFormattedTextField daysLateField = new JFormattedTextField(NumberFormat.getIntegerInstance());
            if(_handinPart.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
            {
                panel.add(daysLateField);
                layout.putConstraint(SpringLayout.WEST, daysLateField, 2, SpringLayout.EAST, statusBox);
                layout.putConstraint(SpringLayout.NORTH, daysLateField, 0, SpringLayout.NORTH, statusBox);


                daysLateField.setHorizontalAlignment(SwingConstants.CENTER);
                daysLateField.setPreferredSize(new Dimension(50, statusBox.getPreferredSize().height));
                daysLateField.setText(_rubric.getDaysLate() + "");

                //If current status is LATE, editable, otherwise uneditable
                boolean editable = (status == TimeStatus.LATE);
                daysLateField.setEditable(editable);
                daysLateField.setVisible(editable);

                //Listen for changes
                daysLateField.getDocument().addDocumentListener(new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e){}

                    public void insertUpdate(DocumentEvent e)
                    {
                        try
                        {
                            //Update with new values
                            int daysLate = Integer.parseInt(daysLateField.getText());
                            _rubric.setDaysLate(daysLate);
                            _stateManager.rubricChanged();

                            //Recalculate score
                            RubricPanel.this.updateTotals();
                        }
                        catch (Exception exc) {}
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        insertUpdate(e);
                    }
                });
            }

            //Listener for TimeStatus box
            statusBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    //Set new status
                    TimeStatus newStatus = ((HandinStatusPair) statusBox.getSelectedItem())._timeStatus;
                    _rubric.setStatus(newStatus);

                    //If the new time status is LATE and LatePolicy is DAILY_DEDUCTION
                    //then enable the text box, otherwise disable it
                    if(_handinPart.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
                    {
                        boolean editable = (newStatus == TimeStatus.LATE);
                        daysLateField.setEditable(editable);
                        daysLateField.setVisible(editable);
                    }

                    //Update editability of extra credit
                    boolean ecEditable = RubricPanel.this.isECEditable();
                    for(NumberField ecField : _ecFields)
                    {
                        ecField.setEditable(ecEditable);
                        //If not editable, clear score
                        if(!ecEditable)
                        {
                            ecField.setValue(0);
                        }
                    }
                    
                    //Recalculate score
                    RubricPanel.this.updateTotals();

                    _stateManager.rubricChanged();
                }
            });
        }
        else
        {
            JTextArea statusText = new JTextArea(status.getPrettyPrintName());
            elemLeft = statusText;
            statusText.setEditable(false);
            statusText.setLineWrap(true);
            statusText.setWrapStyleWord(true);
            statusText.setBackground(this.getBackground());
            vGap = 10;
            layout.putConstraint(SpringLayout.NORTH, statusText, vGap, SpringLayout.SOUTH, elemAbove);
            layout.putConstraint(SpringLayout.WEST, statusText, 5, SpringLayout.WEST, panel);
            panel.add(statusText);
            elemAbove = statusText;
            height += statusText.getPreferredSize().height + vGap + 5;
        }

        //Score field
        _statusPointsField = NumberField.getAsUneditable(Allocator.getGeneralUtilities().round(_rubric.getDeduction(), 2));
        layout.putConstraint(SpringLayout.WEST, _statusPointsField, 460, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, _statusPointsField, 0, SpringLayout.NORTH, elemLeft);
        panel.add(_statusPointsField);

        //OutOf field
        NumberField outOfField = NumberField.getAsUneditable(0.0);
        outOfField.setFocusable(false);
        layout.putConstraint(SpringLayout.WEST, outOfField, 2, SpringLayout.EAST, _statusPointsField);
        layout.putConstraint(SpringLayout.NORTH, outOfField, 0, SpringLayout.NORTH, _statusPointsField);
        panel.add(outOfField);

        panel.setPreferredSize(new Dimension(this.getWidth(), height + 5));
        this.addPanelBelow(panel);
    }

    private class HandinStatusPair {

        private TimeStatus _timeStatus;
        private Calendar _calendar;

        private HandinStatusPair(TimeStatus timeStatus, Calendar calendar) {
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