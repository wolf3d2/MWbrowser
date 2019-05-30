package ru.mail.mailnews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.ui.dialogs.DialogHelp;
import com.mw.superbrowser.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class st
{
	 // строковые константы для уменьшения объёма занимаемой памяти
	public static final String STR_COMMENT = "//";
	public static final String STR_SPACE = " ";
    public static final String STR_ZERO = "0";
    public static final String STR_ONE = "1";
	public static final String STR_NULL = "";
	public static final String STR_LF = "\n";
	public static final String STR_FILE = "file:///";
    public static final String STR_EQALLY = "=";
    public static final String STR_SLASH= "/";
    public static final String STR_COLON= ":";
    public static final String STR_COMMA = ",";
    public static final String STR_POINT = ".";

    // текущая навигация. По умолчанию - жестами
    public static int pref_navigation = 2;
    public static boolean fl_temp_hide_navigationpanel  = false;
    public static boolean DEBUG = true;
    /** Универсальный обсервер. Содержит 2 параметра m_param1 и m_param2, которые вызываются и меняются в зависимости от контекста*/
    public static abstract class UniObserver
    {
    /** Конструктор с двумя параметрами */
        public UniObserver(Object param1,Object param2)
        {
            m_param1 = param1;
            m_param2 = param2;
        }
    /** Пустой конструктор. Оба параметра - null*/
        public UniObserver()
        {
        }
    /** Вызов функции {@link #OnObserver(Object, Object)} с текущими параметрами*/
        public int Observ(){return OnObserver(m_param1, m_param2);}
    /** Основная функция обработчика */ 
        public abstract int OnObserver(Object param1,Object param2);
    /** Пользовательский параметр 1 */  
        public Object m_param1;
    /** Пользовательский параметр 2 */  
        public Object m_param2;
    }
    /** Класс для запуска пользовательского кода синхронно или асинхронно
     * Создаётся без параметров. По окончании выполнения запускается обработчик */
    public static abstract class SyncAsycOper extends AsyncTask<Void,Void,Void>
    {
    /** Конструктор
     * @param obs Обработчик, который запустится по выполнении */
        public SyncAsycOper(UniObserver obs)
        {
            m_obs = obs;
        }
        public SyncAsycOper()
        {
        }
    /** Синхронно стартует операцию {@link #makeOper(UniObserver)}*/
        public void startSync()
        {
        	try{
            makeOper(m_obs);
        	}
        	catch(Throwable e){}
        }
    /** Асинхронно стартует операцию {@link #makeOper(UniObserver)}*/
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void startAsync()
        {
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
            	execute();
        }
    /** @hide */
        @Override
        protected void onProgressUpdate(Void... values)
        {
            try{
            if(m_obs!=null)
                m_obs.Observ();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
    /** @hide */
        @Override
        protected Void doInBackground(Void... arg0)
        {
            try{
                makeOper(m_obs);
                publishProgress();
            }
            catch (Throwable e) {
            	e.printStackTrace();
            }
            return null;
        }
    /** Выполняемая операция  
     * @throws Throwable */
        public abstract void makeOper(UniObserver obs) throws Throwable;
    /** Обработчик операции */  
        protected UniObserver m_obs;
    }
    public static void hideEditKeyboard(final EditText et)
    {
		InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

    }
    public static void showEditKeyboard(final EditText et)
    {
       (new Handler()).postDelayed(new Runnable() {

            public void run() {

                et.requestFocus();
                et.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                et.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));                       

            }
// задержка перед показом клавы
       }, 300);
    }
    public static class LogTime
    {
        String tag;
        long time;
        public LogTime(String tag)
        {
            this.tag = tag;
            time = System.currentTimeMillis();
        }
        public void log(String msg)
        {
            if(!DEBUG)
                return;
            long t1 = System.currentTimeMillis();
            Log.d(tag, msg+" ["+(t1-time)+"]");
            time = t1;
        }
    }
    /** Обработчик нажатия кнопок в диалоге  */ 
    public static class OnButtonListener implements DialogInterface.OnClickListener
    {
/** Конструктор. Получает обработчик нажатия 
 * @param call Обработчик, вызываемый при нажатии кнопки в диалоге. Первый параметр - код нажатой кнопки в виде Integer*/       
        public OnButtonListener()
        {
            callback = null;
        }
        public OnButtonListener(UniObserver call)
        {
            callback = call;
        }
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            if(callback!=null)
                callback.OnObserver(new Integer(which),callback.m_param2);
        }
        UniObserver callback;
    }

           public static String getCurTimeString(Context c)
           {
               return DateFormat.getTimeFormat(c).format(new Date(System.currentTimeMillis()));
           }
           public static void toast(String text)
           {
        	   if (MainActivity.activeInstance!=null)
        		   toast(MainActivity.activeInstance, text);
           }
           public static void toast(int resId)
           {
        	   if (MainActivity.activeInstance!=null){
        		   toast(MainActivity.activeInstance, MainActivity.activeInstance.getString(resId));
        	   }
           }
           public static void toast(Context c,String text)
           {
               try{
                   Toast.makeText(c, text, Toast.LENGTH_SHORT).show();
               }
               catch (Throwable e) {
               }
           }
           public static void toastLong(String text)
           {
        	   if (MainActivity.activeInstance!=null)
        		   toastLong(MainActivity.activeInstance, text);
           }
           public static void toastLong(int resId)
           {
        	   if (MainActivity.activeInstance!=null){
        		   toastLong(MainActivity.activeInstance, MainActivity.activeInstance.getString(resId));
        	   }
           }
           public static void toastLong(Context c,String text)
           {
               try{
                   Toast.makeText(c, text, Toast.LENGTH_LONG).show();
               }
               catch (Throwable e) {
               }
           }
    	   public static void dialogHelp(Context c, int textId, int titleId) {
    		   String tit = null;
    		   if (titleId!=0)
    			   tit = c.getString(titleId);
    		   String text = null;
    		   if (textId!=0)
    			   text = c.getString(textId);
    		   dialogHelp(c, text, tit);
    	   }
    	   public static void dialogHelp(Context c, String text, String title) {
				new DialogHelp(c, title, text, null)
				.show();
    	   }
           private static DisplayMetrics dm;
           public static int dp2px(Context context, int dp){
               if(dm == null)
                   dm = context.getResources().getDisplayMetrics();
               return (int)(dp * dm.density);
           }
       	public static class LongStr
    	{
    	    ArrayList<String> arr=new ArrayList<String>();
    	    public LongStr(String val)
            {
    	        String s[] = val.split(",");
    	        for(String str:s)
    	        {
    	            if(s!=null&&s.length>0)
    	                arr.add(str);
    	        }
            }
    	    public LongStr(Collection<Long> ar)
    	    {
    	        for(Long l:ar)
    	            arr.add(String.valueOf(l));
    	    }
    	    public LongStr()
    	    {
    	    }
    	    public boolean has(long val)
    	    {
    	        String str = String.valueOf(val);
    	        for(String s:arr)
    	        {
    	            if(s.contentEquals(str))
    	                return true;
    	        }
    	        return false;
    	    }
    	    public ArrayList<Long> getLongList()
    	    {
    	        ArrayList<Long>ar = new ArrayList<Long>();
    	        for(String s:arr)
    	        {
    	            if(s.length()>0)
    	                ar.add(Long.decode(s));
    	        }
    	        return ar;
    	    }
    	    public LongStr remove(long val)
            {
                String str = String.valueOf(val);
    	        for(int i=arr.size()-1;i>=0;i--)
    	        {
    	            if(arr.get(i).contentEquals(str))
    	                arr.remove(i);
    	        }
    	        return this;
            }
    	    @Override
    	    public String toString()
    	    {
    	        StringBuffer sb = new StringBuffer();
    	        for(String s:arr)
    	        {
    	            if(sb.length()>0)
    	                sb.append(',');
    	            sb.append(s);
    	        }
    	        return sb.toString();
    	    }
    	    public LongStr add(long val)
    	    {
    	        if(!has(val))
    	            arr.add(String.valueOf(val));
    	        return this;
    	    }
    	    public JSONArray getJSONArray()
    	    {
    	        JSONArray ar = new JSONArray();
    	        for(Long l:getLongList())
    	        {
    	            ar.put(l.longValue());
    	        }
    	        return ar;
    	    }
    	}
       	public static String getGmtString()
       	{
			String gmt = "GMT";
		 	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(gmt),Locale.getDefault());
	        Date currentLocalTime = calendar.getTime();
	        SimpleDateFormat date = new SimpleDateFormat("Z");
	        return gmt+date.format(currentLocalTime);
       	}
    	public static String getIMEI(final Context c)
    	{
    		String deviceId = null;
    		try{
    			TelephonyManager telephonyManager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
    			deviceId = telephonyManager.getDeviceId();
    		}
    		catch(Throwable e)
    		{}
    		if(TextUtils.isEmpty(deviceId))
    			deviceId = Secure.getString(c.getContentResolver(),Secure.ANDROID_ID);
    		return deviceId;
    	}
        public static void testIllegalState(String text)
        {
        	if(TextUtils.isEmpty(text)||text.substring(0,1)!="111")
        		throw new IllegalStateException(text);
        }
	    public static final boolean isLandscape(Context c)
	    {
	    	int or = c.getResources().getConfiguration().orientation;
	        return  or!= Configuration.ORIENTATION_PORTRAIT;
	    }
	    public static String fileToStr(File f)
	    {
	    	String s= null;
			try{
				FileInputStream fin = new FileInputStream(f);
				byte buf[] = new byte[(int) f.length()];
				fin.read(buf);
				fin.close();
				s = new String(buf);
			}
			catch(Throwable e)
			{
			}
			return s;
	    }
		public static String fileToStr(String filename)
		{
			File f = new File(filename);
			if (f!=null)
				return fileToStr(f);
			return null;
		}
	    public static boolean strToFile(String s,File f)
	    {
	    	try{
	    		f.delete();
	    		FileOutputStream fout = new FileOutputStream(f);
	    		fout.write(s.getBytes());
	    		fout.close();
	    		return true;
	    	}
	    	catch(Throwable e){}
	    	return false;
	    }
		@SuppressLint("NewApi")
		public static <Params, Progress, Result> void safeRunOnExecutor(AsyncTask<Params, Progress, Result> task, Params... params) {
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
			} else {
				task.execute(params);
			}
		}
		// устанавливаем цвет пиктограммки (серый или цветной)
		public static void setImageColor(Context c,ImageView iv,int imRes)
		{
			
			if (Prefs.isColorIcon()) {
				// Конвертируем Drawable в Bitmap
				Bitmap src = BitmapFactory.decodeResource(c.getResources(), imRes);
				iv.setImageBitmap(getGrayColorBitmap(src));
				src.recycle();
			} else
				iv.setImageResource(imRes);
		}
		public static Bitmap getGrayColorBitmap(Bitmap src)
		{
			
			int width = src.getWidth();
			int height = src.getHeight();

			Bitmap bitmapResult = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvasResult = new Canvas(bitmapResult);
			Paint paint = new Paint();
			ColorMatrix colorMatrix = new ColorMatrix();
			// насыщенность цвета. От 0 до 1 
			// 0 - чёрно-белый, 1 - без изменений оригинала
			colorMatrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
			paint.setColorFilter(filter);
			canvasResult.drawBitmap(src, 0, 0, paint);
			return bitmapResult;
		}
	    public static int getOrientation(Context c)
	    {
	    	/*int orient = getResources().getConfiguration().orientation;

			switch (orient) {
			case Configuration.ORIENTATION_PORTRAIT:
				size = (int) (measureHeight * port);

				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				size = (int) (measureHeight * land);
				break;
			}*/
	    	return c.getResources().getConfiguration().orientation;
	    }
	    public static int getDisplayWidth(Context c)
	    {
	    	return c.getResources().getDisplayMetrics().widthPixels;
	    }
	    public static int getDisplayHeight(Context c)
	    {
	    	return c.getResources().getDisplayMetrics().heightPixels;
	    }
	    public static float screenDensity(Context c)
	    {
	        return c.getResources().getDisplayMetrics().density;
	    }
	    public static String getTextAssets(Context c, String filename)
	    {
	        byte[] buffer = null;
	        InputStream is;
	        try {
	            is = c.getAssets().open(filename);
	            int size = is.available();
	            buffer = new byte[size];
	            is.read(buffer);
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	            
	        }
	        
	        return new String(buffer).toString();
	   }
	    /** название приложения */
		public static String getAppName(Context c)
		{
			return st.STR_NULL+c.getString(R.string.app_name);
		}
	    /** возвращает строку с названием текущей версии */
	    public static String getAppVersionName(Context c)
	    {
			try {
					return st.STR_NULL+c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {}
			return st.STR_ZERO;
	    }
	    /** возвращает строку с кодом текущей версии */
		public static String getAppVersionCode(Context c)
		{
	        PackageManager pm = c.getPackageManager();
	        try{
	         return st.STR_NULL+pm.getPackageInfo(c.getPackageName(), 0).versionCode;
	        }
	        catch (Throwable e) {
	        }
	        return st.STR_NULL;
		}
		public static String getAppNameAndVersion(Context c)
		{
			return st.STR_NULL+st.getAppName(c)
					+st.STR_SPACE+st.getAppVersionName(c)
					+st.STR_SPACE+"("+st.getAppVersionCode(c)+")";
		}
	    
}