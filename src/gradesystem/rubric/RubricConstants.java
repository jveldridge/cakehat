package gradesystem.rubric;

/**
 * Constants of the XML tags and attributes used in the GML markup. Used for
 * reading and writing the GML files.
 *
 * @author jak2
 */
interface RubricConstants
{
    public final static String

    TEXT_NODE = "#text", COMMENT_NODE = "#comment",
    RUBRIC = "RUBRIC",
    NAME = "NAME", NUMBER = "NUMBER", STATUS = "STATUS", DAYS_LATE = "DAYS-LATE",

    STUDENT = "STUDENT", GRADER = "GRADER", ACCT = "ACCT",
            
    SECTION = "SECTION", EXTRA_CREDIT = "EXTRA-CREDIT",

    TEXT = "TEXT", SCORE = "SCORE", SOURCE = "SOURCE",
    OUTOF = "OUTOF", SUBSECTION = "SUBSECTION",
    NOTES = "NOTES", COMMENTS = "COMMENTS", DETAIL = "DETAIL",
    VALUE = "VALUE", ENTRY = "ENTRY";
}