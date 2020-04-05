package com.jbak.superbrowser.ui.dialogs;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.UrlProcess.DownloadFileInfo;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.Utils;

public class DialogEditor extends ThemedDialog{
// переменные для поиска
	boolean searchviewpanel = false;
	/** максимальное количество строк для mEdit, когда поиск и клавиатура скрыта */
	public static final int MAX_MAX_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_GONE= 20;
	/** максимальное количество строк для mEdit, когда поиск и клавиатура видна*/
	public static final int MAX_MIN_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_VISIBLE= 10;
	LinearLayout llsearch = null;
	protected EditText mEditSearch;
	protected ImageView mSearchClose;
	protected ImageView mSearchUp;
	protected ImageView mSearchDown;
	protected TextView mSearchTvPos;
	ArrayList<Integer> arpos_search = new ArrayList<Integer>();
	int pos_search = -1;

	protected EditText mEdit;
	protected ActArray arAct = null;
	String mFileNameForSave;
	
	public DialogEditor(Context context,String title,String text)
	{
		this(context, title, text, true);
//		View v =  setView(R.layout.dialog_editor);
//		mEdit = (EditText) v.findViewById(R.id.editText);
//		mEdit.setMaxLines(5);
//		mEdit.setText(text);
//		mEdit.setSelection(0);
//		// время в мс, сколько показывать скролбар
//		//mEdit.setScrollBarFadeDuration(5000);
//		
//		if (cb_showKbd!=null) {
//			cb_showKbd.setVisibility(View.VISIBLE);
//			cb_showKbd.setChecked(Prefs.getBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, false));
//			if (cb_showKbd.isChecked())
//				st.showEditKeyboard(mEdit);
//			else
//				st.hideEditKeyboard(mEdit);
//			cb_showKbd.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View arg0) {
//					Prefs.setBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, cb_showKbd.isChecked());
//					if (cb_showKbd.isChecked())
//						st.showEditKeyboard(mEdit);
//					else
//						st.hideEditKeyboard(mEdit);
//
//				}
//			});
//		}
//		// показываем клавиатуру, иначе курсор не виден
//		//st.showEditKeyboard(mEdit);
	}
	public DialogEditor(Context context,String title,String text,boolean hideInput) {
		super(context,hideInput,R.style.DialogEditText);
		init(context, title, text);
	}
	public DialogEditor(Context context,String title,String text,ActArray act) {
		this(context, title, text, true);
		arAct = act;
		init(context, title, text);
	}
	public void init(Context context,String title,String text)
	{
		setDialogMaxWidth(0);
		mDefPaddingDp = 0;
		mContentFrame.setPadding(0, 0, 0, 0);
		View v =  setView(R.layout.dialog_editor);
		setTitleText(title);
		mEdit = (EditText) v.findViewById(R.id.editText);
		//mEdit.setMaxHeight(stat.getSizeHeight(context));
		mEdit.setMaxLines(MAX_MAX_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_GONE);
		mEdit.setText(text);
		llsearch = (LinearLayout) v.findViewById(R.id.editdialog_searchLayout);
		mSearchTvPos = (TextView) llsearch.findViewById(R.id.editdialog_pos);
		mSearchClose = (ImageView) llsearch.findViewById(R.id.editdialog_search_close);
		mSearchClose.setOnClickListener(search_clicklistener);
		mSearchUp = (ImageView) llsearch.findViewById(R.id.editdialog_search_up);
		mSearchUp.setOnClickListener(search_clicklistener);
		mSearchDown = (ImageView) llsearch.findViewById(R.id.editdialog_search_down);
		mSearchDown.setOnClickListener(search_clicklistener);
		mEditSearch = (EditText) llsearch.findViewById(R.id.editdialog_searchText);
//		mEditSearch.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				switch (v.getId()) 
//				{
//				case R.id.search_close:
//					st.toast("nnn");
//					
//					break;
//				}
//			}
//		});
		
//		LinearLayout.LayoutParams lp =  (android.widget.LinearLayout.LayoutParams) mContentFrame.getLayoutParams();
//		lp.height = 0;
//		lp.weight = 1f;
//		lp.leftMargin = lp.rightMargin = 0;
		if (arAct==null) {
			arAct = new ActArray();
			createDefaultActions(arAct);
		}
		setButtons(arAct, 1);
		mContentFrame.setBackgroundColor(0xffff0000);
		mContentFrame.setPadding(0, 0, 0, 0);
		if (cb_showKbd!=null) {
			MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, cb_showKbd);
			cb_showKbd.setVisibility(View.VISIBLE);
			cb_showKbd.setChecked(Prefs.getBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, false));
			if (cb_showKbd.isChecked())
				st.showEditKeyboard(mEdit);
			else
				st.hideEditKeyboard(mEdit);
			cb_showKbd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Prefs.setBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, cb_showKbd.isChecked());
					if (cb_showKbd.isChecked())
						st.showEditKeyboard(mEdit);
					else
						st.hideEditKeyboard(mEdit);

				}
			});
		}
	}
	View.OnClickListener search_clicklistener= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
			case R.id.editdialog_search_close:
				hideSearchPanel();
				//llsearch.setVisibility(View.GONE);
				return;
	        case R.id.editdialog_search_down:
	        	viewPosSearch(1);
	            return;
	        case R.id.editdialog_search_up:
	        	viewPosSearch(-1);
	            return;
			}
			st.toast("bbb");
		}
	};
    public void showSearchPanel() 
    {
    	if (searchviewpanel)
    		return;
    	if (llsearch == null)
    		return;
    	llsearch.setVisibility(View.VISIBLE);
    	
    	mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView tv, int action, KeyEvent tvent) {
				  if (action == EditorInfo.IME_ACTION_SEARCH) {
	   	    			search();
	   	    			mEdit.setMaxLines(MAX_MAX_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_GONE);
	   	    			st.hideEditKeyboard(mEditSearch);
	       				return true;
				  }
	       		return false;
			}
		});
    	mEditSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				try {
					if (hasFocus){
	   	    			mEdit.setMaxLines(MAX_MIN_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_VISIBLE);
						mEditSearch.setBackgroundResource(R.drawable.edittext_back_focus_style);
					}else {
	   	    			mEdit.setMaxLines(MAX_MAX_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_GONE);
						mEditSearch.setBackgroundResource(R.drawable.edittext_back_notfocus_style);
					}
				} catch (Throwable e) {
				}

			}
		});
    	st.showEditKeyboard(mEditSearch);
    	if (mEditSearch.getText().toString().length()>0){
    		search();
    	}
    	searchviewpanel = true;
    }
    public void hideSearchPanel() 
    {
    	int pos = mEdit.getSelectionStart();
    	mEdit.setText(mEdit.getText().toString());
    	mEdit.setSelection(pos);
    	mEdit.setEnabled(true);
		mEdit.setMaxLines(MAX_MAX_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_GONE);
    	if (llsearch == null)
    		return;
    	llsearch.setVisibility(View.GONE);
    	//mEditSearch = null;
    	searchviewpanel = false;
    }
    public void search()
    {
    	if (mEdit.isSelected())
    		st.toast("selected");
    	String search_str = mEditSearch.getText().toString().toLowerCase().trim(); 
    	String ettxt = mEdit.getText().toString().toLowerCase(); 
    	String subtxt = ettxt; 
    	arpos_search.clear();;
    	if (search_str.length() == 0) {
    		mEdit.setText(ettxt);
    		viewPosSearch(0);
    		return;
    	}
    	int pos=-1;
    	int pos1 = 0;
    	boolean fl = true;
    	while (fl)
    	{
    		pos = subtxt.indexOf(search_str);
    		if (pos != -1) {
    			pos = pos + pos1; 
    			arpos_search.add(pos);
    			if (pos<=ettxt.length()){
    				int bbb = pos+search_str.length();
    				subtxt = ettxt.substring(pos+search_str.length());
    			} else {
    				break;
    			}
    			if (pos1 == 0)
    				pos1 = pos;
    			else
    				pos1 = pos+search_str.length();
    			continue;
    		}
    		fl = false;
    	}
//!!!
    	if (arpos_search.size()>1){
    		arpos_search.remove(1);
    	}
    	if (arpos_search.size()>0){
        	Spannable text = new SpannableString(mEdit.getText().toString());
        	for (int i=0; i<arpos_search.size();i++){
            	text.setSpan(new BackgroundColorSpan(0x88ff8c00), arpos_search.get(i), arpos_search.get(i)+search_str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        	}
        	mEdit.setText(text);
        	mEdit.requestFocus();
        	mEdit.setSelection(arpos_search.get(0).intValue());
        	pos_search = 0;
        	
    	} else {
        	pos = mEdit.getSelectionStart();
        	mEdit.setText(mEdit.getText().toString());
        	mEdit.setSelection(pos);

        	mEdit.requestFocus();
        	pos_search = -1;
    	}
    	if (searchviewpanel){
    		mEdit.setEnabled(true);
    		mEdit.setBackgroundColor(Color.WHITE);
    		mEdit.setTextColor(Color.BLACK);
    	}
    	else
    		mEdit.setEnabled(true);

    	mEdit.setCursorVisible(true);
		viewPosSearch(0);
   }
    public void viewPosSearch(int pos) 
    {
    	if (mSearchTvPos == null)
    		return;
    	if (arpos_search.size() == 0){
    		mSearchTvPos.setText("[0/0]");
    		return;
    	}
    	if (pos == 0){
    		pos_search = 0;
    	}
    	else if (pos == 1){
    		pos_search++;
    		if (pos_search >= arpos_search.size())
    			pos_search = 0;
//			pos_search = arpos.size()-1;
    	}
    	else if (pos == -1){
    		pos_search--;
    		if (pos_search < 0)
    			pos_search = arpos_search.size()-1;
//			pos_search = 0;
    	}
    	mSearchTvPos.setText("["+(pos_search+1)+st.STR_SLASH+arpos_search.size()+"]");
    	mEdit.requestFocus();
    	mEdit.setSelection(arpos_search.get(pos_search).intValue());
    }
	public void setOnActionListener(OnAction listener)
	{
		mActionPanel.setOnActionListener(listener);
	}
	// меню, долгое нажатие на элементе сайта (не ссылке)
	public void createDefaultActions(ActArray act)
	{
		act.add(Action.create(Action.COPY_URL_TO_CLIPBOARD).setText(R.string.act_copy_text));
		act.add(Action.create(Action.SAVEFILE));
		act.add(Action.create(Action.STOP).setText(R.string.cancel));
		act.add(Action.create(Action.SEARCH_EDIT_DIALOG_LAYOUT));
		act.add(Action.create(Action.TO_START).setText("to Start"));
		act.add(Action.create(Action.TO_END).setText("to End"));
		act.add(Action.create(Action.HOME).setText("Home"));
		act.add(Action.create(Action.END).setText("End"));
		act.add(Action.create(Action.PGDN).setText("PgDn"));
		act.add(Action.create(Action.PGUP).setText("PgUp"));
	}
// листенер, долгое нажатие на элементе сайта (не ссылке)
	@Override
	public void onAction(Action act) {
		final String text = getText();
		// команды, после нажатие на которые, окно диалога не закрываетсч
		if (act.command != Action.HOME
				&&act.command != Action.END
				&&act.command != Action.PGDN
				&&act.command != Action.PGUP
				&&act.command != Action.TO_START
				&&act.command != Action.TO_END
				&&act.command != Action.SEARCH_EDIT_DIALOG_LAYOUT
				)
			dismiss();
		switch (act.command) {
		case Action.SEARCH_EDIT_DIALOG_LAYOUT:
			if (llsearch == null)
				return;
   			mEdit.setMaxLines(MAX_MIN_LINES_MAIN_EDIT_ON_SEARCH_KEYBOARD_VISIBLE);
			//mEdit.setMinLines(5);
			showSearchPanel();
			//llsearch.setVisibility(View.VISIBLE);
			mEditSearch.requestFocus();
			st.showEditKeyboard(mEditSearch);
			
			break;
		case Action.SAVEFILE:
			String saveName = "textfile.txt";
			if(!TextUtils.isEmpty(mFileNameForSave))
				saveName = mFileNameForSave;
			DownloadFileInfo fi = new DownloadFileInfo();
			fi.filename = saveName;
			fi.mimeType = "text/plain";
			fi.fileSize = text.getBytes().length;
			DownloadOptions d = new DownloadOptions(saveName);
			new DialogDownloadFile(context(),d,fi) {
				
				@Override
				public void doSave(DownloadOptions d) {
					try{
						FileOutputStream fos = new FileOutputStream(d.destFile);
						fos.write(text.getBytes());
						fos.close();
					}
					catch(Throwable e)
					{
						Utils.log(e);
					}
				}
			}.show();
			break;
		case Action.ABOUT:
			try{
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.ABOUT));
			}
			catch (Throwable e) {
			}
		break;
		case Action.HELP:
			try{
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.HELP));
			}
			catch (Throwable e) {
			}
		break;
		case Action.COPY_URL_TO_CLIPBOARD:
			stat.setClipboardString(context(), text);
			break;
		case Action.HOME:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_MOVE_HOME));
			break;
		case Action.END:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_MOVE_END));
			break;
		case Action.PGDN:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_PAGE_DOWN));
			break;
		case Action.PGUP:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_PAGE_UP));
			break;
		case Action.TO_START:
			mEdit.setSelection(0);
			break;
		case Action.TO_END:
			mEdit.setSelection(text.length());
			break;
		default:
			break;
		}
	}
	public DialogEditor setFileNameForSave(String fileName)
	{
		mFileNameForSave = fileName;
		return this;
	}
	public final EditText getEdit()
	{
		return mEdit;
	}
	public final String getText()
	{
		return mEdit.getText().toString();
	}
	@Override
	public void setContentMargins(boolean bigMargins) {
	}
	@Override
	public void onBackPressed() {
		if (llsearch!=null) {
			if (llsearch.getVisibility() == View.VISIBLE) {
				hideSearchPanel();
				//llsearch.setVisibility(View.GONE);
				return;
			}
		}
		super.onBackPressed();
	}

}