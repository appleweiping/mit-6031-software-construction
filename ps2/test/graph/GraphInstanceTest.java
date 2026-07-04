/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for instance methods of Graph.
 * 
 * <p>PS2 instructions: you MUST NOT add constructors, fields, or non-@Test
 * methods to this class, or change the spec of {@link #emptyInstance()}.
 * Your tests MUST only obtain Graph instances by calling emptyInstance().
 * Your tests MUST NOT refer to specific concrete implementations.
 */
public abstract class GraphInstanceTest {
    
    // Testing strategy
    //   add(vertex):
    //     vertex already present / not present
    //   set(source, target, weight):
    //     weight == 0 (remove) / weight > 0 (add or update)
    //     edge already present / not present
    //     endpoints already present / created by set
    //     return value: previous weight (0 or positive)
    //   remove(vertex):
    //     vertex present / absent
    //     vertex has incident edges (incoming, outgoing) / none
    //   vertices():
    //     empty / non-empty
    //   sources(target), targets(source):
    //     0, 1, >1 incident edges; nonexistent vertex

    /**
     * Overridden by implementation-specific test classes.
     *
     * @return a new empty graph of the particular implementation being tested
     */
    public abstract Graph<String> emptyInstance();

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testInitialVerticesEmpty() {
        assertEquals("expected new graph to have no vertices",
                Collections.emptySet(), emptyInstance().vertices());
    }

    @Test
    public void testAddNewAndDuplicateVertex() {
        Graph<String> graph = emptyInstance();
        assertTrue("adding a new vertex returns true", graph.add("a"));
        assertFalse("adding a duplicate returns false", graph.add("a"));
        assertEquals(Collections.singleton("a"), graph.vertices());
    }

    @Test
    public void testSetAddsEdgeAndVertices() {
        Graph<String> graph = emptyInstance();
        int prev = graph.set("a", "b", 5);
        assertEquals("no previous edge -> returns 0", 0, prev);
        Set<String> vs = graph.vertices();
        assertTrue(vs.contains("a"));
        assertTrue(vs.contains("b"));
        assertEquals(Integer.valueOf(5), graph.targets("a").get("b"));
        assertEquals(Integer.valueOf(5), graph.sources("b").get("a"));
    }

    @Test
    public void testSetUpdatesExistingEdge() {
        Graph<String> graph = emptyInstance();
        graph.set("a", "b", 5);
        int prev = graph.set("a", "b", 9);
        assertEquals("update returns previous weight", 5, prev);
        assertEquals(Integer.valueOf(9), graph.targets("a").get("b"));
    }

    @Test
    public void testSetZeroRemovesEdge() {
        Graph<String> graph = emptyInstance();
        graph.set("a", "b", 5);
        int prev = graph.set("a", "b", 0);
        assertEquals("removing returns previous weight", 5, prev);
        assertTrue("edge is gone", graph.targets("a").isEmpty());
        assertTrue("vertices remain", graph.vertices().containsAll(java.util.Arrays.asList("a", "b")));
    }

    @Test
    public void testSetZeroOnAbsentEdge() {
        Graph<String> graph = emptyInstance();
        assertEquals("removing a nonexistent edge returns 0", 0, graph.set("a", "b", 0));
    }

    @Test
    public void testRemoveVertexAndIncidentEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("a", "b", 1);
        graph.set("b", "c", 2);
        graph.set("c", "b", 3);
        assertTrue("removing present vertex returns true", graph.remove("b"));
        assertFalse("b is gone", graph.vertices().contains("b"));
        assertTrue("outgoing edge from b removed", graph.targets("b").isEmpty());
        assertTrue("incoming edge to b removed", graph.sources("b").isEmpty());
        // edges not touching b remain intact
        assertTrue(graph.vertices().containsAll(java.util.Arrays.asList("a", "c")));
    }

    @Test
    public void testRemoveAbsentVertex() {
        Graph<String> graph = emptyInstance();
        assertFalse("removing an absent vertex returns false", graph.remove("x"));
    }

    @Test
    public void testSourcesAndTargetsMultiple() {
        Graph<String> graph = emptyInstance();
        graph.set("a", "c", 1);
        graph.set("b", "c", 2);
        graph.set("c", "d", 3);
        graph.set("c", "e", 4);

        Map<String, Integer> sources = graph.sources("c");
        assertEquals(2, sources.size());
        assertEquals(Integer.valueOf(1), sources.get("a"));
        assertEquals(Integer.valueOf(2), sources.get("b"));

        Map<String, Integer> targets = graph.targets("c");
        assertEquals(2, targets.size());
        assertEquals(Integer.valueOf(3), targets.get("d"));
        assertEquals(Integer.valueOf(4), targets.get("e"));
    }

    @Test
    public void testSourcesTargetsOfNonexistentVertex() {
        Graph<String> graph = emptyInstance();
        assertTrue(graph.sources("nope").isEmpty());
        assertTrue(graph.targets("nope").isEmpty());
    }
}
