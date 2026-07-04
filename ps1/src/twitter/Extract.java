/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        // A Timespan requires start <= end; when the list is empty there is no
        // natural interval, so we return a zero-length span at the epoch (the
        // spec leaves the empty case unconstrained but a valid Timespan must be
        // returned).
        if (tweets.isEmpty()) {
            return new Timespan(Instant.EPOCH, Instant.EPOCH);
        }
        Instant min = null;
        Instant max = null;
        for (Tweet tweet : tweets) {
            Instant t = tweet.getTimestamp();
            if (min == null || t.isBefore(min)) {
                min = t;
            }
            if (max == null || t.isAfter(max)) {
                max = t;
            }
        }
        return new Timespan(min, max);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        // A username character is a letter, digit, underscore, or hyphen. A
        // mention is "@" + username where the "@" is NOT immediately preceded
        // by a username character (so bitdiddle@mit.edu is not a mention of
        // "mit") and the username is NOT immediately followed by a username
        // character. We enforce those boundaries with look-around assertions.
        final Pattern mention = Pattern.compile(
                "(?<![A-Za-z0-9_-])@([A-Za-z0-9_-]+)(?![A-Za-z0-9_-])");
        // Preserve insertion order for deterministic output; usernames are
        // case-insensitive so we canonicalize to lower case and de-duplicate.
        Set<String> users = new LinkedHashSet<>();
        for (Tweet tweet : tweets) {
            Matcher m = mention.matcher(tweet.getText());
            while (m.find()) {
                users.add(m.group(1).toLowerCase());
            }
        }
        return users;
    }

}
