package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.views.shared.TextViewerView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import support.ui.ModalDialog;

/**
 * A special non-user-configurable action that views readme's contained in a group's digital handin. The special
 * aspect of this part action is that it determines whether a given group's digital handin contains readme files
 * without unarchiving their digital handin.
 *
 * @author jak2
 */
class ReadmeAction implements PartAction
{
    /**
     * A file filter that accepts files which begin with "readme" (case insensitive) and do not end with ~.
     */
    static final FileFilter README_FILTER = new FileFilter()
    {
        @Override
        public boolean accept(File file)
        {
            return file.getName().toUpperCase().startsWith("README") && !file.getName().endsWith("~");
        }
    };
    
    /**
     * Keeps track of which groups have readmes so this only needs to be determined once. If a group is not in the map
     * then it has not yet been determined if the group has a readme or not.
     */
    private final Map<Group, Boolean> _groupsWithReadme = new HashMap<Group, Boolean>();
    
    @Override
    public ActionResult performAction(ActionContext context, Part part, Set<Group> groups) throws ActionException
    {
        for(Group group : groups)
        {
            Set<File> readmes;
            try
            {
                readmes = Allocator.getFileSystemUtilities().getFiles(context.getUnarchiveHandinDir(group),
                        README_FILTER);
            }
            catch(IOException e)
            {
                throw new ActionException("Unable to access READMEs\n" +
                        "Part: " + part.getFullDisplayName() + "\n" +
                        "Group: " + group, e);
            }

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
                        File unarchiveDir = context.getUnarchiveHandinDir(group);
                        Allocator.getExternalProcessesUtilities().executeAsynchronously("evince '" +
                                readme.getAbsolutePath() + "'", unarchiveDir);
                    }
                    catch(IOException e)
                    {
                        throw new ActionException("Unable to open readme in evince: " + readme.getAbsolutePath(), e);
                    }
                }
                //Otherwise, the type is not supported, inform the grader
                else
                {
                    ModalDialog.showMessage(context.getGraphicalOwner(), "Cannot open README",
                            group + "'s README cannot be opened by cakehat\n\n" +
                            readme.getAbsolutePath());
                }
            }
        }

        return ActionResult.NO_CHANGES;
    }

    @Override
    public boolean isActionSupported(Part part, Set<Group> groups) throws ActionException
    {
        //Determine if any group has a readme
        boolean anyGroupHasReadme = false;
        for(Group group : groups)
        {
            //Retrieve the cached determination if the group has a readme
            Boolean hasReadme = _groupsWithReadme.get(group);
            
            //If there is no cached determination if the group has a readme
            if(hasReadme == null)
            {
                try
                {
                    File handin = part.getGradableEvent().getDigitalHandin(group);
                    
                    hasReadme = false;

                    //Because the handins are cached, check it still exists
                    if(handin != null && !handin.exists())
                    {
                        try
                        {
                            //Get contents of archive
                            Collection<ArchiveEntry> contents = Allocator.getArchiveUtilities()
                                    .getArchiveContents(handin);

                            //For each entry (file and directory) in the handin
                            for(ArchiveEntry entry : contents)
                            {
                                String path = entry.getName();

                                //Extract the file name
                                String filename = path.substring(path.lastIndexOf("/") + 1);

                                //See if the file name begins with README, regardless of case and doesn't end with ~
                                if(!entry.isDirectory() &&
                                   filename.toUpperCase().startsWith("README") &&
                                   !filename.endsWith("~"))
                                {
                                    hasReadme = true;
                                    break;
                                }
                            }
                        }
                        catch(ArchiveException e)
                        {
                            throw new ActionException("Cannot determine if a readme exists; unable to get handin " +
                                    "contents\n" +
                                    "Part: " + part.getFullDisplayName() + "\n" +
                                    "Group: " + group + "\n" +
                                    "Path to handin: " + handin.getAbsolutePath(), e);
                        }
                    }

                    _groupsWithReadme.put(group, hasReadme);
                }
                catch(IOException e)
                {
                    throw new ActionException("Unable to access digital handin\n" +
                            "Part: " + part.getFullDisplayName() + "\n" +
                            "Group: " + group, e);
                }
            }

            //If this group has a readme, then at least one group has a readme and break out of the loop
            if(hasReadme)
            {
                anyGroupHasReadme = true;
                break;
            }
        }
        
        return anyGroupHasReadme;
    }
}