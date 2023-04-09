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
package tokyo.northside.swing.plaf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
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
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;

import org.fit.cssbox.swingbox.BrowserPane;
import tokyo.northside.swing.TipOfTheDay;
import tokyo.northside.swing.TipOfTheDay.ShowOnStartupChoice;
import tokyo.northside.swing.data.HtmlTipData;
import tokyo.northside.swing.tips.Tip;

/**
 * Base implementation of the <code>TipOfTheDay</code> UI.
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
@SuppressWarnings("unused")
public class BasicTipOfTheDayUI extends TipOfTheDayUI {

    public static ComponentUI createUI(JComponent c) {
        return new BasicTipOfTheDayUI((TipOfTheDay) c);
    }

    protected TipOfTheDay tipPane;
    protected JPanel tipArea;
    protected Component currentTipComponent;

    protected Font tipFont;
    protected PropertyChangeListener changeListener;

    public BasicTipOfTheDayUI(TipOfTheDay tipPane) {
        super();
        this.tipPane = tipPane;
        UIManager.getDefaults().addResourceBundle("tokyo.northside.swing.plaf.BasicTipOfTheDayUI");
    }

    @Override
    public JDialog createDialog(Component parentComponent, final ShowOnStartupChoice choice) {
        return createDialog(parentComponent, choice, true);
    }

    @SuppressWarnings("SameParameterValue")
    protected JDialog createDialog(
            Component parentComponent, final ShowOnStartupChoice choice, boolean showPreviousButton) {
        Locale locale = parentComponent == null ? null : parentComponent.getLocale();
        // XXX
        String title = UIManager.getString("TipOfTheDay.dialogTitle", locale);

        final JDialog dialog;

        Window window;
        if (parentComponent == null) {
            window = JOptionPane.getRootFrame();
        } else {
            window = (parentComponent instanceof Window)
                    ? (Window) parentComponent
                    : SwingUtilities.getWindowAncestor(parentComponent);
        }

        if (window instanceof Frame) {
            dialog = new JDialog((Frame) window, title, true);
        } else {
            dialog = new JDialog((Dialog) window, title, true);
        }

        dialog.getContentPane().setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().add(tipPane, BorderLayout.CENTER);
        ((JComponent) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JCheckBox showOnStartupBox;

        // tip control
        JPanel controls = new JPanel(new BorderLayout());
        dialog.add("South", controls);

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
            previousTipButton.addActionListener(getActionMap().get("previousTip"));
        }

        JButton nextTipButton = new JButton(UIManager.getString("TipOfTheDay.nextTipText", locale));
        buttons.add(nextTipButton);
        nextTipButton.addActionListener(getActionMap().get("nextTip"));

        JButton closeButton = new JButton(UIManager.getString("TipOfTheDay.closeText", locale));
        buttons.add(closeButton);

        final ActionListener saveChoice = e -> {
            ShowOnStartupChoice showOnStartupChoice = (ShowOnStartupChoice) e.getSource();
            if (showOnStartupChoice != null && showOnStartupBox != null) {
                showOnStartupChoice.setShowingOnStartup(showOnStartupBox.isSelected());
            }
        };

        closeButton.addActionListener(e -> {
            if (choice != null) {
                saveChoice.actionPerformed(new ActionEvent(choice, 0, null));
            }
            dialog.setVisible(false);
        });
        dialog.getRootPane().setDefaultButton(closeButton);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (choice != null) {
                    saveChoice.actionPerformed(new ActionEvent(choice, 0, null));
                }
            }
        });

        ((JComponent) dialog.getContentPane())
                .registerKeyboardAction(
                        saveChoice, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);

        return dialog;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        installDefaults();
        installKeyboardActions();
        installComponents();
        installListeners();

        showCurrentTip();
    }

    protected void installKeyboardActions() {
        ActionMap map = getActionMap();
        if (map != null) {
            SwingUtilities.replaceUIActionMap(tipPane, map);
        }
    }

    ActionMap getActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put("previousTip", new PreviousTipAction());
        map.put("nextTip", new NextTipAction());
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
        tipFont = UIManager.getFont("TipOfTheDay.tipFont");
    }

    protected void installComponents() {
        tipPane.setLayout(new BorderLayout());
        tipArea = new JPanel(new BorderLayout(2, 2));
        tipArea.setOpaque(false);
        tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tipPane.add("Center", tipArea);
    }

    protected void showCurrentTip() {
        if (currentTipComponent != null) {
            tipArea.remove(currentTipComponent);
        }

        int currentTip = tipPane.getCurrentTip();
        if (currentTip == -1) {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setBackground(UIManager.getColor("TextArea.background"));
            currentTipComponent = label;
            tipArea.add("Center", currentTipComponent);
            return;
        }

        // tip does not fall in current tip range
        if (tipPane.getModel() == null
                || tipPane.getModel().getTipCount() == 0
                || (currentTip < 0 && currentTip >= tipPane.getModel().getTipCount())) {
            currentTipComponent = new JLabel();
        } else {
            Tip tip = tipPane.getModel().getTipAt(currentTip);

            Object tipObject = tip.getTip();
            if (tipObject instanceof Component) {
                currentTipComponent = (Component) tipObject;
            } else if (tipObject instanceof Icon) {
                currentTipComponent = new JLabel((Icon) tipObject);
            } else if (tipObject instanceof HtmlTipData) {
                HtmlTipData data = (HtmlTipData)tipObject;
                try {
                    URL url = data.getDocumentUri().toURL();
                    JScrollPane tipScroll = new JScrollPane();
                    tipScroll.setBorder(null);
                    tipScroll.setOpaque(false);
                    tipScroll.getViewport().setOpaque(false);
                    tipScroll.setBorder(null);
                    tipScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    BrowserPane editor = new BrowserPane();
                    editor.setEditable(false);
                    editor.setPage(url);
                    editor.setFont(tipPane.getFont());
                    tipScroll.getViewport().setView(editor);
                    currentTipComponent = tipScroll;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                JScrollPane tipScroll = new JScrollPane();
                tipScroll.setBorder(null);
                tipScroll.setOpaque(false);
                tipScroll.getViewport().setOpaque(false);
                tipScroll.setBorder(null);

                String text = tipObject == null ? "" : tipObject.toString();

                if (BasicHTML.isHTMLString(text)) {
                    JEditorPane editor = new JEditorPane("text/html", text);
                    editor.setFont(tipPane.getFont());
                    BasicHTML.updateRenderer(editor, text);
                    editor.setEditable(false);
                    editor.setBorder(null);
                    editor.setMargin(null);
                    editor.setOpaque(false);
                    editor.setFont(tipFont);
                    tipScroll.getViewport().setView(editor);
                } else {
                    JTextArea area = new JTextArea(text);
                    area.setFont(tipPane.getFont());
                    area.setEditable(false);
                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);
                    area.setBorder(null);
                    area.setMargin(null);
                    area.setOpaque(false);
                    area.setFont(tipFont);
                    tipScroll.getViewport().setView(area);
                }

                currentTipComponent = tipScroll;
            }
        }

        tipArea.add("Center", currentTipComponent);
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
            super("previousTip");
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
            super("nextTip");
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
