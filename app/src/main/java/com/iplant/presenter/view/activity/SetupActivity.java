package com.iplant.presenter.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.R;
import com.iplant.util.ConfigUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SetupActivity extends BaseActivity {

    @Bind(R.id.tv_serverip)
    TextView mtv_CurrentIP;

    @Bind(R.id.et_serverip)
    EditText met_NewIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        String wIP = GudData.DOMAIN.substring(7, GudData.DOMAIN.length() - 1);
        mtv_CurrentIP.setText(wIP);
    }

    public void OnSave(View view) {

        try {
            GudData.DOMAIN = "http://" + met_NewIP.getText().toString() + "/";
            ConfigUtils.saveData(this, null, GudData.KEY_ServerIP, GudData.DOMAIN);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CurrentIPonClick(View view) {
        try {
            met_NewIP.setText(mtv_CurrentIP.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
