package ru.mail.webimage;

import ru.mail.webimage.ImageDownloader.LoadBitmap;
import android.content.Context;
import android.graphics.Bitmap;

public interface ImageLoad {
	public boolean canLoad(LoadBitmap lb);
	public Bitmap loadBitmap(Context c,LoadBitmap lb,ImageDownloader downloader);
	public boolean needSaveToCache();
}
