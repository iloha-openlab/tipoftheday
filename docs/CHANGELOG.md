# Change Log

All notable changes to this project will be documented in this file.

## [Unreleased]

## [v0.5.0]
- Extend to accept Tip URI as tip object (#11)
- Drop dependency for SwingBox, add CSSBox 5.0.2.
- feat: update HTML renderer with CSSBox, changes in BasicTipOfTheDayUI class.
- Bump JUnit@5.12.1
- refactor: UI classes to improve initialization logics for stability and safety.
- CI: update secret handlings, and installation of git.

## [v0.4.4]
- Add integration-test: test UI behavior
- fix: not to create duplicated components to reduce resource
- feat: give names to tips component
    - TipOfTheDay objects have names: "tip_pane" and "tip_area"
    - Each object of tips has a user defined name

## [v0.4.3]
- Change the package name to "tokyo.northside.tipoftheday" to avoid conflict and to align JPMS module name.
- Bump commons-io@2.15.0
- Bump SwingBox@1.2.7

## [v0.4.2]
- Use Gradle version catalog
- Bump Gradle@8.3
- Bump SwingBox@1.2.5
- Bump SLF4J@2.0.7
- Bump Nexus-Publish@2.0.0-rc-1
- Bump spotbugs@5.1.3
- Bump spotless@6.19.0
- Add .readthedocs.yaml
- fix(uiclass): escape key action(#6)
- feat(demo): add actionMap usage demo(#4)
- CI on woodpecker

## [v0.4.1]

- Bump SwingBox@1.2.4
- Update a document
- refactor demo application to show how to customize UI.

## [v0.4.0]

- Change BasicTipOfTheDayUI class to be abstract
  - remove BasicTipOfTheDayUI.createUI static method
- Refactor BasicTipOfTheDayUI, DefaultTipOfTheDayUI class

## [v0.3.3]

- HtmlTipData: Fix NPE when loading html from jar resource. 
- demo: allow produce distZip

## [v0.3.2]

- Reduce log messages verbosity.
- Bump SwingBox@1.2.3

## [v0.3.1]

- fix: Null check in saveChoice action to avoid potential NPE
- doc: Update javadoc

## [v0.3.0]

- Support rich text with HTML5 and CSS parser.
- Change development status Beta
- Add DefaultTip.of method

## [v0.2.2]

- Add contribution guide.

## [v0.2.0]

- Add demo application as subproject
- Implement `createUI` method
- Fix bundle loading
- Add a Japanese bundle
- Use UTF-8 for bundle encoding
- Add document for sphinx document solution.

## [v0.1.1]

- Chore(Gradle): config code signing and publishing.

## v0.1.0

- First release.

[unreleased]: https://codeberg.org/miurahr/tipoftheday/compare/v0.5.0...HEAD
[v0.5.0]: https://codeberg.org/miurahr/tipoftheday/compare/v0.4.4...v0.5.0
[v0.4.4]: https://codeberg.org/miurahr/tipoftheday/compare/v0.4.3...v0.4.4
[v0.4.3]: https://codeberg.org/miurahr/tipoftheday/compare/v0.4.2...v0.4.3
[v0.4.2]: https://codeberg.org/miurahr/tipoftheday/compare/v0.4.1...v0.4.2
[v0.4.1]: https://codeberg.org/miurahr/tipoftheday/compare/v0.4.0...v0.4.1
[v0.4.0]: https://codeberg.org/miurahr/tipoftheday/compare/v0.3.3...v0.4.0
[v0.3.3]: https://codeberg.org/miurahr/tipoftheday/compare/v0.3.2...v0.3.3
[v0.3.2]: https://codeberg.org/miurahr/tipoftheday/compare/v0.3.1...v0.3.2
[v0.3.1]: https://codeberg.org/miurahr/tipoftheday/compare/v0.3.0...v0.3.1
[v0.3.0]: https://codeberg.org/miurahr/tipoftheday/compare/v0.2.2...v0.3.0
[v0.2.2]: https://codeberg.org/miurahr/tipoftheday/compare/v0.2.0...v0.2.2
[v0.2.0]: https://codeberg.org/miurahr/tipoftheday/compare/v0.1.1...v0.2.0
[v0.1.1]: https://codeberg.org/miurahr/tipoftheday/compare/v0.1.0...v0.1.1
