package cakehat.database.assignment;

import cakehat.database.assignment.PartActionDescription.ActionType;
import cakehat.database.DbActionProperty;
import cakehat.database.DbAssignment;
import cakehat.database.DbGradableEvent;
import cakehat.database.DbInclusionFilter;
import cakehat.database.DbInclusionFilter.FilterType;
import cakehat.database.DbPart;
import cakehat.database.DbPartAction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
            List<Part> parts = buildParts(dbGradableEvent.getParts(), dbGradableEvent.getDirectory() != null);
            
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
    
    private ImmutableList<Part> buildParts(Set<DbPart> dbParts, boolean hasDigitalHandinDirectory)
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
                    buildFilterProvider(dbPart.getInclusionFilters()),
                    buildPartActions(dbPart, hasDigitalHandinDirectory));
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
    
    private Map<ActionType, PartAction> buildPartActions(DbPart dbPart, boolean hasDigitalHandinDirectory)
    {
        ImmutableMap.Builder<ActionType, PartAction> actionsBuilder = ImmutableMap.builder();
        
        for(ActionType type : ActionType.values())
        {
            //README is treated specially
            if(type == ActionType.README)
            {
                if(hasDigitalHandinDirectory)
                {
                    actionsBuilder.put(ActionType.README, new ReadmeAction());
                }
            }
            else
            {
                DbPartAction dbPartAction = dbPart.getAction(type);
                if(dbPartAction != null)
                {
                    Map<String, String> properties = new HashMap<String, String>();
                    for(DbActionProperty property : dbPartAction.getActionProperties())
                    {
                        properties.put(property.getKey(), property.getValue());
                    }

                    PartAction action = ActionRepository.get().getAction(type, dbPartAction.getName(), properties);
                    actionsBuilder.put(type, action);
                }
            }
        }
        
        return actionsBuilder.build();
    }
}