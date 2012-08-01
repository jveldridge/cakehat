package cakehat.database.assignment;

import cakehat.views.shared.TextViewerView;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Map;
import java.util.Set;
import support.ui.ModalDialog;

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
            "The absolute path to a plain text file that will be displayed to a TA to assist in grading the part",
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
        TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part)
                throws TaskException
        {
            File gradingGuide = new File(properties.get(FILE_PATH_PROPERTY));
            
            if(gradingGuide.exists() && gradingGuide.canRead())
            {
                new TextViewerView(context.getGraphicalOwner(), gradingGuide,
                    part.getFullDisplayName() + " Grading Guide");
            }
            else
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Grading Guide Unavailable",
                        "The specified grading guide does not exist or cannot be read\n" +
                        "File: " + gradingGuide.getAbsolutePath());
            }


            return TaskResult.NO_CHANGES;
        }
    }
}