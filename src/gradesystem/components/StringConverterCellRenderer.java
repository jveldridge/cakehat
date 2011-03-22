package gradesystem.components;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Uses a StringConverter to determine what is rendered for each cell. Delegates
 * the actual rendering.
 *
 * @param <T> The type of the data being rendered to a cell. This type must be
 * accurate or a {@link ClassCastException} will arise. In practice this means
 * the data model using by the Swing Component must be of type <code>T</code>.
 *
 * @author jak2
 */
class StringConverterCellRenderer<T> implements ListCellRenderer
{
    interface ItemInfoProvider<T>
    {
        public T getElementDisplayedAt(int i);
    }

    private final StringConverter<T> _converter;
    private final ListCellRenderer _delegateRenderer;
    private final Map<T, String> _cachedConversions;
    private final ItemInfoProvider<T> _infoProvider;

    /**
     * A ListCellRenderer that uses the <code>converter</code> to determine what
     * is actually rendered by the <code>delegateRenderer</code>. The
     * <code>delegateRenderer</code> should be the renderer that would normally
     * be used if this renderer was not used.
     *
     * @param delegateRenderer
     * @param converter
     */
    public StringConverterCellRenderer(ListCellRenderer delegateRenderer,
            ItemInfoProvider<T> infoProvider, StringConverter<T> converter)
    {
        _delegateRenderer = delegateRenderer;
        _infoProvider = infoProvider;
        _converter = converter;
        _cachedConversions = new HashMap<T, String>();
    }

    /**
     * Clears the cached Strings built using the supplied
     * {@link StringConverter}. The cache is used to increase performance, but
     * the cache will need to be cleared when updated conversions are desired.
     */
    public void clearCache()
    {
        _cachedConversions.clear();
    }

    /**
     * Renders the cell using the <code>delegateRenderer</code> provided to the
     * constructor. Intercepts the object that the <code>delegateRenderer</code>
     * would normally have rendered and instead has it render the String
     * supplied by the <code>converter</code> provided to the constructor.
     *
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
    {
        T valueInModel = _infoProvider.getElementDisplayedAt(index);
        if(!_cachedConversions.containsKey(valueInModel))
        {
            _cachedConversions.put(valueInModel, _converter.convertToString(valueInModel));
        }
        String representation = _cachedConversions.get(valueInModel);

        return _delegateRenderer.getListCellRendererComponent(list,
                representation, index, isSelected, cellHasFocus);
    }
}