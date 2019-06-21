package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import ru.mail.webimage.FileUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.ui.dialogs.DialogDownloadFile;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.Utils;

public class UrlProcess implements IConst{
	public static final String PLAY_MARKET_DOMAIN = "play.google.com";
	public static final String PLAY_MARKET_STORE = "store";
	public static final String APK_EXT = "apk";
	public static final String HTML_EXT = "html";
	public static final String MHT_EXT = "mht";
	
	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_MHT = "multipart/related";
	public static final String APK_MIME = "application/vnd.android.package-archive";
	public static boolean checkFileOpen(MainActivity a,String url)
	{
		Uri uri = Uri.parse(url);
		if(URLUtil.isFileUrl(url))
		{
			File f = new File(uri.getPath());
			if(f.exists())
			{
				String ext = FileUtils.getFileExt(f);
				if(!TextUtils.isEmpty(ext)&&MHT_EXT.compareToIgnoreCase(ext)==0)
				{
					a.openWebArchive(f);
					return true;
				}
			}
		}
		return false;
	}
	static String tmp_url;
	public static final boolean isPlayMarketUri(Uri uri)
	{
		tmp_url = uri.toString();
		if(!tmp_url.contains(PLAY_MARKET_DOMAIN))
			return false;
		List<String>path = uri.getPathSegments();
		if(path.size()>0&&PLAY_MARKET_STORE.equals(path.get(0)))
			return true;
		return false;
	}
	/** проверяем урл и запускаем плей маркет */
	public static boolean overrideUrlLoading(MainActivity a,String url)
	{
		Uri uri = Uri.parse(url);
		if(!MainActivity.ABOUT_BLANK.equals(url)&&(isExternalUri(uri)||isPlayMarketUri(uri)))
		{
			try{
			    Intent in = new Intent(android.content.Intent.ACTION_VIEW);
			    in.setData(uri);
			    a.startActivity(Intent.createChooser(in, a.getString(R.string.app_name)));
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return true;
		}
		if(IConst.INTENT_SCHEME.equals(uri.getScheme()))
		{
			try {
				Intent in = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				a.startActivity(in);
				return true;
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		if(downloadFileIfCan(a, uri))
//			return true;
		return false;
	}
	public static boolean isExternalUri(Uri uri)
	{
		return(!uri.isHierarchical()||MARKET_SCHEME.equals(uri.getScheme()));
	}
	public static class DownloadFileInfo
	{
		public Uri uri;
		public String filename;
		public long fileSize;
		public String mimeType;
		boolean ok = false;
		public int status=-1;
		public String location;
		byte data[];
	}
	public static void forceDownload(final MainActivity a,Uri uri,final boolean fromUser)
	{
		if(downloadFileIfCan(a, uri))
			return;
		getDownloadFileInfo(a, uri, new st.UniObserver() {
			
			@Override
			public int OnObserver(Object param1, Object param2) {
				DownloadFileInfo fi = (DownloadFileInfo)param1;
				if(!fromUser&&fi.mimeType!=null&&fi.mimeType.contains("text"))
					return 0;
				if(fi!=null&&fi.ok)
					downloadFileWithDialog(a, fi);
				else if(fromUser)
					CustomPopup.toast(a, R.string.cant_download_file);
				return 0;
			}
		});
	}
//	public static String getTextCurrentPage(final MainActivity a,Uri uri,final boolean fromUser)
//	{
//		
//		if(downloadFileIfCan(a, uri))
//			return;
//		getDownloadFileInfo(a, uri, new St.UniObserver() {
//			
//			@Override
//			public int OnObserver(Object param1, Object param2) {
//				DownloadFileInfo fi = (DownloadFileInfo)param1;
//				if(!fromUser&&fi.mimeType!=null&&fi.mimeType.contains("text"))
//					return 0;
//				if(fi!=null&&fi.ok)
//					downloadFileWithDialog(a, fi);
//				else if(fromUser)
//					CustomPopup.toast(a, R.string.cant_download_file);
//				return 0;
//			}
//		});
//	}
	@SuppressLint("DefaultLocale")
	public static String getMimeFromFile(File f)
	{
		String ext = FileUtils.getFileExt(f);
		if(ext!=null)
			ext = ext.toLowerCase();
		if(APK_EXT.equals(ext))
			return APK_MIME;
		if(MHT_EXT.equals(ext))
			return MIME_MHT;
		String mime = getMimeFromFileContent(f);
		if(TextUtils.isEmpty(mime)&&ext!=null)
			mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		return mime;
	}
	public static String filenameFromUri(Uri uri,String defExt)
	{
		String ext = TextUtils.isEmpty(defExt)?HTML_EXT:defExt;
		String fn = null; 
		if(!TextUtils.isEmpty(uri.getPath()))
		{
			fn = FileUtils.filenameFromUri(uri, null);
			if(!TextUtils.isEmpty(fn))
				fn+='.'+ext;
		}
		if(TextUtils.isEmpty(fn))
		{
			fn = uri.getHost();
			fn = fn.replace('.', '_');
			fn+='.'+ext;
		}
		return fn;
	}
	public static String filenameFromUrl(String url,String defExt)
	{
		return filenameFromUri(Uri.parse(url),defExt);
	}
	public static boolean canOpenFileInBrowser(File f)
	{
		String mime = getMimeFromFile(f);
		return canOpenMimeInBrowser(mime);
	}
	public static boolean canOpenMimeInBrowser(String mime)
	{
		if(TextUtils.isEmpty(mime))
			return false;
		if((isMimeTextOrImage(mime)||MIME_MHT.equals(mime)))
			return true;
		else if(mime.contains("xml"))
			return true;
		else if(mime.contains(MIME_TEXT_HTML))
			return true;
		return false;
	}
	public static String getMimeFromFileContent(File f)
	{
		InputStream fis = null;
		String mime = null;
		try{
			fis = new BufferedInputStream(new FileInputStream(f));
			mime = URLConnection.guessContentTypeFromStream(fis);
		}
		catch(Throwable err)
		{}
		try{
			if(fis!=null)
				fis.close();
		}
		catch(Throwable ignor){}
		return mime;
	}
	public static String getFileNameFromContentDisposition(String cd)
	{
    	String parts[] = cd.split(";");
    	String fn=null;
        for(String s:parts)
        {
        	s = s.trim();
        	if(s.startsWith(FILENAME))
        	{
        		fn = s.substring(FILENAME.length());
        		if(fn.length()>2&&fn.indexOf('\"')==0&&fn.lastIndexOf('\"')==fn.length()-1)
        			fn = fn.substring(1,fn.length()-1);
        		fn = fn.trim();
        	}
        }
        return fn;
	}
	private static String FILENAME="filename=";
	@SuppressWarnings("deprecation")
	public static boolean downloadHead(String userAgent, Uri uri,String cookies,DownloadFileInfo fi)
	{
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent);
        HttpGet request = new HttpGet(uri.toString());
        if (!TextUtils.isEmpty(cookies)) {
            request.addHeader("Cookie", cookies);
        }
        HttpResponse response;
        try {
            response = client.execute(request);
            // We could get a redirect here, but if we do lets let
            // the download manager take care of it, and thus trust that
            // the server sends the right mimetype
            fi.status = response.getStatusLine().getStatusCode();
            if (fi.status == 200) {
                Header header = response.getFirstHeader("Content-Type");
                if (header != null) {
                    fi.mimeType = header.getValue();
                    final int semicolonIndex = fi.mimeType.indexOf(';');
                    if (semicolonIndex != -1) {
                    	fi.mimeType = fi.mimeType.substring(0, semicolonIndex);
                    }
                }
                Header contentDispositionHeader = response.getFirstHeader("Content-Disposition");
                if (contentDispositionHeader != null) {
                	fi.filename = getFileNameFromContentDisposition(contentDispositionHeader.getValue());
                }
                else if(!TextUtils.isEmpty(fi.mimeType))
                {
                	fi.filename = FileUtils.filenameFromUri(uri, fi.mimeType);
                	//fi.filename = FileUtils.getDateFileName(fi.mimeType);
                }
                header = response.getFirstHeader(org.apache.http.protocol.HTTP.CONTENT_LEN);
                if(header!=null&&TextUtils.isDigitsOnly(header.getValue()))
                {
                	try{
                		fi.fileSize = Long.decode(header.getValue());
                	}
                	catch(Throwable ignor){}
                }
                fi.ok = true;
            }
            else if (fi.status == 405)
            {
            	fi.mimeType = "text/html";
            	fi.filename = FileUtils.getDateFileName(fi.mimeType);
            	fi.ok = true;
            }
            else
            {
                Header header = response.getFirstHeader("Location");
                if(header!=null)
                	fi.location = header.getValue();
            	
            }
        } catch (Throwable ex) {
            request.abort();
            fi.status = -1;
        } finally {
            client.close();
        }
        return true;
	}
	public static DownloadFileInfo getFileInfoFromDataScheme(Uri uri)
	{
		DownloadFileInfo fi = new DownloadFileInfo();
		fi.uri = uri;
		String d = uri.getSchemeSpecificPart(); // "[<MIME-type>][;charset=<encoding>][;base64],<data>"
		int cindex = d.indexOf(',');
		if(cindex<0)
			return fi;
		String dbase64 = d.substring(cindex+1);
		if(TextUtils.isEmpty(dbase64))
			return fi;
		String params = d.substring(0, cindex);
		String vals[] = params.split("\\;");
		if(vals!=null&&vals.length>0)
		{
			fi.data = Base64.decode(dbase64, Base64.DEFAULT);
			fi.fileSize = fi.data.length;
			for(String s:vals)
			{
				if(!s.startsWith("base64")&&!s.startsWith("charset"))
				{
					fi.filename = FileUtils.getDateFileName(s);
					if(fi.filename!=null)
					{
						fi.mimeType = s;
						fi.ok = true;
					}
					break;
				}
			}
		}
		return fi;
	}
	public static void getDownloadFileInfo(MainActivity a,final Uri uri,st.UniObserver observ)
	{
		final String userAgent = a.getWebView().getSettings().getUserAgentString();
		final String cookies = CookieManager.getInstance().getCookie(uri.toString());

		new st.SyncAsycOper(observ) {
			@Override
			public void makeOper(UniObserver obs) throws Throwable {
		        DownloadFileInfo fi = new DownloadFileInfo();
		        if("data".equals(uri.getScheme()))
        		{
		        	fi = getFileInfoFromDataScheme(uri);
			        m_obs.m_param1 = fi;
		        	return;
        		}
		        fi.uri = uri;
		        m_obs.m_param1 = fi;
		        for(;;)
		        {
		        	if(fi.location!=null)
		        	{
		        		fi.uri = Uri.parse(fi.location);
		        		fi.location = null;
		        	}
		        	downloadHead(userAgent, fi.uri, cookies, fi);
		        	if(fi.status<0||fi.ok||TextUtils.isEmpty(fi.location))
		        		break;
		        }
			}
		}.startAsync();
	}
	public static boolean isMimeTextOrImage(String mime)
	{
		return mime.contains("text")||mime.contains("image");
	}
	public static boolean downloadFileIfCan(MainActivity a, Uri uri)
	{
		List<String> path = uri.getPathSegments();
		if(path!=null&&path.size()>0)
		{
			String fn = FileUtils.filenameFromUri(uri,null);
			int dotIndex = fn.indexOf('.');
			if(dotIndex>-1)
			{
				String ext = fn.substring(dotIndex+1);
				MimeTypeMap mm = MimeTypeMap.getSingleton();
				String mime = mm.getMimeTypeFromExtension(ext);
				if(mime!=null&&!isMimeTextOrImage(mime))
				{
					DownloadFileInfo fi = new DownloadFileInfo();
					fi.uri = uri;
					fi.filename = fn;
					fi.mimeType = mime;
					downloadFileWithDialog(a, fi);
					return true;
				}
			}
		}
		return false;
	}
	public static void downloadFileWithDialog(final Activity a,final DownloadFileInfo fi)
	{
		if(TextUtils.isEmpty(fi.filename))
			fi.filename = FileUtils.filenameFromUri(fi.uri,fi.mimeType);
		if(fi.filename==null)
			return;
		DownloadOptions d = new DownloadOptions(fi.uri, fi.filename);
		new DialogDownloadFile(a,d,fi) {
			@Override
			public void doSave(DownloadOptions opt) {
				if(fi.data!=null)
				{
					try{
						FileOutputStream fs = new FileOutputStream(opt.destFile);
						fs.write(fi.data);
						fs.close();
						stat.sendNewFileToMediaScanner(a, opt.destFile);

					}
					catch(Throwable e){}
				}
				else
					stat.downloadFile(context(), opt);
			}
		}.show();
	}
}
