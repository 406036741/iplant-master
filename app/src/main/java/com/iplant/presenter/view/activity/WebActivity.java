package com.iplant.presenter.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.InvalidScannerNameException;
import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.model.QRResult;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.Layout.DragLayout;
import com.iplant.presenter.view.widget.ToggleButton;
import com.iplant.presenter.view.widget.XDatePickDialog;
import com.iplant.util.ConfigUtils;
import com.iplant.util.DesUtil;
import com.iplant.util.ImageFactory;
import com.iplant.util.LoadingDialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.iplant.GudData.mPerson_judge;

/**
 * @author lildu
 * 网页界面
 */
public class WebActivity extends BaseActivity {
    @Bind(R.id.web)
    WebView mWebView;

    @Bind(R.id.tv_name)
    TextView mName;

    @Bind(R.id.tv_account)
    TextView mAccount;

    @Bind(R.id.RadioGroup_Shift)
    RadioGroup radioGroupShift;

    @Bind(R.id.radioButton_NextShift)
    RadioButton radioButtonNextShift;

    @Bind(R.id.radioButton_CurrentShift)
    RadioButton radioButtonCurrentShift;

    @Bind(R.id.radioButton_PreviousShift)
    RadioButton radioButtonPreviousShift;

    String originUrl;
    String mModuleID;
    Account myAccount;
    JSImpl myJsImpl = new JSImpl();
    private DragLayout dl;
    String mCameraFilePath = null;
    Uri mCameraUri = null;

    // 是否是Android 10以上手机
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= 29;

    ValueCallback<Uri[]> mFilePathCallback;
    public static final int REQUEST_SELECT_FILE = 10000;
    public static final int REQUEST_SELECT_CAPTURE = 10001;

    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private Dialog mLoadingDialog;
    private ToggleButton togglebutton;
    private AidcManager manager;
    private static BarcodeReader barcodeReader;

    String QRCallback;
    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mContext = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            String account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
            myAccount = DBManage.queryBy(Account.class, GudData.KEY_Account, account);
            String wLoginID = DesUtil.decrypt(myAccount.encryptAccount);
            originUrl = getIntent().getStringExtra(GudData.KEY_URL);
            mModuleID = getIntent().getStringExtra(GudData.KEY_ModuleID);
            myJsImpl.registerNotify();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initQRCode();
                    initView();
                    initDragLayout();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this, "拍照权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initQRCode() {
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        AidcManager.create(this, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                } catch (InvalidScannerNameException e) {
                    Toast.makeText(WebActivity.this, "Invalid Scanner Name Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(WebActivity.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initDragLayout() {

        dl = (DragLayout) findViewById(R.id.dl_web);

        mName.setText("" + myAccount.name);
        mAccount.setText(myAccount.account);


        dl.setDragListener(new DragLayout.DragListener() {
            @Override
            public void onOpen() {

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onDrag(float percent) {


            }
        });

        togglebutton = (ToggleButton) findViewById(R.id.tbtn_Persontask);
        togglebutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    // 当按钮第一次被点击时候响应的事件
                    if (togglebutton.isToggleOn()) {
                        togglebutton.setToggleOff(true);
                        mPerson_judge = 0;
                    }
                    // 当按钮再次被点击时候响应的事件
                    else {
                        togglebutton.setToggleOn(true);
                        mPerson_judge = 1;
                    }


                    String wURL = mWebView.getUrl();
                    mWebView.loadUrl(wURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (mPerson_judge == 0) {
            togglebutton.setToggleOff(true);
        }
        // 当按钮再次被点击时候响应的事件
        else {
            togglebutton.setToggleOn(true);
        }


        radioGroupShift.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        try {
                            switch (checkedId) {
                                case R.id.radioButton_PreviousShift:
                                    GudData.mShift_Status = -1;
                                    break;
                                case R.id.radioButton_CurrentShift:
                                    GudData.mShift_Status = 0;
                                    break;
                                case R.id.radioButton_NextShift:
                                    GudData.mShift_Status = 1;
                                    break;
                            }
                            String wURL = mWebView.getUrl();
                            mWebView.loadUrl(wURL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
        );

        if (GudData.mShift_Status == -1) {
            radioButtonPreviousShift.setChecked(true);
        } else if (GudData.mShift_Status == 0) {
            radioButtonCurrentShift.setChecked(true);
        } else if (GudData.mShift_Status == 1) {
            radioButtonNextShift.setChecked(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myJsImpl.unregisterNotify();
        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        ButterKnife.bind(this);
        //初始化webview
        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setAppCacheEnabled(true);
        setting.setBuiltInZoomControls(false);
        setting.setUseWideViewPort(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.addJavascriptInterface(myJsImpl, "JSImpl");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //清除缓存
                mWebView.clearCache(true);
                mWebView.clearHistory();
                if (mLoadingDialog == null)
                    mLoadingDialog = LoadingDialogUtils.createLoadingDialog(mContext, "加载中...");
                super.onPageStarted(view, url, favicon);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if (mLoadingDialog != null)
                    LoadingDialogUtils.closeDialog(mLoadingDialog);
                super.onPageFinished(view, url);
            }


        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }

                mFilePathCallback = filePathCallback;
                showImagePick();
                return true;
            }
        });

        mWebView.loadUrl(originUrl);

    }

    public static Intent openImageIntent(Context context, Uri cameraOutputFile) {

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputFile);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "选择照片来源");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        return chooserIntent;
    }


    private void showImagePick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片来源");
        final String[] froms = {"拍照", "相册"};

        builder.setItems(froms, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        checkPermissionAndCamera();
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
//                        Uri wUri = null;
//                        try {
//                            if (Build.VERSION.SDK_INT >= 24) {
//                                mCameraFilePath = mImgPath + "/iplant/" + System.currentTimeMillis() + ".jpg";
//                                File outputImage = new File(mCameraFilePath);
//                                if (!outputImage.getParentFile().exists()) {
//                                    outputImage.getParentFile().mkdirs();
//                                }
//                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                wUri =  FileProvider.getUriForFile(mContext.getApplicationContext(), BuildConfig.APPLICATION_ID+".provider",outputImage);
//
//                            } else {
//
//                                mCameraFilePath = mImgPath + "/iplant/" + System.currentTimeMillis() + ".jpg";
//                                File outputImage = new File(mCameraFilePath);
//                                if (!outputImage.getParentFile().exists()) {
//                                    outputImage.getParentFile().mkdirs();
//                                }
//                                wUri = Uri.fromFile(outputImage);
//                            }
//                            if(PublicUtile.getInstance().IsLessPermission(mContext,"com.huawei.camera.permission.PRIVATE"))
//                            {
//                                Toast.makeText(mContext, "need Permission:com.huawei.camera.permission.PRIVATE", Toast.LENGTH_LONG);
//                            }
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, wUri);
//                            startActivityForResult(intent, REQUEST_SELECT_CAPTURE);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                    break;
                    case 1: {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_SELECT_FILE);
                    }
                    break;
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
            }
        });

        builder.show();
    }


    /**
     * 检查权限并拍照。
     * 调用相机前先检查权限。
     */
    private void checkPermissionAndCamera() {
//        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA);
//        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
//            //有调起相机拍照。
//            openCamera();
//        } else {
        //没有权限，申请权限。
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_CAMERA_REQUEST_CODE);
//        }
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        try {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // 判断是否有相机
            if (captureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                Uri photoUri = null;

                if (isAndroidQ) {
                    // 适配android 10
                    photoUri = createImageUri();
                } else {
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        mCameraFilePath = photoFile.getAbsolutePath();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                        } else {
                            photoUri = Uri.fromFile(photoFile);
                        }
                    }
                }

                mCameraUri = photoUri;

                if (photoUri != null) {
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(captureIntent, REQUEST_SELECT_CAPTURE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        Uri wUri = null;
        try {
            String status = Environment.getExternalStorageState();
            // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                wUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            } else {
                wUri = getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wUri;
    }

    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        File tempFile = null;
        try {
            String imageName = System.currentTimeMillis() + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }
            tempFile = new File(storageDir, imageName);
            if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            String mImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            if (resultCode != RESULT_OK) {
                if (requestCode == REQUEST_SELECT_FILE || requestCode == REQUEST_SELECT_CAPTURE) {
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                    }
                    mFilePathCallback = null;
                }
                return;
            }
            if (requestCode == 1001 && data != null) {
                Bundle bundle = data.getExtras();

                String result = bundle.getString("result");
                QRResult QRResult = new QRResult();
                if (TextUtils.isEmpty(result)) {
                    QRResult.errorcode = MyError.UNKNOWN;
                } else {
                    QRResult.qrcode = result;
                }

                EventBus.getDefault().post(QRResult);
            } else if (requestCode == REQUEST_SELECT_FILE && data != null) {
                if (mFilePathCallback == null)
                    return;

                Uri uri = data.getData();

                new File(mImgPath).mkdirs();
                String outFile = mImgPath + System.currentTimeMillis() + ".jpg";

                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    new ImageFactory().compressAndGenImage(photo, outFile, 500);
                    Uri wUrl = Uri.fromFile(new File(outFile));
                    mFilePathCallback.onReceiveValue(new Uri[]{wUrl});
                } catch (IOException e) {
                    e.printStackTrace();
                    mFilePathCallback.onReceiveValue(null);
                }

                mFilePathCallback = null;
            } else if (requestCode == REQUEST_SELECT_CAPTURE) {
                try {
                    if (mFilePathCallback == null)
                        return;
                    if (isAndroidQ) {
                        // Android 10 使用图片uri加载
                        mFilePathCallback.onReceiveValue(new Uri[]{mCameraUri});
                    } else {
                        boolean isExist = new File(mCameraFilePath).exists();
                        if (isExist) {
                            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";
                            new ImageFactory().compressAndGenImage(mCameraFilePath, outFile, 500, true);
                            mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = null;
                //            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";
                //            boolean isExist = new File(outFile).exists();
                //            try {
                //                new ImageFactory().compressAndGenImage(mCameraFilePath, outFile, 500, true);
                //                mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
                //            } catch (IOException e) {
                //                e.printStackTrace();
                //                mFilePathCallback.onReceiveValue(null);
                //            }
                //            mFilePathCallback = null;
            }

            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class JSImpl {

        String DateCallback;

        public void registerNotify() {
            EventBus.getDefault().register(this);
        }

        public void unregisterNotify() {
            EventBus.getDefault().unregister(this);
        }

        @JavascriptInterface
        public void exit() {
            finish();
        }

        @JavascriptInterface
        public String getShift() {
            return GudData.mShift_Status + "";
        }

        @JavascriptInterface
        public String getPersonjudge() {
            return GudData.mPerson_judge + "";
        }

        @JavascriptInterface
        public String getModuleID() {
            return mModuleID;
        }

        @JavascriptInterface
        public void readQRCode(String callback, String wHint) {
            QRCallback = callback;
            GudData.QRScanHint = wHint;
            OpenScanUI();
        }

        private void OpenScanUI() {
            if (android.os.Build.MODEL.contentEquals("EDA51")) {
                //打开扫码枪扫码
                Intent barcodeIntent = new Intent("android.intent.action.AutomaticBarcodeActivity");
                startActivityForResult(barcodeIntent, 1001);

            } else {
                //打开摄像头扫码
                Intent i = new Intent(WebActivity.this, ZbarQRScanActivity.class);
                startActivityForResult(i, 1001);
            }

        }

        @JavascriptInterface
        public void readQRCode(String callback) {
            QRCallback = callback;
            GudData.QRScanHint = "按设备扫描键扫码";
            OpenScanUI();
        }

        @JavascriptInterface
        public void pickDate(String callback) {
            DateCallback = callback;

            final Calendar cd = Calendar.getInstance();
            XDatePickDialog pickerdialog = new XDatePickDialog(WebActivity.this,
                    new OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, final int year, final int monthOfYear,
                                              final int dayOfMonth) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    String callBack = "javascript:" + DateCallback + "("
                                            + year + ","
                                            + monthOfYear + ","
                                            + dayOfMonth
                                            + ")";

                                    mWebView.loadUrl(callBack);
                                }
                            });
                        }
                    },
                    cd.get(Calendar.YEAR),
                    cd.get(Calendar.MONTH),
                    cd.get(Calendar.DAY_OF_MONTH));

            pickerdialog.show();
        }

        @JavascriptInterface
        public void SetCustomMap(String wKey, String wValue) {
            GudData.CustomMap.put(wKey, wValue);
        }

        @JavascriptInterface
        public String GetCustomMap(String wKey) {
            String wValue = "";
            if (GudData.CustomMap.containsKey(wKey))
                wValue = GudData.CustomMap.get(wKey);
            return wValue;
        }

        @Subscribe
        public void onEventMainThread(QRResult result) {
            if (!result.isValid()) {
                showMsg("获取二维码失败");
            } else {
                //调用js方法
                showMsg(result.qrcode);
                String callBack = "javascript:" + QRCallback + "('" + result.qrcode + "')";
                mWebView.loadUrl(callBack);
            }
        }
    }

    static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }

    /**
     * 重写onKeyDown，当浏览网页，WebView可以后退时执行后退操作。
     * false 执行安卓返回方法即webview返回上一页 true 表示h5处理返回事件，android端不再处理
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mWebView.loadUrl("javascript:backListener()" );
        }
        return true;
    }


}
