/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy
     *
     * getTimespan(tweets):
     *   tweets.size():           1, 2, >2
     *   timestamp ordering:      already ascending, descending, unordered
     *   duplicate timestamps:    all same instant, some same
     *
     * getMentionedUsers(tweets):
     *   number of mentions:      0, 1, >1 (in one tweet, across tweets)
     *   mention position:        start of text, middle, end
     *   boundary handling:       preceded/followed by username char (not a
     *                            mention), e.g. email address bob@mit.edu
     *   case:                    same user mentioned with different casing
     *                            -> deduplicated, lower-cased
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

    // getTimespan --------------------------------------------------------

    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanSingleTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));

        assertEquals("expected start = end for single tweet", d1, timespan.getStart());
        assertEquals("expected end = start for single tweet", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanUnorderedTweets() {
        Tweet later = new Tweet(3, "carol", "later tweet", d3);
        // pass them out of chronological order to check min/max, not first/last
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, later, tweet1));

        assertEquals("expected earliest start", d1, timespan.getStart());
        assertEquals("expected latest end", d3, timespan.getEnd());
    }

    @Test
    public void testGetTimespanSameTimestamps() {
        Tweet a = new Tweet(4, "a", "one", d2);
        Tweet b = new Tweet(5, "b", "two", d2);
        Timespan timespan = Extract.getTimespan(Arrays.asList(a, b));

        assertEquals(d2, timespan.getStart());
        assertEquals(d2, timespan.getEnd());
    }

    // getMentionedUsers --------------------------------------------------

    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersSingleMention() {
        Tweet t = new Tweet(6, "alyssa", "hey @bob how are you", d1);
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(t));

        assertEquals("expected one mention", 1, users.size());
        assertTrue("expected lower-cased username", users.contains("bob"));
    }

    @Test
    public void testGetMentionedUsersMultipleAndCaseInsensitive() {
        Tweet t1 = new Tweet(7, "alyssa", "@Bob and @carol", d1);
        Tweet t2 = new Tweet(8, "ben", "cc @BOB again", d2);
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(t1, t2));

        assertEquals("expected deduplicated set", 2, users.size());
        assertTrue(users.contains("bob"));
        assertTrue(users.contains("carol"));
    }

    @Test
    public void testGetMentionedUsersEmailNotMention() {
        Tweet t = new Tweet(9, "alyssa", "write to bitdiddle@mit.edu please", d1);
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(t));

        assertTrue("email address must not be treated as a mention", users.isEmpty());
    }

    @Test
    public void testGetMentionedUsersFollowedByUsernameChar() {
        // "@foo-" is a valid username "foo-"; but "@foo bar" where '@foo' is
        // followed by a space is a clean mention. Check a trailing valid char.
        Tweet t = new Tweet(10, "alyssa", "reach @alice at the end", d1);
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(t));

        assertTrue(users.contains("alice"));
        assertEquals(1, users.size());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
