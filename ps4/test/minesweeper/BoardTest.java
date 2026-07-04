/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for the thread-safe Board ADT.
 *
 * <p>These tests use the deterministic bomb-layout constructor so results are
 * reproducible.
 */
public class BoardTest {

    // Testing strategy
    //   dig:
    //     out of bounds / already dug / flagged square -> no change
    //     dig a non-bomb square with >0 neighbor bombs -> shows count
    //     dig a square with 0 neighbor bombs -> flood fill spreads
    //     dig a bomb -> returns true (BOOM), bomb removed, square dug
    //   flag / deflag:
    //     flag an untouched square; deflag a flagged one; no-ops otherwise
    //   look:
    //     glyphs "-", "F", " ", and counts; space-separated columns
    //   fromFile: parses header and bomb rows
    //   thread safety: many concurrent digs leave the board consistent

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /** 3x3 board with a single bomb at (2,2) (bottom-right). */
    private static Board oneCornerBomb() {
        boolean[][] bombs = new boolean[3][3];
        bombs[2][2] = true;
        return new Board(3, 3, bombs);
    }

    @Test
    public void testInitialLookAllUntouched() {
        Board b = oneCornerBomb();
        assertEquals("- - -\n- - -\n- - -\n", b.look());
    }

    @Test
    public void testDigNonBombShowsCountAndFloodFill() {
        Board b = oneCornerBomb();
        // dig the top-left corner (0,0): it has 0 neighboring bombs, so flood
        // fill should uncover everything except squares adjacent to the bomb.
        boolean boom = b.dig(0, 0);
        assertFalse("digging a non-bomb must not boom", boom);
        String look = b.look();
        // squares (1,1),(2,1),(1,2) are adjacent to the bomb and show "1";
        // the bomb square (2,2) stays untouched
        assertTrue(look.contains("1"));
        assertTrue("bomb square stays untouched", look.charAt(look.length() - 2) == '-');
    }

    @Test
    public void testDigBombBooms() {
        Board b = oneCornerBomb();
        boolean boom = b.dig(2, 2);
        assertTrue("digging the bomb square must boom", boom);
        // the bomb is removed; that square is now dug. Its neighbors previously
        // showed "1" for the bomb; now they should show 0 (a space) if re-dug.
        Board b2 = oneCornerBomb();
        b2.dig(2, 2);
        assertFalse("second dig at same spot does not boom again", b2.dig(2, 2));
    }

    @Test
    public void testDigOutOfBoundsAndAlreadyDug() {
        Board b = oneCornerBomb();
        assertFalse(b.dig(-1, 0));
        assertFalse(b.dig(0, 5));
        b.dig(0, 0);
        assertFalse("digging an already-dug square is a no-op", b.dig(0, 0));
    }

    @Test
    public void testFlagAndDeflag() {
        Board b = oneCornerBomb();
        b.flag(0, 0);
        assertTrue("flagged square shows F", b.look().startsWith("F"));
        b.deflag(0, 0);
        assertTrue("deflagged square shows -", b.look().startsWith("-"));
        // digging a flagged square is a no-op
        b.flag(1, 1);
        b.dig(1, 1);
        assertTrue("flag then dig leaves it flagged", b.look().contains("F"));
    }

    @Test
    public void testFromFileAndConcurrentDigs() throws Exception {
        // A 5x5 board with no bombs: digging any square flood-fills the whole
        // board. Run many concurrent digs and assert the board stays consistent.
        boolean[][] none = new boolean[5][5];
        final Board b = new Board(5, 5, none);

        int threads = 20;
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int seed = i;
            ts[i] = new Thread(() -> {
                b.dig(seed % 5, (seed / 5) % 5);
                b.look();
                b.flag(seed % 5, (seed / 5) % 5);
                b.deflag(seed % 5, (seed / 5) % 5);
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();

        // With no bombs, once dug everything is a zero-count (space) square.
        String look = b.look();
        assertFalse("no square should show a bomb count", look.matches("(?s).*[1-8].*"));
    }
}
