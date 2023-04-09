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

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.plaf.PanelUI;
import tokyo.northside.swing.TipOfTheDay;

/**
 * Pluggable UI for <code>TipOfTheDay</code>.
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
public abstract class TipOfTheDayUI extends PanelUI {

    /**
     * Creates a new JDialog to display a TipOfTheDay panel.
     * If <code>choice</code> is not null then the window will offer a way
     * for the end-user to not show the tip of the day dialog.
     *
     * @param parentComponent parent component for dialog.
     * @param choice config of the choice.
     * @return a new JDialog to display a TipOfTheDay panel
     */
    public abstract JDialog createDialog(Component parentComponent, TipOfTheDay.ShowOnStartupChoice choice);
}
