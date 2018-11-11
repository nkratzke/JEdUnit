package de.thl.jedunit;

import static de.thl.jedunit.DSL.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;

/**
 * Selector concept for abstract syntax trees (AST).
 * 
 * @author Nane Kratzke
 */
public class Selected <T extends Node> implements Iterable<T> {

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
     * @param fullfills Predicate that expresses a selection criteria
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
     * Returns the first selected node.
     * @return first selected node
     */
    public Selected<T> first() {
        return new Selected<T>(this.nodes.get(0), this.file);
    }

    /**
     * Returns an iterator over selected nodes.
     * @return Iterator object to process selected nodes
     */
    public Iterator<T> iterator() {
        return this.nodes.iterator();
    }

    /**
     * Returns whether no nodes have been selected.
     * @return true, if no nodes are selected
     *         false, otherwise
     */
    public boolean isEmpty() { return this.nodes.isEmpty(); }

    /**
     * Returns whether only a single node is selected.
     */
    public boolean isSingle() { return this.nodes.size() == 1; }

    /**
     * Gets the first selected node.
     * @return First selected node.
     */
    public T asNode() { return this.nodes.get(0); }

    /**
     * Returns whether selected node exists.
     * @return true, if nodes are selected
     *         false, otherwise
     */
    public boolean exists() { return !this.isEmpty(); }

    /**
     * Returns the amount of selected nodes.
     */
    public int count() { return this.nodes.size(); }

    /**
     * Returns the file where the selected nodes are coded.
     */
    public String getFile() { return this.file; }

}