package support.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.joda.time.Period;

/**
 *
 * @author jak2
 */
public class PeriodControl extends JPanel
{
    public static interface PeriodChangeListener
    {
        public void periodChanged(Period prevPeriod, Period newPeriod);
    }
    
    private final List<PeriodChangeListener> _listeners = new CopyOnWriteArrayList<PeriodChangeListener>();
    
    private final GenericJComboBox<Integer> _dayBox;
    private final GenericJComboBox<Integer> _hourBox;
    private final GenericJComboBox<Integer> _minuteBox;
    
    private final PeriodLabel _dayLabel;
    private final PeriodLabel _hourLabel;
    private final PeriodLabel _minuteLabel;
    
    private Period _period;
    
    public PeriodControl(Period period)
    {
        _period = period;
        
        //Create the combo boxes
        _dayBox = new GenericJComboBox<Integer>(generateRange(0, 10), TIME_DESCRIPTION_PROVIDER);
        _hourBox = new GenericJComboBox<Integer>(generateRange(0, 23), TIME_DESCRIPTION_PROVIDER);
        _minuteBox = new GenericJComboBox<Integer>(generateRange(0, 59), TIME_DESCRIPTION_PROVIDER);
        
        //Create the labels
        _dayLabel = new PeriodLabel(" day, ", " days, ");
        _hourLabel = new PeriodLabel(" hour, & ", " hours, & ");
        _minuteLabel = new PeriodLabel(" minute", " minutes");
        
        //Lay out UI
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(_dayBox);
        this.add(_dayLabel);
        this.add(_hourBox);
        this.add(_hourLabel);
        this.add(_minuteBox);
        this.add(_minuteLabel);
        
        //Display provided period
        this.setPeriod(period, true);
    }
    
    public final void setPeriod(Period period, boolean supressNotification)
    {
        Period prevPeriod = _period;
        _period = period;
        
        if(_period == null)
        {
            displayDisabled();
        }
        else
        {
            displayPeriod(period);
        }
        
        if(!supressNotification)
        {
            notifyListeners(prevPeriod, _period);
        }
    }
    
    public final void setPeriod(Period period)
    {
        this.setPeriod(period, false);
    }
    
    public final Period getPeriod()
    {
        return _period;
    }
    
    private void displayPeriod(Period period)
    {
        //Remove listeners (if they are not currently attached this will have no effect)
        _dayBox.removeSelectionListener(_dayListener);
        _hourBox.removeSelectionListener(_hourListener);
        _minuteBox.removeSelectionListener(_minuteListener);
        
        _dayBox.setGenericSelectedItem(period.getDays());
        _dayLabel.updateText(period.getDays());
        _dayBox.setEnabled(true);
        
        _hourBox.setGenericSelectedItem(period.getHours());
        _hourLabel.updateText(period.getHours());
        _hourBox.setEnabled(true);
        
        _minuteBox.setGenericSelectedItem(period.getMinutes());
        _minuteLabel.updateText(period.getMinutes());
        _minuteBox.setEnabled(true);
        
        //Add listeners
        _dayBox.addSelectionListener(_dayListener);
        _hourBox.addSelectionListener(_hourListener);
        _minuteBox.addSelectionListener(_minuteListener);
    }
    
    private void displayDisabled()
    {
        _dayBox.removeSelectionListener(_dayListener);
        _hourBox.removeSelectionListener(_hourListener);
        _minuteBox.removeSelectionListener(_minuteListener);
        
        _dayBox.setEnabled(false);
        _dayBox.setGenericSelectedItem(null);
        _dayLabel.updateText(null);
        
        _hourBox.setEnabled(false);
        _hourBox.setGenericSelectedItem(null);
        _hourLabel.updateText(null);
        
        _minuteBox.setEnabled(false);
        _minuteBox.setGenericSelectedItem(null);
        _minuteLabel.updateText(null);
    }
    
    private final SelectionListener<Integer> _dayListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currDays, Integer newDays, SelectionAction action)
        {
            Period prevPeriod = _period;
            _period = new Period(0, 0, 0, newDays, _period.getHours(), _period.getMinutes(), 0, 0);
            _dayLabel.updateText(newDays);
            notifyListeners(prevPeriod, _period);
        }
    };
    
    private final SelectionListener<Integer> _hourListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currHours, Integer newHours, SelectionAction action)
        {
            Period prevPeriod = _period;
            _period = new Period(0, 0, 0, _period.getDays(), newHours, _period.getMinutes(), 0, 0);
            _hourLabel.updateText(newHours);
            notifyListeners(prevPeriod, _period);
        }
    };
    
    private final SelectionListener<Integer> _minuteListener = new SelectionListener<Integer>()
    {
        @Override
        public void selectionPerformed(Integer currMinutes, Integer newMinutes, SelectionAction action)
        {
            Period prevPeriod = _period;
            _period = new Period(0, 0, 0, _period.getDays(), _period.getHours(), newMinutes, 0, 0);
            _minuteLabel.updateText(newMinutes);
            notifyListeners(prevPeriod, _period);
        }
    };
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;

        return size;
    }
    
    private static List<Integer> generateRange(int min, int max)
    {
        ArrayList<Integer> rangeList = new ArrayList<Integer>(max - min + 1);
        for(int i = min; i <= max; i++)
        {
            rangeList.add(i);
        }
        
        return rangeList;
    }
    
    private void notifyListeners(Period prevPeriod, Period newPeriod)
    {
        for(PeriodChangeListener listener : _listeners)
        {
            listener.periodChanged(prevPeriod, newPeriod);
        }
    }
    
    public void addPeriodChangeListener(PeriodChangeListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removePeriodChangeListener(PeriodChangeListener listener)
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
    
    private static class PeriodLabel extends JLabel
    {
        private final String _singular, _plural;
        
        PeriodLabel(String singular, String plural)
        {
            _singular = singular;
            _plural = plural;
        }
        
        void updateText(Integer quantity)
        {
            this.setText(new Integer(1).equals(quantity) ? _singular : _plural);
        }
    }
}