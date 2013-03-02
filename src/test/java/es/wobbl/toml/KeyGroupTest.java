package es.wobbl.toml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KeyGroupTest {

	@Test
	public void testRecursiveKeyLookup() {
		final KeyGroup root = new KeyGroup("root");
		final KeyGroup c1 = new KeyGroup("c1");
		final KeyGroup c2 = new KeyGroup("c1.c2");
		root.put("c1", c1);
		c1.put("c2", c2);
		c2.put("value", "hello");
		assertEquals("hello", root.get("c1.c2.value"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRecursiveKeyLookupFailure() {
		final KeyGroup root = new KeyGroup("root");
		final KeyGroup c1 = new KeyGroup("c1", true);
		root.put("c1", c1);
		assertEquals("hello", root.get("c1.c2.value"));
	}

	@Test
	public void testPutRecursive() {
		final KeyGroup root = new KeyGroup("__root__");
		root.putRecursive("foo.bar.baz", "hello");
		assertEquals("hello", root.get("foo.bar.baz"));
	}
}
