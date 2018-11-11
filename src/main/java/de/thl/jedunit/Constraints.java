package de.thl.jedunit;

import static de.thl.jedunit.DSL.comment;
import static de.thl.jedunit.DSL.inspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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
     * This method is a hook for the Checks class to configure the evaluation.
     */
    public void configure() {
    }

    @Inspection(description="Inspect source files for suspect code")
    public void cheatDetection() {
        List<String> classes = Arrays.asList("Solution");
        List<String> calls   = Arrays.asList("System.exit", "Solution.");

        for (String file : Config.EVALUATED_FILES) {
            abortOn("Possible cheat detected", () -> inspect(file, ast -> 
                ast.select(ImportDeclaration.class)
                   .filter(imp -> Config.CHEAT_IMPORTS.stream().anyMatch(danger -> imp.getName().asString().startsWith(danger)))
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

    @Inspection(description="Inspect source files for coding violations")
    public void conventions() {
        for (String file : Config.EVALUATED_FILES) {

            if (Config.CHECK_IMPORTS) penalize(Config.IMPORT_PENALTY, "Non-allowed libraries", () -> inspect(file, ast ->
                ast.select(ImportDeclaration.class)
                   .filter(imp -> !Config.ALLOWED_IMPORTS.stream().anyMatch(lib -> imp.getName().asString().startsWith(lib)))
                   .annotate(imp -> "Import of " + imp.getName() + " not allowed")
                   .exists()
            ));

            if (!Config.ALLOW_LOOPS) penalize(Config.LOOP_PENALTY, "No loops", () -> inspect(file, ast ->
                ast.select(WhileStmt.class).annotate("while loop not allowed").exists() |
                ast.select(ForStmt.class).annotate("for loop not allowed").exists() |
                ast.select(ForEachStmt.class).annotate("for loop not allowed").exists() |
                ast.select(DoStmt.class).annotate("do while loop not allowed").exists() |
                ast.select(MethodCallExpr.class).filter(m -> m.toString().contains(".forEach(")).annotate("forEach not allowed").exists()
            ));

            if (!Config.ALLOW_METHODS) penalize(Config.METHOD_PENALTY, "No methods (except main)", () -> inspect(file, ast ->
                ast.select(MethodDeclaration.class)
                   .filter(m -> !m.getNameAsString().equals("main"))
                   .annotate("No methods except main() method allowed")
                   .exists()
            ));

            if (!Config.ALLOW_LAMBDAS) penalize(Config.LAMBDA_PENALITY, "No lambdas", () -> inspect(file, ast ->
                ast.select(LambdaExpr.class).annotate(l -> "lambda expression " + l + " not allowed").exists()
            ));
            
            if (!Config.ALLOW_GLOBAL_VARIABLES) penalize(Config.GLOBAL_VARIABLE_PENALTY, "No global variables", () -> inspect(file, ast ->
                ast.select(FieldDeclaration.class)
                   .filter(field -> !(field.isStatic() && field.isFinal()))
                   .annotate("No datafields allowed. Add the final static modifier to make it a constant value.")
                   .exists()
            ));

            if (!Config.ALLOW_INNER_CLASSES) penalize(Config.INNER_CLASS_PENALTY, "No inner classes", () -> inspect(file, ast -> 
                ast.select(ClassOrInterfaceDeclaration.class)
                   .select(ClassOrInterfaceDeclaration.class)
                   .distinct()
                   .annotate("Inner classes not allowed. So ugly.")
                   .exists()
            ));

            if (!Config.ALLOW_CONSOLE_OUTPUT) penalize(Config.CONSOLE_OUTPUT_PENALTY, "No console output in methods (except main)", () -> inspect(file, ast ->
                ast.select(MethodDeclaration.class)
                   .filter(m -> !m.getDeclarationAsString(false, false, false).equals("void main(String[])"))
                   .select(MethodCallExpr.class, expr -> expr.toString().startsWith("System.out.print"))
                   .annotate(call -> "Console output not allowed here")
                   .exists()
            ));

            if (Config.CHECK_COLLECTION_INTERFACES) {
                List<Class<?>> collections = Arrays.asList(
                    HashMap.class, TreeMap.class, HashSet.class, LinkedList.class, ArrayList.class
                );

                penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for return types", () -> inspect(file, ast ->
                    ast.select(MethodDeclaration.class)
                       .filter(m -> collections.stream().anyMatch(type -> m.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(m -> "Do not use " + m.getType() + " as return type")
                       .exists()
                ));

                penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for parameters", () -> inspect(file, ast ->
                    ast.select(Parameter.class)
                       .filter(param -> collections.stream().anyMatch(type -> param.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(p -> "Do not use " + p.getType() + " as parameter type")
                       .exists()
                ));

                penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for variable declarators", () -> inspect(file, ast -> 
                    ast.select(VariableDeclarator.class)
                       .filter(v -> collections.stream().anyMatch(type -> v.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(v -> "Do not use " + v.getType() + " as variable declarator")
                       .exists()
                ));
            }
        }
    }
}