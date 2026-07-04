/* Copyright (c) 2026 appleweiping. MIT License. */
package expressivo;

import java.util.Map;

/**
 * An immutable numeric literal in an expression, e.g. {@code 3} or {@code 2.4}.
 */
public class Number implements Expression {

    private final double value;

    // Abstraction function:
    //   AF(value) = the numeric literal `value`
    // Representation invariant:
    //   value >= 0 (the grammar only produces nonnegative literals)
    // Safety from rep exposure:
    //   value is a private final primitive double, so no rep can leak.

    /**
     * @param value the (nonnegative) numeric value of this literal
     */
    public Number(double value) {
        this.value = value;
    }

    /** @return the numeric value */
    public double getValue() {
        return value;
    }

    @Override public Expression differentiate(String variable) {
        // d/dx of a constant is 0
        return new Number(0);
    }

    @Override public Expression simplify(Map<String, Double> environment) {
        return this;
    }

    /**
     * @return a parsable representation. Integral values print without a
     *         trailing ".0" (e.g. "3"), others print their decimal form.
     */
    @Override public String toString() {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    @Override public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Number)) {
            return false;
        }
        Number that = (Number) thatObject;
        // structural equality on the numeric value
        return Double.compare(this.value, that.value) == 0;
    }

    @Override public int hashCode() {
        return Double.hashCode(value);
    }
}
