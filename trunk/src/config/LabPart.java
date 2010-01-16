package config;

import java.io.File;
import java.util.HashMap;
import utils.Allocator;

/**
 * Represents a lab part of an assignment. Can retrieve a student's score for
 * a lab.
 *
 * @author jak2
 */
public class LabPart extends Part
{
    private int _labNumber;
    //Maps student logins to points earned
    private HashMap<String, Integer> _scores = null;

    LabPart(Assignment asgn, String name, int points, int labNumber)
    {
        super(asgn, name, points);

        _labNumber = labNumber;
    }

    public int getLabNumber()
    {
        return _labNumber;
    }

    /**
     * Returns the score a student received on this lab. If there is no record
     * for the student 0 will be returned.
     *
     * @param studentLogin
     * @return
     */
    public double getScore(String studentLogin)
    {
        //If scores haven't been processed, process them
        if(_scores == null)
        {
            this.processLabScores();
        }

        //If the student has a score, return it
        if(_scores.containsKey(studentLogin))
        {
            return _scores.get(studentLogin);
        }
        //Else, 0
        else
        {
            return 0;
        }
    }

    /**
     * Processes the lab scores.
     */
    private void processLabScores()
    {
        File dir = new File(Allocator.getCourseInfo().getLabsDir() + _labNumber + "/");
        
        _scores = new HashMap<String, Integer>();

        if(dir.exists())
        {
            for(File file : dir.listFiles())
            {
                String[] parts = file.getName().split(",");

                int score = this.getPoints();
                if(parts.length == 2)
                {
                    score = Integer.valueOf(parts[1]);
                }
                _scores.put(parts[0], score);

                System.out.println(parts[0] + ":" + score);
            }
        }
    }
}