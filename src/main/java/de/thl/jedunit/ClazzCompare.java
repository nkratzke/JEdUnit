package de.thl.jedunit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.Parameter;

public class ClazzCompare {

    private static <T> String join(String sep, Stream<T> s) {
        return s.map(d -> d.toString()).collect(Collectors.joining(sep));
    }

    private static String normalize(ClassOrInterfaceDeclaration clazz) {
        return join(" ", Stream.of(
            "class:",
            clazz.getModifiers(),
            clazz.getNameAsExpression(),
            clazz.getTypeParameters(),
            clazz.getExtendedTypes(),
            clazz.getImplementedTypes()
        ));
    }

    private static String normalize(FieldDeclaration field) {
        String r = "";
        String modifiers = field.getModifiers().toString();
        String type = field.getCommonType().toString();
        for (VariableDeclarator v : field.getVariables()) {
            r += join(" ", Stream.of(
                "field:",
                modifiers,
                type,
                v.getName()
            )) + "\n";
        }
        return r.trim();
    }

    private static String normalize(CallableDeclaration<?> callable) {       
        return join(" ", Stream.of(
            callable.isConstructorDeclaration() ? "constructor:" : "method:",
            callable.getModifiers(),
            callable.getTypeParameters(),
            callable.getDeclarationAsString(false, false)
        ));
    }

    public static String normalize(SyntaxTree t) {
        Class<ClassOrInterfaceDeclaration> clazz = ClassOrInterfaceDeclaration.class;
        Class<FieldDeclaration> field = FieldDeclaration.class;
        Class<ConstructorDeclaration> constructor = ConstructorDeclaration.class;
        Class<MethodDeclaration> method = MethodDeclaration.class;

        Selected<FieldDeclaration> datafields = t.select(clazz).select(field);
        Selected<ConstructorDeclaration> constructors = t.select(clazz).select(constructor);
        Selected<MethodDeclaration> methods = t.select(clazz).select(method);
        
        String cl = normalize(t.select(clazz).first());
        String fs = join("\n", datafields.stream().map(d -> normalize(d)).sorted());
        String cs = join("\n", constructors.stream().map(c -> normalize(c)).sorted());
        String ms = join("\n", methods.stream().map(m -> normalize(m)).sorted());
        return join("\n", Stream.of(cl, fs, cs, ms)) + "\n";
    }

    public static boolean compare(String c1, String c2, String... renamings) {
        try {
            String s1 = normalize(new SyntaxTree(c1));
            String s2 = normalize(new SyntaxTree(c2));

            System.out.println(s1);
            System.out.println(s2);

            return s1.equals(s2);    
        } catch (Exception ex) {
            Evaluator.comment(String.format("Could not compare files %s, %s. Exception: %s", c1, c2, ex));
            return false;
        }
    }

    /*
    public static boolean compare(String c1, String c2, String from, String to) {
        try {
            String s1 = normalize(new SyntaxTree(c1));
            String s2 = normalize(new SyntaxTree(c2));
            return s2.replace(from, to).equals(s1);
        }
    }
    */

}