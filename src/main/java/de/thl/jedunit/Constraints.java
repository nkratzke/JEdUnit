package de.thl.jedunit;

import static de.thl.jedunit.DSL.CALLABLE;
import static de.thl.jedunit.DSL.FIELD;
import static de.thl.jedunit.DSL.comment;
import static de.thl.jedunit.DSL.inspect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
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

import io.vavr.Tuple2;

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
        boolean allfine = true;
        for (String file : Config.EVALUATED_FILES) {

            File f = new File(file);
            if (!f.exists()) {
                comment("File not found: " + file); 
                allfine &= false;
                continue; 
            }

            if (Config.CHECK_IMPORTS) allfine &= !penalize(Config.IMPORT_PENALTY, "Non-allowed libraries", () -> inspect(file, ast ->
                ast.select(ImportDeclaration.class)
                   .filter(imp -> !Config.ALLOWED_IMPORTS.stream().anyMatch(lib -> imp.getName().asString().startsWith(lib)))
                   .annotate(imp -> "Import of " + imp.getName() + " not allowed")
                   .exists()
            ));

            if (!Config.ALLOW_LOOPS) allfine &= !penalize(Config.LOOP_PENALTY, "No loops", () -> inspect(file, ast ->
                ast.select(WhileStmt.class).annotate("while loop not allowed").exists() |
                ast.select(ForStmt.class).annotate("for loop not allowed").exists() |
                ast.select(ForEachStmt.class).annotate("for loop not allowed").exists() |
                ast.select(DoStmt.class).annotate("do while loop not allowed").exists() |
                ast.select(MethodCallExpr.class).filter(m -> m.toString().contains(".forEach(")).annotate("forEach not allowed").exists()
            ));

            if (!Config.ALLOW_METHODS) allfine &= !penalize(Config.METHOD_PENALTY, "No methods (except main)", () -> inspect(file, ast ->
                ast.select(MethodDeclaration.class)
                   .filter(m -> !m.getNameAsString().equals("main"))
                   .annotate("No methods except main() method allowed")
                   .exists()
            ));

            if (!Config.ALLOW_LAMBDAS) allfine &= !penalize(Config.LAMBDA_PENALITY, "No lambdas", () -> inspect(file, ast ->
                ast.select(LambdaExpr.class).annotate(l -> "lambda expression " + l + " not allowed").exists()
            ));
            
            if (!Config.ALLOW_DATAFIELDS) allfine &= !penalize(Config.DATAFIELD_PENALTY, "No global variables", () -> inspect(file, ast ->
                ast.select(FieldDeclaration.class)
                   .filter(field -> !(field.isStatic() && field.isFinal()))
                   .annotate("No datafields allowed. Add the final static modifier to make it a constant value.")
                   .exists()
            ));

            if (!Config.ALLOW_INNER_CLASSES) allfine &= !penalize(Config.INNER_CLASS_PENALTY, "No inner classes", () -> inspect(file, ast -> 
                ast.select(ClassOrInterfaceDeclaration.class)
                   .select(ClassOrInterfaceDeclaration.class)
                   .distinct()
                   .annotate("Inner classes not allowed. So ugly.")
                   .exists()
            ));

            if (!Config.ALLOW_CONSOLE_OUTPUT) allfine &= !penalize(Config.CONSOLE_OUTPUT_PENALTY, "No console output in methods (except main)", () -> inspect(file, ast ->
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

                allfine &= !penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for return types", () -> inspect(file, ast ->
                    ast.select(MethodDeclaration.class)
                       .filter(m -> collections.stream().anyMatch(type -> m.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(m -> "Do not use " + m.getType() + " as return type")
                       .exists()
                ));

                allfine &= !penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for parameters", () -> inspect(file, ast ->
                    ast.select(Parameter.class)
                       .filter(param -> collections.stream().anyMatch(type -> param.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(p -> "Do not use " + p.getType() + " as parameter type")
                       .exists()
                ));

                allfine &= !penalize(Config.COLLECTION_INTERFACE_PENALTY, "Use Map, List, and Set interfaces for variable declarators", () -> inspect(file, ast -> 
                    ast.select(VariableDeclarator.class)
                       .filter(v -> collections.stream().anyMatch(type -> v.getType().asString().startsWith(type.getSimpleName())))
                       .annotate(v -> "Do not use " + v.getType() + " as variable declarator")
                       .exists()
                ));
            }
        }
        
        if (allfine) comment("Everything fine");
    }
    
    /**
     * Compares the structure of a submitted class with a reference class.
     * Reports all structural differences between both classes of following kinds:
     * 
     * - structural difference of class declaration (extends, implements)
     * - structural differences of datafields
     * - structural differences of callables (methods, constructors)
     * 
     * So far: 
     * - no type parameters are considered (generic classes)!
     * - no inner classes are considered
     * 
     * @param ref reference class (reference must be a single selected)
     * @param sub submitted class (submitted must be a single selected)
     * @param renamings List of renaming tuples (to match the names in reference class with the in the submitted class)
     * @return Results of structural difference analysis (CompareResult object)
     */
    @SafeVarargs
    public final CompareResult compareClasses(Selected<ClassOrInterfaceDeclaration> ref, Selected<ClassOrInterfaceDeclaration> sub, Tuple2<String, String>... renamings) {
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
                String rfield = rename("" + normalize(rf.getModifiers()) + " " + rf.getCommonType() + " " + rvar.getName(), renamings);
                boolean found = false;
                for (FieldDeclaration sf : subFields) {
                    for (VariableDeclarator svar : sf.getVariables()) {
                        String sfield = "" + normalize(sf.getModifiers()) + " " + sf.getCommonType() + " " + svar.getName();
                        if (rfield.equals(sfield)) {
                            result.add(true, rf, svar, "Datafield found: " + rfield);
                            found = true;
                        }
                    }
                }
                if (!found) {
                    result.add(false, rf, subClass, "Missing/wrong declared datafield: " + rfield);
                }
            }
        }

        Selected<CallableDeclaration> refCallables = ref.select(CALLABLE);
        Selected<CallableDeclaration> subCallables = sub.select(CALLABLE);

        for (CallableDeclaration rc : refCallables) {
            boolean found = false;
            String kind = rc.isConstructorDeclaration() ? "Constructor" : "Method";
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
                result.add(false, rc, subClass, "Missing/wrong declared " + kind.toLowerCase() + ": " + declaration);
            }
        }

        return result;
    }

    /**
     * Reports a CompareResult of a complex class compare to console.
     * @param result Result of the class compare
     * @param sub Selected class for the compare
     * @since 0.2.2
     */
    public void report(CompareResult result, Selected<ClassOrInterfaceDeclaration> sub) {
        comment("Checking class structure: " + sub.getFile());
        result.forEach(r -> {
            grading(r.getPoints(), r.comment(), () -> r.ok());
            if (r.violates()) {
                comment(sub.getFile(), r.getNode().getRange(), r.comment());
            }
        });
        comment("");
    }


    @SafeVarargs
    private static String normalize(ClassOrInterfaceDeclaration clazz, Tuple2<String, String>... renamings) {
        return join(" ", Stream.of(
            "class:",
            normalize(clazz.getModifiers()),
            rename(clazz.getNameAsExpression(), renamings),
            rename(clazz.getTypeParameters(), renamings),
            rename(clazz.getExtendedTypes(), renamings),
            rename(clazz.getImplementedTypes(), renamings)
        ));
    }

    private static String normalize(EnumSet<?> xs) {
        return xs.stream().map(x -> x.toString().toLowerCase()).collect(Collectors.joining(" "));
    }

    @SafeVarargs
    private static String normalize(CallableDeclaration<?> callable, Tuple2<String, String>... renamings) {       
        return join(" ", Stream.of(
            callable.isConstructorDeclaration() ? "constructor:" : "method:",
            normalize(callable.getModifiers()),
            rename(callable.getTypeParameters(), renamings),
            rename(callable.getDeclarationAsString(true, false, false), renamings)
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
}