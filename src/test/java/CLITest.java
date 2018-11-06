import static de.thl.jedunit.Randomized.s;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
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
        // delete(d);
        System.setProperty("user.dir", CWD);
    }

    @Test public void testCLI() {
        String[] noargs = {};
        CLI.main(noargs);
        File dir = new File(DIR);
        File[] files = dir.listFiles();
        Stream.of(CLI.RESOURCES).forEach(r -> {
            assertTrue(r + " should be created", Stream.of(files).anyMatch(f -> f.getName().equals(r)));
        });
        assertTrue("Scripts should be executable", 
            Stream.of(files).filter(f -> f.getName().endsWith(".sh")).allMatch(f -> f.canExecute())
        );
    }

    @Test public void runBasicExample() throws Exception {

        // TO BE DONE

        String URL = "https://raw.githubusercontent.com/nkratzke/VPL-java-template/working/init.sh";

        Process p = null;
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(new File(DIR + File.separator));
        pb.command("curl", "-s", URL, "|", "sh");
        p = pb.start();

        pb.command("sh", "vpl_evaluate.sh", ">", "evaluate.log");
        p = pb.start();

        pb.command("sh", "vpl_execution", ">", "execution.log");
        p = pb.start();
    }
}