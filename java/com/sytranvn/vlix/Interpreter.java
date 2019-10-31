package com.sytranvn.vlix;

import java.util.List;

import com.sytranvn.vlix.Expr.Assign;
import com.sytranvn.vlix.Expr.Binary;
import com.sytranvn.vlix.Expr.Grouping;
import com.sytranvn.vlix.Expr.Literal;
import com.sytranvn.vlix.Expr.Unary;
import com.sytranvn.vlix.Expr.Variable;
import com.sytranvn.vlix.Stmt.Block;
import com.sytranvn.vlix.Stmt.Expression;
import com.sytranvn.vlix.Stmt.Print;
import com.sytranvn.vlix.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	private Environment environment = new Environment();
	
	void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement: statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}
	
	@Override
	public Object visitBinaryExpr(Binary expr) throws RuntimeError {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
		case GREATER:
			checkNumberOperands(expr.operator, left, right);
			return (double)left > (double)right;
		case GREATER_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left >= (double)right;
		case LESS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left < (double)right;
		case LESS_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left <= (double)right;
		case BANG_EQUAL: return !isEqual(left, right);
		case EQUAL_EQUAL: return isEqual(left, right);
		case MINUS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left - (double)right;
		case PLUS:
			if (left instanceof Double && right instanceof Double) {
				return (double)left + (double)right;
			}
			if (left instanceof String && right instanceof String) {
				return (String)left + (String)right;
			}
			throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
		case SLASH:
			return (double)left / (double)right;
		case STAR:
			return (double)left * (double)right;
		}
		return null;
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) throws RuntimeError {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) throws RuntimeError {
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
			case BANG:
				return !isTruthy(right);
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return -(double)right;
			default:
				return null;
		}
	}
	
	private void checkNumberOperand(Token operator, Object operand) throws RuntimeError {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number");
	}
	
	private void checkNumberOperands(Token operator, Object left, Object right) throws RuntimeError {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers");
	}
	
	private boolean isTruthy(Object object) {
		if (object == null) return false;
		if (object instanceof Boolean) return (boolean)object;
		return true;
	}

	private Object evaluate(Expr expr) throws RuntimeError {
		return expr.accept(this);
	}
	

	private void execute(Stmt statement) throws RuntimeError {
		statement.accept(this);
	}

	private void executeBlock(List<Stmt> statements, Environment environment) throws RuntimeError {
		Environment previous = this.environment;
		try {
			this.environment = environment;
			for (Stmt statement: statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	@Override
	public Void visitBlockStmt(Block stmt) throws RuntimeError {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null) return true;
		if (left == null) return false;
		return left.equals(right);
	}
	
	private String stringify(Object object) {
		if (object == null) return "nil";
		
		// Remove .0 for integer value
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		
		return object.toString();
	}

	@Override
	public Void visitPrintStmt(Print stmt) throws RuntimeError {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) throws RuntimeError {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Object visitAssignExpr(Assign expr) throws RuntimeError {
		Object value = evaluate(expr.value);
		
		environment.assign(expr.name, value);
		return value;
	}

	@Override
	public Object visitVariableExpr(Variable expr) throws RuntimeError {
		return environment.get(expr.name);
	}
}
