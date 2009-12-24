package backend.visualizer;

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
class VizNumberField extends JFormattedTextField {


    private VizNumberField(double value, boolean editable) {
        super(NumberFormat.getNumberInstance());
        this.setText(Double.toString(value));
        this.setEditable(editable);
        this.setColumns(5);

        this.setHorizontalAlignment(CENTER);

    }

    private static VizNumberField getAsEditable(double value) {
        final VizNumberField field = new VizNumberField(value, true);
        return field;
    }

    public static VizNumberField getAsUneditable(double value) {
        VizNumberField toReturn = new VizNumberField(value, false);
        toReturn.setBackground(java.awt.Color.LIGHT_GRAY);
        return toReturn;
    }

    public static VizNumberField getAsScore(Subsection subsection) {
        VizNumberField field = getAsEditable(subsection.Score);
        return field;
    }

    public static VizNumberField getAsScore(ExtraCredit ec) {
        VizNumberField field = getAsEditable(ec.Score);
        return field;
    }
}
