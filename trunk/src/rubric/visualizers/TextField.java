package rubric.visualizers;

import com.inet.jortho.SpellChecker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import rubric.Section;
import rubric.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

/**
 * A custom text field for comment and note sections. Cannot be directly
 * constructed, instead use this class's public static methods.
 *
 * @author jak2
 */
class TextField extends JTextArea
{
    private TextField(Vector<Entry> text)
    {
        //Display stored text
        String storedText = "";
        for (Entry entry : text)
        {
            storedText += entry.Text;
            if (entry != text.lastElement())
            {
                storedText += "\n";
            }
        }
        this.setText(storedText);
    }

    /**
     * Turns on spell checking for this field.
     *
     * TODO: Is this really necessary / useful? Lots of code references will
     * be considered improper spelling.
     */
    private void enableSpellChecking()
    {
        SpellChecker.register(this);
        SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
    }

    /**
     * Creates a comment field for a given section. If a StateManager is passed
     * in then each time the field is changed the section passed in is updated
     * and the StateManager is notified.
     *
     * @param section Section to display a comment for
     * @param manager StateManager governing this field, can pass in null for
     * updates to this field to have no effect on the section
     * @return
     */
    public static TextField getAsCommentField(final Section section, final StateManager manager)
    {
        final TextField field = new TextField(section.Comments);
        
        //commented out temporarily b/c Jonathan's VM doesn't have enough memory
        //field.enableSpellChecking();

        //Set appearance
        field.setRows(4);
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setBorder(BorderFactory.createEtchedBorder());

        //If managed, add a listener
        if(manager != null)
        {
            //Add listener to update rubric with data as it is typed
            field.addKeyListener(new KeyAdapter()
            {
                public void keyReleased(KeyEvent e)
                {
                    //Inform manager
                    manager.rubricChanged();

                    //Split on each new line
                    String[] lines = field.getText().split("\n");

                    Vector<Entry> comments = new Vector<Entry>();
                    for (String line : lines)
                    {
                        Entry entry = new Entry();
                        entry.Text = line;
                        comments.add(entry);
                    }
                    section.Comments = comments;
                }
            });
        }

        return field;
    }

    /**
     * Displays a notes field for the Section passed in. This field is not
     * editable.
     *
     * @param section Section to display notes for
     * @return
     */
    public static TextField getAsNotesField(Section section)
    {
        TextField field = new TextField(section.Notes);
        
        //Set appearance and properties
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setEditable(false);

        return field;
    }
}