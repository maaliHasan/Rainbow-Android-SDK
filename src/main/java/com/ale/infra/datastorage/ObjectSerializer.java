/******************************************************************************
 * Copyright © 2012 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : franci11 6 janv. 2012
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.datastorage;

import android.content.Context;

import com.ale.util.log.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author franci11
 * 
 */
public class ObjectSerializer
{
	private static final String LOG_TAG = "ObjectSerializer";
	private Context m_context;
	
	public ObjectSerializer(Context context)
	{
		m_context = context;
	}
	
	public Object deserializeObjectFromFile(FileInputStream serializedUserFile)
	{
		ObjectInputStream objectInputStream = createObjectInputStreamFromFile(serializedUserFile);
		
		if (objectInputStream == null)
		{
			return null;
		}
		
		Object deserializedObject = readObjectFromInputStream(objectInputStream);
		
		closeObjectInputStream(objectInputStream);
		
		return deserializedObject;
	}
	
	private void closeObjectInputStream(ObjectInputStream objectInputStream)
	{
		try
		{
			objectInputStream.close();
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error closing object input stream", e);
		}
	}
	
	private void closeObjectOutputStream(ObjectOutputStream objectOutputStream)
	{
		try
		{
			objectOutputStream.close();
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error closing object output stream", e);
		}
	}
	
	private Object readObjectFromInputStream(ObjectInputStream in)
	{
		try
		{
			return in.readObject();
		}
		catch (Exception e)
		{
			Log.getLogger().error(LOG_TAG, "Error reading object from inputstream : " + e.getMessage());
		}
		return null;
	}
	
	private ObjectInputStream createObjectInputStreamFromFile(FileInputStream serializedUserFile)
	{
		try
		{
			return new ObjectInputStream(serializedUserFile);
		}
		catch (Exception e)
		{
			Log.getLogger().error(LOG_TAG, "Error creating object input stream", e);
		}
		
		return null;
	}
	
	private ObjectOutputStream createObjectOutputStreamFromFile(FileOutputStream fos)
	{
		try
		{
			return new ObjectOutputStream(fos);
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error creating object output stream", e);
			return null;
		}
	}
	
	public void closeFileInputStream(FileInputStream fileInputStream)
	{
		try
		{
			fileInputStream.close();
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error closing file intput stream", e);
		}
	}
	
	private void closeFileOutputStream(FileOutputStream fileOutputStream)
	{
		try
		{
			fileOutputStream.close();
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error closing file output stream", e);
		}
	}
	
	FileInputStream openFileForReading(String fileName)
	{
		try
		{
			return m_context.openFileInput(fileName);
		}
		catch (FileNotFoundException e)
		{
			// Not necessary an error, it is possible that the file doesn't exists (if nerver been
			// saved)
			Log.getLogger().verbose(LOG_TAG, "Error opening serialized file : " + fileName);
			return null;
		}
	}
	
	/**
	 * Open a private file associated with this Context's application package for writing. Creates
	 * the file if it doesn't already exist.
	 */
	private FileOutputStream openOrCreateFileForWriting(String fileName)
	{
		try
		{
			return m_context.openFileOutput(fileName, Context.MODE_PRIVATE);
		}
		catch (FileNotFoundException e)
		{
			Log.getLogger().error(LOG_TAG, "Error openFileOutput : " + fileName, e);
			return null;
		}
	}
	
	public void setObject(Object objectToSet, String serializationFileName)
	{
		FileOutputStream fos = openOrCreateFileForWriting(serializationFileName);
		
		if (fos == null)
		{
			return;
		}
		
		serializeObject(objectToSet, fos);
		
		closeFileOutputStream(fos);
	}
	
	private void serializeObject(Object objectToSerialize, FileOutputStream fileOutputStream)
	{
		ObjectOutputStream bos = createObjectOutputStreamFromFile(fileOutputStream);
		
		if (bos == null)
		{
			return;
		}
		
		writeObjectInOutputStream(objectToSerialize, bos);
		
		closeObjectOutputStream(bos);
	}
	
	private void writeObjectInOutputStream(Object objectToSerialize, ObjectOutputStream objectOutputStream)
	{
		try
		{
			objectOutputStream.writeObject(objectToSerialize);
		}
		catch (IOException e)
		{
			Log.getLogger().error(LOG_TAG, "Error writing object to output stream", e);
		}
	}
	
}
