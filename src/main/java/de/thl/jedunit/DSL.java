package de.thl.jedunit;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.mifmif.common.regex.Generex;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;

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
     * Abbreviation to select constructors.
     */    
    public final static Class<ConstructorDeclaration> CONSTRUCTOR = ConstructorDeclaration.class;

    /**
     * Abbreviation to select methods.
     */
    public final static Class<MethodDeclaration> METHOD = MethodDeclaration.class;

    /**
     * Abbreviation to select callables (so methods or constructors).
     */
    public final static Class<CallableDeclaration> CALLABLE = CallableDeclaration.class;

    /**
     * Adds a comment for VPL via console output.
     */
    public static void comment(String c) { 
        System.out.println("Comment :=>> " + c); 
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

    @SafeVarargs
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
     * Compares the structure of a submitted class with a reference class.
     * Returns all structural differences between both classes of following kinds:
     * 
     * - structural difference of class declaration (extends, implements)
     * - structural differences of datafields
     * - structural differences of callables (methods, constructors)
     * 
     * So far: no type parameters are supported (generic classes)!
     * 
     * @param ref reference class (reference must be a single selected)
     * @param sub submitted class (submitted must be a single selected)
     * @param renamings List of renaming tuples (to match the names in reference class with the in the submitted class)
     * @return null, if ref of sub are not single selected nodes.
     *         structural differences (as CompareResult object)
     */
    public static CompareResult compareClasses(Selected<ClassOrInterfaceDeclaration> ref, Selected<ClassOrInterfaceDeclaration> sub, Tuple2<String, String>... renamings) {
        if (!ref.isSingle() || !sub.isSingle()) return null;

        CompareResult result = new CompareResult();
        ClassOrInterfaceDeclaration refClass = ref.asNode();
        ClassOrInterfaceDeclaration subClass = sub.asNode();

        if (normalize(subClass).equals(normalize(refClass, renamings))) {
            result.add(true, refClass, subClass, "Class declaration correct");
        } else {
            result.add(false, refClass, subClass, "Class declaration incorrect");
        }

        Selected<FieldDeclaration> refFields = ref.select(FIELD);
        Selected<FieldDeclaration> subFields = sub.select(FIELD);

        for(FieldDeclaration rf : refFields) {
            for (VariableDeclarator rvar : rf.getVariables()) {
                String rfield = rename("" + rf.getModifiers() + rf.getCommonType() + " " + rvar.getName(), renamings);
                boolean found = false;
                for (FieldDeclaration sf : subFields) {
                    for (VariableDeclarator svar : sf.getVariables()) {
                        String sfield = "" + sf.getModifiers() + sf.getCommonType() + " " + svar.getName();
                        if (rfield.equals(sfield)) {
                            result.add(true, rvar, svar, "Datafield found: " + rfield);
                            found = true;
                        }
                    }
                }
                if (!found) {
                    result.add(false, rvar, subClass, "Missing datafield: " + rfield);
                }
            }
        }

        Selected<CallableDeclaration> refCallables = ref.select(CALLABLE);
        Selected<CallableDeclaration> subCallables = sub.select(CALLABLE);

        for (CallableDeclaration rc : refCallables) {
            boolean found = false;
            String kind = rc.isConstructorDeclaration() ? "constructor" : "method";
            for (CallableDeclaration sc : subCallables) {
                if (normalize(rc, renamings).equals(normalize(sc))) {
                    String declaration = sc.getDeclarationAsString(true, false, false);
                    result.add(true, rc, sc, kind + " found: " + declaration);
                    found = true;
                    continue;
                }
            }
            if (!found) {
                String declaration = rename(rc.getDeclarationAsString(true, false, false), renamings);
                result.add(false, rc, subClass, "Missing " + kind + ": " + declaration);
            }
        }

        return result;
    }

    @SafeVarargs
    private static String normalize(ClassOrInterfaceDeclaration clazz, Tuple2<String, String>... renamings) {
        return join(" ", Stream.of(
            "class:",
            clazz.getModifiers(),
            rename(clazz.getNameAsExpression(), renamings),
            rename(clazz.getTypeParameters(), renamings),
            rename(clazz.getExtendedTypes(), renamings),
            rename(clazz.getImplementedTypes(), renamings)
        ));
    }

    @SafeVarargs
    private static String normalize(CallableDeclaration<?> callable, Tuple2<String, String>... renamings) {       
        return join(" ", Stream.of(
            callable.isConstructorDeclaration() ? "constructor:" : "method:",
            callable.getModifiers(),
            rename(callable.getTypeParameters(), renamings),
            rename(callable.getDeclarationAsString(false, false), renamings)
        ));
    }

    @SafeVarargs
    private static <T> String rename(T s, Tuple2<String, String>... renamings) {
        String result = s.toString();
        for (Tuple2<String, String> rename : renamings) {
            result = result.replaceAll(rename._1, rename._2);
        }
        return result;
    }

    private static <T> String join(String sep, Stream<T> s) {
        return s.map(d -> d.toString()).collect(Collectors.joining(sep)).trim();
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
}