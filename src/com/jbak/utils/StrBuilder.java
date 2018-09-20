package com.jbak.utils;

import com.jbak.superbrowser.stat;

import android.content.Context;
import android.text.TextUtils;

public class StrBuilder extends ContextRef{
	
	public static final String QUOT_LEFT = "\"";
	public static final String QUOT_RIGHT="\"";
	public static final String TAG_OPEN="<";
	public static final String TAG_CLOSE=">";
	public static final String TAG_A="a";
	public static final String TAG_BR="br";
	public static final String TAG_B="b";
	public static final String EOL="\n";
    public static String DOTS="…";
    public static String EMPTY=stat.STR_NULL;

	String mStr=stat.STR_NULL;
	public StrBuilder(Context c)
	{
		setContext(c);
	}
	@Override
	public String toString() {
		return makeTypograficReplacement(mStr);
	}
	public final StrBuilder addBullit()
	{
		return add("• ");
	}
	public final StrBuilder add(String text)
	{
		if(text!=null)
		{
			if(mStr.length()>0&&!mStr.endsWith(stat.STR_SPACE)&&!mStr.endsWith(EOL))
				mStr+=' ';
			mStr+=text;
		}
		return this;
	}
	public final StrBuilder add(int textId)
	{
		return add(getContext().getString(textId));
	}
	public final StrBuilder add(char symbol)
	{
		mStr+=symbol;
		return this;
	}
	public final StrBuilder addLong(long val)
	{
		return add(EMPTY+val);
	}
	public final StrBuilder addQuoted(String text)
	{
		return add(QUOT_LEFT+text+QUOT_RIGHT);
	}
	public final StrBuilder addBrackets(int textRes)
	{
		return addBrackets(getContext().getString(textRes));
	}
	public final StrBuilder addBrackets(String text)
	{
		return add('('+text+')');
	}
	public final StrBuilder addTag(String tagName,String text,String att)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TAG_OPEN).append(tagName);
		if(!TextUtils.isEmpty(att))
			sb.append(' ').append(att);
		sb.append(TAG_CLOSE).append(text).append(TAG_OPEN).append('/').append(tagName).append(TAG_CLOSE);
		return add(sb.toString());
	}
	public final StrBuilder addLink(int textId,String url)
	{
		return addLink(getContext().getString(textId), url);
	}
	public final StrBuilder addBr()
	{
		return add(TAG_OPEN+TAG_BR+TAG_CLOSE);
	}
	public final StrBuilder addLink(String text,String url)
	{
		return addTag(TAG_A, text, "href='"+url+'\'');
	}
	public final StrBuilder addQuoted(int textId)
	{
		return addQuoted(getContext().getString(textId));
	}
	public final StrBuilder eol()
	{
		return add(EOL);
	}
	public static final String makeTypograficReplacement(String str)
	{
		return str;
		//return str.replace(" ,", ",").replace(" - ", " – ");
	}
	public final boolean isEmpty()
	{
		return TextUtils.isEmpty(mStr);
	}
}
