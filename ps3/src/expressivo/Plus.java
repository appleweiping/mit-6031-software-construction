/* Copyright (c) 2026 appleweiping. MIT License. */
package expressivo;

import java.util.Map;

/**
 * An immutable addition of two subexpressions, {@code left + right}.
 */
public class Plus implements Expression {

    private final Expression left;
    private final Expression right;

    // Abstraction function:
    //   AF(left, right) = the expression (left + right)
    // Representation invariant:
    //   left != null and right != null
    // Safety from rep exposure:
    //   left and right are private, final, and reference immutable Expressions.

    /**
     * @param left  the left operand, non-null
     * @param right the right operand, non-null
     */
    public Plus(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        checkRep();
    }

    private void checkRep() {
        assert left != null && right != null;
    }

    @Override public Expression differentiate(String variable) {
        // sum rule: (u + v)' = u' + v'
        return new Plus(left.differentiate(variable), right.differentiate(variable));
    }

    @Override public Expression simplify(Map<String, Double> environment) {
        Expression l = left.simplify(environment);
        Expression r = right.simplify(environment);
        // constant folding: if both sides reduced to numbers, add them
        if (l instanceof Number && r instanceof Number) {
            return new Number(((Number) l).getValue() + ((Number) r).getValue());
        }
        return new Plus(l, r);
    }

    @Override public String toString() {
        // Addition is the lowest-precedence operator, so its operands never
        // need extra parentheses; each operand parenthesizes itself as needed.
        return left.toString() + " + " + right.toString();
    }

    @Override public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Plus)) {
            return false;
        }
        Plus that = (Plus) thatObject;
        // structural equality; addition is treated as ordered here (a+b != b+a)
        return this.left.equals(that.left) && this.right.equals(that.right);
    }

    @Override public int hashCode() {
        return 31 * left.hashCode() + right.hashCode();
    }
}
