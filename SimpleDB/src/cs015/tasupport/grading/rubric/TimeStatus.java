package cs015.tasupport.grading.rubric;

public enum TimeStatus
{
	
	ON_TIME(1.0), LATE(.92), EARLY(1.04), NC_LATE(0);	

	private double _scoreMultiplier = Double.NaN;
	
	private TimeStatus(double multiplier)
	{
		_scoreMultiplier = multiplier;
	}
	
	public String getPrettyPrintName()
	{
		switch(this)
		{
			case ON_TIME:
				return "On Time";
			case LATE:
				return "Late";
			case EARLY:
				return "Early";
			case NC_LATE:
				return "NC Late";
			default:
				return "Invalid Time Status";
		}
	}
	
	public double scoreMultiplier()
	{
		return _scoreMultiplier;
	}	
	
	public double getEarlyBonus(double totalPoints)
	{
		if(this == EARLY)
		{
			return totalPoints * (_scoreMultiplier - 1.0);
		}
		else
		{
			return 0;
		}
	}
	
	public double getEarlyOutOf(double totalPoints)
	{
		{
			return totalPoints * .04;
		}	
	}
	
	public double getLateOutOf(double totalPoints)
	{
		{
			return totalPoints * -.08;
		}	
	}
	
	
	public double getLatePenalty(double totalPoints)
	{
		if(this == LATE)
		{
			return totalPoints * (_scoreMultiplier - 1.0);
		}
		else
		{
			return 0;
		}
	}	
	
	public static TimeStatus getStatus(String statusString)
	{
		TimeStatus[] allStatuses = { ON_TIME, LATE, EARLY, NC_LATE };
		
		for(TimeStatus status : allStatuses)
		{
			if(statusString.toUpperCase().equals(status.toString()))
			{
				return status;
			}
		}
		
		throw new Error("Invalid status string");
	}
	
	public boolean isLate()
	{
		if (this == LATE || this == NC_LATE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
