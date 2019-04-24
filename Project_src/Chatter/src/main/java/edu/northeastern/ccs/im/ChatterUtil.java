package edu.northeastern.ccs.im;

public class ChatterUtil {
    public static boolean validateUsernameAndPassword(String password) {
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (!Character.isLetter(ch) && !Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }
}
