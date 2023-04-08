How to customize UI
===================

"Tip of the Day" framework use a Swing standard way for portable Look and Feel.
TipOfTheDay class set its "view" from ``UIManager.getUI(TipOfTheDay.getUIClassID())``


TipOfTheDayUI
-------------

A portable Look and Feel base class for TipOfTheDay class.

BasicTipOfTheDayUI
------------------

A plaf basic class that have common methods to handle all the components.

DefaultTipOfTheDayUI
--------------------

Default UI class for TipOfTheDay.

