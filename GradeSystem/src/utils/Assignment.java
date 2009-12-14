package utils;

import java.util.Calendar;

public class Assignment
{	
	public String Name = "";
	public int Number;
	public AssignmentType Type;
	
	public Points Points = new Points();
	public Calendar Outdate, Early, Ontime, Late;

    public String toString()
    {
        return Name;
    }
}
