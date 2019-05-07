package com.jbak.superbrowser.ui;

import java.net.URI;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.MainActivityRef;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.SearchAction;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.UrlProcess;
import com.jbak.superbrowser.noobfuscate.JavaScriptProcessor;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.utils.Utils;

@SuppressLint("Recycle")
public class WebViewContextMenu extends MainActivityRef implements OnAction{
	public static final int TYPE_DEFAULT = 1;
	public static final int TYPE_INPUT = 2;
	public static final String TAG_A = "a";
	public static final String TAG_IMG = "img";
	public static final String ATT_HREF = "href";
	public static final String ATT_SRC = "src";
	public static final String ATT_DATA_SRC = "data-src";
	public static final String ATT_DATA_HREF = "data-href";
	public static final String ATT_TEXT_LINK= "\">";
	/** url играющего видео потока */
	String vUrl = null;
	int mType = TYPE_DEFAULT;
	String mHtmlText;
	String mText;
	String mBaseUrl;
	String tempbaseurl = null;
	Element mLink;
	Element mImg;
	HitTestResult  mLastHitResult;
	InputInfo mInputInfo;
	public WebViewContextMenu(MainActivity main)
	{
		super(main);
	}
	public boolean processHtml(String html,String baseUrl)
	{
		mBaseUrl = baseUrl;
		Utils.log("ContextMenu", mBaseUrl);
		Utils.log("ContextMenu", html);
		try{
			if(html.startsWith(JavaScriptProcessor.JSONVAL))
			{
				mType = TYPE_INPUT;
				String json = html.substring(JavaScriptProcessor.JSONVAL.length());
				JSONObject jo = new JSONObject(json);
				mHtmlText = jo.optString(JavaScriptProcessor.JSONINFO_HTML);
				mText = jo.optString(JavaScriptProcessor.JSONINFO_VALUE);
				int ss = jo.optInt(JavaScriptProcessor.JSONINFO_SELSTART, -1);
				if(ss>-1)
					mInputInfo = new InputInfo(ss,jo.optInt(JavaScriptProcessor.JSONINFO_SELEND, -1));
				else
					mInputInfo = null;
			}
			else
			{
				mHtmlText = html;
				mText = Html.fromHtml(mHtmlText).toString();
			}
			if(TextUtils.isEmpty(mHtmlText))
				return false;
			vUrl = null;
			Document doc = Jsoup.parse(mHtmlText, mBaseUrl);
			if(!TextUtils.isEmpty(mText))
				mText = mText.trim();
			String tname= null;
			Elements el;
			Elements allTags = doc.getAllElements();
			for(Element t:allTags)
			{
				tname = t.tagName();
				if(TAG_A.equalsIgnoreCase(t.tagName()))
					mLink = t;
				else if(TAG_IMG.equalsIgnoreCase(t.tagName()))
					mImg = t;
				else if("video".equalsIgnoreCase(t.tagName())) {
					el = t.getElementsByAttribute("src");
					vUrl = el.attr("src");
				}
				else if("iframe".equalsIgnoreCase(t.tagName())) {
					el = t.getElementsByAttribute("src");
					vUrl = el.attr("src");
				}
			}
			return true;
		}
		catch (Throwable e) {
		}
		return false;
	}
	/** запуск внешнего видеоплеера для воспроизведения видео */
	public void startExternalVideoPlayer() {
		if (vUrl==null)
			return;
        Uri uri = Uri.parse(vUrl);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");
        try {
            getMain().startActivity(intent);
    		getMain().getWebView().reload();
			
		} catch (Throwable e) {
			st.toastLong(R.string.error_start_ext_player);
		}
		
	}
	public String normalizeUrl(String url)
	{
		if(TextUtils.isEmpty(url))
			return null;
		if(url.indexOf(':')<0&&mBaseUrl!=null)
		{
			URI uri = URI.create(mBaseUrl);
			URI res=null;
			try {
				res = uri.resolve(url);
			} catch (Throwable e) {
				return st.STR_NULL;
	        }
			return res.toString();
		}
		return url;
	}
	/** суперменю при долгом тапе на ссылке */
	public boolean showSuperMenu(MainActivity a)
	{
		setMain(a);
		if(mType==TYPE_INPUT)
		{
			showInputEditor();
			return true;
		}
		ActArray ar = getSuperMenuItemPos(a);
// создаём меню для долгого нажатия на ссылку
		new MenuPanelButton(a, ar, this).show();
		return true;
	}
	/** вщзвращает выбранный в настройках набор расположения кнопок в супер меню */
	private ActArray getSuperMenuItemPos(MainActivity a)
	{
		ActArray ar = new ActArray();
		boolean itemtrans = false;
		String url=st.STR_NULL;
		String turl=st.STR_NULL;
		HitTestResult HitResult = null;
		switch (Prefs.get().getInt(Prefs.SUPERMENU_BUTTON_SET, 0))
		{
		// для экранов 8" под правую руку
		case 1:
			if(mLink==null&&mImg==null)
			{
				HitResult = getMain().getWebView().getHitTestResult();
				if(HitResult.getType()==HitTestResult.SRC_ANCHOR_TYPE||HitResult.getType()==HitTestResult.IMAGE_TYPE||HitResult.getType()==HitTestResult.IMAGE_ANCHOR_TYPE||HitResult.getType()==HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
					url = HitResult.getExtra();
				if(!TextUtils.isEmpty(url))
				{
					ar.add(Action.create(Action.GO,url));
					if (mText!=null&&mText.length()>0)
						ar.add(Action.create(Action.COPY_TEXT_URL_TO_CLIPBOARD,mText));
					ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,url));
					ar.add(Action.create(Action.SHARE_ELEMENT,url));
					ar.add(Action.create(Action.NEW_TAB,url));
					ar.add(Action.create(Action.BACKGROUND_TAB,url));
				}
			}

			if(mLink!=null)
			{
				url = mLink.attr(ATT_HREF);
				if(TextUtils.isEmpty(url))
					url = mLink.attr(ATT_DATA_HREF);
				url = normalizeUrl(url);
				if(!TextUtils.isEmpty(url))
				{
					ar.add(Action.create(Action.GO,url));
					if (mText!=null&&mText.length()>0)
						ar.add(Action.create(Action.COPY_TEXT_URL_TO_CLIPBOARD,mText));
					ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,url));
					ar.add(Action.create(Action.SHARE_ELEMENT,url));
					ar.add(Action.create(Action.NEW_TAB,url));
					ar.add(Action.create(Action.BACKGROUND_TAB,url));
					ar.add(Action.create(Action.SEARCH_ON_PAGE));
					ar.add(Action.create(Action.SAVEFILE,url).setText(R.string.act_save_link));
//					ar.add(Action.create(Action.SELECT_TEXT));
//					Stat.url = url;
//					ar.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));
					itemtrans = true;
				}
			}
			if(mImg!=null)
			{
				url = mImg.attr(ATT_SRC);
				if(!TextUtils.isEmpty(url))
				{
					url = normalizeUrl(url);
					Action aa = Action.create(Action.SAVEFILE,url).setText(R.string.act_save_image).setImageRes(R.drawable.images);
					aa.smallImageRes = R.drawable.sdcard;
					ar.add(aa);
					aa = Action.create(Action.NEW_TAB,url).setImageRes(R.drawable.images).setText(R.string.act_open_image);
					aa.smallImageRes = 0;
					ar.add(aa);
//					ar.add(Action.create(Action.SHARE_ELEMENT,url));
				}
			}
			if(!TextUtils.isEmpty(mText)){
				if (mBaseUrl!=null&&mBaseUrl.length()>0&&itemtrans==false){
					stat.url = mBaseUrl;
//					ar.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));
					ar.add(Action.create(Action.SEARCH_ON_PAGE));
//					if (mBaseUrl.startsWith("file://"))
//						ar.add(Action.create(Action.CODEPAGE,url));
					
				}
				ar.add(Action.create(Action.ITEM_TEXT,mText));
			}
			ar.add(Action.create(Action.SHARE_URL,mBaseUrl));
			ar.add(Action.create(Action.QUICK_SETTINGS));
			ar.add(Action.create(Action.TRANSLATE_LINK));
			ar.add(Action.create(Action.SELECT_TEXT));
			ar.add(Action.create(Action.SOURCE_CODE));
			if(vUrl!=null) {
				ar.add(Action.create(Action.EXTERNAL_VIDEO_PLAYER));
				ar.add(Action.create(Action.COPY_NET_STRIMING_URL));
			}
			break;
		default:
			if(mLink==null&&mImg==null)
			{
				HitResult = getMain().getWebView().getHitTestResult();
				if(HitResult.getType()==HitTestResult.SRC_ANCHOR_TYPE||HitResult.getType()==HitTestResult.IMAGE_TYPE||HitResult.getType()==HitTestResult.IMAGE_ANCHOR_TYPE||HitResult.getType()==HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
					url = HitResult.getExtra();
				if(!TextUtils.isEmpty(url))
				{
					ar.add(Action.create(Action.GO,url));
					ar.add(Action.create(Action.NEW_TAB,url));
					ar.add(Action.create(Action.BACKGROUND_TAB,url));
					ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,url));
					ar.add(Action.create(Action.SHARE_ELEMENT,url));
					
				}
			}

			if(mLink!=null)
			{
				url = mLink.attr(ATT_HREF);
				if(TextUtils.isEmpty(url))
					url = mLink.attr(ATT_DATA_HREF);
				url = normalizeUrl(url);
				if(!TextUtils.isEmpty(url))
				{
					ar.add(Action.create(Action.GO,url));
					ar.add(Action.create(Action.NEW_TAB,url));
					ar.add(Action.create(Action.BACKGROUND_TAB,url));
					if (mText!=null&&mText.length()>0)
						ar.add(Action.create(Action.COPY_TEXT_URL_TO_CLIPBOARD,mText));
					ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,url));
					ar.add(Action.create(Action.SHARE_ELEMENT,url));
					ar.add(Action.create(Action.SAVEFILE,url).setText(R.string.act_save_link));
					ar.add(Action.create(Action.SEARCH_ON_PAGE));
					if(vUrl!=null) {
						ar.add(Action.create(Action.EXTERNAL_VIDEO_PLAYER));
						ar.add(Action.create(Action.COPY_NET_STRIMING_URL));
					}
//					ar.add(Action.create(Action.SELECT_TEXT));
//					Stat.url = url;
//					ar.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));
					itemtrans = true;
				}
			}
			if(mImg!=null)
			{
				url = mImg.attr(ATT_SRC);
				if(!TextUtils.isEmpty(url))
				{
					url = normalizeUrl(url);
					Action aa = Action.create(Action.SAVEFILE,url).setText(R.string.act_save_image).setImageRes(R.drawable.images);
					aa.smallImageRes = R.drawable.sdcard;
					ar.add(aa);
					aa = Action.create(Action.NEW_TAB,url).setImageRes(R.drawable.images).setText(R.string.act_open_image);
					aa.smallImageRes = 0;
					ar.add(aa);
//					ar.add(Action.create(Action.SHARE_ELEMENT,url));
				}
			}
			if(!TextUtils.isEmpty(mText)){
				if (mBaseUrl!=null&&mBaseUrl.length()>0&&itemtrans==false){
					stat.url = mBaseUrl;
//					ar.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));
					ar.add(Action.create(Action.SEARCH_ON_PAGE));
//					if (mBaseUrl.startsWith("file://"))
//						ar.add(Action.create(Action.CODEPAGE,url));
					
				}
				ar.add(Action.create(Action.ITEM_TEXT,mText));
			}
			ar.add(Action.create(Action.SHARE_URL,mBaseUrl));
			ar.add(Action.create(Action.QUICK_SETTINGS));
			ar.add(Action.create(Action.SOURCE_CODE));
			ar.add(Action.create(Action.TRANSLATE_LINK));
			ar.add(Action.create(Action.SELECT_TEXT));
//			if(mImg==null)
//				ar.add(Action.create(Action.SELECT_TEXT));
			if(vUrl!=null) {
				ar.add(Action.create(Action.EXTERNAL_VIDEO_PLAYER));
				ar.add(Action.create(Action.COPY_NET_STRIMING_URL));
			}
		}
		
		return ar;
	}
	private void showInputEditor() {
		final DialogEditor de = new DialogEditor(getMain(), getMain().getString(R.string.act_item_text), mText,false)
		{
			@Override
			public void createDefaultActions(ActArray act) {
				super.createDefaultActions(act);
				Action del = act.removeAction(Action.STOP);
				if(mEdit.length()==0)
					act.add(0,Action.create(Action.PASTE));
				else
					act.add(0,Action.create(Action.CLEAR_TEXT));
				act.add(0,Action.create(Action.APPLY_TEXT));
				act.insertAction(del, Action.COPY_URL_TO_CLIPBOARD, false);
				act.add(Action.create(Action.SOURCE_CODE));
			}
		};
		if(mInputInfo!=null&&mInputInfo.selStart>-1)
		{
			EditText ed = de.getEdit(); 
			if(mInputInfo.selStart>ed.length())
				mInputInfo.selStart = ed.length();
			if(mInputInfo.selEnd>ed.length())
				mInputInfo.selEnd = ed.length();
			if(mInputInfo.selEnd<0)
				ed.setSelection(mInputInfo.selStart);
			else
				ed.setSelection(mInputInfo.selStart,mInputInfo.selEnd);
		}
		de.setOnActionListener(new OnAction() {
			
			@Override
			public void onAction(Action act) {
				switch (act.command) {
				case Action.APPLY_TEXT:
					String text = de.getText();
					de.dismiss();
					getMain().getJavaScriptProcssor().runJavaScript(JavaScriptProcessor.JS_SET_VALUE, text,getMain().getWebView());
					break;
				case Action.CLEAR_TEXT:
					de.getEdit().setText(null);
					
					break;
				case Action.SOURCE_CODE:
					showSourceCode();
					break;
				default:
					de.onAction(act);
					break;
				}
			}
		});
		de.show();
	}
	public void showSourceCode()
	{
		new DialogEditor(getMain(), getMain().getString(R.string.act_source_code), mHtmlText).show();
	}
	@Override
	public void onAction(Action act) {
		switch (act.command) {
		case Action.EXTERNAL_VIDEO_PLAYER:
			startExternalVideoPlayer();
			return;
		case Action.COPY_NET_STRIMING_URL:
			if(vUrl!=null)
				stat.setClipboardString(getMain(), (String)vUrl);	
			return;
		case Action.TRANSLATE_LINK:
			getMain().runAction(act);
			return;
		case Action.SEARCH_ON_PAGE:
			getMain().activeInstance.startSearchPage(act);
			return;
		case Action.SOURCE_CODE:
			showSourceCode();
			return;
		case Action.ITEM_TEXT:
			new DialogEditor(getMain(), getMain().getString(R.string.act_item_text), mText).show();	
			return;
		case Action.COPY_TEXT_URL_TO_CLIPBOARD:
		case Action.COPY_URL_TO_CLIPBOARD:
			stat.setClipboardString(getMain(), (String)act.param);	
			return;
		case Action.GO:
			getMain().getWebView().loadUrl((String)act.param);
			return;
		case Action.BACKGROUND_TAB:
			getMain().openUrl((String)act.param, act.command);
			return;
		case Action.NEW_TAB:
			getMain().openUrl((String)act.param,act.command);
			return;	
		case Action.SELECT_TEXT:
			stat.fl_one_select = true;
			MyWebView mwv = getMain().getWebView();
			Point pt  = mwv.getLastDownRealCoords();
			mwv.getLocationOnScreen(new int[] {pt.x,pt.y});

//			// MotionEvent parameters
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis();
			int action = MotionEvent.ACTION_DOWN;
			int x = pt.x;
			int y = pt.y;
			int metaState = 0;

			// dispatch the event
			MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
			mwv.dispatchTouchEvent(event);

//			mwv..dispatchTouchEvent(MotionEvent event);
			//getMain().getWebView().performLongClick();
			//BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
			return;
		case Action.SAVEFILE:
			String url = (String) act.param;
			UrlProcess.forceDownload(getMain(), Uri.parse(url), true);
			break;
		case Action.SHARE_ELEMENT:
		case Action.SHARE_URL:
			getMain().share((String)act.param,st.STR_NULL);
			return;
		case Action.QUICK_SETTINGS:
			getMain().runAction(act);
			return;
		default:
			break;
		}
		if(act instanceof SearchAction)
		{
			
			((SearchAction)act).doAction(getMain(),"bbb", (String) act.param);
			return;
		}
//		if(act.command==Action.NEW_WINDOW||act.command==Action.IMAGES_ENABLED)
//		{
//			mAct.openUrlInNewWindow((String)act.param);
//		}
//		if(act.command==Action.SELECT_TEXT)
//		{
//			//mAct.getJavaScriptProcssor().runJavaScript(JavaScriptProcessor.GET_INFO_SELECT_TEXT, mAct.getWebView());
//			mAct.getWebView().forceSelectText();
//		}
//		
	}
	MyHandler mHandler;
	public boolean checkLongClick(MainActivity a)
	{
		setMain(a);
		mLastHitResult = a.getWebView().getHitTestResult();
		if(mLastHitResult.getType()==HitTestResult.SRC_ANCHOR_TYPE||mLastHitResult.getType()==HitTestResult.IMAGE_TYPE||mLastHitResult.getType()==HitTestResult.IMAGE_ANCHOR_TYPE||mLastHitResult.getType()==HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
		{
			String url = mLastHitResult.getExtra();
			if(mLastHitResult.getType()==HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
			{
				mHandler = new MyHandler();
				a.getWebView().requestFocusNodeHref(mHandler.obtainMessage());
				return true;
			}
			showContextMenu(url, null);
			return true;
		}
		return false;
	}
	void showContextMenu(String url,String imageUrl)
	{

		ActArray ar = new ActArray();
		if(!TextUtils.isEmpty(url))
		{
			ar.add(Action.create(Action.GO,url));
			ar.add(Action.create(Action.NEW_TAB,url));
			ar.add(Action.create(Action.BACKGROUND_TAB,url));
			ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,url));
			ar.add(Action.create(Action.SHARE_ELEMENT,url));
			ar.add(Action.create(Action.SAVEFILE, url));
		}
		if(!TextUtils.isEmpty(imageUrl))
		{
			Action aa = Action.create(Action.SAVEFILE,imageUrl).setText(R.string.act_save_image).setImageRes(R.drawable.images);
			aa.smallImageRes = R.drawable.sdcard;
			ar.add(aa);
			aa = Action.create(Action.NEW_TAB,imageUrl).setImageRes(R.drawable.images).setText(R.string.act_open_image);
			aa.smallImageRes = 0;
			ar.add(aa);
			ar.add(Action.create(Action.SHARE_URL,url));
		}
		if(vUrl!=null)
			ar.add(Action.create(Action.EXTERNAL_VIDEO_PLAYER));

		new MenuPanelButton(getMain(), ar, this).show();
	}
	@SuppressLint("HandlerLeak")
	public class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String url = null;
			try{
				url = msg.getData().getString(IConst.URL);
			}
			catch(Throwable e)
			{}
			String imageUrl = mLastHitResult.getExtra();
			showContextMenu(url, imageUrl);
		}
	}
	public static class InputInfo
	{
		public InputInfo(int ss,int se)
		{
			selStart = ss;
			selEnd = se;
		}
		int selStart = 0;
		int selEnd = 0;
	}

}
