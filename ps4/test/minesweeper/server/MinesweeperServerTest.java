/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import minesweeper.Board;

/**
 * Integration tests that drive a real MinesweeperServer over a TCP socket.
 */
public class MinesweeperServerTest {

    // Testing strategy
    //   connect -> receive HELLO with correct dimensions and player count
    //   look -> BOARD message of the right height
    //   flag / deflag round-trip visible in BOARD
    //   dig a known bomb -> BOOM! and (non-debug) disconnect
    //   dig a known bomb in debug mode -> BOOM stays connected (still usable)
    //   two concurrent clients share one board

    /** Find a free port for the test server. */
    private static int freePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    /** Start a server with the given board and debug flag; return its port. */
    private static int startServer(Board board, boolean debug) throws IOException, InterruptedException {
        int port = freePort();
        MinesweeperServer server = new MinesweeperServer(port, debug, board);
        Thread t = new Thread(() -> {
            try {
                server.serve();
            } catch (IOException ignore) {
            }
        });
        t.setDaemon(true);
        t.start();
        // give the server a moment to start accepting; probe with a clean
        // connect + bye so we don't trigger a noisy abrupt-disconnect log
        for (int i = 0; i < 50; i++) {
            try (Socket probe = new Socket("localhost", port)) {
                BufferedReader pin = new BufferedReader(new InputStreamReader(probe.getInputStream()));
                PrintWriter pout = new PrintWriter(probe.getOutputStream(), true);
                pin.readLine();      // HELLO
                pout.println("bye"); // clean disconnect
                break;
            } catch (IOException e) {
                Thread.sleep(20);
            }
        }
        return port;
    }

    /** 3x3 board with a single bomb at (0,0). */
    private static Board bombAtOrigin() {
        boolean[][] bombs = new boolean[3][3];
        bombs[0][0] = true;
        return new Board(3, 3, bombs);
    }

    @Test
    public void testHelloAndLook() throws Exception {
        int port = startServer(bombAtOrigin(), false);
        try (Socket sock = new Socket("localhost", port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            String hello = in.readLine();
            assertTrue("HELLO greeting", hello.startsWith("Welcome to Minesweeper."));
            assertTrue("HELLO reports 3 columns", hello.contains("3 columns"));
            assertTrue("HELLO reports 3 rows", hello.contains("3 rows"));
            assertTrue("HELLO reports player count", hello.contains("Players: 1 including you"));

            out.println("look");
            // read exactly 3 board rows
            for (int y = 0; y < 3; y++) {
                assertEquals("- - -", in.readLine());
            }
        }
    }

    @Test
    public void testFlagShowsInBoard() throws Exception {
        int port = startServer(bombAtOrigin(), false);
        try (Socket sock = new Socket("localhost", port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            in.readLine(); // HELLO

            out.println("flag 1 1");
            String row0 = in.readLine();
            String row1 = in.readLine();
            in.readLine();
            assertEquals("- - -", row0);
            assertEquals("center square flagged", "- F -", row1);
        }
    }

    @Test
    public void testDigBombBoomsAndDisconnects() throws Exception {
        int port = startServer(bombAtOrigin(), false);
        try (Socket sock = new Socket("localhost", port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            in.readLine(); // HELLO

            out.println("dig 0 0"); // the bomb
            assertEquals("BOOM!", in.readLine());
            // in non-debug mode the server disconnects: the stream reaches EOF
            assertNull("server should disconnect after BOOM (non-debug)", in.readLine());
        }
    }

    @Test
    public void testDigBombDebugModeStaysConnected() throws Exception {
        int port = startServer(bombAtOrigin(), true);
        try (Socket sock = new Socket("localhost", port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            in.readLine(); // HELLO

            out.println("dig 0 0"); // the bomb; debug mode -> returns BOARD, no disconnect
            // in debug mode the dig returns the board (3 rows), not a disconnect
            String r0 = in.readLine();
            assertNotNull("debug mode keeps the connection open", r0);
            in.readLine();
            in.readLine();
            // connection still usable
            out.println("look");
            assertNotNull(in.readLine());
        }
    }

    @Test
    public void testTwoClientsShareBoard() throws Exception {
        int port = startServer(bombAtOrigin(), false);
        try (Socket a = new Socket("localhost", port);
             Socket b = new Socket("localhost", port)) {
            BufferedReader inA = new BufferedReader(new InputStreamReader(a.getInputStream()));
            PrintWriter outA = new PrintWriter(a.getOutputStream(), true);
            BufferedReader inB = new BufferedReader(new InputStreamReader(b.getInputStream()));
            PrintWriter outB = new PrintWriter(b.getOutputStream(), true);
            inA.readLine();
            inB.readLine();

            // client A flags (2,2); client B should observe it
            outA.println("flag 2 2");
            inA.readLine(); inA.readLine(); inA.readLine();

            outB.println("look");
            inB.readLine();
            inB.readLine();
            String rowB2 = inB.readLine();
            assertEquals("shared board shows A's flag to B", "- - F", rowB2);
        }
    }
}
