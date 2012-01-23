package support.ui;

/**
 * An implementation of {@link DescriptionProvider} which implements {@link #tooltipText()} to return {@code null}.
 *
 * @author jak2
 */
public abstract class PartialDescriptionProvider<T> implements DescriptionProvider<T>
{
    /**
     * Returns {@code null}.
     * 
     * @return 
     */
    @Override
    public String getToolTipText(T item)
    {
        return null;
    }
}