/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SocialNetwork provides methods that operate on a social network.
 * 
 * A social network is represented by a Map<String, Set<String>> where map[A] is
 * the set of people that person A follows on Twitter, and all people are
 * represented by their Twitter usernames. Users can't follow themselves. If A
 * doesn't follow anybody, then map[A] may be the empty set, or A may not even exist
 * as a key in the map; this is true even if A is followed by other people in the network.
 * Twitter usernames are not case sensitive, so "ernie" is the same as "ERNie".
 * A username should appear at most once as a key in the map or in any given
 * map[A] set.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class SocialNetwork {

    /**
     * Guess who might follow whom, from evidence found in tweets.
     * 
     * @param tweets
     *            a list of tweets providing the evidence, not modified by this
     *            method.
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets.
     *         One kind of evidence that Ernie follows Bert is if Ernie
     *         @-mentions Bert in a tweet. This must be implemented. Other kinds
     *         of evidence may be used at the implementor's discretion.
     *         All the Twitter usernames in the returned social network must be
     *         either authors or @-mentions in the list of tweets.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        // Evidence rule: if author A @-mentions user B (A != B), infer A follows
        // B. All usernames are canonicalized to lower case. Authors who mention
        // nobody are simply left out of the graph (an omitted key is allowed by
        // the spec and keeps the graph minimal).
        Map<String, Set<String>> graph = new HashMap<>();
        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase();
            Set<String> mentioned = Extract.getMentionedUsers(List.of(tweet));
            for (String other : mentioned) {
                if (!other.equals(author)) {  // users can't follow themselves
                    graph.computeIfAbsent(author, k -> new HashSet<>()).add(other);
                }
            }
        }
        return graph;
    }

    /**
     * Find the people in a social network who have the greatest influence, in
     * the sense that they have the most followers.
     * 
     * @param followsGraph
     *            a social network (as defined above)
     * @return a list of all distinct Twitter usernames in followsGraph, in
     *         descending order of follower count.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        // Count how many people follow each user. Every username that appears
        // anywhere in the graph (as a follower key or as a followed value) is a
        // distinct node and must appear in the result exactly once.
        Map<String, Integer> followerCount = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : followsGraph.entrySet()) {
            followerCount.putIfAbsent(entry.getKey(), 0);
            for (String followed : entry.getValue()) {
                followerCount.merge(followed, 1, Integer::sum);
            }
        }
        List<String> influencers = new ArrayList<>(followerCount.keySet());
        // Sort by follower count descending; ties keep an arbitrary but stable
        // order (the spec does not constrain ordering among equal counts).
        influencers.sort((a, b) -> Integer.compare(followerCount.get(b), followerCount.get(a)));
        return influencers;
    }

}
