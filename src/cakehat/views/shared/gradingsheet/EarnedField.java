package cakehat.views.shared.gradingsheet;

import java.awt.event.FocusEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 *
 * @author jak2
 */
class EarnedField extends JFormattedTextField
{
    private final Set<EarnedListener> _listeners = new HashSet<EarnedListener>();
    private double _currEarned;
    private double _outOf;

    public EarnedField(double initialEarned, double outOf)
    {
        super(NumberFormat.getNumberInstance());

        _currEarned = initialEarned;
        _outOf = outOf;
        
        this.setColumns(5);
        this.setMinimumSize(this.getPreferredSize());
        this.setMaximumSize(this.getPreferredSize());
        this.setHorizontalAlignment(JTextField.CENTER);

        this.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent fe)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        EarnedField.this.selectAll();
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent fe)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(getText().isEmpty())
                        {
                            setText("0");
                        }
                        
                        double earned = getEarned();
                        if(_currEarned != earned)
                        {
                            notifyListeners(_currEarned, earned);
                            _currEarned = earned;
                        }
                    }
                });
            }
        });
        
        this.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent de) { anyUpdate(de); }

            @Override
            public void removeUpdate(DocumentEvent de) { anyUpdate(de); }

            @Override
            public void changedUpdate(DocumentEvent de) { anyUpdate(de); }
            
            public void anyUpdate(DocumentEvent de)
            {
                Double currEarned = 0D;
                try
                {   
                    String text = de.getDocument().getText(0, de.getDocument().getLength());

                    if(!text.isEmpty())
                    {
                        try
                        {
                            currEarned = Double.parseDouble(text);
                        }
                        catch(NumberFormatException e)
                        {
                            currEarned = null;
                        }
                    }
                }
                catch(BadLocationException e) { }
                
                applyColorIndicator(currEarned);
            }
        });
        
        this.setEarned(initialEarned);
    }

    public final void setEarned(double value)
    {
        try
        {
            this.setText(this.getFormatter().valueToString(value));
        }
        catch(ParseException ex) { }
    }

    public final double getEarned()
    {
        double points = 0;

        String text = this.getText();
        if(!text.isEmpty())
        {
            try
            {
                points = Double.parseDouble(text);
            }
            catch(NumberFormatException e) { }
        }

        return points;
    }

    private void applyColorIndicator(Double earned)
    {
        if(earned == null)
        {
            this.setBackground(Color.RED);
            this.setToolTipText("Value is not a number");
        }
        else if(earned == 0.0D)
        {
            this.setBackground(Color.YELLOW);
            this.setToolTipText("No points have been assigned");
        }
        else if(earned <= _outOf)
        {
            this.setBackground(Color.WHITE);
            this.setToolTipText(null);
        }
        else
        {
            this.setBackground(Color.RED);
            this.setToolTipText("Points assigned exceeds out of");
        }
    }
    
    static interface EarnedListener
    {
        public void earnedChanged(double prevEarned, double currEarned);
    }
    
    public void addEarnedListener(EarnedListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeEarnedListener(EarnedListener listener)
    {
        _listeners.remove(listener);
    }
    
    private void notifyListeners(double prevEarned, double currEarned)
    {
        for(EarnedListener listener : _listeners)
        {
            listener.earnedChanged(prevEarned, currEarned);
        }
    }
}