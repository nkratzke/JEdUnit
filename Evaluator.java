import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Optional;
import java.util.Arrays;
import java.util.Scanner;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.*;

/**
 * Basic evaluator for automatic evaluation of programming excercise assignments.
 * This evaluator is intended to be used with Moodle and VPL (Virtual Programming Lab).
 * It provides basic capabilities to evaluate programming assignents.
 * 
 * !!! Normally there is no need to touch this file !!!
 * !!! Keep it, unless you are perfectly knowing what you are doing !!!
 * 
 * @author Nane Kratzke
 */
public class Evaluator {

    /**
     * Restriction annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Restriction { }

    /**
     * Check annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Check { }

    /**
     * Cache that stores already parsed source files by the Parser.
     */
    private static final Map<String, CompilationUnit> CACHE = new HashMap<>();

    class Selected <T extends Node> {

        private String file = "";

        private List<T> nodes = new LinkedList<>();

        Selected(T n, String f) { 
            this.nodes.add(n); 
            this.file = f;
        }

        Selected(Collection<T> ns, String f) { 
            this.nodes.addAll(ns);
            this.file = f;
        }

        public <R extends Node> Selected<R> select(Class<R> selector) {
            List<R> selected = new LinkedList<>();
            for (T n : this.nodes) {
                List<R> hits = n.findAll(selector);
                hits.remove(n);
                for (R hit : hits) selected.add(hit);
            }
            return new Selected<R>(selected, this.file);
        }

        public <R extends Node> Selected<R> select(Class<R> selector, Predicate<R> pred) {
            Selected<R> selected = this.select(selector);
            return selected.filter(pred);
        }

        /**
         * Filters all nodes from the selected nodes that match a criteria.
         * @param fulfills Predicate that expresses a selection criteria
         * @return Reference to filtered nodes (for method chaining)
         */
        public Selected<T> filter(Predicate<T> fullfills) {
            List<T> filtered = new LinkedList<>();
            for (T n : this.nodes) {
                if (fullfills.test(n)) filtered.add(n);
            } 
            return new Selected<T>(filtered, this.file);
        }

        /**
         * Eliminates all nodes that occure more than once in the selected nodes.
         * @return Reference to selected nodes (for method chaining)
         */
        public Selected<T> distinct() {
            return new Selected<T>(
                this.nodes.stream().distinct().collect(Collectors.toList()), 
                this.file
            );
        }

        /**
         * Annotates each selected node with a message for VPL.
         * Triggers a VPL comment for each node.
         * @param msg Annotating remark
         * @return Self reference (for method chaining)
         */
        public Selected<T> annotate(String msg) {
            for(T node : this.nodes) {
                comment(this.file, node.getRange(), msg);
            }
            return this;
        }

        /**
         * Annotates each selected node with a message for VPL.
         * Triggers a VPL comment for each node.
         * @param msg Lambda function to create a message for each selected node individually
         * @return Self reference (for method chaining)
         */
        public Selected<T> annotate(Function<T, String> msg) {
            for (T node  : this.nodes) {
                comment(this.file, node.getRange(), msg.apply(node));
            }
            return this;
        }

        /**
         * Annotates each selected node matching a predicate with a message for VPL.
         * Triggers a VPL comment for each matching node.
         * @param msg Annotating remark
         * @param pred Predicate used to select a subset of selected nodes for annotation
         * @return Self reference (for method chaining)
         */
        public Selected<T> annotate(String msg, Predicate<T> pred) {
            this.filter(pred).annotate(msg);
            return this;
        }

        public boolean isEmpty() { return this.nodes.isEmpty(); }

        public boolean exists() { return !this.isEmpty(); }

        public List<T> asList() { return this.nodes; }

        public Stream<T> stream() { return this.nodes.stream(); }

    }

    /**
     * Wrapper class for a JavaParser CompilationUnit
     * with plenty of convenience methods.
     * TO BE DONE.
     */
    class Parser {

        private String file;

        private CompilationUnit compilationUnit;

        Parser(String f) throws FileNotFoundException {
            this.file = f;
            this.compilationUnit = Evaluator.CACHE.getOrDefault(
                this.file, 
                JavaParser.parse(new File(this.file))
            );
        }

        public <T extends Node> Selected<T> select(Class<T> selector) {
            Selected<CompilationUnit> s = new Selected<>(this.compilationUnit, this.file);
            return s.select(selector);
        }

        public <T extends Node> Selected<T> select(Class<T> selector, Predicate<T> pred) {
            Selected<CompilationUnit> s = new Selected<>(this.compilationUnit, this.file);
            return s.select(selector, pred);
        }

        public String[] getSource() {
            return this.compilationUnit.toString().split("\n");
        }

    }

    /**
     * The maximum points for a VPL assignment.
     */
    private static final int MAX = 100;

    /**
     * The currently reached points for a VPL assignment.
     */
    private int points = 0;

    /**
     * Test case counter.
     * Declared static to count testcases consecutively across
     * different Check classes.
     */
    private static int testcase = 0;

    /**
     * Adds points for grading if a check is passed (wishful behavior).
     * A comment is always printed whether the check was successfull or not.
     */
    protected final void grading(int add, String remark, Supplier<Boolean> check) {
        testcase++;
        try {
            if (check.get()) {
                this.points += add;
                comment("Check " + testcase + ": [OK] " + remark + " (" + add + " points)");
            } else comment("Check " + testcase + ": [FAILED] " + remark + " (0 of " + add + " points)");
        } catch (Exception ex) {
            comment("Check " + testcase + ": [FAILED due to " + ex + "] " + remark + " (0 of " + add + " points)");
        }
    }

    /**
     * Deletes points (penalzing) if a check is passed (unwishful behavior).
     * A comment is only printed if the check indicates a violation.
     */
    protected final void penalize(int penalty, String remark, Supplier<Boolean> violation) {
        try {
            if (!violation.get()) return;
            this.points -= penalty;
            comment("[FAILED] " + remark + " (-" + penalty + " points)");
        } catch (Exception ex) {
            this.points -= penalty;
            comment("[FAILED due to " + ex + "] " + remark + " (-" + penalty + " points)");
        }
    }

    /**
     * Checks whether a severe condition is met. E.g. a cheating submission.
     * In case the check is evaluated to true the evaluation is aborted immediately.
     */
    protected final void abortOn(String comment, Supplier<Boolean> violation) {
        try {
            if (!violation.get()) return;
            comment("Evaluation aborted! " + comment);
            grade(0);
            System.exit(1);
        } catch (Exception ex) {
            comment(String.format("Evaluation aborted! %s (Exception %s)", comment, ex));
            grade(0);
            System.exit(1);
        }
    }

    protected final Parser parse(String file) throws FileNotFoundException {
        return new Parser(file);
    }

    /**
     * Can be used to formulate arbitrary checks on parsed source code.
     */
    protected final boolean check(String file, Predicate<Parser> test) {
        try {
            return test.test(new Parser(file));
        } catch (Exception ex) {
            comment("Check failed: " + ex);
            comment("Is there a syntax error in your submission? " + file);
            return false;
        }
    }

    /**
     * Adds a comment for VPL via console output.
     */
    protected void comment(String c) { 
        System.out.println("Comment :=>> " + c); 
    }

    /**
     * Adds a comment for VPL via console output.
     * Marks file and line position.
     */
    protected void comment(String file, int line, String c) {
        comment(String.format("%s:%d: %s", file, line, c));
    }

    /**
     * Adds a comment for VPL via console output.
     * Marks file and position via a JavaParser range.
     */
    protected void comment(String file, Optional<Range> range, String c) {
        if (!range.isPresent()) comment(file + ": " + c);
        int line = range.get().begin.line;
        int col = range.get().begin.column;
        comment(String.format("%s:%d:%d: %s", file, line, col, c));
    }

    /**
     * Reports a grade to VPL via console output (truncated to [0, 100]).
     */
    protected void grade(int p) {
        int report = p;
        report = report > MAX ? MAX : report;
        report = report < 0 ? 0 : report;
        System.out.println("Grade :=>> " + report);
    }

    private List<Method> allMethodsOf(Class<?> clazz) {
        if (clazz == null) return new LinkedList<>();
        List<Method> methods = allMethodsOf(clazz.getSuperclass());
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    /**
     * Executes all methods annoted with a specified annotation.
     * @param annotation Annotation, one of [@Restriction, @Check]
     */
    protected final void process(Class<? extends Annotation> annotation) {
        for (Method test : allMethodsOf(this.getClass())) {
            if (!test.isAnnotationPresent(annotation)) continue;
            try {
                test.invoke(this);
                comment("");
            } catch (Exception ex) {
                comment("Test case " + test.getName() + " failed completely." + ex);
            } finally {
                grade(points);
            }
        }
    }

    /**
     * This method evaluates the checkstyle log file.
     */
    protected final void checkstyle() {
        try {
            Scanner in = new Scanner(new File("checkstyle.log"));
            while (in.hasNextLine()) {
                String result = in.nextLine();
                for (String file : Evaluator.EVALUATED_FILES) {
                    if (!result.contains(file)) continue;
                    if (Evaluator.CHECKSTYLE_IGNORES.stream().anyMatch(ignore -> result.contains(ignore))) continue;

                    String msg = result.substring(result.indexOf(file));
                    comment("[CHECKSTYLE]: " + msg);
                    this.points -= Evaluator.CHECKSTYLE_PENALTY;
                }
            }
            in.close();
            comment("");
            comment("[CHECKSTYLE] Result: " + this.points + " points");
            comment("");
        } catch (Exception ex) {
            comment("You are so lucky! We had problems processing the checkstyle.log.");
            comment("This was due to: " + ex);
        }
    }

    /**
     * List of Checkstyle checks that are ignored for evaluation.
     * This list is set in the configure method().
     */
    protected static List<String> CHECKSTYLE_IGNORES = new LinkedList<String>();

    /**
     * List of file names that shall be considered by checkstyle and evaluation.
     * This list is set in the configure method().
     */
    protected static List<String> EVALUATED_FILES = new LinkedList<String>();

    /**
     * Downgrade for every found checkstyle error.
     */
    protected static int CHECKSTYLE_PENALTY = 5;

    protected static boolean CHECK_IMPORTS = true;

    protected static List<String> ALLOWED_IMPORTS = new LinkedList<String>();

    protected static int IMPORT_PENALTY = 25;

    protected static boolean ALLOW_LOOPS = true;

    protected static int LOOP_PENALTY = 100;

    protected static boolean ALLOW_LAMBDAS = true;

    protected static int LAMBDA_PENALITY = 25;

    protected static boolean ALLOW_INNER_CLASSES = false;

    protected static int INNER_CLASS_PENALTY = 100;

    protected static boolean ALLOW_GLOBAL_VARIABLES = false;

    protected static int GLOBAL_VARIABLE_PENALTY = 25;

    protected static boolean CHECK_COLLECTION_INTERFACES = true;

    protected static int COLLECTION_INTERFACE_PENALTY = 25;

    protected static boolean ALLOW_CONSOLE_OUTPUT = false;

    protected static int CONSOLE_OUTPUT_PENALTY = 25;

    /**
     * This method is a hook for the Checks class to configure the evaluation.
     */
    protected void configure() {
        Evaluator.CHECKSTYLE_IGNORES.addAll(Arrays.asList(
            "[NewlineAtEndOfFile]", "[HideUtilityClassConstructor]", "[FinalParameters]",
            "[JavadocPackage]", "[AvoidInlineConditionals]", "[RegexpSingleline]", "[NeedBraces]",
            "[MagicNumber]"
        ));

        Evaluator.EVALUATED_FILES.add("Main.java");

        Evaluator.ALLOWED_IMPORTS.add("java.util");
        Evaluator.ALLOWED_IMPORTS.add("java.io");
    }

    @Restriction
    void cheatDetection() {

        List<String> imports = Arrays.asList("java.lang.reflect", "java.lang.invoke");
        List<String> classes = Arrays.asList("Solution");
        List<String> calls   = Arrays.asList("System.exit", "Solution.");

        for (String file : EVALUATED_FILES) {
            abortOn("Possible cheat detected", () -> check(file, ast -> 
                ast.select(ImportDeclaration.class)
                   .filter(imp -> imports.stream().anyMatch(danger -> imp.getName().asString().startsWith(danger)))
                   .annotate(imp -> "[CHEAT] Forbidden import: " + imp.getName())
                   .exists() |
                ast.select(MethodCallExpr.class)
                   .filter(call -> calls.stream().anyMatch(danger -> call.toString().startsWith(danger)))
                   .annotate(call -> "[CHEAT] Forbidden call: " + call)
                   .exists() |
                ast.select(ObjectCreationExpr.class)
                   .filter(obj -> classes.stream().anyMatch(danger -> obj.toString().contains(danger)))
                   .annotate(obj -> "[CHEAT] Forbidden object creation: " + obj)
                   .exists() |
                ast.select(FieldAccessExpr.class)
                   .filter(field -> classes.stream().anyMatch(danger -> field.toString().startsWith(danger)))
                   .annotate(field -> "[CHEAT] Forbidden field access: " + field)
                   .exists()
            ));
        }
    }

    @Restriction
    protected void conventions() {
        for (String file : EVALUATED_FILES) {

            if (CHECK_IMPORTS) penalize(IMPORT_PENALTY, "Non allowed libraries", () -> check(file, ast ->
                ast.select(ImportDeclaration.class)
                   .filter(imp -> !ALLOWED_IMPORTS.stream().anyMatch(lib -> imp.getName().asString().startsWith(lib)))
                   .annotate(imp -> "Import of " + imp.getName() + " not allowed")
                   .exists()
            ));

            if (!ALLOW_LOOPS) penalize(LOOP_PENALTY, "No loops", () -> check(file, ast ->
                ast.select(WhileStmt.class).annotate("while loop not allowed").exists() |
                ast.select(ForStmt.class).annotate("for loop not allowed").exists() |
                ast.select(ForeachStmt.class).annotate("for loop not allowed").exists() |
                ast.select(DoStmt.class).annotate("do while loop not allowed").exists() |
                ast.select(MethodCallExpr.class).filter(m -> m.toString().contains(".forEach(")).annotate("forEach not allowed").exists()
            ));

            if (!ALLOW_LAMBDAS) penalize(LAMBDA_PENALITY, "No lambdas", () -> check(file, ast ->
                ast.select(LambdaExpr.class).annotate(l -> "lambda expression " + l + " not allowed").exists()
            ));
            
            if (!ALLOW_GLOBAL_VARIABLES) penalize(GLOBAL_VARIABLE_PENALTY, "No global variables", () -> check(file, ast ->
                ast.select(FieldDeclaration.class)
                   .filter(field -> !(field.isStatic() && field.isFinal()))
                   .annotate("No datafields allowed. Add the final static modifier to make it a constant value.")
                   .exists()
            ));

            if (!ALLOW_INNER_CLASSES) penalize(INNER_CLASS_PENALTY, "No inner classes", () -> check(file, ast -> 
                ast.select(ClassOrInterfaceDeclaration.class)
                   .select(ClassOrInterfaceDeclaration.class)
                   .distinct()
                   .annotate("Inner classes not allowed. So ugly.")
                   .exists()
            ));

            if (!ALLOW_CONSOLE_OUTPUT) penalize(CONSOLE_OUTPUT_PENALTY, "No console output in methods (except main)", () -> check(file, ast ->
                ast.select(MethodDeclaration.class)
                   .filter(m -> !m.getDeclarationAsString(false, false, false).equals("void main(String[])"))
                   .select(MethodCallExpr.class, expr -> expr.toString().startsWith("System.out.print"))
                   .annotate(call -> "Console output not allowed here")
                   .exists()
            ));

            if (CHECK_COLLECTION_INTERFACES) {
                List<Class> collections = Arrays.asList(
                    HashMap.class, TreeMap.class, HashSet.class, LinkedList.class, ArrayList.class
                );

                penalize(COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for return types", () -> check(file, ast ->
                    ast.select(MethodDeclaration.class)
                       .filter(m -> collections.stream().anyMatch(type -> m.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(m -> "Do not use " + m.getType() + " as return type")
                       .exists()
                ));

                penalize(COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for parameters", () -> check(file, ast ->
                    ast.select(Parameter.class)
                       .filter(param -> collections.stream().anyMatch(type -> param.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(p -> "Do not use " + p.getType() + " as parameter type")
                       .exists()
                ));
            }
        }
    }

    /**
     * The main method calls the evaluation.
     */
    public static final void main(String[] args) {
        Checks check = new Checks();
        check.configure();
        check.checkstyle();
        check.process(Restriction.class); // process restriction checks
        check.process(Check.class);       // process functional tests
        check.comment("Finished");
    }
}