import static de.thl.jedunit.Randomized.s;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.thl.jedunit.CLI;

public class CLITest {

    public static String DIR = null;
    public static String CWD = null;

    @Before public void createTestDir() {
        DIR = s("/tmp/test-[a-z]{5}-[0-9]{3}");
        CWD = System.setProperty("user.dir", DIR);
        File d = new File(DIR);
        d.mkdirs();
    }

    private void delete(File f) {
        try {
            if (f.isDirectory()) Stream.of(f.listFiles()).forEach(file -> delete(file));
            f.delete();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @After public void removeTestDir() {
        File d = new File(DIR);
        delete(d);
        System.setProperty("user.dir", CWD);
    }

    @Test public void testCLI() {
        String[] noargs = {};
        CLI.main(noargs);
        File dir = new File(DIR);
        File[] files = dir.listFiles();
        Stream.of(CLI.RESOURCES).forEach(r -> {
            String n = r.replace(".template", "");
            assertTrue(n + " should be created", Stream.of(files).anyMatch(f -> f.getName().equals(n)));
        });
        assertTrue("Scripts should be executable", 
            Stream.of(files).filter(f -> f.getName().endsWith(".sh")).allMatch(f -> f.canExecute())
        );
    }

    private String asString(InputStream is) {
        Scanner in = new Scanner(is);
        String line = "";  
        while (in.hasNextLine()) line += in.nextLine() + "\n";
        in.close();
        return line;
    }

    @Test
    public void runBasicExample() throws Exception {
        String URL = "https://raw.githubusercontent.com/nkratzke/VPL-java-template/working/init.sh";

        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(new File(DIR));
        Process p = pb.command("sh", "-c", "curl -s " + URL + " | sh").start();
        int exitCode = p.waitFor();
        String console = asString(p.getInputStream());  
        System.out.println(console);
        assertTrue("init.sh executed without errors", exitCode == 0);
        assertTrue("Installation worked", console.contains("Preparing Checks.java"));
  
        p = pb.command("sh", "vpl_evaluate.sh").start();
        exitCode = p.waitFor();
        File f = new File(DIR + File.separator + "vpl_execution");
        assertTrue("vpl_evaluate.sh executed without errors", exitCode == 0);
        assertTrue("vpl_execution script generated", f.isFile() && f.canExecute());

        p = pb.command("sh", "vpl_execution").start();
        exitCode = p.waitFor();
        console = asString(p.getInputStream());  
        System.out.println(console);
        assertTrue("Evaluation executed without errors", exitCode == 0);
        assertTrue("Evaluation works", console.contains("JEdUnit"));
        assertTrue("Evaluation works", console.contains("Check 18:"));
        assertTrue("Evaluation works", console.contains("Finished: 0 points"));

        exitCode = pb.command("sh", "vpl_run.sh").start().waitFor();
        assertTrue("vpl_run executed without errors", exitCode == 0);

        p = pb.command("sh", "vpl_execution").start();
        exitCode = p.waitFor();
        console = asString(p.getInputStream());
        assertTrue("Run command executes without errors", exitCode == 0);
        assertTrue("Run works", console.equals("-1\n-1\n"));
    }
}