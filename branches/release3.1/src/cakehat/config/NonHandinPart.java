package cakehat.config;

/**
 * Represents a {@link Part} of an Assignment that is not managed by cakehat
 * aside from the recorded grade. Example usage is for a design check or
 * written problem set.
 * 
 * @author jak2
 */
public class NonHandinPart extends Part
{
    NonHandinPart(Assignment asgn, String name, int number, int points)
    {
        super(asgn, name, number, points);
    }
}