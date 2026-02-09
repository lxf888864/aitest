package com.example.calculator.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {
    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
    private static final Map<String, Boolean> RIGHT_ASSOCIATIVE = new HashMap<>();

    static {
        PRECEDENCE.put("+", 1);
        PRECEDENCE.put("-", 1);
        PRECEDENCE.put("*", 2);
        PRECEDENCE.put("/", 2);
        PRECEDENCE.put("^", 3);

        RIGHT_ASSOCIATIVE.put("+", false);
        RIGHT_ASSOCIATIVE.put("-", false);
        RIGHT_ASSOCIATIVE.put("*", false);
        RIGHT_ASSOCIATIVE.put("/", false);
        RIGHT_ASSOCIATIVE.put("^", true);
    }

    public double evaluate(String expression) {
        List<String> tokens = tokenize(expression);
        List<String> rpn = toRpn(tokens);
        return evalRpn(rpn);
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        int length = expression.length();
        StringBuilder number = new StringBuilder();
        StringBuilder identifier = new StringBuilder();
        TokenType lastToken = TokenType.NONE;

        for (int i = 0; i < length; i++) {
            char ch = expression.charAt(i);
            if (Character.isWhitespace(ch)) {
                continue;
            }

            if (Character.isDigit(ch) || ch == '.') {
                number.setLength(0);
                number.append(ch);
                while (i + 1 < length && (Character.isDigit(expression.charAt(i + 1)) || expression.charAt(i + 1) == '.')) {
                    i++;
                    number.append(expression.charAt(i));
                }
                tokens.add(number.toString());
                lastToken = TokenType.NUMBER;
                continue;
            }

            if (isOperatorChar(ch)) {
                if (ch == '-' && (lastToken == TokenType.NONE || lastToken == TokenType.OPERATOR || lastToken == TokenType.LEFT_PAREN)) {
                    if (i + 1 < length && (Character.isDigit(expression.charAt(i + 1)) || expression.charAt(i + 1) == '.')) {
                        number.setLength(0);
                        number.append(ch);
                        i++;
                        number.append(expression.charAt(i));
                        while (i + 1 < length && (Character.isDigit(expression.charAt(i + 1)) || expression.charAt(i + 1) == '.')) {
                            i++;
                            number.append(expression.charAt(i));
                        }
                        tokens.add(number.toString());
                        lastToken = TokenType.NUMBER;
                        continue;
                    }
                    tokens.add("0");
                }
                tokens.add(String.valueOf(ch));
                lastToken = TokenType.OPERATOR;
                continue;
            }

            if (ch == '(') {
                tokens.add("(");
                lastToken = TokenType.LEFT_PAREN;
                continue;
            }

            if (ch == ')') {
                tokens.add(")");
                lastToken = TokenType.RIGHT_PAREN;
                continue;
            }

            if (Character.isLetter(ch)) {
                identifier.setLength(0);
                identifier.append(ch);
                while (i + 1 < length && Character.isLetter(expression.charAt(i + 1))) {
                    i++;
                    identifier.append(expression.charAt(i));
                }
                tokens.add(identifier.toString().toLowerCase());
                lastToken = TokenType.FUNCTION;
                continue;
            }

            throw new IllegalArgumentException("Unsupported character: " + ch);
        }

        return tokens;
    }

    private List<String> toRpn(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
                continue;
            }
            if (isFunction(token)) {
                stack.push(token);
                continue;
            }
            if (isOperator(token)) {
                while (!stack.isEmpty() && (isOperator(stack.peek()) || isFunction(stack.peek()))) {
                    String top = stack.peek();
                    if (isFunction(top)) {
                        output.add(stack.pop());
                        continue;
                    }
                    int precedence = PRECEDENCE.get(token);
                    int topPrecedence = PRECEDENCE.get(top);
                    boolean rightAssociative = RIGHT_ASSOCIATIVE.get(token);
                    if ((rightAssociative && precedence < topPrecedence) || (!rightAssociative && precedence <= topPrecedence)) {
                        output.add(stack.pop());
                    } else {
                        break;
                    }
                }
                stack.push(token);
                continue;
            }
            if (token.equals("(")) {
                stack.push(token);
                continue;
            }
            if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses.");
                }
                stack.pop();
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    output.add(stack.pop());
                }
                continue;
            }
            throw new IllegalArgumentException("Unsupported token: " + token);
        }

        while (!stack.isEmpty()) {
            String token = stack.pop();
            if (token.equals("(") || token.equals(")")) {
                throw new IllegalArgumentException("Mismatched parentheses.");
            }
            output.add(token);
        }
        return output;
    }

    private double evalRpn(List<String> rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
                continue;
            }
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression.");
                }
                double right = stack.pop();
                double left = stack.pop();
                stack.push(applyOperator(token, left, right));
                continue;
            }
            if (isFunction(token)) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Invalid function usage.");
                }
                double value = stack.pop();
                stack.push(applyFunction(token, value));
                continue;
            }
            throw new IllegalArgumentException("Unsupported token: " + token);
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression.");
        }
        return stack.pop();
    }

    private boolean isOperatorChar(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^';
    }

    private boolean isOperator(String token) {
        return PRECEDENCE.containsKey(token);
    }

    private boolean isFunction(String token) {
        return token.equals("sin")
                || token.equals("cos")
                || token.equals("tan")
                || token.equals("sqrt")
                || token.equals("log")
                || token.equals("ln");
    }

    private boolean isNumber(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        char first = token.charAt(0);
        return Character.isDigit(first) || first == '-' || first == '.';
    }

    private double applyOperator(String operator, double left, double right) {
        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0.0) {
                    throw new IllegalArgumentException("Division by zero.");
                }
                return left / right;
            case "^":
                return Math.pow(left, right);
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    private double applyFunction(String function, double value) {
        switch (function) {
            case "sin":
                return Math.sin(Math.toRadians(value));
            case "cos":
                return Math.cos(Math.toRadians(value));
            case "tan":
                return Math.tan(Math.toRadians(value));
            case "sqrt":
                if (value < 0) {
                    throw new IllegalArgumentException("Square root requires non-negative value.");
                }
                return Math.sqrt(value);
            case "log":
                if (value <= 0) {
                    throw new IllegalArgumentException("Log requires positive value.");
                }
                return Math.log10(value);
            case "ln":
                if (value <= 0) {
                    throw new IllegalArgumentException("Ln requires positive value.");
                }
                return Math.log(value);
            default:
                throw new IllegalArgumentException("Unsupported function: " + function);
        }
    }

    private enum TokenType {
        NONE,
        NUMBER,
        OPERATOR,
        LEFT_PAREN,
        RIGHT_PAREN,
        FUNCTION
    }
}
