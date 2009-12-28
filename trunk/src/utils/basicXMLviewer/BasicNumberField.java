package utils.basicXMLviewer;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

import frontend.grader.rubric.ExtraCredit;
import frontend.grader.rubric.Subsection;

/**
 * When an editable instance is returned it directly
 * updates the rubric.
 * 
 * @author jak2
 */
public class BasicNumberField extends JFormattedTextField {

    public BasicNumberField(double value, boolean editable) {
        super(NumberFormat.getNumberInstance());
        this.setText(Double.toString(value));
        this.setEditable(editable);
        this.setColumns(5);

        this.setHorizontalAlignment(CENTER);

    }

    private static BasicNumberField getAsEditable(double value) {
        final BasicNumberField field = new BasicNumberField(value, true);
        return field;
    }

    public static BasicNumberField getAsUneditable(double value) {
        BasicNumberField toReturn = new BasicNumberField(value, false);
        toReturn.setBackground(java.awt.Color.LIGHT_GRAY);
        return toReturn;
    }

    public static BasicNumberField getAsScore(Subsection subsection) {
        BasicNumberField field = getAsEditable(subsection.Score);
        return field;
    }

    public static BasicNumberField getAsScore(ExtraCredit ec) {
        BasicNumberField field = getAsEditable(ec.Score);
        return field;
    }
}
