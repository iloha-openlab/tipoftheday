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
package tokyo.northside.tipoftheday.plaf;

import cz.vutbr.web.css.MediaSpec;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.basic.BasicHTML;
import org.fit.cssbox.awt.BrowserCanvas;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserConfig;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tokyo.northside.tipoftheday.TipOfTheDay;
import tokyo.northside.tipoftheday.data.HtmlTipData;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

/**
 * Base implementation of the <code>TipOfTheDay</code> UI.
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
public abstract class BasicTipOfTheDayUI extends TipOfTheDayUI {

    private static final String PREVIOUS_TIP_ACTION = "previousTip";
    private static final String NEXT_TIP_ACTION = "nextTip";
    private static final String CENTER_POSITION = "Center";
    private static final String SOUTH_POSITION = "South";
    private static final String TEXT_AREA_BACKGROUND_COLOR_KEY = "TextArea.background";
    private static final int NO_CURRENT_TIP = -1;

    protected TipOfTheDay tipPane;
    protected JPanel tipArea;
    protected Component currentTipComponent;
    protected JScrollPane tipScrollPane;
    protected JEditorPane tipEditorPane;
    protected JTextArea tipTextArea;

    protected Font tipFont;
    protected PropertyChangeListener changeListener;

    protected BrowserConfig browserConfig;

    protected BasicTipOfTheDayUI(TipOfTheDay tipPane) {
        super();
        this.tipPane = tipPane;
        UIManager.getDefaults().addResourceBundle("tokyo.northside.tipoftheday.plaf.BasicTipOfTheDayUI");
        tipScrollPane = new JScrollPane();
        tipEditorPane = new JEditorPane("text/html", null);
        tipTextArea = new JTextArea();
        tipScrollPane.setBorder(null);
        tipScrollPane.setOpaque(false);
        tipScrollPane.getViewport().setOpaque(false);
        tipScrollPane.setBorder(null);
        tipScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tipEditorPane.setEditable(false);
        tipTextArea.setEditable(false);
        tipTextArea.setLineWrap(true);
        browserConfig = new BrowserConfig();
    }

    @Override
    public JDialog createDialog(Component parentComponent, final TipOfTheDay.ShowOnStartupChoice choice) {
        return createDialog(parentComponent, choice, true);
    }

    private Window resolveWindow(Component parentComponent) {
        if (parentComponent == null) {
            return JOptionPane.getRootFrame();
        }
        return (parentComponent instanceof Window)
                ? (Window) parentComponent
                : SwingUtilities.getWindowAncestor(parentComponent);
    }

    /**
     * Creates a new JDialog to display a TipOfTheDay panel.
     *
     * @param parentComponent parent component for dialog.
     * @param choice config of the choice.
     * @param showPreviousButton
     *            show the previous button when true, otherwise hide button.
     * @return a new JDialog to display a TipOfTheDay panel
     */
    @SuppressWarnings("SameParameterValue")
    protected JDialog createDialog(
            Component parentComponent, final TipOfTheDay.ShowOnStartupChoice choice, boolean showPreviousButton) {
        Locale locale = parentComponent == null ? null : parentComponent.getLocale();
        String title = UIManager.getString("TipOfTheDay.dialogTitle", locale);

        Window parentWindow = resolveWindow(parentComponent);

        boolean isParentFrame = parentWindow instanceof Frame;
        final JDialog tipDialog = isParentFrame
                ? new JDialog((Frame) parentWindow, title, true)
                : new JDialog((Dialog) parentWindow, title, true);

        tipDialog.getContentPane().setLayout(new BorderLayout(10, 10));
        tipDialog.getContentPane().add(tipPane, BorderLayout.CENTER);
        ((JComponent) tipDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // tip control
        JPanel controls = new JPanel(new BorderLayout());
        tipDialog.add(SOUTH_POSITION, controls);

        final JCheckBox showOnStartupBox;
        if (choice != null) {
            showOnStartupBox = new JCheckBox(
                    UIManager.getString("TipOfTheDay.showOnStartupText", locale), choice.isShowingOnStartup());
            controls.add(showOnStartupBox, BorderLayout.CENTER);
        } else {
            showOnStartupBox = null;
        }

        JPanel buttons = new JPanel(new GridLayout(1, showPreviousButton ? 3 : 2, 9, 0));
        controls.add(buttons, BorderLayout.LINE_END);

        if (showPreviousButton) {
            JButton previousTipButton = new JButton(UIManager.getString("TipOfTheDay.previousTipText", locale));
            buttons.add(previousTipButton);
            previousTipButton.addActionListener(getActionMap().get(PREVIOUS_TIP_ACTION));
        }

        JButton nextTipButton = new JButton(UIManager.getString("TipOfTheDay.nextTipText", locale));
        buttons.add(nextTipButton);
        nextTipButton.addActionListener(getActionMap().get(NEXT_TIP_ACTION));

        JButton closeButton = new JButton(UIManager.getString("TipOfTheDay.closeText", locale));
        buttons.add(closeButton);

        closeButton.addActionListener(e -> {
            if (tipDialog.isDisplayable()) {
                tipDialog.setVisible(false);
            }
        });

        tipDialog.getRootPane().setDefaultButton(closeButton);

        tipDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (choice != null) {
                    choice.setShowingOnStartup(showOnStartupBox.isSelected());
                }
            }
        });

        final ActionListener closeAction = e -> {
            if (tipDialog.isDisplayable()) {
                tipDialog.setVisible(false);
            }
        };

        ((JComponent) tipDialog.getContentPane())
                .registerKeyboardAction(
                        closeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        tipDialog.pack();
        tipDialog.setLocationRelativeTo(parentComponent);

        return tipDialog;
    }

    /**
     * Configures the specified component appropriately for the look and feel.
     * This method is invoked when the BasicTipOfTheDayUI instance is being
     * installed as the UI delegate on the specified component.
     * @param c the component where this UI delegate is being installed
     *
     */
    @Override
    public void installUI(JComponent c) {
        installDefaults();
        installKeyboardActions();
        installComponents();
        installListeners();
    }

    /**
     * Install keyboard actions defined by getActionMap method.
     */
    protected void installKeyboardActions() {
        ActionMap map = getActionMap();
        if (map != null) {
            SwingUtilities.replaceUIActionMap(tipPane, map);
        }
    }

    ActionMap getActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put(PREVIOUS_TIP_ACTION, new PreviousTipAction());
        map.put(NEXT_TIP_ACTION, new NextTipAction());
        return map;
    }

    protected void installListeners() {
        changeListener = createChangeListener();
        tipPane.addPropertyChangeListener(changeListener);
    }

    protected PropertyChangeListener createChangeListener() {
        return new ChangeListener();
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(
                tipPane, "TipOfTheDay.background", "TipOfTheDay.foreground", "TipOfTheDay.font");
        LookAndFeel.installBorder(tipPane, "TipOfTheDay.border");
        LookAndFeel.installProperty(tipPane, "opaque", Boolean.TRUE);
        tipFont = tipPane.getFont();
    }

    protected void installComponents() {
        tipEditorPane.setFont(tipFont);
        tipTextArea.setFont(tipFont);
        tipPane.setLayout(new BorderLayout());
        tipPane.setName("tip_pane");
        tipArea = new JPanel(new BorderLayout(2, 2));
        tipArea.setName("tip_area");
        tipArea.setOpaque(false);
        tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tipArea.setFont(tipFont);
        tipPane.add(CENTER_POSITION, tipArea);
    }

    protected void showCurrentTip() {
        if (currentTipComponent != null) {
            tipArea.remove(currentTipComponent);
        }

        int currentTip = tipPane.getCurrentTip();
        if (currentTip == NO_CURRENT_TIP) {
            currentTipComponent = createEmptyTipLabel();
            tipArea.add(CENTER_POSITION, currentTipComponent);
            return;
        }

        try {
            updateTipComponent(currentTip, tipPane.getModel());
        } catch (IOException | SAXException e) {
            currentTipComponent = createEmptyTipLabel();
        }

        tipArea.add(CENTER_POSITION, currentTipComponent);
        tipArea.revalidate();
        tipArea.repaint();
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
        super.uninstallUI(c);
    }

    protected void uninstallListeners() {
        tipPane.removePropertyChangeListener(changeListener);
    }

    protected void uninstallComponents() {}

    protected void uninstallDefaults() {}

    private JLabel createEmptyTipLabel() {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(UIManager.getColor(TEXT_AREA_BACKGROUND_COLOR_KEY));
        return label;
    }

    private void updateTipComponent(int currentTip, TipOfTheDayModel model) throws IOException, SAXException {
        if (isInvalidTip(currentTip, model)) {
            currentTipComponent = new JLabel();
            return;
        }

        Tip tip = model.getTipAt(currentTip);
        Object tipObject = tip.getTip();

        if (tipObject instanceof Component) {
            setComponentTip((Component) tipObject, tip.getTipName());
        } else if (tipObject instanceof Icon) {
            setComponentTip(new JLabel((Icon) tipObject), tip.getTipName());
        } else if (tipObject instanceof HtmlTipData) {
            currentTipComponent = createHtmlTip((HtmlTipData) tipObject, tip.getTipName());
        } else {
            currentTipComponent = createTextTip(tipObject, tip.getTipName());
        }
    }

    private boolean isInvalidTip(int currentTip, TipOfTheDayModel model) {
        // tip does not fall in current tip range
        return model == null || model.getTipCount() == 0 || currentTip < 0 || currentTip >= model.getTipCount();
    }

    private void setComponentTip(Component component, String tipName) {
        component.setName(tipName);
        currentTipComponent = component;
    }

    private JScrollPane createHtmlTip(HtmlTipData htmlTipData, String tipName) throws IOException, SAXException {
        DocumentSource docSource =
                new DefaultDocumentSource(htmlTipData.getDocumentUri().toString());
        DOMSource parser = new DefaultDOMSource(docSource);
        Document doc = parser.parse();

        MediaSpec media = new MediaSpec("screen");
        updateCurrentMedia(media);

        // Create the CSS analyzer
        DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
        da.setDefaultEncoding(StandardCharsets.UTF_8.name());
        da.setMediaSpec(media);

        da.attributesToStyles();
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
        da.getStyleSheets();

        BrowserCanvas browserCanvas = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
        browserCanvas.setConfig(browserConfig);
        browserCanvas.createLayout(tipScrollPane.getSize(), tipScrollPane.getVisibleRect());
        docSource.close();

        browserCanvas.setName(tipName);
        browserCanvas.setOpaque(false);
        tipScrollPane.getViewport().setView(browserCanvas);
        tipScrollPane.setVisible(true);
        return tipScrollPane;
    }

    /**
     * Updates the given media specification according to the real screen parametres (if they may be obtained).
     * @param media The media specification to be updated.
     */
    protected void updateCurrentMedia(MediaSpec media) {
        Dimension size = tipScrollPane.getViewport().getSize();
        Dimension deviceSize = Toolkit.getDefaultToolkit().getScreenSize();
        ColorModel colors = Toolkit.getDefaultToolkit().getColorModel();

        media.setDimensions(size.width, size.height);
        media.setDeviceDimensions(deviceSize.width, deviceSize.height);
        media.setColor(colors.getComponentSize()[0]);
        if (colors instanceof IndexColorModel) {
            media.setColorIndex(((IndexColorModel) colors).getMapSize());
        }
        media.setResolution(Toolkit.getDefaultToolkit().getScreenResolution());
    }

    private JScrollPane createTextTip(Object tipContent, String tipName) {
        String text = tipContent == null ? "" : tipContent.toString();
        if (BasicHTML.isHTMLString(text)) {
            return createHtmlEditorPane(text, tipName);
        } else {
            return createPlainTextArea(text, tipName);
        }
    }

    private JScrollPane createHtmlEditorPane(String text, String tipName) {
        tipEditorPane.removeAll();
        tipEditorPane.setContentType("text/html");
        tipEditorPane.setText(text);
        tipEditorPane.setFont(tipFont);
        BasicHTML.updateRenderer(tipEditorPane, text);
        tipEditorPane.setName(tipName);
        tipScrollPane.getViewport().setView(tipEditorPane);
        return tipScrollPane;
    }

    private JScrollPane createPlainTextArea(String text, String tipName) {
        tipTextArea.setText(text);
        tipTextArea.setFont(tipFont);
        tipTextArea.setName(tipName);
        tipScrollPane.getViewport().setView(tipTextArea);
        return tipScrollPane;
    }

    class ChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TipOfTheDay.CURRENT_TIP_CHANGED_KEY.equals(evt.getPropertyName())) {
                showCurrentTip();
            }
        }
    }

    class PreviousTipAction extends AbstractAction {
        public PreviousTipAction() {
            super(PREVIOUS_TIP_ACTION);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tipPane.previousTip();
        }

        @Override
        public boolean isEnabled() {
            return tipPane.isEnabled();
        }
    }

    class NextTipAction extends AbstractAction {
        public NextTipAction() {
            super(NEXT_TIP_ACTION);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tipPane.nextTip();
        }

        @Override
        public boolean isEnabled() {
            return tipPane.isEnabled();
        }
    }
}
