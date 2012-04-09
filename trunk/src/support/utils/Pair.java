package support.utils;

/**
 * An immutable pair of two values.
 *
 * @author jak2
 */
public class Pair<A, B>
{
    private final A _first;
    private final B _second;
    
    public static <A, B>  Pair<A, B> of(A first, B second)
    {
        return new Pair<A, B>(first, second);
    }

    private Pair(A first, B second)
    {
        _first = first;
        _second = second;
    }

    public A getFirst()
    {
        return _first;
    }

    public B getSecond()
    {
       return _second;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean equal = false;
        if(obj instanceof Pair)
        {
            Pair other = (Pair) obj;
            
            equal = ((_first == null && other._first == null) || (_first != null && _first.equals(other._first))) &&
                    ((_second == null && other._second == null) || (_second != null && _second.equals(other._second)));
        }
        
        return equal;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 61 * hash + (_first != null ? _first.hashCode() : 0);
        hash = 61 * hash + (_second != null ? _second.hashCode() : 0);
        
        return hash;
    }
    
    @Override
    public String toString()
    {
        return "[" + _first + ", " + _second + "]";
    }
}