package cakehat.services;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.DeadlineInfo;
import cakehat.assignment.DeadlineInfo.DeadlineResolution;
import cakehat.assignment.DeadlineInfo.Type;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Extension;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.GroupGradingSheet.GroupSectionComments;
import cakehat.database.GroupGradingSheet.GroupSubsectionEarned;
import cakehat.database.Student;
import cakehat.gradingsheet.GradingSheetDetail;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

/**
 *
 * @author jeldridg
 */
public class GrdGeneratorImpl implements GrdGenerator {
    
    @Override
     public Map<Student, String> generateGRD(Assignment asgn, Set<Student> students) throws ServicesException {
        Map<Student, Group> groupsForStudents = new HashMap<Student, Group>();
        for (Student student : students) {
            groupsForStudents.put(student, Allocator.getDataServices().getGroup(asgn, student));
        }

        SetMultimap<Part, Group> toRetrieve = HashMultimap.create();
        for (GradableEvent ge : asgn) {
            for (Part part : ge) {
                toRetrieve.putAll(part, groupsForStudents.values());
            }
        }
        
        Map<Part, Map<Group, GroupGradingSheet>> gradingSheets =
                Allocator.getDataServices().getGroupGradingSheets(toRetrieve);
        
        Map<Student, String> toReturn = new HashMap<Student, String>();
        for (Student student : students) {
            Map<Part, GroupGradingSheet> gradingSheetsForStudent = new HashMap<Part, GroupGradingSheet>();
            for (Part part : toRetrieve.keySet()) {
                gradingSheetsForStudent.put(part, gradingSheets.get(part).get(groupsForStudents.get(student)));
            }
            
            toReturn.put(student, generateGrd(asgn, student, gradingSheetsForStudent));
        }
        
        return toReturn;
    }
    
    private String generateGrd(Assignment asgn, Student student, Map<Part, GroupGradingSheet> gradingSheets) throws ServicesException {
        Group group = Allocator.getDataServices().getGroup(asgn, student) ;

        StringBuilder grdBuilder = new StringBuilder("<table width='600px'><tr><td style='text-align:center'>");
        grdBuilder.append(group.getAssignment().getName()).append(" Grading Sheet");
        grdBuilder.append("</td></tr></table>");
        
        grdBuilder.append("<p><b>Student:</b> ").append(student.getName());
        grdBuilder.append(" (").append(student.getLogin()).append(')');
        if (group.size() > 1) {
            Iterator<Student> members = group.getMembers().iterator();
            grdBuilder.append("<br/><b>Group:</b> ").append(group.getName()).append(" (").append(members.next().getLogin());
            while (members.hasNext()) {
                grdBuilder.append(", ").append(members.next().getLogin());
            }
            grdBuilder.append(')');
        }
        grdBuilder.append("</p>");
        
        double asgnEarned = 0;
        double asgnOutOf = 0;
        
        for (GradableEvent ge : group.getAssignment()){
            Score geScore = generateGradableEventGRD(ge, group, gradingSheets, grdBuilder);
            
            asgnEarned += geScore._earned;
            asgnOutOf += geScore._outOf;
            
            grdBuilder.append("<br/>");
        }

        grdBuilder.append("<table width='600px' style='border: 1px solid; border-spacing: 0px'>");
        writeLineWithEarnedAndOutOf("<b>Total Grade</b>", "Earned", "Out of", grdBuilder, TopLineStyle.NONE);
        writeLineWithEarnedAndOutOf("Total Score:", doubleToString(asgnEarned), doubleToString(asgnOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        grdBuilder.append("</table>");
        
        return grdBuilder.toString();
    }
    
    private  Score generateGradableEventGRD(GradableEvent ge, Group group, Map<Part, GroupGradingSheet> gradingSheets,
                                           StringBuilder grdBuilder) throws ServicesException {
        double geEarned = 0;
        double geOutOf = 0;
        
        grdBuilder.append("<table width='600px' style='border: 1px solid; border-spacing: 0px'>");
        
        grdBuilder.append("<tr><td colspan='3' style='border-bottom: 1px solid'><b>").append(ge.getName()).append("</b></td></tr>");
        for (Part part : ge) {
            Score partScore = generatePartGRD(gradingSheets.get(part), grdBuilder);
            
            geEarned += partScore._earned;
            geOutOf += partScore._outOf;
        }
        
        writeLineWithEarnedAndOutOf("Parts total:", doubleToString(geEarned), doubleToString(geOutOf), grdBuilder,
                                         TopLineStyle.FULL);

        DeadlineInfo info = ge.getDeadlineInfo();
        if (info.getType() != Type.NONE) {
            DeadlineResolution res;
            DateTime occurrenceDate = Allocator.getGradingServices().getOccurrenceDates(ge, ImmutableSet.of(group)).get(group);
            Extension extension = Allocator.getDataServices().getExtensions(ge, ImmutableSet.of(group)).get(group);
            res = info.apply(occurrenceDate, extension); 
            double penalty = res.getPenaltyOrBonus(geEarned);

            writeLineWithEarnedAndOutOf("Deadline resolution: " + res.getTimeStatus().toString(),
                                             doubleToString(penalty), "", grdBuilder, TopLineStyle.FULL);

            geEarned += penalty;
        }
        
        writeLineWithEarnedAndOutOf(ge.getName() + " Score", doubleToString(geEarned), doubleToString(geOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        grdBuilder.append("</table>");
        
        return new Score(geEarned, geOutOf);
    }
    
    private  Score generatePartGRD(GroupGradingSheet groupGradingSheet, StringBuilder grdBuilder) {
        double partEarned = 0;
        double partOutOf = 0;
        
        grdBuilder.append("<tr><td colspan='3' style='border-top: 1px solid; border-bottom: 1px solid'>Part: ").append(groupGradingSheet.getGradingSheet().getPart().getName());
        String grader = groupGradingSheet.getAssignedTo() == null
                ? "Not specified"
                : groupGradingSheet.getAssignedTo().getName() + " (" + groupGradingSheet.getAssignedTo().getLogin() + ")";
        grdBuilder.append("<br/>Grader: ").append(grader).append("</td></tr>");
        
        if (groupGradingSheet.isSubmitted()) {
            for (GradingSheetSection section : groupGradingSheet.getGradingSheet().getSections()) {
                Score sectionScore = generateSectionGRD(section, groupGradingSheet, grdBuilder);

                partEarned += sectionScore._earned;
                partOutOf += sectionScore._outOf;
            }
        }
        else {
            grdBuilder.append("<tr><td colspan='3'>");
            grdBuilder.append("<i>Your grade has not been submitted for this part. Please contact the TAs.</i>");
            grdBuilder.append("</td></tr>");
        }
        
        writeLineWithEarnedAndOutOf("Part total:", doubleToString(partEarned), doubleToString(partOutOf),
                                         grdBuilder, TopLineStyle.FULL);
        
        return new Score(partEarned, partOutOf);
    }
    
    private  Score generateSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                             StringBuilder grdBuilder) {        
        if (section.getOutOf() == null) { //additive grading
            return generateAdditiveSectionGRD(section, gradingSheet, grdBuilder);
        }
        else {                            //subtractive grading
            return generateSubtractiveSectionGRD(section, gradingSheet, grdBuilder);
        }
    }
    
    private  Score generateAdditiveSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                             StringBuilder grdBuilder) {            
        double sectionEarned = 0;
        double sectionOutOf = 0;

        writeLineWithEarnedAndOutOf(section.getName(), "Earned", "Out of", grdBuilder, TopLineStyle.POINTS_ONLY);

        for (GradingSheetSubsection subsection : section.getSubsections()) {
            StringBuilder subsectionBuilder = new StringBuilder(generateSpaces(5));
            subsectionBuilder.append(subsection.getText());

            if (!subsection.getDetails().isEmpty()) {
                subsectionBuilder.append("<ul style='margin: 0px 5px 10px;'>");
                for (GradingSheetDetail detail : subsection.getDetails()) {
                    subsectionBuilder.append("<li>").append(detail.getText()).append("</li>");
                }
                subsectionBuilder.append("</ul>");
            }

            Double earned = null;
            GroupSubsectionEarned earnedRecord = gradingSheet.getEarnedPoints().get(subsection);
            if (earnedRecord != null) {
                earned = earnedRecord.getEarned();
            }

            String earnedString = earned == null ? "--" : doubleToString(earned);
            writeLineWithEarnedAndOutOf(subsectionBuilder.toString(), earnedString,
                                             doubleToString(subsection.getOutOf()), grdBuilder, TopLineStyle.NONE);

            sectionEarned += earned == null ? 0 : earned;
            sectionOutOf += subsection.getOutOf();
        }

        generateCommentsGRD(gradingSheet.getComments().get(section), grdBuilder);

        writeLineWithEarnedAndOutOf("Total&nbsp;&nbsp;", TextAlignment.RIGHT, doubleToString(sectionEarned),
                                         doubleToString(sectionOutOf), grdBuilder, TopLineStyle.NONE);

        return new Score(sectionEarned, sectionOutOf);
    }
    
    private  Score generateSubtractiveSectionGRD(GradingSheetSection section, GroupGradingSheet gradingSheet,
                                              StringBuilder grdBuilder) {
        throw new UnsupportedOperationException("Subtractive grading is not yet supported.");
    }
    
    private  void generateCommentsGRD(GroupSectionComments commentsRecord, StringBuilder grdBuilder) {
        if (commentsRecord != null && commentsRecord.getComments() != null && !commentsRecord.getComments().isEmpty()) {
            StringBuilder comments = new StringBuilder("<br/>Comments:<blockquote>");
            comments.append(commentsRecord.getComments());
            comments.append("</blockquote>");

            writeLineWithEarnedAndOutOf(comments.toString(), "", "", grdBuilder, TopLineStyle.NONE);
        }
    }
    
    private  void writeLineWithEarnedAndOutOf(String text, String earned, String outOf, StringBuilder grdBuilder,
                                             TopLineStyle topLineStyle) {
        writeLineWithEarnedAndOutOf(text, TextAlignment.LEFT, earned, outOf, grdBuilder, topLineStyle);
    }
    
    private  void writeLineWithEarnedAndOutOf(String text, TextAlignment alignment, String earned, String outOf,
                                             StringBuilder grdBuilder, TopLineStyle topLineStyle) {
        String textTd = "", pointsTd = "";
        String alignStyle = alignment == TextAlignment.RIGHT ? "text-align: right" : "";
        
        if (topLineStyle == TopLineStyle.NONE) {
            textTd = String.format("<td style='%s'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid;'>";
        }
        else if (topLineStyle == topLineStyle.POINTS_ONLY) {
            textTd = String.format("<td style='%s'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid; border-top: 1px solid'>";
        }
        else if (topLineStyle == topLineStyle.FULL) {
            textTd = String.format("<td style='%s; border-top: 1px solid'>", alignStyle);
            pointsTd = "<td style='text-align: center; vertical-align: top; width: 60px; border-left: 1px solid; border-top: 1px solid'>";
        }
        else {
            throw new IllegalArgumentException("Invalid TopLineStyle given.");
        }

        grdBuilder.append("<tr>").append(textTd).append(text).append("</td>");
        grdBuilder.append(pointsTd).append(earned).append("</td>");
        grdBuilder.append(pointsTd).append(outOf).append("</td></tr>");
    }
    
    private  enum TextAlignment {
        LEFT, RIGHT;
    }
    
    private  enum TopLineStyle {
        NONE, FULL, POINTS_ONLY;
    }
    
    private  String generateSpaces(int numSpaces) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numSpaces; i++) {
            builder.append("&nbsp;");
        }
        
        return builder.toString();
    }

    private  class Score {
        private double _earned;
        private double _outOf;

        public Score(double earned, double outOf) {
            _earned = earned;
            _outOf = outOf;
        }   
    }

    private  String doubleToString(double value) {
        double roundedVal;
        
        if(Double.isNaN(value)) {
            roundedVal = Double.NaN;
        }
        else {
            BigDecimal bd = new BigDecimal(Double.toString(value));
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            roundedVal = bd.doubleValue();
        }

        return Double.toString(roundedVal);
    }
    
}
