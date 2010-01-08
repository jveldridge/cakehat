/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package config;

/**
 *
 * @author jak2
 */
public class NonCodeHandin extends HandinPart
{
    NonCodeHandin(Assignment asgn, String name, int points)
    {
        super(asgn, name, points);
    }

    @Override
    public boolean hasCode()
    {
        return false;
    }

    @Override
    public void openCode(String studentLogin) { }

    @Override
    public void printCode(String studentLogin, String printer) { }

    @Override
    public void printCode(Iterable<String> studentLogins, String printer) { }

    @Override
    public void run(String studentLogin) { }

    @Override
    public boolean hasDemo()
    {
        return false;
    }

    @Override
    public void runDemo() { }

    @Override
    public boolean hasTester()
    {
        return false;
    }

    @Override
    public void runTester(String studentLogin) { }
}
