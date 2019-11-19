package com.iplant.presenter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.ModelBase;
import com.iplant.model.UpdateInfo;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.presenter.view.activity.SelfCloseActivity;
import com.iplant.util.VersionUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdatePresenter {
    static final String TAG = "UpdatePresenter";

    static final int UPDATA_CLIENT = 1;
    static final int GET_UNDATAINFO_ERROR = 2;
    static final int DOWN_ERROR = 3;

    private Context mContext;
    private UpdateInfo info;
    private String versionname;

    private boolean mIsShowDialog;

    public static class UpdateResult extends ModelBase<UpdateResult> {
        private static final long serialVersionUID = 8964726955945447240L;
        public boolean mNeedUpdate;
    }

    public UpdatePresenter(Context c) {
        mContext = c;
        versionname = VersionUtils.getVersionName(c);
    }

    public static void closeApp(Context c) {
        Intent i = new Intent(c, SelfCloseActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(i);
    }


    public void doCheckUpdate() {

        try {
            DataFetchModule.getInstance().fetchJsonGet(
                    GudData.DOMAIN + "api/HomePage/VersionLast?client_info=" + versionname,
                    new JsonListener() {

                        @Override
                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
                            UpdateResult result = new UpdateResult();
                            if (retcode != MyError.SUCCESS) {
                                result.errorcode = retcode;
                                result.erorMsg = extraMsg;
                                EventBus.getDefault().post(result);
                                return;
                            }

                            try {
                                JSONObject infoObject = jsondata.getJSONObject("info");
                                result.mNeedUpdate = infoObject.optBoolean("is_update");

                                info = new UpdateInfo();
                               String wDownLoadUrl= infoObject.optString("url");
                                String wPackageName= wDownLoadUrl.substring(1,wDownLoadUrl.indexOf("/",2));
                                if(GudData.DOMAIN.contains(wPackageName))
                                {
                                    wDownLoadUrl=wDownLoadUrl.substring(wDownLoadUrl.indexOf("/",2)+1);
                                }

                                info.setVersion(   infoObject.optString("version_info"));

                                info.setDescription(infoObject.optString("description"));
                                info.setUrl(GudData.DOMAIN+wDownLoadUrl);

                                if (result.mNeedUpdate) {
                                    showUpdataDialog();
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            EventBus.getDefault().post(result);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void UpDateFavoriteModule(final String account, final int groupType, final String moduleId, final int companyid) throws SQLException {
//
//        try {
//            //构造登陆请求
//            HashMap<String, String> headers = new HashMap<String, String>();
//            headers.put("Content-Type", "application/json");
//
//            Map<String, Object> body = new HashMap<String, Object>();
//            body.put("user_info", account);
//            body.put("company_id", companyid);
//            body.put("module_id", moduleId);
//            body.put("status", groupType);
//
//            DataFetchModule.getInstance().fetchJsonPost(
//                    GudData.DOMAIN + "api/homepage/update_favor",
//                    null,
//                    headers,
//                    JsonBuilder.buildPostBody(body),
//                    new JsonListener() {
//
//                        @Override
//                        public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
//
//                            if (retcode != MyError.SUCCESS) {
//
//                            } else {
//                                new GroupPresenter().update(true,account,companyid);
//                                EventBus.getDefault().post(new GroupPresenter.GroupUpdateResult());
//                            }
//
//                        }
//                    }, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//    }


    public static File getFileFromServer(String path, ProgressDialog pd) throws Exception {
        //如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(18000);
            //获取到文件的大小
            pd.setMax(conn.getContentLength() / 1024);
            InputStream is = conn.getInputStream();
            File file = new File(Environment.getExternalStorageDirectory(), "updata.apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[2048];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                //获取当前下载量
                pd.setProgress(total / 1024);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }

    /*
     *
     * 弹出对话框通知用户更新程序
     *
     * 弹出对话框的步骤：
     * 	1.创建alertDialog的builder.
     *	2.要给builder设置属性, 对话框的内容,样式,按钮
     *	3.通过builder 创建一个对话框
     *	4.对话框show()出来
     */
    protected void showUpdataDialog() {
        if (mIsShowDialog) {
            return;
        }


        AlertDialog.Builder builer = new Builder(mContext);
        builer.setTitle("新版本升级");
        builer.setMessage(info.getDescription());
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("更新", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "下载apk,更新");
                downLoadApk();
            }
        });
        builer.setNegativeButton("取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "取消更新");
                closeApp(mContext);

            }
        });
        AlertDialog dialog = builer.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        }else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        dialog.show();
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(mContext, "下载失败", Toast.LENGTH_LONG).show();
            closeApp(mContext);
        }
    };

    /*
     * 从服务器中下载APK
     */
    protected void downLoadApk() {
        final ProgressDialog pd;    //进度条对话框
        pd = new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.setProgressNumberFormat("%1d kb/%2d kb");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消下载", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeApp(mContext);
                    }
                }
        );
        pd.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsShowDialog = false;
            }
        });
        pd.show();

        new Thread() {
            @Override
            public void run() {
                try {
                    File file = getFileFromServer(info.getUrl(), pd);
                    sleep(1000);
                    pd.dismiss(); //结束掉进度条对话框
                    installApk(file);
                    closeApp(mContext);
                } catch (Exception e) {
                    pd.dismiss(); //结束掉进度条对话框
                    mIsShowDialog = false;
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(0);
                }
            }
        }.start();

    }

    //安装apk
    protected void installApk(File file) {
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 7.0 以上
            Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            // 7.0以下
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }

        mContext.startActivity(intent);
    }
}
