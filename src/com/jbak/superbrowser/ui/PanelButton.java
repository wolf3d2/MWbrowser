package com.jbak.superbrowser.ui;

import java.util.HashMap;

import ru.mail.mailnews.st;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.ui.UIUtils;

public class PanelButton extends FrameLayout {
	Action mAction;
	ImageView mImage;
	ImageView mSmallImage;
	ImageView mBackgroundImage;
	TextView mText;
	TextView mSmallText;
	OnAction mActionListener;
	ViewGroup mContainer;
	View mTabDecoration;
	int mMinWidth = 0;
	int mMaxWidth = Integer.MAX_VALUE;
	Context m_c = null;
	private static int mMinWidthNormal=0;
	private static int mMinWidthSmall=0;
	private static int mMinWidthMedium=0;
	public static final int TYPE_BUTTON_NORMAL = 1;
	public static final int TYPE_BUTTON_SMALL = 2;
	public static final int TYPE_BUTTON_BOOKMARK = 3;
	public static final int TYPE_BUTTON_TEXT_ONELINE = 4;
	public static final int TYPE_BUTTON_ROW = 5;
	public static final int TYPE_BUTTON_MEDIUM = 6;
	public static final int TYPE_BUTTON_MEDIUM_ONE_LINE = 7;
	public static final int TYPE_BUTTON_TEXT_ONLY = 8;
	public static final int TYPE_BUTTON_MEDIUM_BIG_WIDTH = 9;
	int mButtonType;
	public static HashMap<Integer, Integer> gButtonHeights = new HashMap<Integer, Integer>();
	public PanelButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_c = context;
		init(R.layout.panelbutton);
	}
	public PanelButton(Context context) {
		super(context);
		m_c = context;
		init(R.layout.panelbutton);
	}
	public PanelButton(Context context,int type) {
		super(context);
		m_c = context;
		mButtonType = type;
		int layoutId = R.layout.panelbutton;
		switch (mButtonType) {
		case TYPE_BUTTON_ROW:
			layoutId = R.layout.panelbutton_row;
			break;
		case TYPE_BUTTON_TEXT_ONLY:
			layoutId = R.layout.panelbutton_text;
			break;
		case TYPE_BUTTON_BOOKMARK:
			layoutId = R.layout.panelbutton_bookmark;
			break;
		case TYPE_BUTTON_MEDIUM:
		case TYPE_BUTTON_MEDIUM_BIG_WIDTH:
		case TYPE_BUTTON_MEDIUM_ONE_LINE:
			layoutId = R.layout.panelbutton_medium;
			break;
		}
		init(layoutId);
		//setButtonType(type);
	}
	@SuppressLint("NewApi")
	void init(int layoutId)
	{
		mContainer = (ViewGroup) LayoutInflater.from(getContext()).inflate(layoutId, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		addView(mContainer, lp);
		mImage = (ImageView)findViewById(R.id.image);
		mSmallImage = (ImageView)findViewById(R.id.smallImage);
		mText = (TextView)findViewById(R.id.normalText);
		mSmallText = (TextView)findViewById(R.id.smallText);
		mBackgroundImage = (ImageView)findViewById(R.id.backgroundImage);
		mTabDecoration = findViewById(R.id.tabDecoration);
		setButtonType(mButtonType);
	}
	public static int getPanelButtonHeight(Context c,PanelButton pb, int type)
	{
		Integer h = gButtonHeights.get(type);
		if(h!=null)
			return h;
		if(pb==null)
			pb = new PanelButton(c, type);
		try{
		pb.measure(0, 0);
		h = pb.getMeasuredHeight();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			return c.getResources().getDimensionPixelSize(R.dimen.panelButtonSize);
		}
		gButtonHeights.put(type, h);
		return h;
	}
	public static int getMaxWidth(Context c,int buttonType)
	{
		initValues(c);
		switch (buttonType) {
		case TYPE_BUTTON_ROW:
			return Integer.MAX_VALUE;
		case TYPE_BUTTON_SMALL:
			return mMinWidthSmall;
		case TYPE_BUTTON_NORMAL:
		case TYPE_BUTTON_TEXT_ONELINE:
			return mMinWidthNormal;
		case TYPE_BUTTON_MEDIUM:
		case TYPE_BUTTON_MEDIUM_BIG_WIDTH:	
		case TYPE_BUTTON_MEDIUM_ONE_LINE:	
			return Integer.MAX_VALUE;
		}
		return mMinWidthNormal;
	}
	static final void initValues(Context c)
	{
		if(mMinWidthNormal==0)
		{
			mMinWidthNormal = c.getResources().getDimensionPixelSize(R.dimen.panelButtonSize);
			mMinWidthSmall = c.getResources().getDimensionPixelSize(R.dimen.panelButtonIcon);
			mMinWidthMedium = c.getResources().getDimensionPixelSize(R.dimen.panelButtonMediumSize);
		}
	}
	public static int getMinWidth(Context c,int buttonType)
	{
		initValues(c);
		switch (buttonType) {
		case TYPE_BUTTON_ROW:
			return 0;
		case TYPE_BUTTON_SMALL:
			return mMinWidthSmall;
		case TYPE_BUTTON_MEDIUM:
		case TYPE_BUTTON_MEDIUM_ONE_LINE:	
			return mMinWidthMedium;
		case TYPE_BUTTON_MEDIUM_BIG_WIDTH:
			return c.getResources().getDimensionPixelSize(R.dimen.panelButtonMediumBigWidthSize);
		}
		return mMinWidthNormal;
	}
	public void setButtonType(int type)
	{
		mButtonType = type;
		if(type==TYPE_BUTTON_TEXT_ONELINE||type==TYPE_BUTTON_MEDIUM_ONE_LINE)
		{
			getTextView().setLines(1);
			getTextView().setMaxLines(1);
		}
		else if(type==TYPE_BUTTON_BOOKMARK)
		{
//			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) getLayoutParams();
//			lp.leftMargin=3;
		}
		else if(mButtonType==TYPE_BUTTON_SMALL)
		{
			mText.setVisibility(GONE);
			int pad = st.dp2px(getContext(), 4);
			getImageView().setPadding(0, pad, 0, pad);
			ViewGroup.LayoutParams lp = mContainer.getChildAt(0).getLayoutParams();
			lp.height=getPanelButtonHeight(getContext(),this,type);
		}
		int min = getMinWidth(getContext(), type);
		setMinimumWidth(min);
		mContainer.setMinimumWidth(min);
	}
	public PanelButton setAction(Action act)
	{
		if(mImage!=null)
		{
			if(act.drw!=null)
				mImage.setImageDrawable(act.drw);
			else if(act.imageRes!=0){
				st.setImageColor(m_c,mImage,act.imageRes);
			} else
				mImage.setImageBitmap(null);
		}
		setId(act.viewId);
		mText.setText(act.getText(getContext()));
		mAction = act;
		if(mButtonType!=TYPE_BUTTON_ROW&&mSmallImage!=null)
		{
			if(act.smallImageRes!=0)
				st.setImageColor(m_c,mSmallImage, act.smallImageRes);
//				mSmallImage.setImageResource(act.smallImageRes);
			else
				mSmallImage.setImageResource(0);
		}
		UIUtils.setViewsTag(act, this);
		return this;
	}
	// устанавливаем цвет пиктограммки
	public PanelButton setSmallImage(Bitmap bitmap)
	{
		mSmallImage.setImageBitmap(bitmap);
		return this;
	}
	public PanelButton setImage(Bitmap bitmap)
	{
		mImage.setImageBitmap(bitmap);
		return this;
	}
	public PanelButton setBookmark(Bookmark bm)
	{
		return setAction(Action.create(Action.ACTION_BOOKMARK, bm));
	}
	public final boolean isBookmark()
	{
		return mAction!=null&&mAction.command==Action.ACTION_BOOKMARK;
	}
	public Action getAction()
	{
		return mAction;
	}
	public void setOnActionListener(OnAction listener)
	{
		mActionListener = listener;
		if(listener!=null)
		{
			setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mActionListener!=null)
						mActionListener.onAction(mAction);
				}
			});
		}
	}
	public final ImageView getImageView()
	{
		return mImage;
	}
	public final void setBackgroundImage(Bitmap image)
	{
		mBackgroundImage.setVisibility(image==null?GONE:VISIBLE);
		mBackgroundImage.setImageBitmap(image);
	}
	public final TextView getTextView()
	{
		return mText;
	}
	public final int getButtonType() {
		return mButtonType;
	}
	public final void setTabDecoration(int resId) {
		if(mTabDecoration!=null)
			mTabDecoration.setBackgroundResource(resId);
	}
	public final void setMinAndMaxWidth(int min,int max)
	{
		mMinWidth = min;
		mMaxWidth = max;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int min = mMinWidth;
		if(min==0)
			min = getMinWidth(getContext(),mButtonType);
		int max = mMaxWidth;
		int w = getMeasuredWidth();
		if(w<min)
			w = min;
		else if(w>max)
			w = max;
	    if(getMeasuredWidth()!=w){
	        setMeasuredDimension(w, getMeasuredHeight());
	    }
	}
	public final ImageView getBackgroundImageView()
	{
		return mBackgroundImage;
	}
	public final TextView getSmallText()
	{
		return mSmallText;
	}
}
