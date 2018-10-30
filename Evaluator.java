import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import java.nio.file.*;
import java.io.*;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.*;

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

        public Selected<T> filter(Predicate<T> fullfills) {
            List<T> filtered = new LinkedList<>();
            for (T n : this.nodes) {
                if (fullfills.test(n)) filtered.add(n);
            } 
            return new Selected<T>(filtered, this.file);
        }

        public Selected<T> annotate(String msg) {
            for(T node : this.nodes) {
                comment(this.file, node.getRange(), msg);
            }
            return this;
        }

        public Selected<T> annotate(Function<T, String> msg) {
            for (T node  : this.nodes) {
                comment(this.file, node.getRange(), msg.apply(node));
            }
            return this;
        }

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

        public List<FieldDeclaration> getDataFields() {
            return this.compilationUnit.findAll(FieldDeclaration.class);
        }

        public List<MethodDeclaration> getMethods() {
            return this.compilationUnit.findAll(MethodDeclaration.class);
        }

        public List<ClassOrInterfaceDeclaration> getClasses() {
            return this.compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        }

        public List<ImportDeclaration> getImports() {
            return this.compilationUnit.findAll(ImportDeclaration.class);
        }

        public <T extends Expression> List<T> getExpressions(Class<T> expr) {
            return this.compilationUnit.findAll(expr);
        }

        public boolean nonAllowedImports(String... libs) {
            boolean found = false;
            for (ImportDeclaration imp : this.getImports()) {
                String lib = imp.getName().asString();
                if (Stream.of(libs).noneMatch(l -> lib.startsWith(l))) continue;
                comment(this.file, imp.getRange(), String.format("Import of '%s' not allowed.", lib));
                found = true;
            }
            return found;
        }

        public boolean accessOn(String... entities) {
            boolean found = false;
            for (String entity : entities) {
                Stream<MethodCallExpr> calls = this.getExpressions(MethodCallExpr.class).stream();
                Stream<ObjectCreationExpr> creations = this.getExpressions(ObjectCreationExpr.class).stream();
                Stream<FieldAccessExpr> access = this.getExpressions(FieldAccessExpr.class).stream();
                found |= calls.anyMatch(e -> report(e, "Forbidden call", n -> n.toString().contains(entity + "("))) |
                         creations.anyMatch(e -> report(e, "Forbidden call", n -> n.toString().startsWith("new " + entity + "("))) |
                         access.anyMatch(e -> report(e, "Forbidden call", n -> n.toString().startsWith(entity)));
            }
            return found;
        }

        public boolean noParametersOf(Class<?>... types) {
            boolean found = false;
            for (com.github.javaparser.ast.body.Parameter p : this.compilationUnit.findAll(com.github.javaparser.ast.body.Parameter.class)) {
                for (Class<?> type : types) {
                    if (p.getType().asString().startsWith(type.getSimpleName())) {
                        found = true;
                        comment(this.file, p.getRange(), "Do not use " + type.getSimpleName() + " as a parameter type.");
                    }
                }
            }
            return !found;
        }

        public boolean noReturnTypesOf(Class<?>... types) {
            boolean found = false;
            for (MethodDeclaration m : this.getMethods()) {
                String declaration = m.getDeclarationAsString(false, false);
                for (Class<?> type : types) {
                    if (declaration.startsWith(type.getSimpleName())) {
                        found = true;
                        comment(this.file, m.getRange(), "Do not use " + type.getSimpleName() + " as a return type.");
                    }
                }
            }           
            return !found;
        }

        /**
        * Triggers a comment console output for VPL if a specified check condition mets.
        * Marks file and position of the respective node if the condition mets.
        * @param node Node to be marked
        * @param msg Explaining comment to be added
        * @param check Test to be performed on node
        * @return true if check on node is evaluated to true (in this case the message is printed to console for further VPL processing)
        *         false, if check on node evaluated to false (no message is printed in that case)
        */
        protected <T extends Node> boolean report(T node, String msg, Predicate<T> check) {
            boolean valid = check.test(node);
            if (valid) comment(this.file, node.getRange(), String.format("%s (%s)", msg, node));
            return valid;
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
     * Deletes points (penalzing) if a check is not passed (unwishful behavior).
     * A comment is only printed if the check was not successfull.
     * @Deprecated (use penalize instead)
     */
    protected final void degrading(int del, String remark, Supplier<Boolean> check) {
        try {
            if (check.get()) return;
            this.points -= del;
            comment("[FAILED] " + remark + " (subtracted " + del + " points)");
        } catch (Exception ex) {
            this.points -= del;
            comment("[FAILED due to " + ex + "] " + remark + " (subtracted " + del + " points)");
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
        for (String file : EVALUATED_FILES) {
            abortOn("Possible cheat detected", () -> check(file, c -> 
                c.nonAllowedImports("java.lang.reflect", "java.lang.invoke") |
                c.accessOn("Solution", "System.exit")
            ));
        }
    }

    @Restriction
    protected void conventions() {
        for (String file : EVALUATED_FILES) {

            if (CHECK_IMPORTS) penalize(IMPORT_PENALTY, "Non allowed libraries", () -> check(file, c ->
                c.select(ImportDeclaration.class)
                 .filter(imp -> !ALLOWED_IMPORTS.stream().anyMatch(lib -> imp.getName().asString().startsWith(lib)))
                 .annotate(imp -> "Import of " + imp.getName() + " not allowed")
                 .exists()
            ));

            if (!ALLOW_LOOPS) penalize(LOOP_PENALTY, "No loops", () -> check(file, c ->
                c.select(WhileStmt.class).annotate("while loop not allowed").exists() |
                c.select(ForStmt.class).annotate("for loop not allowed").exists() |
                c.select(ForeachStmt.class).annotate("for loop not allowed").exists() |
                c.select(DoStmt.class).annotate("do while loop not allowed").exists() |
                c.select(MethodCallExpr.class).filter(m -> m.toString().contains(".forEach(")).annotate("forEach not allowed").exists()
            ));

            if (!ALLOW_LAMBDAS) penalize(LAMBDA_PENALITY, "No lambdas", () -> check(file, c ->
                c.select(LambdaExpr.class).annotate(l -> "lambda expression " + l + " not allowed").exists()
            ));
            
            if (!ALLOW_GLOBAL_VARIABLES) penalize(GLOBAL_VARIABLE_PENALTY, "No global variables", () -> check(file, c ->
                c.select(FieldDeclaration.class)
                 .filter(field -> !(field.isStatic() && field.isFinal()))
                 .annotate("No datafields allowed. Add the final static modifier to make it a constant value.")
                 .exists()
            ));

            if (!ALLOW_INNER_CLASSES) penalize(INNER_CLASS_PENALTY, "No inner classes", () -> check(file, c -> 
                c.select(ClassOrInterfaceDeclaration.class)
                 .select(ClassOrInterfaceDeclaration.class)
                 .annotate("Inner classes not allowed. So ugly.")
                 .exists()
            ));

            if (CHECK_COLLECTION_INTERFACES) {
                Class[] notAllowedCollectionImplementations = { HashMap.class, TreeMap.class, HashSet.class, LinkedList.class, ArrayList.class };
                degrading(COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces.", () -> 
                    check(file, c -> c.noParametersOf(notAllowedCollectionImplementations)) &
                    check(file, c -> c.noReturnTypesOf(notAllowedCollectionImplementations))
                );    
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