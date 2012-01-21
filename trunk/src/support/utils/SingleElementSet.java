package support.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An immutable set of exactly one element. The element may be {@code null}.
 *
 * @author jak2
 */
public final class SingleElementSet<E> implements Set<E>
{
    private final E _element;
    
    private SingleElementSet(E element)
    {
        _element = element;
    }
    
    public static <T> SingleElementSet<T> of(T element)
    {
        return new SingleElementSet<T>(element);
    }

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean contains(Object o)
    {
        return _element == null ? o == null : _element.equals(o);
    }

    @Override
    public Iterator<E> iterator()
    {
        return new Iterator<E>()
        {
            private boolean _nextCalled = false;
            
            @Override
            public boolean hasNext()
            {
                return !_nextCalled;
            }

            @Override
            public E next()
            {
                if(_nextCalled)
                {
                    throw new NoSuchElementException();
                }
                else
                {
                    _nextCalled = true;
                    
                    return _element;
                }
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("mutation not supported");
            }
        };
    }

    @Override
    public Object[] toArray()
    {
        return new Object[] { _element };
    }

    @Override
    public <T> T[] toArray(T[] ts)
    {
        if(ts.length == 0)
        {
            ts = (T[]) java.lang.reflect.Array.newInstance(ts.getClass().getComponentType(), 1);
        }
        
        ts[0] = (T) _element;
        
        return ts;
    }

    @Override
    public boolean containsAll(Collection<?> clctn)
    {
        boolean containsAll = true;
        for(Object elem : clctn)
        {
            if(!this.contains(elem))
            {
                containsAll = false;
                break;
            }
        }
        
        return containsAll;
    }

    @Override
    public boolean add(E e)
    {
        throw new UnsupportedOperationException("mutation not supported");
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException("mutation not supported");
    }

    @Override
    public boolean addAll(Collection<? extends E> clctn)
    {
        throw new UnsupportedOperationException("mutation not supported");
    }

    @Override
    public boolean retainAll(Collection<?> clctn)
    {
        throw new UnsupportedOperationException("mutation not supported");
    }

    @Override
    public boolean removeAll(Collection<?> clctn)
    {
        throw new UnsupportedOperationException("mutation not supported");
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("mutation not supported");
    }
}