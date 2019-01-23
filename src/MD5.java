import java.security.MessageDigest;

/**
 * This class is used to generate a hashed text from the input
 */
public class MD5 {

    public MD5() {
        //Left blank
    }

    /**
     * This method is used to return a hashed byte array from a string
     *
     * @param input The string you want to hash
     * @return The hashed string as a byte array
     */
    public static byte[] getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (Exception e) {
            System.out.println("User logged out without entering his name");
        }
        return null;
    }
}