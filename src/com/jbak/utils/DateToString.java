package com.jbak.utils;

import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.format.DateFormat;

public class DateToString {
    static String g_today;
    static String g_yesterday;
    static java.text.DateFormat gSystemTimeFormat;
    static boolean gSystemLocale=false;
	public static void create(Context c,String today,String yesterday)
	{
		g_today = today;
		g_yesterday = yesterday;
		gSystemTimeFormat = DateFormat.getTimeFormat(c);
	}
	public static void setUseSystemFormat(boolean systemFormat)
	{
		gSystemLocale = systemFormat;
	}
	public static String formatTime(long time)
	{
		if(gSystemLocale)
			return gSystemTimeFormat.format(new Date(time));
		return DateFormat.format("k:mm",time).toString();
	}
    public static String getDateString(long time)
    {
        GregorianCalendar cal = new GregorianCalendar();
        int cy = cal.get(GregorianCalendar.YEAR);
        cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
        cal.set(GregorianCalendar.MINUTE, 0);
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        if(time>=cal.getTimeInMillis())
        {
            return g_today+' '+formatTime(time);
        }
        cal.roll(GregorianCalendar.DAY_OF_MONTH, -1);
        if(time>=cal.getTimeInMillis())
        {
            return g_yesterday+' '+formatTime(time);
        }
        cal.setTimeInMillis(time);
        int year = cal.get(GregorianCalendar.YEAR);
        if(year<cy)
        {
            return DateFormat.format("dd MMM yyyy", time).toString();
        }
        return DateFormat.format("dd MMM ", time).toString()+formatTime(time);
    }
}
