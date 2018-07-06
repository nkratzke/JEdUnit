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

1. Make use of git and __clone__ this repository to your local machine.
```
git clone https://github.com/nkratzke/VPL-java-template.git
```
2. Import the following __required file__ ([Main.java](Main.java)) via the Moodle web interface from this repository as __starting point for your students__.
3. Import the following __executable files__ ([Checks.java](Checks.java), [Evaluator.java](Evaluator.java) and [vpl_evaluate.sh](vpl_evaluate.sh), [vpl_run.sh](vpl_run.sh)) via the Moodle web interface from this repository. 
    - [Checks.java](Checks.java) is where you place your grading test cases. 
    - [Evaluator.java](Evaluator.java) is a helper class needed for the execution of your [Checks](Checks.java).
    - [vpl_run.sh](vpl_run.sh) is the VPL run script that compiles and executes the [Main.java](Main.java). It must be adapted for assignments that need more than one source file.
    - [vpl_evaluate.sh](vpl_evaluate.sh) is the VPL evaluate script that compiles and executes all Java files and launches [Checks.java](Checks.java) to evaluate the student submission. It might be adapted for assignments that rely on more complex dependencies.
4. Select __to be keeped files__ ([Checks.java](Checks.java), [Evaluator.java](Evaluator.java)) via the Moodle web interface (otherwise these files will be deleted by VPL and your evaluation will not work).

