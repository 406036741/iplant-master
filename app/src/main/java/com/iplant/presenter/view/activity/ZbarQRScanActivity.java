package com.iplant.presenter.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.R;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

public class ZbarQRScanActivity extends Activity implements QRCodeView.Delegate, View.OnClickListener {
    private boolean mNeedFlashLightOpen = true;
    private QRCodeView mQRCodeView;
    private static final String TAG = ZbarQRScanActivity.class.getSimpleName();
    private ImageView mIvFlashLight;
    private TextView mTvFlashLightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_zbar_qrscan);
            mQRCodeView = (ZBarView) findViewById(R.id.zbarview);
            mQRCodeView.setDelegate(this);
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        TextView tv_InputQR = (TextView) findViewById(R.id.qr_code_header_InputQR);
        mIvFlashLight = (ImageView) findViewById(R.id.qr_code_iv_flash_light);
        mTvFlashLightText = (TextView) findViewById(R.id.qr_code_tv_flash_light);
        TextView mTipText = (TextView) findViewById(R.id.qr_code_header_Tip);
        mTipText.setText(GudData.QRScanHint);
        mIvFlashLight.setOnClickListener(this);
        tv_InputQR.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qr_code_iv_flash_light:
                if (mNeedFlashLightOpen) {
                    turnFlashlightOn();
                } else {
                    turnFlashLightOff();
                }
                break;
            case R.id.qr_code_header_InputQR:

                final EditText inputServer = new EditText(this);
                inputServer.setFocusable(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.record_save_dialog_title)).setView(inputServer).setNegativeButton(
                        "取消", null);
                builder.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                String inputName = inputServer.getText().toString();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("result", inputName);
                                ZbarQRScanActivity.this.setResult(RESULT_OK, resultIntent);
                                ZbarQRScanActivity.this.finish();
                            }
                        });
                builder.show();

                break;
        }
    }

    private void turnFlashlightOn() {
        mNeedFlashLightOpen = false;
        mTvFlashLightText.setText(getString(R.string.qr_code_close_flash_light));
        mIvFlashLight.setBackgroundResource(R.drawable.flashlight_turn_off);
        mQRCodeView.openFlashlight();
    }

    private void turnFlashLightOff() {
        mNeedFlashLightOpen = true;
        mTvFlashLightText.setText(getString(R.string.qr_code_open_flash_light));
        mIvFlashLight.setBackgroundResource(R.drawable.flashlight_turn_on);
        mQRCodeView.closeFlashlight();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
//        mQRCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        mQRCodeView.showScanRect();
        mQRCodeView.startSpot();
        mQRCodeView.changeToScanQRCodeStyle();

    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result", result);
        this.setResult(RESULT_OK, resultIntent);
        ZbarQRScanActivity.this.finish();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }
}
