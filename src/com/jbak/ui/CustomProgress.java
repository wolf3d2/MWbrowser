package com.jbak.ui;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.mw.superbrowser.R;

public class CustomProgress extends Dialog
{
    View m_view;
    TextView m_text;
    public CustomProgress(Context context)
    {
        super(context,R.style.CustomDialogTheme);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        m_view = li.inflate(R.layout.custom_progress, null);
        m_text = (TextView)m_view.findViewById(R.id.text);
        m_view.findViewById(R.id.left_icon).setAnimation(getRotateAnimation());
        setContentView(m_view);
    }
    public TextView getTextView()
    {
        return m_text;
    }
    public static RotateAnimation getRotateAnimation()
    {
        RotateAnimation anim = new RotateAnimation(0.0f, 350.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
        return anim;
    }
}
