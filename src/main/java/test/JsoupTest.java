package test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;


import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class JsoupTest {

	@Test
	public void plaintextTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/plaintext.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created head tags
		assertEquals("<head></head>",p.child(0).child(0).toString());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		//Get inner html of body tag
		assertEquals("Let's see how Jsoup parses this! Plain text, invalid html document.",p.child(0).child(1).html());
	}
	
	@Test
	public void missingheadTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missinghead.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created head tags
		assertEquals("head",p.child(0).child(0).tagName());
		
		//Check to see if body was created
		assertEquals("body", p.body().tagName());
		//Check if tags in body were parsed 
		assertEquals("title",p.body().child(0).tagName());
		assertEquals("Hey, I am the headless html!",p.body().child(0).html());
		assertEquals("p",p.body().child(1).tagName());
		assertEquals("Some paragraphs",p.body().child(1).html());
	}
	
	@Test
	public void missinghtmltagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missinghtmltag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created html tags
		assertEquals("html",p.child(0).tagName());
		
		//Check if head elements were put into html tag
		assertEquals("head",p.child(0).child(0).tagName());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		assertEquals("Straight into the head and the body, no html tag", p.child(0).child(1).child(0).html());

	}
	
	@Test
	public void missingbodyTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingbody.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if head parsed
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		//Check if body was created and elements in head were correctly put into body
		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.child(0).child(1).child(0).tagName());
		assertEquals("Hello, My body is in my head. What?!",p.child(0).child(1).child(0).html());
	}
	
	
	//Doesn't handle interlocking tags very well
	@Test
	public void interlockingtagsTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/interlockingtags.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses first child in body
		assertEquals("div",p.body().child(0).tagName());
		//Check if correctly parses first nested tag
		assertEquals("p",p.body().child(0).child(0).tagName());
		//Check if correctly recognizes interlocking and replaces </div> with </a>
		assertEquals("Interlocking Tags, first paragraph <a href=\"http://google.ca\"> Still in </a> first paragraph!",p.body().child(0).child(0).html());
		
	}
	
	@Test
	public void wrongclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/wrongclosingtagname.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if changes wrong closing tag name to the correct one
		assertEquals("h1",p.body().child(0).tagName());
		assertEquals("<h1>Wrong closing tag name</h1>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void invalidclosingtagsyntaxTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/invalidclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if closes unclosed ending tag, add missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("Invalid closing\n<div></div>",p.body().child(0).html());
		assertEquals("div",p.body().child(0).child(0).tagName());
	}
	
	@Test
	public void missingclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if adds missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Missing closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void unclosedclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/unclosedclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if closes unclosed ending tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Unclosed closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void contentinemptytagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/contentinsideemptytag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if changes content inside start tag to attributes
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("",p.body().child(0).attr("content"));
		assertEquals("",p.body().child(0).attr("inside"));
		assertEquals("",p.body().child(0).attr("again,"));
		assertEquals("",p.body().child(0).attr("different"));
		assertEquals("",p.body().child(0).attr("closing"));
		assertEquals("",p.body().child(0).attr("mechanism"));
	}
	
	@Test
	public void missingclosingquoteonattributeTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingendquote.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly discards tag with no closing quote on one attribute value
		assertEquals("",p.body().html());
	}
	
	@Test
	public void noquotationsforattrvaluesTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/noquotesforattr.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses attribute values even with no quotations
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("noquotesforvalue",p.body().child(0).attr("id"));
	}
	
	@Test
	public void nospacesbetweenattributesTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/nospacebetweenattr.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses attribute values even with no spaces between attributes
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("nospacebetweenattributes",p.body().child(0).attr("id"));
	}
	
	
	
}
