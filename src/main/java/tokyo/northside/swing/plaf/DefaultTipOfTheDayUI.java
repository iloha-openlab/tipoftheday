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
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import tokyo.northside.swing.JXTipOfTheDay;

/**
 * Default implementation of the <code>JXTipOfTheDay</code> UI.
 *
 * @author Hiroshi Miura
 */
public class DefaultTipOfTheDayUI extends BasicTipOfTheDayUI {

    public DefaultTipOfTheDayUI(JXTipOfTheDay tipPane) {
        super(tipPane);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        UIDefaults defaults = UIManager.getDefaults();
        addUIDefaults(defaults);
        installDefaults(defaults);
        installKeyboardActions();
        installComponents();
        installListeners();

        showCurrentTip();
    }

    protected void installDefaults(UIDefaults defaults) {
        tipPane.setBackground(defaults.getColor("TipOfTheDay.background"));
        tipPane.setForeground(defaults.getColor("TipOfTheDay.foreground"));
        tipPane.setFont(defaults.getFont("TipOfTheDay.font"));
        tipPane.setBorder(defaults.getBorder("TipOfTheDay.border"));
        tipPane.setOpaque(true);
        tipFont = defaults.getFont("TipOfTheDay.tipFont");
    }

    protected void installComponents(UIDefaults defaults) {
        tipPane.setLayout(new BorderLayout());

        // tip icon
        JLabel tipIcon = new JLabel(defaults.getString("TipOfTheDay.didYouKnowText", tipPane.getLocale()));
        tipIcon.setIcon(defaults.getIcon("TipOfTheDay.icon"));
        tipIcon.setBorder(BorderFactory.createEmptyBorder(22, 15, 22, 15));
        tipPane.add("North", tipIcon);

        // tip area
        tipArea = new JPanel(new BorderLayout(2, 2));
        tipArea.setOpaque(false);
        tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tipPane.add("Center", tipArea);
    }

    protected void addUIDefaults(UIDefaults defaults) {
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
            font = new Font("Dialog", Font.PLAIN, 12);
        }
        font = font.deriveFont(Font.BOLD, 13f);

        defaults.put(JXTipOfTheDay.uiClassID, DefaultTipOfTheDayUI.class.getName());
        Font tipOfTheDayFont = UIManager.getFont("TextPane.font");
        if (tipOfTheDayFont == null) {
            tipOfTheDayFont = new Font("Serif", Font.PLAIN, 12);
        }
        defaults.put("TipOfTheDay.font", tipOfTheDayFont);
        defaults.put("TipOfTheDay.tipFont", new FontUIResource(font));
        defaults.put("TipOfTheDay.background", new ColorUIResource(Color.WHITE));
        defaults.put(
                "TipOfTheDay.icon", LookAndFeel.makeIcon(DefaultTipOfTheDayUI.class, "resources/TipOfTheDay24.gif"));
        defaults.put(
                "TipOfTheDay.border", new BorderUIResource(BorderFactory.createLineBorder(new Color(117, 117, 117))));
        // TODO: Add resource
        // "TipOfTheDay.didYouKnowText"
    }
}
