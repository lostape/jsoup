package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

public class JsoupWhiteboxTests {

	//Normal Data tag tests
	
	@Test
	public void DataStateNullCharTest() {
		String html = "<tag>NullChar\0</tag>";
		Document d = Jsoup.parse(html);
		
		assertEquals("tag",d.body().child(0).tagName());
		assertEquals("NullChar",d.body().child(0).html());
	}
	
	//RcData tests
	
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
	
	//RawText tests
	
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
	
	//Script tests
	
	@Test
	public void ScriptDataNormalTest() {
		String html = "<script><normaltag>sometext</normaltag></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<normaltag>sometext</normaltag>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDataNullCharTest() {
		String html = "<script>NullChar\0</script>";
		Document d = Jsoup.parse(html);
		//Jsoup replaces null character with replacement char
		assertEquals("NullChar\uFFFD", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDataEOFTest() {
		char eof = (char) -1;
		String html = "<script>"+eof+"Stuff after will not be parsed</script>";
		Document d = Jsoup.parse(html);

		assertEquals("", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDataImproperEndTagTest() {
		String html = "<script>Fake endtag script, no space allowed </ script></script>";
		Document d = Jsoup.parse(html);

		assertEquals("Fake endtag script, no space allowed </ script>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDataSelfClosingTagTest() {
		String html = "<script>Improper syntax for self closing tag</script/>";
		Document d = Jsoup.parse(html);

		assertEquals("Improper syntax for self closing tag", d.head().child(0).data());
		fail();
	}
	
	@Test
	public void ScriptEndTagWithAttributeTest() {
		String html = "<script>Hello</script attr=\"1\"/>";
		Document d = Jsoup.parse(html);

		assertEquals("",d.head().child(0).attr("attr"));
		assertEquals("Hello", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEndTagEndsAbruptlyTest() {
		String html = "<script>Hello</script";
		Document d = Jsoup.parse(html);

		assertEquals("Hello</script", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEndTagTest() {
		String html = "<script>Hello</script89764>";
		Document d = Jsoup.parse(html);

		assertEquals("Hello</script89764>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTest() {
		String html = "<script><!EscapeTagNoDash></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<!EscapeTagNoDash>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagOneDashTest() {
		String html = "<script><!-EscapeTagNoDash></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<!-EscapeTagNoDash>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTwoStartDashTest() {
		String html = "<script><!--EscapeTagNoDash></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<!--EscapeTagNoDash>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTwoEndDashTest() {
		String html = "<script><!--EscapeTagNoDash--></script>";
		Document d = Jsoup.parse(html);

		assertEquals("<!--EscapeTagNoDash-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTwoDashEndingAbruptlyTest() {
		String html = "<script><!--";
		Document d = Jsoup.parse(html);

		assertEquals("<!--", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTwoDashDataEndingAbruptlyTest() {
		String html = "<script><!--Ending...";
		Document d = Jsoup.parse(html);

		assertEquals("<!--Ending...", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNormalTest() {
		String html = "<script><!--<escapetag>Escaping a second time</escapetag></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNumberTagTest() {
		String html = "<script><!--<1234>Escaping a second time</1234></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<1234>Escaping a second time</1234>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptTest() {
		String html = "<script><!--<script>Data in second script</script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptTest() {
		String html = "<script><!--<script>Data in second script--></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script--></script>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptLotsofDashesTest() {
		String html = "<script><!--<script>Data in second script</script>-----></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>----->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptLotsofDashesTest() {
		String html = "<script><!--<script>Data in second script-----></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-----></script>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptDashFakeOutTest() {
		String html = "<script><!--<script>Data in second script</script>--FakeOut!--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>--FakeOut!-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptDashFakeOutTest() {
		String html = "<script><!--<script>Data in second script--FakeOut!--></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script--FakeOut!--></script>", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptOneDashThenTagTest() {
		String html = "<script><!--<script>Data in second script</script>-<tag>tag</tag>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>-<tag>tag</tag>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptOneDashThenTagTest() {
		String html = "<script><!--<script>Data in second script-<tag>tag</tag>--></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-<tag>tag</tag>--></script>-<tag>tag</tag>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptWithOneDashThenTagTest() {
		String html = "<script><!--<script>Data in second script-<tag>tag</tag></script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-<tag>tag</tag></script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptWithOneDashThenDataTest() {
		String html = "<script><!--<script>Data in second script-Data</script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-Data</script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptOneDashThenDataTest() {
		String html = "<script><!--<script>Data in second script</script>-Data--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>-Data-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptOneDashThenEndTest() {
		String html = "<script><!--<script>Data in second script</script>-";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>-", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptDashesDataThenDashesTest() {
		String html = "<script><!--<script>Data in second script</script>--Data----></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>--Data---->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptDashesDataThenDashesThenDataTest() {
		String html = "<script><!--<script>Data in second script</script>--Data--Data--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>--Data--Data-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptDashFakeOutThenTagTest() {
		String html = "<script><!--<script>Data in second script</script>--Data--<tag>Hello</tag>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>--Data--<tag>Hello</tag>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNormalTagTest() {
		String html = "<script><!--<script><tag>Hello</tag></script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script><tag>Hello</tag></script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeDashesBeforeSecondScriptEndTagTest() {
		String html = "<script><!--<script><tag>Hello</tag>--</script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script><tag>Hello</tag>--</script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptNestedScriptTest() {
		String html = "<script><script>Second Script</script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<script>Second Script</script>", d.head().child(0).data());
		assertEquals("Second Script", d.head().child(0).child(0).data());
	}
	
	//Attribute tests
	
	@Test
	public void AttributeTagEndingWithSpace() {
		String html = "<tag >Tag with a space</tag>";
		Document d = Jsoup.parse(html);
		
		assertEquals("tag", d.body().child(0).tagName());
		assertEquals("Tag with a space", d.body().child(0).html());
	}
	
	@Test
	public void AttributeWeirdNames() {
		String html = "<tag \t\n\r\f ==equals \0<\"\\=weird1 \"=weird2 \\=w3 <=w4 name\t\n\r\f\t=normal endname/>";
		Document d = Jsoup.parse(html);
		
		Element e = d.body().child(0);
		assertEquals("tag", e.tagName());
		assertEquals("equals", e.attr("="));
		assertEquals("weird1", e.attr("\uFFFD<\"\\"));
		assertEquals("weird2", e.attr("\""));
		assertEquals("w3", e.attr("\\"));
		assertEquals("w4", e.attr("<"));
		assertEquals("", e.attr("endname"));
		assertEquals("normal", e.attr("name"));
	}
	
	@Test
	public void AttributeWeirdNamesWeirdAfterAttributeName() {
		String html = "<tag ==equals \0<\"\\    =weird1 \" =weird2 \\=w3 <=w4 beforenull \0=w5 name=normal weird \"2 \\2 <2 endname >";
		Document d = Jsoup.parse(html);
		
		Element e = d.body().child(0);
		assertEquals("tag", e.tagName());
		assertEquals("equals", e.attr("="));
		assertEquals("weird1", e.attr("\uFFFD<\"\\"));
		assertEquals("weird2", e.attr("\""));
		assertEquals("w3", e.attr("\\"));
		assertEquals("w4", e.attr("<"));
		assertEquals("w5", e.attr("beforenull\uFFFD"));
		assertEquals("", e.attr("\"2"));
		assertEquals("", e.attr("\\2"));
		assertEquals("", e.attr("<2"));
		assertEquals("", e.attr("endname"));
		assertEquals("normal", e.attr("name"));
	}
	
	@Test
	public void AttributeBeforeAttributeValueTest() {
		String html = "<tag a=\t\n\r\f \"&#62\"\t b=\"&\"\n c=\"\0\"\f d=&\t\n\r\f #62 e='three'\r f=\0Null\0 g=<=` " +
				"i='&#62' j='&gt' k='\0' l=&gt m=&\"\\<=` n=>";
		Document d = Jsoup.parse(html);
		
		Element e = d.body().child(0);
		assertEquals("tag", e.tagName());
		assertEquals(">", e.attr("a"));
		assertEquals("&", e.attr("b"));
		assertEquals("\uFFFD", e.attr("c"));
		assertEquals("&", e.attr("d"));
		assertEquals("three", e.attr("e"));
		assertEquals("\uFFFDNull\uFFFD", e.attr("f"));
		assertEquals("<=`", e.attr("g"));
		assertEquals(">", e.attr("i"));
		assertEquals(">", e.attr("j"));
		assertEquals("\uFFFD", e.attr("k"));
		assertEquals(">", e.attr("l"));
		assertEquals("&\"\\<=`", e.attr("m"));
		assertEquals("", e.attr("n"));
	}
	
	@Test
	public void SelfClosingTagStartTest(){
		String html = "<tag /fakeoutselfclosingtag=hello />";
		
		Document d = Jsoup.parse(html);
		assertEquals("hello", d.body().child(0).attr("/fakeoutselfclosingtag"));	
	}
	
	//Markup Tests
	//Comment tests
	
	@Test
	public void CommentCommentStartDashTest(){
		String html = "<!----><!----I'm a comment--><!----\0Null-->";
		
		Document d = Jsoup.parse(html);
		
		assertEquals("", ((Comment) d.childNode(0)).getData());
		assertEquals("I'm a comment", ((Comment) d.childNode(1)).getData());
		assertEquals("\uFFFDNull", ((Comment) d.childNode(2)).getData());
	}
	
	@Test
	public void CommentCommentTest(){
		String html = "<!--><!--\0NullComment\0asdf-asdf-\0--\0--asdf-----><!--CommentBang--!--!asdf--!\0--!>";
		
		Document d = Jsoup.parse(html);
		
		assertEquals("", ((Comment) d.childNode(0)).getData());
		assertEquals("\uFFFDNullComment\uFFFDasdf-asdf-\uFFFD--\uFFFD--asdf---", ((Comment) d.childNode(1)).getData());
		assertEquals("CommentBang--!--!asdf--!\uFFFD", ((Comment) d.childNode(2)).getData());
	}
	
}
