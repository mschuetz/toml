package es.wobbl.toml;

import java.util.Map;

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

	public void add(String key, Object value) {
		members.put(key, value);
	}

	public boolean isRoot() {
		return root;
	}
}
