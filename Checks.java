import java.util.*;
import java.util.stream.*;
import java.lang.reflect.*;

/**
 * Please add your test cases for evaluation here.
 * - Please provide meaningful remarks for your students in grading() calls.
 * - All grading() calls should sum up to 100.
 * - All methods that start with "test" will be executed automatically.
 * - If this sounds similar to unit testing - this is intended ;-)
 */
class Checks extends Evaluator {

    public void testCrazyStrings() {
        grading(10, "Leere Zeichenkette durchsuchen liefert nicht 0.", () -> Main.countChars('x', "") == 0);             
        grading(10, "Null Zeichenkette durchsuchen liefert nicht 0.", () -> Main.countChars('X', null) == 0);
    }

    public void testFindNothing() {
        grading(10, "Es werden nicht vorhandene Zeichen gezählt.", () -> Main.countChars('x', "abc") == 0);     
        grading(10, "Vorhandene Zeichen werden nicht gezählt.", () -> Main.countChars('a', "abc") == 1);     
    }

    public void testCaseSensitity() {
        grading(5, "Bei kleinen Zeichen wird Groß-/Kleinschreibung nicht beachtet.", () -> Main.countChars('b', "abc") == 1);
        grading(5, "Bei kleinen Zeichen wird Groß-/Kleinschreibung nicht beachtet.", () -> Main.countChars('b', "aBc") == 1);        
        grading(5, "Bei großen Zeichen wird Groß-/Kleinschreibung nicht beachtet.", () -> Main.countChars('B', "abc") == 1);
        grading(5, "Bei großen Zeichen wird Groß-/Kleinschreibung nicht beachtet.", () -> Main.countChars('B', "aBc") == 1);
    }

    public void testMultipleOccurrences() {
        grading(5, "Mehrfache Vorkommen werden nicht korrekt gezählt.", () -> Main.countChars('b', "abcbbb") == 4);
        grading(5, "Mehrfache Vorkommen werden nicht korrekt gezählt.", () -> Main.countChars('b', "abcbB") == 3);
        grading(5, "Mehrfache Vorkommen werden nicht korrekt gezählt.", () -> Main.countChars('B', "abcB") == 2);
        grading(5, "Mehrfache Vorkommen werden nicht korrekt gezählt.", () -> Main.countChars('B', "BaBcB") == 3);
    }

    public void testOnStartEnd() {
        grading(10, "Es werden Zeichen nicht am Anfang gefunden.", () -> Main.countChars('x', "xabc") == 1);         
        grading(10, "Es werden Zeichen nicht am Ende gefunden.", () -> Main.countChars('x', "abcx") == 1);         
    }
}