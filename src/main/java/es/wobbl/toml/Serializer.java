package es.wobbl.toml;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Serializer {

	private static class TomlField implements Entry<String, Object> {
		final String name;
		final Object o;

		public TomlField(String name, Object o) {
			this.name = name;
			this.o = o;
		}

		@Override
		public String toString() {
			return "TomlField [name=" + name + ", o=" + o + "]";
		}

		@Override
		public String getKey() {
			return name;
		}

		@Override
		public Object getValue() {
			return o;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("immutable entry");
		}
	}

	static Iterable<Entry<String, Object>> iterFields(final Map<String, Object> map) {
		return map.entrySet();
	}

	static Iterable<Entry<String, Object>> iterFields(final Object o) {

		return new Iterable<Entry<String, Object>>() {

			@Override
			public Iterator<Entry<String, Object>> iterator() {
				// TODO get lazy iterator of array
				return Iterables.transform(Iterables.filter(Lists.newArrayList(o.getClass().getFields()), new Predicate<Field>() {

					@Override
					public boolean apply(Field field) {
						final int mods = field.getModifiers();
						return /* field.isAccessible() && */Modifier.isPublic(mods) && !Modifier.isStatic(mods);
					}
				}), new Function<Field, Entry<String, Object>>() {

					@Override
					public Entry<String, Object> apply(Field field) {
						try {
							return new TomlField(field.getName(), field.get(o));
						} catch (final IllegalAccessException e) {
							throw new RuntimeException(
									"checked if a field was accessible but it turned out not to be. should not happen", e);
						}
					}

				}).iterator();
			}
		};
	}

	public static void serialize(Object o, Appendable out) throws IOException {
		serialize("", o, out);
	}

	public static void serialize(String path, Object o, Appendable out) throws IOException {
		// if not root object
		if (!Strings.isNullOrEmpty(path))
			out.append('[').append(path).append("]\n");

		if (o instanceof KeyGroup) {
			serialize((KeyGroup) o, out);
			return;
		}
		final Iterable<Entry<String, Object>> fields = iterFields(o);
		for (final Entry<String, Object> field : fields) {
			final Object cur = field.getValue();
			if (isTomlPrimitive(cur)) {
				out.append(field.getKey()).append(" = ");
				serializeValue(out, cur);
				out.append('\n');
			}
		}

		for (final Entry<String, Object> field : fields) {
			final Object cur = field.getValue();
			if (!isTomlPrimitive(cur)) {
				if (Strings.isNullOrEmpty(path))
					serialize(field.getKey(), field.getValue(), out);
				else
					serialize(path + "." + field.getKey(), field.getValue(), out);
			}
		}
	}

	public static void serialize(KeyGroup obj, Appendable out) throws IOException {
		if (!obj.isRoot())
			out.append('[').append(obj.getName()).append("]\n");

		for (final Map.Entry<String, Object> entry : obj.entrySet()) {
			final Object cur = entry.getValue();
			if (!isTomlPrimitive(cur))
				continue;
			out.append(entry.getKey()).append(" = ");

			serializeValue(out, cur);
			out.append('\n');
		}

		for (final Map.Entry<String, Object> entry : obj.entrySet()) {
			final Object cur = entry.getValue();
			if (!isTomlPrimitive(cur)) {
				serialize(cur, out);
				out.append('\n');
			}
		}
	}

	public static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\0", "\\0").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")
				.replace("\"", "\\\"");
	}

	// TODO: handle arrays
	public static void serializeValue(Appendable out, final Object obj) throws IOException {
		if (obj instanceof Calendar) {
			out.append(DatatypeConverter.printDateTime((Calendar) obj));
		} else if (obj instanceof Iterable<?>) {
			out.append('[');
			final Iterable<?> iterable = (Iterable<?>) obj;
			for (final Iterator<?> it = iterable.iterator(); it.hasNext();) {
				serializeValue(out, it.next());
				if (it.hasNext())
					out.append(", ");
			}
			out.append(']');
		} else if (obj instanceof Object[]) {
			out.append('[');
			final Object[] arr = (Object[]) obj;
			for (int i = 0; i < arr.length; i++) {
				serializeValue(out, arr[i]);
				if (i < arr.length - 1)
					out.append(", ");
			}
			out.append(']');
		} else if (obj.getClass().isArray()) {
			// handle primitive array separately as they cannot be cast to
			// Object[]
			out.append('[');
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				try {
					final Object element = Array.get(obj, i);
					serializeValue(out, element);
				} catch (final IndexOutOfBoundsException e) {
					break;
				}
				try {
					Array.get(obj, i + 1);
				} catch (final IndexOutOfBoundsException e) {
					continue;
				}
				out.append(", ");
			}
			out.append(']');
		} else if (obj instanceof CharSequence) {
			out.append('"');
			out.append(escape(obj.toString()));
			out.append('"');
		} else if (obj instanceof Number || obj instanceof Boolean) {
			out.append(obj.toString());
		}
	}

	/**
	 * @return true if the object is of a type that may be the right-hand side
	 *         of an assignment in toml. Currently this means, if it's not one
	 *         of Long, Double, String, Calendar, Iterables except Maps, which
	 *         are handled like objects
	 */
	public static boolean isTomlPrimitive(Object o) {
		/*
		 * regarding numbers, what I really would like to check is: if it is a
		 * floating point number: check if it's within the bounds of double if
		 * it is an integer: check if it's within the bounds of a signed long
		 * 
		 * problem: How do I generically find out whether a child class of
		 * Number is fixed or floating? all they have in common are conversion
		 * methods like intValue longValue...
		 * 
		 * <s>idea: call longValue & doubleValue and check for equality.</s>
		 * doesn't work
		 */
		return (o instanceof Number) || (o instanceof CharSequence) || (o instanceof Calendar) || (o instanceof Boolean)
				|| ((o instanceof Iterable) && !((o instanceof Map))) || (o.getClass().isArray());
	}
}
