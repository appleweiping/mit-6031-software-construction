/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package poet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

/**
 * Tests for GraphPoet.
 */
public class GraphPoetTest {

    // Testing strategy
    //   corpus:
    //     single line / multiple lines
    //     word adjacency: bridge exists / no bridge / multiple candidate
    //       bridges (pick maximum weight)
    //   poem input:
    //     empty / single word (no bridges possible) / multiple words
    //     case: input words retain case, bridge words are lower case
    //     punctuation attached to words is part of the word

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /** Write corpus text to a temp file and return it. */
    private static File corpusFile(String text) throws IOException {
        File f = File.createTempFile("corpus", ".txt");
        f.deleteOnExit();
        Files.write(f.toPath(), text.getBytes());
        return f;
    }

    @Test
    public void testMugarExample() throws IOException {
        // The canonical example from the spec.
        GraphPoet poet = new GraphPoet(
                corpusFile("This is a test of the Mugar Omni Theater sound system."));
        String poem = poet.poem("Test the system.");
        assertEquals("Test of the system.", poem);
    }

    @Test
    public void testProvidedCorpusFile() throws IOException {
        // Use the corpus file shipped with the pset. Resolve it relative to the
        // ps2 directory whether the test is launched from the repo root or from
        // ps2 itself, so this does not depend on the working directory.
        File corpus = new File("ps2/src/poet/mugar-omni-theater.txt");
        if (!corpus.exists()) {
            corpus = new File("src/poet/mugar-omni-theater.txt");
        }
        GraphPoet poet = new GraphPoet(corpus);
        String poem = poet.poem("Test the system.");
        assertEquals("Test of the system.", poem);
    }

    @Test
    public void testNoBridgeInserted() throws IOException {
        GraphPoet poet = new GraphPoet(corpusFile("cat dog"));
        // "hello world" has no bridge in this corpus; output unchanged
        assertEquals("hello world", poet.poem("hello world"));
    }

    @Test
    public void testBridgeIsLowerCaseInputPreservesCase() throws IOException {
        GraphPoet poet = new GraphPoet(corpusFile("to explore strange new worlds"));
        // input words keep their case; the inserted bridge "explore" is lower case
        String poem = poet.poem("To Strange");
        assertEquals("To explore Strange", poem);
    }

    @Test
    public void testMaxWeightBridgeChosen() throws IOException {
        // "a x b" appears twice, "a y b" once => bridge from a to b is "x".
        GraphPoet poet = new GraphPoet(corpusFile("a x b a x b a y b"));
        String poem = poet.poem("a b");
        assertEquals("a x b", poem);
    }

    @Test
    public void testEmptyAndSingleWordInput() throws IOException {
        GraphPoet poet = new GraphPoet(corpusFile("hello there world"));
        assertEquals("", poet.poem(""));
        assertEquals("hello", poet.poem("hello"));
    }
}
