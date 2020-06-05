package ru.mail.mailnews;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import com.jbak.utils.IniFile;

import android.content.Context;

public class SiteApp 
{
	public static final String CHECK_KEY = ";cMWbrowser";
	/** параметр Номер версии в чекнутом файле */
	public static final String CHECK_VERSION_NAME = "ver";
	public static final String SITE_APP = "https://jbak2.ucoz.net";
	public static final String PAGE_OTHER_APP= "/index/vse_programmy/0-16";
	public static final String PAGE_UPDATE = "/upd/act_ver_mwbrowser.htm";
	public static final String PAGE_DOWNLOAD = "/load/mwbrowser/19";
	public static final String PAGE_ADD_STAT = "http://vhost-33881.cloudpark.tech";

	/** массив ссылок для скачивания проверочного файла наличия новой версии. <br>
	 * Нулевой индекс - прямая ссылка. ПОРЯДОК НЕ МЕНЯТЬ!*/
	public static final String[] AR_PAGE_UPDATE =  new String[]
			{
				SITE_APP+PAGE_UPDATE,
				PAGE_ADD_STAT,
				"https://is.gd/4EWpu8"
					
			};
    /** (сутки) частота проверки */
	public static final long FREQ_UPDATE_TIME= 1000l*3600l*24l*1l;
	// для тестирования
//	public static final long FREQ_UPDATE_TIME = 1000l*120l;

	public static void checkUpdate(final IniFile ini, final Context c)
	{
		final long curtime = new Date().getTime();
		long lastcheck = 0;
		// читаем и обрабатываем параметр - время последней проверки
		String par = ini.getParamValue(ini.LAST_CHECK_TIME);
		if (par != null) {
			try {
				lastcheck = Long.parseLong(par);
			} catch (NumberFormatException e) {
				lastcheck = 0;
				ini.setParam(ini.LAST_CHECK_TIME, st.STR_NULL+(curtime-1000));
			}
		} else {
			ini.setParam(ini.LAST_CHECK_TIME, st.STR_NULL+(curtime-1000));
			return;
		}

		// проверяем время последней проверки
		// если текущее время меньше <посл.проверка>+<частота проверки>,
		// то дальше не проверяем
		if (curtime < (lastcheck+FREQ_UPDATE_TIME)) {
			return;
		}

		// чекаем в фоне
		new Thread(new Runnable() {
			public void run() {
				if (ini == null)
					return;
				String info = null;
				// пытаемся чекнуть
				Scanner sc =  null;
				info = null;
				boolean fl = false;
				for (int i=1;i<AR_PAGE_UPDATE.length;i++) {
					try {
						if (AR_PAGE_UPDATE[i].compareTo(AR_PAGE_UPDATE[1]) == 0) {
							//param = "act=2";
							AR_PAGE_UPDATE[i] = AR_PAGE_UPDATE[i]+"/index.php?act=2&v="+st.getAppVersionCode(c);
						}
						info = readUrl(AR_PAGE_UPDATE[i]);
//						} else {
//							sc =  new Scanner(new URL(AR_PAGE_UPDATE[i]).openStream(), "UTF-8");
//							sc.useDelimiter("\\A");
//							info = sc.next();
//							sc.close();
//						}
					} catch (Throwable e) {
					}
					if (info != null&&info.startsWith(CHECK_KEY)) {
						break;
					}
					info = null;
			}
			if (info == null) {
				try {
					// прямая ссылка
					sc =  new Scanner(new URL(AR_PAGE_UPDATE[0]).openStream(), "UTF-8");
					sc.useDelimiter("\\A");
					info = sc.next();
					sc.close();
				} catch (Throwable e) {
				}
			}
			// если до сих пор info = null, то причин 3:
			// 1. нет инета
			// 2. разрешения на инет нету
			// 3.закрыли доступ к сайту через hosts
			// а значит просто возврат
			if (info == null) {
				return;
			}

				// обрабатываем результат
				if (info==null&&!fl) {
					return;
				}
				if (!info.startsWith(CHECK_KEY)) {
					return;
				}
				String par = null;
				String param = null;
				String param_value = null;
				sc = new Scanner(info);
				try {
					sc.useLocale(Locale.US);
					while (sc.hasNext()) {
						if (sc.hasNextLine()) {
							par = sc.nextLine();
						}
						if (par.length() != 0) {
							if (par.compareToIgnoreCase(CHECK_KEY)==0)
								continue;
							param = par.substring(0, par.indexOf("="));
							param_value = par.substring(par.indexOf("=") + 1);
							if (param.compareToIgnoreCase(CHECK_VERSION_NAME)==0) {
								ini.setParam(ini.VERSION_UPDATE, param_value);
							}
						}
					}
				} catch (Throwable e) {
				}
				sc.close();
				ini.setParam(ini.LAST_CHECK_TIME, st.STR_NULL+(curtime-1000));
			}
		}).start();
	}
	/** чекаем на новую версию с выведением тоста если новая версия есть <br>
	 * ЗАПУСКАТЬ МЕТОД ТОЛЬКО В ГЛАВНОМ ПОТОКЕ! <br>
	 * Предварительно, перед этой функцией, должна отработать фукция checkUpdate, <br>
	 * чтобы в par.ini уже были готовы данные для обработки <br>
	 * Если возвращает:   <br>
	 * false - новой версии нет <br>
	 * true - есть новая версия <br>
	 */
	public static boolean checkVersion(Context c, IniFile ini) {
		if (ini == null||c==null)
			return false;
		String ver = st.getAppVersionCode(c);
		//ver = "28";
		int vapp = 0;
		try {
			vapp = Integer.parseInt(ver);
		} catch (NumberFormatException e) {
			return false;
		}
		ver = ini.getParamValue(ini.VERSION_UPDATE);
		int vlini = 0;
		try {
			vlini = Integer.parseInt(ver);
		} catch (NumberFormatException e) {
			return false;
		}
		if (vapp < vlini)
			return true;

		return false;
	}
	private static String readUrl(String url) {
		String str = null;
		// local url variable
		URL lurl = null;
		HttpURLConnection urlConnection = null;
		try {
			lurl = new URL(url);
			urlConnection = (HttpURLConnection) lurl.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("act", "2");

		    //Send request
//			if (param!=null) {
//			    DataOutputStream wr = new DataOutputStream (
//			    		urlConnection.getOutputStream());
//			    wr.writeBytes(param);
//			    wr.close();
//			}
		    OutputStreamWriter request = new OutputStreamWriter(urlConnection.getOutputStream());
		    request.write("act=1");
		    request.flush();
		    request.close();
		    
//		    urlConnection.setConnectTimeout(10000);
//			urlConnection.setReadTimeout(10000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()));
	   		char buf[] = new char[200];
	   		in.read(buf);
			in.close();
			str = new String(buf).trim();
			if (str.length() == 0)
				str = null;
		} catch (Throwable e) {
		} finally {
			urlConnection.disconnect();
		}
		return str;
	}
	
}