# Changelog

## Version 0.2.3x

- Endless loop detection (via runtime timeouts for submission logic)
- Added string representation of arrays
- Improvements in array comparisons (assertEquals())
- Several bugfixes

## Version 0.2.3

- Added captureException() to DSL to check for expected exceptions
- Configurable checkstyle penalties (style_penalties.json)
- Several bugfixes

## Version 0.2.2

- Internal class compare optimizations

## Version 0.2.1

- Maximum line length for submissions is now 120 characters.
- Isolated submission and evaluation logic stdout streams (to prevent injection attacks)

## Version 0.2.0 (JEdUnit)

- vavr integration for better expressiveness to formulate tests (especially tuple support)
- JavaParser integration with additional selector support
- Checkstyle integration
- Structural comparision of classes (classCompare())
- Randomized test data generators
- URL callable init script
- Self-contained JAR
- Gradle build system
- Abort on suspect code patterns (Injection, Redirection)
- Pre-defined (opt-in/-out) coding constraints common in educational contexts

## Version 0.1.0

- Basic VPL Template