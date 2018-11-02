package de.thl.jedunit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

/**
 * Wrapper class for a JavaParser CompilationUnit.
 * Can be used to query the parsed abstract syntax tree (AST)
 * via selectors (comparable to the DOM-tree via CSS selectors).
 * 
 * @author Nane Kratzke
 * 
 */
public class SyntaxTree {

    private String file;

    private CompilationUnit compilationUnit;

    /**
     * Cache that stores already parsed source files by the Parser.
     */
    private static final Map<String, CompilationUnit> CACHE = new HashMap<>();

    SyntaxTree(String f) throws FileNotFoundException {
        this.file = f;
        this.compilationUnit = CACHE.getOrDefault(
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