/* Copyright (c) 2026 appleweiping. MIT License. */
package expressivo;

import java.util.Map;

/**
 * An immutable multiplication of two subexpressions, {@code left * right}.
 */
public class Times implements Expression {

    private final Expression left;
    private final Expression right;

    // Abstraction function:
    //   AF(left, right) = the expression (left * right)
    // Representation invariant:
    //   left != null and right != null
    // Safety from rep exposure:
    //   left and right are private, final, and reference immutable Expressions.

    /**
     * @param left  the left operand, non-null
     * @param right the right operand, non-null
     */
    public Times(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        checkRep();
    }

    private void checkRep() {
        assert left != null && right != null;
    }

    @Override public Expression differentiate(String variable) {
        // product rule: (u * v)' = u'v + uv'
        Expression du = left.differentiate(variable);
        Expression dv = right.differentiate(variable);
        return new Plus(new Times(du, right), new Times(left, dv));
    }

    @Override public Expression simplify(Map<String, Double> environment) {
        Expression l = left.simplify(environment);
        Expression r = right.simplify(environment);
        // constant folding: if both sides reduced to numbers, multiply them
        if (l instanceof Number && r instanceof Number) {
            return new Number(((Number) l).getValue() * ((Number) r).getValue());
        }
        return new Times(l, r);
    }

    @Override public String toString() {
        // Multiplication binds tighter than addition, so a Plus operand must be
        // parenthesized to preserve the parse tree; anything else is safe bare.
        return operand(left) + " * " + operand(right);
    }

    private static String operand(Expression e) {
        if (e instanceof Plus) {
            return "(" + e.toString() + ")";
        }
        return e.toString();
    }

    @Override public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Times)) {
            return false;
        }
        Times that = (Times) thatObject;
        return this.left.equals(that.left) && this.right.equals(that.right);
    }

    @Override public int hashCode() {
        return 37 * left.hashCode() + right.hashCode();
    }
}
