/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of Graph.
 *
 * <p>PS2 instructions: you MUST use the provided rep.
 */
public class ConcreteVerticesGraph implements Graph<String> {

    private final List<Vertex> vertices = new ArrayList<>();

    // Abstraction function:
    //   AF(vertices) = a weighted directed graph whose vertex set is
    //     { v.getLabel() : v in vertices }, and which has a directed edge from
    //     v.getLabel() to t with weight w for each entry (t, w) in
    //     v.getTargets(), for each v in vertices.
    // Representation invariant:
    //   - vertex labels are distinct (no two Vertex objects share a label)
    //   - for every vertex v and every target label t in v.getTargets(), there
    //     is a vertex in `vertices` whose label is t
    //   - all edge weights stored in every vertex are positive
    // Safety from rep exposure:
    //   - `vertices` is private final and never returned; vertices() returns a
    //     fresh set of the (immutable String) labels, and sources()/targets()
    //     return fresh maps of immutable keys/values.
    //   - Vertex objects are never handed out to clients.

    /**
     * Create an empty ConcreteVerticesGraph.
     */
    public ConcreteVerticesGraph() {
        checkRep();
    }

    private void checkRep() {
        Set<String> seen = new HashSet<>();
        for (Vertex v : vertices) {
            assert seen.add(v.getLabel()) : "vertex labels must be distinct";
        }
        for (Vertex v : vertices) {
            for (Map.Entry<String, Integer> e : v.getTargets().entrySet()) {
                assert e.getValue() > 0 : "edge weights must be positive";
                assert seen.contains(e.getKey()) : "edge target must be a vertex";
            }
        }
    }

    /** @return the Vertex with the given label, or null if none exists */
    private Vertex find(String label) {
        for (Vertex v : vertices) {
            if (v.getLabel().equals(label)) {
                return v;
            }
        }
        return null;
    }

    @Override public boolean add(String vertex) {
        if (find(vertex) != null) {
            return false;
        }
        vertices.add(new Vertex(vertex));
        checkRep();
        return true;
    }

    @Override public int set(String source, String target, int weight) {
        assert weight >= 0 : "weight must be nonnegative";
        if (weight > 0) {
            add(source);
            add(target);
        }
        Vertex src = find(source);
        int previous = (src == null) ? 0 : src.getTargets().getOrDefault(target, 0);
        if (weight == 0) {
            if (src != null) {
                src.removeTarget(target);
            }
        } else {
            src.setTarget(target, weight);
        }
        checkRep();
        return previous;
    }

    @Override public boolean remove(String vertex) {
        Vertex v = find(vertex);
        if (v == null) {
            return false;
        }
        vertices.remove(v);
        // remove any incoming edges to the removed vertex
        for (Vertex other : vertices) {
            other.removeTarget(vertex);
        }
        checkRep();
        return true;
    }

    @Override public Set<String> vertices() {
        Set<String> labels = new HashSet<>();
        for (Vertex v : vertices) {
            labels.add(v.getLabel());
        }
        return labels;
    }

    @Override public Map<String, Integer> sources(String target) {
        Map<String, Integer> result = new HashMap<>();
        for (Vertex v : vertices) {
            Integer w = v.getTargets().get(target);
            if (w != null) {
                result.put(v.getLabel(), w);
            }
        }
        return result;
    }

    @Override public Map<String, Integer> targets(String source) {
        Vertex v = find(source);
        return (v == null) ? new HashMap<>() : new HashMap<>(v.getTargets());
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Vertex v : vertices) {
            sb.append(v).append("\n");
        }
        return sb.toString();
    }
}

/**
 * A mutable vertex in a weighted directed graph. Stores the vertex's label and
 * the set of outgoing directed edges as a map from target label to positive
 * edge weight.
 *
 * <p>This class is internal to the rep of ConcreteVerticesGraph.
 */
class Vertex {

    private final String label;
    private final Map<String, Integer> targets = new HashMap<>();

    // Abstraction function:
    //   AF(label, targets) = a graph vertex labeled `label` with an outgoing
    //     directed edge to each key t in `targets`, of weight targets.get(t).
    // Representation invariant:
    //   - label != null
    //   - every weight in `targets` is positive (> 0)
    // Safety from rep exposure:
    //   - label is an immutable String; `targets` is private and getTargets()
    //     returns an unmodifiable view so clients cannot mutate the rep.

    /**
     * Create a vertex with the given label and no outgoing edges.
     *
     * @param label the vertex label, non-null
     */
    Vertex(String label) {
        this.label = label;
        checkRep();
    }

    private void checkRep() {
        assert label != null : "label must be non-null";
        for (int w : targets.values()) {
            assert w > 0 : "weights must be positive";
        }
    }

    /** @return this vertex's label */
    String getLabel() {
        return label;
    }

    /** @return an unmodifiable view of the outgoing edges (target -> weight) */
    Map<String, Integer> getTargets() {
        return java.util.Collections.unmodifiableMap(new HashMap<>(targets));
    }

    /**
     * Add or update an outgoing edge to target with the given positive weight.
     *
     * @param target target vertex label
     * @param weight positive edge weight
     */
    void setTarget(String target, int weight) {
        assert weight > 0;
        targets.put(target, weight);
        checkRep();
    }

    /**
     * Remove the outgoing edge to target if present.
     *
     * @param target target vertex label
     */
    void removeTarget(String target) {
        targets.remove(target);
        checkRep();
    }

    @Override public String toString() {
        return label + " -> " + targets;
    }
}
