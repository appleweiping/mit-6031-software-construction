#!/usr/bin/env bash
#
# Build + test runner for the MIT 6.031 / 6.005 problem sets.
#
# Compiles a problem set's src/ and test/ trees with JDK 21 and runs its
# JUnit 4 test suites with assertions enabled (-ea), which the course relies on.
#
# Usage:
#   ./run-tests.sh          # build + test every problem set (ps0..ps4)
#   ./run-tests.sh ps2      # build + test only ps2
#
# Requires: javac/java (JDK 21) on PATH. JUnit jars live in ./lib.
# On Windows/Git-Bash the JVM is a native binary, so every classpath entry is
# converted to a Windows path and joined with ';'. On Linux/macOS paths are
# left as-is and joined with ':'.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

WINDOWS=0
SEP=":"
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) WINDOWS=1; SEP=";";;
esac

# Convert a path to the platform's native form for the JVM.
p() { if [ "$WINDOWS" = 1 ]; then cygpath -w "$1"; else echo "$1"; fi; }

JUNIT="$(p "$ROOT/lib/junit-4.13.2.jar")"
HAMCREST="$(p "$ROOT/lib/hamcrest-core-1.3.jar")"

# Test classes per problem set (fully-qualified names).
declare -A TESTS=(
  [ps0]="rules.RulesOf6005Test turtle.TurtleSoupTest"
  [ps1]="twitter.ExtractTest twitter.FilterTest twitter.SocialNetworkTest"
  [ps2]="graph.GraphStaticTest graph.ConcreteEdgesGraphTest graph.ConcreteVerticesGraphTest poet.GraphPoetTest"
  [ps3]="expressivo.ExpressionTest expressivo.CommandsTest"
  [ps4]="minesweeper.BoardTest minesweeper.server.MinesweeperServerTest"
)

run_ps() {
  local ps="$1"
  local dir="$ROOT/$ps"
  echo "==================== $ps ===================="
  local outdir="$dir/bin"
  rm -rf "$outdir"; mkdir -p "$outdir"
  local out; out="$(p "$outdir")"

  # Extra jars a pset ships in its lib/ (javax.json for ps1, antlr for ps3).
  local extra=""
  if [ -d "$dir/lib" ]; then
    for j in "$dir"/lib/*.jar; do [ -e "$j" ] && extra="${extra}${SEP}$(p "$j")"; done
  fi
  local cp="${JUNIT}${SEP}${HAMCREST}${extra}"

  # Gather sources.
  local srcs
  srcs=$(find "$dir/src" "$dir/test" -name '*.java' 2>/dev/null)

  echo "-- compiling --"
  if ! javac -encoding UTF-8 -cp "$cp" -d "$out" $srcs 2>"$outdir/compile.log"; then
    echo "COMPILE FAILED:"; cat "$outdir/compile.log"; return 1
  fi
  # copy non-java resources (corpora, grammars) so tests can find them on classpath
  if [ -d "$dir/src" ]; then
    (cd "$dir/src" && find . -type f ! -name '*.java' -exec cp --parents {} "$outdir/" \; ) 2>/dev/null
  fi

  echo "-- running JUnit (assertions enabled) --"
  java -Dfile.encoding=UTF-8 -ea -cp "${out}${SEP}${cp}" org.junit.runner.JUnitCore ${TESTS[$ps]}
  return $?
}

status=0
if [ $# -ge 1 ]; then
  run_ps "$1" || status=1
else
  for ps in ps0 ps1 ps2 ps3 ps4; do
    run_ps "$ps" || status=1
  done
fi
exit $status
