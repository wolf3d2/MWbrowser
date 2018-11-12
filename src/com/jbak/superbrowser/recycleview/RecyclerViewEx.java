package com.jbak.superbrowser.recycleview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.jbak.superbrowser.Prefs;
import com.jbak.ui.UIUtils;

/** !!! тут, кроме прочего, задаётся количество колонок в выдаваемых списках! */
public class RecyclerViewEx extends RecyclerView {
	
	/** !!!Если ширина экрана больше этого значения и mSetListTwoColumnsOnWideScreen==true,
	 *  то показывается список в 2 колонки 
	 *  (смотри метод setListTwoColumnsOnWideScreen - в нём устанавливается)*/ 
	public static final int SCREEN_WIDTH_FOR_TWO_COLUMNS = 700;
	public static final int TYPE_HORZ_LINEAR = 1;
	public static final int TYPE_GRID =   	  2;
	/**Вертикальный список. Для использования в качестве списка */
	public static final int TYPE_VERTICAL_LIST =   3;
	/** ViewPager. Все элементы делают match_parent по ширине*/
	public static final int TYPE_VIEW_PAGER =   4;
	protected int mType = TYPE_HORZ_LINEAR;
	/** Если true - проверяется, можно ли растянуть айтемы по горизонтали для заполнения экрана по ширине*/
	protected boolean mCanAutofill = true;
	/** Если true - высчитано, что нужно растянуть элементы по ширине для заполнения экрана*/
	protected boolean mFill = false;
	/** Максимальная высота */
	private int mMaxHeight = Integer.MAX_VALUE;
	protected boolean mReverseLayout = false;
	protected boolean mStackFromEnd = false;
	protected boolean mSetWrapContent=false;
	MyItemDecoration mItemDecoration;
	/** Список делается в 2 колонки на широком экране (в ландшафте) */
	boolean mSetListTwoColumnsOnWideScreen = false;
	int mSelectedItem = -1;

	RecyclerViewExSettings mSet = new RecyclerViewExSettings();
	
	public RecyclerViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		mType = TYPE_HORZ_LINEAR;
		init();
	}
	public RecyclerViewEx(Context context,int type) {
		super(context);
		mType = type;
		init();
	}
	protected void init()
	{
		setLayoutManager(false);
	}
	OnClickListener mOnUnusedSpaceClickListener;
	public void setOnUnusedSpaceClickListener(OnClickListener listener)
	{
		mOnUnusedSpaceClickListener = listener;
	}
    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN && findChildViewUnder(event.getX(), event.getY()) == null)
        {
            if (mOnUnusedSpaceClickListener != null)
            {
                mOnUnusedSpaceClickListener.onClick(this);
            }
        }
        return super.dispatchTouchEvent(event);
    }
	/** Если true - проверяется, можно ли растянуть айтемы по горизонтали для заполнения экрана по ширине*/
	public final void setCanAutofill(boolean setMakeWidth)
	{
		mCanAutofill = setMakeWidth;
	}
	public int getItemWidth()
	{
		return 0;
	}
	protected boolean getFillHorizontal()
	{
		if(mType==TYPE_HORZ_LINEAR)
		{
			int w = getWidth();
			if(!mCanAutofill||w==0||getAdapter()==null)
				return false;
			int mw = getItemWidth();
			if(mw==0)
				return false;
			int cw = getAdapter().getItemCount()*mw;
			return cw<w;
		}
		if(mType==TYPE_GRID)
			return true;
		return false;
	}
	public int getMaxHeight()
	{
		return mMaxHeight;
	}
	public final void setMaxHeight(int max)
	{
		mMaxHeight = max;
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
	    if(getMeasuredHeight() > getMaxHeight()){
	        setMeasuredDimension(getMeasuredWidth(), getMaxHeight());
	    }
	}
	public void setType(int type)
	{
		mType = type;
		if(mType==TYPE_VERTICAL_LIST)
		{
			setVerticalScrollBarEnabled(true);
			setVerticalFadingEdgeEnabled(true);
		}

		setLayoutManager(getFillHorizontal());
	}
	protected GridLayoutManager createGridLayoutManager(int spans,boolean fill)
	{
		return new GridLayoutManager(getContext(),spans, GridLayoutManager.VERTICAL,mReverseLayout);
	}
	protected LinearLayoutManager createLinearLayoutManager(int orientation,boolean fill)
	{
		return new LinearLayoutManager(getContext(),orientation,mReverseLayout);
	}
	protected void setLayoutManager(boolean fill)
	{
		mFill = fill;
		LayoutManager layoutManager=null;
		if(fill)
		{
			int spans = 1;
			if(mType==TYPE_HORZ_LINEAR)
			{
				if(getAdapter()!=null)
					spans = getAdapter().getItemCount();
				if(spans<1)
					spans =1;
			}
			if(mType==TYPE_GRID)
			{
				if(getAdapter()!=null)
					spans = getAdapter().getItemCount();
				int w = getItemWidth();
				if(w>0&&getWidth()>0)
					spans = getWidth()/w;
			}
			if(spans<1)
				spans = 1;
			layoutManager = createGridLayoutManager(spans,fill);
		}
		else if(mType==TYPE_HORZ_LINEAR)
			layoutManager = createLinearLayoutManager(LinearLayoutManager.HORIZONTAL,fill);
		else if(mType==TYPE_GRID)
			layoutManager = createGridLayoutManager(2, fill);
		else if(mType==TYPE_VERTICAL_LIST)
		{
			int spans = 1;
			if(mSetListTwoColumnsOnWideScreen&&getWidth()>SCREEN_WIDTH_FOR_TWO_COLUMNS
					&&!Prefs.isTwoColumn())
				spans = 2;
			layoutManager = createGridLayoutManager(spans, fill);
		}
		else //if(mType==TYPE_VERTICAL_LIST)
			layoutManager = createLinearLayoutManager(LinearLayoutManager.VERTICAL,fill);
		if(layoutManager instanceof LinearLayoutManager)
		{
			//((LinearLayoutManager)layoutManager).setStackFromEnd(mStackFromEnd);
			setLayoutManager(layoutManager);
		}
	}
	public void setItemDecoration(MyItemDecoration decor)
	{
		if(mItemDecoration!=null)
			removeItemDecoration(mItemDecoration);
		mItemDecoration = decor;
		if(mItemDecoration!=null)
			addItemDecoration(mItemDecoration);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(h==0)
			return;
		setLayoutManager(getFillHorizontal());
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(mSetWrapContent)
		{
			int ch = UIUtils.getChildsHeight(this);
			if(ch==0||getHeight()==0)
				return;
			ch+=getPaddingTop()+getPaddingBottom();
			if(ch<getHeight())
			{
				setMaxHeight(ch);
				ViewGroup.LayoutParams lp = getLayoutParams();
				if(lp!=null)
				{
					lp.height = ch;
					setLayoutParams(lp);
				}
				if(getParent() instanceof ViewGroup)
					((ViewGroup)getParent()).requestLayout();
			}
		}
	}
	public final void setStackFromEnd(boolean fromEnd)
	{
		mStackFromEnd = fromEnd;
	}
	public final void setReverseLayout(boolean reverse)
	{
		mReverseLayout = reverse;
	}
	public int getSelectedItem()
	{
		return -1;
	}
	public final void setWrapContent(boolean wrapContent)
	{
		mSetWrapContent = wrapContent;
	}
	/** !!! выдавать список в две колонки (зависит от настройки*/
	public final void setListTwoColumnsOnWideScreen(boolean set)
	{
		mSetListTwoColumnsOnWideScreen = set;
	}
	public final void setSellectedItem(int selectedItem)
	{
		mSelectedItem = selectedItem;
	}
	@Override
	protected void onDetachedFromWindow() {
		try{
			super.onDetachedFromWindow();
		}
		catch(Throwable e)
		{
			
		}
	}
}
