package frontend.grader;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import frontend.grader.rubric.ExtraCredit;
import frontend.grader.rubric.Subsection;

/**
 * When an editable instance is returned it directly
 * updates the rubric.
 * 
 * @author jak2
 */
class NumberField extends utils.basicXMLviewer.BasicNumberField {

    private Subsection _subsection = null;
    private ExtraCredit _extraCredit = null;
    private MainPanel _panel = null;
    private StateManager _stateManager;
    private double _oldValue;

    private NumberField(double value, boolean editable) {
        super(value, editable);

        _oldValue = value;
    }

    private static NumberField getAsEditable(double value) {
        final NumberField field = new NumberField(value, true);

        field.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
            }

            public void insertUpdate(DocumentEvent e) {
                try {
                    double newValue = Double.parseDouble(field.getText());
                    if (newValue != field._oldValue) {
                        field._oldValue = newValue;
                        field._stateManager.rubricChanged();
                    }
                    if (field._subsection != null) {
                        if (newValue > field._subsection.OutOf)
                        {
                            field.setBackground(java.awt.Color.RED);
                        }
                        else
                        {
                            field.setBackground(java.awt.Color.WHITE);
                        }
                        field._subsection.Score = newValue;
                        field._panel.updateTotals();
                    } else if (field._extraCredit != null) {
                        if (newValue > field._extraCredit.OutOf)
                        {
                            field.setBackground(java.awt.Color.RED);
                        }
                        else
                        {
                            field.setBackground(java.awt.Color.WHITE);
                        }
                        field._extraCredit.Score = newValue;
                        field._panel.updateTotals();
                    }
                } catch (Exception exc) {
                }
            }

            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });

        field.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        field.selectAll();
                    }
                });
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("0");
                    try {
                        double newValue = 0.0;
                        if (newValue != field._oldValue) {
                            field._oldValue = newValue;
                            field._stateManager.rubricChanged();
                        }
                        if (field._subsection != null) {
                            field._subsection.Score = newValue;
                            field._panel.updateTotals();
                        } else if (field._extraCredit != null) {
                            field._extraCredit.Score = newValue;
                            field._panel.updateTotals();
                        }
                    } catch (Exception exc) {
                    }
                }
            }
        });

        return field;
    }

    public static NumberField getAsScore(Subsection subsection, MainPanel panel, StateManager manager) {
        NumberField field = getAsEditable(subsection.Score);
        field._subsection = subsection;
        field._panel = panel;
        field._stateManager = manager;
        return field;
    }

    public static NumberField getAsScore(ExtraCredit ec, MainPanel panel, StateManager manager) {
        NumberField field = getAsEditable(ec.Score);
        field._extraCredit = ec;
        field._panel = panel;
        field._stateManager = manager;
        return field;
    }
    
}