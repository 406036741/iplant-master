package com.iplant.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import com.iplant.util.DownloadManagerUtil;
import com.iplant.util.VersionUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdatePresenter {
    static final String TAG = "UpdatePresenter";



    private Context mContext;
    private UpdateInfo info;
    private String versionname;
    long downloadId = 0;
    DownloadManagerUtil downloadManagerUtil;

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
                                result.errorMsg = extraMsg;
                                EventBus.getDefault().post(result);
                                return;
                            }

                            try {
                                JSONObject infoObject = jsondata.getJSONObject("info");
                                result.mNeedUpdate = infoObject.optBoolean("is_update");
                                info = new UpdateInfo();
                                String wDownLoadUrl = infoObject.optString("url");
                                String wPackageName = wDownLoadUrl.substring(1, wDownLoadUrl.indexOf("/", 2));
                                if (GudData.DOMAIN.contains(wPackageName)) {
                                    wDownLoadUrl = wDownLoadUrl.substring(wDownLoadUrl.indexOf("/", 2) + 1);
                                }

                                info.setVersion(infoObject.optString("version_info"));

                                info.setDescription(infoObject.optString("description"));
                                info.setUrl(GudData.DOMAIN + wDownLoadUrl);

                                if (result.mNeedUpdate) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if(!Settings.canDrawOverlays(mContext))
                                        {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                                            mContext.startActivity(intent);
                                        }
                                    }
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
    public void showUpdataDialog() {

        AlertDialog.Builder builer = new AlertDialog.Builder(mContext);
        builer.setTitle("新版本升级");
        builer.setMessage(info.getDescription());

        downloadManagerUtil = new DownloadManagerUtil(mContext);

        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("更新", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //调起系统下载功能
                if (downloadId != 0) {
                    downloadManagerUtil.clearCurrentTask(downloadId);
                }
                downloadId = downloadManagerUtil.download(info.getUrl(), "iplantMES", info.getDescription());
            }
        });
        builer.setNegativeButton("取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "取消更新");
                //closeApp(mContext);
            }
        });
        AlertDialog dialog = builer.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        } else {
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(mContext, "下载失败", Toast.LENGTH_LONG).show();
            //closeApp(mContext);
        }
    };

}
