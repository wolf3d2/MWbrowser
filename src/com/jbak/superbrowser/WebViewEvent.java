package com.jbak.superbrowser;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.widget.EditText;
/** Разнообразные события браузера, на которые подписываются слушатели */
public interface WebViewEvent {
	/** Стартовала загрузка страницы */
	public static final int WWEVENT_PAGE_START = 1;
	public static final int WWEVENT_PAGE_FINISH = 2;
	public static final int WWEVENT_TITLE_LOADED = 3;
	public static final int WWEVENT_FAVICON_LOADED = 4;
	/** Экранная клавиатура показана */
	public static final int WWEVENT_SOFT_KEYBOARD_VISIBLE = 5;
	/** Экранная клавиатура скрыта */
	public static final int WWEVENT_SOFT_KEYBOARD_HIDDEN = 6;
	/** Изменились вкладки  */
	public static final int WWEVENT_WINDOW_LIST_CHANGED = 7;
	/** Изменились закладки  */
	public static final int WWEVENT_BOOKMARKS_CHANGED = 8;
	/** Обновилось текущее окно */
	public static final int WWEVENT_WINDOWS_INVALIDATE = 9;
	/** Загрузка завершена полностью (прогресс=100%) */
	public static final int WWEVENT_LOAD_COMPLETE = 10;
	public static final int WWEVENT_CUR_WINDOW_CHANGES = 11;
	
	public void onWebViewEvent(int code,EventInfo info);
	public static class EventInfo
	{
		private WeakReference<WebView> webView;
		public String startedUrl;
		public String finishedUrl;
		public Bitmap favicon;
		public String title;
		public EditText focusedTextEdit;
		public boolean isLoading = false;
		public boolean isReload = false;
		public String getUrl()
		{
			if(finishedUrl==null)
				return startedUrl;
			return finishedUrl;
		}
		public WebView getWebView() {
			if(webView==null)
				return null;
			return webView.get();
		}
		public void setWebView(WebView webView) {
			this.webView = new WeakReference<WebView>(webView);
		}
	}
}
