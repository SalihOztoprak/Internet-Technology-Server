import java.security.MessageDigest;

public class MD5 {

    public MD5() {
        //Left blank
    }

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