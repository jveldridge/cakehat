package gradesystem.handin;

import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.views.shared.ErrorView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Actions that rely upon commands external to cakehat.
 *
 * @author jak2
 */
class ExternalActions implements ActionProvider
{
    public String getNamespace()
    {
        return "external";
    }

    public List<DistributableActionDescription> getActionDescriptions()
    {
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();
        descriptions.add(new Terminal());

        return descriptions;
    }

    //TODO: Action that executes a config-specified command with many special
    //substrings that get replaced such as ^group^

    private class Terminal implements DistributableActionDescription
    {
        public ActionProvider getProvider()
        {
            return ExternalActions.this;
        }

        public String getName()
        {
            return "terminal";
        }

        public String getDescription()
        {
            return "Opens a terminal that is in the root directory of the " +
                    "unarchived handin";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return new ArrayList<DistributableActionProperty>();
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN, ActionMode.OPEN });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST });
        }

        public DistributableAction getAction(Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                    String terminalName = group.getName() + "'s " + part.getAssignment().getName();
                    try
                    {
                        Allocator.getExternalProcessesUtilities()
                                .executeInVisibleTerminal(terminalName, "cd " + unarchiveDir.getAbsolutePath());
                    }
                    catch(IOException e)
                    {
                        new ErrorView(e, "Unable to open terminal for " + group.getName());
                    }
                }
            };

            return action;
        }
    }
}