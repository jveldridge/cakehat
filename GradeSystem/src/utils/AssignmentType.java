package utils;

public enum AssignmentType
{
	FINAL, PROJECT, LAB, HOMEWORK;
	
	public static AssignmentType getInstance(String typeString)
	{		
		for(AssignmentType type : values())
		{
			if(typeString.toUpperCase().equals(type.toString()))
			{
				return type;
			}
		}
		
		throw new RuntimeException("Invalid input: " + typeString +
                                   ", valid options are " +
                                   java.util.Arrays.toString(values()));
	}
}