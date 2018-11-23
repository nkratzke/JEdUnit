# JEdUnit

This is a basic evaluation framework to simplify automatic evaluation of (small) __Java__ programming assignments
using [Moodle](https://moodle.org/) and [VPL](http://vpl.dis.ulpgc.es/).

JEdUnit is a unit testing framework with a special focus on educational aspects. It strives to simplify automatic evaluation of (small) __Java__ programming assignments using [Moodle](https://moodle.org/) and [VPL](http://vpl.dis.ulpgc.es/).

We developed this framework mainly for our purposes in programming classes at the Lübeck University of Applied Sciences. However, this framework might be helpful for other programming instructors, and that is why this framework is provided as open source.

## Usage and features

We assume the reader to be familiar with Java in general.
Furthermore, we recommend to study at least the following VPL related documentation:

- [VPL General documentation](http://vpl.dis.ulpgc.es/index.php/support)
- [Moodle VPL Tutorials](http://www.science.smith.edu/dftwiki/index.php/Moodle_VPL_Tutorials) from Smith College

According to our experiences students tend to make use of the following kind of "cheats".

- __Overfitting__ (e.g. to map simply the test values to expected results, outside the scope of the test values the solution is useless)
- __Problem evasion__ (e.g. to solve a problem using loops instead of to solve it recursively)
- __Redirection__ (e.g. to call the reference solution instead of implement it on their own)
- __Injection__ (e.g. to write `System.out.println("Grade :=>> 100"); System.exit(0);` to get full points and prevent VPL to check the submission)

So, and in addition to "normal" unit testing frameworks, JEdUnit provides several addons to handle
educational specifics that are hardly covered by current testing frameworks.

- Checkstyle integration to foster "readable" code (OK, that is basic stuff)
- Randomized test case generation to handle overfitting cheats.
- Parser integration and an easy to use selector model (comparable to CSS selectors for a DOM-tree).
- Predefined code inspections that can be switched on/off depending on assignment specifics (object-orientation, recursion, functional programming with lambdas, best practice collection handling, ...)
- and more ...

## Learn how to use it

To make use of this framework, the following workflow is recommended to set up a basic configuration for a new programming assignment.
First of all, initialize a directory on your local machine.

```
curl -s https://raw.githubusercontent.com/nkratzke/VPL-java-template/working/init.sh | sh
```

Then we recommend to study the [Wiki](../../wiki).
