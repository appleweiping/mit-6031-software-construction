/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Tests for ConcreteEdgesGraph.
 * 
 * This class runs the GraphInstanceTest tests against ConcreteEdgesGraph, as
 * well as tests for that particular implementation.
 * 
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteEdgesGraphTest extends GraphInstanceTest {
    
    /*
     * Provide a ConcreteEdgesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteEdgesGraph();
    }
    
    /*
     * Testing ConcreteEdgesGraph...
     */
    
    // Testing strategy for ConcreteEdgesGraph.toString()
    //   empty graph / graph with vertices only / graph with edges
    //   check that vertices and each edge appear in the output

    @Test
    public void testToStringContainsVerticesAndEdges() {
        Graph<String> graph = new ConcreteEdgesGraph();
        graph.set("a", "b", 3);
        graph.add("c");
        String s = graph.toString();
        assertTrue("mentions vertex a", s.contains("a"));
        assertTrue("mentions vertex c", s.contains("c"));
        assertTrue("mentions the edge weight", s.contains("3"));
        assertTrue("shows edge direction", s.contains("a -> b"));
    }

    @Test
    public void testToStringEmpty() {
        Graph<String> graph = new ConcreteEdgesGraph();
        assertNotNull(graph.toString());
    }

    /*
     * Testing Edge...
     */

    // Testing strategy for Edge
    //   observers: getSource, getTarget, getWeight return constructor values
    //   toString includes source, target, and weight

    @Test
    public void testEdgeObservers() {
        Edge edge = new Edge("x", "y", 7);
        assertEquals("x", edge.getSource());
        assertEquals("y", edge.getTarget());
        assertEquals(7, edge.getWeight());
    }

    @Test
    public void testEdgeToString() {
        Edge edge = new Edge("x", "y", 7);
        String s = edge.toString();
        assertTrue(s.contains("x"));
        assertTrue(s.contains("y"));
        assertTrue(s.contains("7"));
    }
}
