/*
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
 *
 */
package tokyo.northside.swing.tips;

import tokyo.northside.swing.TipOfTheDay;

/**
 * A tip.<br>
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
public interface Tip {

    /**
     * @return very short (optional) text describing the tip
     */
    String getTipName();

    /**
     * The tip object to show. See {@link TipOfTheDay} for supported object
     * types.
     *
     * @return the tip to display
     */
    Object getTip();
}
