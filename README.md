# "Tip of the Day" extension for a Java/Swing GUI framework

Provides the "Tip of The Day" pane and dialog.

## Description

Tips are retrieved from the `TipOfTheDayModel` class.
In the most common usage, a tip (as returned by `TipOfTheDayModel.Tip#getTip()`)
is just a `String`. However, the return type of this method is actually `Object`.
Its interpretation depends on its type:

- HTML files
  - You can show rich styled tips from HTML files, please look at a demo code.
  - TipOfTheDay supports CSS, powered by CSSBox library.
  - HtmlTipData class is a placeholder of HTML file.
  - You can use HtmlTipData.from("path/to/html/file")
- Component
  - The `Component` is displayed in the dialog.
- Icon
  - The Icon is wrapped in a JLabel and displayed in the dialog.
- Others
  - The object is converted to a `String` by calling its `toString` method.
  - The result is wrapped in a `JEditorPane` or `JTextArea` and displayed.

![TipOfTheDayCapture.png](docs%2FTipOfTheDayCapture.png)

HTML with CSS.

## Origin

Fork of SwingX project.

## Dependency

TipOfTheDay library depends on CSSBox library version 5.0, and SLF4j logging facade library.
User application requires to use a logging engine, such as logback.

## Documentation and sample project

The Manual page is located at https://tip-of-the-day.readthedocs.io/en/stable/index.html

You can find a demo project source under demo subproject folder.

## License

SPDX: LGPL-2.1-or-later

## Copyright

- Copyright (c) 2005-2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
  Clara, California 95054, U.S.A. All rights reserved.
- Copyright (c) 2023-2025 Hiroshi Miura
