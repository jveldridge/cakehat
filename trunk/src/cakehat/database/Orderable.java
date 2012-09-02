package cakehat.database;

/**
 *
 * @author jak2
 */
public interface Orderable<E extends Orderable>
{
    public void setOrder(int order);
    
    public int getOrder();
    
    /**
     * Returns an iterable that contains all elements which make up the ordering. This element will be in the returned
     * iterable.
     * 
     * @return 
     */
    public Iterable<E> getOrderedElements();
}