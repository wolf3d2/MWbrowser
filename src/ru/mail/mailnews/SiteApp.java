package ru.mail.mailnews;

import java.net.URL;
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
	public static final String PAGE_UPDATE = "/upd/act_ver_mwbrowser.htm";
	public static final String PAGE_DOWТLOAD = "/load/mwbrowser/19";

	public static void checkUpdate(final IniFile ini)
	{
		// чекаем в фоне
		new Thread(new Runnable() {
			public void run() {
				if (ini == null)
					return;
				String info = null;
//				// проверяем время последней проверки
//				// если оно = 0 (выше параметр уже запишется в ini, 
//				// или текущее время меньше <посл.проверка>+<частота проверки>,
//				// то дальше не проверяем
//				
//				if (!bcheck) {
//					if (!bcheck&&lastcheck == 0||curtime < (lastcheck+SiteKbd.FREQ_UPDATE_TIME)) {
//						return;
//					}
//				}

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