package tokyo.northside.tipoftheday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.util.Objects;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.fit.cssbox.awt.BrowserCanvas;
import org.junit.Assert;
import org.junit.Test;

public class IntegrationTest extends AssertJSwingJUnitTestCase {
    protected FrameFixture window;

    private static final String TITLE = "Tip of the Day";
    private static final String NAME = "tip_of_the_day";
    private static final String NEXT_BUTTON = "NextButton";
    private static final String PREVIOUS_BUTTON = "PreviousButton";

    @Override
    protected void onSetUp() {
        window = new FrameFixture(robot(), Objects.requireNonNull(GuiActionRunner.execute(TestApplication::new)));
        window.show();
    }

    @Test
    public void testShowTips() {
        window.requireTitle(TITLE);
        assertTrue(window.panel(NAME).isEnabled());
        window.panel(NAME).textBox(TestApplication.TIP1_NAME).requireText(TestApplication.TIP1);
        window.button(NEXT_BUTTON).click();
        String actual = normalizer(
                window.panel(NAME).textBox(TestApplication.TIP2_NAME).target().getText());
        String expect = normalizer(TestApplication.TIP2);
        Assert.assertEquals(expect, actual);
        window.button(PREVIOUS_BUTTON).click();
        Component component =
                window.panel(NAME).scrollPane().target().getViewport().getView();
        assertEquals(TestApplication.TIP1, ((JTextArea) component).getText());
        window.button(PREVIOUS_BUTTON).click();
        component = window.panel(NAME).scrollPane().target().getViewport().getView();
        Assert.assertTrue(((BrowserCanvas) component).getConfig().getUseHTML());
    }

    /**
     * CSSBox is only able to break a line at an ASCII space, so a Japanese paragraph used to widen
     * the canvas far past the window and get clipped. The canvas of the Swing component is checked
     * through the CSSBox viewport rather than through the component size, because the latter is
     * hidden by the scroll pane.
     */
    @Test
    public void testJapaneseTipWraps() {
        for (int i = 0; i < 3; i++) {
            window.button(NEXT_BUTTON).click();
        }
        JViewport viewport = window.panel(NAME).scrollPane().target().getViewport();
        Component component = viewport.getView();
        assertTrue("expected a BrowserCanvas but got " + component, component instanceof BrowserCanvas);
        assertEquals(TestApplication.TIP4_NAME, component.getName());

        float canvasWidth =
                ((BrowserCanvas) component).getEngine().getViewport().getWidth();
        assertTrue(
                "canvas " + canvasWidth + " overflows the viewport " + viewport.getWidth(),
                canvasWidth <= viewport.getWidth() + 1f);
    }

    private String normalizer(String original) {
        return original.replaceAll("[\\s+]?\n+[\\s+]?", "") // remove newline chars
                .replaceAll("(>)(\\s+)", "$1") // remove white space next tags
                .replaceAll("(\\s+)(<)", "$2") // remove white space before tags
                .toLowerCase();
    }
}
