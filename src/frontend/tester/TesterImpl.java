
package frontend.tester;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.swing.JFrame;
import utils.Allocator;
import utils.Project;
import utils.CS015Project;

/**
 *
 * @author spoletto
 */
public class TesterImpl extends JFrame {

    public TesterImpl(String asgnName, String studentAcct)
    {
            //Get tester.java file
            final String TesterFilePath = Allocator.getConstants().getTesterDir() + asgnName + "/Tester.java";

            String testerName = TesterUtils.getTesterName(asgnName);
            Project prj = Allocator.getProject(asgnName);
            String StudentCodeDir = prj.getStudentCodeDirectory(studentAcct) + testerName;
            String StudentTesterPath = StudentCodeDir + "/Tester.java";
            try {
                copyFile(new File(TesterFilePath), new File(StudentTesterPath));
            } catch (IOException e) {
                throw new Error("Error copying tester to student code directory");
            }

            //TODO: Make this tester generic so that it can work for any course / any language
            ((CS015Project) prj).compile(studentAcct); //TODO: Total hack for tester support
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
        String compileDir = prj.getStudentCodeDirectory(studentLogin);
        String testerName = TesterUtils.getTesterName(prj.getName());

        //TODO: Make this generalizable (perhaps each Project subclass will be responsible for running a tester)
        Allocator.getGeneralUtilities().executeJavaInVisibleTerminal(compileDir, testerName + ".Tester",
                                                                     CS015Project.CLASSPATH, CS015Project.LIBRARY_PATH,
                                                                     "Testing " + studentLogin + "'s " + prj.getName());
    }

    private class TesterGUI extends JFrame
    {
        private TesterGUI(String asgnName, String studentAcct)
        {
              super("Testing " + studentAcct + "'s " + asgnName);
              this.setVisible(false);
              this.setLayout(new BorderLayout());

//            TestResults toDisplay = XMLReader.readXML(asgnName, studentAcct);
//            TesterPanel mp = new TesterPanel(toDisplay);
//            this.add(mp, BorderLayout.CENTER);
//
//            this.pack();
//
//            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//
//            //Open up a dialog on window close to save rubric data
//            this.addWindowListener(new WindowAdapter() {
//
//                public void windowClosing(WindowEvent e) {
//                    TesterGUI.this.dispose();
//                }
//            });
        }

    }

}
