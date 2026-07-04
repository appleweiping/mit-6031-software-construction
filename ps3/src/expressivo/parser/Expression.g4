/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */

/**
 * This file is the grammar file used by ANTLR.
 *
 * In order to compile this file, navigate to this folder
 * (src/expressivo/parser) and run the following command:
 *
 * java -jar ../../../lib/antlr.jar Expression.g4
 *
 * PS3 instructions: you are free to change this grammar.
 *
 * Grammar for polynomial expressions over + and *, nonnegative integer and
 * floating-point literals, and case-sensitive variables (nonempty strings of
 * letters). Multiplication binds tighter than addition; parentheses override
 * precedence.
 *
 *   expr    = sum
 *   sum     = product ('+' product)*
 *   product = primitive ('*' primitive)*
 *   primitive = NUMBER | VARIABLE | '(' sum ')'
 */
grammar Expression;
import Configuration;

/*
 * Nonterminal rules (parser rules) must be lowercase.
 * Terminal rules (lexical rules) must be UPPERCASE.
 * "root" is the start rule and ends with EOF so it matches the whole input.
 */
root : sum EOF;
sum : product ('+' product)*;
product : primitive ('*' primitive)*;
primitive : NUMBER | VARIABLE | '(' sum ')';

NUMBER : [0-9]+ ('.' [0-9]+)? | '.' [0-9]+;
VARIABLE : [A-Za-z]+;

/* Tell Antlr to ignore whitespace around tokens. */
SPACES : [ \t\r\n]+ -> skip;
