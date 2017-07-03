/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 24 oct. 2011
 ******************************************************************************
 * Defects
 *
 */

package com.ale.infra.contact;

import android.graphics.Bitmap;

import java.io.InputStream;

/**
 * @author cebruckn
 * 
 */
public interface IBitmapConverter
{
	Bitmap createBitmapFromInputStream(InputStream photoAsStream,int maxSize);

	Bitmap createBitmapFromByteArray(byte[] blob);
	
	byte[] createByteArrayFromContactPhoto(IContact contact);

	byte[] createByteArrayFromBitmap(Bitmap bmp);

	Bitmap createBitmapFromFilePath(String pathName);

	Bitmap createBitmapFromFilePath(String pathName, int maxSize);
}
