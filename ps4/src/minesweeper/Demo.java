/* Copyright (c) 2026 appleweiping. MIT License.
 *
 * Auxiliary end-to-end demo (not part of the graded skeleton). Starts a
 * MinesweeperServer on an ephemeral port with a fixed board, connects a client,
 * runs a scripted session (LOOK / DIG flood-fill / FLAG / DIG-bomb BOOM), and
 * prints the full transcript so it can be captured as evidence.
 */
package minesweeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import minesweeper.server.MinesweeperServer;

public class Demo {

    public static void main(String[] args) throws Exception {
        // 5x5 board, single bomb at bottom-right (4,4).
        boolean[][] bombs = new boolean[5][5];
        bombs[4][4] = true;
        Board board = new Board(5, 5, bombs);

        int port;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        MinesweeperServer server = new MinesweeperServer(port, false, board);
        Thread t = new Thread(() -> {
            try {
                server.serve();
            } catch (IOException ignore) {
            }
        });
        t.setDaemon(true);
        t.start();

        try (Socket sock = new Socket("localhost", port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            System.out.println("S: " + in.readLine());          // HELLO

            send(out, "look");
            printBoard(in, 5);

            send(out, "dig 0 0");   // 0-neighbor -> floods most of the board
            printBoard(in, 5);

            send(out, "flag 3 3");  // flag a square near the bomb
            printBoard(in, 5);

            send(out, "dig 4 4");   // the bomb!
            System.out.println("S: " + in.readLine());          // BOOM!
            String after = in.readLine();
            System.out.println("S: " + (after == null ? "<disconnected>" : after));
        }
    }

    private static void send(PrintWriter out, String cmd) {
        System.out.println("C: " + cmd);
        out.println(cmd);
    }

    private static void printBoard(BufferedReader in, int rows) throws IOException {
        for (int i = 0; i < rows; i++) {
            System.out.println("S: " + in.readLine());
        }
    }
}
