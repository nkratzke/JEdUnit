# Java Test Template for VPL

This is a basic test template to simplify automatic evaluation of (small) __Java__ programming assignments
using [Moodle](https://moodle.org/) and [VPL](http://vpl.dis.ulpgc.es/) for programming instructors.

Sadly, the interaction between JUnit and VPL is cumbersome in some details. In some configurations
necessary unit testing librarieres do work not frictionless with VPL. That is why we provide a template based only on the pure Java standard library.

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

1. Make use of git and __clone__ this repository via $git clone ...$ to your local machine.
2. Import the following __required file__ (Main.java) via the Moodle web interface from this repository as starting point for your students.
3. Import the following __executable files__ (Checks.java, Evaluator.java) via the Moodle web interface from this repository.
4. Select __to be keeped files__ (Checks.java, Evaluator.java) via the Moodle web interface (otherwise these files will be deleted by VPL and your evaluation will not work).

