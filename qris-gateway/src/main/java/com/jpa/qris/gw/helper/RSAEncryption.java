package com.jpa.qris.gw.helper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RSAEncryption {

	private PrivateKey privKey;
	private PublicKey pubKey;

	public RSAEncryption(String privateKeyPath) throws Exception {
		setPrivKey(getPrivate(privateKeyPath));
	}

	public RSAEncryption(String privateKeyPath, String publicKeyPath)
			throws Exception {
		setPrivKey(getPrivate(privateKeyPath));
		setPubKey(getPublic(publicKeyPath));
	}

	public String processEncryption(HashMap<String, String> map)
			throws Exception {
		if (map.get("mode").equalsIgnoreCase("ENCRYPT")) {
			return encrypt(map.get("message"));
		} else if (map.get("mode").equalsIgnoreCase("DECRYPT")) {
			return decrypt(map.get("message"));
		} else {
			return "UNKNOWN_ENCRYPTION_MODE";
		}
	}

	public String encrypt(String text) throws Exception {
		byte[] cipherText = null;
		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, getPubKey());
		byte[] textB = cipher.doFinal(text.getBytes("UTF-8"));
		cipherText = Base64.encodeBase64(textB);
		return new String(cipherText);
	}

	public String decrypt(String text) throws Exception {
		byte[] decryptedText = null;
		final Cipher cipher = Cipher.getInstance("RSA");
		byte decrypedPwd[] = Base64.decodeBase64(text);
		cipher.init(Cipher.DECRYPT_MODE, getPrivKey());
		decryptedText = cipher.doFinal(decrypedPwd);
		return new String(decryptedText);
	}

	public static PrivateKey getPrivate(String filename) throws Exception {

		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	public static PublicKey getPublic(String filename) throws Exception {

		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	/**
	 * Test the EncryptionUtil
	 */
	public static void main(String[] args) {

		try {

			RSAEncryption rsaen = new RSAEncryption(
					"/Volumes/Data/private_key.der",
					"/Volumes/Data/mobile_public_dev.der");

			final String originalText = "merchant1!";

			// final PublicKey publicKey = getPublic("/Volumes/Data/private_key.der");

			 String cipherText = rsaen.encrypt(originalText);

			 System.out.println("Original: " + originalText);

			 System.out.println("Encrypted: " + new String(cipherText));

			//final PrivateKey privateKey = getPrivate(PRIVATE_KEY_FILE);

			//final String plainText = rsaen
			//		.decrypt("azwTzGYMJXuIK+YRsAaed3gWT23rjuZGLAKpXdi9NjHgWb5rOvqznxXYE7nRRi6DBrxzlo9hzqodowotwF5zeMwfto+oHlv8b2rl3oFjluR+6QNZcz92WMlHbkmJLfvag+IoO+zkQPit4vbLMmoUWTuAdIT+m4W8lHg/f0o8inI=");

			//System.out.println("Decrypted: " + plainText);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PrivateKey getPrivKey() {
		return privKey;
	}

	public void setPrivKey(PrivateKey privKey) {
		this.privKey = privKey;
	}

	public PublicKey getPubKey() {
		return pubKey;
	}

	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}

}
