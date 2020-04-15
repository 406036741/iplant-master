package com.iplant.presenter.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.widget.ClearEditText;
import com.iplant.util.ConfigUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.iplant.util.DesUtil.encrypt;

/**
 * @author lildu
 *	密码修改界面
 */
public class PwdChangeActivity extends BaseActivity {
	@Bind(R.id.tv_bartitle)
	TextView mTitle;
	
	@Bind(R.id.oldpwd)
	ClearEditText mOldPwd;
	
	@Bind(R.id.newpwd)
	ClearEditText mNewPwd;
	
	@Bind(R.id.ensurepwd)
	ClearEditText mEnsurePwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pwdchange);
		
		initView();
		
		EventBus.getDefault().register(this);
	}

	private void initView() {
		try {
			ButterKnife.bind(this);

			mTitle.setText("修改密码");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 提交
	 * @param v
	 */
	public void OnSubmit(View v){ 
		String oldPwd 		= mOldPwd.getEditableText().toString().trim();
		String newPwd 		= mNewPwd.getEditableText().toString().trim();
		String ensurePwd 	= mEnsurePwd.getEditableText().toString().trim();
		String account 		= ConfigUtils.getString(this, null, GudData.KEY_Account);
		
		//检查
		if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(ensurePwd)){
			showMsg("部分内容为空 ，请核对后输入！");
			return;
		}
		
		if (!newPwd.equals(ensurePwd)){
			showMsg("两次输入密码不一致！");
			return;
		}
		
		try {
			Account myAccount = DBManage.queryBy(Account.class, GudData.KEY_Account, account);
			if (!myAccount.encryptPwd.equals(encrypt(oldPwd))){
				showMsg("密码错误，请重新输入！");
				return;
			}
			showWaiting("正在提交...");
			new UserPresenter().changePwd(myAccount.encryptAccount, oldPwd, newPwd);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HideKeyboard();
	}
	
	@Subscribe
	public void onEventMainThread(UserPresenter.ChangePwdResult result){
		closeWaiting();
		if(!result.isValid()){
			showMsg("修改密码失败，请稍后重试！");
		}else{
			showMsg("修改密码成功！");
			finish();
		}
	}
}
