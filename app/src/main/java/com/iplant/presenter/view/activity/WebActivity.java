package com.iplant.presenter.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ComponentName;
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
import android.text.TextUtils;
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
    String mImgPath = null;
    ValueCallback<Uri[]> mFilePathCallback;
    public static final int REQUEST_SELECT_FILE = 10000;
    public static final int REQUEST_SELECT_CAPTURE = 10001;

    private Dialog mLoadingDialog;
    private ToggleButton togglebutton;
    private AidcManager manager;
    private static BarcodeReader barcodeReader;

    String QRCallback;
    Context wContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        wContext = this;

        ActivityCompat.requestPermissions(WebActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, "com.huawei.camera.permission.PRIVATE"}, 1);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        new Thread() {
            @Override
            public void run() {
                try {
                    String account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
                    myAccount = DBManage.queryBy(Account.class, "account", account);
                    String wLoginID = DesUtil.decrypt(myAccount.myID);
                    originUrl = getIntent().getStringExtra(GudData.KEY_URL);
                    mModuleID = getIntent().getStringExtra(GudData.KEY_ModuleID);
                    mImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                    new File(mImgPath).mkdirs();
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
        }.start();
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
                    mLoadingDialog = LoadingDialogUtils.createLoadingDialog(wContext, "加载中...");
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
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                        Uri wUri = null;
                        try {
                            if (Build.VERSION.SDK_INT >= 24) {
                                mCameraFilePath = mImgPath + "/iplant/" + System.currentTimeMillis() + ".jpg";
                                File outputImage = new File(mCameraFilePath);
                                if (!outputImage.getParentFile().exists()) {
                                    outputImage.getParentFile().mkdirs();
                                }
                                wUri = FileProvider.getUriForFile(WebActivity.this, WebActivity.this.getPackageName() + ".fileprovider", outputImage);

                            } else {

                                mCameraFilePath = mImgPath + "/iplant/" + System.currentTimeMillis() + ".jpg";
                                File outputImage = new File(mCameraFilePath);
                                if (!outputImage.getParentFile().exists()) {
                                    outputImage.getParentFile().mkdirs();
                                }
                                wUri = Uri.fromFile(outputImage);
                            }
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, wUri);
                            startActivityForResult(intent, REQUEST_SELECT_CAPTURE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";

            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                new ImageFactory().compressAndGenImage(photo, outFile, 500);
                mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
            } catch (IOException e) {
                e.printStackTrace();
                mFilePathCallback.onReceiveValue(null);
            }

            mFilePathCallback = null;
        } else if (requestCode == REQUEST_SELECT_CAPTURE) {
            if (mFilePathCallback == null)
                return;

            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";
            boolean isExist = new File(outFile).exists();
            try {
                new ImageFactory().compressAndGenImage(mCameraFilePath, outFile, 500, true);
                mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
            } catch (IOException e) {
                e.printStackTrace();
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
}
