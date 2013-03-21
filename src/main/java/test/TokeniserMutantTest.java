package test;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class TokeniserMutantTest {
	
	@Rule
    public Timeout globalTimeout = new Timeout(500);
	
	@Test
	public void DataReferenceTest() {
		String html = "&Notareference";
		String ref = "&#62";
		Document d = Jsoup.parse(html);
		Document a = Jsoup.parse(ref);
		
		assertEquals("&Notareference",d.body().text());
		assertEquals(">",a.body().text());	
	}
	
	@Test
	public void SelfClosingTagTest() {
		String html = "<selfclosing/ attribute=1>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character 'a' in input state [SelfClosingStartTag]",p.getErrors().get(0).getErrorMessage());
		assertEquals("selfclosing", d.body().child(0).tagName());
		assertEquals("1",d.body().child(0).attr("attribute"));
	}
	
	@Test
	public void RCInvalidEndtag() {
		String html = "<title>InvalidEnd</1234></title>";
		
		Document d = Jsoup.parse(html);
		
		assertEquals("InvalidEnd</1234>",d.title());
	}

	@Test
	public void RCEndtagAttributeTest() {
		String html = "<title>End</title attribute=1>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Attributes incorrectly present on end tag",p.getErrors().get(0).getErrorMessage());
		assertEquals("", d.head().child(0).attr("attribute"));
	}

	
}
