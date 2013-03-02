package es.wobbl.toml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import es.wobbl.toml.grammar.TomlBaseVisitor;
import es.wobbl.toml.grammar.TomlLexer;
import es.wobbl.toml.grammar.TomlParser;
import es.wobbl.toml.grammar.TomlParser.ArrayContext;
import es.wobbl.toml.grammar.TomlParser.BoolContext;
import es.wobbl.toml.grammar.TomlParser.DatetimeContext;
import es.wobbl.toml.grammar.TomlParser.HeaderContext;
import es.wobbl.toml.grammar.TomlParser.NameContext;
import es.wobbl.toml.grammar.TomlParser.NumberContext;
import es.wobbl.toml.grammar.TomlParser.ObjectContext;
import es.wobbl.toml.grammar.TomlParser.PairContext;
import es.wobbl.toml.grammar.TomlParser.StringContext;
import es.wobbl.toml.grammar.TomlParser.TomlContext;
import es.wobbl.toml.grammar.TomlParser.ValueContext;

public final class Toml {

	/**
	 * Parses a toml document from an input stream
	 * 
	 * @param in
	 *            an input stream containing a toml document
	 * @return an unnamed root {@link KeyGroup} containing the top level key
	 *         value pairs and key groups of the document
	 */
	public static KeyGroup parse(InputStream in) throws IOException {
		final TomlLexer lexer = new TomlLexer(new ANTLRInputStream(in));
		final TomlParser parser = new TomlParser(new CommonTokenStream(lexer));
		return KEY_GROUP_VISITORS.visit(parser.toml());
	}

	public static KeyGroup parse(String toml) throws IOException {
		return parse(new ByteArrayInputStream(toml.getBytes(Charsets.UTF_8)));
	}

	private static final StringVisitors STRING_VISITORS = new StringVisitors();
	private static final PairVisitor PAIR_VISITOR = new PairVisitor();
	private static final ObjectVisitors OBJECT_VISITORS = new ObjectVisitors();
	private static final KeyGroupVisitors KEY_GROUP_VISITORS = new KeyGroupVisitors();

	private Toml() {
	}

	private static class KeyGroupVisitors extends TomlBaseVisitor<KeyGroup> {
		@Override
		public KeyGroup visitToml(TomlContext ctx) {
			final KeyGroup root = new KeyGroup("", true);
			visitPairs(ctx, root);
			for (final ObjectContext object : ctx.object()) {
				final KeyGroup keyGroup = visitObject(object);
				root.putRecursive(keyGroup.getName(), keyGroup);
			}

			return root;
		}

		@Override
		public KeyGroup visitObject(ObjectContext ctx) {
			final String objectName = STRING_VISITORS.visitHeader(ctx.header());
			final KeyGroup keyGroup = new KeyGroup(objectName);
			visitPairs(ctx, keyGroup);
			return keyGroup;
		}

		private void visitPairs(ParserRuleContext ctx, KeyGroup keyGroup) {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				final PairContext pairCtx = ctx.getChild(PairContext.class, i);
				if (pairCtx == null)
					break;
				final Pair<String, Object> pair = PAIR_VISITOR.visitPair(pairCtx);
				keyGroup.put(pair.a, pair.b);
			}
		}
	}

	private static class StringVisitors extends TomlBaseVisitor<String> {
		@Override
		public String visitHeader(HeaderContext ctx) {
			final String s = ctx.getText();
			return s.substring(1, s.length() - 1);
		}

		@Override
		public String visitName(NameContext ctx) {
			return ctx.getText();
		}
	}

	private static class PairVisitor extends TomlBaseVisitor<Pair<String, Object>> {
		@Override
		public Pair<String, Object> visitPair(PairContext ctx) {
			final String name = STRING_VISITORS.visitName(ctx.name());
			final Object value = OBJECT_VISITORS.visitValue(ctx.value());
			return new Pair<String, Object>(name, value);
		}
	}

	private static class ObjectVisitors extends TomlBaseVisitor<Object> {
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

		/*
		 * \0 - null character (0x00) \t - tab (0x09) \n - newline (0x0a) \r -
		 * carriage return (0x0d) \" - quote (0x22) \\ - backslash (0x5c)
		 */
		@Override
		public String visitString(StringContext ctx) {
			// extract between quotes and unescape
			final String s = ctx.getText();
			return s.substring(1, s.length() - 1).replace("\\0", "\0").replace("\\t", "\t").replace("\\n", "\n")
					.replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
		}

		@Override
		public Boolean visitBool(BoolContext ctx) {
			return Boolean.parseBoolean(ctx.getText());
		}

		@Override
		public Calendar visitDatetime(DatetimeContext ctx) {
			return DatatypeConverter.parseDateTime(ctx.getText());
		}

		@Override
		public List<Object> visitArray(ArrayContext ctx) {
			final ArrayList<Object> arr = Lists.newArrayListWithCapacity(ctx.getChildCount());
			for (final ValueContext value : ctx.value()) {
				arr.add(visitValue(value));
			}
			return arr;
		}

	}
}
