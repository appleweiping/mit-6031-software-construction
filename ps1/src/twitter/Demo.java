/* Copyright (c) 2026 appleweiping. MIT License.
 *
 * Auxiliary end-to-end demo (not part of the graded skeleton). Unlike Main,
 * which fetches from a MIT sample server that is no longer reliably reachable,
 * this runs the full Extract -> SocialNetwork -> influencers pipeline on a
 * small fixed set of tweets so the result can be captured as evidence offline.
 */
package twitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Demo {

    public static void main(String[] args) {
        Instant t0 = Instant.parse("2016-02-17T10:00:00Z");
        List<Tweet> tweets = Arrays.asList(
                new Tweet(1, "alyssa", "great talk by @rivest today, thanks @ben!", t0.plusSeconds(0)),
                new Tweet(2, "ben", "agreed @rivest was excellent, cc @alyssa", t0.plusSeconds(600)),
                new Tweet(3, "carol", "wish i could have seen @rivest speak", t0.plusSeconds(1200)),
                new Tweet(4, "rivest", "thanks everyone! email me at rivest@mit.edu", t0.plusSeconds(1800)),
                new Tweet(5, "dave", "shout out to @ben and @carol for organizing", t0.plusSeconds(2400)));

        System.out.println("tweets: " + tweets.size());

        Timespan span = Extract.getTimespan(tweets);
        System.out.println("timespan: " + span.getStart() + " .. " + span.getEnd());

        Set<String> mentioned = Extract.getMentionedUsers(tweets);
        System.out.println("mentioned users (" + mentioned.size() + "): " + mentioned);
        // note: rivest@mit.edu is NOT counted as a mention of "mit"

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        System.out.println("follows graph (" + graph.size() + " authors with mentions):");
        for (Map.Entry<String, Set<String>> e : graph.entrySet()) {
            System.out.println("  " + e.getKey() + " -> " + e.getValue());
        }

        List<String> influencers = SocialNetwork.influencers(graph);
        System.out.println("influencers (most followed first): " + influencers);
    }
}
