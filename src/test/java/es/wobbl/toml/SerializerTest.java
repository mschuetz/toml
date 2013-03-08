package es.wobbl.toml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SerializerTest {
	@Test
	public void testPojoSerialization() throws IOException {
		final StringBuilder out = new StringBuilder();
		Serializer.serialize(new Object() {
			public final byte b = 1;
			public final int foo = 1;
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
		assertEquals(list, root.getList("bar", Long.class));
		assertEquals(list, root.getList("obj.bar2", Long.class));
		assertEquals(list, root.getList("obj.obj.bar2", Long.class));
		assertEquals(list, root.getList("obj2.obj.bar2", Long.class));
	}

	@Test
	public void testTomlSerialization() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/full.toml"));
		final StringBuilder out = new StringBuilder();
		Serializer.serialize(root, out);
		final KeyGroup root2 = Toml.parse(out.toString());
		assertEquals(root, root2);
	}
}
