package ru.mail.webimage;

import java.io.File;
import java.util.ArrayList;

import ru.mail.webimage.ImageDownloader.LoadBitmap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

@SuppressLint("DefaultLocale")
public abstract class DefaultImageLoaders implements ImageLoad{
	static ArrayList<ImageLoad> mImageLoaders;
	static final int TYPE_FILE = 1;
	int mType;
	public static final String MIME_IMAGE = "image";
	public static final String MIME_VIDEO = "video";
	public abstract boolean canLoad(FileInfo fi);
	public static class FileInfo
	{
		public FileInfo(File file,String mime)
		{
			this.file = file;
			this.mime = mime.toLowerCase();
		}
		public File file;
		public String mime;
	}
	public DefaultImageLoaders setType(int type)
	{
		mType = type;
		return this;
	}
	public static ArrayList<ImageLoad> getLoaders()
	{
		if(mImageLoaders==null)
		{
			mImageLoaders = new ArrayList<ImageLoad>();
			mImageLoaders.add(new FileImagePreviewLoader().setType(TYPE_FILE));
			mImageLoaders.add(new FileVideoPreviewLoader().setType(TYPE_FILE));
		}
		return mImageLoaders;
	}
	public static FileInfo getFileInfoIfCanLoadImage(File f)
	{
		if(f==null)
			return null;
		MimeTypeMap mm = MimeTypeMap.getSingleton();
		String ext = FileUtils.getFileExt(f);
		String mime = null;
		if(ext!=null)
			mime = mm.getMimeTypeFromExtension(ext);
		if(TextUtils.isEmpty(mime))
			return null;
		FileInfo fi = new FileInfo(f, mime);
		for(ImageLoad il:mImageLoaders)
		{
			if(((DefaultImageLoaders)il).canLoad(fi))
				return fi;
		}
		return null;
	}
	public static String getMimeForFileLoader(LoadBitmap lb)
	{
		if(lb.param instanceof FileInfo)
		{
			return ((FileInfo) lb.param).mime;
		}
		return null;
		
	}
	public static File getFileForFileLoader(LoadBitmap lb)
	{
		if(lb.param instanceof FileInfo)
		{
			return ((FileInfo) lb.param).file;
		}
		return null;
		
	}
	/** Загрузка предпросмотра изображения из файла. lb.userParam должен быть файл, lb.userParam2 - строка mime-type */
	public static class FileImagePreviewLoader extends DefaultImageLoaders
	{
		@Override
		public boolean canLoad(LoadBitmap lb) {
			return canLoad((FileInfo)lb.param);
		}
		@Override
		public Bitmap loadBitmap(Context c, LoadBitmap lb,ImageDownloader downloader) {
			return downloader.downloadBitmapFromFile(getFileForFileLoader(lb), lb,false);
		}
		@Override
		public boolean needSaveToCache() {
			return false;
		}
		@Override
		public boolean canLoad(FileInfo fi) {
			if(!TextUtils.isEmpty(fi.mime))
				return fi.mime.startsWith(MIME_IMAGE);
			return false;
		}
	}
	public static class FileVideoPreviewLoader extends DefaultImageLoaders
	{

		@Override
		public boolean canLoad(LoadBitmap lb) {
			return canLoad((FileInfo)lb.param);
		}

		@Override
		public Bitmap loadBitmap(Context c, LoadBitmap lb,ImageDownloader downloader) {
			return ThumbnailUtils.createVideoThumbnail(getFileForFileLoader(lb).getAbsolutePath(), Images.Thumbnails.MINI_KIND);
		}

		@Override
		public boolean needSaveToCache() {
			return false;
		}

		@Override
		public boolean canLoad(FileInfo fi) {
			if(!TextUtils.isEmpty(fi.mime))
				return fi.mime.startsWith(MIME_VIDEO);
			return false;
		}
	}
}
