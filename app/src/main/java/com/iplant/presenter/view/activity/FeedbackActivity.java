package com.iplant.presenter.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.model.QRResult;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.adapter.PicPickerAdapter;
import com.iplant.util.ConfigUtils;
import com.iplant.util.ImageFactory;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import butterknife.Bind;
import butterknife.ButterKnife;


public class FeedbackActivity extends BaseActivity {

    @Bind(R.id.tv_bartitle)
    TextView mTitle;

    @Bind(R.id.tv_sender)
    TextView mtv_sender;

    @Bind(R.id.et_feedback_SenderTel)
    TextView met_feedback_SenderTel;

    @Bind(R.id.et_feedback_theme)
    TextView met_feedback_theme;

    @Bind(R.id.et_feedback_content)
    TextView met_feedback_content;

    @Bind(R.id.btn_feedback_Submit)
    TextView mbtn_feedback_Submit;


    private GridView mgridView;            //网格显示缩略图
    private Bitmap mbmp;//导入临时图片
    List<Map<String, Object>> mData;
    PicPickerAdapter msimpleadapter;
    String mCameraFilePath = null;
    public static final int REQUEST_SELECT_FILE = 20000;
    public static final int REQUEST_SELECT_CAPTURE = 20001;
    String mImgPath = null;
    String mPath_Selected = null;//选择图片路径
    Account myAccount;

    List<String> mCompressPicPath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initView();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET

        }, 1);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();


    }


    private void initView() {
        ButterKnife.bind(this);

        mTitle.setText("写邮件");

        String account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
        try {

            myAccount = DBManage.queryBy(Account.class, "account", account);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mtv_sender.setText(myAccount.name);

        mImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.pathSeparator + "iplant" + File.pathSeparator;
        new File(mImgPath).mkdirs();
          /*
         * 防止键盘挡住输入框
         * 不希望遮挡设置activity属性 android:windowSoftInputMode="adjustPan"
         * 希望动态调整高度 android:windowSoftInputMode="adjustResize"
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.
                SOFT_INPUT_ADJUST_PAN);



        //获取控件对象
        mgridView = (GridView) findViewById(R.id.gridView1);


        mData = new ArrayList<Map<String, Object>>();

        Map<String, Object> mapimage = new HashMap<String, Object>();
        mbmp = BitmapFactory.decodeResource(getResources(), R.drawable.addpic);
        mapimage.put("iamge_item", mbmp);
        mData.add(mapimage);

        msimpleadapter = new PicPickerAdapter(this, mData, R.layout.picturepicker_item, new String[]{"iamge_item"}, new int[]{R.id.iv_PicPickitem});


        mgridView.setAdapter(msimpleadapter);


        mgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View mgridView, int position, long id) {
                if (mgridView.getId() == 0) { //点击图片位置为+ 0对应0张图片
                    showImagePick();
                } else {
                    dialog(position);
                }
            }
        });
    }

    /*
   * Dialog对话框提示用户删除操作
   * position为删除图片位置
   */
    protected void dialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认移除已添加图片吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mData.remove(position);
                if(mCompressPicPath.size()>0)
                {
                    mCompressPicPath.remove(position-1);
                }
                msimpleadapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mCameraFilePath = mImgPath + System.currentTimeMillis() + ".jpg";
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
                        startActivityForResult(intent, REQUEST_SELECT_CAPTURE);
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
        });


        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
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
        } //从文件夹选择图片
        else if (requestCode == REQUEST_SELECT_FILE && data != null) {

            Uri uri = data.getData();
            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";

            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                new ImageFactory().compressAndGenImage(photo, outFile, 500);
                mPath_Selected = outFile;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } // 拍照
        else if (requestCode == REQUEST_SELECT_CAPTURE) {

            String outFile = mImgPath + System.currentTimeMillis() + ".jpg";
            boolean isExist = new File(outFile).exists();
            try {
                new ImageFactory().compressAndGenImage(mCameraFilePath, outFile, 500, true);
                mPath_Selected = outFile;
            } catch (IOException e) {
                e.printStackTrace();

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    //刷新图片
    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mPath_Selected)) {
            if (!mCompressPicPath.contains(mPath_Selected)) {
                mCompressPicPath.add(mPath_Selected);
            }

            Bitmap addbmp = BitmapFactory.decodeFile(mPath_Selected);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("iamge_item", addbmp);
            mData.add(map);
            msimpleadapter.notifyDataSetChanged();

            //刷新后释放防止手机休眠后自动添加
            mPath_Selected = null;
        }
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            closeWaiting();
            mbtn_feedback_Submit.setClickable(true);
            if ((boolean) msg.obj) {
                showMsg("邮件发送成功");
                finish();
            }
            else
            {
                showMsg("邮件发送失败,请检查网络是否正常");
            }

        }
    };

    public void OnSubmit(View view) {

        if (TextUtils.isEmpty(met_feedback_SenderTel.getText().toString().trim())) {
            showMsg("联系人电话不能为空");
            return;
        }
        if (TextUtils.isEmpty(met_feedback_theme.getText().toString().trim())) {
            showMsg("邮件主题不能为空");
            return;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean wIsSentSucceed = false;
                mbtn_feedback_Submit.setClickable(false);
                try {

                    Authenticator authenticator = new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("m13554399617@163.com", "00oooo00");
                        }
                    };
                    //属性
                    Properties props = new Properties();
                    props.setProperty("mail.smtp.host", "smtp.163.com");
                    props.put("mail.smtp.auth", "true");
                    Session session = Session.getDefaultInstance(props, authenticator);

                    //构建Mime消息
                    MimeMessage message = new MimeMessage(session);
                    //设置消息内容

                    message.setFrom(new InternetAddress("m13554399617@163.com"));//发送人邮箱
                    message.setSubject(met_feedback_theme.getText().toString());
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress("m13554399617@163.com"));//接收人邮箱


                    //1、文本
                    MimeBodyPart body_text = new MimeBodyPart();
                    String wContent = met_feedback_content.getText().toString();
                    wContent += "<br>";
                    MimeMultipart relatedMultipart = new MimeMultipart();
                    relatedMultipart.addBodyPart(body_text);
                    for (int i = 0; i < mCompressPicPath.size(); i++) {
                        wContent += String.format("<img src='cid:a%d'>", i);
                        //2、图片
                        MimeBodyPart body_pic = new MimeBodyPart();
                        DataHandler picDataHandler = new DataHandler(new FileDataSource(new File(mCompressPicPath.get(i))));
                        body_pic.setDataHandler(picDataHandler);
                        body_pic.setContentID("a" + i);//和html链接的cid一致
                        relatedMultipart.addBodyPart(body_pic);
                    }

                    wContent += "\n 反馈者:" + myAccount.name + "(UTE) 联系人电话:" + met_feedback_SenderTel.getText().toString().trim();
                    body_text.setContent(wContent, "text/html;charset=utf-8");

                    //3、文本和图片关系


                    relatedMultipart.setSubType("related");

                    //将文本和图片关系封装为body一部分
                    MimeBodyPart contentPart = new MimeBodyPart();
                    contentPart.setContent(relatedMultipart);

                    //5、整合上述部分
                    MimeMultipart mixedMultipart = new MimeMultipart();
                    mixedMultipart.addBodyPart(contentPart);//正文内容
                    mixedMultipart.setSubType("mixed");

                    message.setContent(mixedMultipart);
                    //发送
                    Transport.send(message);
                    wIsSentSucceed = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                android.os.Message msg = new android.os.Message();
                msg.obj = wIsSentSucceed;
                mHandler.sendMessage(msg);

            }
        }).start();

        showWaiting("邮件发送中，请稍后...");
    }


}
