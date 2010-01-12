package rubric;

import com.inet.jortho.SpellChecker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import rubric.Rubric.Section;

/**
 * A custom text field for comment and note sections. Cannot be directly
 * constructed, instead use this class's public static methods.
 *
 * @author jak2
 */
class TextField extends JTextArea
{
    private TextField(Collection<String> text)
    {
        //Display stored text
        String storedText = "";

        String[] textArray = text.toArray(new String[0]);
        for(int i = 0; i < textArray.length; i++)
        {
            storedText += textArray[i];

            //if not the last entry
            if(i != textArray.length-1)
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
        final TextField field = new TextField(section.getComments());
        
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

                    Vector<String> comments = new Vector<String>();
                    for (String line : lines)
                    {
                        comments.add(line);
                    }

                    //Set comments
                    section.setComments(comments);
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
        TextField field = new TextField(section.getNotes());
        
        //Set appearance and properties
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setEditable(false);

        return field;
    }
}