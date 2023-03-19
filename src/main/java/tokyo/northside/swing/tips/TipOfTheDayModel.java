/*
 * $Id: TipOfTheDayModel.java 542 2005-10-10 18:03:15Z rbair $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
package tokyo.northside.swing.tips;

import tokyo.northside.swing.JXTipOfTheDay;

/**
 * A model for {@link JXTipOfTheDay}.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public interface TipOfTheDayModel {

    /**
     * @return the number of tips in this model
     */
    int getTipCount();

    /**
     * @param index
     * @return the tip at <code>index</code>
     * @throws IndexOutOfBoundsException
     *           if the index is out of range (index &lt; 0 || index &gt;=
     *           getTipCount()).
     */
    Tip getTipAt(int index);
}
