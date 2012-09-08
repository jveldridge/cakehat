package cakehat.views.shared.gradingsheet;

import java.awt.event.FocusEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.BorderUIResource.CompoundBorderUIResource;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.plaf.metal.MetalBorders.TextFieldBorder;
import javax.swing.text.BadLocationException;
import support.ui.DocumentAdapter;

/**
 *
 * @author jak2
 */
class EarnedField extends JTextField
{
    private static final Color WARNING_COLOR = new Color(255, 255, 204);    //Pastel yellow
    private static final Color ERROR_COLOR = new Color(255, 204, 204);      //Pastel red
    private static final Color VALID_COLOR = Color.WHITE;
    
    private final Set<EarnedListener> _listeners = new HashSet<EarnedListener>();
    private Double _currEarned;
    private final Double _outOf;

    EarnedField(Double initialEarned, Double outOf)
    {
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
                        scrollToVisible();
                        selectAll();
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
                        String text = getText();
                        if(!text.isEmpty())
                        {
                            try
                            {
                                Double.parseDouble(text);
                            }
                            catch(NumberFormatException e)
                            {
                                setEarned(null);
                            }
                        }
                    }
                });
            }
        });
        
        this.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            public void modificationOccurred(DocumentEvent de)
            {
                boolean isEmpty = false;
                Double earned = null;
                try
                {   
                    String text = de.getDocument().getText(0, de.getDocument().getLength());
                    isEmpty = text.isEmpty();
                    
                    if(!isEmpty)
                    {
                        try
                        {
                            earned = Double.parseDouble(text);
                        }
                        catch(NumberFormatException e)
                        {
                            earned = null;
                        }
                    }
                }
                catch(BadLocationException e) { }
                
                applyColorIndicator(earned, isEmpty);
                
                //Notify listeners
                if(_currEarned != earned)
                {
                    notifyListeners(_currEarned, earned);
                    _currEarned = earned;
                }
            }
        });
        
        this.setEarned(initialEarned);
    }
    
    @Override
    public void setEnabled(boolean enable)
    {
        super.setEnabled(enable);
        
        //Call setEarned(...) with the current earned value so that the visual is updated to reflect the disabled
        //status
        this.setEarned(_currEarned);
    }

    final void setEarned(Double value)
    {
        if(value == null)
        {
            this.setText("");
            this.applyColorIndicator(value, true);
        }
        else
        {
            this.setText(Double.toString(value));
            this.applyColorIndicator(value, false);
        }
    }

    final Double getEarned()
    {
        Double points = null;
        
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
    
    private void applyColorIndicator(Double earned, boolean isEmpty)
    {
        if(this.isEnabled())
        {
            //The custom border used when enabled
            this.setBorder(BorderFactory.createEtchedBorder());
            
            //Subtractive grading - this scenario is not fully supported yet
            if(_outOf == null)
            {
                this.setBackground(VALID_COLOR);
                this.setToolTipText(null);
            }
            else
            {
                if(isEmpty)
                {
                    this.setBackground(WARNING_COLOR);
                    this.setToolTipText("No points have been assigned");
                }
                else if(earned == null)
                {
                    this.setBackground(ERROR_COLOR);
                    this.setToolTipText("Value is not a number");
                }
                else if(earned < 0.0)
                {
                    this.setBackground(WARNING_COLOR);
                    this.setToolTipText("Points assigned is negative");
                }
                else if(earned <= _outOf)
                {
                    this.setBackground(VALID_COLOR);
                    this.setToolTipText(null);
                }
                else
                {
                    this.setBackground(WARNING_COLOR);
                    this.setToolTipText("Points assigned exceeds out of");
                }
            }
        }
        else
        {
            //Creates and sets a border identical to the default Swing Metal border
            MarginBorder innerBorder = new MarginBorder();
            TextFieldBorder outerBorder = new TextFieldBorder();
            CompoundBorderUIResource border = new CompoundBorderUIResource(innerBorder, outerBorder);
            this.setBorder(border);
            
            this.setBackground(VALID_COLOR);
            this.setToolTipText("Points cannot be modified while grading is submitted");
        }
    }
    
    static interface EarnedListener
    {
        void earnedChanged(Double prevEarned, Double currEarned);
    }
    
    void addEarnedListener(EarnedListener listener)
    {
        _listeners.add(listener);
    }
    
    void removeEarnedListener(EarnedListener listener)
    {
        _listeners.remove(listener);
    }
    
    private void notifyListeners(Double prevEarned, Double currEarned)
    {
        for(EarnedListener listener : _listeners)
        {
            listener.earnedChanged(prevEarned, currEarned);
        }
    }
 
    private void scrollToVisible()
    {
        if(this.getParent() instanceof JComponent)
        {
            ((JComponent) this.getParent()).scrollRectToVisible(this.getBounds());
        }
    }
}