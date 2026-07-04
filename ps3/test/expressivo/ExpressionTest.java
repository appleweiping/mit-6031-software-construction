/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package expressivo;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for the Expression abstract data type.
 */
public class ExpressionTest {

    // Testing strategy
    //
    //   parse(input):
    //     literal: integer, decimal
    //     variable: single letter, multiple letters, case-sensitive
    //     operators: +, *, both with precedence, nested parentheses
    //     whitespace: extra spaces
    //     invalid input -> IllegalArgumentException
    //   toString():
    //     round-trips: parse(e.toString()).equals(e)
    //     multiplication parenthesizes a Plus operand
    //   equals()/hashCode():
    //     structurally equal expressions are equal with equal hash codes
    //     grouping matters: (a+b)+c vs a+(b+c) not equal
    //     different types / values not equal

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // parse --------------------------------------------------------------

    @Test
    public void testParseNumberAndVariable() {
        assertEquals(new Number(3), Expression.parse("3"));
        assertEquals(new Number(2.4), Expression.parse("2.4"));
        assertEquals(new Variable("x"), Expression.parse("x"));
        assertEquals(new Variable("foo"), Expression.parse("foo"));
    }

    @Test
    public void testParsePrecedence() {
        // 3 + x * 2 must parse as 3 + (x * 2)
        Expression expected = new Plus(new Number(3),
                new Times(new Variable("x"), new Number(2)));
        assertEquals(expected, Expression.parse("3 + x * 2"));
    }

    @Test
    public void testParseParenthesesOverridePrecedence() {
        // (3 + x) * 2
        Expression expected = new Times(
                new Plus(new Number(3), new Variable("x")), new Number(2));
        assertEquals(expected, Expression.parse("(3 + x) * 2"));
    }

    @Test
    public void testParseWhitespaceIgnored() {
        assertEquals(Expression.parse("3+2.4"), Expression.parse("  3   +  2.4 "));
    }

    @Test
    public void testParseCaseSensitiveVariables() {
        assertNotEquals(Expression.parse("x"), Expression.parse("X"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseInvalidThrows() {
        Expression.parse("3 +");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseInvalidOperatorThrows() {
        Expression.parse("3 % x");
    }

    // toString round-trip ------------------------------------------------

    @Test
    public void testToStringRoundTrip() {
        for (String s : new String[] {
                "3", "2.4", "x", "foo + bar + baz",
                "3 * (x + 2.4)", "(a + b) * (c + d)", "x*x*x + 1" }) {
            Expression e = Expression.parse(s);
            assertEquals("round-trip failed for: " + s, e, Expression.parse(e.toString()));
        }
    }

    @Test
    public void testToStringParenthesizesPlusUnderTimes() {
        Expression e = new Times(new Plus(new Variable("x"), new Number(1)), new Number(2));
        assertEquals("(x + 1) * 2", e.toString());
    }

    // equals / hashCode --------------------------------------------------

    @Test
    public void testEqualsStructural() {
        Expression a = Expression.parse("x + 1");
        Expression b = new Plus(new Variable("x"), new Number(1));
        assertEquals(a, b);
        assertEquals("equal expressions must have equal hash codes",
                a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsGroupingMatters() {
        // (a+b)+c is not structurally equal to a+(b+c)
        Expression left = new Plus(new Plus(new Variable("a"), new Variable("b")), new Variable("c"));
        Expression right = new Plus(new Variable("a"), new Plus(new Variable("b"), new Variable("c")));
        assertNotEquals(left, right);
    }

    @Test
    public void testEqualsDifferentTypesAndValues() {
        assertNotEquals(new Number(3), new Variable("x"));
        assertNotEquals(new Number(3), new Number(4));
        assertNotEquals(new Number(3), "3");
    }
}
