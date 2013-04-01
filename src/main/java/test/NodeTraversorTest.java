package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jsoup.Jsoup;
import org.jsoup.select.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

public class NodeTraversorTest {
	private String string = "";
	public class toString implements NodeVisitor {
		
		@Override
		public void head(Node node, int depth) {
			string += "<" + node.nodeName() + ">";
		}

		@Override
		public void tail(Node node, int depth) {
			string += "</" + node.nodeName() + ">";
		}
		
	}
	
	@Test
	public void first() {
		Document doc = Jsoup.parseBodyFragment("<div><p></p><a></a></div>");
		Node htmlTree = doc.select("div").first();
		toString toString = new toString();
		NodeTraversor nt = new NodeTraversor(toString);
		nt.traverse(htmlTree);
		assertEquals("<div><p></p><a></a></div>", string);
	}
		
}
