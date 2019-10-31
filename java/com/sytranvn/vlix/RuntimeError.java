package com.sytranvn.vlix;

class RuntimeError extends Exception {
	final Token token;
	
	RuntimeError(Token token, String message) {
		super(message);
		this.token = token;
	}
}
