package gradesystem.rubric;

import gradesystem.views.backend.assignmentdist.DistributionRequester;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
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
    public void viewTemplate(DistributablePart dp) throws RubricException;

    public void view(DistributablePart part, Group group, boolean isAdmin);

    /**
     * View the rubric for a group for the given distributable part.
     * If it is already open it will be brought to front and centered on screen.
     *
     * @param part
     * @param group
     * @param isAdmin
     * @param listener
     */
    public void view(DistributablePart part, Group group, boolean isAdmin, RubricSaveListener listener);

    /**
     * Returns whether or not a rubric exists for the given Group for the given
     * DistributablePart.
     *
     * @param part
     * @param group
     * @return true if the Group has a rubric for the DistributablePart; false otherwise
     */
    public boolean hasRubric(DistributablePart part, Group group);

    /**
     * Returns a list of all DistributableParts of the given handin for which the
     * given Group does not have a rubric.  The list will be empty if the Group has
     * a rubric for all DistributableParts of the Handin.
     * 
     * @param handin
     * @param group
     * @return
     */
    public Collection<DistributablePart> getMissingRubrics(Handin handin, Group group);

    /**
     * Get the score for the given Group for the given DistributablePart.
     *
     * @param part
     * @param group
     * @return
     */
    public double getPartScore(DistributablePart part, Group group);

    /**
     * Read the total score from the rubric for each Group on the given DistributablePart.
     *
     * @param group
     * @return map from group to score
     */
    public Map<Group, Double> getPartScores(DistributablePart part, Iterable<Group> groups);

    /**
     * Calculates the appropriate deduction or bonus based on the TimeStatus of,
     * the group's Handin, what the LatePolicy governing this assignment is, and whether the
     * LatePolicy applies to the entire rubric (AFFECT-ALL="TRUE") or just the
     * handin parts of the rubric (AFFECT-ALL="FALSE").
     *
     * If NC_LATE, then all points will be deducted. (Whether that will be all
     * of the points in the assignment or all the points in the handin depends
     * on the AFFECT-ALL value.)
     *
     * @param handin
     * @param group
     * @return
     * @throws RubricException
     */
    public double getHandinPenaltyOrBonus(Handin handin, Group group) throws RubricException;

    /**
     * Converts all rubrics for the DistributableParts of the given Handin to
     * a single GRD file for the given group.
     *
     * @param handin
     * @param group
     */
    public void convertToGRD(Handin handin, Group group) throws RubricException;

    /**
     * Converts all rubrics for the DistributableParts of the given Handin to
     * a single GRD file for the given groups.
     *
     * @param handin
     * @param groups
     */
    public void convertToGRD(Handin handin, Iterable<Group> groups) throws RubricException;

    public void convertToGRD(Map<Handin, Iterable<Group>> toConvert) throws RubricException;

    /**
     * Creates a rubric for each DistributablePart of the given Handin for each
     * Group in the given Collection.  First calculates handin statuses and stores
     * them in the database, then creates an appropriately placed copy of the each
     * DistributablePart's rubric template for each student.  If the given overwrite
     * parameter is true, any existing rubrics will be overwritten; if false, they
     * will be preserved.
     *
     * @param handin
     * @param toDistribute
     * @param minsLeniency
     * @param requester
     * @param overwrite
     * @throws RubricException
     */
    public void distributeRubrics(Handin handin, Collection<Group> toDistribute,
                                  int minsLeniency, DistributionRequester requester,
                                  boolean overwrite) throws RubricException;

    
}