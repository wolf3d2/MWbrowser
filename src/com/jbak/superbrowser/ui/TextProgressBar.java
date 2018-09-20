package com.jbak.superbrowser.ui;

import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;

import ru.mail.mailnews.st;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar {  
    private String mExistText=stat.STR_NULL;
    private TextPaint mTextPaint;
    boolean enableText = false;
    int mTextPadding;
    String mDraw;
    int mDrwX=0;
    int mDrwY=0;
    public TextProgressBar(Context context) {
        super(context);
        init();
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    void init()
    {
    	mTextPadding = st.dp2px(getContext(), (int) getResources().getDimension(R.dimen.magic_padding));
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(st.dp2px(getContext(), 12));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setShadowLayer(3, 2, 2, 0xff0000ff);
    	
    }
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(enableText&&mDraw!=null)
        	canvas.drawText(mDraw, mDrwX, mDrwY, mTextPaint);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	prepareText();
    }
    public synchronized void setText(String text) {
		this.mExistText = text;
    	prepareText();
    }
    void prepareText()
    {
    	mDraw = null;
    	if(!enableText||TextUtils.isEmpty(mExistText))
    		return;
    	CharSequence ellips = TextUtils.ellipsize(mExistText, mTextPaint, getWidth()-mTextPadding*2, TruncateAt.MIDDLE);
        Rect bounds = new Rect();
        mDraw = ellips==null?mExistText:ellips.toString();
        mTextPaint.getTextBounds(mDraw, 0, mDraw.length(), bounds);
        mDrwX = getWidth() / 2 - bounds.centerX();
        mDrwY = getHeight() / 2 - bounds.centerY();
    	drawableStateChanged();
    }
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        drawableStateChanged();
    }
    public void setTextEnabled(boolean enabled)
    {
    	enableText = enabled;
    }
}