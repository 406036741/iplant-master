package com.iplant.presenter.view.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.model.LoginState;
import com.iplant.presenter.UpdatePresenter;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.util.ConfigUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author lildu
 * 用户中心界面
 */
public class UserActivity extends BaseActivity {
    @Bind(R.id.tv_bartitle)
    TextView mTitle;

    @Bind(R.id.name)
    TextView mName;

    @Bind(R.id.account)
    TextView mAccount;
    UpdatePresenter mUpdatePresenter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        mUpdatePresenter = new UpdatePresenter(getApplication());
        initView();


        EventBus.getDefault().register(this);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }


    private void initView() {
        ButterKnife.bind(this);

        mTitle.setText("个人中心");

        String account = ConfigUtils.getString(this, null, GudData.KEY_Account);
        try {
            Account myAccount = DBManage.queryBy(Account.class, GudData.KEY_Account, account);

            mName.setText("" + myAccount.name);
            mAccount.setText(myAccount.account);
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
    }

    public void onUserDetail(View v) {
        jumpTo(UserDetailActivity.class);
    }

    public void onChangePwd(View v) {
        jumpTo(PwdChangeActivity.class);
    }

    public void onCheckUpdate(View v) {
        showWaiting("正在检测更新");
        mUpdatePresenter.doCheckUpdate();
    }


    public void onFeedback(View v) {

        //jumpTo(FeedbackActivity.class);

        String wUrl = GudData.DOMAIN + "my_app/feedback/check.html";

        Intent i = new Intent(this, WebActivity.class);
        i.putExtra(GudData.KEY_URL, wUrl);
        this.startActivity(i);
    }

    public void OnExit(View v) {
        String account = ConfigUtils.getString(this, null, GudData.KEY_Account);
        new UserPresenter().logout(account);
        GudData.mLoginState = LoginState.Logout;
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onEventMainThread(UpdatePresenter.UpdateResult result) {
        closeWaiting();
        if (!result.isValid()) {
            showMsg("检测失败，请稍后在试");
        } else {
            if (!result.mNeedUpdate) {
                showMsg("当前版本已经是最新版本");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getApplication())) {
                        showMsg("需要开启权限:显示在其他应用的上层");
                        return;
                    }
                }
                mUpdatePresenter.showUpdataDialog();
            }

        }
    }


}
