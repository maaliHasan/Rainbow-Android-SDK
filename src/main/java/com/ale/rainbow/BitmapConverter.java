/******************************************************************************
 * Copyright � 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : cebruckn 24 oct. 2011
 ******************************************************************************
 * Defects
 * 2012/03/18 cebruckn crms00426778 [Crash].Crash in loop
 */

package com.ale.rainbow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;

import com.ale.infra.contact.IBitmapConverter;
import com.ale.infra.contact.IContact;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author cebruckn
 * 
 */
public class BitmapConverter implements IBitmapConverter
{
	private static final String LOG_TAG = "BitmapConverter";
	
	private static final int BUFFER_SIZE = 1024;
	private static final int ROTATION_270 = 270;
	private static final int ROTATION_180 = 180;
	private static final int ROTATION_90 = 90;
	private static final int PNG_QUALITY = 100;

	public static final int DEFAULT_MAX_SIZE = 400;

	@Override
	public Bitmap createBitmapFromInputStream(InputStream photoAsStream,int maxSize)
		{
		byte[] byteArr = new byte[0];
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		int count = 0;
		
		try
		{
			while ((len = photoAsStream.read(buffer)) > -1)
			{
				if (len != 0)
				{
					if (count + len > byteArr.length)
					{
						byte[] newbuf = new byte[(count + len) * 2];
						System.arraycopy(byteArr, 0, newbuf, 0, count);
						byteArr = newbuf;
					}
					
					System.arraycopy(buffer, 0, byteArr, count, len);
					count += len;
				}
			}
			
			return decodeBitmapFromByteArray(byteArr,maxSize);
		}
		catch (Exception e)
		{
			Log.getLogger().warn(LOG_TAG, "Could not create byte array from input stream !", e);
			
			return null;
		}
	}

	private Bitmap decodeBitmapFromByteArray(byte[] byteArr) {
		return decodeBitmapFromByteArray(byteArr, DEFAULT_MAX_SIZE);
	}

	private Bitmap decodeBitmapFromByteArray(byte[] byteArr,int maxsize)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length, options);
		
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, maxsize, maxsize);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length, options);
		
		if (bmp == null)
		{
			Log.getLogger().warn(LOG_TAG, "Could not create bitmap from byte array !");
		}
		
		return bmp;
	}
	
	@Override
	public Bitmap createBitmapFromByteArray(byte[] blob)
	{
		if (blob == null)
			return null;
		
		if (blob.length == 0)
			return null;
		
		Bitmap bmp = decodeBitmapFromByteArray(blob);
		
		if (bmp == null)
		{
			return null;
		}
		
		try
		{
			return squareCenterCrop(bmp);
		}
		catch (Exception e)
		{
			Log.getLogger().error(LOG_TAG, "Impossible to squareCenterCrop bitmap: ", e);
			
			return null;
		}
	}
	
	@Override
	public byte[] createByteArrayFromContactPhoto(IContact contact)
	{
		if (contact.getPhoto() == null)
			return null;
		
		return createByteArrayFromBitmap(contact.getPhoto());
	}

	@Override
	public byte[] createByteArrayFromBitmap(Bitmap bmp)
	{
		if (bmp == null)
			return null;
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream);
		return stream.toByteArray();
	}
	
	private Bitmap squareCenterCrop(Bitmap source)
	{
		int newHeight = 0;
		int newWidth = 0;
		
		if (source.getWidth() > source.getHeight())
		{
			newHeight = source.getHeight();
			newWidth = source.getHeight();
		}
		else
		{
			newHeight = source.getWidth();
			newWidth = source.getWidth();
		}
		
		return squareCenterCrop(source, newHeight, newWidth);
	}
	
	private Bitmap squareCenterCrop(Bitmap source, int newHeight, int newWidth)
	{
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		
		// Compute the scaling factors to fit the new height and width, respectively.
		// To cover the final image, the final scaling will be the bigger
		// of these two.
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);
		
		// Now get the size of the source bitmap when scaled
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;
		
		// Let's find out the upper left coordinates if the scaled bitmap
		// should be centered in the new size give by the parameters
		float left = (newWidth - scaledWidth) / 2;
		float top = (newHeight - scaledHeight) / 2;
		
		// The target rectangle for the new, scaled version of the source bitmap will now
		// be
		RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
		
		// Finally, we create a new bitmap of the specified size and draw our new,
		// scaled bitmap onto it.
		Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
		Canvas canvas = new Canvas(dest);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(source, null, targetRect, paint);
		
		return dest;
	}

	@Override
	public Bitmap createBitmapFromFilePath(String pathName)
	{
		return createBitmapFromFilePath(pathName, DEFAULT_MAX_SIZE);
	}

	@Override
	public Bitmap createBitmapFromFilePath(String pathName,int maxSize)
	{
		if( !isImage(pathName) ) {
			Log.getLogger().warn(LOG_TAG, "File is not an image");
			return null;
		}

		int rotationAngle = calculateRotationAngle(pathName);

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(pathName, options);

		if (rotationAngle != 0)
		{
			Matrix matrix = new Matrix();
			matrix.postRotate(rotationAngle);

			return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}
		else
		{
			return bmp;
		}
	}

	private int calculateRotationAngle(String pathName)
	{
		int rotationAngle = 0;
		
		try
		{
			ExifInterface ei = new ExifInterface(pathName);
			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
			{
				rotationAngle = ROTATION_90;
			}
			else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
			{
				rotationAngle = ROTATION_180;
			}
			else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
			{
				rotationAngle = ROTATION_270;
			}
		}
		catch (IOException e)
		{
			Log.getLogger().warn(LOG_TAG, "Can't read file : ", e);
		}
		
		return rotationAngle;
	}
	
	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth)
		{
			int stretchWidth = Math.round((float) width / (float) reqWidth);
			int stretchHeight = Math.round((float) height / (float) reqHeight);
			
			if (stretchWidth <= stretchHeight)
				return stretchHeight;
			else
				return stretchWidth;
		}
		
		return inSampleSize;
	}

	public static boolean isImage(String filePath) {
		if ( StringsUtil.isNullOrEmpty(filePath) ) {
			return false;
		}

		File file = new File(filePath);

		if (file == null || !file.exists()) {
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), options);

		return options.outWidth != -1 && options.outHeight != -1;
	}

	public static Bitmap getCroppedBitmap(Bitmap bitmap) {
		if( bitmap == null)
			return null;

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
				bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		//Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
		//return _bmp;
		return output;
	}
}
