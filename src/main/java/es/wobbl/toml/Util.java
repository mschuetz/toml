package es.wobbl.toml;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

public final class Util {

	private Util() {
	}

	public static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\0", "\\0").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")
				.replace("\"", "\\\"");
	}

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
		} else if (obj instanceof CharSequence) {
			out.append('"');
			out.append(Util.escape(obj.toString()));
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
		 * idea: call longValue & doubleValue and check for equality.
		 */
		return (o instanceof Long) || (o instanceof Byte) || (o instanceof Float) || (o instanceof Integer)
				|| (o instanceof Double) || (o instanceof CharSequence) || (o instanceof Calendar) || (o instanceof Boolean)
				|| ((o instanceof Iterable) && !((o instanceof Map)));
	}
}
