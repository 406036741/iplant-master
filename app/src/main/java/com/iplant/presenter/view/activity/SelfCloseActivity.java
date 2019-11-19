package com.iplant.presenter.view.activity;

import android.app.Activity;
import android.os.Bundle;

public class SelfCloseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.R.style.Theme_NoDisplay);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}


