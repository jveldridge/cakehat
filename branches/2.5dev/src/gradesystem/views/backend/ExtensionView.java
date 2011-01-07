package gradesystem.views.backend;

import gradesystem.components.calendar.CalendarListener;
import gradesystem.components.calendar.CalendarView;
import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import gradesystem.Allocator;

/**
 * Allows for viewing, submitting, and removing extensions.
 *
 * @author jak2
 */
public class ExtensionView extends JFrame
{
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                //Hack to get Cartoon's handin part
                HandinPart part = null;
                for(Assignment asgn : Allocator.getCourseInfo().getHandinAssignments())
                {
                    if(asgn.getName().equals("Week1"))
                    {
                        part = asgn.getHandinPart();
                    }
                }

                String studentLogin = "cs004000";

                new ExtensionView(part, studentLogin).setVisible(true);
            }
        });
    }

    private HandinPart _part;
    private String _studentLogin;
    private Calendar _extensionDate;
    private boolean _hasExtension;
    private CalendarView _calendarView;
    private JComboBox _monthBox, _dayBox, _yearBox;
    private IntegerField _hourField, _minuteField, _secondField;
    private static String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private int _minYear, _maxYear, _startYear;

    private class DateLabel extends JLabel
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

    public ExtensionView(HandinPart part, String studentLogin)
    {
        _part = part;
        _studentLogin = studentLogin;

        this.setTitle(part.getAssignment().getName() + " extension for " + studentLogin);

        //Get extension, if none available default to on time date
        _extensionDate = Allocator.getDatabaseIO().getExtension(studentLogin, part);
        _hasExtension = (_extensionDate != null);
        if(_extensionDate == null)
        {
            _extensionDate = part.getTimeInformation().getOntimeDate();
        }
        int extYear = _extensionDate.get(Calendar.YEAR);
        int ontimeYear = part.getTimeInformation().getOntimeDate().get(Calendar.YEAR);
        _minYear = Math.min(extYear, ontimeYear);
        _maxYear = Math.max(extYear, ontimeYear) + 1;
        _startYear = extYear;

        this.initializeComponents();

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents()
    {
        JPanel mainPanel = new JPanel();
        this.add(mainPanel);

        _calendarView = new CalendarView(_part);
        mainPanel.add(Box.createRigidArea(new Dimension(_calendarView.getPreferredSize().width, 10)));
        mainPanel.add(_calendarView);
        mainPanel.setPreferredSize(new Dimension(_calendarView.getPreferredSize().width + 30, 600));

        _calendarView.addCalendarListener(new CalendarListener()
        {
            public void dateSelected(Calendar cal)
            {
                _extensionDate = cal;

                int month = cal.get(Calendar.MONTH);
                _monthBox.setSelectedIndex(month);
                
                int day = cal.get(Calendar.DAY_OF_MONTH);
                _dayBox.setSelectedItem(day);

                int year = cal.get(Calendar.YEAR);
                _yearBox.setSelectedItem(year);
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
        JLabel earlyLabel = new DateLabel("Early",_part.getTimeInformation().getEarlyDate(), true);
        earlyLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(earlyLabel);

        JLabel ontimeLabel = new DateLabel("Ontime",_part.getTimeInformation().getOntimeDate(), true);
        ontimeLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(ontimeLabel);

        JLabel lateLabel = new DateLabel("Late",_part.getTimeInformation().getLateDate(), true);
        lateLabel.setPreferredSize(new Dimension(datePanel.getPreferredSize().width, 20));
        datePanel.add(lateLabel);

        //Comment area
        String comment = Allocator.getDatabaseIO().getExtensionNote(_studentLogin, _part);
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
                public void focusGained(FocusEvent fe){
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
                Allocator.getDatabaseIO().removeExtension(_studentLogin, _part);

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

                Allocator.getDatabaseIO().grantExtension(_studentLogin, _part, cal, text);

                //Close window
                ExtensionView.this.dispose();
            }
        });
    }

    private JPanel createExtensionSelectionPanel()
    {
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        JPanel panel = new JPanel(layout);

        _monthBox = new JComboBox(MONTHS);
        _monthBox.setSelectedIndex(_extensionDate.get(Calendar.MONTH));
        _monthBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }

        });
        panel.add(_monthBox);

        _dayBox = new JComboBox(generateValues(1, 31));
        _dayBox.setSelectedItem(_extensionDate.get(Calendar.DAY_OF_MONTH));
        _dayBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
            }

        });
        panel.add(_dayBox);

        _yearBox = new JComboBox(generateValues(_minYear, _maxYear));
        _yearBox.setSelectedItem(_startYear);
        _yearBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }
        });
        panel.add(_yearBox);

        panel.add(Box.createRigidArea(new Dimension(10,5)));

        _hourField = new IntegerField(_extensionDate.get(Calendar.HOUR_OF_DAY), 0, 23);
        panel.add(_hourField);

        panel.add(new JLabel(":"));

        _minuteField = new IntegerField(_extensionDate.get(Calendar.MINUTE), 0, 59);
        panel.add(_minuteField);
        
        panel.add(new JLabel(":"));

        _secondField = new IntegerField(_extensionDate.get(Calendar.SECOND), 0, 59);
        panel.add(_secondField);

        return panel;
    }

    private void updateCalendar()
    {
        _calendarView.selectDate(this.getComboBoxCal());
    }

    private Calendar getComboBoxCal()
    {
        int month = _monthBox.getSelectedIndex();
        int day = (Integer) _dayBox.getSelectedItem();
        int year = (Integer) _yearBox.getSelectedItem();

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

    private Integer[] generateValues(int begin, int end)
    {
        Integer[] array = new Integer[end-begin+1];

        for(int i = 0; i < array.length; i++)
        {
            array[i] = begin + i;
        }

        return array;
    }

    private void updateDayBox()
    {
        int currValue = (Integer) _dayBox.getSelectedItem();

        Calendar cal = getComboBoxCal();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        ComboBoxModel model = new DefaultComboBoxModel(generateValues(1,nod));
        _dayBox.setModel(model);
        
        //If the selected value is an available selection
        if(currValue <= nod)
        {
            _dayBox.setSelectedItem(currValue);
        }
        //Otherwise just select the max value
        else
        {
            _dayBox.setSelectedIndex(model.getSize()-1);
        }
    }

    private class IntegerField extends JFormattedTextField
    {
        public IntegerField(int initValue, final int min, final int max)
        {
            super(NumberFormat.getIntegerInstance());

            this.setIntValue(initValue);

            this.setColumns(2);

            this.getDocument().addDocumentListener(new DocumentListener()
            {
                public void changedUpdate(DocumentEvent e){}

                public void insertUpdate(DocumentEvent e)
                {
                    int value = 0;
                    try
                    {
                        value = Integer.parseInt(getText());
                    }
                    catch (Exception exc) {}

                    int newValue = value;
                    if(value < min)
                    {
                        newValue = min;
                    }
                    if(value > max)
                    {
                        newValue = max;
                    }

                    final boolean changeValue = (newValue != value);
                    final int finalValue = newValue;

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            if(changeValue)
                            {
                                setIntValue(finalValue);
                            }
                        }
                    });


                }

                public void removeUpdate(DocumentEvent e)
                {
                    insertUpdate(e);
                }
            });

            this.addFocusListener(new FocusListener()
            {
                public void focusGained(FocusEvent fe) { }


                public void focusLost(FocusEvent fe)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            setIntValue(getIntValue());
                        }
                    });
                }
            });
        }

        public int getIntValue()
        {
            return Integer.parseInt(getText());
        }

        public void setIntValue(int value)
        {
            String valueText = value + "";
            if(value > -1 && value < 10)
            {
                valueText = "0" + valueText;
            }
            this.setText(valueText);
        }
    }

}