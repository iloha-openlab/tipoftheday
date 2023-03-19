package tokyo.northside.swing.tips;

import tokyo.northside.swing.JXTipOfTheDay;

/**
 * A tip.<br>
 */
public interface Tip {

    /**
     * @return very short (optional) text describing the tip
     */
    String getTipName();

    /**
     * The tip object to show. See {@link JXTipOfTheDay} for supported object
     * types.
     *
     * @return the tip to display
     */
    Object getTip();
}
