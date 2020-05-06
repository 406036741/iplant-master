package com.iplant.presenter.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
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
import com.iplant.model.LoginState;
import com.iplant.model.ToolBox;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.view.Layout.DragLayout;
import com.iplant.presenter.view.adapter.ItemMainChildAdapter;
import com.iplant.presenter.view.service.RefreshService;
import com.iplant.presenter.view.widget.ToggleButton;
import com.iplant.util.ConfigUtils;
import com.iplant.util.DesUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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


    List<Group> mGroupList;


    private Group mFavoriteGroup = new Group();

    Account myAccount;
    boolean mIsInitBottomMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        dl = (DragLayout) findViewById(R.id.dl);
        togglebutton = (ToggleButton) findViewById(R.id.tbtn_Persontask);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.SYSTEM_ALERT_WINDOW}, 1);

        if (GudData.mLoginState.equals(LoginState.Default))
        {
           // EmmManager.getInstance().init(this);//初始化中车门户做设备认证，安全接入，应用更新
            PwdLessLogin();
        }
        else if(GudData.mLoginState.equals(LoginState.Logout))
        {
            showMsg("账户已退出请重新登录");
            jumpTo(LoginActivity.class);
            finish();
        }

        initView();
        initDragLayout();

        try {
            String account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
            myAccount = DBManage.queryBy(Account.class, GudData.KEY_Account, account);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            mName.setText("" + myAccount.name);
            mAccount.setText(myAccount.account);
            new GroupPresenter().update(true, myAccount.encryptAccount, myAccount.encryptPwd);
            startService(new Intent(MainActivity.this, RefreshService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PwdLessLogin() {
        try {
            String  account="";
            //   account = AESTool.decryptString(getIntent().getStringExtra("name"));
            account = ConfigUtils.getString(getApplicationContext(), null, GudData.KEY_Account);
            String wToken=  DesUtil.CreateToken(account);

            new UserPresenter().login(account, "", "0",wToken);
            showWaiting("登陆中，请稍后...");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        try {
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
                        new GroupPresenter().update(true, myAccount.encryptAccount, myAccount.encryptPwd);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        try {
            super.onDestroy();

            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //   stopService(new Intent(this, RefreshService.class));
    }

    private void initView() {
        try {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
            if(mGroupList.size()<5)
                return;
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
        try {
            if (now > next) {
                tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                tabHost.setCurrentTabByTag(tag);
                tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
            } else {
                tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                tabHost.setCurrentTabByTag(tag);
                tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
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
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
