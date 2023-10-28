/**
 *
 * Copyright 2004 Sun Microsystems, Inc.
 *           2023 Hiroshi Miura
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package tokyo.northside.tipoftheday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.GraphicsEnvironment;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTree;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.DefaultTipOfTheDayModel;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipLoader;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

public class TipOfTheDayTest {

    @Test
    public void testBean() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        PropertyChangeReport report = new PropertyChangeReport();
        TipOfTheDay totd = new TipOfTheDay();
        assertEquals(0, totd.getCurrentTip());
        // with an empty model next/previous are no-op
        totd.nextTip();
        totd.previousTip();
        assertFalse(report.hasEvents(TipOfTheDay.CURRENT_TIP_CHANGED_KEY));

        totd.addPropertyChangeListener(report);

        DefaultTipOfTheDayModel model = new DefaultTipOfTheDayModel();
        model.add(new DefaultTip("name1", "description1"));
        model.add(new DefaultTip("name2", "<html>description2"));
        model.add(new DefaultTip("name3", new ImageIcon()));
        model.add(new DefaultTip("name3", new JTree()));
        Tip tmpTip = new DefaultTip("name4", new JTable());
        model.add(tmpTip);
        model.remove(tmpTip);

        totd.setModel(model);
        assertTrue(report.hasEvents("model"));

        assertFalse(report.hasEvents(TipOfTheDay.CURRENT_TIP_CHANGED_KEY));
        assertEquals(0, totd.getCurrentTip());
        totd.nextTip();
        assertTrue(report.hasEvents(TipOfTheDay.CURRENT_TIP_CHANGED_KEY));
        assertEquals(1, totd.getCurrentTip());
        totd.nextTip();
        assertEquals(2, totd.getCurrentTip());
        totd.nextTip();
        assertEquals(3, totd.getCurrentTip());
        // tips cycle when bounds are reached
        totd.nextTip();
        assertEquals(0, totd.getCurrentTip());
        totd.previousTip();
        assertEquals(3, totd.getCurrentTip());

        // do not accept to go outside of tip count bounds
        try {
            totd.setCurrentTip(-1);
            fail("Negative tip!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            totd.setCurrentTip(totd.getModel().getTipCount());
            fail("Over the limit!");
        } catch (IllegalArgumentException ignored) {
        }

        // do not accept null model
        try {
            totd.setModel(null);
            fail("Should not reach this code");
        } catch (IllegalArgumentException ignored) {
        }

        DefaultTip tip = new DefaultTip();
        assertNull(tip.getTip());
        assertNull(tip.getTipName());
        tip.setTipName("name");
        assertEquals("name", tip.getTipName());
        tip.setTip("tip");
        assertEquals("tip", tip.getTip());
        assertEquals(tip.getTipName(), tip.toString());
    }

    @Test
    public void testTipLoader() {
        TipOfTheDayModel model;

        Properties props = new Properties();

        // empty properties create empty model
        model = TipLoader.load(props);
        assertEquals(0, model.getTipCount());

        // one tip with name and description
        props.put("tip.1.name", "name1");
        props.put("tip.1.description", "description1");
        model = TipLoader.load(props);
        assertEquals(1, model.getTipCount());
        assertEquals("name1", model.getTipAt(0).getTipName());
        assertEquals("description1", model.getTipAt(0).getTip());

        // one tip with description only (name is optional)
        props.put("tip.2.description", "description2");
        model = TipLoader.load(props);
        assertEquals(2, model.getTipCount());
        assertEquals("name1", model.getTipAt(0).getTipName());
        assertEquals("description1", model.getTipAt(0).getTip());
        assertNull(model.getTipAt(1).getTipName());
        assertEquals("description2", model.getTipAt(1).getTip());

        // one tip with name only, but description is mandatory!
        props.put("tip.3.name", "name3");
        try {
            TipLoader.load(props);
            fail("Must not get here");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // one missing number in the tip sequence
        props.put("tip.3.description", "desc3");
        props.put("tip.5.description", "desc5");
        model = TipLoader.load(props);
        assertEquals(3, model.getTipCount());
    }

    @Test
    public void testChoice() {
        TipOfTheDay.ShowOnStartupChoice noshow = new TipOfTheDay.ShowOnStartupChoice() {
            public boolean isShowingOnStartup() {
                return false;
            }

            public void setShowingOnStartup(boolean showOnStartup) {}
        };

        TipOfTheDay totd = new TipOfTheDay();
        assertFalse(totd.showDialog(null, noshow));
    }
}
