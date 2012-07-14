package cakehat.database.assignment;

import cakehat.views.shared.TextViewerView;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Map;
import java.util.Set;
import support.ui.ModalDialog;

/**
 * Actions for displaying grading guides.
 *
 * @author jak2
 */
public class GradingGuideActions implements ActionProvider
{
    @Override
    public String getNamespace()
    {
        return "grading-guide";
    }

    @Override
    public Set<? extends PartActionDescription> getActionDescriptions()
    {
        return ImmutableSet.of(new TextFile());
    }
    
    private class TextFile extends PartActionDescription
    {
        private final PartActionProperty FILE_PATH_PROPERTY =
            new PartActionProperty("file-path",
            "The absolute path to a plain text file that will be displayed to a TA to assist in grading the part",
            true);

        private TextFile()
        {
            super(GradingGuideActions.this, "text-file");
        }

        @Override
        public String getDescription()
        {
            return "Opens a plain text file in a non-editable view";
        }

        @Override
        public Set<PartActionProperty> getProperties()
        {
            return ImmutableSet.of(FILE_PATH_PROPERTY);
        }

        @Override
        public Set<ActionType> getSuggestedTypes()
        {
            return ImmutableSet.of(ActionType.GRADING_GUIDE);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }

        @Override
        public PartAction getAction(final Map<PartActionProperty, String> properties)
        {
            
            PartAction action = new NoGroupPartAction()
            {
                @Override
                public ActionResult performAction(ActionContext context, Part part)
                        throws ActionException
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
                    
                    
                    return ActionResult.NO_CHANGES;
                }
            };

            return action;
        }
    }
}