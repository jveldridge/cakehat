package cs015.tasupport.grading.config;

import cs015.tasupport.utils.Utils;

public class TestMain
{
	/**
	 * Test main that prints out all fields of the config file.
	 */
	public static void main(String[] args)
	{
		for(String login : ConfigurationManager.getGraderLogins())
		{
			System.out.println("Grader = " + login);
		}
		System.out.println(" ");
		for(String login : ConfigurationManager.getAdminLogins())
		{
			System.out.println("Admin = " + login);
		}
		System.out.println(" ");
		for(Assignment asgn : ConfigurationManager.getAssignments())
		{
			System.out.println(asgn.Name + " " + asgn.Number);
			System.out.println("Type = " + asgn.Type);
			System.out.println("DQ = " + asgn.Points.DQ + ", TOTAL = " + asgn.Points.TOTAL);
			System.out.println("Outdate = " + Utils.getCalendarAsString(asgn.Outdate));
			System.out.println("Early = " + Utils.getCalendarAsString(asgn.Early));
			System.out.println("OnTime = " + Utils.getCalendarAsString(asgn.Ontime));
			System.out.println("Late = " + Utils.getCalendarAsString(asgn.Late));
			System.out.println(" ");
		}
	}
}
