package de.thl.jedunit;

import java.io.File;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.mifmif.common.regex.Generex;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;

/**
 * This class provides the Domain Specific Language (DSL)
 * to express JEdUnit evaluations. It contains methods and constants
 * 
 * - to randomize test cases
 * - to formulate test cases as a set of test data tuples
 * - to parse source files into abstract syntax tree
 * - to select elements of abstract syntax trees (like classes, datafields, methods)
 * - to comment test results
 * 
 * @author Nane Kratzke
 */
public class DSL {

    /**
     * Random generator.
     */
    private static final Random RANDOM = new Random();

    /**
     * Regular expression for default characters.
     */
    private static final String DEFAULT_CHARS = "[a-zA-Z]";

    /**
     * Abbreviation to select classes (and interfaces).
     */
    public final static Class<ClassOrInterfaceDeclaration> CLAZZ = ClassOrInterfaceDeclaration.class;

    /**
     * Abbreviation to select datafields (and constants).
     */
    public final static Class<FieldDeclaration> FIELD = FieldDeclaration.class;
    
    /**
     * Abbreviation to select variables (in class datafield declarations or methods).
     */
    public final static Class<VariableDeclarator> VAR = VariableDeclarator.class;

    /**
     * Abbreviation to select constructors.
     */    
    public final static Class<ConstructorDeclaration> CONSTRUCTOR = ConstructorDeclaration.class;

    /**
     * Abbreviation to select methods.
     */
    public final static Class<MethodDeclaration> METHOD = MethodDeclaration.class;

    /**
     * Abbreviation to select parameters.
     */
    public final static Class<Parameter> PARAMETER = Parameter.class;

    /**
     * Abbreviation to select callables (so methods or constructors).
     */
    public final static Class<CallableDeclaration> CALLABLE = CallableDeclaration.class;

    /**
     * Abbreviation to select lambdas.
     */
    public final static Class<LambdaExpr> LAMBDA = LambdaExpr.class;

    /**
     * Abbreviation to select blocks.
     */
    public final static Class<BlockStmt> BLOCK = BlockStmt.class;

    /**
     * Abbreviation to select if statements.
     */
    public final static Class<IfStmt> IF = IfStmt.class;

    /**
     * Abbreviation to select return statements.
     */
    public final static Class<ReturnStmt> RETURN = ReturnStmt.class;

    /**
     * Abbreviation to select for-loops.
     */
    public final static Class<ForStmt> FOR = ForStmt.class;

    /**
     * Abbreviation to select foreach-loops.
     */
    public final static Class<ForEachStmt> FOREACH = ForEachStmt.class;

    /**
     * Abbreviation to select while-loops.
     */
    public final static Class<WhileStmt> WHILE = WhileStmt.class;

    /**
     * Abbreviation to select do-while-loops.
     */
    public final static Class<DoStmt> DOWHILE = DoStmt.class;

    /**
     * Determines automatically all Java files of the submission
     * for evaluation.
     * @return Set of filenames ending on ".java"
     */
    public static Set<String> autoFiles() {
        return Stream.of(new File(".").listFiles())
            .filter(f -> f.isFile())
            .filter(f -> f.getName().endsWith(".java"))
            .map(f -> f.getName())
            .collect(Collectors.toSet());
    }

    /**
     * Adds a comment for VPL via console output.
     */
    public static void comment(String c) {
        if (c.contains("\n")) {
            Evaluator.LOG.add("<|--");
            Evaluator.LOG.add(c);
            Evaluator.LOG.add("--|>");    
        } else {
            Evaluator.LOG.add("Comment :=>>" + c);
        }
    }

    /**
     * Adds a comment for VPL via console output.
     * Marks file and position via a JavaParser range.
     */
    public static void comment(String file, Optional<Range> range, String c) {
        if (!range.isPresent()) comment(file + ": " + c);
        int line = range.get().begin.line;
        int col = range.get().begin.column;
        comment(String.format("%s:%d:%d: %s", file, line, col, c));
    }

    /**
     * Abbreviation for String.format().
     * @see String.format()
     */
    public static String f(String format, Object... args) {
        Object[] stringified = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            stringified[i] = repr(args[i]);
        }
        return String.format(format, stringified);
    }

    @SuppressWarnings("unchecked")
    public static String repr(Object o) {
        if (o == null) return "null";
        if (o instanceof String) return repr((String)o);
        if (o instanceof Character) return repr((char)o);
        if (o instanceof List) return repr((List)o);
        if (o instanceof Map) return repr((Map)o);
        return o.toString();
    }

    public static String repr(Object[] os) {
        List<String> reps = new LinkedList<>();
        for (Object o : os) reps.add(repr(o));
        return reps.toString();
    }

    private static List<Byte> to_l(byte[] data) { List<Byte> l = new LinkedList<>(); for (byte d : data) l.add(d); return l;}
    private static List<Short> to_l(short[] data) { List<Short> l = new LinkedList<>(); for (short d : data) l.add(d); return l;}
    private static List<Integer> to_l(int[] data) { List<Integer> l = new LinkedList<>(); for (int d : data) l.add(d); return l;}
    private static List<Long> to_l(long[] data) { List<Long> l = new LinkedList<>(); for (long d : data) l.add(d); return l;}
    private static List<Boolean> to_l(boolean[] data) { List<Boolean> l = new LinkedList<>(); for (boolean d : data) l.add(d); return l;}
    private static List<Character> to_l(char[] data) { List<Character> l = new LinkedList<>(); for (char d : data) l.add((Character)d); return l;}
    private static List<Float> to_l(float[] data) { List<Float> l = new LinkedList<>(); for (float d : data) l.add(d); return l;}
    private static List<Double> to_l(double[] data) { List<Double> l = new LinkedList<>(); for (double d : data) l.add(d); return l;}

    public static String repr(byte[] data) { return repr(to_l(data)); }
    public static String repr(short[] data) { return repr(to_l(data)); }
    public static String repr(int[] data) { return repr(to_l(data)); }
    public static String repr(long[] data) { return repr(to_l(data)); }
    public static String repr(boolean[] data) { return repr(to_l(data)); }
    public static String repr(char[] data) { return repr(to_l(data)); }
    public static String repr(float[] data) { return repr(to_l(data)); }
    public static String repr(double[] data) { return repr(to_l(data)); }
    // public static String repr(Object[] data) { return repr(Stream.of(data).collect(Collectors.toList())); }

    /**
     * Makes typical non-printable characters explicit.
     */
    public static String repr(char c) {
        if (c == ' ') return "'\u23b5'";
        if (c == '\t') return "'\u21b9'";
        if (c == '\n') return "'\u23ce'";
        return "'" + c + "'";
    }

    /**
     * Represents typical non-printable chars in Strings.
     */
    public static String repr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace(" ", "\u23b5")
                .replace("\t", "\u21b9")
                .replace("\n", "\u23ce\n") + "\"";
    }

    /**
     * Creates a normalized string representation of a map.
     * Keys are ordered alphabetically.
     * Non-printable chers in Strings are indicated.
     * @param m Map
     * @return normalized representation of a Map as String representation
     */
    public static <K, V> String repr(Map<K, V> m) {
        if (m == null) return "null";
        Map<String, String> r = new TreeMap<>();
        for (K k : m.keySet()) r.put(repr(k), repr(m.get(k)));
        return r.toString();
    }

    /**
     * Creates a normalized string representation of a list.
     * Non-printable characters in Strings and characters are indicated.
     * @param l ist
     * @return normalized representation of the List
     */
    public static <T> String repr(java.util.List<T> list) {
        if (list == null) return "null";
        java.util.List<String> r = new LinkedList<>();
        for (T t : list) r.add(repr(t));
        return r.toString();
    }

    /**
     * Generates a string concatenated from regular expression generated random strings.
     * @param regexps Build patterns (regular expressions) for String generation
     * @return concatenated random String
     */
    public static String s(String... regexps) {
        String r = "";
        for (String regex : regexps) {
            Generex g = new Generex(regex);
            r += g.random();
        }
        return r;
    }

    /**
     * Generates a random string in a specified length range.
     * The string is composed of [a-z] and [A-Z] characters.
     * @param min minimum length
     * @param max maximum length
     * @return random String of length between min and max (inclusive)
     */
    public static String s(int min, int max) {
        return s(String.format(DEFAULT_CHARS + "{%d,%d}", min, max));
    }

    /**
     * Generates a random boolean value.
     * @return true or false
     */
    public static boolean b() {
        return RANDOM.nextBoolean();
    }

    /**
     * Generates a random integer value.
     * @return random value
     */
    public static int i() {
        return RANDOM.nextInt();
    }

    /**
     * Generates a random integer value between 0 and an upper bound.
     * @param max upper bound
     * @return random value in [0, max[
     */
    public static int i(int max) {
        return RANDOM.nextInt(max);
    }

    /**
     * Generates a random integer value between a lower and an upper bound.
     * @param min lower bound
     * @param max upper bound
     * @return random value in [min, max[
     */
    public static int i(int min, int max) {
        return min + i(max - min);
    }

    /**
     * Generates a random double value. 
     * @return random double value in [0.0, 1.0[
     */
    public static double d() {
        return RANDOM.nextDouble();
    }

    /**
     * Generates a random double value between 0 and an upper bound.
     * @param max upper bound
     * @return random value in [0.0, max[
     */
    public static double d(double max) {
        return RANDOM.nextDouble() * max;
    }

    /**
     * Generates a random double value between a lower and an upper bound.
     * @param min lower bound
     * @param max upper bound
     * @return random value in [min, max[
     */
    public static double d(double min, double max) {
        return min + d(max - min);
    }

    /**
     * Generates a random char from a regular expression.
     * @param regexp Regular expression to generate a String
     * @return first char of the randomly generated String
     */
    public static char c(String regexp) {
        return s(regexp).charAt(0);
    }

    /**
     * Generates a random char.
     * @return random char in [a-z] or [A-Z]
     */
    public static char c() {
        return s(DEFAULT_CHARS).charAt(0);
    }

    /**
     * Generates a random list.
     * @param min Minimum length of list (must be >= 0)
     * @param max Maximum length of list (must be > min)
     * @param p Supplier to create random entries for the list
     * @return List of random length with random entries
     */
    public static <T> List<T> l(int min, int max, Supplier<T> p) {
        return Stream.generate(p).limit(i(min, max + 1)).collect(Collectors.toList());
    }

    /**
     * Generates a random list of specified length.
     * @param l Length of list > 0
     * @param p Supplier to create random entries for the list
     * @return List of random entries
     */
    public static <T> List<T> l(int l, Supplier<T> p) {
        return Stream.generate(p).limit(l).collect(Collectors.toList());
    }

    /**
     * Converts a list into an array.
     * @param list List of values
     * @return Array of values
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] a(List<T> list) {
        T[] arr = (T[])new Object[list.size()];
        int i = 0;
        for (T entry : list) arr[i++] = entry;
        return arr;
    }

    /**
     * Generates a tuple from two values.
     * @return two-tuple
     */
    public static <A, B> Tuple2<A, B> t(A a, B b) {
        return Tuple.of(a, b);
    }

    /**
     * Generates a tuple from three values.
     * @return tripple
     */
    public static <A, B, C> Tuple3<A, B, C> t(A a, B b, C c) {
        return Tuple.of(a, b, c);
    }

    /**
     * Generates a tuple from four values.
     * @return four-tuple
     */
    public static <A, B, C, D> Tuple4<A, B, C, D> t(A a, B b, C c, D d) {
        return Tuple.of(a, b, c, d);
    }

    /**
     * Generates a tuple from five values.
     * @return five-tuple
     */
    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> t(A a, B b, C c, D d, E e) {
        return Tuple.of(a, b, c, d, e);
    }

    /**
     * Generates a tuple from six values.
     * @return six-tuple
     */
    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> t(A a, B b, C c, D d, E e, F f) {
        return Tuple.of(a, b, c, d, e, f);
    }

    /**
     * Generates a tuple from seven values.
     * @return seven-tuple
     */    
    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> t(A a, B b, C c, D d, E e, F f, G g) {
        return Tuple.of(a, b, c, d, e, f, g);
    }

    /**
     * Generates a tuple from eight values.
     * @return eight-tuple
     */    
    public static <A, B, C, D, E, F, G, H> Tuple8<A, B, C, D, E, F, G, H> t(A a, B b, C c, D d, E e, F f, G g, H h) {
        return Tuple.of(a, b, c, d, e, f, g, h);
    }

    /**
     * Generates a list of test data tuples.
     * @return List of test data tuples
     * @deprecated Use {@link #test()} instead.
     */
    @SafeVarargs
    @Deprecated
    public static <T> List<T> testWith(T... ts) {
        return List.of(ts);
    }

    /**
     * Parses a Java source file to generate an abstract syntax tree representation.
     * @param f Java source file
     * @return SyntaxTree object
     *         null, if file f could not be parsed sucessfully
     */
    public static SyntaxTree parse(String f) {
        try {
            return new SyntaxTree(f);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Can be used to formulate arbitrary AST-based checks on parsed source code.
     * @param file File to load an parse
     * @param test Logical predicate on the AST of file
     * @return evaluated test predicate
     *         false in case the file could not be loaded or parsed
     */
    public static boolean inspect(String file, Predicate<SyntaxTree> test) {
        try {
            SyntaxTree ast = parse(file);
            if (ast == null) {
                comment("Could not parse file: " + file);
                return false;
            }
            return test.test(ast);
        } catch (Exception ex) {
            comment("Check failed: " + ex);
            comment("Is there a syntax error in your submission? " + file);
            return false;
        }
    }

    /**
     * Returns the file path of a JEdUnit internal resource.
     * Mainly used to simplify testing. Do not use it for your own code.
     * @param r resource
     * @return path to resource
     */
    public static String resource(String r) {
        return ClassLoader.getSystemClassLoader().getResource(r).getFile();
    }

    /**
     * Compares to Map objects.
     * Both maps are converted into key-sorted TreeMaps.
     * Then, the String representation of these key-sorted maps are compared.
     * @param expected Expected map
     * @param actual Actual map
     * @return true, if key-sorted String representations of the maps are equal.
     *         false, otherwise.
     */
    public static <K, V> boolean assertEquals(Map<K, V> expected, Map<K, V> actual) {
        if (expected == null || actual == null) return expected == actual;
        Map<K, V> e = new TreeMap<>(expected);
        Map<K, V> a = new TreeMap<>(actual);
        return e.toString().equals(a.toString());
    }

    /**
     * Compares two objects via their String representations.
     * @param expected Expected value
     * @param actual Actual value
     * @return true, if <code>expected.toString().equals(actual.toString())</code>
     *         false, otherwise
     */
    public static <T> boolean assertEquals(T expected, T actual) {
        if (expected == null || actual == null) return expected == actual;
        return expected.toString().equals(actual.toString());
    }

    /**
     * Captures an Exception.
     * @param code Code to be executed that is expected to raise an exception
     * @return Returns the canonical name of the captured exception object.
     *         <code>null</code> if no exception was thrown
     * @since 0.2.3
     */
    public static <T> String captureException(Supplier<T> code) {
        try {
            code.get();
            return null;
        } catch(Exception ex) {
            return ex.getClass().getCanonicalName();
        }
    }
}