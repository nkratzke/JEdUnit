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
}