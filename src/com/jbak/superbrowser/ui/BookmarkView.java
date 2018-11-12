package com.jbak.superbrowser.ui;

import ru.mail.mailnews.st;
import ru.mail.webimage.widgets.BgImgContainer;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbak.superbrowser.Bookmark;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.DateToString;

public class BookmarkView extends FrameLayout{
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_SQUARE = 1;
	public static final int TYPE_SETTINGS = 3;
	public static final int TYPE_SMALL = 4;
	public static int BookmarkViewType = 0;
	TextView mBigText;
	TextView mNormalText;
	TextView mDate;
	TextView mShortText;
	ImageView mFavIcon;
	BgImgContainer mThumbnail;
	ImageView mClose;
	Bookmark mBm;
	OnCloseListener mCloseListener;
	TextView[]mTextViews;
	int mType=TYPE_DEFAULT;
	
	public BookmarkView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(BookmarkViewType);
	}
	public BookmarkView(Context context,int viewType) {
		super(context);
		init(viewType);
	}
	public BookmarkView(Context context) {
		super(context);
		init(BookmarkViewType);
	}
	private  final int getLayoutId(int type)
	{
		switch (type) {
		case TYPE_SQUARE:
			return R.layout.bookmark_view_square;
		case TYPE_SETTINGS:
			return R.layout.bookmark_view_settings;
		}
		return R.layout.bookmark_view;
	}
	void init(int type)
	{
		mType = type;
		LayoutInflater.from(getContext()).inflate(getLayoutId(mType), this);
		mBigText = (TextView)findViewById(R.id.bigText);
		mNormalText = (TextView)findViewById(R.id.normalText);
		mDate = (TextView)findViewById(R.id.dateTime);
		mFavIcon = (ImageView)findViewById(R.id.favicon);
		mThumbnail = (BgImgContainer)findViewById(R.id.thumbnail);
		mClose = (ImageView)findViewById(R.id.close);
		mShortText = (TextView)findViewById(R.id.shortText);
		mTextViews = new TextView[]{mBigText,mNormalText,mDate,mShortText};
		setThumbnail(null);
	}
	public void setFavIcon(Bitmap bmp)
	{
		mFavIcon.setImageBitmap(bmp);
	}
	public final BgImgContainer getThumbnailView()
	{
		return mThumbnail;
	}
	public final ImageView getFaviconView()
	{
		return mFavIcon;
	}
	public void setThumbnail(Bitmap bmp)
	{
		if(mThumbnail==null)
			return;
		int invis = mType==TYPE_SETTINGS?GONE:VISIBLE;
		mThumbnail.setVisibility(bmp==null?invis:View.VISIBLE);
		if(bmp!=null)
			mThumbnail.getImageView().setImageBitmap(bmp);
	}
	public Bookmark getBookmark()
	{
		return (Bookmark)getTag();
	}
	public void setBookmark(Bookmark bm,int pos,boolean isCurPos)
	{
//		if (bm==null)
//			return;
		String text = bm.getTitle();
		String url = null;
		if(bm.getUrl()!=null)
			url = stat.decode(bm.getUrl());
		else
			url = st.STR_NULL;
		if(TextUtils.isEmpty(text))
			text = url;
		mBigText.setText(bm.getTitle());
		mNormalText.setText(url);
		if(bm.date>0)
			mDate.setText(DateToString.getDateString(bm.date));
		else
			mDate.setText(null);
		UIUtils.setViewsTag(bm, this);
		
		MyTheme.get().setBookmarkView(this, pos, isCurPos);
		if(bm.imageRes!=0)
		{
			mThumbnail.getImageView().setImageResource(bm.imageRes);
			mThumbnail.setVisibility(VISIBLE);

		}
	}
	public void setOnCloseListener(OnCloseListener listener)
	{
		mCloseListener = listener;
		mClose.setVisibility(View.VISIBLE);
		mClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCloseListener!=null)
					mCloseListener.onClose(BookmarkView.this);
			}
		});
	}
	public final TextView[]getTextViews()
	{
		return mTextViews;
	}
	public final TextView getShortTextView()
	{
		return mShortText;
	}
	public final TextView getNormalTextView()
	{
		return mNormalText;
	}
	public final ImageView getClose()
	{
		return mClose;
	}
	public void setType(int type)
	{
		mType = type;
		if(mType==TYPE_SMALL)
		{
			int sz = st.dp2px(getContext(), 64);
			ViewGroup.LayoutParams lp = mThumbnail.getLayoutParams();
			lp.width = sz;
			lp.height = sz;
		}
	}
	public static interface OnCloseListener
	{
		void onClose(BookmarkView bw);
	}
	public final int getType()
	{
		return mType;
	}
}
