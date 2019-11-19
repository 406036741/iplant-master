package com.iplant.presenter.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.view.widget.ClearEditText;
import com.iplant.presenter.view.widget.PwdEditText;
import com.iplant.util.ConfigUtils;
import com.iplant.util.DesUtil;
import com.iplant.util.MacUtil;
import com.iplant.util.ShrisTools;
import com.iplant.util.VersionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @author lildu
 *         登陆
 */
public class LoginActivity extends BaseActivity {
    @Bind(R.id.account)
    ClearEditText mAccount;

    @Bind(R.id.password)
    PwdEditText mPassword;

    @Bind(R.id.version)
    TextView mVersion;

    @Bind(R.id.cb_RememberPwd)
    CheckBox mCheckBox;

    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10001;
    String mid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initView();
        EventBus.getDefault().register(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, }, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
       // initNFC();
    }
    /**
     * 初始化数据
     */
    private void initNFC() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (null == adapter) {
            Toast.makeText(this, "设备不支持NFC功能", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            // 根据包名打开对应的设置界面
            startActivity(intent);
        }
    }

    /**
     * 每次刷NFC都会进这个方法
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        this.mid = ShrisTools.byteToString(myNFCID);

        showMsg(mid);
    }
    @Override
    protected void onResume() {
        super.onResume();
        SetIllustrate();


    }

    private void SetIllustrate() {
        String wIP = GudData.DOMAIN.substring(7, GudData.DOMAIN.length() - 1);
        mVersion.setText(String.format(
                "当前版本:V%s  服务:%s  \n" +
                        " 版权所有 上海中车瑞伯德智能系统股份有限公司 @2016 \n" +
                        "  服务邮箱:zhenghuan.hu@shris.com.cn", VersionUtils.getVersionName(this), wIP));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                   // new UpdatePresenter(this).doCheckUpdate();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        try {
        /*从缓存中读取状态*/
            mAccount.setText(ConfigUtils.getString(this, null, GudData.KEY_Account));
            String wSaveIP = ConfigUtils.getString(this, null, GudData.KEY_ServerIP);
            if (!wSaveIP.isEmpty())
                GudData.DOMAIN = wSaveIP;
            String checkboxstatus = ConfigUtils.getString(this, null, GudData.KEY_REMEMBERPWD);
            if (checkboxstatus.equals("checked".trim())) {
                mCheckBox.setChecked(true);
                String wpwd = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_PASSWORD);
                mPassword.setText(DesUtil.decrypt(wpwd));
            }

            SetIllustrate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击了登陆
     *
     * @param v
     */
    public void OnLogin(View v) {
        String mac = MacUtil.getLocalMacAddress();
        if (mac.trim().isEmpty()) {
            mac = "000000000000";
        }
        mac = "0";
        String account = mAccount.getEditableText().toString().trim();
         GudData.Login_PASSWORD = mPassword.getEditableText().toString().trim();

        //检查
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(GudData.Login_PASSWORD)) {
            showMsg("用户名，密码不合法，请核对后再试！");
            return;
        }
        //默认内网，如果内网不可用自动切换到外网
//		GudData.DOMAIN=NetConnect.testUrlWithTimeOut(GudData.LANIP,GudData.INTERNET,2000);
        new UserPresenter().login(account,GudData.Login_PASSWORD, mac);
        HideKeyboard();
        showWaiting("登陆中，请稍后...");
    }

    /**
     * 点击了重置密码
     *
     * @param v
     */
    public void onPwdRset(View v) {
        jumpTo(PwdResetActivity.class);
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


			/*保存密码到缓存中*/

            if (mCheckBox.isChecked()) {
                ConfigUtils.saveData(this, null, GudData.KEY_REMEMBERPWD, "checked");
            } else {
                ConfigUtils.saveData(this, null, GudData.KEY_REMEMBERPWD, "unchecked");
            }
            GudData.myAccount = myAccount;
            ConfigUtils.saveData(this, null, GudData.KEY_Account, myAccount.account);
            ConfigUtils.saveData(this, null, GudData.KEY_PASSWORD,DesUtil.encrypt(GudData.Login_PASSWORD));
            jumpTo(MainActivity.class);
            finish();
        }

    }

    public void onSetUp(View view) {
        jumpTo(SetupActivity.class);
    }
}
