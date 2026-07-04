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
public class ConcreteEdgesGraph implements Graph<String> {

    private final Set<String> vertices = new HashSet<>();
    private final List<Edge> edges = new ArrayList<>();

    // Abstraction function:
    //   AF(vertices, edges) = a weighted directed graph whose vertex set is
    //     `vertices`, and which has a directed edge from e.source() to
    //     e.target() with weight e.weight() for each Edge e in `edges`.
    // Representation invariant:
    //   - every edge's source and target are members of `vertices`
    //   - all edge weights are positive (> 0)
    //   - there is at most one edge for any (source, target) ordered pair
    // Safety from rep exposure:
    //   - `vertices` and `edges` are private final and never returned directly;
    //     vertices() returns a fresh HashSet copy, and sources()/targets()
    //     return fresh maps built from immutable String keys and int values.
    //   - Edge is immutable, so sharing Edge references would be safe anyway.

    /**
     * Create an empty ConcreteEdgesGraph.
     */
    public ConcreteEdgesGraph() {
        checkRep();
    }

    private void checkRep() {
        for (Edge edge : edges) {
            assert edge.getWeight() > 0 : "edge weights must be positive";
            assert vertices.contains(edge.getSource()) : "edge source must be a vertex";
            assert vertices.contains(edge.getTarget()) : "edge target must be a vertex";
        }
        // uniqueness of (source, target) is maintained by set()
    }

    @Override public boolean add(String vertex) {
        boolean added = vertices.add(vertex);
        checkRep();
        return added;
    }

    @Override public int set(String source, String target, int weight) {
        assert weight >= 0 : "weight must be nonnegative";
        // Find any existing edge for this ordered pair.
        int existingIndex = -1;
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).getSource().equals(source)
                    && edges.get(i).getTarget().equals(target)) {
                existingIndex = i;
                break;
            }
        }
        int previousWeight = existingIndex >= 0 ? edges.get(existingIndex).getWeight() : 0;

        if (weight == 0) {
            // remove the edge if present
            if (existingIndex >= 0) {
                edges.remove(existingIndex);
            }
        } else {
            // ensure endpoints exist
            vertices.add(source);
            vertices.add(target);
            Edge edge = new Edge(source, target, weight);
            if (existingIndex >= 0) {
                edges.set(existingIndex, edge);
            } else {
                edges.add(edge);
            }
        }
        checkRep();
        return previousWeight;
    }

    @Override public boolean remove(String vertex) {
        if (!vertices.contains(vertex)) {
            return false;
        }
        vertices.remove(vertex);
        edges.removeIf(e -> e.getSource().equals(vertex) || e.getTarget().equals(vertex));
        checkRep();
        return true;
    }

    @Override public Set<String> vertices() {
        return new HashSet<>(vertices);
    }

    @Override public Map<String, Integer> sources(String target) {
        Map<String, Integer> result = new HashMap<>();
        for (Edge edge : edges) {
            if (edge.getTarget().equals(target)) {
                result.put(edge.getSource(), edge.getWeight());
            }
        }
        return result;
    }

    @Override public Map<String, Integer> targets(String source) {
        Map<String, Integer> result = new HashMap<>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(source)) {
                result.put(edge.getTarget(), edge.getWeight());
            }
        }
        return result;
    }

    /**
     * @return a string listing the vertices and, on separate lines, each
     *         directed weighted edge as "source -> target (weight)".
     */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("vertices: ").append(vertices).append("\n");
        for (Edge edge : edges) {
            sb.append(edge).append("\n");
        }
        return sb.toString();
    }
}

/**
 * An immutable, weighted, directed edge between two labeled vertices.
 * Represents a single directed connection from {@code source} to {@code target}
 * carrying a positive integer {@code weight}.
 *
 * <p>This class is internal to the rep of ConcreteEdgesGraph.
 */
class Edge {

    private final String source;
    private final String target;
    private final int weight;

    // Abstraction function:
    //   AF(source, target, weight) = a directed edge from vertex `source` to
    //     vertex `target` with weight `weight`.
    // Representation invariant:
    //   - source != null, target != null
    //   - weight > 0
    // Safety from rep exposure:
    //   - all fields are private, final, and of immutable types (String, int),
    //     so no rep can leak or be mutated.

    /**
     * Create an edge from source to target with the given positive weight.
     *
     * @param source label of the source vertex, non-null
     * @param target label of the target vertex, non-null
     * @param weight positive edge weight
     */
    Edge(String source, String target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        checkRep();
    }

    private void checkRep() {
        assert source != null : "source must be non-null";
        assert target != null : "target must be non-null";
        assert weight > 0 : "weight must be positive";
    }

    /** @return the source vertex label */
    String getSource() {
        return source;
    }

    /** @return the target vertex label */
    String getTarget() {
        return target;
    }

    /** @return the (positive) weight of this edge */
    int getWeight() {
        return weight;
    }

    @Override public String toString() {
        return source + " -> " + target + " (" + weight + ")";
    }
}
