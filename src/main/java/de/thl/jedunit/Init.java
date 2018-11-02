package de.thl.jedunit;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class Init {

    public static void main(String[] args) {
        List<String> resources = Arrays.asList(
            "style_checks.xml",
            "vpl_evaluate.sh",
            "vpl_run.sh",
            "Main.java",
            "Solution.java",
            "Checks.java"
        );

        for (String resource : resources) {
            try {
                System.out.println("Preparing " + resource);
                Scanner read = new Scanner(Init.class.getResourceAsStream("/" + resource));
                File f = new File(resource);
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                while (read.hasNextLine()) {
                    writer.write(read.nextLine() + " \n");
                }
                read.close();
                writer.close();
                if (resource.endsWith(".sh")) f.setExecutable(true);    
            } catch (Exception ex) {
                System.out.println(ex);
                System.exit(1);
            }
        }
    }
}