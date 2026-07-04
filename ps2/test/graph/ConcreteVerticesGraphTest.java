/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for ConcreteVerticesGraph.
 * 
 * This class runs the GraphInstanceTest tests against ConcreteVerticesGraph, as
 * well as tests for that particular implementation.
 * 
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteVerticesGraphTest extends GraphInstanceTest {
    
    /*
     * Provide a ConcreteVerticesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteVerticesGraph();
    }
    
    /*
     * Testing ConcreteVerticesGraph...
     */
    
    // Testing strategy for ConcreteVerticesGraph.toString()
    //   empty graph / graph with vertices and edges
    //   check that vertices and their outgoing edges appear

    @Test
    public void testToStringContainsVerticesAndEdges() {
        Graph<String> graph = new ConcreteVerticesGraph();
        graph.set("a", "b", 3);
        String s = graph.toString();
        assertTrue("mentions vertex a", s.contains("a"));
        assertTrue("mentions target b", s.contains("b"));
        assertTrue("mentions weight", s.contains("3"));
    }

    /*
     * Testing Vertex...
     */

    // Testing strategy for Vertex
    //   getLabel returns the label
    //   setTarget adds/updates an outgoing edge; getTargets reflects it
    //   removeTarget deletes an outgoing edge
    //   getTargets returns an unmodifiable view (mutation throws)

    @Test
    public void testVertexLabelAndTargets() {
        Vertex v = new Vertex("a");
        assertEquals("a", v.getLabel());
        assertTrue(v.getTargets().isEmpty());
        v.setTarget("b", 4);
        assertEquals(Integer.valueOf(4), v.getTargets().get("b"));
        v.setTarget("b", 9);   // update
        assertEquals(Integer.valueOf(9), v.getTargets().get("b"));
        v.removeTarget("b");
        assertTrue(v.getTargets().isEmpty());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testVertexTargetsUnmodifiable() {
        Vertex v = new Vertex("a");
        v.setTarget("b", 1);
        v.getTargets().put("c", 2); // must throw: no rep exposure
    }

    @Test
    public void testVertexToString() {
        Vertex v = new Vertex("a");
        v.setTarget("b", 5);
        String s = v.toString();
        assertTrue(s.contains("a"));
        assertTrue(s.contains("b"));
        assertTrue(s.contains("5"));
    }
}
