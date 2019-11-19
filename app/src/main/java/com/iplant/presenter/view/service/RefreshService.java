package com.iplant.presenter.view.service;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import com.iplant.GudData;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.UpdatePresenter;
import java.util.Timer;
import java.util.TimerTask;


public class RefreshService extends Service {
	private GroupPresenter mGroupPresenter;
	private UpdatePresenter mUpdatePresenter;

	private Timer mRefreshTimer;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mRefreshTimer = new Timer();
		mGroupPresenter = new GroupPresenter();
		mUpdatePresenter = new UpdatePresenter(this);
		
//		EventBus.getDefault().register(this);

		startRefresh();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
//		stopRefresh();
		Toast.makeText(this.getApplication(),"service is over",Toast.LENGTH_LONG);
//		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 开始刷新
	 */
	private void startRefresh(){
		try {
			mRefreshTimer.schedule(taskUpdate, GudData.TICKS_UPDATE_VERSION, GudData.TICKS_UPDATE_VERSION);
			mRefreshTimer.schedule(taskUser, GudData.TICKS_UPDATE_GROUP, GudData.TICKS_UPDATE_GROUP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭刷新
	 */
	private void stopRefresh(){
		mRefreshTimer.cancel();
	}
	
	//刷新首页数据
	TimerTask taskUser = new TimerTask() {
		
		@Override
		public void run() {
			try {
				mGroupPresenter.update(false,GudData.myAccount.myID, GudData.myAccount.password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	//检测更新
	TimerTask taskUpdate = new TimerTask() {
		
		@Override
		public void run() {
			try {
				mUpdatePresenter.doCheckUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
