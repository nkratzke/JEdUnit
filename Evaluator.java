import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import java.lang.reflect.*;
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
     * List of Checkstyle checks that are ignored for evaluation.
     * This list is set in the configure method().
     */
    public static final List<String> IGNORE_CHECKS = new LinkedList<String>();

    /**
     * List of file names that shall be considered by checkstyle.
     * This list is set in the configure method().
     */
    public static final List<String> CHECK_FILES = new LinkedList<String>();

    /**
     * Downgrade for every found checkstyle error.
     */
    public static int CHECK_PENALTY = 5;

    /**
     * Cache that stores already parsed source files by the Parser.
     */
    private static final Map<String, CompilationUnit> CACHE = new HashMap<>();

    /**
     * Wrapper class for a JavaParser CompilationUnit
     * with plenty of convenience methods.
     * TO BE DONE.
     */
    class Parser {

        private String file;

        private CompilationUnit compilationUnit;

        public Parser(String file) throws FileNotFoundException {
            this.file = file;
            this.compilationUnit = Evaluator.CACHE.getOrDefault(
                this.file, 
                JavaParser.parse(new File(this.file))
            );
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

        public boolean noImportsOf(String... libs) {
            boolean found = false;
            for (ImportDeclaration imp : this.getImports()) {
                String lib = imp.getName().asString();
                if (Stream.of(libs).allMatch(l -> !lib.startsWith(l))) continue;
                comment(this.file, imp.getRange(), String.format("Import of '%s' not allowed.", lib));
                found = true;
            }
            return !found;
        }

        public boolean onlyImportsOf(String... libs) {
            boolean found = false;
            for (ImportDeclaration imp : this.getImports()) {
                String lib = imp.getName().asString();
                if (Stream.of(libs).anyMatch(l -> lib.startsWith(l))) continue;
                comment(this.file, imp.getRange(), String.format("Import of '%s' not allowed.", lib));
                found = true;
            }
            return !found;
        }

        public boolean noContainOf(String... terms) {
            boolean found = false;
            int lineNo = 0;
            for (String line : this.getSource()) {
                lineNo++;
                for (String term : terms) {
                    if (!line.contains(term)) continue;
                    comment(this.file, lineNo, String.format("Term '%s' not allowed.", term));
                    found = true;
                }
            }
            return !found;
        }

        public boolean noInnerClasses() {
            Set<ClassOrInterfaceDeclaration> found = new HashSet<>();
            for (ClassOrInterfaceDeclaration c : this.getClasses()) {
                List<ClassOrInterfaceDeclaration> inners = c.findAll(ClassOrInterfaceDeclaration.class);
                inners.remove(c);
                if (inners.isEmpty()) continue;
                found.addAll(inners);
            }
            for (ClassOrInterfaceDeclaration inner : found) {
                comment(this.file, inner.getRange(), "Inner class " + inner.getName() + " not allowed.");
            }
            return found.isEmpty();
        }

        public boolean noStatementOf(Class<? extends Statement>... stmts) {
            boolean found = false;
            for (Class<? extends Statement> stmt : stmts) {
                for (Node n : this.compilationUnit.findAll(stmt)) {
                    found = true;
                    comment(this.file, n.getRange(), stmt.getSimpleName() + " not allowed.");
                }
            }
            return !found;
        }

        public boolean noDataFields() {
            boolean found = false;
            for (FieldDeclaration field : this.getDataFields()) {
                found = true;
                SimpleName n = field.getVariables().get(0).getName();
                comment(this.file, field.getRange(), "Datafield " + n + " not allowed.");
            }
            return !found;
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
        * Adds a comment for VPL via console output if a condition mets.
        * Marks file and position via a JavaParser range if the condition mets.
        * @param node Node (Expression) to be marked
        * @param msg Comment to be added
        * @param check Test to be performed on node
        * @return true if check on node is evaluated to true (in this case the message is printed to console fpr further VPL processing)
        *         false, if check  on node evaluated to false (no message is printed in that case)
        */
        protected <T extends Expression> boolean report(T node, String msg, Predicate<T> check) {
            boolean valid = check.test(node);
            if (valid) comment(this.file, node.getRange(), msg + "(" + node + ")");
            return valid;
        }
    }

    /**
     * Deprecated
     */
    class Inspector {

        private Class object;

        public Inspector(String cname) throws ClassNotFoundException { this.object = Class.forName(cname); }

        public String getName() { return object.getSimpleName(); }

        public Stream<Field> fields() { 
            return Stream.of(object.getDeclaredFields()).filter(f -> {
                int m = ((Field)f).getModifiers();
                return !(Modifier.isStatic(m) && Modifier.isFinal(m));
            });
        }
        
        public boolean hasNoFields() { return fields().count() == 0; }

        public Stream<Class> innerClasses() {
            return Stream.of(object.getDeclaredClasses());
        }

        public boolean hasNoInnerClasses() { return innerClasses().count() == 0; }

        public Stream<Field> constants() { 
            return Stream.of(object.getDeclaredFields()).filter(f -> {
                int m = ((Field)f).getModifiers();
                return Modifier.isStatic(m) && Modifier.isFinal(m);
            });
        }

        public boolean hasNo(String... keywords) {
            Path path = Paths.get(object.getSimpleName() + ".java");
            try {
                int i = 0;
                for (String line : Files.readAllLines(path)) {
                    i++;
                    for (String keyword : keywords) {
                        if (line.contains(keyword)) {
                            comment("Line " + i + ": " + line);
                            comment("Line " + i + " in file " + path + " seem to have a not allowed '" + keyword + "' phrase.");
                            
                            // No points if non allowed phrases are found in the submission.
                            // Evaluation is stopped immediately to prevent point injection attacks.
                            System.out.println("Grade :=>> 0");
                            System.exit(1);
                            return false;
                        }
                    }
                }
                return true;    
            } catch (IOException ex) {
                comment("Could not inspect file " + path + " due to exception " + ex.getMessage());
                return false;
            }
        }

        public boolean hasNoLoops() { return hasNo("while", "for"); }

        public boolean hasNoConstants() { return constants().count() == 0; }

        public Stream<Method> methods() { 
            return Stream.of(object.getDeclaredMethods()).filter(m -> !((Method)m).getName().startsWith("lambda$")); 
        }

        public boolean hasNoMethods() { return methods().count() == 0; }
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
     * A comment is printed whether the check was successfull or not.
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
     * Deletes points for grading if a check is not passed (unwishful behavior).
     * A comment is printed if the check was not successfull.
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
     * Checks whether a severe condition is met. E.g. a cheating submission.
     * In case the check is not passed the evaluation is aborted immediately.
     */
    protected final void abortOn(String comment, Supplier<Boolean> check) {
        try {
            if (check.get()) return;
            comment("Evaluation aborted! " + comment);
            grade(0);
            System.exit(1);
        } catch (Exception ex) {
            comment(String.format("Evaluation aborted! %s (Exception %s)", comment, ex));
            grade(0);
            System.exit(1);
        }
    }

    /**
     * Deprecated
     */
    protected final <T> boolean assure(String className, Predicate<Inspector> check) {
        try {
            return check.test(new Inspector(className));
        } catch (Exception ex) {
            comment("Check failed due to " + ex);
            comment("This might be due to a syntax error in your submission.");
            return false;
        }
    }

    /**
     * Can be used to formulate arbitrary checks on parsed source code.
     * TO BE DONE.
     */
    protected final boolean check(String file, Predicate<Parser> test) {
        try {
            return test.test(new Parser(file));
        } catch (Exception ex) {
            comment("Check failed due to " + ex);
            comment("This might be due to a syntax error in your submission: " + file);
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
     * Reports a grade to VPL via console output (limited to [0, 100]).
     */
    protected void grade(int p) {
        int report = p;
        report = report > MAX ? MAX : report;
        report = report < 0 ? 0 : report;
        System.out.println("Grade :=>> " + report);
    }

    /**
     * This method scans and invokes all methods starting with "test" to run the grading.
     */
    protected final void evaluate() {
        for (Method test : this.getClass().getDeclaredMethods()) {
            if (!test.getName().startsWith("test")) continue;
            try {
                test.invoke(this);
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
                for (String file : Evaluator.CHECK_FILES) {
                    if (!result.contains(file)) continue;
                    if (Evaluator.IGNORE_CHECKS.stream().anyMatch(ignore -> result.contains(ignore))) continue;

                    String msg = result.substring(result.indexOf(file));
                    comment("[CHECKSTYLE]: " + msg);
                    this.points -= Evaluator.CHECK_PENALTY;
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
     * This method is a hook for the Checks class to configure the evaluation.
     */
    protected void configure() {
        Evaluator.IGNORE_CHECKS.addAll(Arrays.asList(
            "[NewlineAtEndOfFile]", "[HideUtilityClassConstructor]", "[FinalParameters]",
            "[JavadocPackage]", "[AvoidInlineConditionals]", "[RegexpSingleline]", "[NeedBraces]"
        ));

        Evaluator.CHECK_FILES.add("Main.java");

        Evaluator.CHECK_PENALTY = 5;
    }

    /**
     * The main method calls the evaluation.
     */
    public static final void main(String[] args) {
        Checks checks = new Checks();
        checks.configure();
        checks.checkstyle();
        checks.evaluate();
        checks.comment("Finished");
    }
}