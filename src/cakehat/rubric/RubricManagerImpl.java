package cakehat.rubric;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import cakehat.Allocator;
import cakehat.config.Assignment;
import cakehat.config.GradeUnits;
import cakehat.config.LatePolicy;
import cakehat.config.TimeInformation;
import cakehat.database.Group;
import cakehat.database.HandinStatus;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.rubric.Rubric.Section;
import cakehat.rubric.Rubric.Subsection;
import cakehat.services.ServicesException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

@Deprecated
public class RubricManagerImpl implements RubricManager {

    /**
     * Keeps track of the currently opened rubrics so that if a user attempts
     * to open the same rubric twice the already open one will be brought
     * to the front and centered.
     * Key: a String that is unique for a given DistributablePart and Group
     * Value: the open rubric visualization
     */
    private HashMap<String, GradingVisualizer> _openRubrics = new HashMap<String, GradingVisualizer>();

    @Override
    public void view(DistributablePart part, Group group, boolean isAdmin) throws RubricException
    {
        this.view(part, group, isAdmin, null);
    }

    @Override
    public void view(DistributablePart part, Group group, boolean isAdmin, RubricSaveListener listener) throws RubricException
    {
        //Determine if it has been opened
        final String uniqueID = part.getDBID() + "::" + group.getName();
        
        //If it hasn't been opened
        if(!_openRubrics.containsKey(uniqueID))
        {
            File gmlFile = Allocator.getPathServices().getGroupGMLFile(part, group);
            Rubric rubric = RubricGMLParser.parse(gmlFile, part, group);
            GradingVisualizer visualizer = new GradingVisualizer(rubric, isAdmin);

            if(listener != null)
            {
                visualizer.addSaveListener(listener);
            }

            visualizer.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosed(WindowEvent e)
                {
                    _openRubrics.remove(uniqueID);
                }
            });

            _openRubrics.put(uniqueID, visualizer);
        }
        //If it has, bring it to front and center it on screen
        else
        {
            GradingVisualizer visualizer = _openRubrics.get(uniqueID);
            visualizer.toFront();
            visualizer.setLocationRelativeTo(null);
        }
    }

    @Override
    public void viewTemplate(DistributablePart dp) throws RubricException {
        new TemplateVisualizer(RubricGMLParser.parse(dp.getRubricTemplate(), dp, null));
    }

    @Override
    public boolean hasRubric(DistributablePart part, Group group)
    {
        return Allocator.getPathServices().getGroupGMLFile(part, group).exists();
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
    public Map<Group, Double> getPartScores(DistributablePart part, Iterable<Group> groups) throws RubricException {
        HashMap<Group, Double> totals = new HashMap<Group, Double>();

        for(Group group : groups) {
            totals.put(group, getPartScore(part, group));
        }
        return totals;
    }

    @Override
    public double getPartScore(DistributablePart part, Group group) throws RubricException
    {
        Rubric rubric = RubricGMLParser.parse(Allocator.getPathServices().getGroupGMLFile(part, group), part, group);
        return rubric.getTotalDistPartScore();
    }

    @Override
    public double getHandinPenaltyOrBonus(Handin handin, Group group) throws RubricException {
        Assignment asgn = handin.getAssignment();
        TimeInformation timeInfo = handin.getTimeInformation();

        HandinStatus status;
        try {
            status = Allocator.getDataServices().getHandinStatus(group);
        } catch (ServicesException ex) {
            throw new RubricException("Could not get handin status for group " +
                                        group + " on assignment " + asgn + ".", ex);
        }
        if (status == null) {
            return 0;
        }
        TimeStatus timeStatus = status.getTimeStatus();

        double outOf = 0;
        double score = 0;

        Map<String, Subsection> sources = new HashMap<String, Subsection>();
        for (DistributablePart dp : asgn.getDistributableParts()) {
            File gmlFile = Allocator.getPathServices().getGroupGMLFile(dp, group);
            Rubric rubric = RubricGMLParser.parse(gmlFile, dp, group);

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
        if(timeStatus == TimeStatus.NC_LATE)
        {
            return -score;
        }

        LatePolicy policy = timeInfo.getLatePolicy();
        GradeUnits units = timeInfo.getGradeUnits();

        if(policy == LatePolicy.DAILY_DEDUCTION)
        {
            if(timeStatus == TimeStatus.LATE)
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

                return dailyDeduction * status.getDaysLate();
            }
        }
        else if(policy == LatePolicy.MULTIPLE_DEADLINES)
        {
            // get appropriate value
            double value = 0;
            if(timeStatus == TimeStatus.EARLY)
            {
                value = timeInfo.getEarlyValue();
            }
            else if(timeStatus == TimeStatus.ON_TIME)
            {
                value = timeInfo.getOntimeValue();
            }
            else if(timeStatus == TimeStatus.LATE)
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
            status = Allocator.getDataServices().getHandinStatus(group);
        } catch (ServicesException ex) {
            throw new RubricException("Could not read handin status for group " + group + " " +
                                         "on assignment " + handin.getAssignment() + " from the database.", ex);
        }

        //Write to grd
        File grdFile = Allocator.getPathServices().getGroupGRDFile(handin, group);
        try {
            RubricGRDWriter.write(handin, group, status, grdFile);
        } catch (RubricException e) {
            //delete the incomplete GRD file, if it was written
            if (grdFile.exists()) {
                grdFile.delete();
            }

            throw new RubricException("The rubric could not be written.  "
                    + "No GRD file has been created.", e);
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
    public boolean distributeRubrics(Handin handin, Collection<Group> toDistribute,
                                  DistributionRequester requester, OverwriteMode overwrite) throws RubricException {
        List<DistributablePart> distParts = handin.getAssignment().getDistributableParts();
        int numToDistribute = toDistribute.size() * distParts.size();
        int numDistributedSoFar = 0;
        
        for (int i = 0; i < distParts.size(); i++) {
                DistributablePart part = distParts.get(i);
                File template = part.getRubricTemplate();
                if (template == null || !template.isFile() || !template.exists()) {
                    StringBuilder errMsg = new StringBuilder();
                    errMsg.append("Distributable part ").append(part).append(" does not have a rubric template, ");
                    errMsg.append("or the given template does not exist.\n\n");
                    errMsg.append("Rubrics cannot be distributed for the following distributable parts:\n");
                    for (int j = i; j < distParts.size(); j++) {
                        errMsg.append("\t").append(distParts.get(j)).append("\n");
                    }
                    
                    JOptionPane.showMessageDialog(null, errMsg, "Configuration Error", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
        }

        try {
            for (DistributablePart part : distParts) {
                File template = part.getRubricTemplate();

                for (Group group : toDistribute) {
                    File gmlFile = Allocator.getPathServices().getGroupGMLFile(part, group);
                    Allocator.getFileSystemServices().copy(template, gmlFile,
                            overwrite, false, FileCopyPermissions.READ_WRITE);

                    numDistributedSoFar++;
                    double fractionDone = (double) numDistributedSoFar / (double) numToDistribute;
                    requester.updatePercentDone((int) (fractionDone * 100));
                }
            }
            
            return true;
        } catch (FileCopyingException ex) {
            throw new RubricException("Could not distribute rubrics for " +
                                      "assignment " + handin.getAssignment() + ".", ex);
        }
    }

    @Override
    public boolean areRubricsDistributed(Handin handin) throws RubricException {
        Collection<Group> groups;
        try {
            groups = Allocator.getDataServices().getGroups(handin.getAssignment());
        } catch (ServicesException ex) {
            throw new RubricException("Could not determine if rubrics have been distributed " +
                                      "for assignment " + handin.getAssignment() + ".", ex);
        }
        for (Group group : groups) {
            for (DistributablePart dp : handin.getAssignment().getDistributableParts()) {
                if (this.hasRubric(dp, group)) {
                    return true;
                }
            }
        }

        return false;
    }
}