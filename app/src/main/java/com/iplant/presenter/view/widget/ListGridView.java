package com.iplant.presenter.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ListGridView extends GridView {
	public ListGridView(Context context) {
		super(context);
	}
	
	public ListGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ListGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

    /** 
     * 設置不滾動 
     */  
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  
    {  
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,  
        MeasureSpec.AT_MOST);  
        super.onMeasure(widthMeasureSpec, expandSpec);  
    } 
}
