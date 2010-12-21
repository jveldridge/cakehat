package gradesystem.rubric;

import gradesystem.views.backend.assignmentdist.DistributionRequester;
import gradesystem.config.HandinPart;
import java.util.Collection;
import java.util.Map;

/**
 * Don't directly create this class, access it via the Allocator.
 * All rubric related functionality goes through this class.
 *
 * @author spoletto
 * @author jak2
 */
public interface RubricManager
{
    /**
     * View the rubric for a student for a given handin part. If it is already
     * open it will be brought to front and centered on screen.
     *
     *
     * @param part
     * @param studentLogin
     */
    public void view(HandinPart part, String studentLogin);

    /**
     * View the rubric for a student for a given handin part. If it is already
     * open it will be brought to front and centered on screen.
     *
     * @param part
     * @param studentLogin
     * @param isAdmin if true then on save the rubric's handin score will be written
     *                to the database
     */
    public void view(HandinPart part, String studentLogin, boolean isAdmin);

    /**
     * @date 01/08/2010
     * @return path to student's rubric for a particular project
     *          Note: this is independent of the TA who graded the student
     *         currently, /course/<course>/.cakehat/<year>/rubrics/<assignmentName>/<studentLogin>.gml
     */
    public String getStudentRubricPath(HandinPart part, String studentLogin);

    /**
     * Views an assignment's template rubric.
     *
     * @param rubric
     */
    public void viewTemplate(HandinPart part);

    public boolean hasRubric(HandinPart part, String studentLogin);

    /**
     * Get the scores for the handin part. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return map from student logins to score
     */
    public Map<String, Double> getHandinTotals(HandinPart part, Iterable<String> studentLogins);

    /**
     * Get the score for the handin part. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return
     */
    public double getHandinTotal(HandinPart part, String studentLogin);

    /**
     * Get the score for the entire rubric. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return
     */
    public double getRubricTotal(HandinPart part, String studentLogin);

    /**
     * Get the scores for the entire rubric. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return map from student logins to score
     */
    public Map<String, Double> getRubricTotals(HandinPart part, Iterable<String> studentLogins);

    /**
     * Distributes the rubric for the HandinPart part, mapping TA logins (as
     * strings) to Collections of Strings of studentLogins that TA is assigned
     * to grade. When determining handin time status, takes into account the
     * specified minutes of leniency to apply to deadlines. Also takes into
     * account extensions that have been recorded in the database. If an
     * extension is granted and the policy is MULTIPLE_DEADLINES it is treated
     * as if the policy is NO_LATE using the extension date.
     *
     * @param assignmentName
     * @param distribution
     * @param minutesOfLeniency
     * @return
     */
    public void distributeRubrics(HandinPart part,
                                  Map<String,Collection<String>> distribution,
                                  int minutesOfLeniency,
                                  DistributionRequester requester);

    /**
     * Reads out the time status information from a student's rubric.
     * If the student's rubric cannot be parsed for any reason null will be
     * returned.
     *
     * @param part
     * @param studentLogin
     */
     public TimeStatus getTimeStatus(HandinPart part, String studentLogin);

    /**
     * Reads out the time status information from a student's rubric. If the
     * late policy is DAILY_DEDUCTION and the assignment is LATE, then the
     * number of days late will be included in the returned descriptor.
     *
     * @param part
     * @param studentLogin
     */
     public String getTimeStatusDescriptor(HandinPart part, String studentLogin);

     /**
      * Sets the student's rubric to the given time status and days late. If days late is not
      * applicable pass in 0.
      *
      * @param part
      * @param studentLogin
      * @param status
      * @param daysLate
      */
     public void setTimeStatus(HandinPart part, String studentLogin, TimeStatus status, int daysLate);

    /**
     * Changes the grader of the rubric. Only makes changes in the rubric itself,
     * this has no effect on any information in the database.
     *
     * @param part
     * @param studentLogin
     * @param newGraderLogin
     */
    public void reassignRubric(HandinPart part, String studentLogin, String newGraderLogin);

    /**
     * Converts a rubric to a GRD file.
     *
     * @param part
     * @param studentLogin
     */
    public void convertToGRD(HandinPart part, String studentLogin);

    /**
     * Converts rubrics to GRD files.
     *
     * @param part
     * @param studentLogins
     */
    public void convertToGRD(HandinPart part, Iterable<String> studentLogins);

    public void convertToGRD(Map<HandinPart, Iterable<String>> toConvert);
}