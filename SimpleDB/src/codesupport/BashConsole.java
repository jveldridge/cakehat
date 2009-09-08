package codesupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Vector;

class BashConsole {

    public static Collection<String> write(Iterable<String> input) {
        Vector<String> output = new Vector<String>();

        File wd = new File("/bin");
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("/bin/bash", null, wd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for (String line : input) {
                out.println(line);
            }
            out.println("exit");
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    output.add(line);
                }
                proc.waitFor();
                in.close();
                out.close();
                proc.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return output;
    }
}
