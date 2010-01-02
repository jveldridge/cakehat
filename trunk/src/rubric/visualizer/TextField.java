package rubric.visualizer;

import com.inet.jortho.SpellChecker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import rubric.Section;
import rubric.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

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

    private void enableSpellChecking()
    {
        SpellChecker.register(this);
        SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
    }

    public static TextField getAsCommentField(final Section section, final StateManager manager)
    {
        final TextField field = new TextField(section.Comments);
        
        //commented out temporarily b/c Jonathan's VM doesn't have enough memory
        //TODO: Do we really want spell checking? Plus it sounds like it eats up resources.
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