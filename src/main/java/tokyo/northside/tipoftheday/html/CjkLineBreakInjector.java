/*
 * Copyright 2026 Hiroshi Miura
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package tokyo.northside.tipoftheday.html;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Inserts line-break opportunities into a parsed HTML document for scripts that do not use spaces
 * between words.
 *
 * <p>The CSSBox layout engine only ever breaks a line at an ASCII space (U+0020); it implements
 * neither the Unicode line breaking algorithm (UAX #14) nor the {@code word-break} /
 * {@code overflow-wrap} / {@code line-break} CSS properties. A Japanese, Chinese or Korean
 * paragraph therefore becomes one single unbreakable "word", which both overflows the canvas and
 * inflates the minimal content width of its containing block, so the text is clipped instead of
 * wrapped.
 *
 * <p>This class works around that without patching CSSBox: it walks the text nodes of the
 * document, asks {@link BreakIterator} where the line breaks of the relevant locale are, and
 * splits each text node at those offsets, placing a zero-sized marker element
 * ({@code <span style="font-size:0;line-height:0"> </span>}) in between. The marker contains a
 * plain space, which is exactly what CSSBox already knows how to break at, and renders with no
 * width because of its inline {@code font-size:0}. An inline style attribute is used rather than a
 * style sheet rule so that the document's own style sheet cannot override it.
 *
 * <p>Documents that contain no CJK text are left structurally untouched: the check is made per
 * text node and nothing is inserted unless the node actually holds Han, Hiragana, Katakana, Hangul
 * or Bopomofo characters. Positions where CSSBox is already able to break &mdash; next to an
 * existing white space &mdash; are skipped as well, so the rendering of Latin text, and of the
 * Latin runs embedded in a CJK paragraph, is unchanged.
 *
 * <p><b>Limitation:</b> this runs before the CSS cascade has been computed, so a
 * {@code white-space} value that preserves line breaks can only be honoured when it is given
 * either by the element's own {@code style} attribute or by the element's tag name (such as
 * {@code <pre>}). A {@code white-space: pre} coming from an external style sheet is not seen here.
 *
 * <p>Instances are not thread safe; use one from a single thread, such as the Swing event dispatch
 * thread.
 */
public final class CjkLineBreakInjector {

    /**
     * Attribute marking the elements this class has inserted. It also makes the injection
     * idempotent: a document that already carries markers is left alone.
     */
    private static final String MARKER_ATTRIBUTE = "data-totd-break";

    private static final String MARKER_STYLE = "font-size:0;line-height:0";

    /** Elements whose text content must be kept verbatim or is not rendered at all. */
    private static final Set<String> SKIPPED_TAGS =
            new HashSet<>(Arrays.asList("code", "head", "kbd", "pre", "samp", "script", "style", "textarea", "title"));

    /** {@code white-space} values that suppress the automatic wrapping we are trying to enable. */
    private static final String[] PRESERVING_WHITE_SPACE = {"pre", "pre-wrap", "pre-line", "nowrap"};

    /**
     * Upper bound of markers inserted into a single text node. A pathologically long node is left
     * untouched rather than multiplied into tens of thousands of boxes.
     */
    private static final int MAX_MARKERS_PER_NODE = 4000;

    private final Locale fallbackLocale;

    private final Map<Locale, BreakIterator> iterators = new HashMap<>();

    /**
     * Creates an injector.
     *
     * @param fallbackLocale locale used to pick the line breaking rules for text that is not
     *     covered by a {@code lang} or {@code xml:lang} attribute. When {@code null}, the default
     *     locale is used.
     */
    public CjkLineBreakInjector(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale == null ? Locale.getDefault() : fallbackLocale;
    }

    /**
     * Inserts line-break opportunities into the body of the given document.
     *
     * <p>The document is modified in place. Nothing happens when it holds no CJK text, or when it
     * has already been processed by this class.
     *
     * @param doc the parsed document, or {@code null}.
     */
    public void inject(Document doc) {
        if (doc == null) {
            return;
        }
        Element body = findBody(doc);
        if (body == null) {
            return;
        }
        // Two phases: the whole tree is read before anything is replaced, so that the traversal
        // never observes the nodes we create.
        List<Text> targets = new ArrayList<>();
        if (collect(body, targets)) {
            return; // already injected
        }
        for (Text target : targets) {
            split(target);
        }
    }

    private static Element findBody(Document doc) {
        Element root = doc.getDocumentElement();
        if (root == null) {
            return null;
        }
        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && isTag((Element) child, "body")) {
                return (Element) child;
            }
        }
        return root;
    }

    /**
     * Collects the text nodes that are worth splitting or keeps as-is when already corrected.
     *
     * @return {@code true} when a marker of a previous run was found, meaning that the document
     *     must be left as is.
     */
    private boolean collect(Element element, List<Text> out) {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (containsCjk(child.getNodeValue())) {
                    out.add((Text) child);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                if (childElement.hasAttribute(MARKER_ATTRIBUTE)) {
                    return true;
                }
                if (!isSkipped(childElement) && collect(childElement, out)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSkipped(Element element) {
        String name = element.getTagName();
        if (name != null && SKIPPED_TAGS.contains(name.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return preservesWhiteSpace(element.getAttribute("style"));
    }

    private static boolean preservesWhiteSpace(String style) {
        if (style == null || style.isEmpty()) {
            return false;
        }
        String normalized = style.toLowerCase(Locale.ROOT).replace(" ", "").replace("\t", "");
        for (String value : PRESERVING_WHITE_SPACE) {
            if (normalized.contains("white-space:" + value)) {
                return true;
            }
        }
        return false;
    }

    private void split(Text node) {
        String text = node.getNodeValue();
        Node parent = node.getParentNode();
        if (text == null || parent == null) {
            return;
        }
        List<Integer> positions = breakPositions(text, localeOf(node));
        if (positions.isEmpty() || positions.size() > MAX_MARKERS_PER_NODE) {
            return;
        }
        Document doc = node.getOwnerDocument();
        DocumentFragment fragment = doc.createDocumentFragment();
        int previous = 0;
        for (int position : positions) {
            fragment.appendChild(doc.createTextNode(text.substring(previous, position)));
            fragment.appendChild(createMarker(doc));
            previous = position;
        }
        fragment.appendChild(doc.createTextNode(text.substring(previous)));
        parent.replaceChild(fragment, node);
    }

    private static Element createMarker(Document doc) {
        Element marker = doc.createElement("span");
        marker.setAttribute(MARKER_ATTRIBUTE, "");
        marker.setAttribute("style", MARKER_STYLE);
        marker.appendChild(doc.createTextNode(" "));
        return marker;
    }

    private List<Integer> breakPositions(String text, Locale locale) {
        List<Integer> positions = new ArrayList<>();
        BreakIterator iterator = iteratorFor(locale);
        iterator.setText(text);
        for (int i = iterator.first(); i != BreakIterator.DONE; i = iterator.next()) {
            if (i <= 0 || i >= text.length()) {
                continue; // a break at either end of the node is not an opportunity
            }
            if (Character.isWhitespace(text.charAt(i)) || Character.isWhitespace(text.charAt(i - 1))) {
                continue; // CSSBox can already break here, and splitting the node on a white space
                // boundary would change how the white space is collapsed
            }
            if (!cjkAdjacent(text, i)) {
                continue; // leave the Latin runs of a mixed paragraph alone
            }
            positions.add(i);
        }
        return positions;
    }

    private BreakIterator iteratorFor(Locale locale) {
        return iterators.computeIfAbsent(locale, BreakIterator::getLineInstance);
    }

    /**
     * Resolves the locale of a node from the closest {@code lang} or {@code xml:lang} attribute of
     * its ancestors, falling back to the locale given to the constructor.
     */
    private Locale localeOf(Node node) {
        for (Node current = node.getParentNode(); current != null; current = current.getParentNode()) {
            if (current.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) current;
            String tag = element.getAttribute("lang");
            if (tag.isEmpty()) {
                tag = element.getAttribute("xml:lang");
            }
            if (!tag.isEmpty()) {
                Locale locale = Locale.forLanguageTag(tag);
                if (!locale.getLanguage().isEmpty()) {
                    return locale;
                }
            }
        }
        return fallbackLocale;
    }

    private static boolean isTag(Element element, String name) {
        String tag = element.getTagName();
        return tag != null && tag.equalsIgnoreCase(name);
    }

    /** Tells whether the text holds at least one character of a script that is written unspaced. */
    private static boolean containsCjk(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if (isCjkScript(cp)) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    /** Tells whether at least one of the characters surrounding the offset is CJK. */
    private static boolean cjkAdjacent(String text, int offset) {
        int before = text.codePointBefore(offset);
        int after = text.codePointAt(offset);
        return isCjk(before) || isCjk(after);
    }

    private static boolean isCjk(int cp) {
        return isCjkScript(cp) || isCjkPunctuation(cp);
    }

    private static boolean isCjkScript(int cp) {
        Character.UnicodeScript script = Character.UnicodeScript.of(cp);
        return script == Character.UnicodeScript.HAN
                || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA
                || script == Character.UnicodeScript.HANGUL
                || script == Character.UnicodeScript.BOPOMOFO;
    }

    private static boolean isCjkPunctuation(int cp) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
        return block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || block == Character.UnicodeBlock.VERTICAL_FORMS;
    }
}
