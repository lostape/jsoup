package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.*;

public class NodeTestMutation {

	@Rule
    public Timeout globalTimeout = new Timeout(500);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// MUTATION TESTS

	@Test(expected = IllegalArgumentException.class)
	public void mutationGetAttr() {
		// No attribute
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		assertEquals(null, node.attr(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationSetAttr() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		Node newone = node.attr(null, null);
		assertNotNull(newone);
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationHasAttr() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		assertEquals(null, node.hasAttr(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationSetBaseUri() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.setBaseUri(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationAbsUrl() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.absUrl("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationRemove() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.remove();
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationBeforeParentNodeNull() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.before(new Element(tag, ""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationAfterParentNodeNull() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.after(new Element(tag, ""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mutationReplaceParentNodeNull() {
		Tag tag = Tag.valueOf("a");
		Node node = new Element(tag, "");
		node.replaceWith(new Element(tag, ""));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void mutation_unwrap_noParent() {
		// No parent unwrap
		Document doc = new Document("");
		Element noParentElement = doc.createElement("a");
		
		try
		{
			noParentElement.unwrap();
			fail();
		}
		catch (IllegalArgumentException e)
		{
			throw e;
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void mutation_wrap_noHtml() {
		// No HTML wrap
		Node noParentElement = new Element(Tag.valueOf("a"), "");
		try
		{
			noParentElement.wrap("");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			throw e;
		}
	}
	
	@Test
	public void mutation_wrap_nonNullReturn() {
		Document doc = Jsoup.parseBodyFragment("<div></div>");
		Element element = doc.select("div").first();
		Element after = element.wrap("<p>");
		assertNotNull(after);
	}
}
