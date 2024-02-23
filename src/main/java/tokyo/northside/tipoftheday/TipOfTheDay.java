/*
 *
 * Copyright 2004 Sun Microsystems, Inc.
 *           2023 Hiroshi Miura
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
package tokyo.northside.tipoftheday;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import tokyo.northside.tipoftheday.plaf.DefaultTipOfTheDayUI;
import tokyo.northside.tipoftheday.plaf.TipOfTheDayUI;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.DefaultTipOfTheDayModel;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipLoader;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

/**
 * Provides the "Tip of The Day" pane and dialog.
 *
 * <p>
 * Tips are retrieved from the {@link TipOfTheDayModel}.
 * In the most common usage, a tip (as returned by
 * {@link Tip#getTip()}) is just a
 * <code>String</code>. However, the return type of this method is actually
 * <code>Object</code>. Its interpretation depends on its type:
 * <dl>
 * <dt>Component
 * <dd>The <code>Component</code> is displayed in the dialog.
 * <dt>Icon
 * <dd>The <code>Icon</code> is wrapped in a <code>JLabel</code> and
 * displayed in the dialog.
 * <dt>others
 * <dd>The object is converted to a <code>String</code> by calling its
 * <code>toString</code> method. The result is wrapped in a
 * <code>JEditorPane</code> or <code>JTextArea</code> and displayed.
 * </dl>
 *
 * <p>
 * <code>TipOfTheDay</code> finds its tips in its {@link TipOfTheDayModel}.
 * Such model can be programmatically built using
 * {@link DefaultTipOfTheDayModel} and
 * {@link DefaultTip} but
 * the {@link TipLoader} provides
 * a convenient method to build a model and its tips from a
 * {@link java.util.Properties} object.
 *
 * <p>
 * Example:
 * <p>
 * Let's consider a file <i>tips.properties</i> with the following content:
 * <pre>
 * <code>
 * tip.1.description=This is the first time! Plain text.
 * tip.2.description=&lt;html&gt;This is &lt;b&gt;another tip&lt;/b&gt;,
 *       it uses HTML!
 * tip.3.description=A third one
 * </code>
 * </pre>
 *
 * To load and display the tips:
 *
 * <pre>
 * <code>
 * Properties tips = new Properties();
 * tips.load(new FileInputStream("tips.properties"));
 *
 * TipOfTheDayModel model = tokyo.northside.tipoftheday.tips.swing.TipLoader.load(tips);
 * TipOfTheDay tipOfTheDay = new TipOfTheDay(model);
 *
 * tipOfTheDay.showDialog(someParentComponent);
 * </code>
 * </pre>
 *
 * <p>
 * Additionally, <code>TipOfTheDay</code> features an option enabling
 * the end-user to choose to not display the "Tip Of The Day" dialog.
 * This user choice can be stored in custom storage through
 * the {@link ShowOnStartupChoice} interface.
 *
 * @see TipLoader
 * @see TipOfTheDayModel
 * @see Tip
 * @see #showDialog(Component, ShowOnStartupChoice)
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
public class TipOfTheDay extends JPanel {

    /**
     * TipOfTheDay pluggable UI key <i>TipOfTheDayUI</i>
     */
    private static final String uiClassID = "TipOfTheDayUI";

    /**
     * Used when generating PropertyChangeEvents for the "currentTip" property
     */
    public static final String CURRENT_TIP_CHANGED_KEY = "currentTip";

    private TipOfTheDayModel model;
    private boolean showOnStartUp;
    private int currentTip = 0;

    /**
     * Constructs a new <code>TipOfTheDay</code> with an empty
     * TipOfTheDayModel
     */
    public TipOfTheDay() {
        this(new DefaultTipOfTheDayModel(new Tip[0]));
    }

    /**
     * Constructs a new <code>TipOfTheDay</code> showing tips from the given
     * TipOfTheDayModel.
     *
     * @param model model of the UI.
     */
    public TipOfTheDay(TipOfTheDayModel model) {
        this(model, true);
    }

    /**
     * Constructs a new <code>TipOfTheDay</code> showing tips from the given
     * TipOfTheDayModel.
     *
     * @param model model of the UI.
     * @param showOnStartup true if show on startup, otherwise false.
     */
    public TipOfTheDay(TipOfTheDayModel model, boolean showOnStartup) {
        this.model = model;
        this.showOnStartUp = showOnStartup;
        updateUI();
    }

    /**
     * Notification from the <code>UIManager</code> that the L&amp;F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        if (UIManager.get(uiClassID) != null) {
            setUI((TipOfTheDayUI) UIManager.getUI(this));
        } else {
            setUI(new DefaultTipOfTheDayUI(this));
        }
        setName("tip_of_the_day");
    }

    /**
     * Sets the L&amp;F object that renders this component.
     *
     * @param ui
     *          the <code>TipOfTheDayUI</code> L&amp;F object
     * @see javax.swing.UIDefaults#getUI
     *
     */
    public void setUI(TipOfTheDayUI ui) {
        super.setUI(ui);
    }

    /**
     * Gets the UI object which implements the L&amp;F for this component.
     *
     * @return the TipOfTheDayUI object that implements the TipOfTheDayUI L&amp;F
     */
    @Override
    public TipOfTheDayUI getUI() {
        return (TipOfTheDayUI) ui;
    }

    /**
     * Returns the name of the L&amp;F class that renders this component.
     *
     * @return the string {@link #uiClassID}
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Returns the model.
     * @return TipOfTheDayModel object.
     */
    public TipOfTheDayModel getModel() {
        return model;
    }

    /**
     * Set model data.
     * fire PropertyChange event with an old and a new model objects.
     * @param model TipOfTheDayModel object.
     */
    public void setModel(TipOfTheDayModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model can not be null");
        }
        TipOfTheDayModel old = this.model;
        this.model = model;
        firePropertyChange("model", old, model);
    }

    /**
     * Returns current showing tip.
     * @return index number.
     */
    public int getCurrentTip() {
        return currentTip;
    }

    /**
     * Sets the index of the tip to show.
     *
     * @param currentTip current tip index.
     * @throws IllegalArgumentException if currentTip is not within the bounds [0,
     *        getModel().getTipCount()[.
     */
    public void setCurrentTip(int currentTip) {
        if (currentTip < 0 || currentTip >= getModel().getTipCount()) {
            throw new IllegalArgumentException(
                    "Current tip must be within the bounds [0, " + getModel().getTipCount() + "]");
        }

        int oldTip = this.currentTip;
        this.currentTip = currentTip;
        firePropertyChange(CURRENT_TIP_CHANGED_KEY, oldTip, currentTip);
    }

    /**
     * Shows the next tip in the list. It cycles the tip list.
     */
    public void nextTip() {
        int count = getModel().getTipCount();
        if (count == 0) {
            return;
        }

        int nextTip = currentTip + 1;
        if (nextTip >= count) {
            nextTip = 0;
        }
        setCurrentTip(nextTip);
    }

    /**
     * Shows the previous tip in the list. It cycles the tip list.
     */
    public void previousTip() {
        int count = getModel().getTipCount();
        if (count == 0) {
            return;
        }

        int previousTip = currentTip - 1;
        if (previousTip < 0) {
            previousTip = count - 1;
        }
        setCurrentTip(previousTip);
    }

    /**
     * Pops up a "Tip of the day" dialog.
     *
     * @param parentComponent parent ui component on which pop-up is shown.
     * @exception HeadlessException
     *              if GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    @SuppressWarnings("unused")
    public void showDialog(Component parentComponent) throws HeadlessException {
        showDialog(parentComponent, null);
    }

    /**
     * Pops up a "Tip of the day" dialog. Additionally, it saves the state of the
     * "Show tips on startup" checkbox in a key named "ShowTipOnStartup" in the
     * given Preferences.
     *
     * @param parentComponent parent ui component on which tip pop-ups show.
     * @param force
     *          if true, the dialog is displayed even if the Preferences is set to
     *          hide the dialog
     * @exception HeadlessException
     *              if GraphicsEnvironment.isHeadless() returns true.
     * @throws IllegalArgumentException
     *           if showOnStartupPref is null
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @return true if the user chooses to see the tips again, false
     *         otherwise.
     */
    @SuppressWarnings("unused")
    public boolean showDialog(Component parentComponent, boolean force) throws HeadlessException {

        ShowOnStartupChoice store = new ShowOnStartupChoice() {
            @Override
            public boolean isShowingOnStartup() {
                return TipOfTheDay.this.showOnStartUp;
            }

            @Override
            public void setShowingOnStartup(boolean showOnStartup) {
                TipOfTheDay.this.showOnStartUp = showOnStartup;
            }
        };
        return showDialog(parentComponent, store, force);
    }

    /**
     * Pops up a "Tip of the day" dialog.
     * <p>
     * If <code>choice</code> is not null, the method first checks if
     * {@link ShowOnStartupChoice#isShowingOnStartup()} is true before showing the
     * dialog.
     * Additionally, it saves the state of the "Show tips on startup" checkbox
     * using the given {@link ShowOnStartupChoice} object.
     *
     * @param parentComponent parent ui component on which pop-up is shown.
     * @param choice choice to show it on startup.
     * @exception HeadlessException
     *              if GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @return true if the user chooses to see the tips again, false otherwise.
     */
    public boolean showDialog(Component parentComponent, ShowOnStartupChoice choice) {
        return showDialog(parentComponent, choice, false);
    }

    /**
     * Pops up a "Tip of the day" dialog.
     * <p>
     * If <code>choice</code> is not null, the method first checks if
     * <code>force</code> is true or if
     * {@link ShowOnStartupChoice#isShowingOnStartup()} is true before showing the
     * dialog.
     * Additionally, it saves the state of the "Show tips on startup" checkbox
     * using the given {@link ShowOnStartupChoice} object.
     *
     * @param parentComponent parent ui component on which pop-up is shown.
     * @param choice choice to show when startup.
     * @param force
     *          if true, the dialog is displayed even if
     *          {@link ShowOnStartupChoice#isShowingOnStartup()} is false
     * @exception HeadlessException
     *              if GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @return true if the user chooses to see the tips again, false otherwise.
     */
    public boolean showDialog(Component parentComponent, ShowOnStartupChoice choice, boolean force) {
        if (choice == null) {
            JDialog dialog = createDialog(parentComponent, null);
            dialog.setVisible(true);
            dialog.dispose();
            return true;
        } else if (force || choice.isShowingOnStartup()) {
            JDialog dialog = createDialog(parentComponent, choice);
            dialog.setVisible(true);
            dialog.dispose();
            return choice.isShowingOnStartup();
        } else {
            return false;
        }
    }

    /**
     * Calls
     * {@link TipOfTheDayUI#createDialog(Component, ShowOnStartupChoice)}.
     * <p>
     * This method can be overriden in order to control things such as the
     * placement of the dialog or its title.
     *
     * @param parentComponent parent ui component on which pop-up is shown.
     * @param choice choice of tip when shown startup.
     * @return a JDialog to show this TipOfTheDay pane
     */
    protected JDialog createDialog(Component parentComponent, ShowOnStartupChoice choice) {
        return getUI().createDialog(parentComponent, choice);
    }

    /**
     * Used in conjunction with the
     * {@link TipOfTheDay#showDialog(Component, ShowOnStartupChoice)} to save the
     * "Show tips on startup" choice.
     */
    public interface ShowOnStartupChoice {

        /**
         * Persists the user choice
         * @param showOnStartup the user choice
         */
        void setShowingOnStartup(boolean showOnStartup);

        /**
         * @return the previously stored user choice
         */
        boolean isShowingOnStartup();
    }
}
