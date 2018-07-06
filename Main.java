/**
 * Main class for VPL assignments.
 * Please provide this file to your students.
 * They should start to build their solution with this file.
 * @author Nane Kratzke
 */
class Main {

    public static int countChars(char needle, String s) {
        if (s == null) return 0;
        int n = 0;
        for (char c : s.toLowerCase().toCharArray()) {
            if (Character.toLowerCase(needle) == c) n++;
        }
        return n;
    }

    public static void main(String[] args) {
        System.out.println(countChars('i', "Dies ist nur ein doofes Beispiel."));
    }
}