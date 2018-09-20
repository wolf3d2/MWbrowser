package com.jbak.superbrowser.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.Uri;

import com.jbak.superbrowser.stat;
import com.jbak.utils.StrBuilder;

public class HtmlUtils extends StrBuilder{
	public static String TAG_HEAD = "head";
	public static String HREF = "href";
	public static String TAG_BASE = "base";
	public HtmlUtils(Context c) {
		super(c);
	}
	public static final String att(String name,String val)
	{
		return name+"=\""+val+"\"";
	}
	public static final String tag(String tag,boolean closed,String ... attr)
	{
		StringBuffer sb = new StringBuffer().append(TAG_OPEN).append(tag);
		if(attr!=null)
		{
			for(int i=0;i<attr.length;i++)
			{
				sb.append(' ').append(attr[i]);
			}
		}
		if(closed)
			sb.append('/');
		sb.append('>');
		return sb.toString();
	}
	public static String normalizeBaseUrl(String baseUrl)
	{
		Uri uri = Uri.parse(baseUrl);
		return uri.buildUpon().encodedFragment(stat.STR_NULL).encodedQuery(stat.STR_NULL).build().toString();
	}
	public static String writeBaseUrl(String html,String baseUrl)
	{
		baseUrl = normalizeBaseUrl(baseUrl);
		String regExp = TAG_OPEN+TAG_HEAD+".*?"+TAG_CLOSE;
		Pattern pat = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
		Matcher pm = pat.matcher(html);
		if(pm.find())
		{
			String tag = tag(TAG_BASE, true, att(HREF, baseUrl));
			int end = pm.end();
			return html.substring(0, end)+tag+html.substring(end);
		}
		return html;
	}
	
}
