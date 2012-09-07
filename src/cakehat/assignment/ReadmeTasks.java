package cakehat.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.views.shared.TextViewerView;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import support.ui.ModalDialog;

/**
 *
 * @author jak2
 */
class ReadmeTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "readme";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new MultiFormat());
    }
    
    private class MultiFormat extends MultiGroupTask
    {
        private MultiFormat()
        {
            super(ReadmeTasks.this, "multiformat");
        }

        @Override
        public String getDescription()
        {
            return "Opens plain text or PDF files in the handin that starts with 'README' (case insensitive).";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.<TaskProperty>of();
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.of(ActionDescription.README);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
                throws TaskException
        {
            Set<Group> groupsWithoutReadmes = new HashSet<Group>();
            Set<Group> groupsWithUnsupportedReadmes = new HashSet<Group>();

            for(Group group : groups)
            {
                Set<File> readmes;
                try
                {
                    FileFilter readmeFilter = new FileFilter()
                    {
                        @Override
                        public boolean accept(File file)
                        {
                            return file.getName().toUpperCase().startsWith("README") &&
                                   !file.getName().endsWith("~");
                        }
                    };

                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
                    readmes = Allocator.getFileSystemUtilities().getFiles(unarchiveDir, readmeFilter);
                }
                catch(IOException e)
                {
                    throw new TaskException("Unable to access READMEs\n" +
                            "Part: " + action.getPart().getFullDisplayName() + "\n" +
                            "Group: " + group, e);
                }

                if(readmes.isEmpty())
                {
                    groupsWithoutReadmes.add(group);
                }
                else
                {
                    //For each readme
                    for(File readme : readmes)
                    {
                        String name = readme.getName().toLowerCase();

                        //If a text file
                        if(!name.contains(".") || name.endsWith(".txt"))
                        {
                            new TextViewerView(context.getGraphicalOwner(), readme, group.getName() +"'s Readme");
                        }
                        //If a PDF
                        else if(readme.getAbsolutePath().toLowerCase().endsWith(".pdf"))
                        {
                            try
                            {
                                File unarchiveDir = Allocator.getPathServices()
                                        .getUnarchiveHandinDir(action.getPart(), group);
                                Allocator.getExternalProcessesUtilities().executeAsynchronously("evince '" +
                                        readme.getAbsolutePath() + "'", unarchiveDir);
                            }
                            catch(IOException e)
                            {
                                throw new TaskException("Unable to open readme in evince: " + readme.getAbsolutePath(),
                                        e);
                            }
                        }
                        //Otherwise, the type is not supported, inform the grader
                        else
                        {
                            groupsWithUnsupportedReadmes.add(group);
                        }
                    }
                }
            }

            //Show notification as needed
            if(!groupsWithoutReadmes.isEmpty() && !groupsWithUnsupportedReadmes.isEmpty())
            {
                String groupsOrStudents = action.getPart().getAssignment().hasGroups() ? "Groups" : "Students";

                StringBuilder messageBuilder = new StringBuilder("<html>");

                if(!groupsWithoutReadmes.isEmpty())
                {
                    messageBuilder.append("<h1>");
                    messageBuilder.append(groupsOrStudents);
                    messageBuilder.append(" without READMEs:");
                    messageBuilder.append("</h1>");

                    messageBuilder.append("<ul>");
                    for(Group group : groupsWithoutReadmes)
                    {
                        messageBuilder.append("<li>");
                        messageBuilder.append(group.getName());
                        messageBuilder.append("</li>");
                    }
                    messageBuilder.append("</ul>");
                }

                if(!groupsWithUnsupportedReadmes.isEmpty())
                {
                    messageBuilder.append("<h1>");
                    messageBuilder.append(groupsOrStudents);
                    messageBuilder.append(" with READMEs in unsupported formats:");
                    messageBuilder.append("</h1>");

                    messageBuilder.append("<ul>");
                    for(Group group : groupsWithUnsupportedReadmes)
                    {
                        messageBuilder.append("<li>");
                        messageBuilder.append(group.getName());
                        messageBuilder.append("</li>");
                    }
                    messageBuilder.append("</ul>");
                }

                messageBuilder.append("</html>");

                ModalDialog.showMessage(context.getGraphicalOwner(), "READMEs Not Shown", messageBuilder.toString());
            }
        }
    }
}