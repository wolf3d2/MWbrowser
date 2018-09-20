package com.jbak.superbrowser.noobfuscate;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.WebViewContextMenu;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.Utils;

public class JavaScriptProcessor {

	public static final String INTERFACE_NAME = "jbakBrowserJavaScript";
	public static final String JAVASCRIPT= "javascript";
	public static final String JS_SCHEME = JAVASCRIPT+": ";
	public static final String GET_INFO = "getInfo";
	public static final String ERROR = "error";
	public static final String JSONVAL = "json:";
	public static final String JSONINFO_SELEND = "selend";
	public static final String JSONINFO_SELSTART = "selstart";
	public static final String JSONINFO_VALUE = "value";
	public static final String JSONINFO_HTML = "html";
	public static final String MYATTRIB = "jbakBrowserSelectedElement";
	public static final String MYATTRIB_VAL = "true";
	public static String page_in_str = stat.STR_NULL;
	
	public JavaScriptProcessor(MainActivity act) {
		setMain(act);
	}
	private WeakReference<MainActivity> mActivity;
	public static final int JS_GET_LONG_CLICK_INFO = 1;
	public static final int JS_MOUSE_DOWN = 2;
	public static final int JS_GET_SOURCE_CODE = 3;
	public static final int JS_SELECT_ELEMENT_TEXT = 4;
	public static final int JS_SET_VALUE = 5;
	String mLastDown1;
	String mLastDown2;
	String mRunnedJs;
	int mRunCount=0;
	
	boolean mProcessDownAsLongClick = false;
	@JavascriptInterface
	public void getInfo(int code,String info1,String info2)
	{
		
//		if(code==GET_MOUSE_DOWN)
//		{
//			Point p = mActivity.getWebView().getLastDownCoords();
//			mLastDown1 = info1+p.toString();
//			mLastDown2 = info2;
////			if(mProcessDownAsLongClick)
//				showLastDown();
//		}

		if (info1.isEmpty())
			page_in_str = stat.STR_NULL;
		else
			page_in_str = info1;
		switch (code) {
			case JS_GET_SOURCE_CODE:
				getMain().onJSProcessorEvent(code, info1);
				return;
			case JS_GET_LONG_CLICK_INFO:
				showContextMenu(info1, info2);
				return;
		}
		mRunCount = 0;
	}
	public void showAlert(String info1,String info2)
	{
		new ThemedDialog(getMain()).setAlert(info1+"\n"+info2).show();
	}
	private static String funcGetInfo(int code,String i1,String i2)
	{
		return "window."+INTERFACE_NAME+'.'+GET_INFO+funcParams(code,i1,i2);
	}
	public boolean runJavaScript(int code,Object param,MyWebView webView)
	{
		mRunnedJs = getJavaScript(code,param, webView);
		Utils.log("JsProcessor",mRunnedJs);
		mRunCount = 0;
		return runJavaScript(mRunnedJs, webView);
	}
	private boolean runJavaScript(String code,MyWebView webView)
	{
		++mRunCount;
		webView.loadUrl(mRunnedJs);
		return true;
	}
	private static String func(String jsFunc,Object ...params)
	{
		return jsFunc+funcParams(params);
	}
	private static String funcParams(Object ...params)
	{
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		int pos = 0;
		for(Object o:params)
		{
			if(o==null)
				buf.append("null");
			else
				buf.append(o.toString());
			if(pos<params.length-1)
				buf.append(',');
			++pos;
		}
		buf.append(')');
		return buf.toString();
	}
	public static final String GEN_INFO1_FOR_ELEMENT = 
			"element = document.elementFromPoint(ptX,ptY);"
			+ "info1='error';"
			+ "if(element)"
			+"{"+
				"if(element.tagName=='INPUT'||element.tagName=='TEXTAREA')"
				+ "{"
				+ " var inputInfo={"
					+JSONINFO_VALUE+":element.value,"
					+JSONINFO_HTML+":element.outerHTML,"
					+JSONINFO_SELSTART+":element.selectionStart,"
					+JSONINFO_SELEND+":element.selectionEnd"
				+ "};"
				+ "	info1 = '"+JSONVAL+"'+JSON.stringify(inputInfo);"
				+ "}"
				+ "else"
				+ "	info1 = element.outerHTML;"+
			 "}";
	public static final String JS_GET_MY_ELEMENT = "var myElement = document.querySelector(\"["+MYATTRIB+'='+MYATTRIB_VAL+"]\"); ";
	public static final String JS_REMOVE_ATTRIB = JS_GET_MY_ELEMENT+"if(myElement){myElement.removeAttribute('"+MYATTRIB+"');} ";
	public static final String JS_ADD_ATTRIB = "element.setAttribute('"+MYATTRIB+"','"+MYATTRIB_VAL+"');";
//	public static final String JS_MY_ATTRIB = stat.STR_NULL
//			+ "function addElMyAttrib(element){element.setAttribute('"+MYATTRIB+"','"+MYATTRIB_VAL+"');}; "
//			+ JS_GET_MY_ATTRIB
//			+ "function removeElMyAttrib(){var element = getElMyAttrib();if(element){element.removeAttrbute('"+MYATTRIB+"');};"
//			+ "function setElMyAttrib(element){removeElMyAttrib();addElMyAttrib(element);};"; 
	public String getJavaScript(int code,Object param,MyWebView webView)
	{
		// Вернет INPUT и содержимое текстового поля
		//			return JS_SCHEME+"window."+INTERFACE_NAME+'.'+funcGetInfo(GET_INFO_CLICK, "document.activeElement.tagName", "document.activeElement.value");
		if(code==JS_SELECT_ELEMENT_TEXT)
		{
			Point p = webView.getLastDownCoords();
			String js = "element = "+func("document.elementFromPoint",p.x,p.y)+"; if(element!=null&&window.getSelection){var selection = window.getSelection();var range = document.createRange();range.selectNodeContents(element);selection.removeAllRanges();selection.addRange(range);}";
			return JS_SCHEME+js;
			
		}
		if(code==JS_SET_VALUE)
		{
			String val = stat.STR_NULL;
			if(param instanceof String)
			{
				val = ((String)param).replace("\"", "\\\"");
				val = val.replace("\n", "\\n");
				val = val.replace("\r", stat.STR_SPACE);
			}
			val='\"'+val+'\"';
			String js = JS_GET_MY_ELEMENT+ " ; myElement.value="+val+";";
			return JS_SCHEME+js;
			
		}
		if(code==JS_GET_LONG_CLICK_INFO)
		{
			Point p = webView.getLastDownCoords();
			String js = JS_REMOVE_ATTRIB+ "var ptX="+p.x+",ptY="+p.y+";"+GEN_INFO1_FOR_ELEMENT+';'+JS_ADD_ATTRIB;
			return JS_SCHEME+js+funcGetInfo(code, "info1",p.x+"+','+"+p.y);
			
		}
		if(code==JS_GET_SOURCE_CODE)
		{
			return JS_SCHEME+funcGetInfo(code, "document.documentElement.outerHTML","1");
		}
		else if(code==JS_MOUSE_DOWN)
		{
//			String func = "function(event){element = event.target; info1 = element.tagName+' '+element.id; info2 = '';if(element == document.body) info2='Bbody'; else info2=element.outerHTML;"+funcGetInfo(GET_MOUSE_DOWN, "info1", "info2")+";}";
			//String func = "function(event){"+funcGetInfo(GET_MOUSE_DOWN, "'1'", "'2'")+";}";
//			String func = "function(event){element = event.target; info1 = element.tagName+' '+element.id; info2 = '';if(element == document.body) info2='Bbody'; else info2=element.tagName;"+funcGetInfo(GET_MOUSE_DOWN, "info1", "info2")+";}";
//			return JS_SCHEME+"document.addEventListener('mousedown', "+func+");";
			String func = "function(event){info1=event.x+','+event.y;"+funcGetInfo(JS_MOUSE_DOWN, "info1", "'1'")+";}";
			return JS_SCHEME+"document.addEventListener('mousedown', "+func+");";
		}
		return null;
	}
	public boolean hasLastDownEvent()
	{
		return mLastDown1!=null||mLastDown2!=null;
	}
	public void showLastDown()
	{
//		final HtmlContextMenu cm = new HtmlContextMenu();
//		if(cm.processHtml(mLastDown2))
//		{
//			mActivity.getWebView().post(new Runnable() {
//				
//				@Override
//				public void run() {
//					cm.showMenu(mActivity);					
//				}
//			});
//		}
//		else
			showAlert(mLastDown1, mLastDown2);
	}
	public void resetLastDown()
	{
		mProcessDownAsLongClick = false;
		mLastDown1 = mLastDown2 = null;
	}
	public static boolean isUriJavaScript(Uri uri)
	{
		return JAVASCRIPT.equals(uri.getScheme());
	}
	public void setProcessDown(boolean process)
	{
		mProcessDownAsLongClick = process;
	}
	private void showContextMenu(final String info1,String info2)
	{
		final WebViewContextMenu cm = new WebViewContextMenu(getMain());
			getMain().getWebView().post(new Runnable() {
				
				@Override
				public void run() {
					if(cm.processHtml(info1,getMain().getWebView().getUrl())){
						cm.showMenu(getMain());
					} else
						CustomPopup.toast(getMain(), R.string.context_menu_error);
				}
			});
	}
	@SuppressLint("NewApi")
	public void onConsoleMessage(ConsoleMessage msg)
	{
		
		if(!TextUtils.isEmpty(msg.message())&&msg.message().contains(GET_INFO))
		{
			Utils.log("JSError", msg.message());
			if(mRunCount<3)
			{
				try{
				getMain().getWebView().removeJavascriptInterface(INTERFACE_NAME);
				getMain().getWebView().addJavascriptInterface(this, INTERFACE_NAME);
				}
				catch(Throwable ignor)
				{}
				runJavaScript(mRunnedJs, getMain().getWebView());
			}
			else
			{
				getMain().getWebView().post(new Runnable() {
					
					@Override
					public void run() {
						new ThemedDialog(getMain()).setAlert(R.string.context_menu_error).show();
					}
				});
				
			}
		}
	}
	MainActivity getMain() {
		if(mActivity!=null)
			return mActivity.get();
		return null;
	}
	void setMain(MainActivity main) {
		this.mActivity = new WeakReference<MainActivity>(main);
	}
}
