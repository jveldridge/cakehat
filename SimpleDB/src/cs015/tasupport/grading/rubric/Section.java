package cs015.tasupport.grading.rubric;

public class Section
{
	public String Name = "";	
	public java.util.Vector<Subsection> Subsections = new java.util.Vector<Subsection>();
	public java.util.Vector<Entry> Notes = new java.util.Vector<Entry>();
	public java.util.Vector<Entry> Comments = new java.util.Vector<Entry>();
	
	public double getSectionScore() 
	{
		double score = 0.0;
		for(Subsection subsection : this.Subsections)
		{
			score += subsection.Score;
		}
		return score;
	}
	
	public double getSectionOutOf()
	{
		double outOf = 0.0;
		for(Subsection subsection : this.Subsections)
		{
			outOf += subsection.OutOf;
		}
		return outOf;
	}
}
