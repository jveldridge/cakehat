package backend.calendar;

import config.HandinPart;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * A Calendar view.
 *
 * @author jak2
 */
public class CalendarView extends JPanel
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();

        CalendarView view = new CalendarView(getCalendar(2010, 0, 8), null, getCalendar(2010, 0, 12));
        
        CalendarListener listener = new CalendarListener()
        {
            public void dateSelected(Calendar cal)
            {
                System.out.println(cal);
            }

        };
        view.addCalendarListener(listener);

        frame.add(view);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private DefaultTableModel _tableModel;
    private JTable _calendar;
    private JScrollPane _calendarPane;
    private JButton _prevButton, _nextButton;
    private JLabel _monthYearLabel;
    private int _currentMonth, _currentYear;
    private Calendar _selectedDate;
    private int _maxYear = Integer.MAX_VALUE, _minYear = Integer.MIN_VALUE;

    private Vector<CalendarListener> _listeners = new Vector<CalendarListener>();

    private static Color SELECTED_COLOR = new Color(238,221,130),
                         EARLY_COLOR = new Color(135,206,250),
                         ON_TIME_COLOR = new Color(152,251,152),
                         LATE_COLOR = new Color(205,92,92),
                         NORMAL_DAY_COLOR = Color.WHITE,
                         NON_DAY_COLOR = Color.LIGHT_GRAY;

    private static Calendar getCalendar(int year, int month, int day)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal;
    }

    private static boolean areSameDay(Calendar cal1, Calendar cal2)
    {
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        int month1 = cal1.get(Calendar.MONTH);
        int month2 = cal2.get(Calendar.MONTH);

        int day1 = cal1.get(Calendar.DAY_OF_MONTH);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);

        return (year1 == year2 && month1 == month2 && day1 == day2);
    }

    /**
     * Constructs a Calendar.
     *
     */
    public CalendarView()
    {
        this(new HashMap<Calendar, Color>());
    }

    public CalendarView(HandinPart part)
    {
        this(part.getTimeInformation().getEarlyDate(),
             part.getTimeInformation().getOntimeDate(),
             part.getTimeInformation().getLateDate());
    }

    /**
     * Constructs a calendar and highlights early, ontime, and late dates with
     * default set colors. Any or all of these calendars may be null.
     *
     * @param early
     * @param ontime
     * @param late
     */
    public CalendarView(Calendar early, Calendar ontime, Calendar late)
    {
        this(createMapping(early, ontime, late));

        if(ontime != null)
        {
            refreshCalendar(ontime.get(Calendar.MONTH), ontime.get(Calendar.YEAR));
        }
    }

    private static HashMap<Calendar, Color> createMapping(Calendar early, Calendar ontime, Calendar late)
    {
       HashMap<Calendar,Color> map = new HashMap<Calendar,Color>();
       if(early != null)
       {
           map.put(early, EARLY_COLOR);
       }
       if(ontime != null)
       {
           map.put(ontime, ON_TIME_COLOR);
       }
       if(late != null)
       {
           map.put(late, LATE_COLOR);
       }

       return map;
    }

    private HashMap<Calendar, Color> _dates;

    /**
     * Constructs a Calendar with the specified dates and colors. For each
     * Calendar, if the day it represents is on the screen then it will be
     * shown in the color it is mapped to.
     *
     * @param dates
     */
    public CalendarView(HashMap<Calendar, Color> dates)
    {
        _dates = dates;

        this.setLayout(new BorderLayout());

        _tableModel = new CalendarTableModel();
        _calendar = new JTable(_tableModel);
        _calendarPane = new JScrollPane(_calendar);
        _calendarPane.setPreferredSize(new Dimension(400,250));
        this.setPreferredSize(new Dimension(400, 280));
        this.add(_calendarPane, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel(new GridLayout(1,0));
        _prevButton = new JButton ("<<");
        _prevButton.addActionListener(new PreviousAction());
        _nextButton = new JButton (">>");
        _nextButton.addActionListener(new NextAction());
        _monthYearLabel = new JLabel();
        _monthYearLabel.setHorizontalAlignment(SwingConstants.HORIZONTAL);
        controlPanel.add(_prevButton);
        controlPanel.add(_monthYearLabel);
        controlPanel.add(_nextButton);
        this.add(controlPanel, BorderLayout.NORTH);

        //Days of the week as headers
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek)
        {
            _tableModel.addColumn(day);
        }

        //No resize/reorder
        _calendar.getTableHeader().setResizingAllowed(false);
        _calendar.getTableHeader().setReorderingAllowed(false);

        //Single cell selection
        _calendar.setColumnSelectionAllowed(true);
        _calendar.setRowSelectionAllowed(true);
        _calendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Set row/column count
        _calendar.setRowHeight(38);
        _tableModel.setColumnCount(7);
        _tableModel.setRowCount(6);

        //Get current month and year
        Calendar today = GregorianCalendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        _currentMonth = today.get(Calendar.MONTH);
        _currentYear = today.get(Calendar.YEAR);
        
        this.refreshCalendar(_currentMonth, _currentYear);
    }

    public void restrictYearRange(int minYear, int maxYear)
    {
        _minYear = minYear;
        _maxYear = maxYear;
    }

    /**
     * Selects the current date and moves the calendar to the appropriate
     * month and year if necessary. Does not notify calendar listeners.
     *
     * @param cal
     */
    public void selectDate(Calendar cal)
    {
        _selectedDate = cal;
        refreshCalendar(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
    }

    public void addCalendarListener(CalendarListener listener)
    {
        _listeners.add(listener);
    }

    public void removeCalendarListener(CalendarListener listener)
    {
        _listeners.remove(listener);
    }

    private void notifyListeners(Calendar cal)
    {
        for(CalendarListener listener : _listeners)
        {
            listener.dateSelected(cal);
        }
    }

    private class CalendarTableModel extends DefaultTableModel
    {
        @Override
        public boolean isCellEditable(int rowIndex, int colIndex)
        {
            Object value = this.getValueAt(rowIndex, colIndex);

            if(value != null)
            {
                _selectedDate = getCalendar(_currentYear, _currentMonth, (Integer) value);
                _calendar.revalidate();
                CalendarView.this.notifyListeners(_selectedDate);
            }

            return false;
        }
    }

    private class CalendarRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean selected, boolean focused, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);

            //If not a day of the Calendar, gray out
            if (value == null)
            {
                setBackground(NON_DAY_COLOR);
            }
            //Otherwise color depending on the day
            else
            {
                Calendar selectedCal = getCalendar(_currentYear, _currentMonth, (Integer) value);

                boolean colored = false;
                for(Calendar cal : _dates.keySet())
                {
                    if(areSameDay(selectedCal, cal))
                    {
                        colored = true;
                        setBackground(_dates.get(cal));
                    }
                }
                if(_selectedDate != null && areSameDay(selectedCal, _selectedDate))
                {
                    setBackground(SELECTED_COLOR);
                }
                else if(!colored)
                {
                    setBackground(NORMAL_DAY_COLOR);
                }
            }
            
            this.setBorder(BorderFactory.createEtchedBorder());
            this.setForeground(Color.BLACK);

            return this;
        }
    }

    private static String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private void refreshCalendar(int month, int year)
    {
        _currentMonth = month;
        _currentYear = year;

        //Update buttons appropriately
        _prevButton.setEnabled(!((_currentYear == _minYear && _currentMonth == 0)));
        _nextButton.setEnabled(!(_currentYear == _maxYear && _currentMonth == 11));
        /*
        if(_currentYear == _minYear && _currentMonth == 0)
        {
            _prevButton.setEnabled(false);
        }
        if(_currentYear == _maxYear && _currentMonth == 11)
        {
            _nextButton.setEnabled(false);
        }
         */

        //Update the label showing the current month and year
        _monthYearLabel.setText(MONTHS[month] + ", " + year);
        
        //Clear table
        for (int i=0; i<6; i++)
        {
            for (int j=0; j<7; j++)
            {
                _tableModel.setValueAt(null, i, j);
            }
        }

        //Get first day of month and number of days
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        //Number of days
        int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        //Start of Month
        int som = cal.get(GregorianCalendar.DAY_OF_WEEK);

        //Draw calendar
        for (int i=1; i<=nod; i++)
        {
            int row = (int) ((i+som-2)/7);
            int column = (i+som-2)%7;
            
            _tableModel.setValueAt(i, row, column);
        }

        //Apply renderers
        _calendar.setDefaultRenderer(_calendar.getColumnClass(0), new CalendarRenderer());
    }

    private class PreviousAction implements ActionListener
    {
        public void actionPerformed (ActionEvent e)
        {
            //Back one year
            if (_currentMonth == 0)
            { 
                _currentMonth = 11;
                _currentYear -= 1;
            }
            //Back one month
            else
            { 
                _currentMonth -= 1;
            }
            refreshCalendar(_currentMonth, _currentYear);
        }
    }

    private class NextAction implements ActionListener
    {
        public void actionPerformed (ActionEvent e)
        {
            //Forward one year
            if (_currentMonth == 11)
            { 
                _currentMonth = 0;
                _currentYear += 1;
            }
            //Forward one month
            else
            { 
                _currentMonth += 1;
            }
            refreshCalendar(_currentMonth, _currentYear);
        }
    }
}
