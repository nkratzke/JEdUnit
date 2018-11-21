package de.thl.jedunit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class CLI {

    public static final String[] RESOURCES = {
        "style_checks.xml",
        "vpl_evaluate.sh",
        "vpl_run.sh",
        "clean.sh",
        "Main.java.template",
        "Solution.java.template",
        "Checks.java.template"
    };

    public static void main(String[] args) {
        for (String resource : RESOURCES) {
            try {
                System.out.println("Preparing " + resource.replace(".template", ""));
                Scanner read = new Scanner(CLI.class.getResourceAsStream("/" + resource));
                File f = new File(System.getProperty("user.dir") + File.separator + resource.replace(".template", ""));
                if (f.exists()) continue;
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                while (read.hasNextLine()) {
                    writer.write(read.nextLine() + "\n");
                }
                read.close();
                writer.close();
                if (resource.endsWith(".sh")) f.setExecutable(true, false);    
            } catch (Exception ex) {
                System.out.println(ex);
                System.exit(1);
            }
        }
    }
}