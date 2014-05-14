package es.wobbl.toml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SerializerTest {

	StringBuilder out = null;

	@Before
	public void createOutput() {
		out = new StringBuilder();
	}

	@Test
	@SuppressWarnings("unused")
	public void testPojoSerialization() throws IOException {
		Serializer.serialize(new Object() {
			public final BigInteger big = new BigInteger("1111111111111111111111111111111111");
			public final boolean bool = true;
			public final byte b = 1;
			public final int foo = 1;
			public final boolean[] booleanArray = { true, false };
			public final String[] stringArray = { "a", "b", "c" };
			public final Integer[] array = { 1, 2, 3 };
			public final Integer[] emptyArray = {};
			public final List<Integer> bar = ImmutableList.of(1, 2, 3);
			public final String myName = "toml";
			public final Object obj = new Object() {
				public final List<Integer> bar2 = ImmutableList.of(1, 2, 3);
				public final String myName2 = "toml";
				public final Object obj = new Object() {
					public final List<Integer> bar2 = ImmutableList.of(1, 2, 3);
					public final String myName2 = "toml";
				};
			};
			public final Object obj2 = new Object() {
				public final List<Integer> bar2 = ImmutableList.of(1, 2, 3);
				public final String myName2 = "toml";
				public final Object obj = new Object() {
					public final List<Integer> bar2 = ImmutableList.of(1, 2, 3);
					public final String myName2 = "toml";
				};
			};
		}, out);
		System.out.println(out.toString());
		final KeyGroup root = Toml.parse(out.toString());
		System.out.println("-------------");
		Serializer.serialize(root, System.out);
		final ImmutableList<Long> list = ImmutableList.of(1L, 2L, 3L);
		assertTrue(root.getBool("bool"));
		assertEquals(new BigInteger("1111111111111111111111111111111111"), root.getBigInteger("big"));
		assertEquals(ImmutableList.of(), root.getList("emptyArray", Long.class));
		assertEquals(ImmutableList.of("a", "b", "c"), root.getList("stringArray", String.class));
		assertEquals(ImmutableList.of(true, false), root.getList("booleanArray", Boolean.class));
		assertEquals(list, root.getList("array", Long.class));
		assertEquals(list, root.getList("bar", Long.class));
		assertEquals(list, root.getList("obj.bar2", Long.class));
		assertEquals(list, root.getList("obj.obj.bar2", Long.class));
		assertEquals(list, root.getList("obj2.obj.bar2", Long.class));
	}

	@Test
	public void testTomlSerialization() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/full.toml"));
		Serializer.serialize(root, out);
		final KeyGroup root2 = Toml.parse(out.toString());
		assertEquals(root, root2);
	}

	@Test
	public void testSerializeCalendar() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0L);
		Serializer.serializeValue(calendar, out);
		assertEquals("1970-01-01T00:00:00Z", out.toString());
	}

	@Test
	public void testSerializeDate() throws Exception {
		final StringBuilder out = new StringBuilder();
		Serializer.serializeValue(new Date(0L), out);
		assertEquals("1970-01-01T00:00:00Z", out.toString());
	}

	@Test
	public void testSerializeInstant() throws Exception {
		final StringBuilder out = new StringBuilder();
		Serializer.serializeValue(Instant.ofEpochMilli(0L), out);
		assertEquals("1970-01-01T00:00:00Z", out.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializeOutputStream() throws Exception {
		Serializer.serializeValue(new ByteArrayOutputStream(), out);
	}
}
