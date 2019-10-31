package com.sytranvn.vlix;

import com.sytranvn.vlix.Expr.Binary;
import com.sytranvn.vlix.Expr.Grouping;
import com.sytranvn.vlix.Expr.Literal;
import com.sytranvn.vlix.Expr.Unary;
import com.sytranvn.vlix.Expr.Variable;

abstract class AstPrinter implements Expr.Visitor<String> {
	String print(Expr expr) throws RuntimeError {
		try {
			return expr.accept(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String visitBinaryExpr(Binary expr) throws RuntimeError {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Grouping expr) throws RuntimeError {
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Literal expr) {
		if (expr.value == null) return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Unary expr) throws RuntimeError {
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	private String parenthesize(String name, Expr... exprs) throws RuntimeError {
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(name);
		for (Expr expr: exprs) {
			builder.append(" ");
			try {
				builder.append(expr.accept(this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return null;
	}
}
