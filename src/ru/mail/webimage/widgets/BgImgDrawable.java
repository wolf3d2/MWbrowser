package ru.mail.webimage.widgets;

import ru.mail.mailnews.st;
import ru.mail.webimage.WidgetImageLoader;
import ru.mail.webimage.WidgetImageLoader.WebImage;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class BgImgDrawable extends ImageView implements WebImage{

    public static Bitmap gDefaultBigImage;
	public static Bitmap gDefaultSmallImage;
	protected int mType=BgImgContainer.SMALL;
	private OnSetImageBitmapListener listener;
	private Bitmap defaultImage;
	public String url = null;
	boolean defaultMeasure = false;
	boolean imageLoaded = false;
	boolean fitScreen = false;
	int maxBigWidth = 0;
	int minBigHeight = 0;
	int mImageSize = 0;
	Object mUserParam;
	public BgImgDrawable(Context context) {
		this(context, BgImgContainer.SMALL);
	}
	
	public BgImgDrawable(Context context, int type) {
		super(context);
		this.mType = type;
        init(getContext());
		initDefaultImage();
		initBackground();
		resetImage();
	}
	public BgImgDrawable(Context context, AttributeSet attrs)
    {
	    super(context, attrs);
	    defaultMeasure = true;
        this.mType = BgImgContainer.SMALL;
        init(getContext());
        initDefaultImage();
        initBackground();
        resetImage();
    }
	public BgImgDrawable(BgImgContainer container, int type)
    {
	    super(container.getContext());
	    defaultMeasure = true;
        this.mType = type;
        init(getContext());
        container.setDefaultImage(this);
        container.setImageBackground(this);
        resetImage();
    }
	void init(Context c)
	{
		fitScreen = mType==BgImgContainer.BIG_FIT_SCREEN;
		if(fitScreen)
			defaultMeasure = true;
		if(fitScreen)
		{
			DisplayMetrics dm = c.getResources().getDisplayMetrics();
			maxBigWidth = Math.min(dm.widthPixels, dm.heightPixels);
			maxBigWidth -= 2 * st.dp2px(c, 20);
		}

	}
    public void setUrl(String url)
	{
	    this.url = url;
	}
	void initDefaultImage(){
		switch (mType) {
		case BgImgContainer.BIG:
		case BgImgContainer.BIG_FIT_SCREEN:
		{
			defaultImage = null;
		}
		case BgImgContainer.IMAGE_ONLY:
		case BgImgContainer.IMAGE_OR_NO_CONNECT:
			defaultImage = null;
			break;
        case BgImgContainer.TRANSPARENT:
		case BgImgContainer.NONE:
		case BgImgContainer.SMALL:
		default:{
			break;
		}
	}		
	}
	
	void initBackground(){
	    try{
    		switch (mType) {
    		case BgImgContainer.BIG:
    		case BgImgContainer.BIG_FIT_SCREEN:	
    			//setBackgroundResource(R.drawable.bg_photo_big);
    			break;
    		case BgImgContainer.IMAGE_ONLY:	
    		case BgImgContainer.IMAGE_OR_NO_CONNECT:	
            case BgImgContainer.TRANSPARENT:
                setBackgroundColor(0x00ffffff);
                defaultMeasure = true;
                break;
    		case BgImgContainer.NONE:
    		    setBackgroundColor(0xffbbbbbb);
    		    defaultMeasure = true;
    		    setScaleType(ScaleType.CENTER_CROP);
    		    break;
    		}
	    }
	    catch (Throwable e) {
        }
	}
	
	public void resetImage(){
		recycle(false);
		super.setImageBitmap(defaultImage);
	}
	public void resetImageNow(){
		recycle(true);
		super.setImageBitmap(defaultImage);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		//setWebImageUrl(null);
		recycle(false);
		WidgetImageLoader.cancelDownload(this);
	}
	
	public void recycle(boolean now){
		Drawable toRecycle = getDrawable();
		if (imageLoaded&&!TextUtils.isEmpty(url)&&toRecycle != null && toRecycle instanceof BitmapDrawable){
			Bitmap bmp = ((BitmapDrawable)toRecycle).getBitmap();
			if(bmp != null && bmp != defaultImage){
				if(now)
				    WidgetImageLoader.INSTANCE.recycleNow(bmp);
				else
					WidgetImageLoader.INSTANCE.recycle(bmp);
				imageLoaded = false;
//				bmp.recycle();	
			}
		}		
	}
	@Override
	public void setImageDrawable(Drawable drawable)
	{
        recycle(false);
        if(listener != null)
            listener.onSetImageBitmap();
	    super.setImageDrawable(drawable);
	}
	@Override
	public void setImageBitmap(Bitmap bm) {
	    recycle(false);
	    imageLoaded = false;
		if(listener != null)
			listener.onSetImageBitmap();
		super.setImageBitmap(bm);
	}
	
	public void setOnSetImageBitmapListener(OnSetImageBitmapListener listener){
		this.listener = listener;
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	protected int getSuggestedMinimumHeight() {
		if(minBigHeight>0)
			return minBigHeight;
		return super.getSuggestedMinimumHeight();
	}
	@Override
	protected int getSuggestedMinimumWidth() {
		if(fitScreen)
			return maxBigWidth;
		return super.getSuggestedMinimumWidth();
	}
	int getBigWidthMeasureSpec(int widthMeasureSpec)
	{
		if(!fitScreen)
			return widthMeasureSpec;
		return MeasureSpec.makeMeasureSpec(maxBigWidth, MeasureSpec.UNSPECIFIED);
	}
	int getMinHeight(int widthMeasureSpec)
	{
		if(!fitScreen)
			return 0;
		int w = MeasureSpec.getSize(widthMeasureSpec);
		return w*2/3;
	}
	@Override
	public void setImageResource(int resId) {
		recycle(false);
		imageLoaded = false;
		super.setImageResource(resId);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		widthMeasureSpec = getBigWidthMeasureSpec(widthMeasureSpec);
		minBigHeight = getMinHeight(widthMeasureSpec);
		if(fitScreen)
		{
	        super.onMeasure(widthMeasureSpec, minBigHeight);
	        return;
		}
	    if(defaultMeasure)
	    {
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        return;
	    }
		int width = MeasureSpec.getSize(widthMeasureSpec);
		Drawable drawable = getDrawable();
		if(drawable != null){
			Drawable frame = getBackground();
			int frameW = 0;
			int frameH = 0;
			if(frame != null){
				frameW = frame.getIntrinsicWidth();
				frameH = frame.getIntrinsicHeight();
			}
			int origW = drawable.getIntrinsicWidth();
			int origH = drawable.getIntrinsicHeight();
			int height = origW==0?frameH:(width - frameW) * origH / origW + frameH;
        	setMeasuredDimension(width, height);
		}
		else
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public interface OnSetImageBitmapListener{
		void onSetImageBitmap();
	}
	public void checkImage()
	{
        Drawable drw = getDrawable();
        if (drw != null && drw instanceof BitmapDrawable){
            Bitmap bmp = ((BitmapDrawable)drw).getBitmap();
            if(bmp != null && bmp != defaultImage&&bmp.isRecycled()){
                resetImage();
                if(url!=null)
                    WidgetImageLoader.displayImage(url, this,mImageSize,mUserParam);
            }
        }       
	    
	}
    @Override
    public void setWebImageUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String getWebImageUrl()
    {
        return url;
    }

    @Override
    public void setWebImage(String url, Bitmap bmp,int size,Object userParam)
    {
        setWebImageUrl(url);
        if(url==null)
        {
            resetImage();
        }
        else
        {
            setImageDrawable(new WebImageDrawable(bmp, this,size,userParam));
            if(bmp!=null)
            imageLoaded = true;
        }
    }

    @Override
    public void setEmptyImage()
    {
        resetImage();
    }

    @Override
    public Drawable getCurrentDrawable()
    {
        return getDrawable();
    }
}
