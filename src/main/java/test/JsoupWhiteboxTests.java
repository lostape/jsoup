package test;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class JsoupWhiteboxTests {

	@Test
	public void DataStateNullCharTest() {
		String html = "<tag>NullChar\0</tag>";
		Document d = Jsoup.parse(html);
		
		assertEquals("tag",d.body().child(0).tagName());
		assertEquals("NullChar",d.body().child(0).html());
	}
	
	@Test
	public void RcDataStateReferenceCharTest() {
		String html = "<title><tag>Greaterthan&#62</tag></title>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<tag>Greaterthan></tag>",d.title());
	}
	
	@Test
	public void RcDataStateAmpersandTest() {
		String html = "<title><tag>Greaterthan&</tag></title>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<tag>Greaterthan&</tag>",d.title());
	}
	
	@Test
	public void RcDataStateNullCharTest() {
		String html = "<title>\0NullChar</title>";
		Document d = Jsoup.parse(html);
		//Jsoup replaces null char with appropriate
		assertEquals("\uFFFDNullChar",d.title());
	}
	
	@Test
	public void RcDataStateEOFTest() {
		char eof = (char) -1;
		String html = "<title>"+eof+"Hello</title>";
		Document d = Jsoup.parse(html);
		//Jsoup stops parsing
		assertEquals("",d.title());
	}
	
	@Test
	public void RawTextNormalTest() {
		String html = "<iframe><p>RawText does not parse stuff inside</p></iframe>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(0).text());
	}
	
	@Test
	public void RawTextNullCharTest() {
		String html = "<iframe>NullChar\0</iframe>";
		Document d = Jsoup.parse(html);
		//Jsoup replaces null character with replacement char
		assertEquals("NullChar\uFFFD", d.body().child(0).text());
	}
	
	@Test
	public void RawTextEOFTest() {
		char eof = (char) -1;
		String html = "<iframe>"+eof+"Stuff after eof, will not be parsed</iframe>";
		Document d = Jsoup.parse(html);
		//Jsoup replaces null character with replacement char
		assertEquals("", d.body().child(0).text());
	}
	
	@Test
	public void ScriptDataNormalTest() {
		String html = "<script><normaltag>sometext</normaltag></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<normaltag>sometext</normaltag>", d.head().child(0).html());
	}
	
	@Test
	public void ScriptDataNullCharTest() {
		String html = "<script>NullChar\0</script>";
		Document d = Jsoup.parse(html);
		//Jsoup replaces null character with replacement char
		assertEquals("NullChar\uFFFD", d.head().child(0).html());
	}
	
	@Test
	public void ScriptDataEOFTest() {
		char eof = (char) -1;
		String html = "<script>"+eof+"Stuff after will not be parsed</script>";
		Document d = Jsoup.parse(html);

		assertEquals("", d.head().child(0).html());
	}
	
	@Test
	public void ScriptDataImproperEndTagTest() {
		String html = "<script>Fake endtag script, no space allowed </ script></script>";
		Document d = Jsoup.parse(html);

		assertEquals("Fake endtag script, no space allowed </ script>", d.head().child(0).html());
	}
	
	@Test
	public void ScriptDataSelfClosingTagTest() {
		String html = "<script>Improper syntax for self closing tag</script/>";
		Document d = Jsoup.parse(html);

		assertEquals("Improper syntax for self closing tag", d.head().child(0).html());
		fail();
	}
	
	@Test
	public void ScriptEndTagWithAttributeTest() {
		String html = "<script>Hello</script attr=\"1\"/>";
		Document d = Jsoup.parse(html);

		assertEquals("",d.head().child(0).attr("attr"));
		assertEquals("Hello", d.head().child(0).html());
	}
	
	@Test
	public void ScriptEndTagEndsAbruptlyTest() {
		String html = "<script>Hello</script";
		Document d = Jsoup.parse(html);

		assertEquals("Hello</script", d.head().child(0).html());
	}
	
	@Test
	public void ScriptEndTagTest() {
		String html = "<script>Hello</script89764>";
		Document d = Jsoup.parse(html);

		assertEquals("Hello</script89764>", d.head().child(0).html());
	}

}
