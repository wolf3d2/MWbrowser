package ru.mail.webimage.widgets;

import ru.mail.webimage.widgets.BgImgDrawable.OnSetImageBitmapListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class BgImgContainer extends FrameLayout implements OnSetImageBitmapListener {

	private ScaleType imgScaleType;

	public final static int SMALL = 0;
	public final static int BIG = 1;
	public final static int NONE = 2;
	/** Картинка без рамки */
	public final static int TRANSPARENT = 3;
	/**
	 * Показываем только картинку, если есть. Крутяшку загрузки и ошибку
	 * сетевого соединения не показываем
	 */
	public final static int IMAGE_ONLY = 4;
	/**
	 * Показываем картинку или сообщение об отсутствии подключения. Крутяшку
	 * загрузки не показываем
	 */
	public final static int IMAGE_OR_NO_CONNECT = 5;
	/** То же, что и BIG, но с выравниванием по экрану */
	public final static int BIG_FIT_SCREEN = 6;

	protected BgImgDrawable innerDrawable;
	protected View innerProgres;
	// private ProgressBar progressBar;
	View noConnect;
	protected int type = TRANSPARENT;
	int minHeight = 0;
	private int innerImgHeight = FrameLayout.LayoutParams.MATCH_PARENT;
	private int innerImgWidth = FrameLayout.LayoutParams.MATCH_PARENT;

	public BgImgContainer(Context context) {
		super(context);
		type = getDefaultType();
		init(context);
	}

	public BgImgContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	void init(Context context) {
		innerDrawable = new BgImgDrawable(this, type);
		innerDrawable.setScaleType(getScaleType());
		innerDrawable.setOnSetImageBitmapListener(this);

		addView(innerDrawable, new FrameLayout.LayoutParams(innerImgWidth, innerImgHeight));

		innerProgres = createProgressView();
		addView(innerProgres, createProgressLayoutParams());
		if (type == IMAGE_ONLY || type == IMAGE_OR_NO_CONNECT)
			innerProgres.setVisibility(View.GONE);
		noConnect = new FrameLayout(context);
		noConnect.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		addView(noConnect);
		noConnect.setVisibility(View.GONE);
	}

	public ImageView getImageView() {
		return innerDrawable;
	}

	public View createProgressView() {
		ImageView iv = new ImageView(getContext());
		return iv;
	}

	public FrameLayout.LayoutParams createProgressLayoutParams() {
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		return lp;
	}

	public void setNoConnect() {
		innerDrawable.resetImage();
		innerProgres.setVisibility(View.GONE);
		noConnect.setVisibility(type == IMAGE_ONLY ? View.GONE : View.VISIBLE);

	}

	public void resetImage(boolean now) {
		noConnect.setVisibility(View.GONE);
		if (now)
			innerDrawable.resetImageNow();
		else
			innerDrawable.resetImage();
		innerProgres.setVisibility((type == IMAGE_ONLY || type == IMAGE_OR_NO_CONNECT) ? View.GONE : View.VISIBLE);
	}

	public void resetImage() {
		resetImage(false);
	}

	public void recycle() {
		if (innerDrawable != null)
			innerDrawable.recycle(false);
	}
	@Override
	protected int getSuggestedMinimumHeight() {
		if(minHeight>0)
			return minHeight;
		return super.getSuggestedMinimumHeight();
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		widthMeasureSpec = innerDrawable.getBigWidthMeasureSpec(widthMeasureSpec);
//		minHeight = innerDrawable.getMinHeight(widthMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public int getDefaultType() {
		return SMALL;
	}

	public Bitmap getBitmap() {
		if (innerDrawable == null)
			return null;
		Drawable drw = innerDrawable.getDrawable();
		if (drw == null || !(drw instanceof BitmapDrawable))
			return null;
		return ((BitmapDrawable) drw).getBitmap();
	}

	public String getUrl() {
		if (innerDrawable == null)
			return null;
		return innerDrawable.getWebImageUrl();
	}

	public void setScaleType(ScaleType type) {
		if (innerDrawable != null)
			innerDrawable.setScaleType(type);
	}

	public ScaleType getScaleType() {
		if (imgScaleType != null) {
			return imgScaleType;
		}

		if (type == TRANSPARENT)
			return ScaleType.FIT_CENTER;
		return ScaleType.CENTER_CROP;
	}

	public boolean isImageLoaded() {
		return innerDrawable.imageLoaded;
	}

	@Override
	public void onSetImageBitmap() {
		innerProgres.setVisibility(View.GONE);
		noConnect.setVisibility(View.GONE);
	}

	public void setImageBackground(BgImgDrawable drw) {
		drw.initBackground();
	}

	public void setDefaultImage(BgImgDrawable drw) {
		drw.initDefaultImage();
	}
}
