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
package tokyo.northside.swing.tips;

/**
 * Default {@link Tip} implementation.
 *
 * @author Frederic Lavigne
 * @author Hiroshi Miura
 */
public class DefaultTip implements Tip {

    private String name;

    private Object tip;

    public DefaultTip() {}

    public DefaultTip(String name, Object tip) {
        this.name = name;
        this.tip = tip;
    }

    @Override
    public Object getTip() {
        return tip;
    }

    public void setTip(Object tip) {
        this.tip = tip;
    }

    @Override
    public String getTipName() {
        return name;
    }

    public void setTipName(String name) {
        this.name = name;
    }

    public static Tip of(String name, Object value) {
        return new DefaultTip(name, value);
    }

    @Override
    public String toString() {
        return getTipName();
    }
}
