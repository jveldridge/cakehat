package utils.basicXMLviewer;

import com.inet.jortho.SpellChecker;
import frontend.grader.rubric.Section;
import frontend.grader.rubric.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

public class BasicTextField extends JTextArea {

    public BasicTextField(Vector<Entry> text, boolean spellCheckEnabled) {
        //lol spellchecking - psastras
        spellCheckEnabled = false;      //added temporarily b/c Jonathan's VM doesn't have enough memory
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

    public static BasicTextField getAsCommentField(final Section section) {
        final BasicTextField field = new BasicTextField(section.Comments, true);

        //Set appearance
        field.setRows(4);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setBorder(BorderFactory.createEtchedBorder());


        return field;
    }

    public static BasicTextField getAsNotesField(Section section) {
        BasicTextField field = new BasicTextField(section.Notes, false);
        //Set appearance and properties
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setEditable(false);
        return field;
    }
}
