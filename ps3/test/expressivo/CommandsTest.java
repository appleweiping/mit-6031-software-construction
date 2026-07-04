/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package expressivo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for the static methods of Commands.
 */
public class CommandsTest {

    // Testing strategy
    //
    //   differentiate(expr, var):
    //     expr: constant, the variable itself, a different variable,
    //           sum (sum rule), product (product rule), nested
    //     result verified by parsing both sides and comparing as Expressions
    //   simplify(expr, env):
    //     env: empty, covers all variables (-> single number), covers some
    //     folding: constant subexpressions folded
    //     invalid expression -> IllegalArgumentException

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /** Assert two expression strings are structurally-equal expressions. */
    private static void assertExprEquals(String expected, String actual) {
        assertEquals(Expression.parse(expected), Expression.parse(actual));
    }

    // differentiate ------------------------------------------------------

    @Test
    public void testDifferentiateConstant() {
        assertExprEquals("0", Commands.differentiate("5", "x"));
    }

    @Test
    public void testDifferentiateVariableItself() {
        assertExprEquals("1", Commands.differentiate("x", "x"));
    }

    @Test
    public void testDifferentiateOtherVariable() {
        assertExprEquals("0", Commands.differentiate("y", "x"));
    }

    @Test
    public void testDifferentiateSumRule() {
        // d/dx (x + x) = 1 + 1
        assertExprEquals("1 + 1", Commands.differentiate("x + x", "x"));
    }

    @Test
    public void testDifferentiateProductRule() {
        // d/dx (x * x) = 1*x + x*1
        assertExprEquals("1*x + x*1", Commands.differentiate("x * x", "x"));
    }

    @Test
    public void testDifferentiateProductThenSimplifyIsCorrect() {
        // The derivative of x*x, evaluated at x=3, should be 6.
        String d = Commands.differentiate("x * x", "x");
        Map<String, Double> env = new HashMap<>();
        env.put("x", 3.0);
        assertExprEquals("6", Commands.simplify(d, env));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDifferentiateInvalidVariable() {
        Commands.differentiate("x", "3");
    }

    // simplify -----------------------------------------------------------

    @Test
    public void testSimplifyFullEnvironmentToNumber() {
        Map<String, Double> env = new HashMap<>();
        env.put("x", 2.0);
        env.put("y", 3.0);
        // x*y + x = 2*3 + 2 = 8
        assertExprEquals("8", Commands.simplify("x * y + x", env));
    }

    @Test
    public void testSimplifyPartialEnvironment() {
        Map<String, Double> env = new HashMap<>();
        env.put("x", 4.0);
        // 4 * y  (y unbound stays symbolic)
        assertExprEquals("4 * y", Commands.simplify("x * y", env));
    }

    @Test
    public void testSimplifyConstantFoldingNoEnv() {
        // 2 + 3 * 4 = 14 even with no variables and empty environment
        assertExprEquals("14", Commands.simplify("2 + 3 * 4", new HashMap<>()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSimplifyInvalidExpression() {
        Commands.simplify("* 3", new HashMap<>());
    }
}
