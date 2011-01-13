package gradesystem.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import gradesystem.Allocator;

/**
 * Represents a lab part of an assignment. Can retrieve check off scores for a
 * lab.
 *
 * @author jak2
 */
public class LabPart extends Part
{
    private final int _labNumber;

    LabPart(Assignment asgn, String name, int number, int points, int labNumber)
    {
        super(asgn, name, number, points);

        _labNumber = labNumber;
    }

    /**
     * Lab number that is used when checking off a lab. Also used by this
     * class to retrieve the scores given.
     *
     * @return
     */
    public int getLabNumber()
    {
        return _labNumber;
    }

    /**
     * Returns the score a student received on this lab. If there is no record
     * for the student 0 will be returned.
     *
     * This will reprocess all scores for this lab, so if you want multiple
     * lab scores you should call getLabScores() instead of this method.
     *
     * @param studentLogin
     * @return lab score
     */
    public double getScore(String studentLogin)
    {
        //If scores haven't been processed, process them
        Map<String, Double> scores = getLabScores();

        //If the student has a score, return it
        if(scores.containsKey(studentLogin))
        {
            return scores.get(studentLogin);
        }
        //Else, 0
        else
        {
            return 0;
        }
    }

    /**
     * Returns a mapping of student logins to scores received for this lab.
     * This will return logins as specified by the check off script, so if
     * someone checked off with a login that is wrong, it will still be
     * returned by this method.
     *
     * @return mapping of student logins to lab scores
     */
    public Map<String, Double> getLabScores()
    {
        File dir = new File(Allocator.getCourseInfo().getLabsDir() + _labNumber + "/");
        
        Map<String, Double> scores = new HashMap<String, Double>();

        if(dir.exists())
        {
            for(File file : dir.listFiles())
            {
                String[] parts = file.getName().split(",");

                double score = this.getPoints();
                if(parts.length == 2)
                {
                    score = Double.parseDouble(parts[1]);
                }
                scores.put(parts[0], score);
            }
        }

        return scores;
    }
}