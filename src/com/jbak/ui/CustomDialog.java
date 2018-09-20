package com.jbak.ui;

import java.lang.ref.WeakReference;

import ru.mail.mailnews.st;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;

public class CustomDialog extends Dialog implements OnClickListener{
	OnButtonClick mButtonListener;
	protected RelativeLayout mTopContainer;
	WeakReference<Context> mContextRef;
	protected TextView mDialogTitle;
	protected TextView mDialogText;
	protected EditText mText;
	protected boolean mDispatchTouchEventToParent=false;
	protected ViewGroup mExtraControls;
	protected OnUserInput mInputListener;
	int mBackColor = 0xcc000000;
	int mWindowAnim = R.style.playerControlAnim;
	protected int mDefPaddingDp = 10;
	public CustomDialog(Context context,boolean hideInput,int theme) {
		super(context ,theme);
		int si = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		if(hideInput)
			si|=WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
		getWindow().setSoftInputMode(si);
		mContextRef = new WeakReference<Context>(context);
		
	}
	public CustomDialog(Context context,int theme) {
		this(context,true,theme);
	}
	public CustomDialog(Context context) {
		this(context,R.style.CustomDialogTheme);
	}
	public CustomDialog(Context context,boolean hideInput) {
		this(context,hideInput,R.style.CustomDialogTheme);
	}
	public final CustomDialog setBackColor(int color)
	{
		mBackColor = color;
		return this;
	}
	/** Показывает сплеш */
//	public CustomDialog setSplash()
//	{
//		inflateToAppHeader(R.layout.splash_layout);
//		setOnCancelListener(new OnCancelListener() {
//			
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				if(ActivityMain.INSTANCE!=null)
//					ActivityMain.INSTANCE.finish();
//			}
//		});
//		return this;
//	}
	public final Context context()
	{
		if(mContextRef!=null&&mContextRef.get()!=null)
			return mContextRef.get();
		return getContext();
	}
	public CustomDialog setInputSelection(int start,int end)
	{
		if(mText!=null)
			mText.setSelection(start, end);
		return this;
	}
	public CustomDialog setOnButtonClick(OnButtonClick listener)
	{
		mButtonListener = listener;
		return this;
	}
	static void setTextViewText(View v,int id,String text)
	{
		View tv = v.findViewById(id);
		tv.setVisibility(text==null?View.GONE:View.VISIBLE);
		if(tv!=null&&tv instanceof TextView)
			((TextView)tv).setText(text);
	}
	protected final void createTopContainer()
	{
		mTopContainer = new RelativeLayout(context());
		mTopContainer.setId(R.id.idClose);
		mTopContainer.setOnClickListener(this);
		mTopContainer.setBackgroundColor(mBackColor);
		mTopContainer.setSoundEffectsEnabled(false);
	}
	protected View inflate(int res)
	{
		createTopContainer();
		int pad = st.dp2px(context(), mDefPaddingDp);
		mTopContainer.setPadding(pad, pad, pad, pad);
		mTopContainer.setGravity(Gravity.CENTER);
		View v = LayoutInflater.from(context()).inflate(res, null);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		v.measure(0, 0);
//		int w = v.getMeasuredWidth();
//		if(w>600)
//			lp.width = 600;
		v.setId(R.id.dialogBody);
		v.setOnClickListener(this);
		v.setHapticFeedbackEnabled(false);
		mTopContainer.addView(v,lp);
		setContentView(mTopContainer);
		return v;
	}
	protected View inflate(View v,RelativeLayout.LayoutParams showParams,int gravity)
	{
		createTopContainer();
		mTopContainer.setPadding(0, 0, 0, 0);
		mTopContainer.setGravity(gravity);
		if(showParams==null)
			mTopContainer.addView(v);
		else
			mTopContainer.addView(v,showParams);
		setContentView(mTopContainer);
		return v;
	}
	protected View inflate(View v,RelativeLayout.LayoutParams showParams)
	{
		mTopContainer = new RelativeLayout(context());
		mTopContainer.setId(R.id.idClose);
		mTopContainer.setOnClickListener(this);
		mTopContainer.setGravity(Gravity.CENTER);
		mTopContainer.setBackgroundColor(mBackColor);
		if(showParams==null)
			mTopContainer.addView(v);
		else
			mTopContainer.addView(v,showParams);
		setContentView(mTopContainer);
		return v;
	}
	public CustomDialog setDialogText(String text)
	{
		if(mDialogText!=null)
		{
			mDialogText.setVisibility(TextUtils.isEmpty(text)?View.GONE:View.VISIBLE);
			mDialogText.setText(text);
		}
		return this;
	}
	public CustomDialog setDialogTitle(int textId)
	{
		return setDialogTitle(context().getString(textId));
	}
	public CustomDialog setDialogTitle(String text)
	{
		if(mDialogTitle!=null)
		{
			mDialogTitle.setText(text);
			mDialogTitle.setVisibility(TextUtils.isEmpty(text)?View.GONE:View.VISIBLE);
		}
		return this;
	}
	public CustomDialog setCustomView(View v,RelativeLayout.LayoutParams showParams)
	{
		inflate(v, showParams);
		return this;
	}
//	public static final String getMarketUrl(boolean allApps)
//	{
//		if(AntConfig.MARKET_SAMSUNG.equals(AntConfig.MARKET))
//		{
//			if(allApps)
//				return "samsungapps://ProductDetail/com.jbak.player0";
//			return "samsungapps://ProductDetail/com.jbak.player0";
//		}
//		if(AntConfig.MARKET_AMAZON.equals(AntConfig.MARKET))
//		{
//			if(allApps)
//				return "http://www.amazon.com/gp/mas/dl/android?p=com.jbak.player0";
//			return "http://www.amazon.com/gp/mas/dl/android?p=com.jbak.player0";
//		}
//		else
//		{
//			if(allApps)
//				return "http://play.google.com/store/apps/details?id=com.jbak.player0";
//			return "http://play.google.com/store/search?q=pub:jbak";
//		}
//	}
//	public CustomDialog setAbout()
//	{
//		inflateToAppHeader(R.layout.about_dialog);
//		TextViewUrls tvAppLink = (TextViewUrls) mTopContainer.findViewById(R.id.about_app_market);
//		String am = new StrBuilder(getContext()).addLink(R.string.about_app_on_market, getMarketUrl(false)).toString();
//		tvAppLink.setHtml(am);
//		TextViewUrls tv = (TextViewUrls) mTopContainer.findViewById(R.id.about_other_desc);
//		String text = new StrBuilder(getContext())
//				.addLink(R.string.about_my_apps, getMarketUrl(true))
//				.addBr()
//				.add(R.string.about_yt_grate).addLink("YouTube Data Api", "https://developers.google.com/youtube/v3")
//				.toString();
//		tv.setHtml(text);
//		return this;
//	}
	public CustomDialog setWindowAnim(int anim) {
		mWindowAnim = anim;
		return this;
	}
	@Override
	public void show() {
		try{
			//getWindow().setWindowAnimations(mWindowAnim==0?android.R.style.Animation:mWindowAnim);

			super.show();
		}
		catch(Throwable e){}
	}
	public static String getAppVersion(Context c)
	{
        PackageManager pm = c.getPackageManager();
        try{
         return pm.getPackageInfo(c.getPackageName(), 0).versionName;
        }
        catch (Throwable e) {
        }
        return stat.STR_NULL;
	}
	public static String getAppName(Context c)
	{
		return c.getString(R.string.app_name);
	}
	public static String getAppNameAndVersion(Context c)
	{
		return c.getString(R.string.app_name)+stat.STR_SPACE+getAppVersion(c);
	}
	protected void onOk(boolean ok)
	{
		dismiss();
		if(mButtonListener!=null)
			mButtonListener.onButtonClick(ok);
	}
	protected void processId(int id)
	{
		if(id==R.id.buttonYes||id==R.id.buttonNo||id==R.id.idClose||id==R.id.buttonOk)
		{
			boolean ok = id==R.id.buttonYes||id==R.id.buttonOk;
			if(mText!=null&&mInputListener!=null)
			{
				if(ok&&mText.getText().length()<1)
				{
					CustomPopup.toast(context(), R.string.empty_edit);
					return;
				}
				dismiss();
				mInputListener.onUserInput(ok, mText.getText().toString());
				return;
			}
			onOk(ok);
		}
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.dialogBody)
			return;
		processId(v.getId());
	}
	public static interface OnButtonClick
	{
		public void onButtonClick(boolean yes);
	}
	public static interface OnUserInput
	{
		public void onUserInput(boolean ok,String newText);
	}
	@Override
	public void dismiss() {
		try{
		super.dismiss();
		}
		catch(Throwable e){}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//YouTubeApp.processActivityTouchEvent(ev);
		if(mDispatchTouchEventToParent&&context() instanceof Activity)
		{
			((Activity)context()).dispatchTouchEvent(ev);
			return false;
		}
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public void onBackPressed() {
		if(mButtonListener!=null)
			mButtonListener.onButtonClick(false);
		super.onBackPressed();
		if(st.fl_temp_hide_navigationpanel&&MainActivity.activeInstance!=null){
			
			MainActivity.activeInstance.setInterface(2);
			MainActivity.activeInstance.showMagickAndNavigation();
			st.fl_temp_hide_navigationpanel=false;
		}
	}
	public View inflateCustomView(int layoutId)
	{
		View v = LayoutInflater.from(context()).inflate(layoutId, null);
		mExtraControls.addView(v);
		mExtraControls.setVisibility(View.VISIBLE);
		return v;
	}
}
