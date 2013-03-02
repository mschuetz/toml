# A TOML Parser for Java
A [TOML](https://github.com/mojombo/toml) parser for Java powered by [ANTLR 4](http://antlr.org).
[![Build Status](https://secure.travis-ci.org/mschuetz/toml.png)](http://travis-ci.org/mschuetz/toml)

## Example Usage:

	import es.wobbl.toml.Toml
	
	KeyGroup root = Toml.parse(new FileInputStream("example.toml"))
	String ip = root.getString("servers.alpha.ip")
	List<Long> ports = root.getList("database.ports", Long.class)
	
## MIT License

Copyright (c) 2013 Matthias Schütz

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
