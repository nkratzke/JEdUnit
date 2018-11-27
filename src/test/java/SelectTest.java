import static org.junit.Assert.*;

import com.github.javaparser.printer.lexicalpreservation.Kept;

import org.junit.Test;

import static de.thl.jedunit.DSL.*;

public class SelectTest {

    @Test public void testSelect() {
        inspect(resource("Solution.java.template"), ast -> {
            assertTrue(ast.select(METHOD).select(IF).exists());
            assertTrue(ast.select(METHOD).childSelect(IF).isEmpty());
            assertTrue(ast.select(FOREACH).childSelect(BLOCK).childSelect(IF).exists());
            return true;
        });
    }

    @Test public void testFilteredNameSelect() {
        inspect(resource("Submission.java.test"), ast -> {
            assertEquals(2, ast.select(METHOD).filter("name=method").count());
            assertEquals(1, ast.select(METHOD).filter("name=test").count());
            assertEquals(0, ast.select(METHOD).filter("name=missing").count());
            assertEquals(3, ast.select(METHOD).filter("name").count());
            return true;
        });
    }

    @Test public void testFilteredModifierSelect() {
        inspect(resource("Submission.java.test"), ast -> {
            assertEquals(2, ast.select(FIELD).filter("modifier=public").count());
            assertEquals(2, ast.select(FIELD).filter("modifier=private").count());
            assertEquals(1, ast.select(FIELD).filter("modifier=protected").count());
            assertEquals(1, ast.select(FIELD).filter("modifier=static").count());
            assertEquals(2, ast.select(METHOD).filter("modifier=public").count());
            assertEquals(1, ast.select(CONSTRUCTOR).filter("modifier").count());
            return true;
        });
    }

    @Test public void testFilteredTypeSelect() {
        inspect(resource("Submission.java.test"), ast -> {
            assertEquals(5, ast.select(FIELD).select(VAR).filter("type").count());
            assertEquals(2, ast.select(FIELD).select(VAR).filter("type=int").count());
            assertEquals(1, ast.select(FIELD).select(VAR).filter("type=List<Submission>").count());
            assertEquals(3, ast.select(METHOD).filter("type").count());
            assertEquals(1, ast.select(METHOD).filter("type=int").count());
            return true;
        });
    }

}