# MIT 6.031 (6.005) Software Construction — Problem Sets

> Full solutions to the five Software Construction problem sets — an independent,
> from-skeleton implementation of **6.031 / 6.005 — Software Construction** (MIT),
> part of a [csdiy.wiki](https://csdiy.wiki/) full-catalog build.

![status](https://img.shields.io/badge/status-complete-brightgreen)
![language](https://img.shields.io/badge/Java-JDK%2021-informational)
![tests](https://img.shields.io/badge/JUnit-109%20passing-brightgreen)
![license](https://img.shields.io/badge/license-MIT-blue)

## Overview

6.031 (formerly 6.005) is MIT's course on writing software that is **safe from
bugs, easy to understand, and ready for change** — the discipline of
specifications, abstract data types (ADTs), test-first programming, immutability,
recursive data types, grammars/parsers, and concurrency.

This repository implements all five problem sets of the **Spring 2016 (6.005)**
edition — the canonical Java version whose starter code is archived on MIT
OpenCourseWare — starting from the official skeletons and filling in every `TODO`
to spec, with proper `checkRep` / abstraction functions / rep-invariant
documentation and comprehensive partitioned JUnit test suites.

> **Version note.** csdiy.wiki links to the current 6.031 site, whose recent
> years (sp22+) are TypeScript with renamed psets (Flashcards, Cityscape,
> Memely, Memory Scramble). The classic psets requested here — turtle graphics,
> tweet analysis, poetic-walks graph ADT, expression parser, and a multiplayer
> game — are the **Java** era. This build targets the fully-public, archived
> **6.005 Spring 2016** version (`ocw.mit.edu/ans7870/6/6.005/s16`), which
> contains exactly those five assignments, and uses **JDK 21 + JUnit 4** as the
> factory spec directs for Java-based years.

## Results (measured on Windows, JDK 21 Temurin, CPU)

Every problem set is verified with its own JUnit suite (run with assertions
enabled, `-ea`, as the course requires) plus a real end-to-end run captured to
[`results/`](results).

| Pset | Assignment | What it does | Verification (measured) |
|---|---|---|---|
| ps0 | **Turtle Graphics** | Logo turtle geometry, polygon math, headings, personal art | **6/6 tests** pass; art rendered → `results/ps0-personal-art.png` |
| ps1 | **Tweet Tweet** | Extract / Filter / SocialNetwork over tweets | **27/27 tests** pass; pipeline demo → `results/ps1-demo.txt` |
| ps2 | **Poetic Walks** | Graph ADT (two reps) + GraphPoet poetry generator | **39/39 tests** pass; poem demo → `results/ps2-poet-demo.txt` |
| ps3 | **Expressivo** | ANTLR parser + recursive Expression ADT, differentiate/simplify | **25/25 tests** pass; REPL demo → `results/ps3-repl-demo.txt` |
| ps4 | **Multiplayer Minesweeper** | Thread-safe Board + concurrent TCP server | **12/12 tests** pass; server transcript → `results/ps4-server-demo.txt` |
| | | **Total** | **109/109 JUnit tests pass** |

### ps0 personal art (real output)

The turtle draws a 36-hexagon color rosette using only `forward`/`turn`/`color`,
rendered headlessly to PNG:

![ps0 personal art](results/ps0-personal-art.png)

### ps3 Expressivo (real REPL transcript)

```
> x * x * x
> (1 * x + x * 1) * x + x * x * 1      # symbolic derivative via the product rule
> 3 * (x + 2.4)
> 22.200000000000003                    # simplify with x = 5
```

### ps4 Minesweeper (real server transcript)

```
S: Welcome to Minesweeper. Board: 5 columns by 5 rows. Players: 1 including you. ...
C: dig 0 0
S:          ...        # flood-fill uncovers the zero-count region
S:       1 1           # squares next to the single bomb show count 1
C: dig 4 4
S: BOOM!
S: <disconnected>      # non-debug mode disconnects after a boom
```

## Implemented assignments

- [x] **ps0 — Turtle Graphics** — `drawSquare`, `calculateRegularPolygonAngle`,
  `calculatePolygonSidesFromAngle`, `drawRegularPolygon`, `calculateHeadingToPoint`
  (clockwise-from-north via `atan2`), `calculateHeadings`, `drawPersonalArt`,
  plus the 6.005 collaboration-policy predicate.
- [x] **ps1 — Tweet Tweet** — mention extraction with proper username boundaries
  (email addresses excluded), timespan/author/word filters (whole-word,
  case-insensitive), and a follows-graph + influencer ranking.
- [x] **ps2 — Poetic Walks** — a `Graph<L>` ADT implemented two ways
  (`ConcreteEdgesGraph` with an immutable `Edge`; `ConcreteVerticesGraph` with a
  mutable `Vertex`), each with full AF/RI/rep-safety docs, exercised by a shared
  `GraphInstanceTest`; and `GraphPoet`, which builds a word-affinity graph and
  inserts maximum-weight bridge words.
- [x] **ps3 — Expressivo** — an extended ANTLR grammar (`+`, `*`, int/decimal
  literals, variables, parentheses, correct precedence), a recursive `Expression`
  ADT (`Number`, `Variable`, `Plus`, `Times`) with structural `equals`/`hashCode`
  and a round-tripping `toString`, symbolic `differentiate` (sum & product rules)
  and `simplify` (substitution + constant folding).
- [x] **ps4 — Multiplayer Minesweeper** — a thread-safe `Board` monitor (dig with
  bomb removal and recursive flood-fill, flag/deflag, protocol rendering, board
  files) and a concurrent server (one thread per client, shared board, full
  protocol, BOOM + non-debug disconnect), with a documented thread-safety argument.

## Project structure

```
mit-6031-software-construction/
├── ps0/  src/turtle, src/rules          # turtle graphics + collab policy
├── ps1/  src/twitter, test/twitter      # tweet analysis
├── ps2/  src/graph, src/poet, test/…    # graph ADT + poetry
├── ps3/  src/expressivo (+parser), test # expression parser (ANTLR)
├── ps4/  src/minesweeper(+server), test # concurrent minesweeper
├── lib/  junit-4.13.2.jar, hamcrest…    # test dependencies
├── results/                             # captured test output + demos
├── run-tests.sh                         # build + JUnit harness
└── LICENSE
```

Each pset keeps its official Eclipse layout (`src/`, `test/`, `.classpath`,
`.project`) so it can also be imported into Eclipse as the course intended.

## How to run

Requires **JDK 21** (`javac`/`java` on `PATH`). JUnit 4 + Hamcrest jars are in
`lib/`; ps1 ships `javax.json`, ps3 ships `antlr.jar` (ANTLR 4.5.1) in their own
`lib/` folders.

```bash
# Build and test every problem set (assertions enabled):
./run-tests.sh

# Or a single one:
./run-tests.sh ps3
```

Run the captured demos yourself:

```bash
# ps0 — render the personal art to PNG (headless):
java -cp ps0/bin turtle.PngTurtle results/ps0-personal-art.png

# ps1 — tweet-analysis pipeline:
java -cp "ps1/bin;ps1/lib/javax.json-1.0.jar" twitter.Demo

# ps2 — poetry generator:
( cd ps2 && java -cp bin poet.Main )

# ps3 — expression REPL (type: an expression, then !d/dx or !simplify x=5):
java -cp "ps3/bin;ps3/lib/antlr.jar" expressivo.Main

# ps4 — start the multiplayer server, then `telnet localhost 4444`:
java -cp ps4/bin minesweeper.server.MinesweeperServer --port 4444 --file ps4/boards/demo.txt
```

To regenerate the ps3 parser after editing the grammar:

```bash
cd ps3/src/expressivo/parser && java -jar ../../../lib/antlr.jar Expression.g4
```

## Verification

- **Autograder-equivalent:** each pset is validated by the course's own JUnit
  test framework, run with `-ea` (the course insists on assertions). `run-tests.sh`
  compiles `src/` + `test/` and runs every suite via `org.junit.runner.JUnitCore`.
  Combined output is in [`results/all-junit.txt`](results/all-junit.txt):
  **109/109 tests pass** (ps0 6, ps1 27, ps2 39, ps3 25, ps4 12).
- **Real runs** beyond unit tests: a rendered PNG (ps0), a tweet pipeline (ps1),
  a generated poem (ps2), a differentiate/simplify REPL session (ps3), and a live
  client↔server Minesweeper transcript over TCP including a flood-fill and a BOOM
  (ps4) — all in `results/`.
- The test suites follow the course's rules: `GraphInstanceTest` obtains graphs
  only via `emptyInstance()` and never refers to a concrete class, and the ps1
  test cases do not strengthen the specs (they remain valid against any spec-
  conforming implementation).

## Tech stack

Java (JDK 21), JUnit 4.13.2 + Hamcrest, ANTLR 4.5.1 (ps3 parser generator),
`javax.json` (ps1 tweet reader), `java.util.concurrent` (ps4 server), AWT/ImageIO
(ps0 headless rendering).

## Key ideas / what I learned

- **Specifications & ADTs:** every ADT documents its abstraction function,
  representation invariant, and rep-exposure safety, checked at runtime by
  `checkRep`.
- **Test-first, partition-based testing:** each test file states a testing
  strategy that partitions the input/output space before the cases.
- **Immutability & structural equality:** `Expression` and `Edge` are immutable;
  equality is structural and consistent with `hashCode`, enabling safe sharing.
- **Recursive data types + grammars:** ps3 pairs a context-free grammar with a
  recursive `Expression` type and interpreters (`differentiate`, `simplify`).
- **Concurrency:** ps4 uses the monitor pattern to make a shared mutable `Board`
  thread-safe under a one-thread-per-client server, with an explicit written
  thread-safety argument.

## Credits & license

Based on the problem sets of **6.005 / 6.031 Software Construction** by the MIT
EECS course staff (Spring 2016 edition, archived on MIT OpenCourseWare). This
repository is an independent educational reimplementation; all course materials,
skeleton code, and specifications belong to their original authors and are used
here under the OCW terms for educational purposes. Original implementation code in
this repo is released under the [MIT License](LICENSE).
