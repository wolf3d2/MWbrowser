package ru.mail.webimage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.SyncAsycOper;
import ru.mail.mailnews.st.UniObserver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.jbak.utils.ContextRef;

public class ImageDownloader extends ContextRef
{
	Vector<LoadBitmap> arBitmaps = new Vector<ImageDownloader.LoadBitmap>();
    SyncAsycOper m_oper = null;
    MemorySafeImageCache m_cache;
    private byte mBuf[] = new byte[FileUtils.BUF_SIZE];
    private BitmapFactory.Options mOpts;
    ArrayList<ImageLoad> mImageLoaders = new ArrayList<ImageLoad>();
    public ImageDownloader(Context context,String internalDir,String externalDir)
    {
    	setContext(context);
        m_cache = new MemorySafeImageCache(internalDir, externalDir);
        setContext(context);
        init(context);
    }
    public ImageDownloader(Context context, MemorySafeImageCache cache)
    {
        m_cache = cache;
        init(context);
    }
    void init(Context c)
    {
    	mOpts = new BitmapFactory.Options();
    	mOpts.inTempStorage = mBuf;
    }
    public void setImageLoaders(ArrayList<ImageLoad> loaders)
    {
    	mImageLoaders = loaders;
    }
    public void addImageLoader(ImageLoad loader)
    {
    	mImageLoaders.add(loader);
    }
    public ImageDownloader getCopy()
    {
    	return new ImageDownloader(getContext(),m_cache);
    }
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg) 
        {
            LoadBitmap lb = (LoadBitmap)msg.obj;
            if(lb.listener!=null)
                lb.listener.onImageLoad(lb);
        };
    };
    Bitmap loadBitmapFromLoaders(LoadBitmap lb,String cachePath) throws Throwable
    {
    	for(ImageLoad id:mImageLoaders)
    	{
    		if(id.canLoad(lb))
    		{
        		Bitmap bmp =  id.loadBitmap(getContext(),lb,this);
        		if(bmp==null)
        			return bmp;
        		if(!id.needSaveToCache())
        			return bmp;
        		OutputStream os = new FileOutputStream(cachePath);
        		boolean compress = bmp.compress(CompressFormat.JPEG, 100, os);
        		if(compress)
        			lb.bitmap = bmp;
        		return bmp;
    		}
    	}
    	return null;

    }
    boolean downloadBitmap(ImageCache cache, LoadBitmap lb, String path) throws Throwable
    {
    	boolean ok = false;
    	WebDownload wd = new WebDownload().setBuffer(mBuf);
    	ok = wd.getUrlToFile(lb.url, path, true);
    	if(!ok)
    	{
    		wd = new WebDownload().setBuffer(mBuf);
        	ok = wd.getUrlToFile(lb.url, path, false);
    	}
    	return ok;
    }
    Bitmap loadBitmap(LoadBitmap lb) throws Throwable
    {
        String url = lb.url;
        ImageCache cache = m_cache.selectCache();
        String path = cache.getCachePath(url);
    	Bitmap bitmap = loadBitmapFromLoaders(lb, path);
    	if(bitmap!=null)
    		return bitmap;
        File f = new File(path);
        if(!f.exists())
        {
        	if(downloadBitmap(cache, lb, path))
        	{
                cache.putCacheFile(path);
                f = new File(path);
        	}
        }
        return downloadBitmapFromFile(f, lb,true);
    }
    public Bitmap downloadBitmapFromFile(File f,LoadBitmap lb,boolean delFileIfBad)
    {
        if(f.exists())
        {
            try{
                if(lb.desiredSize!=null)
                {
                    Bitmap result = decodeSampledBitmapFromFile(f.getAbsolutePath(), lb.desiredSize.x, lb.desiredSize.y,mOpts);
                    if(result==null&&delFileIfBad)
                    	f.delete();
                    return result;
                }
                else
                {
                	BufferedInputStream is = new BufferedInputStream(new FileInputStream(f),FileUtils.BUF_SIZE);
                	Bitmap result = BitmapFactory.decodeStream(is,null,mOpts);
                	is.close();
                	if(delFileIfBad&&(result == null||(result.getWidth() * result.getHeight())<=1)){
                		f.delete();
                	}
                	return result;
                }
                    
            }
            catch (Throwable e) {
            	Log.d(TAG, "load exception "+e.toString());
            	e.printStackTrace();
            }
        }
        return null;
    }
    public void loadImage(LoadBitmap lb)
    {
        arBitmaps.add(lb);
        if(m_oper==null)
            newAsyncOper();
    }
    public static final String TAG = "ImageDownloader";
    void newAsyncOper()
    {
        m_oper = new st.SyncAsycOper(null)
        {
            @Override
            public void makeOper(UniObserver obs)
            {
                while(arBitmaps.size()>0)
                {
                    LoadBitmap lb = arBitmaps.get(0);
                    arBitmaps.remove(0);
                    if(lb.canceled)
                    {
                        Log.d(TAG, "cancel:"+lb.url);
                        continue;
                    }
                    if(lb.bitmap!=null&&!lb.bitmap.isRecycled()){
                    	try{
                            Log.d(TAG, "copy:"+lb.url);
                    		lb.bitmap = lb.bitmap.copy(lb.bitmap.getConfig(), false);
                    	}
                    	catch (Throwable e) {
                            Log.d(TAG, "copy fail:"+lb.url);
                    		lb.bitmap = null;
                    	}
                    }
                    if(lb.bitmap==null)
						try {
                            Log.d(TAG, "load:"+lb.url);
							lb.bitmap = loadBitmap(lb);
						} catch (Throwable e) {
                            Log.d(TAG, "load err:"+lb.url);
							e.printStackTrace();
							lb.bitmap = null;
						}
                    lb.success = lb.bitmap!=null;
                    Log.d(TAG, (lb.success?"success:":"fail:")+lb.url);
                    if(lb.canceled)
                    {
                        Log.d(TAG, "cancel:"+lb.url);
                    	if(lb.bitmap!=null){
                    		lb.bitmap.recycle();
                    		lb.bitmap = null;
                    	}
                    	continue;
                    }
                    handler.sendMessage(handler.obtainMessage(0, lb));
                }
                m_oper = null;
            }
        };
        m_oper.startAsync();
    }
    public static class LoadBitmap
    {
        public LoadBitmap(String url,OnImageLoad listener)
        {
            this.url = url;
            this.listener = listener;
        }
        public String url;
        public boolean success = false;
        public Object widgetLoaderParam;
        public Object userParam2;
        public Point desiredSize;
        public Bitmap bitmap;
        public OnImageLoad listener;
        public boolean canceled = false;
        public int userInt;
        public Object param;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight && width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
        }
	    public static Bitmap decodeSampledBitmapFromFile(String path,int reqWidth, int reqHeight) throws IOException 
	    {
	    	return decodeSampledBitmapFromFile(path, reqWidth, reqHeight, null);
	    }
        public static Bitmap decodeSampledBitmapFromFile(String path,int reqWidth, int reqHeight,Options options) throws IOException 
        {
            FileInputStream fis = null;
            Bitmap b = null;
            try{
            	fis = new FileInputStream(path);
                if(options==null)
                	options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(fis, null, options);
                fis.getChannel().position(0);
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                b = BitmapFactory.decodeStream(fis,null,options);
                options.inSampleSize = 1;
            }
            catch (Throwable e) {
            	e.printStackTrace();
            }
            try{
                if(fis!=null)
                    fis.close();
            }
            catch (Throwable e) {
            }
            return b;
        }
        public File getCachedFile(String url)
        {
            return new File(m_cache.selectCache().getCachePath(url));
        }
        public void cancelAll()
        {
            try{
                for(int i=arBitmaps.size()-1;i>=0;i--)
                {
                    LoadBitmap lb = arBitmaps.get(i);
                    lb.canceled = true;
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        	
        }
        public void cancelByUserParam(Object param)
        {
            try{
                for(int i=arBitmaps.size()-1;i>=0;i--)
                {
                    LoadBitmap lb = arBitmaps.get(i);
                    if(lb.widgetLoaderParam==param)
                    {
                        lb.canceled = true;
                    }
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
        public Vector<LoadBitmap> getDownloadsByUrl(String url)
        {
        	Vector<LoadBitmap> ar = new Vector<ImageDownloader.LoadBitmap>();
        	if(TextUtils.isEmpty(url))
        		return ar;
            for(int i=arBitmaps.size()-1;i>=0;i--)
            {
            	LoadBitmap lb = arBitmaps.get(i);
            	if(url.equals(lb.url))
            		ar.add(lb);
            }
            return ar;
        }
}
