
package frontend.tester;

/**
 *
 * @author spoletto
 */
public abstract class GenericTester
{
    private TesterManager _manager;

    public GenericTester(String asgnName, String studentAcct)
    {
        _manager = new TesterManager(asgnName, studentAcct);
    }

    public void testComplete(String testName, String status, String details)
    {
        _manager.testComplete(testName, status, details);
    }

    public void allTestsComplete()
    {
        _manager.writeToXML();
    }

}
