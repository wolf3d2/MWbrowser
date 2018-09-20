package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.PanelButton;

public class GrayscaleTheme extends ColorTheme
{
	ColorMatrixColorFilter mFilter;
	@Override
	public void setPanelButton(PanelButton pb, int pos, boolean transparent) {
		super.setPanelButton(pb, pos, transparent);
		if(pb.getImageView()==null)
			return;
		if(mFilter==null)
		{
		    ColorMatrix matrix = new ColorMatrix();
		    matrix.setSaturation(0.5f);
		    mFilter = new ColorMatrixColorFilter(matrix);
		}
		pb.getImageView().setColorFilter(mFilter);
	}
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_grayscale), "theme_grayscale");
	}
}