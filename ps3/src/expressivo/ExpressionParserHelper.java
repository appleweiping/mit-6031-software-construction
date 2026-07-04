/* Copyright (c) 2026 appleweiping. MIT License. */
package expressivo;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import expressivo.parser.ExpressionBaseListener;
import expressivo.parser.ExpressionLexer;
import expressivo.parser.ExpressionParser;

/**
 * Parses expression strings into {@link Expression} ASTs using the ANTLR-
 * generated grammar. Not part of the public spec; used by {@link
 * Expression#parse(String)}.
 */
class ExpressionParserHelper {

    /**
     * Parse an expression string into an Expression AST.
     *
     * @param input the expression text, e.g. "3 * (x + 2.4)"
     * @return the corresponding Expression
     * @throws IllegalArgumentException if the input is not a valid expression
     */
    static Expression parse(String input) {
        try {
            ANTLRInputStream stream = new ANTLRInputStream(input);
            ExpressionLexer lexer = new ExpressionLexer(stream);
            lexer.reportErrorsAsExceptions();
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            ExpressionParser parser = new ExpressionParser(tokens);
            parser.reportErrorsAsExceptions();

            ParseTree tree = parser.root();

            BuildAstListener listener = new BuildAstListener();
            ParseTreeWalker.DEFAULT.walk(listener, tree);
            return listener.result();
        } catch (RuntimeException e) {
            // ANTLR throws ParseCancellationException (a RuntimeException) on
            // invalid input; surface it as the contract's IllegalArgumentException.
            throw new IllegalArgumentException("invalid expression: " + input, e);
        }
    }

    /**
     * A listener that builds an Expression bottom-up using an explicit stack.
     * On exit of each rule that yields a value, it pops its children's results
     * and pushes the combined Expression.
     */
    private static class BuildAstListener extends ExpressionBaseListener {

        private final Deque<Expression> stack = new ArrayDeque<>();

        Expression result() {
            return stack.peek();
        }

        @Override public void exitPrimitive(ExpressionParser.PrimitiveContext ctx) {
            if (ctx.NUMBER() != null) {
                stack.push(new Number(Double.parseDouble(ctx.NUMBER().getText())));
            } else if (ctx.VARIABLE() != null) {
                stack.push(new Variable(ctx.VARIABLE().getText()));
            }
            // otherwise it is '(' sum ')': the inner sum already left its
            // Expression on the stack, so there is nothing to do.
        }

        @Override public void exitProduct(ExpressionParser.ProductContext ctx) {
            // product : primitive ('*' primitive)*
            int factors = ctx.primitive().size();
            // the `factors` sub-results are on the stack in order; combine them
            // left-associatively into a chain of Times nodes.
            combine(factors, /*isProduct=*/true);
        }

        @Override public void exitSum(ExpressionParser.SumContext ctx) {
            // sum : product ('+' product)*
            int terms = ctx.product().size();
            combine(terms, /*isProduct=*/false);
        }

        /** Pop `count` operands and push a left-associative Plus/Times chain. */
        private void combine(int count, boolean isProduct) {
            if (count <= 1) {
                return; // single operand: already on the stack
            }
            // pop operands into an array (they were pushed left-to-right, so the
            // top of the stack is the rightmost operand)
            Expression[] operands = new Expression[count];
            for (int i = count - 1; i >= 0; i--) {
                operands[i] = stack.pop();
            }
            Expression acc = operands[0];
            for (int i = 1; i < count; i++) {
                acc = isProduct ? new Times(acc, operands[i]) : new Plus(acc, operands[i]);
            }
            stack.push(acc);
        }
    }
}
