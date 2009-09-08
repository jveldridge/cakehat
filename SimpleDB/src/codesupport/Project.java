package codesupport;

import java.util.Calendar;
import java.util.Collection;
import java.io.File;

public class Project
{
	private final static String HANDIN_DIR = "/course/cs015/handin/";
	private String _projectName;
	private final Collection<File> HAND_INS;
	public Project(String prjName)
	{
        _projectName = prjName;
		HAND_INS = initializeHandins();
	}

	String getName()
	{
		return _projectName;
	}

	private String getHandinPath()
	{
		String path = HANDIN_DIR + getName() + "/" + getCurrentYear() + "/";
		return path;
	}

	private int getCurrentYear()
	{
		return 2008; //For testing purposes only
		//return Calendar.getInstance().get(Calendar.YEAR);
	}

	private Collection<File> initializeHandins()
	{
		return Utils.getFiles(getHandinPath(), "tar");
	}

	Collection<File> getHandins()
	{
		return HAND_INS;
	}

	File getHandin(String studentLogin)
	{
		for(File handin : getHandins())
		{
			if(handin.getName().equals(studentLogin+".tar"))
			{
				return handin;
			}
		}

		return null;
	}
}
