package es.wobbl.toml;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

	// TODO: handle arrays
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
			if (Util.isTomlPrimitive(cur)) {
				out.append(field.getKey()).append(" = ");
				Util.serializeValue(out, cur);
				out.append('\n');
			}
		}

		for (final Entry<String, Object> field : fields) {
			final Object cur = field.getValue();
			if (!Util.isTomlPrimitive(cur)) {
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
			if (!Util.isTomlPrimitive(cur))
				continue;
			out.append(entry.getKey()).append(" = ");

			Util.serializeValue(out, cur);
			out.append('\n');
		}

		for (final Map.Entry<String, Object> entry : obj.entrySet()) {
			final Object cur = entry.getValue();
			if (!Util.isTomlPrimitive(cur)) {
				serialize(cur, out);
				out.append('\n');
			}
		}
	}

}
