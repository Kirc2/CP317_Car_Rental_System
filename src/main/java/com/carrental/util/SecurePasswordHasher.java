package main.java.com.carrental.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurePasswordHasher {

	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
	private static final int ITERATIONS = 66536;
	private static final int KEY_LENGTH = 512;
	private static final int SALT_LENGTH = 16;
	
	
	/**
	 * Hashes a password with a unique salt and returns a string for storage
	 * Format: salt (Base64):hash (Base64):iterations:keylength
	 * @param password the password to hash
	 * @return the hashed password
	 */
	public static String hashPassword(String password) {
		
		SecureRandom secureRandom = new SecureRandom();
		byte[] salt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(salt);
		
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
		String saltBase64 = null;
		String hashBase64 = null;
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			byte hash[] = factory.generateSecret(spec).getEncoded();
			 saltBase64 = Base64.getEncoder().encodeToString(salt);
			 hashBase64 = Base64.getEncoder().encodeToString(hash);
			
		} catch (NoSuchAlgorithmException e) {
			System.err.print("No algorithm available for password");
		} catch (InvalidKeySpecException e) {
			System.err.print("Invalid key SPEC");
		}
		return String.format("%s:%s:%d:%d", saltBase64, hashBase64, ITERATIONS, KEY_LENGTH);
	}
	
	/**
	 * Verifies an input password against a stored hash string
	 * @param inputPassword the password provided by the user
	 * @param storedHashData The stored string (salt:hash:iterations:keyLength)
	 * @return true if the password matches, false otherwise.
	 */
    public static boolean verifyPassword(String inputPassword, String storedHashData) {

    	String[] parts = storedHashData.split(":");
        String saltBase64 = parts[0];
        String storedHashBase64 = parts[1];
        int iterations = Integer.parseInt(parts[2]);
        int keyLength = Integer.parseInt(parts[3]);
 
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] storedHash = Base64.getDecoder().decode(storedHashBase64);
 
        KeySpec spec = new PBEKeySpec(
            inputPassword.toCharArray(), 
            salt, 
            iterations, 
            keyLength
        );
        
        byte[] computedHash = null;
        SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance(ALGORITHM);
	        computedHash = factory.generateSecret(spec).getEncoded();

		} catch (NoSuchAlgorithmException e) {
			System.err.print("No algorithm available for password");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			System.err.print("Invalid key SPEC");
			e.printStackTrace();
		}
 
        return MessageDigest.isEqual(computedHash, storedHash);
    }

	
}
