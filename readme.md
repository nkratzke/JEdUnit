# Java Test Template for VPL

This is a basic test template to simplify automatic evaluation of (small) __Java__ programming assignments
using [Moodle](https://moodle.org/) and [VPL](http://vpl.dis.ulpgc.es/) for programming instructors.

Sadly, in some configurations
necessary unit testing librarieres do work not frictionless with VPLs Jail server concept although they would be more than useful for
automatic grading. That is why we provide a template that make use of the pure Java standard library only.
There is no need to modify classpaths or to import additional libraries and JAR files.

## Intended audience

This VPL template is written for programming instructors and teachers at schools, colleges, universities or further
programming training facilities that want to automatically evaluate and grade (small)
Java programming assignments typically at a "freshman" (1st/2nd semester) level.

It might be not feasible for larger and more complex programming assignents.

## Usage

We assume the reader to be familiar with Java in general.
Furthermore, we recommend to study at least the following VPL related documentation:

- [VPL General documentation](http://vpl.dis.ulpgc.es/index.php/support)
- [Moodle VPL Tutorials](http://www.science.smith.edu/dftwiki/index.php/Moodle_VPL_Tutorials) from Smith College

### Basic configuration

To make use of this template the following workflow is recommended to set up a basic configuration for a new programming assignment.
First of all, clone this repository to your local machine.
```
git clone https://github.com/nkratzke/VPL-java-template.git
```

Then

1. Import the following __requested files__ ([Main.java](Main.java)) via the Moodle web interface from this repository as __starting point for your students__.
2. Import the following __execution files__ ([Checks.java](Checks.java), [Evaluator.java](Evaluator.java) and [vpl_evaluate.sh](vpl_evaluate.sh), [vpl_run.sh](vpl_run.sh)) via the Moodle web interface from this repository. 
    - [Checks.java](Checks.java) is where you place your grading test cases. 
    - [Evaluator.java](Evaluator.java) is a helper class needed for the execution of your [Checks](Checks.java).
    - [vpl_run.sh](vpl_run.sh) is the VPL run script that compiles and executes the [Main.java](Main.java). It must be adapted for assignments that need more than one source file.
    - [vpl_evaluate.sh](vpl_evaluate.sh) is the VPL evaluate script that compiles and executes all Java files and launches [Checks.java](Checks.java) to evaluate the student submission. It might be adapted for assignments that rely on more complex dependencies.
3. Select __to be keeped files__ ([Checks.java](Checks.java), [Evaluator.java](Evaluator.java)) via the Moodle web interface (otherwise these files will be deleted by VPL and your evaluation will not work).

### Writing an assignment

Let us assume a small assignment example. Students have to develop a method to count all occurences of a given char _c_ in a String _s_ (non case sensitive). This method should be called like this:

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

This class will be extended and uploaded via Moodle. A possible solution might look like that:

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

- to be done