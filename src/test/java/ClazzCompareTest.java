import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.thl.jedunit.ClazzCompare;

public class ClazzCompareTest {

    @Test
    public void testCompare() {
        String c1 = ClassLoader.getSystemClassLoader().getResource("A.java").getFile();
        String c2 = ClassLoader.getSystemClassLoader().getResource("B.java").getFile();
        
        assertTrue("Same classes should be equal", ClazzCompare.compare(c1, c1));
        assertTrue("Same classes should be equal", ClazzCompare.compare(c2, c2));
        

    }

}