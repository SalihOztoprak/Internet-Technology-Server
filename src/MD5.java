import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

    public MD5() {
        //Left blank
    }

    public static byte[] getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}