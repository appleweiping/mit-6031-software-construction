/* Copyright (c) 2026 appleweiping. MIT License. */
package expressivo;

import java.util.Map;

/**
 * An immutable variable in an expression, e.g. {@code x} or {@code foo}.
 * Variable names are case-sensitive nonempty strings of letters.
 */
public class Variable implements Expression {

    private final String name;

    // Abstraction function:
    //   AF(name) = the variable named `name`
    // Representation invariant:
    //   name is a nonempty string of letters (A-Z, a-z)
    // Safety from rep exposure:
    //   name is a private final immutable String.

    /**
     * @param name the variable name, a nonempty string of letters
     */
    public Variable(String name) {
        this.name = name;
        checkRep();
    }

    private void checkRep() {
        assert name != null && name.matches("[A-Za-z]+") : "variable must be letters";
    }

    /** @return the variable name */
    public String getName() {
        return name;
    }

    @Override public Expression differentiate(String variable) {
        // d/dx of x is 1; d/dx of any other variable is 0
        return new Number(name.equals(variable) ? 1 : 0);
    }

    @Override public Expression simplify(Map<String, Double> environment) {
        // substitute if bound in the environment, otherwise keep the variable
        if (environment.containsKey(name)) {
            return new Number(environment.get(name));
        }
        return this;
    }

    @Override public String toString() {
        return name;
    }

    @Override public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Variable)) {
            return false;
        }
        Variable that = (Variable) thatObject;
        // case-sensitive structural equality
        return this.name.equals(that.name);
    }

    @Override public int hashCode() {
        return name.hashCode();
    }
}
