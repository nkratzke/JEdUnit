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
curl https://raw.githubusercontent.com/nkratzke/VPL-java-template/working/init.sh | sh
```

Then

1. Import the following __requested files__ `Main.java` via the Moodle web interface from this repository as __starting point for your students__.
2. Import the following __execution files__ `Checks.java`, `Solution.java`, `style_checks.xml`, `vpl_evaluate.sh`, and `vpl_run.sh` via the Moodle web interface from this directory.
    - `Solution.java` is where you should place your reference solution.
    - `Checks.java` is where you place your grading test cases.
    - _`style_checks.xml` is a [checkstyle](http://checkstyle.sourceforge.net/index.html) file that is used to enforce coding standards. In most cases you do not need to touch this file._
    - _`vpl_run.sh` is the VPL run script that compiles and executes the `Main.java`. In most cases you do not need to touch this file._
    - _`vpl_evaluate.sh` is the VPL evaluate script that checks style, and compiles all Java files. Furthermore, it launches `Checks.java` to evaluate the student submission. In most cases you do not need to touch this file._
3. Select __to be keeped files__ `Checks.java`, `Solution.java` via the Moodle web interface (otherwise these files will be deleted by the VPL jail server and your evaluation will not work).

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

To write checks one simply has to take the [Checks](Checks.java) class as a template

```Java
/**
 * Please add your test cases for evaluation here.
 * - Please provide meaningful remarks for your students in grading() calls.
 * - All grading() calls should sum up to 100.
 * - If you give more than 100 points it will be truncated. 
 * - This enable bonus rules (e.g. giving 120 points instead of 100) to tolerate some errors worth 20 points. 
 * - All methods that start with "test" will be executed automatically.
 * - If this sounds similar to unit testing - this is intended ;-)
 */
class Checks extends Constraints {

    @Check
    public void testSubmissions() {
        // Please add your checks and grading points here
        grading(10, "This will always fail.", () -> "hello".equals("Hello"));
    }
}
```

and extend it with assignment specific checks and grading points. This could be done like this:

```Java
class Checks extends Constraints {

    @Check
    public void testExampleCases() {
        // You can give less points for provided example cases
        grading(5, "Counting 'o' in 'Hello World' must return 2.", () -> Main.countChars('o', "Hello World") == 2);
        grading(5, "Counting 'w' in 'Hello World' must return 2.", () -> Main.countChars('w', "Hello World") == 1);
    }

    @Check
    public void testAdditionalCases() {
        // You can give more points for additional checks
        grading(10, "Counting 'x' in 'xxx' must return 3.", () -> Main.countChars('x', "xxx") == 3);
        grading(10, "Counting 'X' in 'XxX' must return 3.", () -> Main.countChars('X', "XxX") == 3);
        grading(10, "Counting 'x' in 'YYX' must return 1.", () -> Main.countChars('x', "YYX") == 1);
        grading(10, "Counting 'X' in 'Xyy' must return 1.", () -> Main.countChars('X', "Xyy") == 1);
        grading(10, "Counting 'x' in 'Xyy' must return 1.", () -> Main.countChars('x', "Xyy") == 1);

        // You can give less points for less problematic checks
        grading(5, "Counting 'x' in 'X' must return 1.", () -> Main.countChars('x', "X") == 1);
        grading(5, "Counting 'y' in 'X' must return 0.", () -> Main.countChars('y', "X") == 0);
        grading(5, "Counting 'Y' in 'X' must return 0.", () -> Main.countChars('Y', "X") == 0);
    }

    @Check
    public void testBoundaryCases() {
        // You can give more points for problem sensibilizing checks
        grading(15, "Counting chars in an empty string must return 0.", () -> Main.countChars('x', "") == 0);
        grading(15, "Counting chars in a null string must return 0.", () -> Main.countChars('x', null) == 0);

        // And a little bit less points for just potential problematic checks
        String example = "Just  an example! ";
        grading(10, "Counting ' ' in '" + example + "'", () -> Main.countChars(' ', example) == 4);
    }
}
```

As you see, the `grading()` command is essential here. It takes the following parameters:

- __points__ (`int`) that will be added if the check is successfull (evaluates to `true`)
- __remark__ (`String`) that will be printed in comments. A remark should describe the testcase in a meaningful but short way.
- __check__ (`Supplier<Boolean>`) that will be executed. If evaluated to `true` points will be added, otherwise no points will be added.

A VPL evaluation (triggered via the `vpl_evaluate.sh` script) will generate the following console output

```
Comment :=>> Check 1: Counting 'o' in 'Hello World' must return 2. [OK] (5 points)
Comment :=>> Check 2: Counting 'w' in 'Hello World' must return 2. [OK] (5 points)
Comment :=>> Check 3: Counting 'x' in 'xxx' must return 3. [OK] (10 points)
Comment :=>> Check 4: Counting 'X' in 'XxX' must return 3. [FAILED] (0 of 10 points)
Comment :=>> Check 5: Counting 'x' in 'YYX' must return 1. [OK] (10 points)
Comment :=>> Check 6: Counting 'X' in 'Xyy' must return 1. [FAILED] (0 of 10 points)
Comment :=>> Check 7: Counting 'x' in 'Xyy' must return 1. [OK] (10 points)
Comment :=>> Check 8: Counting 'x' in 'X' must return 1. [OK] (5 points)
Comment :=>> Check 9: Counting 'y' in 'X' must return 0. [OK] (5 points)
Comment :=>> Check 10: Counting 'Y' in 'X' must return 0. [OK] (5 points)
Comment :=>> Check 11: Counting chars in an empty string must return 0. [OK] (15 points)
Comment :=>> Check 12: Counting chars in a null string must return 0. [FAILED due to java.lang.NullPointerException] (0 of 15 points)
Comment :=>> Check 13: Counting ' ' in 'Just  an example! ' [OK] (10 points)
Grade :=>> 80
```

that can be evaluated by VPL for automatic grading and commenting of student submissions.

In this example case our student would have passed

- 10 of 13 test cases and
- and got 80 of 100 points.

Thats all the magic, basically.

## More features

However, there are more features that will be explained in the Wiki (TO BE DONE).
According to our experiences students tend to make use of the following kind of "cheats".

- __Overfitting__ (e.g. to map simply the test values to expected results, outside the scope of the test values the solution is useless)
- __Problem evasion__ (e.g. to solve a problem using loops instead of to solve it recursively)
- __Redirection__ (e.g. to call the reference solution instead of implement it on their own)
- __Injection__ (e.g. to write `System.out.println("Grade :=>> 100"); System.exit(0);` to get full points and prevent VPL to check the submission)

So, and in addition to "normal" unit testing frameworks JEdUnit provides several addons to handle these
special kinds of educational specifics that are hardly covered by current testing frameworks.

- Checkstyle integration to foster "readable" code.
- Parser integration and an easy to use selector model (comparable to CSS selectors for a DOM-tree) that enables to formulate
- checks to detect the import of non allowed libraries,
- checks to detect the use of non allowed programming constructs like loops, methods, lambda functions, and so on
- checks to detect the import of non allowed reflection libraries or method calls that enable "Injection" attacks or the redirection to reference solutions
