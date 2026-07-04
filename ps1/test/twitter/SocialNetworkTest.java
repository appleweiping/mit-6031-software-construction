/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy
     *
     * guessFollowsGraph(tweets):
     *   tweets:                 empty, no mentions, with mentions
     *   mentions:               single, multiple mentions in a tweet
     *   self-mention:           author mentions self -> not added
     *   case:                   author/mention casing normalized to lower case
     *
     * influencers(followsGraph):
     *   graph:                  empty, single user, multiple users
     *   follower counts:        all zero, distinct counts, ties
     *   ordering:               descending by follower count
     *   node coverage:          followed-only users still appear
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // guessFollowsGraph --------------------------------------------------

    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphMention() {
        Tweet t = new Tweet(1, "alyssa", "hey @bob and @carol", d1);
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("alyssa should be a key", graph.containsKey("alyssa"));
        Set<String> follows = graph.get("alyssa");
        assertTrue("alyssa follows bob", follows.contains("bob"));
        assertTrue("alyssa follows carol", follows.contains("carol"));
    }

    @Test
    public void testGuessFollowsGraphNoSelfFollow() {
        Tweet t = new Tweet(2, "alyssa", "talking to @alyssa herself", d1);
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        // users can't follow themselves
        Set<String> follows = graph.getOrDefault("alyssa", new HashSet<>());
        assertFalse("no self-follow", follows.contains("alyssa"));
    }

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        Tweet t = new Tweet(3, "Alyssa", "hi @BOB", d2);
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("author normalized to lower case", graph.containsKey("alyssa"));
        assertTrue("mention normalized to lower case", graph.get("alyssa").contains("bob"));
    }

    // influencers --------------------------------------------------------

    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersDescendingOrder() {
        // bob is followed by alyssa and carol; carol by alyssa; alyssa by nobody
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alyssa", new HashSet<>(Arrays.asList("bob", "carol")));
        graph.put("carol", new HashSet<>(Arrays.asList("bob")));

        List<String> influencers = SocialNetwork.influencers(graph);

        assertEquals("bob has the most followers", "bob", influencers.get(0));
        // every node appears exactly once
        assertTrue(influencers.containsAll(Arrays.asList("alyssa", "bob", "carol")));
        assertEquals(3, influencers.size());
        // bob (2 followers) must rank above carol (1) and alyssa (0)
        assertTrue(influencers.indexOf("bob") < influencers.indexOf("carol"));
        assertTrue(influencers.indexOf("carol") < influencers.indexOf("alyssa"));
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

}
