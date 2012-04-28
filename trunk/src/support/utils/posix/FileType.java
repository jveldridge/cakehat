package support.utils.posix;

/**
 * The various types of files that exist on *NIX.
 *
 * @author jak2
 */
public enum FileType
{
    //All values are octal
    REGULAR_FILE(0100000), DIRECTORY(040000), SYMBOLIC_LINK(0120000), DOMAIN_SOCKET(0140000), CHARACTER_SPECIAL(020000),
    BLOCK_SPECIAL(060000), NAMED_PIPE(010000);

    private int _value;

    private FileType(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }
}