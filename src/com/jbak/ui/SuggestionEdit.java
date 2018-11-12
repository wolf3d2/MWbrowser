package com.jbak.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class SuggestionEdit extends AutoCompleteTextView implements OnClickListener,OnItemClickListener{
	public static final int MODE_CLEAR = 0;
	public static final int MODE_VOICE = 1;
	private int mMode = MODE_VOICE;
	private ImageView mImageButton;
	OnClickListener mVoiceClickListener;
	OnAutoCompleteAction mListener;
	String mBlockFilter;
	public SuggestionEdit(Context context) {
		super(context);
		init();
	}
	public SuggestionEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	void init()
	{
		setSelectAllOnFocus(true);
		setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(event==null||event.getAction()==KeyEvent.ACTION_DOWN)
				{
					if(mListener!=null)
						mListener.onEditorAction(actionId);
					return true;
				}
				return false;
			}
		});
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onTextChanged(CharSequence text, int start,int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		setMode(getText().length()>0?MODE_CLEAR:MODE_VOICE);
		if(mListener!=null)
			mListener.onTextChanged(SuggestionEdit.this);
	}
	@Override
	public void showDropDown() {
		Drawable drw = MyTheme.get().getDropdownBackgroundDrawable(getContext());
		if(drw!=null)
			setDropDownBackgroundDrawable(drw);
		super.showDropDown();
		if(mListener!=null)
			mListener.onDropDownShown(true);
	}
	@Override
	public void dismissDropDown() {
		super.dismissDropDown();
		if(mListener!=null)
			mListener.onDropDownShown(false);
	}
	@Override
	public void setDropDownHeight(int height) {
		super.setDropDownHeight(height);
	}
	public void setBlockFilterString(String blockFilter)
	{
		mBlockFilter = blockFilter;
	}
	@Override
	public boolean enoughToFilter() {
		String txt = getText().toString();
		if(TextUtils.isEmpty(txt)||mBlockFilter!=null&&txt.equals(mBlockFilter))
			return false;
		return super.enoughToFilter();
	}
	@Override
	protected void onFocusChanged(boolean focused, int direction,Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
//		if(focused)
//		{
//			//showDropDown();
//			setMode(getText().length()>0?MODE_CLEAR:MODE_VOICE);
//			new Handler().post(new Runnable() {
//				
//				@Override
//				public void run() {
//					setSelection(0, getText().length());
//				}
//			});
//		}
		if(mListener!=null)
			mListener.onFocusChanged(this, focused);
	}
	boolean mTempFocusChange = false;
	public void setMode(int mode)
	{
		if(mMode==mode)
			return;
		mMode = mode;
//		if(mImageButton!=null)
//		{
//			mTempFocusChange = true;
//			switch (mMode) {
//			case MODE_VOICE:
//					mImageButton.setImageResource(R.drawable.search_mic);
//				break;
//			default:
//				mImageButton.setImageResource(R.drawable.clear);
//				break;
//			}
//		}
	}
	public SuggestionEdit setImageButton(ImageView button,OnClickListener onVoiceClickListener)
	{
		mVoiceClickListener = onVoiceClickListener;
		mImageButton = button;
		mImageButton.setOnClickListener(this);
		return this;
	}
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
	}
	@Override
	public void onClick(View v) {
		if(v==mImageButton)
		{
			if(mMode==MODE_CLEAR)
			{
				setText(st.STR_NULL);
			}
			else if(mVoiceClickListener!=null)
				mVoiceClickListener.onClick(v);
		}
	}
	public void setOnAutoCompleteAction(OnAutoCompleteAction listener)
	{
		mListener = listener;
	}
	public static interface OnAutoCompleteAction
	{
		public void onFocusChanged(SuggestionEdit edit, boolean focus);
		public void onTextChanged(SuggestionEdit edit);
		public void onDropDownShown(boolean show);
		public void onEditorAction(int action);
	}
	@Override
	public void onItemClick(AdapterView<?> adapt, View v, int pos, long id) {
		
		
	}
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.actionId = EditorInfo.IME_ACTION_GO;
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }
}
