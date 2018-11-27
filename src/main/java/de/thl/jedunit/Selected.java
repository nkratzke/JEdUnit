package de.thl.jedunit;

import static de.thl.jedunit.DSL.comment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;

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

    /**
     * Selects nodes that are recursive childs of selected nodes.
     * @param selector Child nodes to be selected
     * @return Reference to selected child nodes
     */
    public <R extends Node> Selected<R> select(Class<R> selector) {
        List<R> selected = new LinkedList<>();
        for (T n : this.nodes) {
            List<R> hits = n.findAll(selector);
            hits.remove(n);
            for (R hit : hits) selected.add(hit);
        }
        return new Selected<R>(selected, this.file);
    }

    /**
     * Selects nodes that are recursive childs of selected nodes
     * and fulfill a condition.
     * @param selector Child nodes to be selected
     * @param pred Filter predicate
     * @return Reference to selected and filtered child nodes
     */
    public <R extends Node> Selected<R> select(Class<R> selector, Predicate<R> pred) {
        Selected<R> selected = this.select(selector);
        return selected.filter(pred);
    }

    /**
     * Selects nodes that are direct childs of selected nodes.
     * @param selector Child nodes to be selected
     * @return Reference to selected child nodes
     */
    @SuppressWarnings("unchecked")
    public <R extends Node> Selected<R> childSelect(Class<R> selector) {
        List<R> selected = new LinkedList<>();
        for (T n : this.nodes) {
            for (Node child : n.findAll(selector)) {
                if (child.getParentNode().get().equals(n)) selected.add((R)child);
            }
        }
        return new Selected<R>(selected, this.file);
    }

    private boolean matchName(Node n, Optional<String> name) {
        if (!(n instanceof NodeWithSimpleName)) return false;
        NodeWithSimpleName<?> node = (NodeWithSimpleName<?>)n;
        if (!name.isPresent()) return true;
        return node.getNameAsString().equals(name.get());
    }

    private boolean matchModifier(Node n, Optional<String> modifier) {
        if (!(n instanceof NodeWithModifiers)) return false;
        NodeWithModifiers<?> node = (NodeWithModifiers<?>)n;
        if (!modifier.isPresent() && !node.getModifiers().isEmpty()) return true;
        if (!modifier.isPresent()) return false;
        String modifiers = node.getModifiers().toString().toLowerCase();
        return modifiers.contains(modifier.get().toLowerCase());
    }

    private boolean matchType(Node n, Optional<String> type) {
        if (!(n instanceof NodeWithType)) return false;
        NodeWithType<?,?> node = (NodeWithType<?,?>)n;
        if (!type.isPresent()) return true;
        return node.getTypeAsString().equals(type.get());
    }

    @SuppressWarnings("unchecked")
    public Selected<T> filter(String... filters) {
        List<T> selected = new LinkedList<>();
        for (String filter : filters) {
            String[] components = filter.split("\\*=|^=|$=|=");
            String attribute = components[0].trim();
            Optional<String> value = components.length == 2 ? Optional.of(components[1].trim()) : Optional.empty();
            for (Node n : this.nodes) {
                if (attribute.equals("name") && matchName(n, value)) selected.add((T)n);
                if (attribute.equals("modifier") && matchModifier(n, value)) selected.add((T)n);
                if (attribute.equals("type") && matchType(n, value)) selected.add((T)n);
            }
        }
        return new Selected<T>(selected, this.file);
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