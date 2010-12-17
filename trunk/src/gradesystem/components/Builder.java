package gradesystem.components;

/**
 * A builder for a class should allow for setting fields and then on build
 * should construct an object with immutable fields that have the values
 * provided to the builder.
 *
 * @author jak2
 */
public interface Builder<T>
{
    public T build();
}