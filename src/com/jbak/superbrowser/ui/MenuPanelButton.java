package com.jbak.superbrowser.ui;

import ru.mail.mailnews.st;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomDialog;

public class MenuPanelButton extends CustomDialog {

	OnAction mActionListener;
	public MenuPanelButton(Context context,ActArray actions,OnAction listener) {
		super(context, context instanceof MainActivity&&Prefs.getFullscreen()?R.style.CustomDialogFullscreenTheme:R.style.CustomDialogTheme);
		mActionListener = listener;
		HorizontalPanel grid = new HorizontalPanel(context());
		grid.setActions(actions);
		grid.setWrapContent(true);
		grid.setType(RecyclerViewEx.TYPE_GRID);
		grid.setMaxHeight(Integer.MAX_VALUE);
		grid.setOnActionListener(new OnAction() {
			
			@Override
			public void onAction(Action act) {
				dismiss();
				onActionSelected(act);
			}
		});
		//PanelButtonRecyclerAdapter adapt = new PanelButtonRecyclerAdapter(actions,PanelButton.TYPE_BUTTON_NORMAL);
//		grid.setAdapter(adapt);
//		grid.setOnItemClickListener(this);
//		grid.setNumColumns(GridView.AUTO_FIT);
//		grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
//		grid.setColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.panelButtonSize));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		grid.setOnUnusedSpaceClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onOk(false);
			}

		});
		inflate(grid, lp, Gravity.CENTER);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, grid);
	}
	public void onActionSelected(Action a)
	{
		if(mActionListener!=null)
			mActionListener.onAction(a);
		else
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, a);
	}
	public static class MenuBookmark extends MenuPanelButton
	{
		Bookmark mBookmark;
		int mType;
		Bitmap mPreviewImage;
		public MenuBookmark(Context context, Bookmark bookmark,int type,OnAction listener,Bitmap previewImage) {
			super(context, getActionsForBookmark(context, bookmark, type),listener);
			mPreviewImage = previewImage;
			mBookmark = bookmark;
			mType = type;
		}
		public static ActArray getActionsForBookmark(Context c,Bookmark bm,int type)
		{
			ActArray ar = new ActArray();
			if(bm.isBookmarkFolder())
			{
				ar.add(Action.create(Action.DELETE_FOLDER, bm));
				ar.add(Action.create(Action.EDIT,bm));
			}
			else if(type!=BookmarkActivity.TYPE_WINDOOW_HISTORY)
			{
				ar.add(Action.create(Action.GO, bm));
				ar.add(Action.create(Action.NEW_TAB, bm));
				ar.add(Action.create(Action.BACKGROUND_TAB, bm));
				ar.add(Action.create(Action.DELETE_BOOKMARK, bm).setClosePanel(false));
				ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,bm));
				ar.add(Action.create(Action.EDIT,bm));
				ar.add(Action.create(Action.SHARE_ELEMENT, bm));
			}
			else
				ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD,bm));
			return ar;
		}
		public void onActionDone(Action act)
		{
			
		}
		@Override
		public void onActionSelected(Action a) {
			switch (a.command) {
				case Action.EDIT:
					final Action act = a;
					if(mBookmark==null||!(mBookmark.param instanceof Long))
						return;
					Bookmark parentFolder =  BookmarkFolderAdapter.getParentFolder(context().getContentResolver(), (Long)mBookmark.param);
					stat.editBookmarkWithDialog(context(), mBookmark, parentFolder,new st.UniObserver() {
						
						@Override
						public int OnObserver(Object param1, Object param2) {
							onActionDone(act);
							return 0;
						}
					},mPreviewImage);
					return;
				case Action.COPY_URL_TO_CLIPBOARD:
					stat.setClipboardString(context(), mBookmark.getUrl());
					onActionDone(a);
					return;
				case Action.DELETE_FOLDER:
				case Action.DELETE_BOOKMARK:
					String confirm = stat.getDeleteConfirm(context(), mBookmark.getTitle());
					new ThemedDialog(context()).setConfirm(confirm, a, new ConfirmOper() {
						
						@Override
						public void onConfirm(Object userParam) {
							if(mBookmark.isBookmarkFolder())
								stat.deleteBookmarkFolder(context(), mBookmark);
							else
								stat.deleteBookmark(context(), mBookmark, mType);
							onActionDone((Action)userParam);
							BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, mBookmark);
						}
					}.setTitle(R.string.act_delete_bookmark));
					return;
				case Action.GO:
					a = Action.create(Action.ACTION_BOOKMARK, mBookmark);
					break;
			}
			super.onActionSelected(a);
		}
	}
}
