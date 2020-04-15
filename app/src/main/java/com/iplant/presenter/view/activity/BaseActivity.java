package com.iplant.presenter.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.Account;
import com.iplant.model.LoginState;
import com.iplant.util.ConfigUtils;
import com.iplant.util.DesUtil;

import org.greenrobot.eventbus.Subscribe;

/**
 * @author lildu
 * 基础activity
 */
public class BaseActivity extends Activity implements DialogInterface.OnClickListener {

    //警告对话框
    public static final int DIALOG_TYPE_WARNING = 1;
    //错误对话框（默认不退出）
    public static final int DIALOG_TYPE_ERROR_1 = 2;
    //错误对话框（默认退出）
    public static final int DIALOG_TYPE_ERROR_2 = 3;

    protected ProgressDialog pbWaiting;

    /**
     * 记录当前是什么对话框
     */
    protected int mCurentDlgType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeWaiting();
    }

    /**
     * 统一处理返回
     *
     * @param v
     */
    public void goBack(View v) {

        finish();
    }

    /**
     * 跳到下一个界面
     *
     * @param clazz
     */
    protected void jumpTo(Class<? extends Activity> clazz) {
        Intent i = new Intent(this, clazz);
        startActivity(i);
    }

    /**
     * 显示消息
     *
     * @param msg
     */
    protected void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 隐藏输入法
     */
    protected void HideKeyboard() {
        View a = getCurrentFocus();
        if (a != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(a.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示警告对话框
     *
     * @param msg
     */
    protected void showWarningDlg(String msg, String buttonText1) {
        mCurentDlgType = DIALOG_TYPE_WARNING;
        if (TextUtils.isEmpty(buttonText1)) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(buttonText1, this)
                    .create()
                    .show();
        }
    }

    /**
     * 显示错误对话框， 不退出
     *
     * @param msg
     */
    protected void showErrorDlg(String msg, String buttonText1) {
        mCurentDlgType = DIALOG_TYPE_ERROR_1;
        if (TextUtils.isEmpty(buttonText1)) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(buttonText1, this)
                    .create()
                    .show();
        }
    }

    /**
     * 显示错误对话框， 退出
     *
     * @param msg
     */
    protected void showErrorExitDlg(String msg, String buttonText1) {
        mCurentDlgType = DIALOG_TYPE_ERROR_2;
        if (TextUtils.isEmpty(buttonText1)) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(buttonText1, this)
                    .create()
                    .show();
        }
    }

    /**
     * 显示等待对话框
     *
     * @param msg
     */
    protected void showWaiting(String msg) {
        closeWaiting();

        pbWaiting = new ProgressDialog(this);
        pbWaiting.setMessage(msg);
        pbWaiting.show();
    }

    /**
     * 关闭等待对话框
     *
     * @param msg
     */
    protected void closeWaiting() {
        if (pbWaiting != null) {
            pbWaiting.dismiss();
            pbWaiting = null;
        }
    }

    @Subscribe
    public void onEventMainThread(Account myAccount) {
        closeWaiting();
        if (!myAccount.isValid()) {
            if (myAccount.errorcode == MyError.DISCONNECT ||
                    myAccount.errorcode == MyError.DNS_RESOLVE ||
                    myAccount.errorcode == MyError.NET ||
                    myAccount.errorcode == MyError.TIMEOUT) {
                showMsg("网络连接超时，请检查网络");
            } else {
                showMsg("登陆失败，请确认用户名和密码");
            }
        } else {
            GudData.mLoginState = LoginState.Login;
            ConfigUtils.saveData(this, null, GudData.KEY_Account, myAccount.account);
            ConfigUtils.saveData(this, null, GudData.KEY_PASSWORD, DesUtil.encrypt(GudData.Login_PASSWORD));
            jumpTo(MainActivity.class);
            finish();
        }

    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (mCurentDlgType) {
            case DIALOG_TYPE_WARNING:
                //do nothing
                break;
            case DIALOG_TYPE_ERROR_1:
                //do nothing
                break;
            case DIALOG_TYPE_ERROR_2:
                finish();
                break;
        }
    }
}
