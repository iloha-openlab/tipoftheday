How to use "Tip of the Day" library
===================================

"Tip of the Day" library is extension of Java Swing GUI framework, which provide
UI component to show a "Tip of the Day" dialog, or show it on panel / pane.

You can find a demo application in a ``demo`` folder.

There are several classes to realize "Tip of the Day" feature.

TipOfTheDayModel
----------------

Data model interface of a component.

DefaultTipOfTheDayModel
-----------------------

Default model which implement ``TipOfTheDayModel`` interface.
It has a data manipulation method ``#add`` which accept an object
which implements ``Tip`` interface.

You can use ``DefaultTip`` data class that accept a text, html text,
UI component.

``DefaultTipOfTheDayModel`` constructor can accept ``List<Tip>`` list
object.

TipLoader
---------

You can create ``Tip`` object from properties.

.. code-block:: none

   tip.1.name=First Tip
   tip.1.description=This is the description
   tip.2.name=Second Tip
   tip.2.description=&lt;html&gt;This is an html description


HtmlTipData
-----------

Data class that represents HTML file URI, base path and its text.
A complete class of BasicTipOfTheDayUI abstract class, or DefaultTipOfTheDay
can show HTML contents using SwingBox library's "BrowserPane" component
that can parse HTML5 and CSS.


TipOfTheDay
-----------

Controller of "Tip of the Day" UI component.
Constructor requests the model.
It has several controller methods.

- ``#previousTip``
- ``#nextTip``
- ``#setCurrentTip``
- ``#getCurrentTip``
- ``#showDialog``

``previousTip`` and ``nextTip`` methods change content shown in order of the model.
``setCurrentTip`` takes integer number that is an index of the model.

``showDialog`` create an dialog with default UI design and show on parent component.
You can pass ``TipOfTheDay.ShowOnStartUpChoice`` object which is data class to
keep a status of "show on startup" checkbox choice.
When pass ``force`` boolean argument with ``true``, it shows the dialog even when
``choice`` indicate not to show.


Portable look and feel UI classes
=================================

"Tip of the Day" framework use a Swing standard way for portable Look and Feel.
TipOfTheDay class set its "view" from ``UIManager.getUI(TipOfTheDay.getUIClassID())``


TipOfTheDayUI
-------------

A portable Look and Feel base abstract class for TipOfTheDay class.
A complete class can be obtained by ``TipOfTheDay#getUI`` method.

You can obtain "Tip of the Day" dialog using ``TipOfTheDayUI#createDialog``
method.

When you do not want to interact with UI class, you can show the dialog
using ``TipOfTheDay#showDialog`` method.

After obtaining ``JDialog`` object by ``TipOfTheDayUI#createDialog``,
you can decorate and modify the object as you want.

You can find an example in a demo application that set a size of
dialog using ``setPrefferedSize`` method.
Please see a ``ShowDialogActionListener`` class.

BasicTipOfTheDayUI
------------------

A plaf basic class that have common methods to handle all the components.
When you want to build your own UI class, you can extends the ``BasicTipOfTheDayUI``
class.


DefaultTipOfTheDayUI
--------------------

Default UI class for ``TipOfTheDay``.
When ``UIManager.get(uiClassID)`` returns null,  ``TipOfTheDay`` constructor
use the ``DefaultTipOfTheDayUI`` as a plaf class.
