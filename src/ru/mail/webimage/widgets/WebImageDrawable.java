package ru.mail.webimage.widgets;

import ru.mail.webimage.WidgetImageLoader;
import ru.mail.webimage.WidgetImageLoader.WebImage;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class WebImageDrawable extends BitmapDrawable
{
    WebImage webImage;
    Drawable.Callback oldCallback;
    boolean autoload = true;
    int size = WidgetImageLoader.SIZE_FULL;
    Object mUserParam;
    @SuppressWarnings("deprecation")
	public WebImageDrawable(Bitmap bitmap,WebImage wi,int size,Object userParam)
    {
        super(bitmap);
        webImage = wi;
        this.size = size;
        mUserParam = userParam;
    }
    public void setAutoload(boolean autoload)
    {
        this.autoload = autoload;
    }
    @Override
    public void draw(Canvas canvas)
    {
        Bitmap b = getBitmap();
        if(b==null&&!TextUtils.isEmpty(webImage.getWebImageUrl()))
        {
        	WidgetImageLoader.displayWebImage(webImage.getWebImageUrl(), webImage,size,mUserParam);
        	return;
        }
        if(b!=null&&b.isRecycled())
        {
            webImage.setEmptyImage();
            if(autoload)
                WidgetImageLoader.displayWebImage(webImage.getWebImageUrl(), webImage,size,mUserParam);
            Drawable drw = webImage.getCurrentDrawable();
            if(drw!=null)
            {
                try{
                    drw.draw(canvas);
                }
                catch (Throwable e) {
                }
            }
            return;
        }
        super.draw(canvas);
    }
}
