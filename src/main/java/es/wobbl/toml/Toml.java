package es.wobbl.toml;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;

import com.google.common.collect.Lists;

import es.wobbl.toml.TomlParser.ArrayContext;
import es.wobbl.toml.TomlParser.BoolContext;
import es.wobbl.toml.TomlParser.DatetimeContext;
import es.wobbl.toml.TomlParser.HeaderContext;
import es.wobbl.toml.TomlParser.NameContext;
import es.wobbl.toml.TomlParser.NumberContext;
import es.wobbl.toml.TomlParser.ObjectContext;
import es.wobbl.toml.TomlParser.ObjectnameContext;
import es.wobbl.toml.TomlParser.PairContext;
import es.wobbl.toml.TomlParser.StringContext;
import es.wobbl.toml.TomlParser.TomlContext;
import es.wobbl.toml.TomlParser.ValueContext;

public class Toml extends TomlBaseVisitor<Object> {
	final KeyGroup root = new KeyGroup("__root__");

	private Toml() {
	}

	public static KeyGroup parse(InputStream in) throws IOException {
		final TomlLexer lexer = new TomlLexer(new ANTLRInputStream(in));
		final TomlParser parser = new TomlParser(new CommonTokenStream(lexer));
		final Toml visitor = new Toml();
		return (KeyGroup) visitor.visit(parser.toml());
	}

	@Override
	public Object visitToml(TomlContext ctx) {
		visitPairs(ctx, root);
		for (final ObjectContext object : ctx.object()) {
			final KeyGroup keyGroup = (KeyGroup) visitObject(object);
			root.putRecursive(keyGroup.getName(), keyGroup);
		}

		return root;
	}

	@Override
	public Object visitHeader(HeaderContext ctx) {
		return visitObjectname(ctx.objectname());
	}

	@Override
	public Object visitObjectname(ObjectnameContext ctx) {
		return ctx.getText();
	}

	@Override
	public Object visitPair(PairContext ctx) {
		final String name = (String) visitName(ctx.name());
		final Object value = visitValue(ctx.value());
		return new Pair<String, Object>(name, value);
	}

	/*
	 * \0 - null character (0x00) \t - tab (0x09) \n - newline (0x0a) \r -
	 * carriage return (0x0d) \" - quote (0x22) \\ - backslash (0x5c)
	 */
	@Override
	public Object visitString(StringContext ctx) {
		// extract between quotes and unescape
		final String s = ctx.getText();
		return s.substring(1, s.length() - 1).replace("\\0", "\0").replace("\\t", "\t").replace("\\n", "\n").replace("\\r", "\r")
				.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	@Override
	public Object visitName(NameContext ctx) {
		return ctx.getText();
	}

	@Override
	public Object visitNumber(NumberContext ctx) {
		final String number = ctx.getText();
		try {
			return Long.parseLong(number);
		} catch (final NumberFormatException e1) {
			try {
				return new BigInteger(number);
			} catch (final NumberFormatException e2) {
				return Double.parseDouble(number);
			}

		}
	}

	@Override
	public Object visitBool(BoolContext ctx) {
		return Boolean.parseBoolean(ctx.getText());
	}

	private void visitPairs(ParserRuleContext ctx, KeyGroup keyGroup) {
		for (int i = 0; i < ctx.getChildCount(); i++) {
			final PairContext pairCtx = ctx.getChild(PairContext.class, i);
			if (pairCtx == null)
				break;
			final Pair<String, Object> pair = (Pair<String, Object>) visitPair(pairCtx);
			keyGroup.put(pair.a, pair.b);
		}
	}

	@Override
	public Object visitObject(ObjectContext ctx) {
		final String objectName = (String) visitHeader(ctx.header());
		final KeyGroup keyGroup = new KeyGroup(objectName);
		visitPairs(ctx, keyGroup);
		return keyGroup;
	}

	@Override
	public Object visitDatetime(DatetimeContext ctx) {
		return DatatypeConverter.parseDateTime(ctx.getText());
	}

	@Override
	public Object visitArray(ArrayContext ctx) {
		final ArrayList<Object> arr = Lists.newArrayListWithCapacity(ctx.getChildCount());
		for (final ValueContext value : ctx.value()) {
			arr.add(visitValue(value));
		}
		return arr;
	}
}
