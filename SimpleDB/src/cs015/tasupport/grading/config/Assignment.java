package cs015.tasupport.grading.config;

import java.util.Calendar;

public class Assignment
{	
	public String Name = "";
	public int Number;
	public AssignmentType Type;
	
	public Points Points = new Points();
	public Calendar Outdate, Early, Ontime, Late;
}
