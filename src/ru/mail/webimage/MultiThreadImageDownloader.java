package ru.mail.webimage;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;

public class MultiThreadImageDownloader extends ImageDownloader {

	int mThreadsPos=0;
	CopyOnWriteArrayList<ImageDownloader> mListDownloaders = new CopyOnWriteArrayList<ImageDownloader>();
	public MultiThreadImageDownloader(Context context, String internalDir,String externalDir,int threadsSize) {
		super(context, internalDir, externalDir);
		for(int i=0;i<threadsSize-1;i++)
		{
			ImageDownloader id = super.getCopy();
			id.setImageLoaders(mImageLoaders);
			mListDownloaders.add(id);
		}
	}
	public MultiThreadImageDownloader(Context context, MemorySafeImageCache cache) {
		super(context, cache);
	}
	@Override
	public void loadImage(LoadBitmap lb) {
		if(arBitmaps.size()==0)
		{
			super.loadImage(lb);
			return;
		}
		for(ImageDownloader id:mListDownloaders)
		{
			if(id.arBitmaps.size()==0)
			{
				id.loadImage(lb);
				return;
			}
		}
		if(mThreadsPos==0)
			super.loadImage(lb);
		else
			mListDownloaders.get(mThreadsPos-1).loadImage(lb);
		++mThreadsPos;
		if(mThreadsPos>=mListDownloaders.size()+1)
			mThreadsPos = 0;
	}
	@Override
	public void cancelByUserParam(Object param) {
		super.cancelByUserParam(param);
		for(ImageDownloader id:mListDownloaders)
			id.cancelByUserParam(param);
	}
	@Override
	public void cancelAll() {
		super.cancelAll();
		for(ImageDownloader id:mListDownloaders)
			id.cancelAll();
	}
	@Override
	public Vector<LoadBitmap> getDownloadsByUrl(String url) {
		Vector<LoadBitmap> v = super.getDownloadsByUrl(url); 
		for(ImageDownloader id:mListDownloaders)
			v.addAll(id.getDownloadsByUrl(url));
		return v;
	}
	@Override
	public ImageDownloader getCopy() {
		MultiThreadImageDownloader id = new MultiThreadImageDownloader(getContext(), m_cache);
		id.mListDownloaders = mListDownloaders;
		return id;
	}
}
