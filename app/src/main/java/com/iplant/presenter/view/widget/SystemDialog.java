package com.iplant.presenter.view.widget;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.iplant.GudData;
import com.iplant.R;
import com.iplant.model.LoginState;

import static android.content.Context.NOTIFICATION_SERVICE;


public class SystemDialog {
    Context mContext;
    int mUnreadCount;

    public SystemDialog(Context wContext) {
        mContext = wContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

//            channelId = "subscribe";
//            channelName = "订阅消息";
//            importance = NotificationManager.IMPORTANCE_DEFAULT;
//            createNotificationChannel(channelId, channelName, importance);
        }
    }


    public void show(String title, String msg, int wUnreadCount) {
//==============弹框====================
// 			AlertDialog dialog = new AlertDialog.Builder(mContext).setPositiveButton("知道了", null).create();
//			dialog.setMessage(msg);
//			dialog.setTitle(title);
//			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//			dialog.show();
        mUnreadCount = wUnreadCount;

        //=================调用系统通知栏====================
        NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel("chat");
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                mContext.startActivity(intent);
                Toast.makeText(mContext, "请手动将通知打开", Toast.LENGTH_SHORT).show();
            }

        }


        Notification notification = new NotificationCompat.Builder(mContext, "chat")
                .setContentTitle(title)
                .setTicker("收到MES通知")//通知首次出现在通知栏，带上升动画效果的
                .setAutoCancel(true)//点击后消失
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)//设置通知铃声和震动
                .setContentIntent(createIntent(msg))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(msg)
                .setNumber(wUnreadCount)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.iplant_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.iplant_launcher))
                .setAutoCancel(true)

                .build();
        manager.notify(1, notification);
    }

    public PendingIntent createIntent(String wContent) {
        Intent intent;
        PendingIntent contentIntent = null;
        Bundle mBundle = new Bundle();
        intent = new Intent();
        String content = wContent;
        mBundle.putString("content", content);
        intent.putExtras(mBundle);
        intent.setClass(mContext, com.iplant.presenter.view.activity.MainActivity.class);
        contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //intent.setClass(mContext, NotifyClickReceiver.class);
        //intent.setAction("com.dianping.kmm.receiver.click.notify");
        //contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return contentIntent;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        if(GudData.mLoginState.equals(LoginState.Default))
        {
            GudData.mLoginState=LoginState.Logout;
        }
    }
}
