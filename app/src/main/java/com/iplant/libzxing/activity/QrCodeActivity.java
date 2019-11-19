package com.iplant.libzxing.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.iplant.R;
import com.iplant.libzxing.camera.CameraManager;
import com.iplant.libzxing.decode.CaptureActivityHandler;
import com.iplant.libzxing.decode.DecodeImageCallback;
import com.iplant.libzxing.decode.DecodeImageThread;
import com.iplant.libzxing.decode.DecodeManager;
import com.iplant.libzxing.decode.InactivityTimer;
import com.iplant.libzxing.view.QrCodeFinderView;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xingli on 12/26/15.
 * <p/>
 * 二维码扫描类。
 */
public class QrCodeActivity extends Activity implements Callback, OnClickListener {

    private static final int REQUEST_SYSTEM_PICTURE = 0;
    private static final int REQUEST_PICTURE = 1;
    public static final int MSG_DECODE_SUCCEED = 1;
    public static final int MSG_DECODE_FAIL = 2;
    private CaptureActivityHandler mCaptureActivityHandler;
    private boolean mHasSurface;
    private boolean mPermissionOk;
    private InactivityTimer mInactivityTimer;
    private QrCodeFinderView mQrCodeFinderView;
    private SurfaceView mSurfaceView;
    private View mLlFlashLight;
    private FrameLayout fl_scanner;
    private RelativeLayout mRL_QR_Bar;
    private final DecodeManager mDecodeManager = new DecodeManager();
    /**
     * 声音和振动相关参数
     */
    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;
    private MediaPlayer mMediaPlayer;
    private boolean mPlayBeep;
    private boolean mVibrate;
    private boolean mNeedFlashLightOpen = true;
    private ImageView mIvFlashLight;
    private TextView mTvFlashLightText;
    private Executor mQrCodeExecutor;
    private Handler mHandler;

    private static Intent createIntent(Context context) {
        Intent i = new Intent(context, QrCodeActivity.class);
        return i;
    }

    public static void launch(Context context) {
        Intent i = createIntent(context);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        initView();
        initData();

    }

    private void checkPermission() {
        boolean hasHardware = checkCameraHardWare(this);
        if (hasHardware) {
            if (!hasCameraPermission()) {
                findViewById(R.id.qr_code_view_background).setVisibility(View.VISIBLE);
                mQrCodeFinderView.setVisibility(View.GONE);
                mPermissionOk = false;
            } else {
                mPermissionOk = true;
            }
        } else {
            mPermissionOk = false;
            finish();
        }
    }

    private void initView() {
        TextView tv_InputQR = (TextView) findViewById(R.id.qr_code_header_InputQR);
        mIvFlashLight = (ImageView) findViewById(R.id.qr_code_iv_flash_light);
        mTvFlashLightText = (TextView) findViewById(R.id.qr_code_tv_flash_light);
        mQrCodeFinderView = (QrCodeFinderView) findViewById(R.id.qr_code_view_finder);
        mSurfaceView = (SurfaceView) findViewById(R.id.qr_code_preview_view);
        mLlFlashLight = findViewById(R.id.qr_code_ll_flash_light);
        fl_scanner = (FrameLayout) findViewById(R.id.qr_code_fl_scanner);
        mRL_QR_Bar = (RelativeLayout) findViewById(R.id.qr_code_header_bar);
        mHasSurface = false;
        mIvFlashLight.setOnClickListener(this);
        tv_InputQR.setOnClickListener(this);
    }

    private void initData() {
        CameraManager.init(this);
        mInactivityTimer = new InactivityTimer(QrCodeActivity.this);
        mQrCodeExecutor = Executors.newSingleThreadExecutor();
        mHandler = new WeakHandler(this);
    }


    public Rect getCropRect() {

        //获取相机的分辨率
        int cameraHeight = CameraManager.mConfigManager.mCameraResolution.width;
        int cameraWidth = CameraManager.mConfigManager.mCameraResolution.height;
        int Temp=0;
        //如果为竖屏模式
        if(cameraHeight<cameraWidth)
        {
            Temp= cameraHeight;
            cameraHeight=cameraWidth;
            cameraWidth=Temp;
        }

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        fl_scanner.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1]-mRL_QR_Bar.getHeight();

        int cropWidth = fl_scanner.getWidth();
        int cropHeight = fl_scanner.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = mSurfaceView.getWidth();
        int containerHeight = mSurfaceView.getHeight() + mRL_QR_Bar.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        return new Rect(x, y, width + x, height + y);

    }

    private boolean hasCameraPermission() {
        PackageManager pm = getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.CAMERA", getPackageName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
        if (!mPermissionOk) {
            mDecodeManager.showPermissionDeniedDialog(this);
            return;
        }
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        turnFlashLightOff();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        mPlayBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            mPlayBeep = false;
        }
        initBeepSound();
        mVibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.quitSynchronously();
            mCaptureActivityHandler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        if (null != mInactivityTimer) {
            mInactivityTimer.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     */
    public void handleDecode(Result result) {
        mInactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        if (null == result) {
            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
                @Override
                public void refresh() {
                    restartPreview();
                }
            });
        } else {
            String resultString = result.getText();
            handleResult(resultString);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException e) {
            // 基本不会出现相机不存在的情况
            Toast.makeText(this, getString(R.string.qr_code_camera_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (RuntimeException re) {
            re.printStackTrace();
            mDecodeManager.showPermissionDeniedDialog(this);
            return;
        }
        mQrCodeFinderView.setVisibility(View.VISIBLE);
        mSurfaceView.setVisibility(View.VISIBLE);
        mLlFlashLight.setVisibility(View.VISIBLE);
        findViewById(R.id.qr_code_view_background).setVisibility(View.GONE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(this);
        }

    }

    private void restartPreview() {
        if (null != mCaptureActivityHandler) {
            mCaptureActivityHandler.restartPreviewAndDecode();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /* 检测相机是否存在 */
    private boolean checkCameraHardWare(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    public Handler getCaptureActivityHandler() {
        return mCaptureActivityHandler;
    }

    private void initBeepSound() {
        if (mPlayBeep && mMediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(mBeepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                mMediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (mPlayBeep && mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        if (mVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener mBeepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

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
//                if (!hasCameraPermission()) {
//                    mDecodeManager.showPermissionDeniedDialog(this);
//                } else {
//                    openSystemAlbum();
//                }
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
                                QrCodeActivity.this.setResult(RESULT_OK, resultIntent);
                                QrCodeActivity.this.finish();
                            }
                        });
                builder.show();

                break;
        }
    }

    private void openSystemAlbum() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, REQUEST_SYSTEM_PICTURE);
    }

    private void turnFlashlightOn() {
        mNeedFlashLightOpen = false;
        mTvFlashLightText.setText(getString(R.string.qr_code_close_flash_light));
        mIvFlashLight.setBackgroundResource(R.drawable.flashlight_turn_off);
        CameraManager.get().setFlashLight(true);
    }

    private void turnFlashLightOff() {
        mNeedFlashLightOpen = true;
        mTvFlashLightText.setText(getString(R.string.qr_code_open_flash_light));
        mIvFlashLight.setBackgroundResource(R.drawable.flashlight_turn_on);
        CameraManager.get().setFlashLight(false);
    }

    private void handleResult(String resultString) {
        if (TextUtils.isEmpty(resultString)) {
            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
                @Override
                public void refresh() {
                    restartPreview();
                }
            });
        } else {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("result", resultString);
            this.setResult(RESULT_OK, resultIntent);
            QrCodeActivity.this.finish();
//            mDecodeManager.showResultDialog(this, resultString, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    restartPreview();
//                }
//            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_PICTURE:
                finish();
                break;
            case REQUEST_SYSTEM_PICTURE:
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (null != cursor) {
                    cursor.moveToFirst();
                    String imgPath = cursor.getString(1); // 图片文件路径
                    cursor.close();
                    if (null != mQrCodeExecutor && !TextUtils.isEmpty(imgPath)) {
                        mQrCodeExecutor.execute(new DecodeImageThread(imgPath, mDecodeImageCallback));
                    }
                }
                break;
        }
    }

    private DecodeImageCallback mDecodeImageCallback = new DecodeImageCallback() {
        @Override
        public void decodeSucceed(Result result) {
            mHandler.obtainMessage(MSG_DECODE_SUCCEED, result).sendToTarget();
        }

        @Override
        public void decodeFail(int type, String reason) {
            mHandler.sendEmptyMessage(MSG_DECODE_FAIL);
        }
    };

    private static class WeakHandler extends Handler {
        private WeakReference<QrCodeActivity> mWeakQrCodeActivity;
        private DecodeManager mDecodeManager = new DecodeManager();

        public WeakHandler(QrCodeActivity imagePickerActivity) {
            super();
            this.mWeakQrCodeActivity = new WeakReference<>(imagePickerActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            QrCodeActivity qrCodeActivity = mWeakQrCodeActivity.get();
            switch (msg.what) {
                case MSG_DECODE_SUCCEED:
                    Result result = (Result) msg.obj;
                    if (null == result) {
                        mDecodeManager.showCouldNotReadQrCodeFromPicture(qrCodeActivity);
                    } else {
                        String resultString = result.getText();
                        handleResult(resultString);
                    }
                    break;
                case MSG_DECODE_FAIL:
                    mDecodeManager.showCouldNotReadQrCodeFromPicture(qrCodeActivity);
                    break;
            }
            super.handleMessage(msg);
        }

        private void handleResult(String resultString) {
            QrCodeActivity imagePickerActivity = mWeakQrCodeActivity.get();
            mDecodeManager.showResultDialog(imagePickerActivity, resultString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

    }
}