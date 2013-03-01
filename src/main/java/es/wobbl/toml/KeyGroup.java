package es.wobbl.toml;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class KeyGroup {

	private final String name;
	private final Map<String, Object> members = Maps.newLinkedHashMap();
	private final boolean root;

	public KeyGroup(String name, boolean root) {
		this.name = Preconditions.checkNotNull(name);
		this.root = root;
	}

	public KeyGroup(String name) {
		this(name, false);
	}

	public String getName() {
		return name;
	}

	/**
	 * recursively looks up a child element of this key group
	 */
	public Object get(String path) {
		KeyGroup cur = this;
		int i = 0;
		final String[] parts = path.split("\\.");
		for (final String key : parts) {
			final Object obj = cur.members.get(key);
			Preconditions.checkArgument(obj != null, "child node doesn't exist: %s", path);
			if (i < parts.length - 1) {
				Preconditions.checkArgument(obj instanceof KeyGroup, "not at end of path but got non-keygroup child");
				cur = (KeyGroup) obj;
			} else {
				return obj;
			}
			i++;
		}
		throw new IllegalStateException("unreachable");
	}

	public String getString(String path) {
		return (String) get(path);
	}

	public long getLong(String path) {
		return (Long) get(path);
	}

	public boolean getBool(String path) {
		return (Boolean) get(path);
	}

	public Calendar getCalendar(String path) {
		return (Calendar) get(path);
	}

	public KeyGroup getKeyGroup(String path) {
		return (KeyGroup) get(path);
	}

	public void put(String key, Object value) {
		members.put(key, value);
	}

	/**
	 * creates keygroups recursively up to the second last path segment and puts
	 * the given object into the last keygroup under the last path segment as
	 * name.
	 * 
	 * new keygroups along the path will be created if they don't exist yet. an
	 * {@link IllegalArgumentException} is raised.
	 * 
	 * @throws IllegalArgumentException
	 *             if it finds a non-keygroup object along the path
	 */
	public void putRecursive(String path, Object obj) {
		KeyGroup cur = this;
		int i = 0;
		final String[] parts = path.split("\\.");
		for (final String key : parts) {
			if (i < parts.length - 1) {
				final Object next = cur.members.get(key);
				if (next == null) {
					final KeyGroup nextKeyGroup = new KeyGroup(key);
					cur.members.put(key, nextKeyGroup);
					cur = nextKeyGroup;
				} else {
					Preconditions.checkArgument(next instanceof KeyGroup, "not at end of path but got non-keygroup child");
					cur = (KeyGroup) next;
				}
			} else {
				cur.members.put(key, obj);
			}
			i++;
		}
	}

	public boolean isRoot() {
		return root;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
