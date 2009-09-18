package cs015.tasupport.grading.rubric;

public class Rubric
{
	//Assignment
	public String Name = "", Status = "";
	public int Number = 0;
	
	//Student
	public Student Student = new Student();
	
	//Grader
	public Grader Grader = new Grader();
	
	//Sections
	public java.util.Vector<Section> Sections = new java.util.Vector<Section>();
	
	//Extra credit
	public java.util.Vector<ExtraCredit> ExtraCredit = new java.util.Vector<ExtraCredit>();
	
	public double getTotalScore()
	{
		double score = 0.0;
		for (Section section: Sections)
		{
			for(Subsection subsection : section.Subsections)
			{
				score += subsection.Score;
			}
		}
		for (ExtraCredit extraCredit : ExtraCredit)
		{
			score += extraCredit.Score;
		}
		TimeStatus status = TimeStatus.getStatus(this.Status);
		score += status.getEarlyBonus(getTotalOutOf());
		score += status.getLatePenalty(getTotalOutOf());
		if (status.getPrettyPrintName().toUpperCase().equals("NC LATE"))
		{
			score = 0.0;
		}
		return score;
	}
	
	public double getTotalOutOf()
	{
		double outOf = 0.0;
		for (Section section: Sections)
		{
			for(Subsection subsection : section.Subsections)
			{
				outOf += subsection.OutOf;
			}
		}
		return outOf;
	}
}
