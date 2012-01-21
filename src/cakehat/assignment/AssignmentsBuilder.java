package cakehat.assignment;

import cakehat.assignment.PartActionDescription.ActionType;
import cakehat.newdatabase.DbActionProperty;
import cakehat.newdatabase.DbAssignment;
import cakehat.newdatabase.DbGradableEvent;
import cakehat.newdatabase.DbInclusionFilter;
import cakehat.newdatabase.DbInclusionFilter.FilterType;
import cakehat.newdatabase.DbPart;
import cakehat.newdatabase.DbPartAction;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds assignment objects from database objects.
 * 
 * @author jak2
 */
class AssignmentsBuilder
{   
    /**
     * Builds an immutable list of {@link Assignment}s.
     * 
     * @param dbAssignments
     * @return 
     */
    ImmutableList<Assignment> buildAssigments(Iterable<DbAssignment> dbAssignments)
    {
        ImmutableList.Builder<Assignment> assignmentsBuilder = ImmutableList.builder();
        
        for(DbAssignment dbAssignment : dbAssignments)
        {
            List<GradableEvent> gradableEvents = buildGradableEvents(dbAssignment.getGradableEvents());
            
            Assignment assignment = new Assignment(
                    dbAssignment.getId(),
                    dbAssignment.getName(),
                    dbAssignment.getOrder(),
                    dbAssignment.getHasGroups(),
                    gradableEvents);
            assignmentsBuilder.add(assignment);
            
            for(GradableEvent gradableEvent : gradableEvents)
            {
                gradableEvent.setAssignment(assignment);
            }
        }
        
        return assignmentsBuilder.build();
    }
    
    private ImmutableList<GradableEvent> buildGradableEvents(Iterable<DbGradableEvent> dbGradableEvents)
    {
        ImmutableList.Builder<GradableEvent> gradableEventsBuilder = ImmutableList.builder();
        
        for(DbGradableEvent dbGradableEvent : dbGradableEvents)
        {
            List<Part> parts = buildParts(dbGradableEvent.getParts());
            
            GradableEvent gradableEvent = new GradableEvent(
                    dbGradableEvent.getId(),
                    dbGradableEvent.getName(),
                    dbGradableEvent.getOrder(),
                    dbGradableEvent.getDirectory(),
                    parts);
            gradableEventsBuilder.add(gradableEvent);
            
            for(Part part : parts)
            {
                part.setGradableEvent(gradableEvent);
            }
        }
        
        return gradableEventsBuilder.build();
    }
    
    private ImmutableList<Part> buildParts(Iterable<DbPart> dbParts)
    {
        ImmutableList.Builder<Part> partsBuilder = ImmutableList.builder();
        
        for(DbPart dbPart : dbParts)
        {
            Part part = new Part(
                    dbPart.getId(),
                    dbPart.getName(),
                    dbPart.getOrder(),
                    dbPart.getOutOf(),
                    dbPart.getGmlTemplate(),
                    dbPart.getQuickName(),
                    dbPart.getGradingGuide(),
                    buildFilterProvider(dbPart.getInclusionFilters()),
                    buildPartAction(ActionType.RUN, dbPart.getAction(ActionType.RUN)),
                    buildPartAction(ActionType.DEMO, dbPart.getAction(ActionType.DEMO)),
                    buildPartAction(ActionType.TEST, dbPart.getAction(ActionType.TEST)),
                    buildPartAction(ActionType.OPEN, dbPart.getAction(ActionType.OPEN)),
                    buildPartAction(ActionType.PRINT, dbPart.getAction(ActionType.PRINT)));
            partsBuilder.add(part);
        }
        
        return partsBuilder.build();
    }
    
    private FilterProvider buildFilterProvider(Iterable<DbInclusionFilter> dbFilters)
    {
        ImmutableList.Builder<FilterProvider> providers = ImmutableList.builder();
        for(DbInclusionFilter dbFilter : dbFilters)
        {
            if(dbFilter.getType() == FilterType.DIRECTORY)
            {
                providers.add(new DirectoryFilterProvider(dbFilter.getPath()));
            }
            else if(dbFilter.getType() == FilterType.FILE)
            {
                providers.add(new FileFilterProvider(dbFilter.getPath()));
            }
        }
        
        return new OrFilterProvider(providers.build());
    }
    
    private PartAction buildPartAction(PartActionDescription.ActionType type, DbPartAction dbPartAction)
    {
        Map<String, String> properties = new HashMap<String, String>();
        for(DbActionProperty property : dbPartAction.getActionProperties())
        {
            properties.put(property.getKey(), property.getValue());
        }
        
        return ActionRepository.get().getAction(type, dbPartAction.getName(), properties);
    }
}