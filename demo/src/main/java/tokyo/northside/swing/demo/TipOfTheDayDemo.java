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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tokyo.northside.swing.TipOfTheDay;
import tokyo.northside.swing.data.HtmlTipData;
import tokyo.northside.swing.plaf.BasicTipOfTheDayUI;
import tokyo.northside.swing.tips.DefaultTip;
import tokyo.northside.swing.tips.DefaultTipOfTheDayModel;
import tokyo.northside.swing.tips.TipOfTheDayModel;

/**
 * A demo for the {@code JXTipOfTheDay}.
 *
 * @author Karl George Schaefer
 * @author l2fprod (original JXTipOfTheDayDemoPanel)
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class TipOfTheDayDemo extends JPanel {
    private TipOfTheDayModel model;
    private static final JFrame frame = new JFrame();

    /**
     * The main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("\"Tip of the Day\" Demo Application");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.add(new JLabel("Following is embed version of Tip of the Day."));
            panel.add(new TipOfTheDayDemo());
            frame.getContentPane().add(panel);
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
        JPanel panel = new JPanel();
        TipOfTheDay totd1 = new TipOfTheDay(model);
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
        JButton previousTipButton = new JButton();
        previousTipButton.setText("Previous tip<<");
        buttonPanel.add(previousTipButton);
        JButton nextTipButton = new JButton();
        nextTipButton.setText(">>Next tip");
        buttonPanel.add(nextTipButton);

        JButton dialogButton = new JButton();
        dialogButton.setText("show tip of the day as dialog");
        buttonPanel.add(dialogButton);
        JButton closeButton = new JButton();
        closeButton.setText("Close");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        previousTipButton.addActionListener(actionEvent -> {
            totd1.previousTip();
        });
        nextTipButton.addActionListener(actionEvent -> {
            totd1.nextTip();
        });

        TipOfTheDay.ShowOnStartupChoice choice = new TipOfTheDay.ShowOnStartupChoice() {
            @Override
            public void setShowingOnStartup(boolean showOnStartup) {
                // Save to storage.
            }

            @Override
            public boolean isShowingOnStartup() {
                // true when preference is to show "Tip of the Day" dialog on start-up.
                return true;
            }
        };

        dialogButton.addActionListener(actionEvent -> {
            TipOfTheDay totd = new TipOfTheDay(model);
            totd.setCurrentTip(0);
            totd.showDialog(TipOfTheDayDemo.this, choice, true);
        });

        closeButton.addActionListener(actionEvent -> {
            if (frame.isDisplayable()) {
                frame.dispose();
            }
        });
    }

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

    public static URI getTipsFileURI(String filename) {
        return getTipsFileURI(filename, null);
    }

    public static URI getTipsFileURI(String filename, Locale locale) {
        // find in install_dir
        String path = locale == null ? filename : locale.toLanguageTag() + File.separator + filename;
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
     * Returns OmegaT installation directory.
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
                    } else {
                        // Running from an IDE or build tool; use "$CWD/demo".
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
