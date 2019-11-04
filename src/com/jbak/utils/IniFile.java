package com.jbak.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import com.jbak.ui.CustomDialog;

import android.content.Context;
import ru.mail.mailnews.st;

/** обработка своего .ini файла с доп параметрами */
public class IniFile {
	public ArrayList<iniParam> par = new ArrayList<iniParam>();
	// переданный контекст
	Context m_c;
	// текущий путь и имя файла
	String m_fn;
	// последний раз файл изменялся 
	public long last_modify = 1;

	// имя файла par.ini
	public final String PAR_INI= "par.ini";

	// имена параметров в par.ini
	/** версия которая была установлена <br>
	 * для вывода окна Что нового */
	public final String VERSION_CODE = "version_code";
	/** номер чекнутой версии */
	public static final String VERSION_UPDATE = "version_update";

	public IniFile(Context c) {
		m_c = c;
		m_fn = null;
	}

	public static class iniParam {
		String par = st.STR_NULL;
		String val = st.STR_NULL;

		public iniParam(String param, String value) {
			par = param;
			val = value;
		}

		public iniParam() {
			par = st.STR_NULL;
			val = st.STR_NULL;
		}
	}
	public void setFilename(String fn) {
		m_fn = fn;
	}
//	public boolean create()
//	{
//    	ini = new IniFile(inst);
//    	ini.setFilename(st.getSettingsPath()+ini.PAR_INI);
//    	if (!ini.isFileExist())
//    		ini.create(st.getSettingsPath(), ini.PAR_INI);
//    	if (!ini.isFileExist())
//    		return false;
//		return true;
//	}
	/** создаем файл с первоначальными параметрами */
	public boolean create(String path, String filename) {
// не обработано, пока не решил - нужно ли... Посмотрим на реакцию юзеров		
//		public static final String DESC_BEGIN = "desc_begin";
//		public static final String QUICK_SETTING = "quick_setting";
		m_fn = path+filename;
		try {
			// проверяем путь, если его нет пробуем создать
			if (path.endsWith(st.STR_SLASH))
				path = path.substring(0,path.length()-1);
			File ff = new File(path);
			// должен быть именно один &
			if (ff == null & !ff.exists())
				if (!ff.isDirectory()){
					if(!ff.mkdir())
						return false;
				} else
					return false;
				
			FileWriter wr = new FileWriter(m_fn, false);
			wr.write(VERSION_CODE+st.STR_EQALLY+st.getAppVersionCode(m_c)+st.STR_LF);
			wr.flush();
			wr.close();
		} catch (IOException ex) {
			return false;
		}
		
		return true;
	}

	// читаем файл ini
	public boolean readFile() {
		if (m_fn == null)
			return false;
		File ff = new File(m_fn);
		// должен быть именно один &
		if (ff == null & !ff.exists())
			return false;
		String parr = st.STR_NULL;
		int indcom = -1;
		if (par.size() > 0)
			par.clear();
		FileReader fr;
		int ind = -1;
		try {
			fr = new FileReader(m_fn);
			Scanner sc = new Scanner(fr);
			sc.useLocale(Locale.US);
			while (sc.hasNext()) {
				if (sc.hasNextLine()) {
					parr = sc.nextLine();
				}
				// обрезаем коммент
				indcom = par.indexOf("//");
				if (indcom > -1)
					parr = parr.substring(0, indcom);
				parr = parr.trim();
				if (parr.length() > 0) {
					ind = parr.indexOf("=");
					if (ind>-1){
						iniParam ip = new iniParam();
						ip.par = parr.substring(0, ind);
						ip.val = parr.substring(ind + 1);
						par.add(ip);
					}

				}
			}
			last_modify = ff.lastModified();
		} catch (FileNotFoundException e) {
			last_modify = 0;
		}
		return true;
	}

	public boolean setParam(String param, String value) {
		if (m_fn == null)
			return false;
		boolean fl = false;
		try {
			FileWriter wr = new FileWriter(m_fn, false);
			for (iniParam p : par) {
				if (p.par.compareToIgnoreCase(param)==0){
					p.val = value;
					fl = true;
				}
				wr.write(p.par+"="+p.val+st.STR_LF);
			}
			if (!fl)
				wr.write(param+"="+value+st.STR_LF);
				
			wr.flush();
			wr.close();
		} catch (IOException ex) {
			// System.out.println(ex.getMessage());
		}
		return true;
	}
//	public void setParam(String par, String value) {
//		if (m_fn == null)
//			return;
//		try {
//			String out = st.STR_NULL;
//			String fpar = st.STR_NULL;
//			String fpar_value = st.STR_NULL;
//			String par_string = st.STR_NULL;
//			FileReader fr = new FileReader(m_fn);
//			Scanner sc = new Scanner(fr);
//			sc.useLocale(Locale.US);
//			boolean fl = false;
//
//			while (sc.hasNextLine()) {
//				par_string = sc.nextLine();
//				if (par_string.length() != 0) {
//					fpar = par_string.substring(0, par_string.indexOf("="));
//					fpar_value = par_string.substring(par_string.indexOf("=") + 1);
//					if (fpar.compareToIgnoreCase(par) == 0) {
//						out += par + "=" + value + st.STR_CR;
//						fl = true;
//					} else {
//						out += par_string + st.STR_CR;
//					}
//				}
//			}
//			sc.close();
//			if (fl != true)
//				out += par + "=" + value + st.STR_CR;
//			FileWriter writer = new FileWriter(m_fn, false);
//			writer.write(out);
//			writer.flush();
//			writer.close();
//		} catch (IOException ex) {
//			// System.out.println(ex.getMessage());
//		}
//	}

	public String getParamValue(String pname) {
		if (m_fn == null)
			return null;
		File ff = new File(m_fn);
		// должен быть именно один &
		if (ff == null & !ff.exists())
			return null;
		if (last_modify!=0&last_modify!= ff.lastModified())
			readFile();
		if (par != null & par.size() < 1)
			readFile();
		if (par != null & par.size() < 1)
			return null;
		for (iniParam p : par) {
			if (p.par.compareToIgnoreCase(pname) == 0)
				return p.val;
		}
		return null;
	}

	// true - данный параметр есть
	public boolean isParamEmpty(String par) {
		if (getParamValue(par) != null)
			return true;
		return false;
	}
	// true - файл существует
	public boolean isFileExist() {
		if (m_fn == null)
			return false;
		File ff = new File(m_fn);
		// должен быть именно один &
		if (ff == null)
			return false;
		if (!ff.exists())
			return false;
		return true;
	}


}