package de.thl.jedunit;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;


public class CompareResult {

    public class Entry {

        private boolean ok;
        private Node node;
        private String comment;

        /**
         * Constructor to create a result entry.
         * @param ok check passed successfully
         * @param n JavaParser node object to indicate the position in source file
         * @param c comment
         */
        public Entry(boolean ok, Node n, String c) {
            this.ok = ok;
            this.node = n;
            this.comment = c;
        }

        public boolean violates() { return !this.ok(); }

        public boolean ok() { return this.ok; }

        public Node getNode() { return this.node; }

        public String comment() { return this.comment; }

        public String toString() {
            return String.format("%s %s", this.ok ? "[OK]" : "[FAILED]", this.comment);
        }

    }

    private List<Entry> results = new LinkedList<>();

    public void add(boolean ok, Node n, String c) {
        results.add(new Entry(ok, n, c));
    }

    public Stream<Entry> results() {
        return this.results.stream();
    }

    public Stream<Entry> violations() {
        return this.results.stream().filter(r -> r.violates());
    }

    public boolean noViolations() {
        return this.violations().count() == 0;
    }

    public String toString() {
        return results.stream().map(r -> r.toString()).collect(Collectors.joining("\n"));
    } 

}