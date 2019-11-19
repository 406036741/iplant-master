package com.iplant.presenter.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.iplant.GudData;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.model.Group;
import com.iplant.model.ToolBox;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.Layout.DragLayout;
import com.iplant.presenter.view.adapter.ItemMainChildAdapter;
import com.iplant.presenter.view.service.RefreshService;
import com.iplant.presenter.view.widget.SystemDialog;
import com.iplant.presenter.view.widget.ToggleButton;
import com.iplant.util.ConfigUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.iplant.GudData.mPerson_judge;


/**
 * @author lildu
 * 主界面
 */
public class MainActivity extends BaseActivity {
    @Bind(R.id.back)
    TextView mBack;

    @Bind(R.id.tv_bartitle)
    TextView mTitle;

    @Bind(R.id.menu)
    ImageButton mMenu;

    // tab用参数
    private TabHost tabHost;
    private RadioGroup radiogroup;

    @Bind(R.id.tabgridView1)
    GridView mTabGrid1;

    @Bind(R.id.tabgridView2)
    GridView mTabGrid2;

    @Bind(R.id.tabgridView3)
    GridView mTabGrid3;

    @Bind(R.id.tabgridView4)
    GridView mTabGrid4;

    @Bind(R.id.tabgridView5)
    GridView mTabGrid5;

    @Bind(R.id.radio_1)
    RadioButton RadioButton1;

    @Bind(R.id.radio_2)
    RadioButton RadioButton2;

    @Bind(R.id.radio_3)
    RadioButton RadioButton3;

    @Bind(R.id.radio_4)
    RadioButton RadioButton4;

    @Bind(R.id.radio_5)
    RadioButton RadioButton5;

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

    ItemMainChildAdapter mChildAdapter1 = null;
    ItemMainChildAdapter mChildAdapter2 = null;
    ItemMainChildAdapter mChildAdapter3 = null;
    ItemMainChildAdapter mChildAdapter4 = null;
    ItemMainChildAdapter mChildAdapter5 = null;

    private DragLayout dl;

    private ToggleButton togglebutton;

    boolean shouldPlayBeep = true;
    List<Group> mGroupList;


    private Group mFavoriteGroup = new Group();

    Account myAccount;
    boolean mIsInitBottomMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mFavoriteGroup.groupType = 1;
        dl = (DragLayout) findViewById(R.id.dl);
        togglebutton = (ToggleButton) findViewById(R.id.tbtn_Persontask);


        initView();
        initDragLayout();


        new Thread() {
            @Override
            public void run() {
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
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      mName.setText("" + myAccount.name);
                                      mAccount.setText(myAccount.account);
                                      new GroupPresenter().update(true, myAccount.myID, myAccount.password);
                                      startService(new Intent(MainActivity.this, RefreshService.class));
                                  }
                              }
                );

            }

        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mPerson_judge == 0) {
                togglebutton.setToggleOff(true);
            }
            // 当按钮再次被点击时候响应的事件
            else {
                togglebutton.setToggleOn(true);
            }
            if (GudData.mShift_Status == -1) {
                radioButtonPreviousShift.setChecked(true);
            } else if (GudData.mShift_Status == 0) {
                radioButtonCurrentShift.setChecked(true);
            } else if (GudData.mShift_Status == 1) {
                radioButtonNextShift.setChecked(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDragLayout() {

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
                    new GroupPresenter().update(true, myAccount.myID, myAccount.password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        radioGroupShift.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
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
                    }

                }
        );

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        EventBus.getDefault().unregister(this);

     //   stopService(new Intent(this, RefreshService.class));
    }

    private void initView() {
        try {
            ButterKnife.bind(this);

            mBack.setVisibility(View.GONE);
            mMenu.setVisibility(View.VISIBLE);
            mMenu.setImageResource(R.drawable.user_l);


            radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
            tabHost = (TabHost) findViewById(android.R.id.tabhost);
            tabHost.setup();

            InitBottomMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void InitBottomMenu() {

        try {
            mGroupList = DBManage.queryAll(Group.class);
            for (int i = 0; i < mGroupList.size(); i++) {
                switch (i) {
                    case 0:
                        tabHost.addTab(tabHost.newTabSpec(mGroupList.get(i).groupID).setIndicator("")
                                .setContent(R.id.tab1));
                        tabHost.setCurrentTab(0);
                        mTitle.setText(mGroupList.get(i).groupName);
                        RadioButton1.setText(mGroupList.get(i).groupName);
                        break;
                    case 1:
                        tabHost.addTab(tabHost.newTabSpec(mGroupList.get(i).groupID).setIndicator("")
                                .setContent(R.id.tab2));
                        RadioButton2.setText(mGroupList.get(i).groupName);
                        break;
                    case 2:
                        tabHost.addTab(tabHost.newTabSpec(mGroupList.get(i).groupID).setIndicator("")
                                .setContent(R.id.tab3));
                        RadioButton3.setText(mGroupList.get(i).groupName);
                        break;
                    case 3:
                        tabHost.addTab(tabHost.newTabSpec(mGroupList.get(i).groupID).setIndicator("")
                                .setContent(R.id.tab4));
                        RadioButton4.setText(mGroupList.get(i).groupName);
                        break;
                    case 4:
                        tabHost.addTab(tabHost.newTabSpec(mGroupList.get(i).groupID).setIndicator("")
                                .setContent(R.id.tab5));
                        RadioButton5.setText(mGroupList.get(i).groupName);
                        break;
                }
                mIsInitBottomMenu = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                try {
                    int currentTab = tabHost.getCurrentTab();
                    Group wGroup;
                    RadioButton1.setTextColor(0xFF000000);
                    RadioButton2.setTextColor(0xFF000000);
                    RadioButton3.setTextColor(0xFF000000);
                    RadioButton4.setTextColor(0xFF000000);
                    RadioButton5.setTextColor(0xFF000000);
                    switch (checkedId) {
                        case R.id.radio_1:
                            wGroup = mGroupList.get(0);
                            tabHost.setCurrentTabByTag(wGroup.groupID);
                            //如果需要动画效果就使用
                            setCurrentTabWithAnim(currentTab, 0, wGroup.groupID);
                            mTitle.setText(wGroup.groupName);
                            RadioButton1.setTextColor(0xFF09bef6);
                            break;
                        case R.id.radio_2:
                            wGroup = mGroupList.get(1);
                            tabHost.setCurrentTabByTag(wGroup.groupID);
                            //如果需要动画效果就使用
                            setCurrentTabWithAnim(currentTab, 1, wGroup.groupID);
                            mTitle.setText(wGroup.groupName);
                            RadioButton2.setTextColor(0xFF09bef6);
                            break;
                        case R.id.radio_3:
                            wGroup = mGroupList.get(2);
                            tabHost.setCurrentTabByTag(wGroup.groupID);
                            //如果需要动画效果就使用
                            setCurrentTabWithAnim(currentTab, 2, wGroup.groupID);
                            mTitle.setText(wGroup.groupName);
                            RadioButton3.setTextColor(0xFF09bef6);
                            break;
                        case R.id.radio_4:
                            wGroup = mGroupList.get(3);
                            tabHost.setCurrentTabByTag(wGroup.groupID);
                            //如果需要动画效果就使用
                            setCurrentTabWithAnim(currentTab, 3, wGroup.groupID);
                            mTitle.setText(wGroup.groupName);
                            RadioButton4.setTextColor(0xFF09bef6);
                            break;

                        case R.id.radio_5:
                            wGroup = mGroupList.get(4);
                            tabHost.setCurrentTabByTag(wGroup.groupID);
                            //如果需要动画效果就使用
                            setCurrentTabWithAnim(currentTab, 4, wGroup.groupID);
                            mTitle.setText(wGroup.groupName);
                            RadioButton5.setTextColor(0xFF09bef6);
                            break;
                    }
                    // 刷新actionbar的menu
                    getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 这个方法是关键，用来判断动画滑动的方向
    private void setCurrentTabWithAnim(int now, int next, String tag) {
        if (now > next) {
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
            tabHost.setCurrentTabByTag(tag);
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
        } else {
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
            tabHost.setCurrentTabByTag(tag);
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
        }
    }


    @Subscribe
    public void onEventMainThread(GroupPresenter.GroupUpdateResult result) {
        if (!result.isValid()) {
//			new GroupPresenter().update();
        } else {
            //在adapter中刷新数据
            try {
                if (!mIsInitBottomMenu)
                    InitBottomMenu();

                mGroupList = DBManage.queryAll(Group.class);
                if (mGroupList != null) {
                    for (int i = 0; i < mGroupList.size(); i++) {
                        List<ToolBox> wToolBoxList = DBManage.queryListBy(ToolBox.class, "groupName", mGroupList.get(i).groupID);
                        switch (i) {
                            case 0:
                                if (mChildAdapter1 == null) {
                                    mChildAdapter1 = new ItemMainChildAdapter(this);
                                }
                                mTabGrid1.setAdapter(mChildAdapter1);
                                mChildAdapter1.setData(wToolBoxList);
                                break;
                            case 1:
                                if (mChildAdapter2 == null) {
                                    mChildAdapter2 = new ItemMainChildAdapter(this);
                                }
                                mTabGrid2.setAdapter(mChildAdapter2);
                                mChildAdapter2.setData(wToolBoxList);
                                break;
                            case 2:
                                if (mChildAdapter3 == null) {
                                    mChildAdapter3 = new ItemMainChildAdapter(this);
                                }
                                mTabGrid3.setAdapter(mChildAdapter3);
                                mChildAdapter3.setData(wToolBoxList);
                                break;
                            case 3:
                                if (mChildAdapter4 == null) {
                                    mChildAdapter4 = new ItemMainChildAdapter(this);
                                }
                                mTabGrid4.setAdapter(mChildAdapter4);
                                mChildAdapter4.setData(wToolBoxList);
                                break;
                            case 4:
                                if (mChildAdapter5 == null) {
                                    mChildAdapter5 = new ItemMainChildAdapter(this);
                                }
                                mTabGrid5.setAdapter(mChildAdapter5);
                                mChildAdapter5.setData(wToolBoxList);
                                break;
                            default:
                                break;
                        }
                    }

                }
                if (result.haveNewMsg > 0) {
                    PlayBeep();
                    SystemDialog.show(getApplicationContext(), "新消息提醒", String.format("您有%d条新消息！", result.haveNewMsg));
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void PlayBeep() {
        AudioManager audioService = (AudioManager) getSystemService(this.AUDIO_SERVICE);

        //判断是否为非静音模式
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            shouldPlayBeep = false;
        }

        MediaPlayer mediaPlayer = CreateMediaPlayer();

        if (shouldPlayBeep && mediaPlayer != null) {
            //1.开启蜂鸣器
            mediaPlayer.start();
        }

        try {
            //2.获得震动服务。
            Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
            //3.启动震动。
            //第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
            //   比如  [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
            //第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
            //会一直保持循环，我们需要用 vibrator.cancel()主动终止
            vibrator.vibrate(new long[]{500, 500, 500, 500}, -1);
        } catch (Exception ex) {
            Log.e("PlayBeep", ex.toString());
        }

    }

    private MediaPlayer CreateMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        //设定数据源，并准备播放
        AssetFileDescriptor file = getResources().openRawResourceFd(
                R.raw.pizzicato);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.prepare();
        } catch (IOException ioe) {

            mediaPlayer = null;
        }
        return mediaPlayer;
    }

    public void onMenu(View v) {
        jumpTo(UserActivity.class);
    }

    @Override
    public void onBackPressed() {

//        if (mChildMessageAdapter != null) {
//            mChildMessageAdapter.checkBackPress();
//        }
    }

}
