package de.thl.jedunit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

/**
 * This class provides several preconfigured constraints.
 * This is mainly done for the convenience of the formulation of assignments.
 * 
 * @author Nane Kratzke
 * 
 */
public class Constraints extends Evaluator {

    /**
     * The convention is to check imports (whitelist of libraries).
     */
    protected static boolean CHECK_IMPORTS = true;

    protected static List<String> ALLOWED_IMPORTS = Arrays.asList("java.util");

    protected static int IMPORT_PENALTY = 25;

    /**
     * The following imports are never allowed, because they could be used
     * to do arbitrary harm (like to mask point injection attacks).
     */
    protected static List<String> CHEAT_IMPORTS = Arrays.asList("java.lang.reflect", "java.lang.invoke");

    protected static boolean ALLOW_LOOPS = true;

    protected static int LOOP_PENALTY = 100;

    protected static boolean ALLOW_METHODS = true;

    protected static int METHOD_PENALTY = 100;

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
    public void configure() {
    }

    @Constraint
    public void cheatDetection() {
        comment("Running pre-checks on " + EVALUATED_FILES.stream().collect(Collectors.joining(", ")));

        List<String> classes = Arrays.asList("Solution");
        List<String> calls   = Arrays.asList("System.exit", "Solution.");

        for (String file : EVALUATED_FILES) {
            abortOn("Possible cheat detected", () -> check(file, ast -> 
                ast.select(ImportDeclaration.class)
                   .filter(imp -> CHEAT_IMPORTS.stream().anyMatch(danger -> imp.getName().asString().startsWith(danger)))
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
        comment("Everything fine");
    }

    @Constraint
    public void conventions() {
        comment("Checking coding restrictions for " + EVALUATED_FILES.stream().collect(Collectors.joining(", ")));
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
                ast.select(ForEachStmt.class).annotate("for loop not allowed").exists() |
                ast.select(DoStmt.class).annotate("do while loop not allowed").exists() |
                ast.select(MethodCallExpr.class).filter(m -> m.toString().contains(".forEach(")).annotate("forEach not allowed").exists()
            ));

            if (!ALLOW_METHODS) penalize(METHOD_PENALTY, "No methods (except main)", () -> check(file, ast ->
                ast.select(MethodDeclaration.class)
                   .filter(m -> !m.getNameAsString().equals("main"))
                   .annotate("No methods except main() method allowed")
                   .exists()
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

                penalize(COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for variable declarators", () -> check(file, ast -> 
                    ast.select(VariableDeclarator.class)
                       .filter(v -> collections.stream().anyMatch(type -> v.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(v -> "Do not use " + v.getType() + " as variable declarator")
                       .exists()
                ));
            }
        }
    }
}