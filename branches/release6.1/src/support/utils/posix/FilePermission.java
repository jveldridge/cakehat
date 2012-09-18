package support.utils.posix;

/**
 * POSIX file permissions. These are used instead of direct use of octal values. This is for two reasons:
 * <ul>
 * <li>There is no guaranteed way to ensure octal values are used, and 0700 and 700 have completely different values.
 * Mistakenly using the non-octal value would introduce a horrendous bug.</li>
 * <li>This form is more object oriented.</li>
 * </ul>
 */
public enum FilePermission
{
    //All values are octal        
    OWNER_READ(0400), OWNER_WRITE(0200), OWNER_EXECUTE(0100),
    GROUP_READ(0040), GROUP_WRITE(0020), GROUP_EXECUTE(0010),
    OTHERS_READ(0004), OTHERS_WRITE(0002), OTHERS_EXECUTE(0001),
    SET_USER_ID_UPON_EXECUTION(04000), SET_GROUP_ID_UPON_EXECUTION(02000), STICKY_BIT(01000);

    private final int _value;

    private FilePermission(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }
}