package ru.mail.webimage;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.jbak.superbrowser.stat;

import android.util.Log;
import ru.mail.webimage.FileUtils.FileProgressInputStream;


@SuppressWarnings("deprecation")
public class WebDownload {
	public static String METHOD_DELETE = "DELETE";
	public static String METHOD_PUT = "PUT";
	public static final String POST_CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String POST_CONTENT_TYPE_JSON = "application/json";
	public static boolean useDefaultTimeouts = false;
	public HttpURLConnection httpConnection;
	public DefaultHttpClient httpClient;
	public InputStream input;
	HashMap<String, String> mRequestHeaders;
	public int contentLength;
	ArrayList<Closeable> mClosables = new ArrayList<Closeable>();
	HttpResponse httpResponce;
	public int responceCode = -1;
	public String responceMessage = stat.STR_NULL;
	public static final int BUF_SIZE = 8192;
	private byte[] mBuffer;
	public Throwable error;
	public String mHttpMethod;
	public PostParams mPostParams;
	/** Если false - не читаем input (input==null) */
	private boolean mReadInput = true;
	/** false - выставляются таймауты 80 сек */
	private boolean mUseDefaultTimeouts = false;
	private WebDownloadCallback mCallback;
	private FileProgressInputStream mCurrentFile;
	public void startGetHttpConnection(String url) throws Throwable {
		URL u = new URL(url);
		httpConnection = (HttpURLConnection) u.openConnection();
		httpConnection.setDefaultUseCaches(false);
		if (!mUseDefaultTimeouts) {
			httpConnection.setConnectTimeout(80000);
			httpConnection.setReadTimeout(80000);
		}
		if (mRequestHeaders != null) {
			for (Entry<String, String> ent : mRequestHeaders.entrySet())
				httpConnection.setRequestProperty(ent.getKey(), ent.getValue());
		}
		if(mHttpMethod!=null)
			httpConnection.setRequestMethod(mHttpMethod);
		if (mPostParams != null) {
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty(HTTP.CONTENT_TYPE,mPostParams.contentType);
			OutputStream os = httpConnection.getOutputStream();
			if(mPostParams.file!=null)
			{
				mCurrentFile = new FileProgressInputStream(mPostParams.file);
				BufferedInputStream bis = new BufferedInputStream(mCurrentFile, BUF_SIZE);
				addClosable(bis);
				InputStreamEntity entity = new InputStreamEntity(bis, mCurrentFile.getTotalSize());
				entity.writeTo(os);
				bis.close();
				Log.d("FUpload", "End of file");
			}
			else
			{
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, HTTP.UTF_8));
				writer.write(mPostParams.content);
				writer.flush();
				writer.close();
			}
		}
		try {
			if (mReadInput) {
				InputStream is = null;
				try {
					is = httpConnection.getInputStream();
					responceCode = 200;
				} catch (Throwable e) {
					is = httpConnection.getErrorStream();
				}
				input = new BufferedInputStream(is, BUF_SIZE);
				addClosable(input);
			}
			responceCode = httpConnection.getResponseCode();
			responceMessage = httpConnection.getResponseMessage();
			contentLength = httpConnection.getContentLength();
		} catch (Throwable e) {
			String stat = httpConnection.getHeaderField(0);
			if(stat==null)
				stat = httpConnection.getHeaderField(null);
			if(stat!=null)
			{
				String s[] = stat.split("");
				int len = s.length;
				if(len>1)
					responceCode = Integer.decode(s[1]);
				if(len>2)
				{
					responceMessage="";
					for(int i=2;i<len;i++)
						responceMessage+=s[i]+"";
				}
			}
		}
		sendCallbackEvent(WebDownloadCallback.WD_EVENT_GOT_HEADERS, null);
	}
	private void sendCallbackEvent(int event,Object param)
	{
		if(mCallback==null)
			return;
		try{
		mCallback.onWebDownloadEvent(event, this, param);
		}
		catch(Throwable e){e.printStackTrace();}
	}
	@SuppressWarnings("deprecation")
	public void startGetDefaultHttpClient(String url) throws Throwable {
		httpClient = new DefaultHttpClient();
		HttpRequestBase req;
		if(mPostParams!=null)
		{
			if(METHOD_PUT.equals(mHttpMethod))
			{
				HttpPut hp = new HttpPut(url);
				hp.addHeader(HTTP.CONTENT_TYPE, mPostParams.contentType);
				hp.setEntity(new StringEntity(mPostParams.content));
				req= hp;
			}
			else
			{
				HttpPost hp = new HttpPost(url);
				hp.addHeader(HTTP.CONTENT_TYPE, mPostParams.contentType);
				if(mPostParams.file!=null)
				{
					mCurrentFile = new FileProgressInputStream(mPostParams.file);
					BufferedInputStream bis = new BufferedInputStream(mCurrentFile, BUF_SIZE);
					addClosable(bis);
					hp.setEntity(new InputStreamEntity(bis, mCurrentFile.getTotalSize()));
				}
				else
					hp.setEntity(new StringEntity(mPostParams.content));
				req= hp;
			}
		}
		else
		{
			if(METHOD_DELETE.equals(mHttpMethod))
				req = new HttpDelete(url);
			else
				req = new HttpGet(url);
		}
		if(mRequestHeaders!=null)
		{
			for (Entry<String, String> ent : mRequestHeaders.entrySet())
				req.addHeader(ent.getKey(), ent.getValue());
		}
		httpResponce = httpClient.execute(req);
		StatusLine sl = httpResponce.getStatusLine();
		responceCode = sl.getStatusCode();
		responceMessage = sl.getReasonPhrase();
		sendCallbackEvent(WebDownloadCallback.WD_EVENT_GOT_HEADERS, null);
		if(mReadInput&&httpResponce.getEntity()!=null)
		{
			input = new BufferedInputStream(httpResponce.getEntity().getContent(),BUF_SIZE);
			addClosable(input);
		}
	}
	public WebDownload setCallback(WebDownloadCallback callback)
	{
		mCallback = callback;
		return this;
	}
	public static String getCookieByName(String name,HttpURLConnection conn)
	{
		Map<String, List<String>> map = conn.getHeaderFields();
		List<String> c = null;
		String c1 = "Set-cookie";
		String c2 = "set-cookie";
		if (map.containsKey(c1)) {
			c = conn.getHeaderFields().get(c1);
		} else if (map.containsKey(c2)) {
			c = conn.getHeaderFields().get(c2);
		} else {
			return null;
		}
		for (String cook : c) {
			if (cook.startsWith(name)) {
				return cook;
			}
		}
		return null;

	}
	public FileProgressInputStream getCurrentInputFile()
	{
		return mCurrentFile;
	}
	public boolean getUrlToFile(String url, String path,boolean useUrlConnection) {
		error = null;
		boolean ok = false;
		File f = new File(path);
		try {
			if (useUrlConnection)
				startGetHttpConnection(url);
			else
				startGetDefaultHttpClient(url);
			f.delete();
			f.getParentFile().mkdirs();
			FileOutputStream fout = new FileOutputStream(f);
			addClosable(fout);
			//httpConnection.setFixedLengthStreamingMode(contentLength);
//			ReadableByteChannel inc = Channels.newChannel(input);
//			FileChannel fc = fout.getChannel(); 
//			fc.position(0);
//			long trans = fc.transferFrom(inc, 0, 100000);
//			fout.flush();
//			long len = f.length();
			byte[] buffer = null;
			if (mBuffer != null)
				buffer = mBuffer;
			else
				buffer = new byte[BUF_SIZE];
			//BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(f), buffer.length);
			addClosable(fout);
			int read = 0;
			while ((read = input.read(buffer)) != -1)
				fout.write(buffer, 0, read);
			ok = true;
		} catch (Throwable e) {
			error = e;
		}
		clear();
		if (!ok)
			f.delete();
		return ok;
	}

	/** Устанавливает буффер для getUrlToFile() */
	public WebDownload setBuffer(byte buffer[]) {
		mBuffer = buffer;
		return this;
	}

	public String getUrl(String url, boolean useUrlConnection) {
		error = null;
		String content = stat.STR_NULL;
		try {
			if (useUrlConnection)
				startGetHttpConnection(url);
			else
				startGetDefaultHttpClient(url);
			content = streamToString(input);
		} catch (Throwable e) {
			error = e;
		}
		clear();
		return content;
	}

	public void clear() {
		for (Closeable c : mClosables) {
			try {
				c.close();
			} catch (Throwable e) {
			}
		}
		httpResponce = null;
		httpClient = null;
		httpConnection = null;
	}

	public void addClosable(Closeable c) {
		mClosables.add(c);
	}

	public static String streamToString(InputStream stream)
			throws Throwable {
		Writer writer = new StringWriter();
		InputStreamReader input = new InputStreamReader(stream, HTTP.UTF_8);
		final char[] buffer = new char[BUF_SIZE];
		int read = 0;
		while ((read = input.read(buffer)) != -1)
			writer.write(buffer, 0, read);
		return writer.toString();
	}

	public final WebDownload setUseDefaultTimeout(boolean useDefault) {
		mUseDefaultTimeouts = useDefault;
		return this;
	}

	public WebDownload addRequestHeader(String name, String value) {
		if (mRequestHeaders == null)
			mRequestHeaders = new HashMap<String, String>();
		mRequestHeaders.put(name, value);
		return this;
	}
	public WebDownload setRequestHeaders(HashMap<String, String> headers) {
		mRequestHeaders = headers;
		return this;
	}

	/**
	 * Разрешает/запрещает считывание данных
	 * 
	 * @param read
	 *            true - данные читаются, false - нет (после
	 *            startGetHttpConnection input==null)
	 * @return Возвращает текущий объект
	 */
	public WebDownload setReadInput(boolean read) {
		mReadInput = read;
		return this;
	}

	/**
	 * Выставление параметров для пост-запроса
	 * 
	 * @param postParams
	 *            Параметры post-запроса. Если null (по умолчанию) - запрос
	 *            выполняется методом GET
	 * @return Возвращает текущий объект
	 */
	public WebDownload setPostParams(PostParams postParams) {
		mPostParams = postParams;
		return this;
	}
	public WebDownload setHttpMethod(String method) {
		mHttpMethod = method;
		return this;
	}

	public static String enc(String v) {
		try {
			return URLEncoder.encode(v, HTTP.UTF_8);
		} catch (Throwable e) {
		}
		return v;
	}
	/**
	 * Параметры для запросов методом POST. Пока поддерживаются только строковые
	 * данные
	 * 
	 * @author JJ
	 */
	public static class PostParams {
		public String contentType = POST_CONTENT_TYPE_FORM_URLENCODED;
		public String content = stat.STR_NULL;
		public File file; 
		/**
		 * Конструктор для передачи json-объекта методом POST
		 * 
		 * @param obj
		 *            объект для отправки. Content-type выставляется в
		 *            POST_CONTENT_TYPE_JSON
		 */
		public PostParams(JSONObject obj) {
			this.contentType = POST_CONTENT_TYPE_JSON;
			content = obj.toString();
		}

		/** Конструктор с установкой Content-Type */
		public PostParams(String contentType) {
			this.contentType = contentType;
		}

		public PostParams(File file,String contentType) {
			this.file = file;
			this.contentType = contentType;
		}
		public PostParams() {
		}

		/** Устанавливает строку контента */
		public PostParams setContentString(String content) {
			this.content = content;
			return this;
		}

		/**
		 * Добавляет параметр для запросов POST_CONTENT_TYPE_FORM_URLENCODED.
		 * 
		 * @param name
		 *            Название параметра
		 * @param value
		 *            Значение параметра. Автоматически кодируется из кодировки
		 *            UTF-8
		 */
		public PostParams addParam(String name, String value) {
			if (content.length() > 0)
				content += '&';
			content += name + '=' + enc(value);
			return this;
		}
	}
	public String getHeaderByName(String name)
	{
		if(httpConnection!=null)
			return httpConnection.getHeaderField(name);
		else if(httpResponce!=null)
		{
			Header h = httpResponce.getFirstHeader(name);
			if(h!=null)
				return h.getValue();
		}
		return null;
	}
	public static boolean isOkResponce(int code)
	{
		return code>=200&&code<300;
	}
	public void cancel()
	{
		if(httpConnection!=null)
		{
			try{
				httpConnection.disconnect();
			}
			catch(Throwable ignor){}
		}
		if(httpClient!=null)
		{
			try{
				httpClient.getConnectionManager().shutdown();
			}
			catch(Throwable ignor){}
		}
		clear();
	}
	
}
