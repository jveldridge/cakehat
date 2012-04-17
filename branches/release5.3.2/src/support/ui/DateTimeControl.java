package support.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.joda.time.DateTime;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
public class DateTimeControl extends JPanel
{
    public static interface DateTimeChangeListener
    {
        public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime);
    }
    
    private final List<DateTimeChangeListener> _listeners = new CopyOnWriteArrayList<DateTimeChangeListener>();
    
    private final GenericJComboBox<Integer> _monthBox;
    private final GenericJComboBox<Integer> _dayBox;
    private final JComboBox _yearBox;
    private final GenericJComboBox<Integer> _hourBox;
    private final GenericJComboBox<Integer> _minuteBox;
    private DateTime _dateTime;
    
    public DateTimeControl(DateTime dateTime)
    {
        _dateTime = dateTime;
        
        //Create the combo boxes
        _monthBox = new GenericJComboBox<Integer>(TIME_DESCRIPTION_PROVIDER);
        _dayBox = new GenericJComboBox<Integer>(TIME_DESCRIPTION_PROVIDER);
        _yearBox = new JComboBox();
        _hourBox = new GenericJComboBox<Integer>(TIME_DESCRIPTION_PROVIDER);
        _minuteBox = new GenericJComboBox<Integer>(TIME_DESCRIPTION_PROVIDER);
        
        //Year is always disabled - just a combobox for UI consistency
        _yearBox.setEnabled(false);
        
        //Lay out UI
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.add(_monthBox);
        this.add(new JLabel("/"));
        this.add(_dayBox);
        this.add(new JLabel("/"));
        this.add(_yearBox);
        
        this.add(new JLabel("  at  "));
        
        this.add(_hourBox);
        this.add(new JLabel(" : "));
        this.add(_minuteBox);
        
        //Display provided date time
        this.setDateTime(dateTime, true);
    }
    
    /**
     * Sets the control to display the date and time represented by {@code dateTime}. If {@code null} this control will
     * be disabled.
     * 
     * @param dateTime may be {@code null}
     * @param suppressNotification if {@code true} listeners will not be notified of this change
     */
    public final void setDateTime(DateTime dateTime, boolean suppressNotification)
    {
        DateTime prevDateTime = _dateTime;
        _dateTime = dateTime;
        
        if(_dateTime == null)
        {
            this.displayDisabled();
        }
        else
        {
            this.displayDateTime(dateTime);
        }
        
        if(!suppressNotification)
        {
            this.notifyListeners(prevDateTime, _dateTime);
        }
    }
    
    public final void setDateTime(DateTime dateTime)
    {
        this.setDateTime(dateTime, false);
    }
    
    /**
     * Returns the date time currently visualized by this control. This will be {@code null} if no date is being
     * visualized.
     * 
     * @return 
     */
    public final DateTime getDateTime()
    {
        return _dateTime;
    }
    
    private void displayDateTime(DateTime dateTime)
    {
        //Remove listeners (if they are not currently attached this will have no effect)
        _monthBox.removeSelectionListener(_monthListener);
        _dayBox.removeSelectionListener(_dayListener);
        _hourBox.removeSelectionListener(_hourListener);
        _minuteBox.removeSelectionListener(_minuteListener);
        
        _monthBox.setEnabled(true);
        _monthBox.setItems(generateRange(_dateTime.monthOfYear()));
        _monthBox.setGenericSelectedItem(dateTime.getMonthOfYear());
        
        _dayBox.setEnabled(true);
        _dayBox.setItems(generateRange(_dateTime.dayOfMonth()));
        _dayBox.setGenericSelectedItem(dateTime.getDayOfMonth());
        
        _yearBox.removeAllItems();
        _yearBox.addItem(Integer.toString(dateTime.getYear()));
        
        _hourBox.setEnabled(true);
        _hourBox.setItems(generateRange(_dateTime.hourOfDay()));
        _hourBox.setGenericSelectedItem(dateTime.getHourOfDay());
        
        _minuteBox.setEnabled(true);
        _minuteBox.setItems(generateRange(_dateTime.minuteOfHour()));
        _minuteBox.setGenericSelectedItem(dateTime.getMinuteOfHour());
        
        //Add listeners
        _monthBox.addSelectionListener(_monthListener);
        _dayBox.addSelectionListener(_dayListener);
        _hourBox.addSelectionListener(_hourListener);
        _minuteBox.addSelectionListener(_minuteListener);
    }
    
    private void displayDisabled()
    {
        _monthBox.removeSelectionListener(_monthListener);
        _dayBox.removeSelectionListener(_dayListener);
        _hourBox.removeSelectionListener(_hourListener);
        _minuteBox.removeSelectionListener(_minuteListener);
        
        _monthBox.setEnabled(false);
        _monthBox.setItems(Arrays.<Integer>asList((Integer) null));
        _monthBox.setGenericSelectedItem(null);
        
        _dayBox.setEnabled(false);
        _dayBox.setItems(Arrays.<Integer>asList((Integer) null));
        _dayBox.setGenericSelectedItem(null);
        
        _yearBox.removeAllItems();
        _yearBox.addItem("----");
        
        _hourBox.setEnabled(false);
        _hourBox.setItems(Arrays.<Integer>asList((Integer) null));
        _hourBox.setGenericSelectedItem(null);
        
        _minuteBox.setEnabled(false);
        _minuteBox.setItems(Arrays.<Integer>asList((Integer) null));
        _minuteBox.setGenericSelectedItem(null);
    }
    
    private final SelectionListener<Integer> _monthListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currMonthOfYear, Integer newMonthOfYear, SelectionAction action)
        {
            DateTime prevDateTime = _dateTime;

            //Calculate new date time
            int year = _dateTime.getYear();
            int month = newMonthOfYear;
            DateTime newYearAndMonth = new DateTime(year, month, 1, 0, 0);
            int day = Math.min(_dateTime.getDayOfMonth(), newYearAndMonth.dayOfMonth().getMaximumValue());
            _dateTime = new DateTime(year, month, day, _dateTime.getHourOfDay(), _dateTime.getMinuteOfHour(),
                    _dateTime.getSecondOfMinute(), _dateTime.getMillisOfSecond());

            //If the day changed, update day box
            if(day != _dateTime.getDayOfMonth())
            {
                _dayBox.setItems(generateRange(_dateTime.dayOfMonth()));
                _dayBox.setGenericSelectedItem(day);
            }

            notifyListeners(prevDateTime, _dateTime);
        }
    };
    
    private final SelectionListener<Integer> _dayListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currDayOfMonth, Integer newDayOfMonth, SelectionAction action)
        {
            DateTime prevDateTime = _dateTime;

            _dateTime = new DateTime(_dateTime.getYear(), _dateTime.getMonthOfYear(), newDayOfMonth,
                    _dateTime.getHourOfDay(), _dateTime.getMinuteOfHour(), _dateTime.getSecondOfMinute(),
                    _dateTime.getMillisOfSecond());

            //The date time may not have changed in the case that the day box was updated programmatically by the
            //month box
            if(!prevDateTime.equals(_dateTime))
            {
                notifyListeners(prevDateTime, _dateTime);
            }
        }
    };
    
    private final SelectionListener<Integer> _hourListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currHourOfDay, Integer newHourOfDay, SelectionAction action)
        {
            DateTime prevDateTime = _dateTime;

            _dateTime = new DateTime(_dateTime.getYear(), _dateTime.getMonthOfYear(), _dateTime.getDayOfMonth(),
                    newHourOfDay, _dateTime.getMinuteOfHour(), _dateTime.getSecondOfMinute(),
                    _dateTime.getMillisOfSecond());

            notifyListeners(prevDateTime, _dateTime);
        }
    };
    
    private final SelectionListener<Integer> _minuteListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currMinuteOfHour, Integer newMinuteOfHour, SelectionAction action)
        {
            DateTime prevDateTime = _dateTime;

            _dateTime = new DateTime(_dateTime.getYear(), _dateTime.getMonthOfYear(), _dateTime.getDayOfMonth(),
                    _dateTime.getHourOfDay(), newMinuteOfHour, _dateTime.getSecondOfMinute(),
                    _dateTime.getMillisOfSecond());

            notifyListeners(prevDateTime, _dateTime);
        }
    };
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;

        return size;
    }
    
    private static List<Integer> generateRange(DateTime.Property property)
    {
        int min = property.getMinimumValue();
        int max = property.getMaximumValue();
        ArrayList<Integer> rangeList = new ArrayList<Integer>(max - min + 1);
        for(int i = min; i <= max; i++)
        {
            rangeList.add(i);
        }
        
        return rangeList;
    }
    
    private void notifyListeners(DateTime prevDateTime, DateTime newDateTime)
    {
        for(DateTimeChangeListener listener : _listeners)
        {
            listener.dateTimeChanged(prevDateTime, newDateTime);
        }
    }
    
    public void addDateTimeChangeListener(DateTimeChangeListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeDateTimeChangeListener(DateTimeChangeListener listener)
    {
        _listeners.remove(listener);
    }
    
    private static final PartialDescriptionProvider<Integer> TIME_DESCRIPTION_PROVIDER =
            new PartialDescriptionProvider<Integer>()
    {
        @Override
        public String getDisplayText(Integer val)
        {
            String text;
            if(val != null)
            {
                text = Integer.toString(val);
                if(text.length() == 1)
                {
                    text = "0" + text;
                }   
            }
            else
            {
                text = "--";
            }
            
            return text;
        }
    };
}