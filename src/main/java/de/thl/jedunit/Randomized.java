package de.thl.jedunit;

import java.util.Random;

import com.mifmif.common.regex.Generex;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;

/**
 * This class provides helper methods for randomized test cases.
 * 
 * @author Nane Kratzke
 */
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
     * Generates a string concatenated from regular expression generated random strings.
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
     * Generates a random string in a specified length range.
     * The string is composed of [a-z] and [A-Z] characters.
     * @param min minimum length
     * @param max maximum length
     * @return random String of length between min and max (inclusive)
     */
    public static String s(int min, int max) {
        return s(String.format(DEFAULT_CHARS + "{%d,%d}", min, max));
    }

    /**
     * Generates a random boolean value.
     * @return true or false
     */
    public static boolean b() {
        return RANDOM.nextBoolean();
    }

    /**
     * Generates a random integer value.
     * @return random value
     */
    public static int i() {
        return RANDOM.nextInt();
    }

    /**
     * Generates a random integer value between 0 and an upper bound.
     * @param max upper bound
     * @return random value in [0, max[
     */
    public static int i(int max) {
        return RANDOM.nextInt(max);
    }

    /**
     * Generates a random integer value between a lower and an upper bound.
     * @param min lower bound
     * @param max upper bound
     * @return random value in [min, max[
     */
    public static int i(int min, int max) {
        return min + i(max - min);
    }

    /**
     * Generates a random double value. 
     * @return random double value in [0.0, 1.0[
     */
    public static double d() {
        return RANDOM.nextDouble();
    }

    /**
     * Generates a random double value between 0 and an upper bound.
     * @param max upper bound
     * @return random value in [0.0, max[
     */
    public static double d(double max) {
        return RANDOM.nextDouble() * max;
    }

    /**
     * Generates a random double value between a lower and an upper bound.
     * @param min lower bound
     * @param max upper bound
     * @return random value in [min, max[
     */
    public static double d(double min, double max) {
        return min + d(max - min);
    }

    /**
     * Generates a random char from a regular expression.
     * @param regexp Regular expression to generate a String
     * @return first char of the randomly generated String
     */
    public static char c(String regexp) {
        return s(regexp).charAt(0);
    }

    /**
     * Generates a random char.
     * @return random char in [a-z] or [A-Z]
     */
    public static char c() {
        return s(DEFAULT_CHARS).charAt(0);
    }

    /**
     * Generates a tuple from two values.
     * @return two-tuple
     */
    public static <A, B> Tuple2<A, B> t(A a, B b) {
        return Tuple.of(a, b);
    }

    /**
     * Generates a tuple from three values.
     * @return tripple
     */
    public static <A, B, C> Tuple3<A, B, C> t(A a, B b, C c) {
        return Tuple.of(a, b, c);
    }

    /**
     * Generates a tuple from four values.
     * @return four-tuple
     */
    public static <A, B, C, D> Tuple4<A, B, C, D> t(A a, B b, C c, D d) {
        return Tuple.of(a, b, c, d);
    }

    /**
     * Generates a tuple from five values.
     * @return five-tuple
     */
    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> t(A a, B b, C c, D d, E e) {
        return Tuple.of(a, b, c, d, e);
    }

    /**
     * Generates a tuple from six values.
     * @return six-tuple
     */
    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> t(A a, B b, C c, D d, E e, F f) {
        return Tuple.of(a, b, c, d, e, f);
    }

    /**
     * Generates a tuple from seven values.
     * @return seven-tuple
     */    
    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> t(A a, B b, C c, D d, E e, F f, G g) {
        return Tuple.of(a, b, c, d, e, f, g);
    }

    /**
     * Generates a tuple from eight values.
     * @return eight-tuple
     */    
    public static <A, B, C, D, E, F, G, H> Tuple8<A, B, C, D, E, F, G, H> t(A a, B b, C c, D d, E e, F f, G g, H h) {
        return Tuple.of(a, b, c, d, e, f, g, h);
    }

    public static <T> List<T> testWith(T... ts) {
        return List.of(ts);
    }

}