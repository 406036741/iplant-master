package com.iplant.presenter.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.presenter.view.widget.ClearEditText;
import com.iplant.util.CaptchaCode;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author lildu
 *	密码重置界面
 */
public class PwdResetActivity extends BaseActivity {
	@Bind(R.id.tv_bartitle)
	TextView mTitle;
	
	@Bind(R.id.account)
	ClearEditText mAccount;
	
	@Bind(R.id.captcha)
	EditText mCaptcha;
	
	@Bind(R.id.captcha_img)
	ImageView mCaptchaImg;
	
	private Bitmap captchaBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pwdreset);
		
		initView();
	}


	private void initView() {
		ButterKnife.bind(this);		
		
		mTitle.setText("找回密码");
		
		captchaBitmap = CaptchaCode.getInstance().getBitmap();
		mCaptchaImg.setImageBitmap(captchaBitmap);
	}
	
	/**
	 * 切换验证码
	 * @param v
	 */
	public void onChange(View v){ 
		Bitmap newMap = CaptchaCode.getInstance().getBitmap();
		mCaptchaImg.setImageBitmap(newMap);
		
		if (captchaBitmap != null){
			captchaBitmap.recycle();
		}
		captchaBitmap = newMap;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (captchaBitmap != null){
			captchaBitmap.recycle();
			captchaBitmap = null;
		}
	}
	
	/**
	 * 提交
	 * @param v
	 */
	public void OnSubmit(View v){ 
		String account = mAccount.getEditableText().toString().trim();
		String captcha = mCaptcha.getEditableText().toString().trim();
		
		//检查
		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(captcha)){
			showMsg("用户名，验证码为空 ，请核对后输入！");
			return;
		}
		
		if (!captcha.equalsIgnoreCase(CaptchaCode.getInstance().getCode())){
			showMsg("验证码不合法，请核对后输入！");
			return;
		}
		
		HideKeyboard();
		showWaiting("正在提交...");
		DataFetchModule.getInstance().fetchJsonGet(GudData.DOMAIN + "api/pointer/retrievepwd?user_info=" + account,
				new JsonListener() {
					
					@Override
					public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
						closeWaiting();
						if (retcode != MyError.SUCCESS){
							showMsg("网络错误，请稍后重试！");
						}else{
							showMsg("已通知管理员重置密码, 请联系管理员");
							finish();
						}
					}
				});
	}
}
