package cakehat.database.assignment;

import cakehat.database.DbAction;
import cakehat.database.DbActionProperty;
import cakehat.database.DbAssignment;
import cakehat.database.DbGradableEvent;
import cakehat.database.DbInclusionFilter;
import cakehat.database.DbInclusionFilter.FilterType;
import cakehat.database.DbPart;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
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
            List<Action> actions = buildActions(dbPart.getActions());
            
            Part part = new Part(
                    dbPart.getId(),
                    dbPart.getName(),
                    dbPart.getOrder(),
                    dbPart.getOutOf(),
                    dbPart.getGmlTemplate(),
                    dbPart.getQuickName(),
                    buildFilterProvider(dbPart.getInclusionFilters()),
                    actions);
            partsBuilder.add(part);
            
            for(Action action : actions)
            {
                action.setPart(part);
            }
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
    
    private ImmutableList<Action> buildActions(Set<DbAction> dbActions)
    {
        ImmutableList.Builder<Action> actionsBuilder = ImmutableList.builder();
        
        List<DbAction> sortedDbActions = new ArrayList<DbAction>(dbActions);
        Collections.sort(sortedDbActions);
        for(DbAction dbAction : sortedDbActions)
        {   
            Task task = TaskRepository.getTask(dbAction.getTask());
            
            Action action = new Action(
                    dbAction.getId(),
                    dbAction.getName(),
                    dbAction.getIcon(),
                    dbAction.getOrder(),
                    task,
                    buildActionProperties(task, dbAction.getActionProperties()));
            actionsBuilder.add(action);
        }
        
        return actionsBuilder.build();
    }
    
    private Map<TaskProperty, String> buildActionProperties(Task task, Set<DbActionProperty> dbActionProperties)
    {
        ImmutableMap.Builder<TaskProperty, String> convertedProperties = ImmutableMap.builder();
        for(TaskProperty actualProperty : task.getProperties())
        {
            for(DbActionProperty dbActionProperty : dbActionProperties)
            {
                if(actualProperty.getName().equals(dbActionProperty.getKey()))
                {
                    convertedProperties.put(actualProperty, dbActionProperty.getValue());
                }
            }
        }

        return convertedProperties.build();
    }
}