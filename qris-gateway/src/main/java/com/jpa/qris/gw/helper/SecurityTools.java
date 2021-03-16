/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.helper;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author Fikri
 */
public abstract class SecurityTools {

	public static void main(String[] args) {
		try {
			// [CardNo SEBELUM DEKRIP :
			// hcdoE03pql5cKO+rB8s4QOllOgKX1H8ivww0r03E0IH8EuDDMTNedg==]
			String encoded = SecurityTools.encodeAES("1234567890123456",
					"4617007700000039");
			System.out.println(encoded);
			System.out.println("gjQ6u6lCo0DDoW5q8nsWHQUBh6DN5amHLLqwkatz5VM=");
			System.out.println(SecurityTools.decodeAES("1234567890123456",
					encoded));
		} catch (Exception ex) {

		}
	}

	public static String encodeDES(String pass, String text)
			throws InvalidKeyException, UnsupportedEncodingException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {

		DESKeySpec keySpec = new DESKeySpec(pass.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		byte[] cleartext = text.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedPwd = Base64.encodeBase64(cipher.doFinal(cleartext));
		return new String(encryptedPwd, "UTF-8");
	}

	public static String decodeDES(String pass, String encoded)
			throws InvalidKeyException, UnsupportedEncodingException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {

		DESKeySpec keySpec = new DESKeySpec(pass.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		byte[] decrypedPwd = Base64.decodeBase64((encoded));
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] plainTextPwdBytes = cipher.doFinal(decrypedPwd);
		return new String(plainTextPwdBytes, "UTF-8");
	}

	public static String encodeAES(String key, String plainText)
			throws Exception {

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encryptedTextBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
		return new Base64().encodeToString(encryptedTextBytes);
	}

	public static String decodeAES(String key, String encryptedText)
			throws Exception {

		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] encryptedTextBytes = Base64.decodeBase64(encryptedText);
		byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
		return new String(decryptedTextBytes);
	}

	public static String getHashFromString(String param) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(param.getBytes());
			byte byteData[] = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			String ghash = sb.toString();
			return ghash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getHashFromStringUpper(String param) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(param.toUpperCase().getBytes());
			byte byteData[] = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			String ghash = sb.toString();
			return ghash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean compareHash(String signature, String hash) {
		return signature.equals(hash);
	}

}
