package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class JsoupWhiteboxTests {

	@Rule
    public Timeout globalTimeout = new Timeout(500);
	
	//Plaintext tests
	
	@Test
	public void PlainTextTest() {
		String html = "<plaintext>Null\0</plaintext>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character ' ' in input state [PLAINTEXT]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Null\uFFFD</plaintext>", d.body().child(0).text());
	}
	
	//Normal Data tag tests
	
	@Test
	public void DataStateNullCharTest() {
		String html = "<tag>Null\0Char</tag>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("9: Unexpected character '\0' in input state [Data]",p.getErrors().get(0).toString());
		assertEquals("tag",d.body().child(0).tagName());
		assertEquals("Null\0Char",d.body().child(0).html());
	}
	
	@Test
	public void DataStateTagOpenAndEndTagOpenTest() {
		String html = "<tag><1234></tag>" +
				"<tag>2</tag>" +
				"<tag>3</1234></tag>" +
				"<tag>4</></tag>" +
				"<tag>5</";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(100);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("<1234>", d.body().child(0).text());
		assertEquals("Unexpected character '1' in input state [TagOpen]",p.getErrors().get(0).getErrorMessage());
		assertEquals("2", d.body().child(1).text());
		assertEquals("3", d.body().child(2).text());
		assertEquals("Unexpected character '1' in input state [EndTagOpen]", p.getErrors().get(1).getErrorMessage());
		assertEquals("4", d.body().child(3).text());
		assertEquals("Unexpected character '>' in input state [EndTagOpen]",p.getErrors().get(2).getErrorMessage());
		assertEquals("5</", d.body().child(4).text());
		assertEquals("Unexpectedly reached end of file (EOF) in input state [EndTagOpen]",p.getErrors().get(3).getErrorMessage());
	}
	
	@Test
	public void DataStateTagNameTest() {
		String html = "<tag\t>Hello</tag>" +
				"<tag\n>1</tag>" +
				"<tag\r>2</tag>" +
				"<tag\f>3</tag>" +
				"<tag >4</tag>" +
				"<Null\0>Hello</Null\0>";
		Document d = Jsoup.parse(html);
		
		assertEquals("Hello", d.body().child(0).text());
		assertEquals("1", d.body().child(1).text());
		assertEquals("2", d.body().child(2).text());
		assertEquals("3", d.body().child(3).text());
		assertEquals("4", d.body().child(4).text());
		assertEquals("null\uFFFD", d.body().child(5).tagName());
	}
	
	//RcData tests
	
	@Test
	public void RcDataStateReferenceCharTest() {
		String html = "<title><tag>Greaterthan&#62</tag></title>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html, "",p);
		
		assertEquals("Invalid character reference: missing semicolon",p.getErrors().get(0).getErrorMessage());
		assertEquals("<tag>Greaterthan></tag>",d.title());
	}
	
	@Test
	public void RcDataStateAmpersandTest() {
		String html = "<title><tag>Greaterthan&</tag></title>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<tag>Greaterthan&</tag>",d.title());
	}
	
	@Test
	public void RcDataStateLessThanSignEarlyExitTest() {
		String html = "<title><tag>Greaterthan&</tag>";
		String noletterandcorrectendtag = "<title><9title><tag>Greater&than</tag></title>";
		Document d = Jsoup.parse(html);
		Document a = Jsoup.parse(noletterandcorrectendtag);
		
		assertEquals("",d.title());
		assertEquals("<9title><tag>Greater&than</tag>", a.title());
	}
	
	@Test
	public void RcDataStateEndTagTest() {
		String html = "<title><tag>Greaterthan</tag></title>" +
				"<title>Greaterthan</title\t>" +
				"<title>Greaterthan</title\n>" +
				"<title>Greaterthan</title\r>" +
				"<title>Greaterthan</title\f>" +
				"<title>Greaterthan</title >" +
				"<title>Greaterthan</wrong ></title>" +
				"<title>Greaterthan</title/attribute>" +
				"<title>Greaterthan</wrong/></title>" +
				"<title>Greaterthan</a9></title>";

		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character 't' in input state [SelfClosingStartTag]",p.getErrors().get(0).getErrorMessage());
		assertEquals("<tag>Greaterthan</tag>",d.head().child(0).text());
		assertEquals("Greaterthan",d.head().child(1).text());
		assertEquals("Greaterthan",d.head().child(2).text());
		assertEquals("Greaterthan",d.head().child(3).text());
		assertEquals("Greaterthan",d.head().child(4).text());
		assertEquals("Greaterthan",d.head().child(5).text());
		assertEquals("Greaterthan</wrong >",d.head().child(6).text());
		assertEquals("Greaterthan",d.head().child(7).text());
		assertEquals("Greaterthan</wrong/>",d.head().child(8).text());
		assertEquals("Greaterthan</a9>",d.head().child(9).text());
	}
	
	@Test
	public void RcDataStateNullCharTest() {
		String html = "<title>\0NullChar</title>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		//Jsoup replaces null char with appropriate
		assertEquals("\uFFFDNullChar",d.title());
		assertEquals("Unexpected character ' ' in input state [Rcdata]",p.getErrors().get(0).getErrorMessage());
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
	public void RawTextAttributeTest() {
		String html = "<iframe><p>RawText does not parse stuff inside</p></iframe =>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe\t>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe\n>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe\r>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe\f>" +
				"<iframe></iframe/attribute>" + 
				"<iframe></1234></iframe>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe9>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character '>' in input state [BeforeAttributeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Attributes incorrectly present on end tag",p.getErrors().get(1).getErrorMessage());
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(0).text());
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(1).text());
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(2).text());
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(3).text());
		assertEquals("<p>RawText does not parse stuff inside</p>", d.body().child(4).text());
		assertEquals("", d.body().child(5).text());
		assertEquals("</1234>", d.body().child(6).text());
		assertEquals("<p>RawText does not parse stuff inside</p></iframe9>" +
				"<iframe><p>RawText does not parse stuff inside</p></iframe", d.body().child(7).text());
	}
	
	@Test
	public void RawTextNullCharTest() {
		String html = "<iframe>NullChar\0</iframe>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html, "", p);
		//Jsoup replaces null character with replacement char
		assertEquals("NullChar\uFFFD", d.body().child(0).text());
		assertEquals("Unexpected character ' ' in input state [Rawtext]", p.getErrors().get(0).getErrorMessage());
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
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		//Jsoup replaces null character with replacement char
		assertEquals("NullChar\uFFFD", d.head().child(0).data());
		assertEquals("Unexpected character ' ' in input state [ScriptData]",p.getErrors().get(0).getErrorMessage());
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
		String html = "<script>Improper syntax for self closing tag</script/attribute>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);

		assertEquals("Unexpected character 't' in input state [SelfClosingStartTag]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Improper syntax for self closing tag", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEndTagWithAttributeTest() {
		String html = "<script>Hello</script attr=\"1\"/>" +
				"<script>Hello</script\tattr=\"1\"/>" +
				"<script>Hello</script\nattr=\"1\"/>" +
				"<script>Hello</script\rattr=\"1\"/>" +
				"<script>Hello</script\fattr=\"1\"/>";
		Document d = Jsoup.parse(html);

		assertEquals("",d.head().child(0).attr("attr"));
		assertEquals("Hello", d.head().child(0).data());
		
		assertEquals("",d.head().child(1).attr("attr"));
		assertEquals("Hello", d.head().child(1).data());
		
		assertEquals("",d.head().child(2).attr("attr"));
		assertEquals("Hello", d.head().child(2).data());
		
		assertEquals("",d.head().child(3).attr("attr"));
		assertEquals("Hello", d.head().child(3).data());
		
		assertEquals("",d.head().child(4).attr("attr"));
		assertEquals("Hello", d.head().child(4).data());
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
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);

		assertEquals("Unexpectedly reached end of file (EOF) in input state [ScriptDataEscapedDashDash]",p.getErrors().get(0).getErrorMessage());
		assertEquals("<!--", d.head().child(0).data());
	}
	
	@Test
	public void ScriptEscapeTagTwoDashDataEndingAbruptlyTest() {
		String html = "<script><!--Ending...";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);

		assertEquals("Unexpectedly reached end of file (EOF) in input state [ScriptDataEscaped]",p.getErrors().get(0).getErrorMessage());
		assertEquals("<!--Ending...", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNormalTest() {
		String html = "<script><!--<escapetag>Escaping a second time</escapetag><a9>Nine</a9></script>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script\t>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script\n>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script\r>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script\n>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script >" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script/>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script9>" +
				"<script><!--<escapetag>Escaping a second time</escapetag></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<escapetag>Escaping a second time</escapetag><a9>Nine</a9>", d.head().child(0).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(1).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(2).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(3).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(4).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(5).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag>", d.head().child(6).data());
		assertEquals("<!--<escapetag>Escaping a second time</escapetag></script9><script>" +
				"<!--<escapetag>Escaping a second time</escapetag></script>", d.head().child(7).data());
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
		String tab = "<script><!--<script\t>Data in second script</script\t>--></script>";
		String newline = "<script><!--<script\n>Data in second script</script\n>--></script>";
		String ret = "<script><!--<script\r>Data in second script</script\r>--></script>";
		String space = "<script><!--<script\f>Data in second script</script >--></script>";
		String slash = "<script><!--<script >Data in second script</script/>--></script>";
		String elsecase = "<script><!--<script/>Data in second script</script1>--></script>";
		String formfeed = "<script><!--<script>Data in second script</script\f>--></script>";
		
		Document a = Jsoup.parse(html);
		Document b = Jsoup.parse(tab);
		Document c = Jsoup.parse(newline);
		Document d = Jsoup.parse(ret);
		Document e = Jsoup.parse(space);
		Document f = Jsoup.parse(slash);
		Document g = Jsoup.parse(elsecase);
		Document h = Jsoup.parse(formfeed);
		
		assertEquals("<!--<script>Data in second script</script>-->", a.head().child(0).data());
		assertEquals("<!--<script\t>Data in second script</script\t>-->", b.head().child(0).data());
		assertEquals("<!--<script\n>Data in second script</script\n>-->", c.head().child(0).data());
		assertEquals("<!--<script\r>Data in second script</script\r>-->", d.head().child(0).data());
		assertEquals("<!--<script\f>Data in second script</script >-->", e.head().child(0).data());
		assertEquals("<!--<script >Data in second script</script/>-->", f.head().child(0).data());
		assertEquals("<!--<script/>Data in second script</script1>-->", g.head().child(0).data());
		assertEquals("<!--<script>Data in second script</script\f>-->", h.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptTest() {
		String html = "<script><!--<script>Data in second script--></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptLotsofDashesAndNullTest() {
		String html = "<script><!--<script>Data in second script</script>-----></script>"+
		"<script><!--\0<script>Data in second script</script>-\0\0--\0---></script>";
		
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character '<' in input state [ScriptDataEscapedDashDash]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [ScriptDataEscapedDash]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [ScriptDataEscaped]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [ScriptDataEscapedDashDash]",p.getErrors().get(3).getErrorMessage());
		assertEquals("<!--<script>Data in second script</script>----->", d.head().child(0).data());
		assertEquals("<!--\uFFFD<script>Data in second script</script>-\uFFFD\uFFFD--\uFFFD--->", d.head().child(1).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptLotsofDashesAndNullTest() {
		String html = "<script><!--<script>Data in second script\0-\0\0---\0---></script></script>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character ' ' in input state [ScriptDataDoubleEscaped]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [ScriptDataDoubleEscaped]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [ScriptDataDoubleEscaped]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [ScriptDataDoubleEscapedDashDash]",p.getErrors().get(3).getErrorMessage());
		assertEquals("<!--<script>Data in second script\uFFFD-\uFFFD\uFFFD---\uFFFD--->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptDashFakeOutTest() {
		String html = "<script><!--<script>Data in second script</script>--FakeOut!----></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script</script>--FakeOut!---->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeInsideNestedScriptDashFakeOutTest() {
		String html = "<script><!--<script>Data in second script--FakeOut!--></script></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script--FakeOut!-->", d.head().child(0).data());
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
		
		assertEquals("<!--<script>Data in second script-<tag>tag</tag>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptWithOneDashThenTagTest() {
		String html = "<script><!--<script>Data in second script-<tag>tag</tag></script>--></script>";
		Document d = Jsoup.parse(html);
		
		assertEquals("<!--<script>Data in second script-<tag>tag</tag></script>-->", d.head().child(0).data());
	}
	
	@Test
	public void ScriptDoubleEscapeNestedScriptWithOneDashThenDataTest() {
		String html = "<script><!-\0-A\0--<script>Data in second script-Data</script>-->\0</script>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character ' ' in input state [ScriptData]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [ScriptData]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [Data]",p.getErrors().get(2).getErrorMessage());
		assertEquals("<!-\uFFFD-A\uFFFD--<script>Data in second script-Data", d.head().child(0).data());
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
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpectedly reached end of file (EOF) in input state [ScriptDataEscapedDash]",p.getErrors().get(0).getErrorMessage());
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
	public void ScriptDoubleEscapeNormalTagAndNullTest() {
		String html = "<script><!--Null\0<script><tag>Hello</tag></script>--></script>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character ' ' in input state [ScriptDataEscaped]",p.getErrors().get(0).getErrorMessage());
		assertEquals("<!--Null\uFFFD<script><tag>Hello</tag></script>-->", d.head().child(0).data());
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
		
		assertEquals("<script>Second Script", d.head().child(0).data());
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
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		Element e = d.body().child(0);
		assertEquals("Unexpected character '=' in input state [BeforeAttributeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '<' in input state [BeforeAttributeName]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character '<' in input state [AttributeName]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character '\"' in input state [AttributeName]",p.getErrors().get(3).getErrorMessage());
		assertEquals("Unexpected character '\\' in input state [AttributeName]",p.getErrors().get(4).getErrorMessage());
		assertEquals("Unexpected character '=' in input state [BeforeAttributeName]",p.getErrors().get(5).getErrorMessage());
		assertEquals("Unexpected character '=' in input state [BeforeAttributeName]",p.getErrors().get(6).getErrorMessage());
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
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		Element e = d.body().child(0);
		assertEquals("Unexpected character '=' in input state [BeforeAttributeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '<' in input state [BeforeAttributeName]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character '<' in input state [AttributeName]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character '\"' in input state [AttributeName]",p.getErrors().get(3).getErrorMessage());
		assertEquals("Unexpected character '\\' in input state [AttributeName]",p.getErrors().get(4).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [BeforeAttributeName]",p.getErrors().get(5).getErrorMessage());
		assertEquals("Unexpected character '=' in input state [BeforeAttributeName]",p.getErrors().get(6).getErrorMessage());
		assertEquals("Unexpected character '=' in input state [AfterAttributeName]",p.getErrors().get(7).getErrorMessage());
		assertEquals("Unexpected character '2' in input state [AfterAttributeName]",p.getErrors().get(8).getErrorMessage());
		assertEquals("Unexpected character '2' in input state [AfterAttributeName]",p.getErrors().get(9).getErrorMessage());
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
				"i='&#62' j='&gt' k='\0' l=&gt m=&\"\\<=` o='&' n=>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		Element e = d.body().child(0);
		assertEquals("Invalid character reference: missing semicolon",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '\"' in input state [AttributeValue_doubleQuoted]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [BeforeAttributeValue]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [AttributeValue_unquoted]",p.getErrors().get(3).getErrorMessage());
		assertEquals("Unexpected character '=' in input state [BeforeAttributeValue]",p.getErrors().get(4).getErrorMessage());
		assertEquals("Unexpected character '`' in input state [AttributeValue_unquoted]",p.getErrors().get(5).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [AttributeValue_unquoted]",p.getErrors().get(6).getErrorMessage());
		assertEquals("Invalid character reference: missing semicolon",p.getErrors().get(7).getErrorMessage());
		assertEquals("Invalid character reference: missing semicolon",p.getErrors().get(8).getErrorMessage());
		assertEquals("Unexpected character ''' in input state [AttributeValue_singleQuoted]",p.getErrors().get(9).getErrorMessage());
	
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
		assertEquals("&", e.attr("o"));
		assertEquals("", e.attr("n"));
	}
	
	@Test
	public void SelfClosingTagStartTest(){
		String html = "<tag /fakeoutselfclosingtag=hello />";
		
		Document d = Jsoup.parse(html);
		assertEquals("", d.body().child(0).attr("/fakeoutselfclosingtag"));	
	}
	
	//Markup Tests
	//Comment tests
	
	@Test
	public void CommentCommentStartDashTest(){
		String html = "<!----><!----I'm a comment--><!----\0Null-->";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character '<' in input state [CommentStartDash]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [CommentStartDash]",p.getErrors().get(1).getErrorMessage());
		assertEquals("", ((Comment) d.childNode(0)).getData());
		assertEquals("I'm a comment", ((Comment) d.childNode(1)).getData());
		assertEquals("\uFFFDNull", ((Comment) d.childNode(2)).getData());
	}
	
	@Test
	public void CommentCommentTest(){
		String html = "<!--><!--\0NullComment\0asdf-asdf-\0--\0--asdf-----><!--CommentBang--!--!asdf--!\0N--!>";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document d = Jsoup.parse(html,"",p);
		
		assertEquals("Unexpected character '<' in input state [CommentStart]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [CommentStart]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [Comment]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [CommentEndDash]",p.getErrors().get(3).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [CommentEnd]",p.getErrors().get(4).getErrorMessage());
		assertEquals("Unexpected character 's' in input state [CommentEnd]",p.getErrors().get(5).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [CommentEnd]",p.getErrors().get(6).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [CommentEnd]",p.getErrors().get(7).getErrorMessage());
		assertEquals("Unexpected character '>' in input state [CommentEnd]",p.getErrors().get(8).getErrorMessage());
		assertEquals("Unexpected character '-' in input state [CommentEnd]",p.getErrors().get(9).getErrorMessage());
		
		assertEquals("", ((Comment) d.childNode(0)).getData());
		assertEquals("\uFFFDNullComment\uFFFDasdf-asdf-\uFFFD--\uFFFD--asdf---", ((Comment) d.childNode(1)).getData());
		assertEquals("CommentBang--!--!asdf--!\uFFFDN", ((Comment) d.childNode(2)).getData());
	}
	
	//Doctype tests
	
	@Test
	public void DoctypeWithBothPublicAndSystemKeywordsDoubleQuoted(){
		String html = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\"\0Null\"\t\t\n\r\f \"\0Null\"\t\n\f\r >";
		String systemsingle = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\"\0Null\"\t\t\n\r\f 'single'>";
		String bogus = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\"\0Null\"\t\t\n\r\f bogus >";
		String closing = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\"\0Null\"\t\t\n\r\f >";
		
		Parser p = Parser.htmlParser();
		p.setTrackErrors(100);
		Document a = Jsoup.parse(html,"",p);
		DocumentType t = (DocumentType) a.childNode(0);
		
		Document b = Jsoup.parse(systemsingle);
		DocumentType t2 = (DocumentType) b.childNode(0);
		
		Document c = Jsoup.parse(bogus);
		DocumentType t3 = (DocumentType) c.childNode(0);
		
		Document d = Jsoup.parse(closing);
		DocumentType t4 = (DocumentType) d.childNode(0);
		
		assertEquals("Unexpected character '9' in input state [DoctypeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '\u0000' in input state [AfterDoctypePublicKeyword]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [DoctypePublicIdentifier_doubleQuoted]",p.getErrors().get(2).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [BetweenDoctypePublicAndSystemIdentifiers]",p.getErrors().get(3).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [DoctypeSystemIdentifier_doubleQuoted]",p.getErrors().get(4).getErrorMessage());
		assertEquals("html\uFFFD9", t.attr("name"));
		assertEquals("\uFFFDNull", t.attr("publicId"));
		assertEquals("\uFFFDNull", t.attr("systemId"));
		
		assertEquals("html\uFFFD9", t2.attr("name"));
		assertEquals("\uFFFDNull", t2.attr("publicId"));
		assertEquals("single", t2.attr("systemId"));
		
		assertEquals("html\uFFFD9", t3.attr("name"));
		assertEquals("\uFFFDNull", t3.attr("publicId"));
		assertEquals("", t3.attr("systemId"));
		
		assertEquals("html\uFFFD9", t4.attr("name"));
		assertEquals("\uFFFDNull", t4.attr("publicId"));
		assertEquals("", t4.attr("systemId"));
	}
	
	@Test
	public void DoctypeWithSystemDoubleQuoted(){
		String html = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\"\0Null\"\t\t\n\r\f >";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(100);
		Document d = Jsoup.parse(html,"",p);
		DocumentType t = (DocumentType) d.childNode(0);
		
		assertEquals("Unexpected character '9' in input state [DoctypeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character ' ' in input state [AfterDoctypeSystemKeyword]",p.getErrors().get(1).getErrorMessage());
		assertEquals("Unexpected character 'N' in input state [DoctypeSystemIdentifier_doubleQuoted]",p.getErrors().get(2).getErrorMessage());
		
		assertEquals("html\uFFFD9", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("\uFFFDNull", t.attr("systemId"));
	}
	
	@Test
	public void DoctypeWithSystemClosingAbruptly(){
		String html = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM>";
		
		Document d = Jsoup.parse(html);
		DocumentType t = (DocumentType) d.childNode(0);
		
		assertEquals("html\uFFFD9", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
	}
	
	@Test
	public void DoctypeWithSystemKeyword(){
		String html = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t\t\n\r\f \"double\">";
		String single = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t 'single'>";
		String ret = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\r>";
		String formfeed = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\f>";
		String newline = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\n>";
		String bogus = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM bogus>";
		String doubleendquick = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t\t\n\r\f \">";
		String singlenull = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t\t\n\r\f '\0Null'>";
		String singleendquick = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t\t\n\r\f '>";
		String nowhitesinglequote = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM'single'>";
		String elsecase = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM1234>";
		String weirdstuffafter = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f SYSTEM\t\t\n\r\f \"weirdstuffcoming\"Weirdstuff>";
		
		Document a = Jsoup.parse(html);
		DocumentType t = (DocumentType) a.childNode(0);
		
		Document b = Jsoup.parse(single);
		DocumentType t2 = (DocumentType) b.childNode(0);
		
		Document c = Jsoup.parse(ret);
		DocumentType t3 = (DocumentType) c.childNode(0);
		
		Document d = Jsoup.parse(formfeed);
		DocumentType t4 = (DocumentType) d.childNode(0);
		
		Document e = Jsoup.parse(newline);
		DocumentType t5 = (DocumentType) e.childNode(0);
		
		Document f = Jsoup.parse(bogus);
		DocumentType t6 = (DocumentType) f.childNode(0);
		
		Document g = Jsoup.parse(doubleendquick);
		DocumentType t7 = (DocumentType) g.childNode(0);
		
		Document h = Jsoup.parse(singlenull);
		DocumentType t8 = (DocumentType) h.childNode(0);
		
		Document i = Jsoup.parse(singleendquick);
		DocumentType t9 = (DocumentType) i.childNode(0);
		
		Document j = Jsoup.parse(nowhitesinglequote);
		DocumentType t10 = (DocumentType) j.childNode(0);
		
		Document k = Jsoup.parse(elsecase);
		DocumentType t11 = (DocumentType) k.childNode(0);
		
		Document l = Jsoup.parse(weirdstuffafter);
		DocumentType t12 = (DocumentType) l.childNode(0);
		
		assertEquals("html\uFFFD9", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("double", t.attr("systemId"));
		
		assertEquals("html\uFFFD9", t2.attr("name"));
		assertEquals("", t2.attr("publicId"));
		assertEquals("single", t2.attr("systemId"));
		
		assertEquals("html\uFFFD9", t3.attr("name"));
		assertEquals("", t3.attr("publicId"));
		assertEquals("", t3.attr("systemId"));
		
		assertEquals("html\uFFFD9", t4.attr("name"));
		assertEquals("", t4.attr("publicId"));
		assertEquals("", t4.attr("systemId"));
		
		assertEquals("html\uFFFD9", t5.attr("name"));
		assertEquals("", t5.attr("publicId"));
		assertEquals("", t5.attr("systemId"));
		
		assertEquals("html\uFFFD9", t6.attr("name"));
		assertEquals("", t6.attr("publicId"));
		assertEquals("", t6.attr("systemId"));
		
		assertEquals("html\uFFFD9", t7.attr("name"));
		assertEquals("", t7.attr("publicId"));
		assertEquals("", t7.attr("systemId"));
		
		assertEquals("html\uFFFD9", t8.attr("name"));
		assertEquals("", t8.attr("publicId"));
		assertEquals("\uFFFDNull", t8.attr("systemId"));
		
		assertEquals("html\uFFFD9", t9.attr("name"));
		assertEquals("", t9.attr("publicId"));
		assertEquals("", t9.attr("systemId"));
		
		assertEquals("html\uFFFD9", t10.attr("name"));
		assertEquals("", t10.attr("publicId"));
		assertEquals("single", t10.attr("systemId"));
		
		assertEquals("html\uFFFD9", t11.attr("name"));
		assertEquals("", t11.attr("publicId"));
		assertEquals("", t11.attr("systemId"));
		
		assertEquals("html\uFFFD9", t12.attr("name"));
		assertEquals("", t12.attr("publicId"));
		assertEquals("weirdstuffcoming", t12.attr("systemId"));
	}
	
	@Test
	public void DoctypeWithPublicKeyword(){
		String html = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f 	PUBLIC\t\t\n\r\f \"double\">";
		String single = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t 'single'>";
		String ret = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\r>";
		String formfeed = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\f>";
		String newline = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\n>";
		String bogus = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC bogus>";
		String doubleendquick = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f \">";
		String singlenull = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f '\0Null'>";
		String singleendquick = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f '>";
		String nowhitesinglequote = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC'>";
		String elsecase = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC1234>";
		String weirdstuffafter = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f \"weirdstuffcoming\"Weirdstuff>";
		String systemidentifierdouble = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f \"public\"\"system\">";
		String systemidentifiersingle = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC\t\t\n\r\f \"public\"'system'>";
		String closeafterkeyword = "<!DOCTYPE\t\t\r\n\f html\09\t\r\n\f PUBLIC>";
		
		
		Document a = Jsoup.parse(html);
		DocumentType t = (DocumentType) a.childNode(0);
		
		Document b = Jsoup.parse(single);
		DocumentType t2 = (DocumentType) b.childNode(0);
		
		Document c = Jsoup.parse(ret);
		DocumentType t3 = (DocumentType) c.childNode(0);
		
		Document d = Jsoup.parse(formfeed);
		DocumentType t4 = (DocumentType) d.childNode(0);
		
		Document e = Jsoup.parse(newline);
		DocumentType t5 = (DocumentType) e.childNode(0);
		
		Document f = Jsoup.parse(bogus);
		DocumentType t6 = (DocumentType) f.childNode(0);
		
		Document g = Jsoup.parse(doubleendquick);
		DocumentType t7 = (DocumentType) g.childNode(0);
		
		Document h = Jsoup.parse(singlenull);
		DocumentType t8 = (DocumentType) h.childNode(0);
		
		Document i = Jsoup.parse(singleendquick);
		DocumentType t9 = (DocumentType) i.childNode(0);
		
		Parser p = Parser.htmlParser();
		p.setTrackErrors(100);
		Document j = Jsoup.parse(nowhitesinglequote,"",p);
		DocumentType t10 = (DocumentType) j.childNode(0);
		
		Parser p3 = Parser.htmlParser();
		p3.setTrackErrors(100);
		Document k = Jsoup.parse(elsecase,"",p3);
		DocumentType t11 = (DocumentType) k.childNode(0);
		
		Document l = Jsoup.parse(weirdstuffafter);
		DocumentType t12 = (DocumentType) l.childNode(0);
		
		Document m = Jsoup.parse(systemidentifierdouble);
		DocumentType t13 = (DocumentType) m.childNode(0);
		
		Document n = Jsoup.parse(systemidentifiersingle);
		DocumentType t14 = (DocumentType) n.childNode(0);
		
		Parser p2 = Parser.htmlParser();
		p2.setTrackErrors(100);
		Document o = Jsoup.parse(closeafterkeyword,"",p2);
		DocumentType t15 = (DocumentType) o.childNode(0);
		
		assertEquals("html\uFFFD9", t.attr("name"));
		assertEquals("double", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
		
		assertEquals("html\uFFFD9", t2.attr("name"));
		assertEquals("single", t2.attr("publicId"));
		assertEquals("", t2.attr("systemId"));
		
		assertEquals("html\uFFFD9", t3.attr("name"));
		assertEquals("", t3.attr("publicId"));
		assertEquals("", t3.attr("systemId"));
		
		assertEquals("html\uFFFD9", t4.attr("name"));
		assertEquals("", t4.attr("publicId"));
		assertEquals("", t4.attr("systemId"));
		
		assertEquals("html\uFFFD9", t5.attr("name"));
		assertEquals("", t5.attr("publicId"));
		assertEquals("", t5.attr("systemId"));
		
		assertEquals("html\uFFFD9", t6.attr("name"));
		assertEquals("", t6.attr("publicId"));
		assertEquals("", t6.attr("systemId"));
		
		assertEquals("html\uFFFD9", t7.attr("name"));
		assertEquals("", t7.attr("publicId"));
		assertEquals("", t7.attr("systemId"));
		
		assertEquals("html\uFFFD9", t8.attr("name"));
		assertEquals("\uFFFDNull", t8.attr("publicId"));
		assertEquals("", t8.attr("systemId"));
		
		assertEquals("html\uFFFD9", t9.attr("name"));
		assertEquals("", t9.attr("publicId"));
		assertEquals("", t9.attr("systemId"));
		
		assertEquals("Unexpected character '9' in input state [DoctypeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '>' in input state [AfterDoctypePublicKeyword]",p.getErrors().get(1).getErrorMessage());
		assertEquals("html\uFFFD9", t10.attr("name"));
		assertEquals("", t10.attr("publicId"));
		assertEquals("", t10.attr("systemId"));
		
		assertEquals("Unexpected character '9' in input state [DoctypeName]",p3.getErrors().get(0).getErrorMessage());
		assertEquals("Unexpected character '2' in input state [AfterDoctypePublicKeyword]",p3.getErrors().get(1).getErrorMessage());
		assertEquals("html\uFFFD9", t11.attr("name"));
		assertEquals("", t11.attr("publicId"));
		assertEquals("", t11.attr("systemId"));
		
		assertEquals("html\uFFFD9", t12.attr("name"));
		assertEquals("weirdstuffcoming", t12.attr("publicId"));
		assertEquals("", t12.attr("systemId"));
		
		assertEquals("html\uFFFD9", t13.attr("name"));
		assertEquals("public", t13.attr("publicId"));
		assertEquals("system", t13.attr("systemId"));
		
		assertEquals("html\uFFFD9", t14.attr("name"));
		assertEquals("public", t14.attr("publicId"));
		assertEquals("system", t14.attr("systemId"));
		
		assertEquals("Unexpected character '9' in input state [DoctypeName]",p2.getErrors().get(0).getErrorMessage());
		assertEquals("", t15.attr("publicId"));
		assertEquals("", t15.attr("systemId"));
	}
	
	@Test
	//(timeout=10)
	public void DoctypeClosedAbruptly(){
		String html = "<!DOCTYPE html >";
		
		Document d = Jsoup.parse(html);
		DocumentType t = (DocumentType) d.childNode(0);
		
		assertEquals("html", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
	}
	
	@Test
	public void DoctypeEndsAbruptly(){
		String html = "<!DOCTYPE html ";
		Parser p = Parser.htmlParser();
		p.setTrackErrors(100);
		Document d = Jsoup.parse(html,"",p);
		DocumentType t = (DocumentType) d.childNode(0);
		
		assertEquals("Unexpectedly reached end of file (EOF) in input state [AfterDoctypeName]",p.getErrors().get(0).getErrorMessage());
		assertEquals("html", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
	}
	
	//@Test
	public void DoctypeNullBeforeName(){
		String html = "<!DOCTYPE \0null>";
		
		Document d = Jsoup.parse(html);
		DocumentType t = (DocumentType)d.childNode(0);
		
		assertEquals("\uFFFDNull", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
	}
	
	@Test
	public void DoctypeNumbersBeforeName(){
		String html = "<!DOCTYPE 1234Numbers>";
		String bogus = "<!DOCTYPEbogus>";
		String bogusaftername = "<!DOCTYPE name bogus>";
		
		Document d = Jsoup.parse(html);
		DocumentType t = (DocumentType) d.childNode(0);
		
		Parser p = Parser.htmlParser();
		p.setTrackErrors(10);
		Document a = Jsoup.parse(bogus,"",p);
		DocumentType t2 = (DocumentType) a.childNode(0);
		
		Parser p2 = Parser.htmlParser();
		p2.setTrackErrors(100);
		Document b = Jsoup.parse(bogusaftername,"",p2);
		DocumentType t3 = (DocumentType) b.childNode(0);
		
		
		assertEquals("1234numbers", t.attr("name"));
		assertEquals("", t.attr("publicId"));
		assertEquals("", t.attr("systemId"));
		
		assertEquals("Unexpected character 'o' in input state [Doctype]",p.getErrors().get(0).getErrorMessage());
		assertEquals("ogus", t2.attr("name"));
		assertEquals("", t2.attr("publicId"));
		assertEquals("", t2.attr("systemId"));
		
		assertEquals("Unexpected character 'b' in input state [AfterDoctypeName]",p2.getErrors().get(0).getErrorMessage());
		assertEquals("name", t3.attr("name"));
		assertEquals("", t3.attr("publicId"));
		assertEquals("", t3.attr("systemId"));
	}
	
	@Test
	public void CdataStateTest(){
		String html = "<![CDATA[cdatasection, just consumes the characters and puts in text]]>";
		
		Document d = Jsoup.parse(html);
		
		assertEquals("cdatasection, just consumes the characters and puts in text", d.child(0).text());
	}
	
}
