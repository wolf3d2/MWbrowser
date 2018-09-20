package com.jbak.superbrowser;

import android.graphics.Point;

public class ImageSizes {
	public static final int SIZE_FULL = 0;
	public static final int SIZE_FILEMANAGER_IMAGE = 1;
	public static Point[] getSizes()
	{
		return new Point[]
				{
					null,
					new Point(200, 200) // SIZE_FILEMANAGER_IMAGE
				};
	}
}
