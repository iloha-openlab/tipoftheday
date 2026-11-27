package tokyo.northside.tipoftheday.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.vutbr.web.css.MediaSpec;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.fit.cssbox.awt.GraphicsEngine;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.io.StreamDocumentSource;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Checks what the CSSBox layout actually does with the markers inserted by
 * {@link CjkLineBreakInjector}.
 */
class CjkLineBreakRenderTest {

    private static final int NARROW = 300;
    private static final int WIDE = 2400;

    private static final String JAPANESE = "<html lang=\"ja\"><head><title>t</title></head><body>"
            + "<p>[編集][用語の追加...(Ctrl-Shift-G)]を使って、用語集に新しい用語を追加します。"
            + "OmegaTは、glossary/glossary.txtに書き込みます。テキストエディタで編集もできます。</p>"
            + "</body></html>";

    private static final String LATIN = "<html lang=\"en\"><head><title>t</title></head><body>"
            + "<p>Use the Edit menu to add a new term to the glossary. OmegaT then writes it "
            + "to glossary/glossary.txt, which you may also edit with a text editor.</p>"
            + "</body></html>";

    /**
     * Records the bug this class works around: on its own, CSSBox cannot break a Japanese
     * paragraph, so the paragraph widens the canvas far past the window instead of wrapping.
     */
    @Test
    void withoutInjection_overflows() throws Exception {
        Viewport viewport = layout(JAPANESE, NARROW, false);

        assertTrue(
                viewport.getWidth() > NARROW,
                "CSSBox is expected to overflow without the injection, width was " + viewport.getWidth());
    }

    @Test
    void withInjection_fitsAndWraps() throws Exception {
        Viewport narrow = layout(JAPANESE, NARROW, true);
        Viewport wide = layout(JAPANESE, WIDE, true);

        assertTrue(narrow.getWidth() <= NARROW, "the canvas should fit the window, width was " + narrow.getWidth());
        assertTrue(
                countLines(narrow) > countLines(wide) * 2,
                "the paragraph should wrap into more lines when narrow, got " + countLines(narrow) + " against "
                        + countLines(wide));
    }

    /** The injection is a no-op for a document that holds no CJK text. */
    @Test
    void latinUnchanged() throws Exception {
        Viewport plain = layout(LATIN, NARROW, false);
        Viewport injected = layout(LATIN, NARROW, true);

        assertEquals(plain.getWidth(), injected.getWidth());
        assertEquals(plain.getHeight(), injected.getHeight());
        assertEquals(countLines(plain), countLines(injected));
    }

    private static Viewport layout(String html, int width, boolean inject) throws Exception {
        try (DocumentSource src = new StreamDocumentSource(
                new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)),
                new URL("file:///cjk-test.html"), "text/html; charset=UTF-8")) {
            Document doc = new DefaultDOMSource(src).parse();
            DOMAnalyzer da = new DOMAnalyzer(doc, src.getURL());
            da.setDefaultEncoding(StandardCharsets.UTF_8.name());
            da.setMediaSpec(new MediaSpec("screen"));
            da.attributesToStyles();
            if (inject) {
                new CjkLineBreakInjector(Locale.ENGLISH).inject(doc);
            }
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.getStyleSheets();
            GraphicsEngine engine = new GraphicsEngine(da.getRoot(), da, src.getURL());
            engine.setAutoMediaUpdate(false);
            engine.createLayout(new Dimension(width, 400f), new Rectangle(0f, 0f, width, 400f), false);
            return engine.getViewport();
        }
    }

    /**
     * Counts the rendered lines. CSSBox does not expose its line boxes, so the distinct vertical
     * positions of the text boxes are counted instead.
     */
    private static int countLines(Viewport viewport) {
        Set<Integer> baselines = new LinkedHashSet<>();
        collectBaselines(viewport, baselines);
        return baselines.size();
    }

    private static void collectBaselines(Box box, Set<Integer> baselines) {
        if (box instanceof TextBox) {
            if (!box.getText().trim().isEmpty()) {
                baselines.add(Math.round(box.getAbsoluteBounds().y));
            }
        } else if (box instanceof ElementBox) {
            ElementBox element = (ElementBox) box;
            for (int i = 0; i < element.getSubBoxNumber(); i++) {
                collectBaselines(element.getSubBox(i), baselines);
            }
        }
    }
}
