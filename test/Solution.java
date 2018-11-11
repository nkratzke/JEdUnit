/**
 * You should place your reference solution here.
 * Do not forget to provide meaningful Javadoc 
 * comments!!!
 */
public class Solution {

    public static int countChars(char c, String s) {
        int n = 0;
        for (char x : s.toLowerCase().toCharArray()) {
            if (x == Character.toLowerCase(c)) n++;
        }
        return n;
    }

}
