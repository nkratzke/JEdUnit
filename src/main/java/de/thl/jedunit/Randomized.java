package de.thl.jedunit;

import java.util.Random;

import com.mifmif.common.regex.Generex;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;

import io.vavr.collection.*;

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

    public static <A, B> Tuple2<A, B> t(A a, B b) {
        return Tuple.of(a, b);
    }

    public static <A, B, C> Tuple3<A, B, C> t(A a, B b, C c) {
        return Tuple.of(a, b, c);
    }

    public static <A, B, C, D> Tuple4<A, B, C, D> t(A a, B b, C c, D d) {
        return Tuple.of(a, b, c, d);
    }

    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> t(A a, B b, C c, D d, E e) {
        return Tuple.of(a, b, c, d, e);
    }

    public static <T> List<T> testWith(T... ts) {
        return List.of(ts);
    }

}