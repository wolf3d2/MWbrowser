package ru.mail.webimage;

public interface WebDownloadCallback {
	public static final int WD_EVENT_GOT_HEADERS = 1;
	public void onWebDownloadEvent(int event,WebDownload wd,Object param);
}
