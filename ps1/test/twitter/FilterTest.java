/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy
     *
     * writtenBy(tweets, username):
     *   result size:            0, 1, >1 matches
     *   username case:          exact case, different case (case-insensitive)
     *   order:                  results preserve input order
     *
     * inTimespan(tweets, timespan):
     *   matches:                0, some, all
     *   endpoints:              tweet exactly at start / end (inclusive)
     *   order:                  results preserve input order
     *
     * containing(tweets, words):
     *   matches:                0, some, all
     *   words list:             single word, multiple words (OR semantics)
     *   case:                   different case matches
     *   whole-word:             substring of a word does NOT match
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // writtenBy ----------------------------------------------------------

    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");

        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByNoMatch() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "nobody");

        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByCaseInsensitiveAndOrder() {
        Tweet t3 = new Tweet(3, "ALYSSA", "another from alyssa", d3);
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, t3), "Alyssa");

        assertEquals("expected two matches ignoring case", 2, writtenBy.size());
        assertEquals("expected input order preserved", tweet1, writtenBy.get(0));
        assertEquals(t3, writtenBy.get(1));
    }

    // inTimespan ---------------------------------------------------------

    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));

        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }

    @Test
    public void testInTimespanInclusiveEndpoints() {
        // timespan exactly [d1, d2]: both endpoints must be included
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d1, d2));

        assertEquals("both endpoint tweets are inside", 2, inTimespan.size());
    }

    @Test
    public void testInTimespanNoMatch() {
        Instant s = Instant.parse("2016-02-18T09:00:00Z");
        Instant e = Instant.parse("2016-02-18T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(s, e));

        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    // containing ---------------------------------------------------------

    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));

        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    @Test
    public void testContainingCaseInsensitiveMultipleWords() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("RIVEST", "hype"));

        assertEquals("both tweets mention rivest (case-insensitive)", 2, containing.size());
    }

    @Test
    public void testContainingNoMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("zzz"));

        assertTrue("expected empty list", containing.isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}
