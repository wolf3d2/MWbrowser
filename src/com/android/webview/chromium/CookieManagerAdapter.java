/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.webview.chromium;

import android.net.ParseException;
import android.net.WebAddress;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;


public class CookieManagerAdapter extends CookieManager {

    private static final String LOGTAG = "CookieManager";

    Object mChromeCookieManager;

    public CookieManagerAdapter(CookieManager chromeCookieManager) {
        mChromeCookieManager = chromeCookieManager;
    }

    @Override
    public synchronized void setAcceptCookie(boolean accept) {
    }

    @Override
    public synchronized boolean acceptCookie() {
    	return true;
    }

    @Override
    public synchronized void setAcceptThirdPartyCookies(WebView webView, boolean accept) {
    }

    @Override
    public synchronized boolean acceptThirdPartyCookies(WebView webView) {
        return true;
    }

    @Override
    public void setCookie(String url, String value) {
    }

    @Override
    public void setCookie(String url, String value, ValueCallback<Boolean> callback) {
    }

    @Override
    public String getCookie(String url) {
        return null;
    }

    public String getCookie(String url, boolean privateBrowsing) {
        return getCookie(url);
    }

    public synchronized String getCookie(WebAddress uri) {
        return getCookie(uri.toString());
    }

    @Override
    public void removeSessionCookie() {
        
    }

    @Override
    public void removeSessionCookies(ValueCallback<Boolean> callback) {
    }

    @Override
    public void removeAllCookie() {
    }

    @Override
    public void removeAllCookies(ValueCallback<Boolean> callback) {
    }

    @Override
    public synchronized boolean hasCookies() {
    	return false;
    }

    public synchronized boolean hasCookies(boolean privateBrowsing) {
    	return false;
    }

    @Override
    public void removeExpiredCookie() {
        
    }

    protected void flushCookieStore() {
        
    }

    protected boolean allowFileSchemeCookiesImpl() {
        return true;
    }

    protected void setAcceptFileSchemeCookiesImpl(boolean accept) {
    }

    private static String fixupUrl(String url) throws ParseException {
        // WebAddress is a private API in the android framework and a "quirk"
        // of the Classic WebView implementation that allowed embedders to
        // be relaxed about what URLs they passed into the CookieManager, so we
        // do the same normalisation before entering the chromium stack.
        return new WebAddress(url).toString();
    }

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

}
