package socialnetwork.domain;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;


public class Password
{

    private static final int iterations=1000;
    public static final int saltLen=32;
    public static final int desiredKeyLen=100;

    public static String getSaltedHash(String password) throws Exception
    {
        byte[] salt= SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
        return Base64.getEncoder().encodeToString(salt) + "$" + hash(password,salt);
    }

    private static String hash(String password, byte[] salt) throws Exception {
        if (password == null || password.length() <=5)
            throw new IllegalArgumentException("The password should have at least 5 characters!");
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static boolean check(String password, String stored) throws Exception
    {
        String[] saltAndHash = stored.split("\\$");
        if (saltAndHash.length != 2) {
            throw new IllegalStateException(
                    "The stored password must have the form 'salt$hash'");
        }
        String hashOfInput = hash(password, Base64.getDecoder().decode(saltAndHash[0]));
        return hashOfInput.equals(saltAndHash[1]);
    }



}
