package com.jbak.superbrowser.media;

import android.content.Context;
import android.net.Uri;
import android.widget.VideoView;
/** пока не используется */
public class MyVideoView extends VideoView 
{
    public MyVideoView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	Uri uri;

    @Override
    public void setVideoURI (Uri uri)
    {
        super.setVideoURI(uri);
        this.uri = uri;
    }
}