package de.thl.jedunit;

import java.util.Random;
import com.mifmif.common.regex.Generex;

public class Randomized {

    /**
     * Random generator.
     */
    private static final Random RANDOM = new Random();

    /**
     * Regular expression for default characters.
     */
    private static final String DEFAULT_CHARS = "[a-zA-Z]";

    /**
     * Generates a String concatenated from random Strings where each String is generated from a regular expression.
     * @param regexps Build patterns (regular expressions) for String generation
     * @return concatenated random String
     */
    public static String s(String... regexps) {
        String r = "";
        for (String regex : regexps) {
            Generex g = new Generex(regex);
            r += g.random();
        }
        return r;
    }

    /**
     * Generates a random String between a specified length.
     * @param min minimum length
     * @param max maximum length
     * @return random String of length min <= length <= max
     */
    public static String s(int min, int max) {
        return s(String.format(DEFAULT_CHARS + "{%d,%d}", min, max));
    }

    public static boolean b() {
        return RANDOM.nextBoolean();
    }

    public static int i() {
        return RANDOM.nextInt();
    }

    public static int i(int max) {
        return RANDOM.nextInt(max);
    }

    public static int i(int min, int max) {
        return min + i(max - min);
    }

    public static double d() {
        return RANDOM.nextDouble();
    }

    public static double d(double max) {
        return RANDOM.nextDouble() * max;
    }

    public static double d(double min, double max) {
        return min + d(max - min);
    }

    public static char c(String regexp) {
        return s(regexp).charAt(0);
    }

    public static char c() {
        return s(DEFAULT_CHARS).charAt(0);
    }

}