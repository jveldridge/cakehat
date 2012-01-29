package support.utils;

/**
 *
 * @author jak2
 */
public class Pair<A, B>
{
    private A _first;
    private B _second;
    
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

    public void setFirst(A first)
    {
        _first = first;
    }

    public B getSecond()
    {
       return _second;
    }

    public void setSecond(B second)
    {
        _second = second;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }
        if(getClass() != obj.getClass())
        {
            return false;
        }
        final Pair<A, B> other = (Pair<A, B>) obj;
        if(this._first != other._first && (this._first == null || !this._first.equals(other._first)))
        {
            return false;
        }
        if(this._second != other._second && (this._second == null || !this._second.equals(other._second)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 61 * hash + (this._first != null ? this._first.hashCode() : 0);
        hash = 61 * hash + (this._second != null ? this._second.hashCode() : 0);
        return hash;
    }
}