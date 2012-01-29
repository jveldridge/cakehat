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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds assignment objects from database objects.
 * 
 * @author jak2
 */
public class AssignmentsBuilder
{   
    /**
     * Builds an immutable list of {@link Assignment}s.
     * 
     * @param dbAssignments
     * @return 
     */
    public ImmutableList<Assignment> buildAssigments(Set<DbAssignment> dbAssignments)
    {
        ImmutableList.Builder<Assignment> assignmentsBuilder = ImmutableList.builder();
        
        List<DbAssignment> sortedDbAssignments = new ArrayList<DbAssignment>(dbAssignments);
        Collections.sort(sortedDbAssignments);
        for(DbAssignment dbAssignment : sortedDbAssignments)
        {
            List<GradableEvent> gradableEvents = buildGradableEvents(dbAssignment.getGradableEvents());
            
            Assignment assignment = new Assignment(
                    dbAssignment.getId(),
                    dbAssignment.getName(),
                    dbAssignment.getOrder(),
                    dbAssignment.hasGroups(),
                    gradableEvents);
            assignmentsBuilder.add(assignment);
            
            for(GradableEvent gradableEvent : gradableEvents)
            {
                gradableEvent.setAssignment(assignment);
            }
        }
        
        return assignmentsBuilder.build();
    }
    
    private ImmutableList<GradableEvent> buildGradableEvents(Set<DbGradableEvent> dbGradableEvents)
    {
        ImmutableList.Builder<GradableEvent> gradableEventsBuilder = ImmutableList.builder();
        
        List<DbGradableEvent> sortedDbGradableEvents = new ArrayList<DbGradableEvent>(dbGradableEvents);
        Collections.sort(sortedDbGradableEvents);
        for(DbGradableEvent dbGradableEvent : sortedDbGradableEvents)
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
    
    private ImmutableList<Part> buildParts(Set<DbPart> dbParts)
    {
        ImmutableList.Builder<Part> partsBuilder = ImmutableList.builder();
        
        List<DbPart> sortedDbParts = new ArrayList<DbPart>(dbParts);
        Collections.sort(sortedDbParts);
        for(DbPart dbPart : sortedDbParts)
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
    
    private FilterProvider buildFilterProvider(Set<DbInclusionFilter> dbFilters)
    {
        FilterProvider provider;
        if(dbFilters.isEmpty())
        {
            provider = new AlwaysAcceptingFilterProvider();
        }
        else
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
            provider = new OrFilterProvider(providers.build());
        }
        
        return provider;
    }
    
    private PartAction buildPartAction(PartActionDescription.ActionType type, DbPartAction dbPartAction)
    {
        PartAction action = null;
        if(dbPartAction != null)
        {
            Map<String, String> properties = new HashMap<String, String>();
            for(DbActionProperty property : dbPartAction.getActionProperties())
            {
                properties.put(property.getKey(), property.getValue());
            }
            
            action = ActionRepository.get().getAction(type, dbPartAction.getName(), properties);
        }
        
        return action;
    }
}