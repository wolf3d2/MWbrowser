package ru.mail.mailnews;

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

    /** (сутки) частота проверки */
	public static final long FREQ_UPDATE_TIME= 1000l*3600l*24l*1l;
	// для тестирования
//	public static final long FREQ_UPDATE_TIME = 1000l*120l;

	public static void checkUpdate(final IniFile ini)
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
				try {
					sc =  new Scanner(new URL(SITE_APP+PAGE_UPDATE).openStream(), "UTF-8");
					sc.useDelimiter("\\A");
					info = sc.next();
					sc.close();
					fl = true;
				} catch (Throwable e) {
					// если словили исключение, то причин 3:
					// 1. нет инета
					// 2. разрешения на инет нету
					// 3.закрыли доступ к сайту через hosts
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
	 * 0 - новой версии нет <br>
	 * 1 - есть новая версия <br>
	 * 2 - прошло 3 месяца, а прога не обновлена <br>
	 */
	public static boolean checkVersion(Context c, IniFile ini) {
		if (ini == null||c==null)
			return false;
		String ver = st.getAppVersionCode(c);
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

}