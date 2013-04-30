package es.wobbl.toml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class KeyGroupTest {

	@Test
	public void testUnicodeEscapeSequence() throws IOException {
		final String src = "[unicode]\nsnowman = \"\\u2603\"";
		final KeyGroup kg = Toml.parse(src);
		assertEquals(kg.getString("unicode.snowman"), "☃");
	}

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

	@Test
	public void testTypes() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/types.toml"));
		assertEquals(1234567890L, root.getLong("types.long"));
		assertEquals(3.14159, root.getDouble("types.double1"), 1E-6);
		assertEquals(1E6, root.getDouble("types.double2"), 1E-6);
		assertEquals("foo\nbar\tbaz", root.getString("types.string"));
		assertEquals(1000000000L, root.getCalendar("types.iso8601").getTimeInMillis() / 1000L);
	}

	@Test
	public void testTypesInArrays() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/types.toml"));
		assertEquals(Long.valueOf(1234567890L), root.getList("types_in_arrays.long", Long.class).get(0));
		assertEquals(3.14159, root.getList("types_in_arrays.double1", Double.class).get(0), 1E-6);
		assertEquals(1E6, root.getList("types_in_arrays.double2", Double.class).get(0), 1E-6);
		assertEquals("foo\nbar\tbaz", root.getList("types_in_arrays.string", String.class).get(0));
		assertEquals(1000000000L, root.getList("types_in_arrays.iso8601", Calendar.class).get(0).getTimeInMillis() / 1000L);
	}

	@Test
	public void testSpecialCharsInHeader() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/sample1.toml"));
		assertEquals("lentil", root.getString("legume#works\"[.type"));
	}

	@Test
	public void testTopLevel() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/full.toml"));
		assertEquals("TOML Example", root.getString("title"));
	}

	@Test
	public void testHardExample() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/hard_example.toml"));
		final StringBuilder out = new StringBuilder();
		Serializer.serialize(root, out);
		final KeyGroup root2 = Toml.parse(out.toString());
		// assertEquals(root, root2);
		assertEquals(ImmutableList.of("] ", " # "), root.getList("the.hard.test_array", String.class));
		assertEquals(ImmutableList.of("Test #11 ]proved that", "Experiment #9 was a success"),
				root.getList("the.hard.test_array2", String.class));
		assertEquals(" And when \"'s are in the string, along with # \"", root.getString("the.hard.harder_test_string"));

		assertEquals("You don't think some user won't do that?", root.getString("the.hard.bit#.what?"));
		assertEquals(ImmutableList.of("]"), root.getList("the.hard.bit#.multi_line_array", String.class));
	}
}
