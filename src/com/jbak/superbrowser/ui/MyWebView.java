package com.jbak.superbrowser.ui;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.NetworkChecker;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.WebViewEvent.EventInfo;
import com.jbak.superbrowser.utils.TempCookieStorage;
import com.jbak.utils.SameThreadTimer;
import com.mw.superbrowser.R;

public class MyWebView extends WebView {
//	String codepage = null;
	boolean page_boundary = false;
	public static SameThreadTimer m_tm = null;
	Context m_c = null;
	MyWebView inst = null;
	GestureDetector gestureDetector = null; 
	private EventInfo eventInfo;
	boolean mForceSelectText;
	Point mLastDownCoords = new Point();
	private OnLongClickListener mContextMenuHandler;
	WeakReference<InputConnection> mInputConnection;
	public static final int DELAY_SAVE_EVENT = 500;
	float mDensity = 1f;
	OnLongClickListener mLongClickListener;
	
	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public MyWebView(Context context) {
		super(context);
		init(context);
	}
	void init(Context c)
	{
		m_c = c;
		eventInfo = new EventInfo();
		eventInfo.setWebView(this);
		mDensity = c.getResources().getDisplayMetrics().density;
		inst = this;
		inst.setBackgroundColor(Prefs.getWWBackgroundColor());
		gestureDetector =  new GestureDetector(c, simpleongesturelistener);

	}
	Point pt_last = new Point(0,0);
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (st.pref_navigation==2&&gestureDetector.onTouchEvent(event))
			return true;
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			pt_last.set((int)event.getX(), (int)event.getY());
			mLastDownCoords.set(contentCoord(event.getX()), contentCoord(event.getY()));
			//mLastDownCoords.set((int)event.getX(), (int)event.getY());
		}
    	page_boundary=false;
		boolean ret = super.onTouchEvent(event);
		return ret;
	}
	public int contentCoord(float screenCoord)
	{
		@SuppressWarnings("deprecation")
		float sc = getScale();
		float f = screenCoord;///mDensity;
		if(sc!=0)
			f = f/sc;
		return (int)f;
	}
	public Point getLastDownRealCoords()
	{
		return pt_last;
	}
	public Point getLastDownCoords()
	{
		return mLastDownCoords;
	}
//	public void setLongClickHandler(OnLongClickListener handler)
//	{
//		//setLongClickable(false);
//		mContextMenuHandler = handler;
//	}
//	@Override
//	public boolean showContextMenu() {
//		if(mContextMenuHandler!=null)
//		{
//			mContextMenuHandler.onLongClick(this);
//			return true;
//		}
//		return super.showContextMenu();
//	}
	@Override
	protected void onScrollChanged(int ll, int tt, int oldl, int oldt) {
		// если есть скроллинг
//		if ()
		page_boundary = true;
		super.onScrollChanged(ll, tt, oldl, oldt);
	}
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mLongClickListener = l;
		super.setOnLongClickListener(l);
	}
//	@Override
//	public ActionMode startActionMode(Callback callback) {
//		return super.startActionMode(callback);
//	}
	@Override
	public void loadUrl(String url) {
		eventInfo.isReload = false;
		checkCacheMode();
		TempCookieStorage.onStartRequest(this, url);
		if (url.startsWith(st.STR_FILE)){
//			if (codepage==null||codepage.isEmpty())
//				codepage = IConst.UTF8;
			loadDataWithBaseURL(url, st.fileToStr(url.substring(6)), "text/html", IConst.UTF8, url);
//			codepage = null;
			return;
		}
		super.loadUrl(url);
	}
	public void checkCacheMode()
	{
		WebSettings ws = getSettings();
		if(!eventInfo.isReload&&!NetworkChecker.inetAvaliable)
			ws.setBlockNetworkLoads(true);
		else
			ws.setBlockNetworkLoads(false);
	}
	public final EventInfo getEventInfo()
	{
		return eventInfo;
	}
	@Override
	public void reload() {
		eventInfo.isReload = true;
		checkCacheMode();
		super.reload();
	}
	public final boolean isReload()
	{
		return eventInfo.isReload;
	}
	public void forceSelectText()
	{
	    try {
	        // ICS
	            WebView.class.getMethod("selectText").invoke(this);
	        } catch (Exception e1) {
	        try {
	        	//loadUrl("javascript:android.selection.longTouch();");
//	        	KeyEvent shiftPressEvent = new KeyEvent(0,0,
//	                     KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0,0);
//	            shiftPressEvent.dispatch(this);
//	            Method m = WebView.class.getMethod("emulateShiftHeld", (Class[])null);
//	            m.invoke(this, (Object[])null);
	        } catch (Exception e2) {
	            // fallback
	        }
	    }
//		final OnLongClickListener ex = mLongClickListener;
//		setOnLongClickListener(null);
//		showContextMenu();
//		postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				setOnLongClickListener(ex);
//			}
//		}, 1000);
//		 try {
//		        // ICS
//		        WebView.class.getMethod("selectText").invoke(this);
//		     } 
//		 	catch (Throwable e1) 
//		 {
//		 		e1.printStackTrace();
//		 }

	}
	public final InputConnection getInputConnection()
	{
		if(mInputConnection==null)
			return null;
		return mInputConnection.get();
	}
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		InputConnection input = super.onCreateInputConnection(outAttrs);
		if(input!=null)
			mInputConnection = new WeakReference<InputConnection>(input);
		return input;
	}
	final boolean isMyEvent(EventInfo info)
	{
		return eventInfo == info;
	}
	@SuppressLint("NewApi")
	public void setScaleX(float scaleX) {
		super.setScaleX(scaleX);
	}
	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		try{
		super.onSizeChanged(w, h, ow, oh);
		}
		catch(Throwable ignor)
		{}
	}
	@Override
	public void computeScroll() {
		try{
			super.computeScroll();
		}
		catch(Throwable e)
		{}
	}
	// обработка жестов
	   SimpleOnGestureListener simpleongesturelistener = new SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return super.onDoubleTap(e);
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velX,
					float velY) {
				// процент области экрана справа и лева для горизонтальных жестов
				int AREAL_LENGTH_PROC = 50;
				// области от края экрана в процентах - если жест в их пределат, то обрабатывать
				int ar_l= (int)((st.getDisplayWidth(m_c)/100)*AREAL_LENGTH_PROC);
				int ar_r= (int)st.getDisplayWidth(m_c)-((st.getDisplayWidth(m_c)/100)*AREAL_LENGTH_PROC);
	        	
				int begX = (int)e1.getX();
	            int begY = (int)e1.getY();
	            float dx = e2.getX()-e1.getX();
	            float dy = e2.getY()-e1.getY();
	            float mdx = Math.abs(dx);
	            float mdy = Math.abs(dy);
	            
//	            st.toastLong("velX= "+velX
//	            		  +"\nvelY= "+velY
//	            		  +"\nmdX= "+mdx
//	            		  +"\nmdY= "+mdy
//	            		  );
	            // жесты:
	            // от левого края экрана к правому
	            if (begX < ar_l&&mdy<150&&Math.abs(velX)>700&&!page_boundary){
//	            if (!canScrollHorizontally(-1)
//	            		){
	            	if (inst.canGoBack()){
	            		
	                	inst.clearView();//.loadUrl(MainActivity.ABOUT_BLANK);
	                	inst.goBack();
	            	}
	            	else
	            		st.toast(R.string.page_tab_first);
	            	//page_boundary=false;
	            }
	            // от правого к левому
	            else if (begX > ar_r&&mdy<150&&Math.abs(velX)>700&&!page_boundary){
//	            else if (!canScrollHorizontally(1)){
	            	if (inst.canGoForward())
	            		inst.goForward();
	            	else
	            		st.toast(R.string.page_tab_last);
	            	//page_boundary=false;
	            }
	            // снизу вверх
	            else if (dy<=0&&Math.abs(velY)>800&&mdy>300){
	            	showNavigationButton(false);
	            }
	            // сверху вниз
	            else if (dy>0&&Math.abs(velY)>800&&mdy>300){
	            	showNavigationButton(true);
	            }
            	page_boundary=false;
            	//BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.GO_BACK));
				return super.onFling(e1, e2, velX, velY);
			}

			@Override
			public void onLongPress(MotionEvent e) {
				super.onLongPress(e);
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				return super.onSingleTapConfirmed(e);
			}
		};
	public void showNavigationButton(final boolean updown)
	{
		if (m_c==null)
			return;
		final MainActivity ma = (MainActivity)m_c;
		if (ma.round_btn == null)
			return;
		if (updown){
			ma.round_btn.setText(" ∆ ");
		} else {
			ma.round_btn.setText(" ∇ ");
		}
		ma.round_btn.setTextSize(25);
		if (Prefs.getWWDarkWhiteTheme()==0){
			ma.round_btn.setTextColor(Color.WHITE);
			ma.round_btn.setBackgroundResource(R.drawable.round_button_cyan);
		} else {
			ma.round_btn.setTextColor(Color.WHITE);
			ma.round_btn.setBackgroundResource(R.drawable.round_button_dark_gray);
		}
		if(m_tm!=null)
		    m_tm.cancel();
		m_tm = new SameThreadTimer(2000,0)
		{
		    @Override
		    public void onTimer(SameThreadTimer timer)
		    {
		        if (ma.round_btn!=null)
		        	ma.round_btn.setVisibility(View.GONE);
		        m_tm.cancel();
		    }
		};
		m_tm.start();
		ma.round_btn.setVisibility(View.VISIBLE);
	}

}