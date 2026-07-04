/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A mutable, thread-safe Minesweeper board.
 *
 * <p>A board is a grid of {@code sizeX} columns by {@code sizeY} rows. Each
 * square is in one of three states -- UNTOUCHED, FLAGGED, or DUG -- and either
 * contains a bomb or not. The board supports the operations used by the
 * Minesweeper protocol: look, dig, flag, and deflag.
 *
 * <p>Coordinates are (x, y) with x the column (0-based, left to right) and y
 * the row (0-based, top to bottom).
 */
public class Board {

    private enum State { UNTOUCHED, FLAGGED, DUG }

    private final int sizeX;
    private final int sizeY;
    private final boolean[][] bomb;    // bomb[y][x] == true iff a bomb is there
    private final State[][] state;     // state[y][x]

    // Abstraction function:
    //   AF(sizeX, sizeY, bomb, state) = a Minesweeper board of sizeX columns and
    //     sizeY rows, where the square at column x, row y has a bomb iff
    //     bomb[y][x], and is untouched/flagged/dug according to state[y][x].
    // Representation invariant:
    //   - bomb and state are sizeY-by-sizeX rectangular arrays
    //   - no square is both DUG and contains a bomb (digging a bomb removes it)
    // Safety from rep exposure:
    //   - all fields are private final; the arrays are never returned. Callers
    //     only observe the board through toString()/look() which returns a String.
    // Thread safety:
    //   - the board is a monitor: every public method is synchronized on `this`,
    //     so all accesses to the mutable rep are serialized. No rep component is
    //     ever leaked to a client, so there is no way to bypass the lock.

    /**
     * Create a board of the given size with each square independently containing
     * a bomb with probability 0.25.
     *
     * @param sizeX number of columns, > 0
     * @param sizeY number of rows, > 0
     */
    public Board(int sizeX, int sizeY) {
        this(sizeX, sizeY, randomBombs(sizeX, sizeY, new Random()));
    }

    /**
     * Create a board of the given size using a supplied bomb layout.
     *
     * @param sizeX number of columns, > 0
     * @param sizeY number of rows, > 0
     * @param bombs bombs[y][x] == true iff the square at (x, y) has a bomb;
     *              must be a sizeY-by-sizeX array
     */
    public Board(int sizeX, int sizeY, boolean[][] bombs) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.bomb = new boolean[sizeY][sizeX];
        this.state = new State[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                this.bomb[y][x] = bombs[y][x];
                this.state[y][x] = State.UNTOUCHED;
            }
        }
        checkRep();
    }

    private static boolean[][] randomBombs(int sizeX, int sizeY, Random rng) {
        boolean[][] bombs = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                bombs[y][x] = rng.nextDouble() < 0.25;
            }
        }
        return bombs;
    }

    /**
     * Load a board from a file in the PS4 board-file format:
     * <pre>
     *   FILE ::= BOARD LINE+
     *   BOARD ::= X SPACE Y NEWLINE
     *   LINE ::= (VAL SPACE)* VAL NEWLINE
     *   VAL ::= 0 | 1
     * </pre>
     *
     * @param file the board file
     * @return a new Board initialized from the file
     * @throws IOException if the file cannot be read or is malformed
     */
    public static Board fromFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath())) {
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new IOException("empty board file");
        }
        String[] header = lines.get(0).trim().split("\\s+");
        int sizeX = Integer.parseInt(header[0]);
        int sizeY = Integer.parseInt(header[1]);
        if (lines.size() < 1 + sizeY) {
            throw new IOException("board file has too few rows");
        }
        boolean[][] bombs = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            String[] vals = lines.get(1 + y).trim().split("\\s+");
            if (vals.length != sizeX) {
                throw new IOException("row " + y + " has wrong number of columns");
            }
            for (int x = 0; x < sizeX; x++) {
                int v = Integer.parseInt(vals[x]);
                if (v != 0 && v != 1) {
                    throw new IOException("board values must be 0 or 1");
                }
                bombs[y][x] = (v == 1);
            }
        }
        return new Board(sizeX, sizeY, bombs);
    }

    private void checkRep() {
        assert bomb.length == sizeY;
        assert state.length == sizeY;
        for (int y = 0; y < sizeY; y++) {
            assert bomb[y].length == sizeX;
            assert state[y].length == sizeX;
            for (int x = 0; x < sizeX; x++) {
                assert !(state[y][x] == State.DUG && bomb[y][x])
                        : "a dug square must not contain a bomb";
            }
        }
    }

    /** @return the number of columns */
    public int getSizeX() {
        return sizeX;
    }

    /** @return the number of rows */
    public int getSizeY() {
        return sizeY;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }

    /** @return the number of bombs in the eight squares neighboring (x, y). */
    private int neighborBombs(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx, ny = y + dy;
                if (inBounds(nx, ny) && bomb[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Dig the square at (x, y).
     *
     * <p>If (x, y) is out of bounds or not UNTOUCHED, the board is unchanged.
     * Otherwise the square becomes DUG. If it contained a bomb, the bomb is
     * removed (and this returns true to signal a BOOM); the caller is
     * responsible for the BOOM message. If the dug square has zero neighboring
     * bombs, dig recursively flood-fills its untouched neighbors.
     *
     * @param x column
     * @param y row
     * @return true if a bomb was detonated by this dig, false otherwise
     */
    public synchronized boolean dig(int x, int y) {
        if (!inBounds(x, y) || state[y][x] != State.UNTOUCHED) {
            return false;
        }
        boolean hitBomb = bomb[y][x];
        state[y][x] = State.DUG;
        if (hitBomb) {
            bomb[y][x] = false;   // remove the detonated bomb
        }
        // Flood-fill: if this square (now, with any bomb removed) has no
        // neighboring bombs, recursively dig its untouched neighbors.
        floodFill(x, y);
        checkRep();
        return hitBomb;
    }

    private void floodFill(int x, int y) {
        if (neighborBombs(x, y) != 0) {
            return;
        }
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx, ny = y + dy;
                if (inBounds(nx, ny) && state[ny][nx] == State.UNTOUCHED) {
                    state[ny][nx] = State.DUG;
                    floodFill(nx, ny);
                }
            }
        }
    }

    /**
     * Flag the square at (x, y) if it is in bounds and UNTOUCHED; otherwise no
     * change.
     *
     * @param x column
     * @param y row
     */
    public synchronized void flag(int x, int y) {
        if (inBounds(x, y) && state[y][x] == State.UNTOUCHED) {
            state[y][x] = State.FLAGGED;
        }
    }

    /**
     * Deflag the square at (x, y) if it is in bounds and FLAGGED; otherwise no
     * change.
     *
     * @param x column
     * @param y row
     */
    public synchronized void deflag(int x, int y) {
        if (inBounds(x, y) && state[y][x] == State.FLAGGED) {
            state[y][x] = State.UNTOUCHED;
        }
    }

    /**
     * @return the BOARD message for the current board state, as defined by the
     *         protocol: rows of space-separated square glyphs. Untouched squares
     *         are "-", flagged "F", dug squares with zero neighboring bombs are
     *         " ", and dug squares with N (1-8) neighboring bombs are "N".
     */
    public synchronized String look() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                if (x > 0) {
                    sb.append(' ');
                }
                sb.append(glyph(x, y));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private char glyph(int x, int y) {
        switch (state[y][x]) {
            case UNTOUCHED: return '-';
            case FLAGGED:   return 'F';
            case DUG:
                int n = neighborBombs(x, y);
                return n == 0 ? ' ' : (char) ('0' + n);
            default:
                throw new AssertionError("unreachable");
        }
    }

    @Override public synchronized String toString() {
        return look();
    }
}
