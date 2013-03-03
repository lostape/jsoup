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

}
