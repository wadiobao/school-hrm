package com.kltn.school_hrm.module.formula.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.formula.engine.expression.BinaryExpression;
import com.kltn.school_hrm.module.formula.engine.expression.Expression;
import com.kltn.school_hrm.module.formula.engine.expression.NumberExpression;
import com.kltn.school_hrm.module.formula.engine.expression.UnaryMinusExpression;
import com.kltn.school_hrm.module.formula.engine.expression.VariableExpression;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

/**
 * Parser đệ quy xuống, chỉ chấp nhận số, biến đã đăng ký và toán tử +, -, *, /, ().
 * Không eval chuỗi, không gọi engine JavaScript.
 */
@Component
public class RecursiveDescentFormulaParser implements FormulaParser {

    public static final int MAX_FORMULA_LENGTH = 500;
    public static final int MAX_AST_DEPTH = 32;

    private static final Set<String> ALLOWED_OPERATORS = Set.of("+", "-", "*", "/", "(", ")");

    private final FormulaVariableRegistry variableRegistry;

    public RecursiveDescentFormulaParser(FormulaVariableRegistry variableRegistry) {
        this.variableRegistry = variableRegistry;
    }

    @Override
    public Expression parse(String formula) {
        if (formula == null || formula.isBlank()) {
            throw new BusinessException("Công thức không được để trống");
        }
        String trimmed = formula.trim();
        if (trimmed.length() > MAX_FORMULA_LENGTH) {
            throw new BusinessException("Công thức vượt quá " + MAX_FORMULA_LENGTH + " ký tự");
        }

        List<Token> tokens = tokenize(trimmed);
        ParserState state = new ParserState(tokens);
        Expression expression = parseExpression(state);
        if (state.current().type != TokenType.EOF) {
            throw new BusinessException("Công thức còn ký tự không hợp lệ tại: " + state.current().lexeme);
        }
        if (expression.depth() > MAX_AST_DEPTH) {
            throw new BusinessException("Công thức quá phức tạp (độ sâu AST > " + MAX_AST_DEPTH + ")");
        }
        return expression;
    }

    private Expression parseExpression(ParserState state) {
        Expression left = parseTerm(state);
        while (state.current().type == TokenType.PLUS || state.current().type == TokenType.MINUS) {
            char op = state.current().type == TokenType.PLUS ? '+' : '-';
            state.advance();
            Expression right = parseTerm(state);
            left = new BinaryExpression(left, op, right);
        }
        return left;
    }

    private Expression parseTerm(ParserState state) {
        Expression left = parseUnary(state);
        while (state.current().type == TokenType.STAR || state.current().type == TokenType.SLASH) {
            char op = state.current().type == TokenType.STAR ? '*' : '/';
            state.advance();
            Expression right = parseUnary(state);
            left = new BinaryExpression(left, op, right);
        }
        return left;
    }

    private Expression parseUnary(ParserState state) {
        if (state.current().type == TokenType.MINUS) {
            state.advance();
            return new UnaryMinusExpression(parseUnary(state));
        }
        return parsePrimary(state);
    }

    private Expression parsePrimary(ParserState state) {
        Token token = state.current();
        if (token.type == TokenType.NUMBER) {
            state.advance();
            return new NumberExpression(new BigDecimal(token.lexeme));
        }
        if (token.type == TokenType.IDENT) {
            if (!variableRegistry.isAllowed(token.lexeme)) {
                throw new BusinessException("Biến chưa được đăng ký: " + token.lexeme);
            }
            state.advance();
            return new VariableExpression(token.lexeme);
        }
        if (token.type == TokenType.LPAREN) {
            state.advance();
            Expression inner = parseExpression(state);
            if (state.current().type != TokenType.RPAREN) {
                throw new BusinessException("Thiếu dấu đóng ngoặc ')'");
            }
            state.advance();
            return inner;
        }
        throw new BusinessException("Cú pháp không hợp lệ tại: " + (token.lexeme.isEmpty() ? "cuối công thức" : token.lexeme));
    }

    private List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
                if (!ALLOWED_OPERATORS.contains(String.valueOf(c))) {
                    throw new BusinessException("Toán tử không được hỗ trợ: " + c);
                }
                tokens.add(new Token(switch (c) {
                    case '+' -> TokenType.PLUS;
                    case '-' -> TokenType.MINUS;
                    case '*' -> TokenType.STAR;
                    case '/' -> TokenType.SLASH;
                    case '(' -> TokenType.LPAREN;
                    default -> TokenType.RPAREN;
                }, String.valueOf(c)));
                i++;
                continue;
            }
            if (Character.isDigit(c) || (c == '.' && i + 1 < source.length() && Character.isDigit(source.charAt(i + 1)))) {
                int start = i;
                boolean seenDot = false;
                while (i < source.length()) {
                    char d = source.charAt(i);
                    if (Character.isDigit(d)) {
                        i++;
                    } else if (d == '.' && !seenDot) {
                        seenDot = true;
                        i++;
                    } else {
                        break;
                    }
                }
                String number = source.substring(start, i);
                if (number.equals(".") || number.endsWith(".")) {
                    throw new BusinessException("Số không hợp lệ: " + number);
                }
                tokens.add(new Token(TokenType.NUMBER, number));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                i++;
                while (i < source.length()) {
                    char d = source.charAt(i);
                    if (Character.isLetterOrDigit(d) || d == '_') {
                        i++;
                    } else {
                        break;
                    }
                }
                tokens.add(new Token(TokenType.IDENT, source.substring(start, i)));
                continue;
            }
            throw new BusinessException("Ký tự không hợp lệ trong công thức: '" + c + "'");
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private enum TokenType {
        NUMBER, IDENT, PLUS, MINUS, STAR, SLASH, LPAREN, RPAREN, EOF
    }

    private record Token(TokenType type, String lexeme) {
    }

    private static final class ParserState {
        private final List<Token> tokens;
        private int index;

        private ParserState(List<Token> tokens) {
            this.tokens = tokens;
        }

        private Token current() {
            return tokens.get(index);
        }

        private void advance() {
            if (index < tokens.size() - 1) {
                index++;
            }
        }
    }
}
