/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package poet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.Graph;

/**
 * A graph-based poetry generator.
 * 
 * <p>GraphPoet is initialized with a corpus of text, which it uses to derive a
 * word affinity graph.
 * Vertices in the graph are words. Words are defined as non-empty
 * case-insensitive strings of non-space non-newline characters. They are
 * delimited in the corpus by spaces, newlines, or the ends of the file.
 * Edges in the graph count adjacencies: the number of times "w1" is followed by
 * "w2" in the corpus is the weight of the edge from w1 to w2.
 * 
 * <p>For example, given this corpus:
 * <pre>    Hello, HELLO, hello, goodbye!    </pre>
 * <p>the graph would contain two edges:
 * <ul><li> ("hello,") -> ("hello,")   with weight 2
 *     <li> ("hello,") -> ("goodbye!") with weight 1 </ul>
 * <p>where the vertices represent case-insensitive {@code "hello,"} and
 * {@code "goodbye!"}.
 * 
 * <p>Given an input string, GraphPoet generates a poem by attempting to
 * insert a bridge word between every adjacent pair of words in the input.
 * The bridge word between input words "w1" and "w2" will be some "b" such that
 * w1 -> b -> w2 is a two-edge-long path with maximum-weight weight among all
 * the two-edge-long paths from w1 to w2 in the affinity graph.
 * If there are no such paths, no bridge word is inserted.
 * In the output poem, input words retain their original case, while bridge
 * words are lower case. The whitespace between every word in the poem is a
 * single space.
 * 
 * <p>For example, given this corpus:
 * <pre>    This is a test of the Mugar Omni Theater sound system.    </pre>
 * <p>on this input:
 * <pre>    Test the system.    </pre>
 * <p>the output poem would be:
 * <pre>    Test of the system.    </pre>
 * 
 * <p>PS2 instructions: this is a required ADT class, and you MUST NOT weaken
 * the required specifications. However, you MAY strengthen the specifications
 * and you MAY add additional methods.
 * You MUST use Graph in your rep, but otherwise the implementation of this
 * class is up to you.
 */
public class GraphPoet {
    
    private final Graph<String> graph = Graph.empty();

    // Abstraction function:
    //   AF(graph) = a poetry generator whose word-affinity graph is `graph`,
    //     where each vertex is a lower-cased corpus word and the weight of the
    //     edge w1 -> w2 is the number of times w1 is immediately followed by w2
    //     in the corpus.
    // Representation invariant:
    //   - every vertex label is a non-empty, all-lower-case string with no
    //     whitespace
    //   - all edge weights are positive (guaranteed by Graph)
    // Safety from rep exposure:
    //   - `graph` is private final and never returned or exposed to clients;
    //     poem() only reads from it via the Graph interface.

    /**
     * Create a new poet with the graph from corpus (as described above).
     *
     * @param corpus text file from which to derive the poet's affinity graph
     * @throws IOException if the corpus file cannot be found or read
     */
    public GraphPoet(File corpus) throws IOException {
        String text = new String(Files.readAllBytes(corpus.toPath()));
        List<String> words = splitWords(text);
        // Count adjacency: increment the weight of w[i] -> w[i+1] for each
        // consecutive pair.
        for (int i = 0; i + 1 < words.size(); i++) {
            String w1 = words.get(i).toLowerCase();
            String w2 = words.get(i + 1).toLowerCase();
            int current = graph.targets(w1).getOrDefault(w2, 0);
            graph.set(w1, w2, current + 1);
        }
        checkRep();
    }

    /**
     * Split text into words, where a word is a maximal non-empty run of
     * non-whitespace characters.
     */
    private static List<String> splitWords(String text) {
        List<String> words = new ArrayList<>();
        for (String token : text.split("\\s+")) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }
        return words;
    }

    private void checkRep() {
        for (String v : graph.vertices()) {
            assert !v.isEmpty() : "vertex must be non-empty";
            assert v.equals(v.toLowerCase()) : "vertex must be lower case";
            assert !v.matches(".*\\s.*") : "vertex must not contain whitespace";
        }
    }

    /**
     * Generate a poem.
     *
     * @param input string from which to create the poem
     * @return poem (as described above)
     */
    public String poem(String input) {
        List<String> words = splitWords(input);
        if (words.isEmpty()) {
            return "";
        }
        StringBuilder poem = new StringBuilder();
        poem.append(words.get(0));
        for (int i = 0; i + 1 < words.size(); i++) {
            String w1 = words.get(i).toLowerCase();
            String w2 = words.get(i + 1).toLowerCase();
            String bridge = bestBridge(w1, w2);
            if (bridge != null) {
                poem.append(" ").append(bridge);
            }
            poem.append(" ").append(words.get(i + 1));
        }
        return poem.toString();
    }

    /**
     * Find a maximum-weight bridge word b such that w1 -> b -> w2 is a two-edge
     * path in the affinity graph, i.e. maximizing weight(w1,b) + weight(b,w2).
     *
     * @return the best bridge word (lower case), or null if no such path exists
     */
    private String bestBridge(String w1, String w2) {
        Map<String, Integer> fromW1 = graph.targets(w1);   // b -> weight(w1,b)
        Map<String, Integer> toW2 = graph.sources(w2);     // b -> weight(b,w2)
        String best = null;
        int bestWeight = 0;
        for (Map.Entry<String, Integer> e : fromW1.entrySet()) {
            String b = e.getKey();
            Integer second = toW2.get(b);
            if (second != null) {
                int total = e.getValue() + second;
                if (total > bestWeight) {
                    bestWeight = total;
                    best = b;
                }
            }
        }
        return best;
    }

    @Override public String toString() {
        return "GraphPoet over affinity graph:\n" + graph.toString();
    }
}
