package cakehat.database.assignment;

import cakehat.views.shared.TextViewerView;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Tasks for displaying grading guides.
 *
 * @author jak2
 */
public class GradingGuideTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "grading-guide";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new TextFile());
    }
    
    private class TextFile extends NoGroupTask
    {
        private final TaskProperty FILE_PATH_PROPERTY =
            new TaskProperty("file-path",
            "The absolute path to a plain text file that will be displayed to a TA to assist in grading the part.",
            true);

        private TextFile()
        {
            super(GradingGuideTasks.this, "text-file");
        }

        @Override
        public String getDescription()
        {
            return "Opens a plain text file in a non-editable view";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(FILE_PATH_PROPERTY);
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.GRADING_GUIDE);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }
        
        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action) throws TaskException,
            TaskConfigurationIssue
        {
            File gradingGuide = new File(properties.get(FILE_PATH_PROPERTY));
            
            if(!gradingGuide.exists() || !gradingGuide.canRead())
            {
                throw new TaskConfigurationIssue("The specified grading guide does not exist or cannot be read\n" +
                        "File: " + gradingGuide.getAbsolutePath());
            }
            
            new TextViewerView(context.getGraphicalOwner(), gradingGuide, action.getPart().getFullDisplayName() +
                    " Grading Guide");
        }
    }
}