How to use "Tip of the Day" library
===================================

"Tip of the Day" library is extension of Java Swing GUI framework, which provide
UI component to show a "Tip of the Day" dialog, or show it on panel / pane.

You can find a demo application in a ``demo`` folder.

There are three important class to realize "Tip of the Day" feature.

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



JXTipOfTheDay
-------------

Controller of "Tip of the Day" UI component.
Constructor requests the model.
It extends ``JPanel``. You can ``#setPreferredSize``, and ``#setName``.

It has several controller methods.

- ``#previousTip``
- ``#nextTip``
- ``#setCurrentTip``
- ``#showDialog``

