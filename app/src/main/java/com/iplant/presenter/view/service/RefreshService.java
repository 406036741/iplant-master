package com.iplant.presenter.view.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.model.Account;
import com.iplant.model.EXCMessage;
import com.iplant.model.ZMessage;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.MessagePresenter;
import com.iplant.presenter.UpdatePresenter;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.widget.SystemDialog;
import com.iplant.util.ConfigUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;


public class RefreshService extends Service {
    private GroupPresenter mGroupPresenter;
    private UpdatePresenter mUpdatePresenter;
    private MessagePresenter mMessagePresenter;
    private int mMessageCount = 0;
    Account myAccount;
    private Timer mRefreshTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mRefreshTimer = new Timer();
        mGroupPresenter = new GroupPresenter();
        mUpdatePresenter = new UpdatePresenter(this);
        mMessagePresenter = new MessagePresenter(this);
        startRefresh();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retVal = super.onStartCommand(intent, flags, startId);
        try {
            String account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
            myAccount = DBManage.queryBy(Account.class, GudData.KEY_Account, account);
            mMessageCount = myAccount.MessageCount;
            mGroupPresenter.update(true, myAccount.encryptAccount, myAccount.encryptPwd);
            mMessagePresenter.doCheckMessage(myAccount.encryptAccount, myAccount.encryptPwd);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return retVal;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        stopRefresh();
        Toast.makeText(this.getApplication(), "service is over", Toast.LENGTH_LONG);
        EventBus.getDefault().unregister(this);
    }

    /**
     * 开始刷新
     */
    private void startRefresh() {
        try {
            mRefreshTimer.schedule(taskUpdate, 1000, GudData.TICKS_UPDATE_VERSION);
            mRefreshTimer.schedule(taskUser, 1000, GudData.TICKS_UPDATE_GROUP);
            mRefreshTimer.schedule(taskMessage, 1000, GudData.TICKS_UPDATE_GROUP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭刷新
     */
    private void stopRefresh() {
        mRefreshTimer.cancel();
    }

    //刷新首页数据
    TimerTask taskUser = new TimerTask() {

        @Override
        public void run() {
            try {
                mGroupPresenter.update(false, myAccount.encryptAccount, myAccount.encryptPwd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //检测更新
    TimerTask taskUpdate = new TimerTask() {

        @Override
        public void run() {
            try {
                mUpdatePresenter.doCheckUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //检测消息数
    TimerTask taskMessage = new TimerTask() {

        @Override
        public void run() {
            try {
                mMessagePresenter.doCheckMessage(myAccount.encryptAccount, myAccount.encryptPwd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Subscribe
    public void onEventMainThread(UpdatePresenter.UpdateResult result) {

        if (!result.isValid()) {

        } else {
            if (!result.mNeedUpdate) {
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getApplication())) {
                        return;
                    }
                }
                mUpdatePresenter.showUpdataDialog();
            }

        }
    }

    @Subscribe
    public void onEventMainThread(ZMessage result) {

        try {
            if (!result.isValid()) {

            } else {
                if (result.EXCMessageList.size() > 0) {
                    for (EXCMessage wEXCMessage : result.EXCMessageList) {
                        if (wEXCMessage.Active == 0) {
                            SystemDialog wSystemDialog = new SystemDialog(getApplicationContext());
                            wSystemDialog.show(wEXCMessage.Title, wEXCMessage.MessageText, result.EXCMessageList.size());
                            wEXCMessage.Active = 1;
                        }
                    }
                    new UserPresenter().GetMessageState(myAccount.encryptAccount, myAccount.encryptPwd, result.EXCMessageList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
