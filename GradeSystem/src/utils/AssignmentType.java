package utils;

public enum AssignmentType
{
	PROJECT, LAB, HOMEWORK;
	
	public static AssignmentType getInstance(String typeString)
	{
		AssignmentType[] types = { PROJECT, LAB, HOMEWORK };
		
		for(AssignmentType type : types)
		{
			if(typeString.toUpperCase().equals(type.toString()))
			{
				return type;
			}
		}
		
		throw new Error("Invalid type string");
	}
}
