/*
 *
 * Copyright 2009 Sun Microsystems, Inc.
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
package tokyo.northside.swing.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tokyo.northside.tipoftheday.TipOfTheDay;
import tokyo.northside.tipoftheday.data.HtmlTipData;
import tokyo.northside.tipoftheday.plaf.TipOfTheDayUI;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.DefaultTipOfTheDayModel;
import tokyo.northside.tipoftheday.tips.TipLoader;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

/**
 * A demo for the {@code TipOfTheDay}.
 *
 * @author Karl George Schaefer
 * @author l2fprod (original JXTipOfTheDayDemoPanel)
 * @author Hiroshi Miura
 */
public class TipOfTheDayDemo extends JPanel {
    private static final JFrame frame = new JFrame();

    // model of "tip of the day" component.
    private TipOfTheDayModel model;
    // controller of "tip of the day" component.
    private TipOfTheDay totd1;


    // gui parts for embed demo.
    private JButton previousTipButton;
    private JButton nextTipButton;
    private JButton dialogButton;
    JButton closeButton;

    /**
     * The main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("\"Tip of the Day\" Demo Application");
            TipOfTheDayDemo demoPanel = new TipOfTheDayDemo();
            frame.getContentPane().add(demoPanel);
            frame.getRootPane().setDefaultButton(demoPanel.closeButton);
            frame.setPreferredSize(new Dimension(950, 500));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public TipOfTheDayDemo() {
        super(new BorderLayout());
        createTipOfTheDayDemo();
    }

    private void createTipOfTheDayDemo() {
        model = createTipOfTheDayModel();
        // Create a component of TipOfTheDay.
        JPanel panel = new JPanel();
        totd1 = new TipOfTheDay(model);
        // When you want to customize UI design, you can make your
        // custom class inherit from BasicTipOfTheDayUI, and set it
        // using setUI method.
        // ex, totd1.setUI(new BasicTipOfTheDayUI(totd1));
        totd1.setPreferredSize(new Dimension(900, 450));
        totd1.setName("totd");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totd1);
        add(panel);

        JPanel buttonPanel = new JPanel();
        previousTipButton = new JButton();
        previousTipButton.setText("Previous tip(F1)<<");
        buttonPanel.add(previousTipButton);
        nextTipButton = new JButton();
        nextTipButton.setText(">>Next tip(F2)");
        buttonPanel.add(nextTipButton);

        dialogButton = new JButton();
        dialogButton.setText("show tip of the day as dialog(D)");
        buttonPanel.add(dialogButton);
        closeButton = new JButton();
        closeButton.setText("Close(ctrl Q)");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        setupActions();
        // set keymap for actions of TipOfTheDay
        totd1.getInputMap().put(KeyStroke.getKeyStroke("F1"),"previousTip");
        totd1.getInputMap().put(KeyStroke.getKeyStroke("F2"), "nextTip");
        // set keymap for our actions.
        getInputMap().put(KeyStroke.getKeyStroke("D"), "dialog");
        getInputMap().put(KeyStroke.getKeyStroke("ctrl Q"), "close");
    }

    /**
     * set actions for embed demo.
     */
    private void setupActions() {
        previousTipButton.addActionListener(e -> totd1.previousTip());
        nextTipButton.addActionListener(e -> totd1.nextTip());
        dialogButton.addActionListener(e -> showDialog(TipOfTheDayDemo.this, model));
        closeButton.addActionListener(actionEvent -> {
            if (frame.isDisplayable()) {
                frame.dispose();
            }
        });

        // define our actions for key map.
        getActionMap().put("dialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDialog(TipOfTheDayDemo.this, model);
            }
        });
        getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frame.isDisplayable()) {
                    frame.dispose();
                }
            }
        });
    }

    private static final String PREFS_KEY = "tipsoftheday_demo_show_on_startup";
    private static final String PREFS_NOT_SET = "prefs_not_set";
    private static final String PREFS_YES = "true";
    private static final String PREFS_NO = "false";

    /**
     * Choice implementation demo.
     * This use {@link java.util.prefs.Preferences} for persistence of configuration.
     */
    static class ShowOnStartupChoiceImpl implements TipOfTheDay.ShowOnStartupChoice {
        private final Preferences prefs;

        public ShowOnStartupChoiceImpl() {
            prefs = Preferences.userNodeForPackage(TipOfTheDayDemo.class);
        }

        @Override
        public void setShowingOnStartup(boolean showOnStartup) {
            // Save to storage.
            // Demo uses Java Preferences feature.
            if (showOnStartup) {
                prefs.put(PREFS_KEY, PREFS_YES);
            } else {
                prefs.put(PREFS_KEY, PREFS_NO);
            }
        }

        @Override
        public boolean isShowingOnStartup() {
            // true when preference is to show "Tip of the Day" dialog on start-up.
            String config = prefs.get(PREFS_KEY, PREFS_NOT_SET);
            return config.equals(PREFS_NOT_SET) || config.equalsIgnoreCase("true");
        }
    }

    /**
     * Show Dialog.
     * <p>
     * demonstrate how to create a custom Dialog of Tip of the Day.
     * Use {@link TipOfTheDay#showDialog}
     * when default UI is ok for you, otherwise please use
     * {@link TipOfTheDayUI#createDialog}
     * This demo example explains how to customize dialog size.
     */
    protected static void showDialog(JComponent parent, TipOfTheDayModel model) {
        TipOfTheDay.ShowOnStartupChoice choice = new ShowOnStartupChoiceImpl();
        final ResourceBundle rb = ResourceBundle.getBundle("tokyo.northside.swing.demo.TipOfTheDayDemo");
        for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements(); ) {
            String key = keys.nextElement();
            UIManager.getDefaults().put(key, rb.getObject(key));
        }
        TipOfTheDay totd = new TipOfTheDay(model);
        totd.setCurrentTip(0);
        // When using default dialog:
        //   boolean result = totd.showDialog(parent, choice, true);
        // Here we use a primitive method to customize dialog size.
        JDialog dialog = totd.getUI().createDialog(parent, choice);
        dialog.setPreferredSize(new Dimension(600, 350));
        dialog.pack();
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Example of "tip of the day" model.
     * @return the model object.
     */
    protected TipOfTheDayModel createTipOfTheDayModel() {
        // Create a tip model with some tips
        DefaultTipOfTheDayModel tips = new DefaultTipOfTheDayModel();

        // plain text
        tips.add(new DefaultTip("Plain Text Tip", "This is the first tip " + "This is the first tip "
                + "This is the first tip " + "This is the first tip " + "This is the first tip "
                + "This is the first tip\n" + "This is the first tip " + "This is the first tip"));

        // html text
        tips.add(DefaultTip.of("HTML Text Tip", "<html>This is an html <b>TIP</b><br><center>"
                + "<table border=\"1\">" + "<tr><td>1</td><td>entry 1</td></tr>"
                + "<tr><td>2</td><td>entry 2</td></tr>" + "<tr><td>3</td><td>entry 3</td></tr>"
                + "</table>"));

        // a Component
        tips.add(DefaultTip.of("Component Tip", new JTree()));

        // an Icon
        tips.add(DefaultTip.of("Icon Tip", new ImageIcon(
                Objects.requireNonNull(TipOfTheDayDemo.class.getClassLoader().getResource("tips/TipOfTheDayDemo.png")))));

        // HTML from resource
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("tips/SimpleText.html")) {
            if (is != null) {
                tips.add(new DefaultTip("Simple HTML", new String(is.readAllBytes(), StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
        }

        // Use TipLoader utility
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("tips/SimpleTips.properties")) {
            props.load(is);
        } catch (IOException ignored) {
        }
        TipOfTheDayModel propTips = TipLoader.load(props);
        // you can use propTips as your model for TipOfTheDay constructor.
        // ex. new TipOfTheDay(propTips);
        // Here we retrieve tip objects from the model and merge it into the default model.
        int numPropTips = propTips.getTipCount();
        for (int i = 0; i < numPropTips; i++) {
            tips.add(propTips.getTipAt(i));
        }

        // HTML file
        Map<String, URI> tipsUri = new TreeMap<>();
        tipsUri.put("DAY 1", getTipsFileURI("day1.html"));
        tipsUri.put("DAY 2", getTipsFileURI("day2.html"));
        tipsUri.put("DAY 3", getTipsFileURI("day3.html"));
        for (Map.Entry<String, URI> en: tipsUri.entrySet()) {
            try {
                tips.add(DefaultTip.of(en.getKey(), HtmlTipData.from(en.getValue())));
            } catch (IOException ignored) {
            }
        }
        return tips;
    }

    // --- utilities --

    public static URI getTipsFileURI(String filename) {
        return getTipsFileURI(filename, null);
    }

    public static URI getTipsFileURI(String filename, Locale locale) {
        String path;
        // find in install_dir
        path = locale == null ? filename : locale.toLanguageTag() + File.separator + filename;
        File file = Paths.get(installDir(),"docs", "tips", path).toFile();
        if (file.isFile()) {
            return file.toURI();
        }
        // find in classpath
        path = locale == null ? filename : locale.toLanguageTag() + '/' + filename;
        URL url = TipOfTheDayDemo.class.getResource('/' + "tips" + '/' + path);
        if (url != null) {
            try {
                return url.toURI();
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    /** Caching install dir */
    private static String installDir = null;

    /**
     * Returns demo app installation directory.
     */
    public static String installDir() {
        if (installDir == null) {
            File file = null;
            try {
                URI sourceUri = TipOfTheDayDemo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                if (sourceUri.getScheme().equals("file")) {
                    File uriFile = Paths.get(sourceUri).toFile();
                    // If running from a JAR, get the enclosing folder
                    // (the JAR is assumed to be at the installation root)
                    if (uriFile.getName().endsWith(".jar")) {
                        file = uriFile.getParentFile();
                    }
                }
            } catch (URISyntaxException ignored) {
            }
            if (file == null) {
                file = Paths.get(".").toFile();
            }
            installDir = file.getAbsolutePath();
        }
        return installDir;
    }

}
