# "Tip of the Day" extension for Java/Swing GUI framework

Provides the "Tip of The Day" pane and dialog.<br>

## Description

Tips are retrieved from the `TipOfTheDayModel` class.
In the most common usage, a tip (as returned by `TipOfTheDayModel.Tip#getTip()`)
is just a `String`. However, the return type of this method is actually `Object`.
Its interpretation depends on its type:

* Component
  * The `Component` is displayed in the dialog.
* Icon
  * The Icon is wrapped in a JLabel and  displayed in the dialog.
* Others
  * The object is converted to a `String` by calling its `toString` method.
  * The result is wrapped in a `JEditorPane` or `JTextArea` and displayed.


## Origin

Fork of SwingX project.

## License

LGPL-2.1

## Copyright

- Copyright (c) 2005-2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
  Clara, California 95054, U.S.A. All rights reserved.  
- Copyright (c) 2023 Hiroshi Miura