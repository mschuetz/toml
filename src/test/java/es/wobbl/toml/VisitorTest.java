package es.wobbl.toml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class VisitorTest {

	@Test
	public void testSimple() throws IOException {
		final String toml = "[foo]\nkey = \"value\"";
		final KeyGroup root = Toml.parse(toml);
		assertEquals("foo", root.getKeyGroup("foo").getName());
		assertEquals("value", root.getKeyGroup("foo").get("key"));
		assertEquals("value", root.getString("foo.key"));
	}

	@Test
	public void testFull() throws IOException {
		final KeyGroup root = Toml.parse(VisitorTest.class.getResourceAsStream("/full.toml"));
		assertEquals("Tom Preston-Werner", root.getString("owner.name"));
		assertEquals("GitHub Cofounder & CEO\nLikes tater tots and beer.", root.getString("owner.bio"));
		assertEquals(ImmutableList.of(8001L, 8001L, 8002L), root.getList("database.ports", Long.class));
		assertEquals("10.0.0.2", root.get("servers.beta.ip"));
		assertEquals("10.0.0.1", root.getKeyGroup("servers").getKeyGroup("alpha").getString("ip"));
		assertEquals(ImmutableList.of(ImmutableList.of("gamma", "delta"), ImmutableList.of(1L, 2L)),
				root.getList("clients.data", List.class));
		assertEquals(7, root.getCalendar("owner.dob").get(Calendar.HOUR_OF_DAY));
		assertEquals(1979, root.getCalendar("owner.dob").get(Calendar.YEAR));
	}
}
