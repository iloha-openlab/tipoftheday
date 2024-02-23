package tokyo.northside.tipoftheday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assert;
import org.junit.Test;

public class IntegrationTest extends AssertJSwingJUnitTestCase {
    protected FrameFixture window;

    @Override
    protected void onSetUp() {
        window = new FrameFixture(robot(), GuiActionRunner.execute(TestApplication::new));
        window.show();
    }

    @Test
    public void testShowTips() {
        window.requireTitle("Tip of the Day");
        assertTrue(window.panel("tip_of_the_day").isEnabled());
        window.panel("tip_of_the_day").textBox(TestApplication.TIP1_NAME).requireText(TestApplication.TIP1);
        window.button("NextButton").click();
        String actual = normalizer(window.panel("tip_of_the_day")
                .textBox(TestApplication.TIP2_NAME)
                .target()
                .getText());
        String expect = normalizer(TestApplication.TIP2);
        Assert.assertEquals(expect, actual);
        window.button("PreviousButton").click();
        Component component = window.panel("tip_of_the_day")
                .scrollPane()
                .target()
                .getViewport()
                .getView();
        assertEquals(TestApplication.TIP1, ((JTextArea) component).getText());
        window.button("PreviousButton").click();
        component = window.panel("tip_of_the_day")
                .scrollPane()
                .target()
                .getViewport()
                .getView();
        actual = normalizer(((JEditorPane) component).getText());
        Assert.assertEquals(expect, actual);
    }

    private String normalizer(String original) {
        return original.replaceAll("[\\s+]?\n+[\\s+]?", "") // remove newline chars
                .replaceAll("(>)(\\s+)", "$1") // remove white space next tags
                .replaceAll("(\\s+)(<)", "$2") // remove white space before tags
                .toLowerCase();
    }
}
