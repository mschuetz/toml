package es.wobbl.toml;

import java.io.IOException;
import java.io.InputStream;

public class VisitorTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final InputStream in = VisitorTest.class.getResourceAsStream("/sample1.toml");
		System.out.println(Toml.parse(in));
	}
}
