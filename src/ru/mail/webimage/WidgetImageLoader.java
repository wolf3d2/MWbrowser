package ru.mail.webimage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Vector;

import com.jbak.superbrowser.stat;

import ru.mail.webimage.ImageDownloader.LoadBitmap;
import ru.mail.webimage.widgets.BgImgContainer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
/** Загружает картинки с учетом логики виджетов с автоматической выгрузкой ненужных картинок из памяти */
public class WidgetImageLoader implements OnImageLoad{

	public static WidgetImageLoader INSTANCE;
	
//	private ImageManager imgLoader;
	ImageDownloader imgLoader;
	private Vector<Entry> arLoadedBitmaps;
	public static final int SIZE_FULL = 0;
    public static String IMAGE_CACHE_SUBFOLDER = "imageCache";
    Point arSizes[];
    int SMALL_CACHE_SIZE = 5;
    ImagesSettings m_imgSettings;
    public static interface ImagesSettings
    {
    	public boolean isNetworkAvaliable();
    	public boolean isImagesEnabled();
    }
	public int getLoadedSize()
	{
	    return arLoadedBitmaps.size();
	}
	public WidgetImageLoader()
	{
		INSTANCE = this;
	}
	public WidgetImageLoader init(Context context,String externalDir,ImagesSettings set){
		return init(context, externalDir, set, 0);
	}
	public static String getInternalPath(Context context)
	{
		return context.getCacheDir().getAbsolutePath()+'/'+IMAGE_CACHE_SUBFOLDER;
	}
	public WidgetImageLoader init(Context context,String externalDir,ImagesSettings set,int threadSize){
		
		m_imgSettings = set;
	    String folder = "imageCache";
	    String extDir = externalDir+folder;
	    if(threadSize>1)
			imgLoader = new MultiThreadImageDownloader(context, getInternalPath(context),extDir,threadSize);//new ImageManager(context,extDir);
	    else
	    	imgLoader = new ImageDownloader(context, context.getCacheDir().getAbsolutePath()+'/'+folder,extDir);//new ImageManager(context,extDir);
		arLoadedBitmaps = new Vector<Entry>();
		return this;
	}
	
//	public ImageManager getLoader(){
//		return null;
//	}
	private final void recycleEntry(Entry e)
	{
	    e.recycledTime = System.currentTimeMillis()+2500;
	    if(!recycleHandler.hasMessages(0))
	    	recycleHandler.sendEmptyMessageDelayed(0, 3000);
	}
    public void recycleNow(Bitmap bmp)
    {
    	boolean ret = false;
        if(bmp!=null)
        {
            for(int i = arLoadedBitmaps.size()-1;i>=0;i--)
            {
            	Entry e = arLoadedBitmaps.get(i);
                if(e.bitmap==bmp)
                {
                	arLoadedBitmaps.remove(i);
                	e.bitmap.recycle();
                	e.bitmap = null;
                }
            }
            if(!ret){
            	bmp.recycle();
            	bmp = null;
            }
        }
    	
    }
    public void recycleWebImage(WebImage iv)
    {
        for(Entry e:arLoadedBitmaps)
        {
            if(e.wi!=null&&e.wi.get()==iv)
            {
                recycleEntry(e);
            }
        }
    }
	/** Удаление картинки bmp. Выполняется не сразу, а с задержкой */
    public void recycle(Bitmap bmp)
    {
    	boolean ret = false;
        if(bmp!=null)
        {
            for(Entry e:arLoadedBitmaps)
            {
                if(e.bitmap==bmp)
                {
                    recycleEntry(e);
                    ret = true;
                }
            }
            if(!ret){
            	bmp.recycle();
            	bmp = null;
            }
        }
    }
    public static void cancelDownload(WebImage wi)
    {
        INSTANCE.imgLoader.cancelByUserParam(wi);
    }
    public static void displayWebImage(String url,WebImage wi,int size,Object userParam)
    {
        INSTANCE.imgLoader.cancelByUserParam(wi);
        wi.setWebImageUrl(url);
        if(url==null)
        {
            wi.setWebImage(url, null,size,userParam);
            return;
        }
        Entry e = new Entry(url, wi,size,userParam);
        INSTANCE.imgLoader.loadImage(INSTANCE.getLoadBitmap(e,userParam));
//        INSTANCE.imgLoader.load(url, INSTANCE.onLoadListener);
        
    }
    public void cancelAllDownloads()
    {
    	imgLoader.cancelAll();
    }
/** Удаляет все загруженные картинки */    
    public void clearMemory()
    {
        try{
            for(Entry e:arLoadedBitmaps)
            {
                if(e.bitmap!=null){
                    e.bitmap.recycle();
                    e.bitmap = null;
                }
            }
            arLoadedBitmaps.clear();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public void destroy()
    {
        clearMemory();
    }
    /** Возвращает true, если есть соединение или картинка загружена в кеш */
    public static boolean canBeShown(String url)
    {
        if(url==null)
            return true;
        if(INSTANCE.m_imgSettings==null||INSTANCE.m_imgSettings.isNetworkAvaliable())
            return true;
        try{ 
            return INSTANCE.hasCachedImage(url);
        }
        catch (Throwable e) {
        }
        return false;
    }
    /** Возвращает true, если мы можем заюзать уже загруженную картинку, но поставленную в очередь на удаление
     *  Предотвращает передергивание картинок при подгрузке списка
     */
    boolean canReuseBitmap(WebImage wi,String url,int size,Object userParam)
    {
        try{
            for(Entry e:arLoadedBitmaps)
            {
                if(e.url.equals(url)&&e.bitmap!=null&&!e.bitmap.isRecycled())
                {
                    if(e.recycledTime>0)
                    {
                        // Если картинка в процессе удаления - не будем удалять, а вернем картинку
                        e.recycledTime = 0;
                        wi.setWebImage(url, e.bitmap,size,userParam);
                        e.wi = new WeakReference<WebImage>(wi);
                    }
                    else
                    {
                        Entry ent = new Entry(url, wi, size,userParam);
                        ent.bitmap = Bitmap.createBitmap(e.bitmap);
                        wi.setWebImage(url, ent.bitmap,size,userParam);
                    }
                    return true;
                }
            }
        }
        catch (Throwable e) {
        }
        return false;
    }
    /** Асинхронная загрузка картинок. 
     * Производится если в настройках включено отображение картинок и доступна сеть, либо картинка есть в кеше
    *@param url адрес картинки
    *@param iv Контейнер картинки
    *@param ignoreSetting true - картинка будет загружена, даже если в настройках отключено отображение картинок
    *@param size Размер, одна из констант SIZE_
    *@return true - изображение будет показано, false - не будет
     */
    public static boolean displayImageIfNeed(String url,BgImgContainer iv,boolean ignoreSetting,int size,Object userParam)
    {
        if(iv==null||iv.getImageView()==null)
            return false;
        if(!ignoreSetting&&INSTANCE.m_imgSettings!=null&&!INSTANCE.m_imgSettings.isImagesEnabled())
        {
            iv.setVisibility(View.GONE);
            return false;
        }
        else if(TextUtils.isEmpty(url))
        {
            // Пустой адрес 
            iv.setVisibility(View.GONE);
            displayImage(null, iv.getImageView(),size,userParam);
            return false;
        }
        else{
            if(!canBeShown(url))
            {
                // Нет соединения  и нет картинки в кеше
                iv.setNoConnect();
                return false;
            }
            WebImage wi = (WebImage)iv.getImageView();
            if(url.equals(iv.getUrl())&&iv.isImageLoaded())
            	return true;
            
            iv.setVisibility(View.VISIBLE);
            if(INSTANCE.canReuseBitmap(wi, url, size,userParam))
                return true;
            iv.resetImage();
            WidgetImageLoader.displayWebImage(url, wi,size,userParam);
        }
        return true;
        
    }
    public static void displayImage(String url,ImageView iv,int size,Object userParam)
    {
        if(url==null)
        {
            iv.setImageDrawable(null);
            return;
        }
        if(iv instanceof WebImage)
        {
            displayWebImage(url, (WebImage)iv,size,userParam);
        }
        else
        {
            // Нельзя загружать картинку в ImageView! Иначе после зачистки через clearmemory будет вылет
            throw new IllegalStateException("Don't load image into ImageView");
        }
    }
    /** Обработчик загрузки картинок */
    @Override
    public void onImageLoad(LoadBitmap lb)
    {
        Entry ent = (Entry)lb.userParam2;
        if(lb.canceled||ent.wi==null||ent.wi.get()==null||!lb.url.equals(ent.wi.get().getWebImageUrl()))
        {
            if(lb.bitmap!=null)
            {
                ent.bitmap = lb.bitmap;
                recycleEntry(ent);
            }
            return;
        }
        String url = lb.url;
        Bitmap b = lb.bitmap;
        boolean bSet = false;
        try{
            ent.wi.get().setWebImage(url, b,lb.userInt,lb.param);
            bSet = true;
        }
        catch (Throwable err) {
            err.printStackTrace();
        }
        if(b!=null)
        {
            if(bSet)
            {
                ent.bitmap = b;
                arLoadedBitmaps.add(ent);
//                Log.d("SET IMAGE", url+stat.STR_SPACE+arLoadedBitmaps.size()+stat.STR_SPACE+ent.wi);
            }
            else{
                b.recycle();
                b = null;
            }
        }
    }
    public boolean hasCachedImage(String url)
    {
        File f = imgLoader.getCachedFile(url);
        return f.exists()&&!f.isDirectory();
    }
    public File getCachedImage(String url)
    {
        return imgLoader.getCachedFile(url);
    }
/** Загрузка изображения без каких-либо дополнительных обработок*/    
    public void load(LoadBitmap lb)
    {
        lb.widgetLoaderParam = lb;
        imgLoader.loadImage(lb);
    }
    LoadBitmap getLoadBitmap(Entry ent,Object userParam)
    {
        LoadBitmap ret = new LoadBitmap(ent.url, this);
        ret.userInt = ent.size;
        ret.desiredSize = arSizes[ent.size];
        ret.widgetLoaderParam = ent.wi.get();
        ret.userParam2 = ent;
        ret.param = userParam;
        return ret;
    }
    Handler recycleHandler = new Handler()
    {
    	public void handleMessage(Message msg) 
    	{
            int rc = 0,rd = 0;
            try{
            long t = System.currentTimeMillis();
            int sz = arLoadedBitmaps.size();
            for(int i=sz-1;i>=0;i--)
            {
                Entry e = arLoadedBitmaps.get(i);
                if(e.recycledTime!=0||e.wi.get()==null)
                {
                	rc++;
                	if(e.recycledTime<=t)
                	{
                		rd++;
                        if(e.bitmap!=null)
                        {
                            Bitmap b = e.bitmap;
                            e.bitmap = null;
                            b.recycle();
                        }
                        arLoadedBitmaps.remove(i);
                	}
                }
            }
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            System.gc();
            Log.d("RECYCLE_TIMER", "left "+arLoadedBitmaps.size()+" rc="+rc+" rd="+rd);
            if(rc>0&&rc!=rd)
            	sendEmptyMessageDelayed(0, 2500);
    	};
    };
    
    public static interface WebImage
    {
        public void setWebImageUrl(String url);
        public String getWebImageUrl();
        public void setWebImage(String url,Bitmap bmp,int size,Object userParam);
        public void setEmptyImage();
        public Drawable getCurrentDrawable();
    }
/** Информация о загружаемой либо загруженной картинке */    
    private static class Entry
    {
        public String url;
        public WeakReference<WebImage>wi;
        public int size=SIZE_FULL;
        public Bitmap bitmap;
        public long recycledTime = 0;
        public Object userParam;
        public Entry(String url,WebImage wi,int size,Object userParam)
        {
            this.url = url;
            this.wi = new WeakReference<WebImage>(wi);
            this.size = size;
            this.userParam = userParam;
        }
        @Override
        public String toString() {
        	return url+stat.STR_SPACE+(wi==null||wi.get()==null?"null wi":wi.get().toString());
        }
    }
    public static void recycleImageView(ImageView imageView){
		Drawable toRecycle = imageView.getDrawable();
		if (toRecycle != null && toRecycle instanceof BitmapDrawable){
            Bitmap bmp = ((BitmapDrawable)toRecycle).getBitmap();
            INSTANCE.recycle(bmp);
        }
    }
    public void setSizes(Point sizes[])
    {
    	arSizes = sizes;
    }
    public void setSize(int size,int width,int height)
    {
    	if(size>=arSizes.length)
    		return;
    	arSizes[size] = new Point(width, height);
    }
    public final ImageDownloader getImageDownloader()
    {
    	return imgLoader;
    }
}
