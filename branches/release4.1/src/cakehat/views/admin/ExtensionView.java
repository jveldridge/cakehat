package cakehat.views.admin;

import cakehat.CakehatException;
import support.ui.CalendarListener;
import support.ui.CalendarView;
import cakehat.config.Assignment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import cakehat.Allocator;
import cakehat.CakehatMain;
import cakehat.config.TimeInformation;
import cakehat.config.handin.DistributablePart;
import cakehat.database.Group;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JOptionPane;
import support.ui.GenericJComboBox;
import support.ui.IntegerField;
import support.ui.StringConverter;

/**
 * Allows for viewing, submitting, and removing extensions.
 *
 * @author jak2
 */
class ExtensionView extends JFrame
{
    private final Assignment _asgn;
    private final Group _group;
    private Calendar _extensionDate;
    private final boolean _hasExtension;
    private final int _minYear, _maxYear, _startYear;
    
    private CalendarView _calendarView;
    private GenericJComboBox<Integer> _monthBox, _dayBox, _yearBox;
    private IntegerField _hourField, _minuteField, _secondField;

    //Months in Java's Calendar are 0 based, so the indices of Strings in the
    //following array correspond with that month's numeric representation
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public ExtensionView(Assignment asgn, Group group)
    {
        _asgn = asgn;
        _group = group;

        this.setTitle(asgn.getName() + " extension for " + group);
        try
        {
            //Get extension, if none available default to on time date
            _extensionDate = Allocator.getDataServices().getExtension(group);
        }
        catch (ServicesException ex)
        {
            new ErrorView(ex, "Could not retrieve extension information for student " +
                               group + " for assignment " + asgn + ".  Assuming that the " +
                               "student does not have an extension.");
        }

        _hasExtension = (_extensionDate != null);
        if(_extensionDate == null)
        {
            _extensionDate = asgn.getHandin().getTimeInformation().getOntimeDate();
        }
        int extYear = _extensionDate.get(Calendar.YEAR);
        int ontimeYear = asgn.getHandin().getTimeInformation().getOntimeDate().get(Calendar.YEAR);
        _minYear = Math.min(extYear, ontimeYear);
        _maxYear = Math.max(extYear, ontimeYear) + 1;
        _startYear = extYear;

        this.initializeComponents();

        this.pack();
        this.setResizable(false);

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents()
    {
        JPanel mainPanel = new JPanel();
        this.add(mainPanel);

        //Create calendar showing early, ontime, and late deadlines
        TimeInformation timeInfo = _asgn.getHandin().getTimeInformation();
        HashMap<Calendar,Color> dateColorMap = new HashMap<Calendar,Color>();
        if(timeInfo.getEarlyDate() != null)
        {
           dateColorMap.put(timeInfo.getEarlyDate(), new Color(135,206,250));
        }
        if(timeInfo.getOntimeDate() != null)
        {
           dateColorMap.put(timeInfo.getOntimeDate(), new Color(152,251,152));
        }
        if(timeInfo.getLateDate() != null)
        {
           dateColorMap.put(timeInfo.getLateDate(), new Color(205,92,92));
        }
        _calendarView = new CalendarView(dateColorMap);

        mainPanel.add(Box.createRigidArea(new Dimension(_calendarView.getPreferredSize().width, 10)));
        mainPanel.add(_calendarView);
        mainPanel.setPreferredSize(new Dimension(_calendarView.getPreferredSize().width + 30, 600));

        _calendarView.addCalendarListener(new CalendarListener()
        {
            public void dateSelected(Calendar cal)
            {
                _extensionDate = cal;

                int month = cal.get(Calendar.MONTH);
                _monthBox.setGenericSelectedItem(month);
                
                int day = cal.get(Calendar.DAY_OF_MONTH);
                _dayBox.setGenericSelectedItem(day);

                int year = cal.get(Calendar.YEAR);
                _yearBox.setGenericSelectedItem(year);
            }
        });
        _calendarView.selectDate(_extensionDate);
        _calendarView.restrictYearRange(_minYear, _maxYear);

        //Add extension date
        mainPanel.add(this.createExtensionSelectionPanel());

        //Early, ontime, and late
        JPanel datePanel = new JPanel();
        mainPanel.add(datePanel);

        datePanel.setPreferredSize(new Dimension(_calendarView.getPreferredSize().width, 80));
        JLabel earlyLabel = new DateLabel("Early",_asgn.getHandin().getTimeInformation().getEarlyDate(), true);
        earlyLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(earlyLabel);

        JLabel ontimeLabel = new DateLabel("Ontime",_asgn.getHandin().getTimeInformation().getOntimeDate(), true);
        ontimeLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(ontimeLabel);

        JLabel lateLabel = new DateLabel("Late",_asgn.getHandin().getTimeInformation().getLateDate(), true);
        lateLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(lateLabel);

        //Comment area
        String comment = null;
        try
        {
            comment = Allocator.getDataServices().getExtensionNote(_group);
        }
        catch (ServicesException ex)
        {
            new ErrorView(ex, "Could not retrieve the extension note for student " +
                              _group + " " + "on assignment " + _asgn + ".");
        }
        boolean noComment = (comment == null);
        if(noComment)
        {
            comment = "Please insert an explanation of the extension here.";
        }

        final JTextArea commentArea = new JTextArea(comment);
        //If there was no initial comment, then display initial text in a lighter
        //color and on first focus gained clear the message
        if(noComment)
        {
            commentArea.setForeground(Color.DARK_GRAY);
            commentArea.addFocusListener(new FocusListener()
            {
                public void focusGained(FocusEvent fe)
                {
                    commentArea.setText("");
                    commentArea.setForeground(Color.BLACK);
                    commentArea.removeFocusListener(this);
                }

                public void focusLost(FocusEvent fe) {}
            });
        }
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentPane = new JScrollPane(commentArea);
        commentPane.setPreferredSize(new Dimension(_calendarView.getPreferredSize().width, 120));
        mainPanel.add(commentPane);

        //Remove extension button
        JButton removeExtensionButton = new JButton("Remove Extension");
        removeExtensionButton.setEnabled(_hasExtension);
        mainPanel.add(removeExtensionButton);
        removeExtensionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //Call to remove extension
                try
                {
                    Allocator.getDataServices().removeExtension(_group);
                    JOptionPane.showMessageDialog(ExtensionView.this, "Extension removed successfully.");
                }
                catch (ServicesException ex)
                {
                    new ErrorView(ex, "Removing the extension for group " + _group + " " +
                                      "on assignment " + _asgn + " failed.");
                }

                //Close window
                ExtensionView.this.dispose();
            }
        });

        //Submit button
        JButton submitButton = new JButton("Submit Extension");
        mainPanel.add(submitButton);
        submitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                String text = commentArea.getText();
                Calendar cal = getCalendar();
                try
                {
                    Allocator.getDataServices().grantExtension(_group, cal, text);
                    
                    String successMessage = "Extension added successfully.";

                    if(hasAlreadyBeenDistributed())
                    {
                        try
                        {
                            Allocator.getGradingServices() .storeHandinStatuses(
                                    _asgn.getHandin(),
                                    Arrays.asList(_group),
                                    Allocator.getConfigurationInfo().getMinutesOfLeniency(),
                                    true);

                            successMessage += "\nHandin status updated with extension date.";
                        }
                        catch(ServicesException e)
                        {
                            new ErrorView(e, "Unable to update handin status " +
                                    "group " + _group + " on assignment " +
                                    _asgn + ".");
                        }
                    }
                    
                    JOptionPane.showMessageDialog(ExtensionView.this, successMessage);
                } 
                catch (ServicesException ex)
                {
                    new ErrorView(ex, "Granting the extension for group " + _group + " " +
                                      "on assignment " + _asgn + " failed.");
                }

                //Close window
                ExtensionView.this.dispose();
            }
        });
    }

    private JPanel createExtensionSelectionPanel()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        //Months are zero based for Calendar
        _monthBox = new GenericJComboBox<Integer>(generateRange(0, 11),
        new StringConverter<Integer>()
        {
            @Override
            public String convertToString(Integer numMonth)
            {
                return MONTHS[numMonth];
            }

        });
        _monthBox.setGenericSelectedItem(_extensionDate.get(Calendar.MONTH));
        _monthBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }
        });
        panel.add(_monthBox);

        _dayBox = new GenericJComboBox<Integer>(generateRange(1, 31));
        _dayBox.setGenericSelectedItem(_extensionDate.get(Calendar.DAY_OF_MONTH));
        _dayBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
            }
        });
        panel.add(_dayBox);

        _yearBox = new GenericJComboBox<Integer>(generateRange(_minYear, _maxYear));
        _yearBox.setGenericSelectedItem(_startYear);
        _yearBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }
        });
        panel.add(_yearBox);

        //Update day box to have the correct number of days for the current month
        updateDayBox(); 

        panel.add(Box.createRigidArea(new Dimension(10, 5)));

        _hourField = new IntegerField(_extensionDate.get(Calendar.HOUR_OF_DAY));
        _hourField.setShowLeadingZero(true);
        _hourField.setRangeRestriction(0, 23);
        panel.add(_hourField);

        panel.add(new JLabel(":"));

        _minuteField = new IntegerField(_extensionDate.get(Calendar.MINUTE));
        _minuteField.setShowLeadingZero(true);
        _minuteField.setRangeRestriction(0, 59);
        panel.add(_minuteField);
        
        panel.add(new JLabel(":"));

        _secondField = new IntegerField(_extensionDate.get(Calendar.SECOND));
        _secondField.setShowLeadingZero(true);
        _secondField.setRangeRestriction(0, 59);
        panel.add(_secondField);

        return panel;
    }

    private void updateCalendar()
    {
        _calendarView.selectDate(this.getComboBoxCal());
    }

    private Calendar getComboBoxCal()
    {
        int month = _monthBox.getSelectedItem();
        int day = _dayBox.getSelectedItem();
        int year = _yearBox.getSelectedItem();

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal;
    }

    private Calendar getCalendar()
    {
        Calendar cal = getComboBoxCal();

        cal.set(Calendar.HOUR_OF_DAY, _hourField.getIntValue());
        cal.set(Calendar.MINUTE, _minuteField.getIntValue());
        cal.set(Calendar.SECOND, _secondField.getIntValue());

        return cal;
    }

    private Iterable<Integer> generateRange(int begin, int end)
    {
        int range = end - begin + 1;
        ArrayList<Integer> list = new ArrayList<Integer>(range);

        for(int i = 0; i < range; i++)
        {
            list.add(begin + i);
        }

        return list;
    }

    private void updateDayBox()
    {
        int currValue = _dayBox.getSelectedItem();

        Calendar cal = getComboBoxCal();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int numberOfDays = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        _dayBox.setItems(generateRange(1, numberOfDays));
        
        //If the selected value is an available selection
        if(currValue <= numberOfDays)
        {
            _dayBox.setGenericSelectedItem(currValue);
        }
        //Otherwise just select the max value
        else
        {
            _dayBox.setGenericSelectedItem(numberOfDays);
        }
    }

    /**
     * Whether any distributable part of this assignment has already been
     * distributed to a TA.
     * 
     * @return
     */
    private boolean hasAlreadyBeenDistributed()
    {
        boolean alreadyDistributed = false;

        for(DistributablePart part : _asgn.getDistributableParts())
        {
            try
            {
                if(Allocator.getDataServices().getAssignedGroups(part).contains(_group))
                {
                    alreadyDistributed = true;
                    break;
                }
            }
            catch(ServicesException e)
            {
                new ErrorView(e, "Unable to determine if " + _group + " has " +
                        "been assigned to a TA for assignment part " + part);
            }
        }

        return alreadyDistributed;
    }

    private static class DateLabel extends JLabel
    {
        private String _text;
        private boolean _showTime;

        public DateLabel(String text, Calendar cal, boolean showTime)
        {
            _text = text;
            _showTime = showTime;

            this.setCalendar(cal);
        }

        public void setCalendar(Calendar cal)
        {
            String calString = "unavailable";
            if(cal != null)
            {
                calString = MONTHS[cal.get(Calendar.MONTH)] + " " +
                            cal.get(Calendar.DAY_OF_MONTH) + ", " +
                            cal.get(Calendar.YEAR);

                if(_showTime)
                {
                    calString += " " + ensureLeadingZero(cal.get(Calendar.HOUR_OF_DAY))
                                 + ":" + ensureLeadingZero(cal.get(Calendar.MINUTE))
                                 + ":" + ensureLeadingZero(cal.get(Calendar.SECOND));
                }
            }

            this.setText("<html><b>" + _text + ": </b>" + calString + "</html>");
        }

        private String ensureLeadingZero(int value)
        {
            String valueText = value + "";
            if(value > -1 && value < 10)
            {
                valueText = "0" + valueText;
            }

            return valueText;
        }
    }

    public static void main(String args[]) throws CakehatException
    {
        CakehatMain.initializeForTesting();

        Assignment asgn = Allocator.getConfigurationInfo().getHandinAssignments().get(0);
        try
        {
            Group group = Allocator.getDataServices().getGroups(asgn).iterator().next();

            ExtensionView view = new ExtensionView(asgn, group);
            view.setLocationRelativeTo(null);
            view.setVisible(true);
        }
        catch (ServicesException ex)
        {
            ex.printStackTrace();
        }
    }
}