# JEdUnit

This is a basic evaluation framework to simplify automatic evaluation of (small) __Java__ programming assignments
using [Moodle](https://moodle.org/) and [VPL](http://vpl.dis.ulpgc.es/).

Sadly, in some configurations
necessary unit testing librarieres do work not frictionless with VPLs Jail server concept although they would be more than useful for
automatic grading. Therefore, we developed a framework mainly for our own purposes at the Lübeck University of Applied Sciences.
However, this framework might be helpful for other programming instructors and that is why this framework is provided as open source.

## Intended audience

This VPL evaluation framework is written for programming instructors and teachers at schools, colleges, universities or further
programming training facilities that want (or need) to automatically evaluate and grade (small)
Java programming assignments typically at a "freshman" (1st/2nd semester) level.

## Usage

We assume the reader to be familiar with Java in general.
Furthermore, we recommend to study at least the following VPL related documentation:

- [VPL General documentation](http://vpl.dis.ulpgc.es/index.php/support)
- [Moodle VPL Tutorials](http://www.science.smith.edu/dftwiki/index.php/Moodle_VPL_Tutorials) from Smith College

### Basic configuration

To make use of this framework, the following workflow is recommended to set up a basic configuration for a new programming assignment.
First of all, initialize a directory on your local machine.

```
curl -s https://raw.githubusercontent.com/nkratzke/VPL-java-template/working/init.sh | sh
```

Then

1. Import the following __requested files__ `Main.java` via the Moodle web interface from this repository as __starting point for your students__.
2. Import the following __execution files__ `Checks.java`, `Solution.java`, `style_checks.xml`, `vpl_evaluate.sh`, `vpl_run.sh`, `jedunit.jar`, and `checkstyle.jar` via the Moodle web interface from this directory.
    - `Solution.java` is where you should place your reference solution.
    - `Checks.java` is where you place your grading test cases.
    - _`style_checks.xml` is a [checkstyle](http://checkstyle.sourceforge.net/index.html) file that is used to enforce coding standards. In most cases you do not need to touch this file._
    - _`vpl_run.sh` is the VPL run script that compiles and executes the `Main.java`. In most cases you do not need to touch this file._
    - _`vpl_evaluate.sh` is the VPL evaluate script that runs checkstyle, compiles all Java files for evaluation, and executes `Checks.java` to evaluate student submissions. In most cases you do not need to touch this file._
    - `jedunit.jar`, and `checkstyle.jar` are Java libraries that are needed on the classpath. Do not touch these files.
3. __!!!IMPORTANT!!!__ Select `jedunit.jar` as a __to be kept file__ via the Moodle web interface (otherwise your evaluation will not work).

It is recommended to do these __settings only once for a Moodle course__ and derive all your assignments via the __"based-on"__ feature of the Moodle VPL plugin.

### Writing an assignment

Let us assume a small assignment example. Students have to develop a method to count all occurences of a given char _c_ in a (possible null) String _s_ (non case sensitive). This method should be called like this:

```Java
System.out.println(countChars('o', "Hello World")); // => 2
System.out.println(countChars('w', "Hello World")); // => 1
```

The following basic [Main](Main.java) class is provided as starting point.

```Java
class Main {
    public static void main(String[] args) {
        System.out.println("Your solution is missing here.");
    }
}
```

This class will be extended and uploaded by your students via Moodle. A possible solution might look like that:

```Java
class Main {

    public static int countChars(char c, String s) {
        int n = 0;
        for (char process : s.toLowerCase().toCharArray()) {
            if (process == c) n++;
        }
        return n++;
    }

    public static void main(String[] args) {
        System.out.println(countChars('o', "Hello World")); // => 2
        System.out.println(countChars('w', "Hello World")); // => 1
    }
}
```

Students have the tendency to check their solution minimally by using the provided examples only.
But is the solution correct, and how to check this automatically for grading?

### Writing checks for automatic evaluation of an assignment

To write checks one simply has to take the `Checks.java` class as a template

```Java
import static de.thl.jedunit.Randomized.*;

import de.thl.jedunit.Test;
import de.thl.jedunit.Constraints;
import de.thl.jedunit.Config;

public class Checks extends Constraints {

    @Test(weight=1.0, description="Tests the submission")
    public void testSubmissions() {
        // Please add your checks and grading points here
        grading(10, "This will always fail.", () -> "hello".equals("Hello"));
    }
}
```

and extend it with assignment specific checks and grading points. This could be done like this:

```Java
import static de.thl.jedunit.Randomized.*;

import de.thl.jedunit.Test;
import de.thl.jedunit.Constraints;
import de.thl.jedunit.Config;

public class Checks extends Constraints {

    @Test(weight=0.25, description="Provided example calls")
    public void examples() {
        testWith(
            t('o', "Hello World", 5),
            t('w', "Hello World", 5)
        ).forEach(d -> {
            int expected = Solution.countChars(d._1, d._2);
            grading(d._3, String.format("Counting '%s' in \"%s\" must return %d.", d._1, d._2, expected), 
                () -> Main.countChars(d._1, d._2) == expected
            );
        });
    }

    @Test(weight=0.25, description="Boundary testcases (unknown test cases)")
    public void furtherTestcases() {
        testWith(
            t('x', "", 10),
            t('X', "", 10),
            t('x', "x", 5),
            t('X', "x", 5),
            t('x', "X", 5),
            t('X', "X", 5),
            t('X', "ax", 5),
            t('x', "Xa", 5)
        ).forEach(d -> {
            int expected = Solution.countChars(d._1, d._2);
            String msg = String.format("countChars('%s', \"%s\") -> %d", d._1, d._2, expected);
            grading(d._3, msg, () -> Main.countChars(d._1, d._2) == expected);
        });
    }

    @Test(weight=0.5, description="Randomized testcases")
    public void randomizedTestcases() {
        String r = "[a-zA-Z!?&%$§]{5,17}";
        char c = c("[a-z]");
        char C = Character.toUpperCase(c);

        testWith(
            t(c, s(c + "{1,7}", r, r), 5),
            t(C, s(c + "{1,7}", r, r), 5),
            t(c, s(r.toUpperCase(), r, C + "{1,7}"), 5),
            t(C, s(r.toUpperCase(), r, C + "{1,7}"), 5),
            t(c, s(r, C + "{1,7}", r.toLowerCase()), 5),
            t(C, s(r, C + "{1,7}", r.toLowerCase()), 5),
            t(c, s(r, c + "{1,7}", r.toUpperCase()), 5),
            t(C, s(r, c + "{1,7}", r.toUpperCase()), 5)
        ).forEach(d -> {
            int expected = Solution.countChars(d._1, d._2);
            String msg = String.format("countChars('%s', \"%s\") -> %d", d._1, d._2, expected);
            grading(d._3, msg, () -> Main.countChars(d._1, d._2) == expected);
        });
    }
}
```

A VPL evaluation (triggered via the `vpl_evaluate.sh` script) will generate the following console output

```
Comment :=>> JEdUnit 0.1.13
Comment :=>> 
Comment :=>> Checkstyle
Comment :=>> Main.java:11:5: Es fehlt ein Javadoc-Kommentar. [JavadocMethod]
Comment :=>> [CHECKSTYLE] Found violations (-5%)
Comment :=>> Current percentage: -5%
Grade :=>> 0
Comment :=>> 
Comment :=>> Inspect source files for suspect code
Comment :=>> Everything fine
Comment :=>> Current percentage: -5%
Grade :=>> 0
Comment :=>> 
Comment :=>> Inspect source files for coding violations
Comment :=>> Current percentage: -5%
Grade :=>> 0
Comment :=>> 
Comment :=>> [25,00%]: Provided example calls
Comment :=>> Check 1: [OK] Counting 'o' in "Hello World" must return 2. (5 points)
Comment :=>> Check 2: [OK] Counting 'w' in "Hello World" must return 1. (5 points)
Comment :=>> Result for this test: 10 of 10 points (100%)
Comment :=>> Current percentage: 20%
Grade :=>> 20
Comment :=>> 
Comment :=>> [25,00%]: Boundary testcases (unknown test cases)
Comment :=>> Check 3: [OK] countChars('x', "") -> 0 (10 points)
Comment :=>> Check 4: [OK] countChars('X', "") -> 0 (10 points)
Comment :=>> Check 5: [OK] countChars('x', "x") -> 1 (5 points)
Comment :=>> Check 6: [FAILED] countChars('X', "x") -> 1 (0 of 5 points)
Comment :=>> Check 7: [OK] countChars('x', "X") -> 1 (5 points)
Comment :=>> Check 8: [FAILED] countChars('X', "X") -> 1 (0 of 5 points)
Comment :=>> Check 9: [FAILED] countChars('X', "ax") -> 1 (0 of 5 points)
Comment :=>> Check 10: [OK] countChars('x', "Xa") -> 1 (5 points)
Comment :=>> Result for this test: 35 of 50 points (70%)
Comment :=>> Current percentage: 38%
Grade :=>> 38
Comment :=>> 
Comment :=>> [50,00%]: Randomized testcases
Comment :=>> Check 11: [OK] countChars('p', "ppppp?n§?VIV!?Hk") -> 5 (5 points)
Comment :=>> Check 12: [FAILED] countChars('P', "p§&??$%Z?b?&O?!§§?") -> 1 (0 of 5 points)
Comment :=>> Check 13: [OK] countChars('p', "?%!§TVRi§!?§PP") -> 2 (5 points)
Comment :=>> Check 14: [FAILED] countChars('P', "O$X&!§p?fyPPPPPPP") -> 8 (0 of 5 points)
Comment :=>> Check 15: [OK] countChars('p', "I&$E!ehPirr%§") -> 1 (5 points)
Comment :=>> Check 16: [FAILED] countChars('P', "???§?§!P$??!jn§") -> 1 (0 of 5 points)
Comment :=>> Check 17: [OK] countChars('p', "fng$?ppppT%§F$&") -> 4 (5 points)
Comment :=>> Check 18: [FAILED] countChars('P', "§D!j?p$F?&&$$") -> 1 (0 of 5 points)
Comment :=>> Result for this test: 20 of 40 points (50%)
Comment :=>> Current percentage: 63%
Grade :=>> 63
Comment :=>> 
Comment :=>> Finished: 63 points
```

that can be evaluated by VPL for automatic grading and commenting of student submissions.

In this example case our student would have passed

- 12 of 18 test cases,
- violated one checkstyle rule,
- and got in total 63 of 100 points.

Thats all the magic, basically.

## More features

However, there are more features that will be explained in the Wiki (TO BE DONE).
According to our experiences students tend to make use of the following kind of "cheats".

- __Overfitting__ (e.g. to map simply the test values to expected results, outside the scope of the test values the solution is useless)
- __Problem evasion__ (e.g. to solve a problem using loops instead of to solve it recursively)
- __Redirection__ (e.g. to call the reference solution instead of implement it on their own)
- __Injection__ (e.g. to write `System.out.println("Grade :=>> 100"); System.exit(0);` to get full points and prevent VPL to check the submission)

So, and in addition to "normal" unit testing frameworks JEdUnit provides several addons to handle 
educational specifics that are hardly covered by current testing frameworks.

- Checkstyle integration to foster "readable" code.
- Randomized test case generation to handle overfitting cheats.
- Parser integration and an easy to use selector model (comparable to CSS selectors for a DOM-tree).
- Selectors enable to detect the import of non allowed libraries,
- the use of non allowed programming constructs like loops, methods, lambda functions, and so on
- or the import of non allowed reflection libraries or method calls that enable "Injection" attacks or the redirection to reference solutions
