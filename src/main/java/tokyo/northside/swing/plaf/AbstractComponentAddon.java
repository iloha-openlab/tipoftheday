/*
 * $Id: AbstractComponentAddon.java 4047 2011-07-19 18:51:12Z kschaefe $
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
package tokyo.northside.swing.plaf;

/**
 * Ease the work of creating an addon for a component.<br>
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl Schaefer
 */
@SuppressWarnings("nls")
public abstract class AbstractComponentAddon implements ComponentAddon {

    private String name;

    protected AbstractComponentAddon(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void initialize(LookAndFeelAddons addon) {
        addon.loadDefaults(getDefaults(addon));
    }

    @Override
    public void uninitialize(LookAndFeelAddons addon) {
        // commented after Issue 446. Maybe addon should keep track of its
        // added defaults to correctly remove them on uninitialize
        // addon.unloadDefaults(getDefaults(addon));
    }

    /**
     * Adds default key/value pairs to the given list.
     * 
     * @param addon
     * @param defaults
     */
    protected void addBasicDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addMetalDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addMotifDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addWindowsDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addLinuxDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Default implementation calls
     * {@link #addBasicDefaults(LookAndFeelAddons, DefaultsList)}
     * 
     * @param addon
     * @param defaults
     */
    protected void addNimbusDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        addBasicDefaults(addon, defaults);
    }

    /**
     * Gets the defaults for the given addon.
     * 
     * @param addon
     * @return an array of key/value pairs. For example:
     * 
     *         <pre>
     * Object[] uiDefaults = { &quot;Font&quot;, new Font(&quot;Dialog&quot;, Font.BOLD, 12), &quot;Color&quot;,
     *         Color.red, &quot;five&quot;, new Integer(5) };
     * </pre>
     */
    private Object[] getDefaults(LookAndFeelAddons addon) {
        DefaultsList defaults = new DefaultsList();
        addBasicDefaults(addon, defaults);
        return defaults.toArray();
    }
}
