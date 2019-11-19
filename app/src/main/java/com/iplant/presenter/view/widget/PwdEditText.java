package com.iplant.presenter.view.widget;

import com.iplant.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

public class PwdEditText extends EditText  { 
	/**
	 * 可见的时候
	 */
    private Drawable mClearDrawable1; 
    
	/**
	 * 不可见的时候
	 */
    private Drawable mClearDrawable2; 
    
    private int drawableWidth;
 
    private boolean pwdIsShow = false;
    
    public PwdEditText(Context context) { 
    	this(context, null); 
    } 
 
    public PwdEditText(Context context, AttributeSet attrs) { 
    	//杩欓噷鏋勯�犳柟娉曚篃寰堥噸瑕侊紝涓嶅姞杩欎釜寰堝灞炴�т笉鑳藉啀XML閲岄潰瀹氫箟
    	this(context, attrs, android.R.attr.editTextStyle); 
    } 
    
    public PwdEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    
    private void init() { 
//        mClearDrawable1 = getResources().getDrawable(R.drawable.quicklook);
//        mClearDrawable2 = getResources().getDrawable(R.drawable.quicklook_2);
//
//        drawableWidth = mClearDrawable1.getIntrinsicWidth();
//
//        mClearDrawable1.setBounds(0, 0, mClearDrawable1.getIntrinsicWidth(), mClearDrawable1.getIntrinsicHeight());
//        mClearDrawable2.setBounds(0, 0, mClearDrawable2.getIntrinsicWidth(), mClearDrawable2.getIntrinsicHeight());
        setClearIconVisible(); 
    } 
 
 
    private void doShow(){
    	pwdIsShow = !pwdIsShow;
    	setClearIconVisible();
    	if (pwdIsShow){
    		setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    	}else{
    		setTransformationMethod(PasswordTransformationMethod.getInstance());
    	}
    }
    
    /**
     * 鍥犱负鎴戜滑涓嶈兘鐩存帴缁橢ditText璁剧疆鐐瑰嚮浜嬩欢锛屾墍浠ユ垜浠敤璁颁綇鎴戜滑鎸変笅鐨勪綅缃潵妯℃嫙鐐瑰嚮浜嬩欢
     * 褰撴垜浠寜涓嬬殑浣嶇疆 鍦�  EditText鐨勫搴� - 鍥炬爣鍒版帶浠跺彸杈圭殑闂磋窛 - 鍥炬爣鐨勫搴�  鍜�
     * EditText鐨勫搴� - 鍥炬爣鍒版帶浠跺彸杈圭殑闂磋窛涔嬮棿鎴戜滑灏辩畻鐐瑰嚮浜嗗浘鏍囷紝绔栫洿鏂瑰悜灏辨病鏈夎�冭檻
     */
    @Override 
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (getCompoundDrawables()[2] != null) {

				boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight() - drawableWidth);
				
				if (touchable) {
					doShow();
				}
			}
		}

		return super.onTouchEvent(event);
	}
 
    /**
     * 璁剧疆娓呴櫎鍥炬爣鐨勬樉绀轰笌闅愯棌锛岃皟鐢╯etCompoundDrawables涓篍ditText缁樺埗涓婂幓
     * @param visible
     */
    protected void setClearIconVisible() { 
        Drawable right = pwdIsShow ? mClearDrawable2 : mClearDrawable1; 
        setCompoundDrawables(getCompoundDrawables()[0], 
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]); 
    } 
    
    /**
     * 璁剧疆鏅冨姩鍔ㄧ敾
     */
    public void setShakeAnimation(){
    	this.setAnimation(shakeAnimation(5));
    }
    
    
    /**
     * 鏅冨姩鍔ㄧ敾
     * @param counts 1绉掗挓鏅冨姩澶氬皯涓�
     * @return
     */
    public static Animation shakeAnimation(int counts){
    	Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
    	translateAnimation.setInterpolator(new CycleInterpolator(counts));
    	translateAnimation.setDuration(1000);
    	return translateAnimation;
    }
 
 
}
