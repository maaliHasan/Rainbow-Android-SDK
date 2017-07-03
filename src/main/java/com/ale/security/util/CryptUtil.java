/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : CryptUtil.java
 * Summary : 
 ******************************************************************************
 * History
 * 2013/06/11 cebruckn crms00441038 [OXO OBS]user password to be better protected in source code
 */
package com.ale.security.util;

import com.ale.util.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to crypt string.
 * 
 */
public final class CryptUtil
{
	private static final String LOG_TAG = "Cryptil";
	
	private static final int KEYLEN_BITS = 128; // see notes below where this is used.
	private static final int ITERATIONS = 473;
	private static final int MAX_FILE_BUF = 1024;
	
	private static Cipher m_encodeCipher;
	private static Cipher m_decodeCipher;
	
	/**
	 * Instantiating utility classes does not make sense. Hence the constructors should either be
	 * private or (if you want to allow subclassing) protected. A common mistake is forgetting to
	 * hide the default constructor.
	 */
	private CryptUtil()
	{
		throw new UnsupportedOperationException();
	}
	
	public static void initWithKeyPhrase(String newKeyPhrase)
	{
		try
		{
			SecretKeyFactory factory;
			
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] salt = digest.digest(newKeyPhrase.getBytes());
			
			KeySpec spec = new PBEKeySpec(newKeyPhrase.toCharArray(), salt, ITERATIONS, KEYLEN_BITS);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			initEncoder(secretKey);
			initDecoder(secretKey);
		}
		catch (Exception e)
		{
			Log.getLogger().error(LOG_TAG, "Failed to initialize cryptutil : ", e);
		}
	}
	
	/**
	 * Construct a new encoder.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	private static void initEncoder(SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		
		m_encodeCipher = cipher;
	}
	
	/**
	 * Constuct a new decoder.
	 * 
	 * @param secretKey
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	private static void initDecoder(SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		
		m_decodeCipher = cipher;
	}
	
	/**
	 * Encode a string
	 * 
	 * @param value
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidParameterSpecException
	 */
	public static String encode(String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException
	{
		
		if (value == null)
		{
			return null;
		}
		
		String crypted = fromByte(m_encodeCipher.doFinal(value.getBytes()));
		return crypted;
	}
	
	/**
	 * Encode a string
	 * 
	 * @param value
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static String decode(String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		
		if (value == null)
		{
			return null;
		}
		
		String crypted = new String(m_decodeCipher.doFinal(fromString(value)));
		return crypted;
	}
	
	/**
	 * Encode a byte array in a string.
	 * 
	 * @param bytes
	 * @return
	 */
	private static byte[] fromString(String value)
	{
		
		int len = value.length() / 2;
		byte[] bytes = new byte[len];
		
		for (int i = 0; i < len; i++)
		{
			String part = value.substring(i * 2, i * 2 + 2);
			int intVal = Integer.parseInt(part, 16);
			bytes[i] = (byte) (intVal & 0xff);
		}
		
		return bytes;
	}
	
	/**
	 * Encode a byte array in a string.
	 * 
	 * @param bytes
	 * @return
	 */
	private static String fromByte(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		
		for (byte b : bytes)
		{
			String value = String.format("%02x", b & 0xff);
			sb.append(value);
		}
		
		return sb.toString();
	}
	
	/**
	 * This is where we write out the actual encrypted data to disk using the Cipher created in
	 * setupEncrypt(). Pass two file objects representing the actual input (cleartext) and output
	 * file to be encrypted.
	 * 
	 * there may be a way to write a cleartext header to the encrypted file containing the salt, but
	 * I ran into uncertain problems with that.
	 * 
	 * @param input
	 *            - the cleartext file to be encrypted
	 * @param output
	 *            - the encrypted data file
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void writeEncryptedFile(File input, File output) throws IOException, IllegalBlockSizeException, BadPaddingException
	{
		FileInputStream fin;
		FileOutputStream fout;
		int nread = 0;
		byte[] inbuf = new byte[MAX_FILE_BUF];
		
		fout = new FileOutputStream(output);
		fin = new FileInputStream(input);
		
		while ((nread = fin.read(inbuf)) > 0)
		{
			// create a buffer to write with the exact number of bytes read. Otherwise a short read
			// fills inbuf with 0x0
			// and results in full blocks of MAX_FILE_BUF being written.
			byte[] trimbuf = new byte[nread];
			for (int i = 0; i < nread; i++)
				trimbuf[i] = inbuf[i];
			
			// encrypt the buffer using the cipher obtained previosly
			byte[] tmp = m_encodeCipher.update(trimbuf);
			
			// I don't think this should happen, but just in case..
			if (tmp != null)
				fout.write(tmp);
		}
		
		// finalize the encryption since we've done it in blocks of MAX_FILE_BUF
		byte[] finalbuf = m_encodeCipher.doFinal();
		if (finalbuf != null)
			fout.write(finalbuf);
		
		fout.flush();
		fin.close();
		fout.close();
		fout.close();
	}
	
	/**
	 * Read from the encrypted file (input) and turn the cipher back into cleartext. Write the
	 * cleartext buffer back out to disk as (output) File.
	 * 
	 * I left CipherInputStream in here as a test to see if I could mix it with the fillEmptyFieldsWithContact() and
	 * final() methods of encrypting and still have a correctly decrypted file in the end. Seems to
	 * work so left it in.
	 * 
	 * @param input
	 *            - File object representing encrypted data on disk
	 * @param output
	 *            - File object of cleartext data to write out after decrypting
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static void readEncryptedFile(File input, File output) throws IllegalBlockSizeException, BadPaddingException, IOException
	{
		FileInputStream fin;
		FileOutputStream fout;
		CipherInputStream cin;
		int nread = 0;
		byte[] inbuf = new byte[MAX_FILE_BUF];
		
		fout = new FileOutputStream(output);
		fin = new FileInputStream(input);
		
		// creating a decoding stream from the FileInputStream above using the cipher created from
		// setupDecrypt()
		cin = new CipherInputStream(fin, m_decodeCipher);
		
		while ((nread = cin.read(inbuf)) > 0)
		{
			// create a buffer to write with the exact number of bytes read. Otherwise a short read
			// fills inbuf with 0x0
			byte[] trimbuf = new byte[nread];
			for (int i = 0; i < nread; i++)
				trimbuf[i] = inbuf[i];
			
			// write out the size-adjusted buffer
			fout.write(trimbuf);
		}
		
		fout.flush();
		cin.close();
		fin.close();
		fout.close();
	}
}
