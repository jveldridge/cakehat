package cakehat.gradingsheet;

import cakehat.database.DbGradingSheetDetail;
import cakehat.database.DbGradingSheetSection;
import cakehat.database.DbGradingSheetSubsection;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Builds grading sheet objects from database objects.
 * 
 * @author jeldridg
 */
public class GradingSheetBuilder {

    public GradingSheet buildGradingSheet(Set<DbGradingSheetSection> sections) {
        GradingSheet gradingSheet = new GradingSheet(buildSections(sections));
        
        for (GradingSheetSection section : gradingSheet.getSections()) {
            section.setGradingSheet(gradingSheet);
        }
        
        return gradingSheet;
    }
    
    private ImmutableList<GradingSheetSection> buildSections(Set<DbGradingSheetSection> dbSections) {
        ImmutableList.Builder<GradingSheetSection> sectionsBuilder = ImmutableList.builder();
        
        List<DbGradingSheetSection> sortedDbSections = new ArrayList<DbGradingSheetSection>(dbSections);
        Collections.sort(sortedDbSections);
        for (DbGradingSheetSection dbSection : sortedDbSections) {
            GradingSheetSection section = new GradingSheetSection(dbSection.getId(), dbSection.getName(),
                                                                  dbSection.getOrder(), dbSection.getOutOf(),
                                                                  buildSubsections(dbSection.getSubsections()));
            sectionsBuilder.add(section);
            
            for (GradingSheetSubsection subsection : section.getSubsections()) {
                subsection.setGradingSheetSection(section);
            }
        }
        
        return sectionsBuilder.build();
    }
    
    private ImmutableList<GradingSheetSubsection> buildSubsections(Set<DbGradingSheetSubsection> dbSubsections) {
        ImmutableList.Builder<GradingSheetSubsection> subsectionsBuilder = ImmutableList.builder();
        
        List<DbGradingSheetSubsection> sortedDbSubsections = new ArrayList<DbGradingSheetSubsection>(dbSubsections);
        Collections.sort(sortedDbSubsections);
        for (DbGradingSheetSubsection dbSubsection : sortedDbSubsections) {
            GradingSheetSubsection subsection = new GradingSheetSubsection(dbSubsection.getId(), dbSubsection.getText(),
                                                                           dbSubsection.getOrder(), dbSubsection.getOutOf(),
                                                                           buildDetails(dbSubsection.getDetails()));
            subsectionsBuilder.add(subsection);
            
            for (GradingSheetDetail detail : subsection.getDetails()) {
                detail.setGradingSheetSubsection(subsection);
            }
        }
        
        return subsectionsBuilder.build();
    }

    private ImmutableList<GradingSheetDetail> buildDetails(Set<DbGradingSheetDetail> dbDetails) {
        ImmutableList.Builder<GradingSheetDetail> detailsBuilder = ImmutableList.builder();
        
        List<DbGradingSheetDetail> sortedDbDetails = new ArrayList<DbGradingSheetDetail>(dbDetails);
        Collections.sort(sortedDbDetails);
        for (DbGradingSheetDetail detail : sortedDbDetails) {
            detailsBuilder.add(new GradingSheetDetail(detail.getId(), detail.getText(), detail.getOrder()));
        }
        
        return detailsBuilder.build();
    }
}