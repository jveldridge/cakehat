package backend.visualizer;

import frontend.grader.*;
import com.inet.jortho.SpellChecker;
import frontend.grader.rubric.Section;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import frontend.grader.rubric.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

class VizTextField extends JTextArea {

    private VizTextField(Vector<Entry> text, boolean spellCheckEnabled) {
        //lol spellchecking - psastras
        if (spellCheckEnabled) {
            SpellChecker.register(this);
            SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
        }
        //Display stored text
        String storedText = "";
        for (Entry entry : text) {
            storedText += entry.Text;
            if (entry != text.lastElement()) {
                storedText += "\n";
            }
        }
        this.setText(storedText);
    }

    public static VizTextField getAsCommentField(final Section section) {
        final VizTextField field = new VizTextField(section.Comments, true);

        //Set appearance
        field.setRows(4);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setBorder(BorderFactory.createEtchedBorder());


        return field;
    }

    public static VizTextField getAsNotesField(Section section) {
        VizTextField field = new VizTextField(section.Notes, false);
        //Set appearance and properties
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setEditable(false);
        return field;
    }
}
