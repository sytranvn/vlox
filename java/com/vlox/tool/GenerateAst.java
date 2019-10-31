package com.vlox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output_dir>");
			System.exit(1);
		}
		String outputDir = args[0];
		
		defineAst(outputDir, "Expr", Arrays.asList(
			"Assign		: Token name, Expr value				: RuntimeError",
			"Binary		: Expr left, Token operator, Expr right	: RuntimeError",
			"Grouping	: Expr expression						: RuntimeError",
			"Literal	: Object value",
			"Unary		: Token operator, Expr right			: RuntimeError",
			"Variable	: Token name							: RuntimeError"
		));
		
		defineAst(outputDir, "Stmt", Arrays.asList(
			"Block		: List<Stmt> statements					: RuntimeError",
			"Expression	: Expr expression						: RuntimeError",
			"Print		: Expr expression						: RuntimeError",
			"Var		: Token name, Expr initializer			: RuntimeError"
		));
	}

	private static void defineAst(
		String outputDir, 
		String baseName, 
		List<String> types) throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		
		writer.println("package com.vlox.lox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("abstract class " + baseName + " {");

		defineVisitor(writer, baseName, types);
		
		// The AST classes
		for (String type: types) {
			String[] defs = type.split(":");
			String className = defs[0].trim();
			String fields = defs[1].trim();
			String error = "";
			if (defs.length > 2) {
				error  = " throws " + defs[2].trim();
			}
			defineType(writer, baseName, className, fields, error);
		}
		
		writer.println();
		writer.println("	abstract <R> R accept(Visitor<R> visitor) throws RuntimeError;");
		
		writer.println("}");
		writer.close();
	}

	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("	interface Visitor<R> {");
		
		for (String type : types) {
			String[] defs = type.split(":");
			String typeName = defs[0].trim();
			String error = defs.length > 2 ? " throws " + defs[2].trim() : "";
			writer.println("		R visit" + typeName + baseName + "(" +
					typeName + " " + baseName.toLowerCase() + ")" + error + ";");
		}
		
		writer.println("		}");
	}

	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList, String error) {
		writer.println("	static class " + className + " extends " + baseName + " {");
			writer.println("		" + className + "(" + fieldList + ") {");
			String[] fields = fieldList.split(", ");
			for (String field: fields) {
				String name = field.split(" ")[1];
				writer.println("			this." + name + " = " + name + ";");
			}
			writer.println("		}");
			
			writer.println();
			writer.println("		<R> R accept(Visitor<R> visitor)" + error + " {");
			writer.println("			return visitor.visit" +
					className + baseName + "(this);");
			writer.println("		}");
			
			writer.println();
			for (String field : fields) {
				writer.println("		final "  + field + ";");
			}
		writer.println("	}");
	}

}
