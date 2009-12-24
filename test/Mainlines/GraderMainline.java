
package Mainlines;

import frontend.grader.*;

public class GraderMainline
{
	public static void main(String[] args)
	{
		Grader g = new Grader(args[0],args[1],args[2]);
        g.setLocationRelativeTo(null);
        g.setVisible(true);
	}
}
