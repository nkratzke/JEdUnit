package de.thl.jedunit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;

/**
 * Result of a class compare.
 * 
 * @author Nane Kratzke
 */
public class CompareResult implements Iterable<CompareResult.Entry> {

    /**
     * Entry of a class compare result.
     * For each member (field, constructor, method) of a reference class an entry is generated
     * that indicates whether a member of the reference class is implemented accordingly in a
     * submission class.
     */
     public class Entry {

        private boolean ok;
        private Node reference;
        private Node submission;
        private String comment;

        /**
         * Constructor to create a result entry.
         * @param ok check passed successfully
         * @param r reference node object (to get the )
         * @param s submission node object to indicate the position in source file
         * @param c comment
         */
        public Entry(boolean ok, Node r, Node s, String c) {
            this.reference = r;
            this.submission = s;
            this.comment = c;
            this.ok = ok;
        }

        /**
         * Returns whether a submission member does not fit exactly a member of the reference class.
         * @return true, reference member is not found in submission
         *         false, reference member is found in submission
         */
        public boolean violates() { return !this.ok(); }

        /**
         * Returns whether a submission member fits exactly a member of the reference class.
         * @return true, reference member is found in submission
         *         false, reference member is not found in submission
         */
        public boolean ok() { return this.ok; }

        /**
         * Returns the submission node.
         * @return Submission node
         */
        public Node getNode() { return this.submission; }

        /**
         * Returns the points for a member of the reference class.
         * @return 1, if no @points annotation is found (or content of annotation is not parseable to an integer value)
         *         value of the @points annotation encoded as Javadoc at the reference class member 
         */
        public int getPoints() { 
            try {
                return Integer.parseInt(
                    this.reference.getComment().get().asJavadocComment().parse().getBlockTags().stream()
                        .filter(t -> t.getTagName().equals("points"))
                        .map(t -> t.getContent().toText())
                        .findFirst().get()
                );
            } catch (Exception ex) {
                return 1;
            }
        }

        /**
         * Returns the comment for this compare result entry.
         */
        public String comment() { return this.comment; }

    }

    private List<Entry> results = new LinkedList<>();

    /**
     * Adds a compare result.
     * @param ok Whether the check was ok or not
     * @param r The reference node
     * @param s The submission node
     * @param c Comment
     */
    public void add(boolean ok, Node r, Node s, String c) {
        results.add(new Entry(ok, r, s, c));
    }

    /**
     * Returns an iterator to process all elements sequentially.
     * @return Iterator object over all entries
     */
    public Iterator<CompareResult.Entry> iterator() {
        return this.results().iterator();
    }

    /**
     * Returns all results
     */
    public Stream<Entry> results() {
        return this.results.stream();
    }

    /**
     * Returns a stream of all violating entries.
     */
    public Stream<Entry> violations() {
        return this.results.stream().filter(r -> r.violates());
    }

    /**
     * Checks that no violations are found.
     */
    public boolean noViolations() {
        return this.violations().count() == 0;
    }

    public String toString() {
        return results().map(r -> r.toString()).collect(Collectors.joining("\n"));
    } 

}