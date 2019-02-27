package de.thl.jedunit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provides all config options for JEdUnit that can be set in
 * the `configure()` method of a `Checks` class.
 * @author Nane Kratzke
 */
public class Config {

    /**
     * Version of JEdUnit (Semantic Versioning).
     */
    public final static String VERSION = "0.2.2"; 

    /**
     * Set of file names that shall be considered by checkstyle and evaluation.
     * This set is determined automatically but can be overwritten in the configure method().
     */
    public static Set<String> EVALUATED_FILES = DSL.autoFiles();

    /**
     * Std-out redirection of submission code.
     * This is used to avoid injection attacks. 
     */
    public static String STD_OUT_REDIRECTION = "console.log";

    /**
     * Option to evaluate the checkstyle log.
     */
    public static boolean CHECKSTYLE = true;

    /**
     * Downgrade for every found checkstyle error.
     */
    public static int CHECKSTYLE_PENALTY = 5;

    /**
     * List of Checkstyle checks that are ignored for evaluation.
     * This list can be adapted in the configure method().
     */
    public static List<String> CHECKSTYLE_IGNORES = new LinkedList<String>(); 
    static { 
        CHECKSTYLE_IGNORES.addAll(Arrays.asList("[Javadoc", "Javadoc]",
            "[NewlineAtEndOfFile]", "[HideUtilityClassConstructor]", "[FinalParameters]",
            "[AvoidInlineConditionals]", "[RegexpSingleline]", 
            "[NeedBraces]", "[MagicNumber]", "[RedundantModifier]", "[VisibilityModifier]",
            "[DesignForExtension]"
        ));
    }

    /**
     * The convention is to check imports (whitelist of libraries).
     */
    public static boolean CHECK_IMPORTS = true;

    /**
     * Option to set the allowed set of libraries.
     */
    public static List<String> ALLOWED_IMPORTS = Arrays.asList("java.util");    

    /**
     * Option to set the penalty for importing not allowed libraries.
     */
    public static int IMPORT_PENALTY = 25;

    /**
     * The following imports are never allowed, because they could be used
     * to do arbitrary harm (like to mask point injection attacks).
     */
    public static List<String> CHEAT_IMPORTS = Arrays.asList("java.lang.reflect", "java.lang.invoke");

    /**
     * Option to allow loops (for, while, do while, forEach()).
     */
    public static boolean ALLOW_LOOPS = true;

    /**
     * Option to penalize the use of loops;
     */
    public static int LOOP_PENALTY = 100;

    /**
     * Option to allow make use of methods.
     */
    public static boolean ALLOW_METHODS = true;

    /**
     * Option to penalize the use of methods.
     */
    public static int METHOD_PENALTY = 100;

    /**
     * Option to allow lambda functions (x -&gt; x + 1).
     */
    public static boolean ALLOW_LAMBDAS = true;

    /**
     * Option to penalize the use of lambda functions.
     */
    public static int LAMBDA_PENALITY = 25;

    /**
     * Option to allow inner classes. Not allowed by default.
     */
    public static boolean ALLOW_INNER_CLASSES = false;

    /**
     * Option to penalize the use of inner classes.
     */
    public static int INNER_CLASS_PENALTY = 100;

    /**
     * Option to allow non final static datafields. Not allowed by default.
     */
    public static boolean ALLOW_DATAFIELDS = false;

    /**
     * Option to penalize the use of non final static datafields.
     */
    public static int DATAFIELD_PENALTY = 25;

    /**
     * Option to check for proper use of collection interfaces Map, Set, List.
     * Map, Set, List must be used for method parameters and return types by default.
     */
    public static boolean CHECK_COLLECTION_INTERFACES = true;

    /**
     * Option to penalize improper use of collection interfaces.
     */
    public static int COLLECTION_INTERFACE_PENALTY = 25;

    /**
     * Option to check that `System.out.println()` statements occur only in the `main()` method.
     */
    public static boolean ALLOW_CONSOLE_OUTPUT = false;

    /**
     * Option to penalize the use of `System.out.println()` outside the `main()` method.
     */
    public static int CONSOLE_OUTPUT_PENALTY = 25;

}