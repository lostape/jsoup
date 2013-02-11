package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;

public class NodeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	// TODO : add the tag <br> type of tag
	
	@Test
	public void absUrl() {
		Tag tag = Tag.valueOf("a");
		// Testing for relative href and absolute href
        Attributes attributes = new Attributes();
        attributes.put("relHref", "/example");
        attributes.put("absHref", "http://google.com/");
        
        // No URI provided
		Element hrefElNoUri = new Element(tag, "", attributes);
		assertEquals("", hrefElNoUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElNoUri.absUrl("absHref"));
		
		// Valid URI (valid protocol)
		Element hrefElValidUri = new Element(tag, "https://google.com/", attributes);
		assertEquals("https://google.com/example", hrefElValidUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElValidUri.absUrl("absHref"));
		
		// False protocol
		Element hrefElInvalidUri = new Element(tag, "lol://google.com/", attributes);
		assertEquals("", hrefElInvalidUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElInvalidUri.absUrl("absHref"));
	}

	@Test
	public void insertAfter() {
		Tag tagSibling = Tag.valueOf("a");
		Element sibling = new Element(tagSibling, "");
		
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><p></p><a></a></div>");
		
		docBefore.select("p").first().after(sibling);
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}
	
	@Test
	public void insertAfterString() {
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><p></p><b>testing</b></div>");
		
		docBefore.select("p").first().after("<b>testing</b>");
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}
	
	@Test
	public void getAttr() {
		// No base URI
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
        attributes.put("title", "testing");
		Node nodeWithoutURI = new Element(tag, "", attributes);
		assertEquals("testing", nodeWithoutURI.attr("title"));
		assertEquals("", nodeWithoutURI.attr("abs:title"));
		
		// Base URI
		tag = Tag.valueOf("a");
		attributes = new Attributes();
        attributes.put("href", "testing");
        Node nodeWithURI = new Element(tag, "http://testing.com/", attributes);
		assertEquals("", nodeWithURI.attr("title"));
		assertEquals("http://testing.com/testing", nodeWithURI.attr("abs:href"));
	}
	
	@Test
	public void setAttr() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Empty attribute
		assertEquals("", node.attr("title"));
		// Standard attribute insertion
		node.attr("title", "testing");
		assertEquals("testing", node.attr("title"));
		// Erasing insertion
		node.attr("title", "noMoreTesting");
		assertEquals("noMoreTesting", node.attr("title"));
	}
	
	@Test
	public void getAttributes() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// No attributes
		Attributes attributesTest = new Attributes();
		assertEquals(attributesTest, node.attributes());
		// One attributes
		node.attr("title", "testing");
		attributesTest.put("title", "testing");
		assertEquals(attributes, node.attributes());
		// Several attributes
		node.attr("class", "testing");
		node.attr("align", "testing");
		attributesTest.put("class", "testing");
		attributesTest.put("align", "testing");
		assertEquals(attributesTest, node.attributes());
	}
	
	@Test
	public void baseUri() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		// No URI
		Node nodeNoURI = new Element(tag, "", attributes);
		assertEquals("", nodeNoURI.baseUri());
		// URI present
		Node nodeURI = new Element(tag, "/testing", attributes);
		assertEquals("/testing", nodeURI.baseUri());
	}
	
	@Test
	public void insertBefore() {
		Tag tagSibling = Tag.valueOf("a");
		Element sibling = new Element(tagSibling, "");
		
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><a></a><p></p></div>");
		
		docBefore.select("p").first().before(sibling);
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}
	
	@Test
	public void insertBeforeString() {
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><b>testing</b><p></p></div>");
		
		docBefore.select("p").first().before("<b>testing</b>");
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void childNode() {
		// No children
		Document noChild = Jsoup.parseBodyFragment("");
		assertEquals(null, noChild.body().childNode(0));
		// Only one child
		Tag tag = Tag.valueOf("div");
		Document oneChild = Jsoup.parseBodyFragment("<div></div>");
		assertEquals((new Element(tag, "")).toString(), oneChild.body().childNode(0).toString());
		// i th child
		tag = Tag.valueOf("a");
		Document severalChildren = Jsoup.parseBodyFragment("<div></div><p></p><a></a>");
		assertEquals((new Element(tag, "")).toString(), severalChildren.body().childNode(2).toString());
		// IndexOutOfBoundsException raised
		severalChildren.body().childNode(3).toString();
	}
	
	@Test
	public void childNodes() {
		/* Tests both childNodes and childNodeSize functions */
		// No children
		Document noChild = Jsoup.parseBodyFragment("");
		assertEquals(0, noChild.body().childNodeSize());
		assertEquals(new ArrayList<Node>(), noChild.body().childNodes());
		// Some children
		Document children = Jsoup.parseBodyFragment("<div></div><p></p><a></a>");
		List<Node> listNodes = new ArrayList<>();
		listNodes.add(children.body().childNode(0));
		listNodes.add(children.body().childNode(1));
		listNodes.add(children.body().childNode(2));
		assertEquals(3, children.body().childNodeSize());
		assertEquals(listNodes, children.body().childNodes());
	}

	@Test
	public void cloneNode() {
		// Testing the same content
		Node toCopy = Jsoup.parseBodyFragment("<div><p></p><a></a></div>").body();
		Node copied = toCopy.clone();
		// Testing the same content for both the original and the copy
		assertEquals(copied.toString(), toCopy.toString());
		
		// Testing after modification*
		copied.childNode(0).after("<a>testing</a>");
		assertNotSame(copied.toString(), toCopy.toString());
	}
	
	@Test
	public void hasAttr() {
	}
	
	@Test
	public void test() {
	}

}
