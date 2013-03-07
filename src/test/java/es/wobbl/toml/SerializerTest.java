package es.wobbl.toml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SerializerTest {
	@Test
	public void testPojoSerialization() throws IOException {
		Serializer.serialize(new Object() {
			public final byte b = 1;
			public final int foo = 1;
			public final List<Integer> bar = ImmutableList.of(1, 2, 3);
			public final String myName = "toml";
			public final Object obj = new Object() {
				public final List<Integer> bar2 = ImmutableList.of(1, 2, 3);
				public final String myName2 = "toml";
			};
		}, System.out);
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
