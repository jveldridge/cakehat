package gradesystem.rubric;

import gradesystem.views.backend.assignmentdist.DistributionRequester;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.config.GradeUnits;
import gradesystem.config.LatePolicy;
import gradesystem.config.TimeInformation;
import gradesystem.database.Group;
import gradesystem.database.HandinStatus;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import gradesystem.rubric.Rubric.Section;
import gradesystem.rubric.Rubric.Subsection;
import gradesystem.services.ServicesException;
import gradesystem.views.shared.ErrorView;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class RubricManagerImpl implements RubricManager {

    private HashMap<String, GradingVisualizer> _graders = new HashMap<String, GradingVisualizer>();

    @Override
    public void view(DistributablePart part, Group group) {
        view(part, group, false);
    }

    @Override
    public void view(DistributablePart part, Group group, boolean isAdmin) {
        String GMLFilePath = getGroupRubricPath(part, group);

        //Determine if it has been opened
        final String graderViewName = part.getAssignment().getName() + "::" + part.getName() + "::"+ group.getName();
        //If it hasn't been opened
        if(!_graders.containsKey(graderViewName))
        {
            try
            {
                Rubric rubric = RubricGMLParser.parse(GMLFilePath, part, group);
                GradingVisualizer visualizer = new GradingVisualizer(rubric, isAdmin);
                visualizer.addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosed(WindowEvent e)
                    {
                        _graders.remove(graderViewName);
                    }
                });

                _graders.put(graderViewName, visualizer);
            }
            catch (RubricException ex)
            {
                new ErrorView(ex);
            }
        }
        //If it has, bring it to front and center it on screen
        else
        {
            GradingVisualizer visualizer = _graders.get(graderViewName);
            visualizer.toFront();
            visualizer.setLocationRelativeTo(null);
        }

    }

    @Override
    public void viewTemplate(DistributablePart dp) throws RubricException {
        new TemplateVisualizer(RubricGMLParser.parse(dp.getRubricTemplate().getAbsolutePath(), dp, null));
    }

    @Override
    public String getGroupRubricPath(DistributablePart part, Group group) {
        return Allocator.getCourseInfo().getRubricDir() + part.getAssignment().getName() + "/" +
                part.getName() + "/" + group.getName() + ".gml";
    }

    @Override
    public boolean hasRubric(DistributablePart part, Group group) {
        return new File(getGroupRubricPath(part, group)).exists();
    }

    @Override
    public Collection<DistributablePart> getMissingRubrics(Handin handin, Group group) {
        List<DistributablePart> partsMissingRubrics = new LinkedList<DistributablePart>();

        for (DistributablePart dp : handin.getAssignment().getDistributableParts()) {
            if (!this.hasRubric(dp, group)) {
                partsMissingRubrics.add(dp);
            }
        }

        return partsMissingRubrics;
    }

    @Override
    public Map<Group, Double> getPartScores(DistributablePart part, Iterable<Group> groups) {
        HashMap<Group, Double> totals = new HashMap<Group, Double>();

        for(Group group : groups) {
            totals.put(group, getPartScore(part, group));
        }

        return totals;
    }

    @Override
    public double getPartScore(DistributablePart part, Group group)
    {
        try
        {
            Rubric rubric = RubricGMLParser.parse(getGroupRubricPath(part, group), part, group);
            return rubric.getTotalDistPartScore();
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }

        return 0;
    }

    @Override
    public double getHandinPenaltyOrBonus(Handin handin, Group group) throws RubricException {
        Assignment asgn = handin.getAssignment();
        TimeInformation timeInfo = handin.getTimeInformation();

        HandinStatus statusPair;
        try {
            statusPair = Allocator.getDatabaseIO().getHandinStatus(handin, group);
        } catch (SQLException ex) {
            throw new RubricException("Could not get handin status for group " +
                                        group + " on assignment " + asgn + ".", ex);
        }
        TimeStatus status = statusPair.getTimeStatus();

        double outOf = 0;
        double score = 0;

        Map<String, Subsection> sources = new HashMap<String, Subsection>();
        for (DistributablePart dp : asgn.getDistributableParts()) {
            Rubric rubric = RubricGMLParser.parse(getGroupRubricPath(dp, group), dp, group);

            outOf += rubric.getTotalDistPartOutOf();
            score += rubric.getTotalDistPartScore();
            
            for (Section section : rubric.getSections()) {
                for (Subsection subsection : section.getSubsections()) {
                    if (subsection.hasSource() && !sources.containsKey(subsection.getSource())) {
                        sources.put(subsection.getSource(), subsection);
                    }
                }
            }
        }

        //if bonus/deduction applies to entire assignment, add values of sections w/sources
        //each subsection is only added once, even if it appeared on multiple rubrics
        if(timeInfo.affectsAll())
        {
            double sourceOutOf = 0;
            double sourceScore = 0;
            for (Subsection s : sources.values()) {
                sourceOutOf += s.getOutOf();
                sourceScore += s.getScore();
            }

            outOf += sourceOutOf;
            score += sourceScore;
        }

        // If NC Late, negate all points
        if(status == TimeStatus.NC_LATE)
        {
            return -score;
        }

        LatePolicy policy = timeInfo.getLatePolicy();
        GradeUnits units = timeInfo.getGradeUnits();

        if(policy == LatePolicy.DAILY_DEDUCTION)
        {
            if(status == TimeStatus.LATE)
            {
                double dailyDeduction = 0;
                if(units == GradeUnits.PERCENTAGE)
                {
                    double percent = timeInfo.getOntimeValue() / 100.0;
                    dailyDeduction = outOf * percent;
                }
                else if(units == GradeUnits.POINTS)
                {
                    dailyDeduction = timeInfo.getOntimeValue();
                }

                return dailyDeduction * statusPair.getDaysLate();
            }
        }
        else if(policy == LatePolicy.MULTIPLE_DEADLINES)
        {
            // get appropriate value
            double value = 0;
            if(status == TimeStatus.EARLY)
            {
                value = timeInfo.getEarlyValue();
            }
            else if(status == TimeStatus.ON_TIME)
            {
                value = timeInfo.getOntimeValue();
            }
            else if(status == TimeStatus.LATE)
            {
                value = timeInfo.getLateValue();
            }

            // deduction based on grade units
            if(units == GradeUnits.PERCENTAGE)
            {
                return outOf * (value / 100.0);
            }
            else if(units == GradeUnits.POINTS)
            {
                return value;
            }
        }

        return 0;
    }

    @Override
    public void convertToGRD(Handin handin, Group group) throws RubricException {
        HandinStatus status;
        try {
            status = Allocator.getDatabaseIO().getHandinStatus(handin, group);
        } catch (SQLException ex) {
            throw new RubricException("Could not read handin status for group " + group + " " +
                                         "on assignment " + handin.getAssignment() + " from the database.", ex);
        }

        //Write to grd
        String grdPath = Allocator.getGradingServices().getGroupGRDPath(handin, group);
        try {
            RubricGRDWriter.write(handin, group, status, grdPath);
        } catch (RubricException e) {
            //delete the incomplete GRD file, if it was written
            File grdFile = new File(grdPath);
            if (grdFile.exists()) {
                grdFile.delete();
            }

            //display the error to the user
            new ErrorView(e, "The rubric could not be written.  "
                    + "No GRD file has been created.");
        }
    }

    @Override
    public void convertToGRD(Handin handin, Iterable<Group> groups) throws RubricException {
        for (Group group : groups) {
            this.convertToGRD(handin, group);
        }
    }

    @Override
    public void convertToGRD(Map<Handin, Iterable<Group>> toConvert) throws RubricException {
        for (Handin handin : toConvert.keySet()) {
            for (Group group : toConvert.get(handin)) {
                convertToGRD(handin, group);
            }
        }
    }

    @Override
    public void distributeRubrics(Handin handin, Collection<Group> toDistribute,
                                  int minsLeniency, DistributionRequester requester,
                                  boolean overwrite) throws RubricException {
        this.storeHandinStatuses(handin.getAssignment(), toDistribute, minsLeniency);

        Collection<DistributablePart> distParts = handin.getAssignment().getDistributableParts();
        int numToDistribute = toDistribute.size() * distParts.size();
        int numDistributedSoFar = 0;

        try {
            for (DistributablePart part : distParts) {
                File template = part.getRubricTemplate();

                //ensure that appropriate rubric directory exists
                File partRubricDir = new File(Allocator.getCourseInfo().getRubricDir() + part.getAssignment() + "/" + part.getName() + "/");
                if (!partRubricDir.exists()) {
                    Allocator.getFileSystemServices().makeDirectory(partRubricDir);
                }

                for (Group group : toDistribute) {
                    //if rubric already exists and not in overwrite mode, go on
                    //to next Group
                    if (!overwrite && this.hasRubric(part, group)) {
                        continue;
                    }

                    List<File> copiedFiles = Allocator.getFileSystemServices().copy(template, new File(this.getGroupRubricPath(part, group)), true, false);
                    for (File file : copiedFiles) {
                        Allocator.getFileSystemServices().sanitize(file);
                    }

                    numDistributedSoFar++;
                    double fractionDone = (double) numDistributedSoFar / (double) numToDistribute;
                    requester.updatePercentDone((int) (fractionDone * 100));
                }
            }
        } catch (ServicesException ex) {
            throw new RubricException("Could not distribute rubrics for " +
                                      "assignment " + handin.getAssignment() + ".", ex);
        }
    }

    /**
     * Calculates the HandinStatus (consisting of a TimeStatus and a number of days late)
     * for each Group's handin for the given Assignment by calling the GradingServices
     * getHandinStatuses(...) method and stores the result in the database.
     *
     * @param asgn
     * @param groups
     * @param minsLeniency
     * @throws RubricException
     */
    private void storeHandinStatuses(Assignment asgn, Collection<Group> groups, int minsLeniency) throws RubricException {
        Map<Group, Calendar> extensions;
        try {
            extensions = Allocator.getDatabaseIO().getAllExtensions(asgn.getHandin());
        } catch (SQLException ex) {
            throw new RubricException("Could not read extensions for assignment " + asgn + " " +
                                      "from the database.  Rubrics cannot be distributed because handin " +
                                      "status cannot be determined.", ex);
        }

        Map<Group, HandinStatus> handinStatuses;
        try {
            handinStatuses = Allocator.getGradingServices().getHandinStatuses(asgn.getHandin(), groups, extensions, minsLeniency);
        } catch (ServicesException ex) {
            throw new RubricException("Could determine time statuses for handins. " +
                                      "Rubrics cannot be distributed.", ex);
        }
        try {
            Allocator.getDatabaseIO().setHandinStatuses(asgn.getHandin(), handinStatuses);
        } catch (SQLException ex) {
            throw new RubricException("Could not store handin statuses in the database for " +
                                      "assignment " + asgn + ".  Rubrics cannot be distributed.", ex);
        }
    }
    
}