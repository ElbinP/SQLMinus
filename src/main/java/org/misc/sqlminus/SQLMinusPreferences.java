package org.misc.sqlminus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SQLMinusPreferences {

	private Preferences preferences = Preferences.userNodeForPackage(SQLMinus.class);
	private final Optional<String> passPhrase;

	public SQLMinusPreferences() {
		Optional<String> passPhraseFromFile = getPassphrase();
		if (passPhraseFromFile.isEmpty()) {
			passPhrase = generatePassphrase();
		} else {
			passPhrase = Optional.of(passPhraseFromFile.get());
		}
	}

	public String get(String key, String def) {
		return preferences.get(key, def);
	}

	public int getInt(String key, int def) {
		return preferences.getInt(key, def);
	}

	public void put(String key, String value) {
		preferences.put(key, value);
	}

	public void putInt(String key, int value) {
		preferences.putInt(key, value);
	}

	public void putEncryptedValue(String key, String value) {
		if (passPhrase.isPresent()) {
			try {
				// Generate random salt and IV
				SecureRandom random = new SecureRandom();
				byte[] salt = new byte[16];
				byte[] iv = new byte[16];
				random.nextBytes(salt);
				random.nextBytes(iv);

				SecretKey secretKey = getSecretKey(passPhrase.get().toCharArray(), salt);

				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
				byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));

				// Concatenate salt + iv + ciphertext
				byte[] combined = new byte[salt.length + iv.length + encrypted.length];
				System.arraycopy(salt, 0, combined, 0, salt.length);
				System.arraycopy(iv, 0, combined, salt.length, iv.length);
				System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);

				String encryptedValue = Base64.getEncoder().encodeToString(combined);
				preferences.put(key, encryptedValue);
			} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| UnsupportedEncodingException e) {
				System.err.println(e.getMessage());
			}
		} else {
			System.err.println("no passphrase available to encrypte");
		}
	}

	public String getDecryptedValue(String key, String def) {

		String decryptedValue;
		if (passPhrase.isPresent()) {
			try {
				if (!(Stream.of(preferences.keys()).anyMatch(k -> k.equals(key)))) {
					// Return default value if this key does not exist in preferences
					decryptedValue = def;
				} else {
					String encryptedText = preferences.get(key, "");
					byte[] allBytes = Base64.getDecoder().decode(encryptedText);

					// Extract salt, iv, and ciphertext
					byte[] salt = new byte[16];
					byte[] iv = new byte[16];
					byte[] cipherBytes = new byte[allBytes.length - 32];
					System.arraycopy(allBytes, 0, salt, 0, 16);
					System.arraycopy(allBytes, 16, iv, 0, 16);
					System.arraycopy(allBytes, 32, cipherBytes, 0, cipherBytes.length);

					SecretKey secretKey = getSecretKey(passPhrase.get().toCharArray(), salt);

					Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
					cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
					byte[] decrypted = cipher.doFinal(cipherBytes);

					decryptedValue = new String(decrypted, "UTF-8");
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| UnsupportedEncodingException | BackingStoreException e) {
				System.err.println(e.getMessage());
				// return default value if we are not able to decrypt from preferences
				decryptedValue = def;
			}
		} else {
			System.err.println("no passphrase available to decrypt, returning default value");
			decryptedValue = def;
		}

		return decryptedValue;
	}

	private SecretKey getSecretKey(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 128); // 128-bit AES
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

	private Optional<String> getPassphrase() {
		Optional<String> aesKey = Optional.empty();
		if (Files.exists(Paths.get(Constants.PREFERENCES_SECRET_KEY_FILE))) {
			try {
				aesKey = Optional
						.of(Files.readString(Paths.get(Constants.PREFERENCES_SECRET_KEY_FILE), StandardCharsets.UTF_8));
			} catch (IOException e) {
			}
		}

		return aesKey;
	}

	private Optional<String> generatePassphrase() {
		Optional<String> generatedPassPhrase;
		try {
			if (Files.exists(Paths.get(Constants.PREFERENCES_SECRET_KEY_FILE))) {
				throw new IllegalStateException(
						"Unable to create " + Constants.PREFERENCES_SECRET_KEY_FILE + ", file already exists");
			}

			String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()<>?:{};";
			SecureRandom random = new SecureRandom();

			int lengthOfKey = 25;
			StringBuilder sb = new StringBuilder(lengthOfKey);
			for (int i = 0; i < lengthOfKey; i++) {
				int index = random.nextInt(CHARACTERS.length());
				sb.append(CHARACTERS.charAt(index));
			}
			generatedPassPhrase = Optional.of(sb.toString());

			Files.writeString(Paths.get(Constants.PREFERENCES_SECRET_KEY_FILE), generatedPassPhrase.get(),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			generatedPassPhrase = Optional.empty();
		}

		return generatedPassPhrase;
	}

}
