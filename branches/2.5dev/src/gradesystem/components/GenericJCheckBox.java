package gradesystem.components;

import javax.swing.JCheckBox;

/**
 * A parameterized JCheckBox.
 * 
 * @author jeldridg
 */
public class GenericJCheckBox<E> extends JCheckBox {

    private E _item;

    public GenericJCheckBox(E item) {
        super(item.toString());
        _item = item;
    }

    public E getItem() {
        return _item;
    }

}
