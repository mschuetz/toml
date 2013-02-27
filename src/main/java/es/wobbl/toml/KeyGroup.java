package es.wobbl.toml;

import java.util.Map;

import com.google.common.base.Preconditions;

public class KeyGroup {

	private final String name;
	private final Map<String, Object> members = Util.newMap();

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

	public Object getMember(String path) {
		KeyGroup cur = this;
		int i = 0;
		final String[] parts = path.split("\\.");
		for (final String key : parts) {
			if (i < parts.length - 1) {
				cur = (KeyGroup) cur.members.get(key);
				Preconditions.checkArgument(cur != null, "child node doesn't exist: %s", path);
			} else {
				Object obj = cur.members.get(key);
				Preconditions.checkArgument(obj != null, "child node doesn't exist: %s", path);
				return obj;
			}
			i++;
		}
		throw new IllegalStateException("unreachable");
	}

	public void addMember(String key, Object value) {
		members.put(key, value);
	}

	public boolean isRoot() {
		return root;
	}
}
