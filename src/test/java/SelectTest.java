import static de.thl.jedunit.DSL.BLOCK;
import static de.thl.jedunit.DSL.CONSTRUCTOR;
import static de.thl.jedunit.DSL.FIELD;
import static de.thl.jedunit.DSL.FOREACH;
import static de.thl.jedunit.DSL.IF;
import static de.thl.jedunit.DSL.METHOD;
import static de.thl.jedunit.DSL.PARAMETER;
import static de.thl.jedunit.DSL.RETURN;
import static de.thl.jedunit.DSL.VAR;
import static de.thl.jedunit.DSL.inspect;
import static de.thl.jedunit.DSL.resource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
            assertEquals(2, ast.select(RETURN).count());
            assertEquals(0, ast.select(RETURN).filter("name").count());
            return true;
        });
    }

    @Test public void testFilteredOr() {
        inspect(resource("Submission.java.test"), ast -> {
            assertEquals(2, ast.select(METHOD).filter("name=method").count());
            assertEquals(1, ast.select(METHOD).filter("name=test").count());
            assertEquals(3, ast.select(METHOD).filter("name=method", "name=test").count());
            assertEquals(3, ast.select(METHOD).filter("name=test", "modifier=public").count());

            assertEquals(0, ast.select(METHOD).filter("name=missing").count());
            assertEquals(3, ast.select(METHOD).filter("name").count());
            assertEquals(3, ast.select(METHOD).filter("name", "name=missing").count());

            return true;
        });
    }

    @Test public void testFilteredAnd() {
        inspect(resource("Submission.java.test"), ast -> {
            assertEquals(2, ast.select(METHOD).filter("name=method").count());
            assertEquals(1, ast.select(METHOD).filter("name=test").count());
            assertEquals(0, ast.select(METHOD).filter("name=method").filter("name=test").count());
            assertEquals(0, ast.select(METHOD).filter("name=test").filter("name=method").count());
            assertEquals(0, ast.select(METHOD).filter("name=test").filter("modifier=public").count());
            assertEquals(1, ast.select(METHOD).filter("name=test").filter("modifier=private").count());
            assertEquals(1, ast.select(METHOD).filter("modifier=private").filter("name=test").count());

            assertEquals(0, ast.select(METHOD).filter("name=missing").count());
            assertEquals(3, ast.select(METHOD).filter("name").count());
            assertEquals(0, ast.select(METHOD).filter("name").filter("name=missing").count());
            assertEquals(0, ast.select(METHOD).filter("name=missing").filter("name").count());
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
            assertEquals(0, ast.select(RETURN).filter("modifier").count());
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
            assertEquals(0, ast.select(RETURN).filter("type").count());
            return true;
        });
    }
}