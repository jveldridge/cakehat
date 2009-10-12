
package frontend.tester;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.swing.JFrame;
import utils.BashConsole;
import utils.Constants;
import utils.Project;

/**
 *
 * @author spoletto
 */
public class TesterImpl {

    public TesterImpl(String asgnName, String studentAcct)
    {
            //Get tester.java file
            final String TesterFilePath = Constants.TESTER_DIR + asgnName + "/tester.java";

            Project prj = Project.getInstance(asgnName);
            String StudentCodeDir = utils.ProjectManager.getStudentSpecificDirectory(prj, studentAcct) + asgnName;
            String StudentTesterPath = StudentCodeDir + "/tester.java";

            try {
                copyFile(new File(TesterFilePath), new File(StudentTesterPath));
            } catch (IOException e) {
                throw new Error("Error copying tester to student code directory");
            }

            utils.ProjectManager.compile(prj, studentAcct);
            executeTester(prj, studentAcct);

            new TesterGUI(asgnName, studentAcct);

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException
     {
         if(!destFile.exists())
         {
            destFile.createNewFile();
         }

         FileChannel source = null;
         FileChannel destination = null;
         try
         {
            destination = new FileOutputStream(destFile).getChannel();
            source = new FileInputStream(sourceFile).getChannel();
            destination.transferFrom(source, 0, source.size());
         }
         finally
         {
             if(source != null)
             {
                source.close();
             }
             if(destination != null)
             {
                destination.close();
             }
        }

    }

    public static void executeTester(Project prj, String studentLogin) {
        String compileDir = utils.ProjectManager.getStudentSpecificDirectory(prj, studentLogin);

        String classPath = compileDir + ":" + utils.Utils.getClassPath();

        classPath = classPath.replace("/home/"+ utils.Utils.getUserLogin() + "/course/cs015", "");

        String cmd = "java -classpath " + classPath + " " + prj.getName() + ".Tester";

        try {
            BashConsole.write(cmd);
        }
        catch(Exception e)
        {
            throw new Error("Failed to run tester.");
        }
    }

    private class TesterGUI extends JFrame
    {
        private TesterGUI(String asgnName, String studentAcct)
        {
            super("Testing " + studentAcct + "'s " + asgnName);
            this.setVisible(false);
            this.setLayout(new BorderLayout());

            TestResults toDisplay = TesterManager.readXML(asgnName, studentAcct);
            TesterPanel mp = new TesterPanel(toDisplay);
            this.add(mp, BorderLayout.CENTER);

            this.pack();

            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            //Open up a dialog on window close to save rubric data
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    TesterGUI.this.dispose();
                }
            });
        }

    }

}
