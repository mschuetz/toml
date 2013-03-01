package es.wobbl.toml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.common.base.Charsets;

public class VisitorTest {

	@Test
	public void testSimple() throws IOException {
		final String toml = "[foo]\nkey = \"value\"";
		final KeyGroup root = Toml.parse(new ByteArrayInputStream(toml.getBytes(Charsets.UTF_8)));
		assertEquals("foo", root.getKeyGroup("foo").getName());
		assertEquals("value", root.getKeyGroup("foo").get("key"));
		assertEquals("value", root.getString("foo.key"));
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final InputStream in = VisitorTest.class.getResourceAsStream("/sample1.toml");
		final KeyGroup kg = Toml.parse(in);
		System.out.println(kg);
	}
}
