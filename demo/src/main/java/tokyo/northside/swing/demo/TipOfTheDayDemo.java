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
import java.awt.event.ActionListener;

import javax.swing.*;

import tokyo.northside.swing.JXTipOfTheDay;
import tokyo.northside.swing.plaf.DefaultTipOfTheDayUI;
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
    private JXTipOfTheDay totd;
    private JButton dialogButton;

    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new TipOfTheDayDemo());
            frame.setPreferredSize(new Dimension(800, 600));
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
        totd = new JXTipOfTheDay(model);
        totd.setName("totd");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totd);
        add(panel);

        dialogButton = new JButton();
        dialogButton.setText("show tip of the day");
        add(dialogButton, BorderLayout.SOUTH);
        dialogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JXTipOfTheDay totd = new JXTipOfTheDay(model);
                totd.setCurrentTip(0);
                totd.showDialog(TipOfTheDayDemo.this);
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
        tips.add(new DefaultTip("HTML Text Tip", "<html>This is an html <b>TIP</b><br><center>"
                + "<table border=\"1\">" + "<tr><td>1</td><td>entry 1</td></tr>"
                + "<tr><td>2</td><td>entry 2</td></tr>" + "<tr><td>3</td><td>entry 3</td></tr>"
                + "</table>"));

        // a Component
        tips.add(new DefaultTip("Component Tip", new JTree()));

        // an Icon
        tips.add(new DefaultTip("Icon Tip", new ImageIcon(TipOfTheDayDemo.class.getResource("TipOfTheDayDemo.png"))));

        return tips;
    }
}
