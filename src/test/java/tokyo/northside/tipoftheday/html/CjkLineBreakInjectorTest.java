package tokyo.northside.tipoftheday.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class CjkLineBreakInjectorTest {

    private static final String MARKER_ATTRIBUTE = "data-totd-break";

    @Test
    void preservesTheTextOfTheDocument() throws Exception {
        String body = "<p>[編集]を使って、用語集に新しい用語を追加します。OmegaT is fine.</p>" + "<h3>書き込み可能な用語集： glossary.txt</h3>";
        Document doc = parse("<html lang=\"ja\"><body>" + body + "</body></html>");
        String before = textContent(doc.getDocumentElement());

        inject(doc, Locale.JAPANESE);

        assertEquals(before, textContent(doc.getDocumentElement()));
    }

    @Test
    void splitsBetweenEveryJapaneseCharacter() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>書き込み可能</p></body></html>");

        inject(doc, Locale.JAPANESE);

        Node p = doc.getElementsByTagName("p").item(0);
        assertEquals(6, countTextNodes(p));
        assertEquals(5, countMarkers(p));
    }

    @Test
    void honoursJapaneseLineBreakingRules() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>使って、新しい</p></body></html>");

        inject(doc, Locale.JAPANESE);

        List<String> segments = segments(doc.getElementsByTagName("p").item(0));
        // "使っ" is not broken after the stem, and "、" stays attached to the character it follows.
        assertEquals(List.of("使っ", "て、", "新", "し", "い"), segments);
    }

    @Test
    void leavesLatinDocumentsUntouched() throws Exception {
        Document doc = parse("<html><body><p>OmegaT is fine.</p></body></html>");
        Node p = doc.getElementsByTagName("p").item(0);
        Node text = p.getFirstChild();

        inject(doc, Locale.ENGLISH);

        assertEquals(1, p.getChildNodes().getLength());
        assertSame(text, p.getFirstChild());
    }

    @Test
    void doesNotMarkPositionsNextToASpace() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>日本語 and English</p></body></html>");

        inject(doc, Locale.JAPANESE);

        // Only 日|本|語 are split; CSSBox can already break at the ASCII spaces.
        assertEquals(
                List.of("日", "本", "語 and English"),
                segments(doc.getElementsByTagName("p").item(0)));
    }

    @Test
    void skipsElementsWhoseTextMustBeKeptVerbatim() throws Exception {
        for (String tag : List.of("pre", "script", "style", "textarea", "code", "kbd", "samp")) {
            Document doc = parse("<html lang=\"ja\"><body><" + tag + ">書き込み</" + tag + "></body></html>");
            Node element = doc.getElementsByTagName(tag).item(0);
            Node text = element.getFirstChild();

            inject(doc, Locale.JAPANESE);

            assertEquals(1, element.getChildNodes().getLength(), tag + " was modified");
            assertSame(text, element.getFirstChild(), tag + " was modified");
        }
    }

    @Test
    void skipsElementsThatPreserveWhiteSpace() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body>" + "<p style=\"white-space: nowrap\">書き込み</p>"
                + "<p style=\"white-space:pre\">書き込み</p>"
                + "<p style=\"white-space: pre-wrap\">書き込み</p>"
                + "</body></html>");

        inject(doc, Locale.JAPANESE);

        assertEquals(0, countMarkers(doc.getDocumentElement()));
    }

    @Test
    void skipsTheDocumentTitle() throws Exception {
        Document doc = parse("<html lang=\"ja\"><head><title>書き込み</title></head><body><p>x</p></body></html>");

        inject(doc, Locale.JAPANESE);

        assertEquals(0, countMarkers(doc.getDocumentElement()));
    }

    @Test
    void resolvesTheLocaleFromTheClosestLangAttribute() throws Exception {
        // Korean breaks between words, Japanese between characters: the per-element lang wins over
        // the lang of the document and over the fall-back locale.
        Document doc = parse("<html lang=\"en\"><body><div lang=\"ja\"><p>書き込み</p></div></body></html>");

        inject(doc, Locale.ENGLISH);

        assertEquals(
                List.of("書", "き", "込", "み"),
                segments(doc.getElementsByTagName("p").item(0)));
    }

    @Test
    void usesTheFallbackLocaleWhenTheDocumentDeclaresNone() throws Exception {
        Document doc = parse("<html><body><p>書き込み</p></body></html>");

        inject(doc, Locale.JAPANESE);

        assertEquals(
                List.of("書", "き", "込", "み"),
                segments(doc.getElementsByTagName("p").item(0)));
    }

    @Test
    void doesNotSplitSurrogatePairs() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>あ𠀋い</p></body></html>");

        inject(doc, Locale.JAPANESE);

        assertEquals(
                List.of("あ", "𠀋", "い"), segments(doc.getElementsByTagName("p").item(0)));
    }

    @Test
    void isIdempotent() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>書き込み可能</p></body></html>");

        inject(doc, Locale.JAPANESE);
        int once = doc.getElementsByTagName("p").item(0).getChildNodes().getLength();
        inject(doc, Locale.JAPANESE);

        assertEquals(once, doc.getElementsByTagName("p").item(0).getChildNodes().getLength());
    }

    @Test
    void markersAreInvisible() throws Exception {
        Document doc = parse("<html lang=\"ja\"><body><p>書き込み</p></body></html>");

        inject(doc, Locale.JAPANESE);

        Node p = doc.getElementsByTagName("p").item(0);
        boolean found = false;
        for (Node c = p.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                Element marker = (Element) c;
                found = true;
                assertEquals("span", marker.getTagName());
                // the break opportunity CSSBox understands, given no width by an inline style
                assertEquals(" ", marker.getTextContent());
                assertTrue(marker.getAttribute("style").contains("font-size:0"));
            }
        }
        assertTrue(found, "no marker was inserted");
    }

    @Test
    void acceptsADocumentWithoutABody() throws Exception {
        Document doc = parse("<html lang=\"ja\"><p>書き込み</p></html>");

        inject(doc, Locale.JAPANESE);

        assertNotEquals(0, countMarkers(doc.getDocumentElement()));
    }

    // ---- helpers --------------------------------------------------------

    private static void inject(Document doc, Locale locale) {
        new CjkLineBreakInjector(locale).inject(doc);
    }

    private static Document parse(String html) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
    }

    /** All the text of the document with the markers left out. */
    private static String textContent(Node node) {
        StringBuilder sb = new StringBuilder();
        for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.TEXT_NODE) {
                sb.append(c.getNodeValue());
            } else if (c.getNodeType() == Node.ELEMENT_NODE && !((Element) c).hasAttribute(MARKER_ATTRIBUTE)) {
                sb.append(textContent(c));
            }
        }
        return sb.toString();
    }

    /** The text segments of a node, with the markers left out. */
    private static List<String> segments(Node node) {
        List<String> result = new ArrayList<>();
        for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.TEXT_NODE) {
                result.add(c.getNodeValue());
            }
        }
        return result;
    }

    private static int countTextNodes(Node node) {
        return segments(node).size();
    }

    private static int countMarkers(Node node) {
        int count = 0;
        for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                if (((Element) c).hasAttribute(MARKER_ATTRIBUTE)) {
                    count++;
                }
                count += countMarkers(c);
            }
        }
        return count;
    }
}
